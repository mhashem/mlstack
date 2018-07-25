package co.rxstack.ml.classifier;

import smile.classification.NeuralNetwork;

public class ClassifiersFactory {

	public static NeuralNetwork makeNeuralNetwork(int hiddenLayersCount, int featureCount, int labelsCount) {
		return 	new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY,
			NeuralNetwork.ActivationFunction.SOFTMAX, featureCount,
			hiddenLayersCount, labelsCount);
	}

}
