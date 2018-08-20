package co.rxstack.ml.tensorflow.service.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_64FC1;
import static org.bytedeco.javacpp.opencv_ml.ROW_SAMPLE;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.model.Identity;
import co.rxstack.ml.faces.service.IFaceService;
import co.rxstack.ml.faces.service.IIdentityService;
import co.rxstack.ml.tensorflow.TensorFlowResult;
import co.rxstack.ml.tensorflow.config.FaceNetConfig;
import co.rxstack.ml.tensorflow.exception.GraphLoadingException;
import co.rxstack.ml.tensorflow.service.IFaceNetService;
import co.rxstack.ml.tensorflow.utils.GraphUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_ml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import smile.classification.NeuralNetwork;
import smile.classification.RBFNetwork;
import smile.classification.SVM;
import smile.math.kernel.GaussianKernel;
import smile.math.kernel.LinearKernel;
import smile.math.rbf.RadialBasisFunction;
import smile.util.SmileUtils;

@Component
public class FaceNetService implements IFaceNetService {

	private static final Logger log = LoggerFactory.getLogger(FaceNetService.class);
	private static final String EMBEDDINGS_FILE_K_V_DELIMITER = ","; // be careful must be a comma
	private static final DistanceMeasure DISTANCE_MEASURE = new EuclideanDistance();

	private Session session;
	private Graph faceNetTensorGraph;
	private IFaceService faceService;
	private IIdentityService identityService;
	private FaceNetConfig faceNetConfig;

	private SVM<double[]> classifier;
	private opencv_ml.SVM svmsgd;
	// private BiMap<Integer, Integer> faceIdentityBiMap = HashBiMap.create();
	private BiMap<Integer, Integer> identityClassLabelsMap = Maps.synchronizedBiMap(HashBiMap.create());

	private AtomicInteger labelsCountAtomic = new AtomicInteger(0);

	private ConcurrentHashMap<Integer, double[]> embeddings = new ConcurrentHashMap<>();


	@Autowired
	public FaceNetService(IFaceService faceService, IIdentityService identityService, FaceNetConfig faceNetConfig) throws GraphLoadingException {
		Preconditions.checkNotNull(faceNetConfig);
		Preconditions.checkNotNull(faceService);
		Preconditions.checkNotNull(identityService);

		this.faceNetConfig = faceNetConfig;
		this.faceService = faceService;
		this.identityService = identityService;

		Path faceNetGraphPath = Paths.get(faceNetConfig.getFaceNetGraphPath());

		if (!faceNetGraphPath.toFile().exists()) {
			log.warn("No ProtoBuffer graph found at {}", faceNetGraphPath.toAbsolutePath());
			throw new GraphLoadingException(faceNetGraphPath.toAbsolutePath().toString());
		}

		CompletableFuture.runAsync(() -> {
			log.info("Loading FaceNet TF graph asynchronously");
			Stopwatch stopwatch = Stopwatch.createStarted();
			byte[] graphDef = GraphUtils.readAllBytes(faceNetGraphPath);
			if (graphDef != null) {
				faceNetTensorGraph = new Graph();
				faceNetTensorGraph.importGraphDef(graphDef);
				log.info("FaceNet TF graph loaded successfully in {}ms", stopwatch.elapsed(MILLISECONDS));
			} else {
				log.error("Failed to load FaceNet TF graph!");
			}
			session = new Session(faceNetTensorGraph);
			loadEmbeddingsVector();
			trainClassifier();
		});
	}

	@Deprecated
	@Override
	public void saveEmbeddings(ConcurrentMap<Integer, double[]> embeddings) throws FileNotFoundException {
		log.info("Saving a total of {} embeddings to disk at {}", embeddings.size(),
			faceNetConfig.getEmbeddingsFilePath());
		PrintWriter printWriter = new PrintWriter(new File(faceNetConfig.getEmbeddingsFilePath()));
		embeddings.keySet().forEach(s -> 
			printWriter.printf("%d%s%s%n", s.intValue(), EMBEDDINGS_FILE_K_V_DELIMITER, Arrays.toString(embeddings.get(s))
				.replace("[", "").replace("]", "")));
		printWriter.close();
	}


