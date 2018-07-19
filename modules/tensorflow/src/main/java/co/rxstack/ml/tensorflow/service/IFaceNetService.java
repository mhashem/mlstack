package co.rxstack.ml.tensorflow.service;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;

import co.rxstack.ml.tensorflow.TensorFlowResult;

public interface IFaceNetService {

	double DEFAULT_THRESHOLD = 50d;

	void saveEmbeddings(Map<String, float[]> embeddings) throws FileNotFoundException;

	void loadEmbeddingsVector();

	void loadEmbeddingsVectorFromFile() throws FileNotFoundException;

	/**
	 * Performs computations for obtaining a feature vector
	 * using FaceNet neural network
	 *
	 * @param bufferedImage {@link BufferedImage}
	 * @return float array of 128 feature space vector
	 */
	double[] computeEmbeddingsFeaturesVector(BufferedImage bufferedImage);

	Optional<TensorFlowResult> computeDistance(double[] vector);

	Optional<TensorFlowResult> computeDistance(double[] vector, double threshold);
}
