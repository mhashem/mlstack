/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package co.rxstack.ml.tensorflow.experimental;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.CV_32FC1;

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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvTermCriteria;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.TermCriteria;
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

/**
 * Sample use of the TensorFlow Java API to label images using a pre-trained model.
 */
@SuppressWarnings("ALL")
public class FaceNetLabeling {


	private static Graph tensorflowGraph;

	private static void printUsage(PrintStream s) {
		final String url = "https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip";
		s.println("Java program that uses a pre-trained Inception model (http://arxiv.org/abs/1512.00567)");
		s.println("to label JPEG images.");
		s.println("TensorFlow version: " + TensorFlow.version());
		s.println();
		s.println("Usage: label_image <model dir> <image file>");
		s.println();
		s.println("Where:");
		s.println("<model dir> is a directory containing the unzipped contents of the inception model");
		s.println("            (from " + url + ")");
		s.println("<image file> is the path to a JPEG image file");
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			printUsage(System.err);
			System.exit(1);
		}
		String modelDir = args[0];
		String imageFile = args[1];
		String imageDir = args[2];

		String graphFile = "20180402-114759.pb";
		String labelsFile = "imagenet_comp_graph_label_strings.txt";

		byte[] graphDef = readAllBytesOrExit(Paths.get(modelDir, graphFile));
		List<String> labels = readAllLinesOrExit(Paths.get("C:\\Users\\mahmoud\\Documents\\datascience\\models\\Facenet-20170511-185253", labelsFile));
		BufferedImage testImage = readBufImage(Paths.get(imageFile));

		Map<String, BufferedImage> nameBufferedImageMap = readBufferedImages(Paths.get(imageDir));
		Map<String, float[]> embeddings = Maps.newHashMap();

		tensorflowGraph = new Graph();
		tensorflowGraph.importGraphDef(graphDef);

		embeddings.putAll(loadEmbeddings());

		/*for (String name : nameBufferedImageMap.keySet()) {
			float[] embeddingsArray = computeEmbeddings(nameBufferedImageMap.get(name));
			embeddings.put(name, embeddingsArray);
		}*/

		//float[][] trainingData = new float[embeddings.size()][128];

		/*Mat trainingFeaturesDataMat = new Mat(embeddings.size(), 128, opencv_core.CV_32FC1);
		//Vector<Integer> trainingLabels = new Vector<>(embeddings.size());

		Mat trainingLabelsDataMat = new Mat(embeddings.size(), 1, opencv_core.CV_32F);

		FloatBuffer buffer = trainingLabelsDataMat.createBuffer();
*/


		Mat classes = new Mat();
		Mat trainingData = new Mat();

		Mat trainingImages = new Mat();
		Vector<Integer> trainingLabels = new Vector<Integer>();

		final int[] i = { 0 };

		embeddings.keySet().forEach(label -> {
			Mat featuresArrayMat = new Mat(embeddings.get(label));
			featuresArrayMat = featuresArrayMat.reshape(0, 1);

			trainingImages.push_back(featuresArrayMat);
			trainingLabels.add(label.hashCode());

			System.out.println(label + " -> " + label.hashCode());

			i[0]++;
		});

		trainingImages.copyTo(trainingData);
		trainingData.convertTo(trainingData, CV_32FC1);

		int[] labelss = new int[trainingLabels.size()];
		for(int ii=0;ii < trainingLabels.size();++ii)
			labelss[ii] = trainingLabels.get(ii).intValue();
		new Mat(labelss).copyTo(classes);

/*
		CvTermCriteria cvTermCriteria =
			new CvTermCriteria(opencv_core.CV_TERMCRIT_ITER, 1000, 0.000001);
*/

/*
		SVM svmClassifier = SVM.create();
		svmClassifier.setType(SVM.C_SVC);
		svmClassifier.setKernel(SVM.LINEAR);
		svmClassifier.setTermCriteria(cvTermCriteria.asTermCriteria());

		opencv_ml.TrainData trainData =
			new opencv_ml.TrainData(trainingData);

		System.out.println("h " + trainingData.size().height() + " - w " + trainingData.size().width());
		System.out.println("h " + classes.size().height() + " - w " + classes.size().width());

		svmClassifier.train(trainingData, opencv_ml.ROW_SAMPLE, classes);
*/

