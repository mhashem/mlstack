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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.google.common.base.Stopwatch;
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
		if (args.length != 2) {
			printUsage(System.err);
			System.exit(1);
		}
		String modelDir = args[0];
		String imageFile = args[1];

		String graphFile = "20170511-185253.pb";
		String labelsFile = "imagenet_comp_graph_label_strings.txt";

		byte[] graphDef = readAllBytesOrExit(Paths.get(modelDir, graphFile));
		List<String> labels = readAllLinesOrExit(Paths.get(modelDir, labelsFile));
		byte[] imageBytes = readAllBytesOrExit(Paths.get(imageFile));


		ByteBuffer buf = ByteBuffer.wrap(imageBytes);
		FloatBuffer floatBuffer = ((ByteBuffer) buf.rewind()).asFloatBuffer();

		// fromFileMultipleChannels(Paths.get(imageFile).toFile()))

		// Tensor.create(fromFileMultipleChannels(Paths.get(imageFile).toFile())))
		
		try (Tensor<?> image = Tensors.create(imageBytes)) {
			float[] labelProbabilities = executeInceptionGraph(graphDef, image);
			int bestLabelIdx = maxIndex(labelProbabilities);
			System.out.println(String.format("BEST MATCH: %s (%.2f%% likely)", labels.get(bestLabelIdx),
				labelProbabilities[bestLabelIdx] * 100f));
		}
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

				Tensor<float[]> result = s.runner()
					.feed("input:0", image)
					.feed("phase_train:0", Tensors.create(false))
					.fetch("embeddings:0").run().get(0)
					.expect(float[].class);

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


	/**
	 * Load a rastered image from file
	 * @param file the file to load
	 * @return the rastered image
	 * @throws IOException
	 */
	public static float[][][][] fromFileMultipleChannels(File file) throws IOException {
		BufferedImage image = ImageIO.read(file);
		//image = scalingIfNeed(image, false);

		int w = image.getWidth(), h = image.getHeight();
		int bands = image.getSampleModel().getNumBands();
		float[][][][] ret = new float[128][w][h][3];
		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				for (int k = 0; k < 3; k++) {
					if (k >= bands)
						break;
					ret[0][i][j][3] = pixels[3 * w * i + 3 * j + k];
				}
			}
		}
		return ret;
	}

	/*protected BufferedImage scalingIfNeed(BufferedImage image, boolean needAlpha) {
		return scalingIfNeed(image, image.getHeight(), image.getWidth(), needAlpha);
	}*/

	/*protected BufferedImage scalingIfNeed(BufferedImage image, int dstHeight, int dstWidth, boolean needAlpha) {
		if (dstHeight > 0 && dstWidth > 0 && (image.getHeight() != dstHeight || image.getWidth() != dstWidth)) {
			Image scaled = image.getScaledInstance(dstWidth, dstHeight, Image.SCALE_SMOOTH);

			if (needAlpha && image.getColorModel().hasAlpha() && 3 == BufferedImage.TYPE_4BYTE_ABGR) {
				return toBufferedImage(scaled, BufferedImage.TYPE_4BYTE_ABGR);
			} else {
				if (channels == BufferedImage.TYPE_BYTE_GRAY)
					return toBufferedImage(scaled, BufferedImage.TYPE_BYTE_GRAY);
				else
					return toBufferedImage(scaled, BufferedImage.TYPE_3BYTE_BGR);
			}
		} else {
			if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR || image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
				return image;
			} else if (needAlpha && image.getColorModel().hasAlpha() && channels == BufferedImage.TYPE_4BYTE_ABGR) {
				return toBufferedImage(image, BufferedImage.TYPE_4BYTE_ABGR);
			} else {
				if (channels == BufferedImage.TYPE_BYTE_GRAY)
					return toBufferedImage(image, BufferedImage.TYPE_BYTE_GRAY);
				else
					return toBufferedImage(image, BufferedImage.TYPE_3BYTE_BGR);
			}
		}
	}*/

	public static Tensor<?> test() {
		Random r = new Random();
		int imageSize = 224 * 224 * 3;
		int batch = 128;
		long[] shape = new long[] {batch, imageSize};
		FloatBuffer buf = FloatBuffer.allocate(imageSize * batch);
		for (int i = 0; i < imageSize * batch; ++i) {
			buf.put(r.nextFloat());
		}
		buf.flip();

		long start = System.nanoTime();
		Tensor<Float> floatTensor = Tensor.create(shape, buf);
		long end = System.nanoTime();
		System.out.println("Took: " + (end - start));

		return floatTensor;
	}


}
