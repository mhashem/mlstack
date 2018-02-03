package co.rxstack.ml.aggregator.service;

public interface IStorageService {

	boolean saveFile(String fileName, String extension, String folder, byte[] fileBytes,
		StorageStrategy.Strategy strategy);

}
