package co.rxstack.ml.aggregator;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public interface IFaceRecognitionService<C, T> {

	void loadModel(String modelName);

	void trainModel(Map<String, Object> dataMap);

	C predict(T record);

}
