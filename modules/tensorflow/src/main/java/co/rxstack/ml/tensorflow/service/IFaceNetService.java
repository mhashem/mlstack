package co.rxstack.ml.tensorflow.service;

import java.awt.image.BufferedImage;
import java.util.Optional;

import co.rxstack.ml.tensorflow.TensorFlowResult;

public interface IFaceNetService {

	double DEFAULT_THRESHOLD = 50d;

	/**
	 * Performs computations for obtaining a feature vector
	 * using FaceNet neural network
	 *
	 * @param bufferedImage {@link BufferedImage}
	 * @return float array of 128 feature space vector
	 */
	float[] computeEmbeddingsFeaturesVector(BufferedImage bufferedImage);

	Optional<TensorFlowResult> computeDistance(float[] vector);

	Optional<TensorFlowResult> computeDistance(float[] vector, double threshold);
}
