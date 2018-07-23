package co.rxstack.ml.tensorflow.service;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.tensorflow.TensorFlowResult;

public interface IFaceNetService {

	double DEFAULT_THRESHOLD = 50d;

	void saveEmbeddings(ConcurrentMap<Integer, double[]> embeddings) throws FileNotFoundException;

	void saveFaces(List<Face> faceList) throws FileNotFoundException;

	void loadEmbeddingsVector();

	void loadEmbeddingsVectorFromFile() throws FileNotFoundException;

	/**
	 * Performs computations for obtaining a feature vector
	 * using FaceNet neural network
	 *
	 * @param bufferedImage {@link BufferedImage}
	 * @return float array representing features vector
	 */
	double[] computeEmbeddingsFeaturesVector(BufferedImage bufferedImage);

	Optional<TensorFlowResult> computeDistance(double[] vector);

	Optional<TensorFlowResult> computeDistance(double[] vector, double threshold);
}
