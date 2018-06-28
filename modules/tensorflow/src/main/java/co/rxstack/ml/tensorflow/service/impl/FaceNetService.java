package co.rxstack.ml.tensorflow.service.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.annotation.PreDestroy;

import co.rxstack.ml.tensorflow.TensorFlowResult;
import co.rxstack.ml.tensorflow.config.FaceNetConfig;
import co.rxstack.ml.tensorflow.exception.GraphLoadingException;
import co.rxstack.ml.tensorflow.service.IFaceNetService;
import co.rxstack.ml.tensorflow.utils.GraphUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
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

	private Session session;
	private Graph faceNetTensorGraph;
	private FaceNetConfig faceNetConfig;

	@Autowired
	public FaceNetService(FaceNetConfig faceNetConfig) throws GraphLoadingException {
		Preconditions.checkNotNull(faceNetConfig);
		this.faceNetConfig = faceNetConfig;

		Path faceNetGraphPath = Paths.get(faceNetConfig.getFaceNetGraphPath());

		if (!faceNetGraphPath.toFile().exists()) {
			log.warn("No ProtoBuffer graph found at {}", faceNetGraphPath.toAbsolutePath());
			throw new GraphLoadingException(faceNetGraphPath.toAbsolutePath().toString());
		}

		Stopwatch stopwatch = Stopwatch.createStarted();
		byte[] graphDef = GraphUtils.readAllBytes(faceNetGraphPath);
		if (graphDef != null) {
			faceNetTensorGraph = new Graph();
			faceNetTensorGraph.importGraphDef(graphDef);
			log.info("FaceNet tensorflow graph loaded successfully in {}ms", stopwatch.elapsed(MILLISECONDS));
		} else {
			log.warn("Failed to load FaceNet tensorflow graph!");
		}

		session = new Session(faceNetTensorGraph);
	}

	@Override
	public float[] computeEmbeddingsFeaturesVector(BufferedImage bufferedImage) {
		log.info("Computing embeddings feature vector");
		try (Tensor<Float> image = Tensors.create(imageTo4DTensor(bufferedImage))) {
			float[] embeddings = new float[128];
			Stopwatch stopwatch = Stopwatch.createStarted();
			Tensor<Float> result = session.runner()
				.feed("input:0", image)
				.feed("phase_train:0", Tensors.create(false))
				.fetch("embeddings:0")
				.run()
				.get(0)
				.expect(Float.class);
			result.writeTo(FloatBuffer.wrap(embeddings));
			log.info("Embeddings computation completed in {}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
				return embeddings;
			} catch (Exception e) {
			log.error(e.getMessage(), e);
			}
		return new float[] {};
	}

	@Override
	public Optional<TensorFlowResult> computeDistance(float[] vector) {
		return computeDistance(vector, DEFAULT_THRESHOLD);
	}

	@Override
	public Optional<TensorFlowResult> computeDistance(float[] vector, double threshold) {

		Map<String, float[]> embeddings = Maps.newHashMap();

		// load vectors

		// todo find solution for vector loading

		Map<Double, String> resultsVector = Maps.newHashMap();

		EuclideanDistance euclideanDistance = new EuclideanDistance();

		embeddings.keySet().forEach(label -> {
			double d = euclideanDistance.compute(toDoubleArray(embeddings.get(label)), toDoubleArray(vector));
			resultsVector.put(d, label);
		});

		Optional<Double> aDouble = resultsVector.keySet().stream().min(Comparator.naturalOrder());

		if (aDouble.isPresent()) {
			TensorFlowResult result =
				new TensorFlowResult(resultsVector.get(aDouble.get()), Math.round((1 - aDouble.get()) * 100));
			return Optional.of(result);
		}
		return Optional.empty();
	}

	@PreDestroy
	public void onDestroy() {
		log.info("PreDestroy() fired, releasing Tensorflow Session, and Graph");
		session.close();
		faceNetTensorGraph.close();
	}

	private double[] toDoubleArray(float[] e) {
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
