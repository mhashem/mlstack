package co.rxstack.ml.aggregator.service;

import java.util.List;
import java.util.Map;

public interface IClassifierService {

	void load();

	void save();
	
	void train(Map<Integer, List<float[]>> embeddings);
}
