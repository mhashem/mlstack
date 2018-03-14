package co.rxstack.ml.tensorflow.service;

import java.awt.image.BufferedImage;

public interface IFaceNetService {

	/**
	 * Performs computations for obtaining a feature vector
	 * using FaceNet neural network
	 *
	 * @param bufferedImage {@link BufferedImage}
	 * @return float array of 128 feature space vector
	 */
	float[] computeEmbeddingsFeaturesVector(BufferedImage bufferedImage);
	
}