	@Override
	public void saveFaces(List<Face> faceList) throws FileNotFoundException {
		log.info("Saving embeddings for identity file to disk");
		PrintWriter printWriter = new PrintWriter(new File(faceNetConfig.getEmbeddingsFilePath()));
		faceList.forEach(face ->
			printWriter.printf("%d%s%s%n", face.getIdentity().getId(), EMBEDDINGS_FILE_K_V_DELIMITER,
				Arrays.toString(face.getEmbeddingsVector())
				.replace("[", "").replace("]", "").trim()));
		printWriter.close();
	}

	@Override
	public void loadEmbeddingsVector() {
		log.info("Loading embeddings vectors from DB");

		Schedulers.computation().createWorker().schedulePeriodically(() -> {
			log.info("Refreshing local face data");
			// this.faceIdentityBiMap.putAll(faceService.findFaceIdentityBiMap());
			this.embeddings.putAll(faceService.findAllFaceIdEmbeddingsMap());
		}, 10, 15, TimeUnit.SECONDS);

		// TODO be careful Observables
		// TODO handle canceling Subscription
		/*faceService.getRefreshingFacesObservable()
			.subscribe(signal -> {
				log.info("Intercepted Faces refreshed signal");
			});

		CompletableFuture.runAsync(() -> {
			try {
				//this.saveEmbeddings(embeddings);
				this.saveFaces(faceService.findAll());
			} catch (FileNotFoundException e) {
				log.error(e.getMessage(), e);
			}
		});*/
	}

	@Override
	public void loadEmbeddingsVectorFromFile() {
		log.info("Loading embeddings file to memory");
		CompletableFuture.runAsync(() -> {
			try {
				File embeddingsFile = new File(faceNetConfig.getEmbeddingsFilePath());
				if (embeddingsFile.isFile() && embeddingsFile.exists()) {
					BufferedReader reader = new BufferedReader(new FileReader(embeddingsFile));
					reader.lines().forEach(line -> {
						String[] ee = line.split(EMBEDDINGS_FILE_K_V_DELIMITER);
						double[] vector = new double[faceNetConfig.getFeatureVectorSize()];
						for (int i = 1; i < faceNetConfig.getFeatureVectorSize() + 1; i++) {
							vector[i - 1] = Float.valueOf(ee[i]);
						}
						embeddings.put(Integer.valueOf(ee[0]), vector);
					});
				} else {
					log.warn("No existing embeddings file found for loading!");
				}
			} catch (FileNotFoundException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException("Failed to load embeddings file not found");
			}
		});
	}
	
