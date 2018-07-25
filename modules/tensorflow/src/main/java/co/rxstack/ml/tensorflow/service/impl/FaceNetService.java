package co.rxstack.ml.tensorflow.service.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.annotation.PreDestroy;

import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.service.IFaceService;
import co.rxstack.ml.tensorflow.TensorFlowResult;
import co.rxstack.ml.tensorflow.config.FaceNetConfig;
import co.rxstack.ml.tensorflow.exception.GraphLoadingException;
import co.rxstack.ml.tensorflow.service.IFaceNetService;
import co.rxstack.ml.tensorflow.utils.GraphUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

@Component
public class FaceNetService implements IFaceNetService {

	private static final Logger log = LoggerFactory.getLogger(FaceNetService.class);
	private static final String EMBEDDINGS_FILE_K_V_DELIMITER = ","; // be careful must be a comma
	private static final DistanceMeasure DISTANCE_MEASURE = new EuclideanDistance();

	private Session session;
	private Graph faceNetTensorGraph;
	private IFaceService faceService;
	private FaceNetConfig faceNetConfig;

	private ConcurrentHashMap<Integer, double[]> embeddings = new ConcurrentHashMap<>();

	@Autowired
	public FaceNetService(IFaceService faceService, FaceNetConfig faceNetConfig) throws GraphLoadingException {
		Preconditions.checkNotNull(faceNetConfig);
		Preconditions.checkNotNull(faceService);
		this.faceNetConfig = faceNetConfig;
		this.faceService = faceService;

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

		// TODO be careful Observables
		faceService.getRefreshingFacesObservable()
			.subscribe(signal -> {
				log.info("Intercepted Faces refreshed signal");
				this.embeddings.putAll(faceService.findAllEmbeddings());
			});

		CompletableFuture.runAsync(() -> {
			try {
				//this.saveEmbeddings(embeddings);
				this.saveFaces(faceService.findAll());
			} catch (FileNotFoundException e) {
				log.error(e.getMessage(), e);
			}
		});
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
		return new double[] {};
	}

	@Override
	public Optional<TensorFlowResult> computeDistance(double[] vector) {
		return computeDistance(vector, DEFAULT_THRESHOLD);
	}

	@Override
	public Optional<TensorFlowResult> computeDistance(double[] vector, double threshold) {

		log.info("Computing distance with available vectors");
		Map<Double, Integer> resultsVector = Maps.newHashMap();
		
		if (vector.length == faceNetConfig.getFeatureVectorSize()) {
			log.debug("Vector length is {} as supposed!", faceNetConfig.getFeatureVectorSize());

			log.debug("Current Embeddings vectors: {} ", embeddings.size());

			embeddings.keySet().forEach(label -> {
				double[] embVector = embeddings.get(label);
				if (embVector.length == faceNetConfig.getFeatureVectorSize()) {
					double d = DISTANCE_MEASURE.compute(embVector, vector);
					resultsVector.put(d, label);
				}
			});

			Optional<Double> aDouble = resultsVector.keySet().stream().min(Comparator.naturalOrder());

			if (aDouble.isPresent()) {
				TensorFlowResult result =
					new TensorFlowResult(resultsVector.get(aDouble.get()), Math.round((1 - aDouble.get()) * 100));
				return Optional.of(result);
			}
		}

		return Optional.empty();
	}

	@PreDestroy
	public void onDestroy() {
		log.info("PreDestroy() fired, releasing TF Session, and Graph");
		session.close();
		faceNetTensorGraph.close();
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

}
