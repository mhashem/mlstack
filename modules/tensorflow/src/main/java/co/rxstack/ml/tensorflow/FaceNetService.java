package co.rxstack.ml.tensorflow;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import co.rxstack.ml.tensorflow.config.FaceNetConfig;
import co.rxstack.ml.tensorflow.exception.GraphLoadingException;
import co.rxstack.ml.tensorflow.utils.GraphUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
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
	public FaceNetService(FaceNetConfig faceNetConfig) throws Exception {
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
		try (Tensor<Float> image = Tensors.create(imageToMultiDimensionalArray(bufferedImage))) {
			float[] embeddings = new float[128];
				Stopwatch stopwatch = Stopwatch.createStarted();
			Tensor<Float> result = session.runner()
						.feed("input:0", image)
						.feed("phase_train:0", Tensors.create(false))
						.fetch("embeddings:0").run().get(0)
						.expect(Float.class);
				result.writeTo(FloatBuffer.wrap(embeddings));
			log.info("Embeddings computation completed in {}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
				return embeddings;
			} catch (Exception e) {
			log.error(e.getMessage(), e);
			}
			return null;
	}

	@PreDestroy
	public void onDestroy() {
		log.info("PreDestroy() fired, releasing Tensorflow Session, and Graph");
		session.close();
		faceNetTensorGraph.close();
	}

	private float[][][][] imageToMultiDimensionalArray(BufferedImage bi) {
		int height = bi.getHeight(), width = bi.getWidth(), depth = 3;
		float image[][][][] = new float[1][width][height][depth];
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