		/*svmClassifier.trainAuto(trainData, 10,
			SVM.getDefaultGrid(SVM.C),
			SVM.getDefaultGrid(SVM.GAMMA),
			SVM.getDefaultGrid(SVM.P),
			SVM.getDefaultGrid(SVM.NU),
			SVM.getDefaultGrid(SVM.COEF),
			SVM.getDefaultGrid(SVM.DEGREE),
			true);*/

/*
		opencv_core.CvFileStorage fsTo = opencv_core.CvFileStorage
			.open("C:/etc/mlstack/output/svm.xml", opencv_core.CvMemStorage.create(), CV_STORAGE_WRITE);
		opencv_core.FileStorage fsto2=new opencv_core.FileStorage(fsTo);
		svmClassifier.write(fsto2);
*/


/*
		CvMat layerSizes = new CvMat(new Mat(4, 1, CV_32FC1));
		layerSizes.put(0, 0, 128);
		layerSizes.put(0, 1, labelss.length * 8);
		layerSizes.put(0, 2, labelss.length * 4);
		layerSizes.put(0, 3, 1);


		Mat layers = new Mat(4, 1, CV_32FC1);
		layers.row(0).put(new Scalar(128)); // input
		layers.row(1).put(new Scalar(labelss.length * 8)); // hidden
		layers.row(2).put(new Scalar(labelss.length * 4)); // hidden
		layers.row(3).put(new Scalar(labelss.length)); // output

		opencv_ml.ANN_MLP ann_mlp = opencv_ml.ANN_MLP.create();
		ann_mlp.setLayerSizes(new Mat(layerSizes));
		ann_mlp.setBackpropWeightScale(0.1);
		ann_mlp.setBackpropMomentumScale(0.1);
		ann_mlp.setActivationFunction(opencv_ml.ANN_MLP.SIGMOID_SYM, 1, 1);
		ann_mlp.setTermCriteria(new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 300, 0.0));
		ann_mlp.setTrainMethod(opencv_ml.ANN_MLP.BACKPROP);

		ann_mlp.train(trainingData, opencv_ml.ROW_SAMPLE, classes); // maybe use floatValue() for labelss!
*/

		EuclideanDistance distance = new EuclideanDistance();

		/*double testAliDistance = distance
			.compute(toDoubleArray(embeddings.get("ali_mohammad_test")),
				toDoubleArray(embeddings.get("ali_mohammad")));

		double testMahmoudDistance = distance
			.compute(toDoubleArray(embeddings.get("ali_mohammad_test")),
				toDoubleArray(embeddings.get("mahmoud_hachem")));

		double testMahmoudMahmoudDistance = distance
			.compute(toDoubleArray(embeddings.get("mahmoud_hachem_test")),
				toDoubleArray(embeddings.get("mahmoud_hachem")));

		double testAliMamoudDistance = distance
			.compute(toDoubleArray(embeddings.get("mahmoud_hachem_test")),
				toDoubleArray(embeddings.get("ali_mohammad")));
*/
		double[] testFloatVector = toDoubleArray(computeEmbeddings(testImage));
		Map<Double, String> resultsVector = Maps.newHashMap();

		Mat reshapedMat = new Mat(computeEmbeddings(testImage)).reshape(0, 1);
		Stopwatch predictionStopwatch = Stopwatch.createStarted();
/*
		float predict = ann_mlp.predict(reshapedMat, classes, opencv_ml.StatModel.RAW_OUTPUT);
		System.out.println("Classifier: prediction " + predict + " completed in "
			+ predictionStopwatch.elapsed(TimeUnit.MILLISECONDS)  + "ms");
*/

		embeddings.keySet().forEach(label -> {
			double d = distance.compute(toDoubleArray(embeddings.get(label)), testFloatVector);
			resultsVector.put(d, label);
		});

		List<Double> collect = resultsVector.keySet().stream().sorted().collect(Collectors.toList());
		collect.stream().forEach(aDouble -> {
			System.out
				.println(resultsVector.get(aDouble) + " with confidence " + aDouble + " rounded -> " + Math.round((1 - aDouble) * 100) + "%");
		});
		System.out.println();
		Double aDouble = resultsVector.keySet().stream().min(Comparator.naturalOrder()).get();
		System.out.println(resultsVector.get(aDouble) + " with confidence " + aDouble + " rounded -> " + Math.round((1 - aDouble) * 100) + "%");

		// FIXME
		//saveEmbeddings(embeddings);
/*
		System.out.println("Ali to Ali Test: " + testAliDistance);
		System.out.println("Mahmoud to Ali Test: " + testMahmoudDistance);
		System.out.println("Mahmoud to Mahmoud Test: " + testMahmoudMahmoudDistance);
		System.out.println("Ali to Mahmoud Test: " + testAliMamoudDistance);
*/


		/*try (Tensor<Float> image = Tensors.create(imageToMultiArray(bufferedImages.get(0)))) {
			float[] labelProbabilities = executeInceptionGraph(graphDef, image);
			int bestLabelIdx = maxIndex(labelProbabilities);
			System.out.println(String.format("BEST MATCH: %s (%.2f%% likely)", labels.get(bestLabelIdx),
				labelProbabilities[bestLabelIdx] * 100f));
		}*/

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

	private static Tensor<String> constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {
		try (Graph g = new Graph()) {
			GraphBuilder b = new GraphBuilder(g);
			// Some constants specific to the pre-trained model at:
			// https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
			//
			// - The model was trained with images scaled to 224x224 pixels.
			// - The colors, represented as R, G, B in 1-byte each were converted to
			//   float using (value - Mean)/Scale.
			final int H = 224;
			final int W = 224;
			final float mean = 117f;
			final float scale = 1f;

			// Since the graph is being constructed once per execution here, we can use a constant for the
			// input image. If the graph were to be re-used for multiple input images, a placeholder would
			// have been more appropriate.
			final Output<String> input = b.constant("input", imageBytes);
			final Output<Float> output = b.div(b.sub(
				b.resizeBilinear(b.expandDims(b.cast(b.decodeJpeg(input, 3), String.class), b.constant("make_batch", 0)),
					b.constant("size", new int[] { H, W })), b.constant("mean", mean)), b.constant("scale", scale));
			try (Session s = new Session(g)) {
				return s.runner().fetch(output.op().name()).run().get(0).expect(String.class);
			}
		}
	}

