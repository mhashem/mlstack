package co.rxstack.ml.tensorflow;

import java.awt.image.BufferedImage;

public interface IFaceNetService {

	void train();

	/**
	 * @param bufferedImage {@link BufferedImage}
	 * @return float array of 128 feature space vector
	 */
	float[] computeEmbeddingsVector(BufferedImage bufferedImage);
	
}
