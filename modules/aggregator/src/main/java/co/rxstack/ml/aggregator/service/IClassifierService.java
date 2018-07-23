package co.rxstack.ml.aggregator.service;

import java.util.List;
import java.util.Map;

public interface IClassifierService<T> {

	T getClassifier();
	
	void load();
	
	void train(Map<Integer, List<float[]>> embeddings);

	void save();

	int predict(float[] vector);
}
