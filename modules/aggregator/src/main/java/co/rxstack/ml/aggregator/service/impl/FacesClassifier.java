package co.rxstack.ml.aggregator.service.impl;

import static co.rxstack.ml.classifier.ClassifiersFactory.makeNeuralNetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smile.classification.NeuralNetwork;
import smile.classification.SVM;

//@Component
public class FacesClassifier {

	private static final Logger log = LoggerFactory.getLogger(FacesClassifier.class);

	/*@Autowired
	public FacesClassifier() {
	}*/

	public static SVM<double[]> trainSVM(double[][] features, int[] labels) {
		return null;
	}

	public static NeuralNetwork trainNN(double[][] features, int[] labels) {
		log.info("train NN fired");
		NeuralNetwork neuralNetwork = makeNeuralNetwork(100, features[0].length, labels.length);
		neuralNetwork.setLearningRate(0.5);
		for (int i = 0; i < 100; i++) {
			if (i % 10 == 0) {
				log.info("Epoch {}", i);
			}
			neuralNetwork.learn(features, labels);
		}
		return neuralNetwork;
	}

}
