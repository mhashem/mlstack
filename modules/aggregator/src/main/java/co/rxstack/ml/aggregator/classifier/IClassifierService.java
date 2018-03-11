package co.rxstack.ml.aggregator.classifier;

public interface IClassifierService {

	void load();

	void save();

	void train(String imagesDir);

}