	@Override
	public double[] computeEmbeddingsFeaturesVector(BufferedImage bufferedImage) {
		log.info("Computing embeddings feature vector");

		try (Tensor<Float> image = Tensors.create(imageTo4DTensor(bufferedImage))) {
			float[] embeddingsArray = new float[faceNetConfig.getFeatureVectorSize()];
			Stopwatch stopwatch = Stopwatch.createStarted();
			Tensor<Float> result = session.runner()
				.feed("input:0", image)
				.feed("phase_train:0", Tensors.create(false))
				.fetch("embeddings:0")
				.run()
				.get(0)
				.expect(Float.class);

			log.debug("TF result type: {}, shape: {}, dim: {}, elements: {}", result.dataType().name(),
				result.shape(),
				result.numDimensions(), result.numElements());

			result.writeTo(FloatBuffer.wrap(embeddingsArray));

			log.info("Embeddings computation completed in {}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
				return toDoubleArray(embeddingsArray);
			} catch (Exception e) {
			log.error(e.getMessage(), e);
			}
		return null;
	}

	@Override
	public Optional<TensorFlowResult> computeDistance(double[] vector) {
		return computeDistance(vector, DEFAULT_THRESHOLD);
	}

	@Override
	public Optional<TensorFlowResult> computeDistance(double[] vector, double threshold) {

		log.info("Computing distance with available vectors");

		if (vector.length == faceNetConfig.getFeatureVectorSize()) {
			Stopwatch stopwatch = Stopwatch.createStarted();
			log.info("Executing Classifier [predict] operation");

			double[] confidences = new double[labelsCountAtomic.get()];

			int classLabel = classifier.predict(vector, confidences);
			double confidence = Arrays.stream(confidences).max().orElse(0) * 100;

			float sgdPredictedValue = svmsgd.predict(new opencv_core.Mat(Floats.toArray(Doubles.asList(vector))).reshape(0, 1));
			log.info("-------------------------------> {}", sgdPredictedValue);

			log.info("Confidences {}", Arrays.toString(confidences));

			TensorFlowResult tensorFlowResult= null;

			try {
				log.info("Predicted class {} in {}ms with confidence {}",
					classLabel, stopwatch.elapsed(MILLISECONDS), confidence);
				int matchedIdentity = identityClassLabelsMap.inverse().getOrDefault(classLabel, -1);
				int sgdMatchedIdentity = identityClassLabelsMap.inverse().getOrDefault((int)sgdPredictedValue, -1);

				if (sgdMatchedIdentity != -1) {
					log.info("SGD Matched identity {}", identityService.findById(sgdMatchedIdentity).map(Identity::getName).orElse("Unknown!"));
				}

				if (matchedIdentity != -1) {
					log.info("Matched Identity Id {}", matchedIdentity);
					tensorFlowResult = new TensorFlowResult(
						identityService.findById(matchedIdentity)
							.map(Identity::getName).orElse("Unknown"),
						confidence);
				}
				else {
					log.info("No matching Identity Id from DB found");
				}

				return Optional.ofNullable(tensorFlowResult);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		return Optional.empty();
	}

	private void trainClassifier() {
		Schedulers.computation().createWorker().schedulePeriodically(() -> {
			try {
				log.info("-----> Started Classifier training...");

				Multimap<Integer, double[]> embeddingsByIdentity = faceService.findAllEmbeddingsForIdentity();

				if (embeddingsByIdentity.isEmpty()) {
					log.info("No embeddings found in the database for training");
					return;
				}

				Supplier<Stream<double[]>> streamSupplier = asFilteredStream(embeddingsByIdentity);

				int vectorsCount = (int) streamSupplier.get()
					.filter(v -> v.length == faceNetConfig.getFeatureVectorSize())
					.count();

				double[][] features = new double[vectorsCount][];
				int[] labels = new int[features.length];

				int index = 0;
				int labelsIndex = 0;

				// fixme not very safe
				identityClassLabelsMap.clear();

				for (Map.Entry<Integer, double[]> entry : embeddingsByIdentity.entries()) {
					if (entry.getValue().length == faceNetConfig.getFeatureVectorSize()) {
						if (!identityClassLabelsMap.containsKey(entry.getKey())) {
							identityClassLabelsMap.put(entry.getKey(), labelsIndex);
							labelsIndex++;
						}
						features[index] = entry.getValue();
						labels[index] = identityClassLabelsMap.get(entry.getKey());
						index++;
					}
				}

				/* use with SVM labelsCountAtomic.set(labelsIndex);*/

				labelsCountAtomic.set(labelsIndex);

				log.info("Labels: {}", Arrays.toString(labels));

				/*classifier = KNN.learn(features, labels, 3);*/

				classifier = makeSVMClassifier(labelsCountAtomic.get());
				classifier.learn(features, labels);
				classifier.finish();
				classifier.trainPlattScaling(features, labels);

				svmsgd = svmsgd(features, labels);

				/*classifier = makeNeuralNetwork(faceNetConfig.getFeatureVectorSize(), labelsIndex);
				classifier.setLearningRate(0.01);
				classifier.learn(features, labels);
				for (int i = 0; i < 100; i++) {
					if (i % 5 == 0)
						log.info("-----> Epoch {} running", i);
					classifier.learn(features, labels);
				}*/

				log.info("-----> Ending Classifier training...");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}, 10, 120, TimeUnit.SECONDS);
	}


	@PreDestroy
	public void onDestroy() {
		log.info("PreDestroy() fired, releasing TF Session, and Graph");
		session.close();
		faceNetTensorGraph.close();
	}

	private NeuralNetwork makeNeuralNetwork(int featureCount, int labelsCount) {
		return new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY,
			NeuralNetwork.ActivationFunction.SOFTMAX, featureCount, 512, 1000, 512, 50, labelsCount);
	}

	private SVM<double[]> makeSVMClassifier(int k) {
		// return new SVM<>(new GaussianKernel(1.0), 1.0, 3, SVM.Multiclass.ONE_VS_ALL);
		return new SVM<>(new LinearKernel(), 1.0, k, SVM.Multiclass.ONE_VS_ONE);
	}

	public RBFNetwork<double[]> makeRBfNetwork(double[][] data, int[] label) {
		double[][] centers = new double[3][];
		RadialBasisFunction basis = SmileUtils.learnGaussianRadialBasis(data, centers);
		return new RBFNetwork(data, label, new smile.math.distance.EuclideanDistance(), basis, centers);
	}

	private double[] toDoubleArray(Integer[] e) {
		return Arrays.stream(e).mapToDouble(value -> e[value]).toArray();
	}

	protected opencv_ml.SVM svmsgd(double[][] features, int[] labels) {

		opencv_core.Mat trainingDataMat = new opencv_core.Mat();

		opencv_core.Mat labelsMatrix = new opencv_core.Mat(features.length, 1, CV_32SC1 );
		labelsMatrix.put(new opencv_core.Mat(labels).reshape(0, features.length));

		for (int i=0; i < features.length; i++) {
			float[] v = Floats.toArray(Doubles.asList(features[i]));
			opencv_core.Mat vm = new opencv_core.Mat(v);
			vm = vm.reshape(0, 1);

			trainingDataMat.push_back(vm);
		}

		// trainingDataMat = trainingDataMat.reshape(0, 128);

		log.info("-------------------------------------> training data shape [{}, {}]", trainingDataMat.rows(), trainingDataMat.cols());
		log.info("-------------------------------------> labels shape [{}, {}]", labelsMatrix.rows(), labelsMatrix.cols());

		opencv_ml.SVM svm = opencv_ml.SVM.create();
		svm.setKernel(opencv_ml.SVM.LINEAR);
		svm.setType(opencv_ml.SVM.C_SVC);
		opencv_core.TermCriteria criteria = new opencv_core.TermCriteria(opencv_core.TermCriteria.EPS + opencv_core.TermCriteria.MAX_ITER, 1000, 0);
		svm.setTermCriteria(criteria);
		svm.setGamma(0.5);
		svm.setNu(0.5);
		svm.set
		svm.setC(1);
		boolean success = svm.train(trainingDataMat, ROW_SAMPLE, labelsMatrix);

		if (success) {
			log.info("SVMSGD trained successfully!");
		}
		return svm;
	}

	protected double[] toDoubleArray(float[] e) {
		return IntStream.range(0, e.length).mapToDouble(value -> e[value]).toArray();
	}

	private float[][][][] imageTo4DTensor(BufferedImage bi) {
		int height = bi.getHeight();
		int width = bi.getWidth();
		int depth = 3;
		float[][][][] image = new float[1][width][height][depth];
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				int rgb = bi.getRGB(i, j);
				Color color = new Color(rgb);
				image[0][i][j][0] = color.getRed();
				image[0][i][j][1] = color.getGreen();
				image[0][i][j][2] = color.getBlue();
			}
		}
		return image;
	}

	private Supplier<Stream<double[]>> asFilteredStream(Multimap<Integer, double[]> multimap) {
		return () -> multimap.values().stream()
			.filter(v -> v.length == faceNetConfig.getFeatureVectorSize());
	}

}
