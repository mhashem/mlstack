package co.rxstack.ml.tensorflow;

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_STORAGE_WRITE;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import co.rxstack.ml.tensorflow.experimental.FaceNetLabeling;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_ml;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.Tensors;
import org.tensorflow.types.UInt8;

public class FaceNetService {

	private static Graph tensorflowGraph;

	public static void main(String[] args) throws IOException {

		// TODO remove

		String modelDir = args[0];
		String imageFile = args[1];
		String imageDir = args[2];

		String graphFile = "20170511-185253.pb";
		String labelsFile = "imagenet_comp_graph_label_strings.txt";

		byte[] graphDef = readAllBytesOrExit(Paths.get(modelDir, graphFile));
		List<String> labels = readAllLinesOrExit(Paths.get(modelDir, labelsFile));
		BufferedImage testImage = readBufImage(Paths.get(imageFile));

		Map<String, BufferedImage> nameBufferedImageMap = readBufferedImages(Paths.get(imageDir));
		Map<String, float[]> embeddings = Maps.newHashMap();

		tensorflowGraph = new Graph();
		tensorflowGraph.importGraphDef(graphDef);

		embeddings.putAll(loadEmbeddings());

		for (String name : nameBufferedImageMap.keySet()) {
			float[] embeddingsArray = computeEmbeddings(nameBufferedImageMap.get(name));
			embeddings.put(name, embeddingsArray);
		}

		opencv_core.Mat classes = new opencv_core.Mat();
		opencv_core.Mat trainingData = new opencv_core.Mat();

		opencv_core.Mat trainingImages = new opencv_core.Mat();
		Vector<Integer> trainingLabels = new Vector<Integer>();

		final int[] i = { 0 };

		embeddings.keySet().forEach(label -> {
			opencv_core.Mat featuresArrayMat = new opencv_core.Mat(embeddings.get(label));
			featuresArrayMat = featuresArrayMat.reshape(0, 1);

			trainingImages.push_back(featuresArrayMat);
			trainingLabels.add(label.hashCode());

			System.out.println(label + " -> " + label.hashCode());

			i[0]++;
		});

		trainingImages.copyTo(trainingData);
		trainingData.convertTo(trainingData, CV_32FC1);

		int labelss[] = new int[trainingLabels.size()];
		for(int ii=0;ii < trainingLabels.size();++ii)
			labelss[ii] = trainingLabels.get(ii).intValue();
		new opencv_core.Mat(labelss).copyTo(classes);

		opencv_core.CvTermCriteria cvTermCriteria =
			new opencv_core.CvTermCriteria(opencv_core.CV_TERMCRIT_ITER, 100, 0.0001);

		opencv_ml.SVM svmClassifier = opencv_ml.SVM.create();
		svmClassifier.setType(opencv_ml.SVM.C_SVC);
		svmClassifier.setKernel(opencv_ml.SVM.RBF);
		svmClassifier.setTermCriteria(cvTermCriteria.asTermCriteria());

		opencv_ml.TrainData trainData =
			new opencv_ml.TrainData(trainingData);

		System.out.println("h " + trainingData.size().height() + " - w " + trainingData.size().width());
		System.out.println("h " + classes.size().height() + " - w " + classes.size().width());

		svmClassifier.train(trainingData, opencv_ml.ROW_SAMPLE, classes);

		opencv_core.CvFileStorage fsTo = opencv_core.CvFileStorage
			.open("C:/etc/mlstack/output/svm.xml", opencv_core.CvMemStorage.create(), CV_STORAGE_WRITE);
		opencv_core.FileStorage fsto2=new opencv_core.FileStorage(fsTo);
		svmClassifier.write(fsto2);



		EuclideanDistance distance = new EuclideanDistance();

		double[] testFloatVector = toDoubleArray(computeEmbeddings(testImage));
		Map<Double, String> resultsVector = Maps.newHashMap();

		float predict = svmClassifier.predict(new opencv_core.Mat(computeEmbeddings(testImage)).reshape(0, 1));
		System.out.println("SVM: prediction " + predict);

		embeddings.keySet().forEach(label -> {
			double d = distance.compute(toDoubleArray(embeddings.get(label)), testFloatVector);
			resultsVector.put(d, label);
		});

		List<Double> collect = resultsVector.keySet().stream().sorted().collect(Collectors.toList());
		collect.stream().forEach(aDouble -> {
			System.out
				.println(resultsVector.get(aDouble) + " with confidence " + Math.round((1 - aDouble) * 100) + "%");
		});
		System.out.println();
		Double aDouble = resultsVector.keySet().stream().min(Comparator.naturalOrder()).get();
		System.out.println(resultsVector.get(aDouble) + " with confidence " + Math.round((1 - aDouble) * 100) + "%");

		tensorflowGraph.close();
	}

