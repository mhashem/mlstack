package co.rxstack.ml.aggregator.service;

public interface StorageStrategy {

	static enum Strategy {
		DISK, S3_BUCKET
	}

}