	public static float[] computeEmbeddings(BufferedImage bufferedImage) {

		System.out.println(tensorflowGraph.operation("image_batch").type());

		Tensor<?> tensor = Tensor.create(bufferedImage);
		System.out.println(tensor.dataType());

		Iterator<Operation> operations = tensorflowGraph.operations();
		while (operations.hasNext()) {
			System.out.println(operations.next().name());
		}

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


					System.out.println("Execution completed in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
				return embeddings;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/*public static float[] computeEmbeddings(byte[] graphDef, BufferedImage bufferedImage) {
		try (Tensor<Float> image = Tensors.create(imageToMultiArray(bufferedImage)); Graph g = new Graph()) {
			g.importGraphDef(graphDef);
			try (Session s = new Session(g)) {
				Stopwatch stopwatch = Stopwatch.createStarted();
				Tensor<Float> result =
					s.runner()
						.feed("input:0", image)
						.feed("phase_train:0", Tensors.create(false))
						.fetch("embeddings:0").run().get(0)
						.expect(Float.class);

				float[] embeddings = new float[128];
				result.writeTo(FloatBuffer.wrap(embeddings));
				System.out.println("Execution completed in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
				return embeddings;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}*/

	private static float[] executeInceptionGraph(byte[] graphDef, Tensor<?> image) {
		try (Graph g = new Graph()) {
			g.importGraphDef(graphDef);

			Iterator<Operation> operations = g.operations();

			while (operations.hasNext()) {
				System.out.println(operations.next().name());
			}

			try(Session s = new Session(g)) {

				Stopwatch stopwatch = Stopwatch.createStarted();


				// input:0
				// embeddings:0
				// phase_train:0

				Tensor<Float> result =
					s.runner().feed("input:0", image).feed("phase_train:0", Tensors.create(false)).fetch("embeddings:0")
						.run().get(0).expect(Float.class);

				result.shape();

				float[] embeddings = new float[128];

				result.writeTo(FloatBuffer.wrap(embeddings));

				System.out.println(Arrays.toString(embeddings));

				System.out.println("Execution completed in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

				final long[] rshape = result.shape();

				if (result.numDimensions() != 2 || rshape[0] != 1) {
					throw new RuntimeException(String.format(
						"Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
						Arrays.toString(rshape)));
				}
				int nlabels = (int) rshape[1];
				float[][] floats = result.copyTo(new float[1][nlabels]);

				return floats[0];
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	private static int maxIndex(float[] probabilities) {
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
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

	static class Holder {
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

	// In the fullness of time, equivalents of the methods of this class should be auto-generated from
	// the OpDefs linked into libtensorflow_jni.so. That would match what is done in other languages
	// like Python, C++ and Go.
	static class GraphBuilder {
		GraphBuilder(Graph g) {
			this.g = g;
		}

		Output<Float> div(Output<Float> x, Output<Float> y) {
			return binaryOp("Div", x, y);
		}

		<T> Output<T> sub(Output<T> x, Output<T> y) {
			return binaryOp("Sub", x, y);
		}

		<T> Output<Float> resizeBilinear(Output<T> images, Output<Integer> size) {
			return binaryOp3("ResizeBilinear", images, size);
		}

		<T> Output<T> expandDims(Output<T> input, Output<Integer> dim) {
			return binaryOp3("ExpandDims", input, dim);
		}

		<T, U> Output<U> cast(Output<T> value, Class<U> type) {
			DataType dtype = DataType.fromClass(type);
			return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().<U>output(0);
		}

		Output<UInt8> decodeJpeg(Output<String> contents, long channels) {
			return g.opBuilder("DecodeJpeg", "DecodeJpeg").addInput(contents).setAttr("channels", channels)
				.build().<UInt8>output(0);
		}

		<T> Output<T> constant(String name, Object value, Class<T> type) {
			try (Tensor<T> t = Tensor.<T>create(value, type)) {
				return g.opBuilder("Const", name).setAttr("dtype", DataType.fromClass(type)).setAttr("value", t)
					.build().<T>output(0);
			}
		}

		Output<String> constant(String name, byte[] value) {
			return this.constant(name, value, String.class);
		}

		Output<Integer> constant(String name, int value) {
			return this.constant(name, value, Integer.class);
		}

		Output<Integer> constant(String name, int[] value) {
			return this.constant(name, value, Integer.class);
		}

		Output<Float> constant(String name, float value) {
			return this.constant(name, value, Float.class);
		}

		private <T> Output<T> binaryOp(String type, Output<T> in1, Output<T> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
		}

		private <T, U, V> Output<T> binaryOp3(String type, Output<U> in1, Output<V> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
		}

		private Graph g;
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