	private static void saveEmbeddings(Map<String, float[]> embeddings) throws FileNotFoundException {
		PrintWriter printWriter = new PrintWriter(new File("C:/etc/mlstack/output/embeddings/emb.csv"));
		for (String s : embeddings.keySet()) {
			printWriter.printf("%s,%s\n", s, Arrays.toString(embeddings.get(s)).replace("[", "").replace("]", ""));
		}
		printWriter.close();
	}

	public static Map<String, float[]> loadEmbeddings() throws FileNotFoundException {
		BufferedReader reader =
			new BufferedReader(new FileReader(new File("C:/etc/mlstack/output/embeddings/emb.csv")));
		return reader.lines().map(line -> {
			String[] ee = line.split(",");

			Holder h = new Holder();
			h.name = ee[0];
			float[] vector = new float[128];

			for (int i = 1; i < 129; i++) {
				vector[i - 1] = Float.valueOf(ee[i]);
			}

			h.embeddings = vector;
			return h;
		}).collect(Collectors.toMap(o -> o.name, o -> o.embeddings));
	}

	public static void writeDat(String filename, double[] x, double[] y, int xprecision, int yprecision)
		throws IOException {
		assert x.length == y.length;
		PrintWriter out = new PrintWriter(filename);
		for (int i = 0; i < x.length; i++)
			out.printf("%." + xprecision + "g\t%." + yprecision + "g\n", x[i], y[i]);
		out.close();
	}

	public static double[] toDoubleArray(float[] e) {
		double[] vector = new double[e.length];
		for (int i = 0; i < e.length; i++) {
			vector[i] = e[i];
		}
		return vector;
	}

	public static float[] computeEmbeddings(BufferedImage bufferedImage) {
		try (Tensor<Float> image = Tensors.create(imageToMultiArray(bufferedImage))) {
			try (Session s = new Session(tensorflowGraph)) {
				float[] embeddings = new float[128];;
				Stopwatch stopwatch = Stopwatch.createStarted();
				Tensor<Float> result =
					s.runner()
						.feed("input:0", image)
						.feed("phase_train:0", Tensors.create(false))
						.fetch("embeddings:0").run().get(0)
						.expect(Float.class);
				result.writeTo(FloatBuffer.wrap(embeddings));
				return embeddings;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private static byte[] readAllBytesOrExit(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			System.err.println("Failed to read [" + path + "]: " + e.getMessage());
			System.exit(1);
		}
		return null;
	}

	private static class Holder {
		String name;
		BufferedImage image;
		float[] embeddings;
	}

	private static BufferedImage readBufImage(Path imagePath) throws IOException {
		return ImageIO.read(imagePath.toFile());
	}

	private static Map<String, BufferedImage> readBufferedImages(Path dir) {
		return Arrays.stream(dir.toFile().listFiles()).map(file -> {
			try {
				Holder holder = new Holder();
				holder.name = FilenameUtils.getBaseName(file.getAbsolutePath());
				holder.image = ImageIO.read(file);
				return holder;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toMap(o -> o.name, o -> o.image));
	}

	private static List<String> readAllLinesOrExit(Path path) {
		try {
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {
			System.err.println("Failed to read [" + path + "]: " + e.getMessage());
			System.exit(0);
		}
		return null;
	}

	public static float[][][][] imageToMultiArray(BufferedImage bi) {
		int height = 0, width = 0, depth = 3;
		//reads a jpeg image from a specified file path and writes it to a specified array
		//final image array for output of fireworks
		float image[][][][] = new float[1][bi.getWidth()][bi.getHeight()][3];

		int imageCount = 0;
		width = bi.getWidth();
		height = bi.getHeight();

		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				int rgb = bi.getRGB(i, j);
				Color color = new Color(rgb);
				image[imageCount][i][j][0] = color.getRed();
				image[imageCount][i][j][1] = color.getGreen();
				image[imageCount][i][j][2] = color.getBlue();
			}
		}

		return image;
	}



}
