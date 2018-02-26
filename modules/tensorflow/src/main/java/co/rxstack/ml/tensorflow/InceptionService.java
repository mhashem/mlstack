package co.rxstack.ml.tensorflow;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.PreDestroy;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

@Component
public class InceptionService {

	private static final Logger log = LoggerFactory.getLogger(InceptionService.class);

	private Graph graph;
	private List<String> labels;

	private InceptionConfig inceptionConfig;

	@Autowired
	public InceptionService(InceptionConfig inceptionConfig) throws Exception {
		Preconditions.checkNotNull(inceptionConfig);
		Preconditions.checkNotNull(inceptionConfig.getGraphPath());
		Preconditions.checkNotNull(inceptionConfig.getLabelsPath());

		Path graphPath = Paths.get(inceptionConfig.getGraphPath());
		Path labelsPath = Paths.get(inceptionConfig.getLabelsPath());

		if (!Files.exists(graphPath)) {
			log.warn("No ProtoBuffer graph found at {}", graphPath.toAbsolutePath());
			throw new Exception("No ProtoBuffer graph found at " + graphPath.toAbsolutePath());
		}

		if (!Files.exists(labelsPath)) {
			log.error("No Labels file found at ", labelsPath.toAbsolutePath());
			throw new Exception("No Labels file found at " + labelsPath.toAbsolutePath());
		}

		Stopwatch stopwatch = Stopwatch.createStarted();
		byte[] graphDef = readAllBytes(graphPath);

		if (graphDef != null) {
			graph = new Graph();
			graph.importGraphDef(graphDef);
			log.info("Tensorflow graph loaded successfully in {}ms", stopwatch.elapsed(MILLISECONDS));
		} else {
			log.warn("Failed to load Tensorflow graph!");
		}

		labels = Lists.newArrayList();
		labels.addAll(readAllLines(labelsPath));

		if (labels.isEmpty()) {
			log.error(">>>>> No Labels found! <<<<<");
		}
	}

	public Optional<TensorFlowResult> predictBest(byte[] imageBytes) {
		log.info("Predicting best result for image with {} bytes", imageBytes.length);
		try(Tensor<String> image = Tensors.create(imageBytes)) {
			float[] labelProbabilities = executeInceptionGraph(image);
			int bestLabelIdx = maxIndex(labelProbabilities);
			TensorFlowResult result = new TensorFlowResult(labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f);
			return Optional.of(result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return Optional.empty();
	}

	private float[] executeInceptionGraph(Tensor<String> image) {
		log.info("executing inception graph {}", image.toString());
		try (Session session = new Session(graph)) {
			Stopwatch stopwatch = Stopwatch.createStarted();
			Tensor<Float> result =
				session.runner().feed("DecodeJpeg/contents:0", image).fetch("final_result:0").run().get(0)
					.expect(Float.class);
			log.info("Tensor execution completed in {}ms", stopwatch.elapsed(MILLISECONDS));
			final long[] rshape = result.shape();
			log.info("rshape {}", Arrays.toString(rshape));
			if (result.numDimensions() != 2 || rshape[0] != 1) {
				String message =
					"Expected model to produce a [1 N] shaped tensor where N is number of labels, instead it produced one with shape "
						+ Arrays.toString(rshape);
				log.warn(message);
				throw new RuntimeException(message);
			}
			int nlabels = (int) rshape[1];
			log.info("Result label number {}", nlabels);
			float[][] floats = result.copyTo(new float[1][nlabels]);
			return floats[0];
		}
	}

	@PreDestroy
	public void cleanUp() {
		graph.close();
	}

	private byte[] readAllBytes(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			log.error("Failed to read [{}]", path);
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private List<String> readAllLines(Path path) {
		try {
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {
			log.error("Failed to read [{}]: message {}",path, e.getMessage(), e);
		}
		return ImmutableList.of();
	}

	private int maxIndex(float[] probabilities) {
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}

}
