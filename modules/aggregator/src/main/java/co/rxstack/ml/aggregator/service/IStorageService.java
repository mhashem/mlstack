package co.rxstack.ml.aggregator.service;

import java.io.IOException;

public interface IStorageService {

	boolean saveTemporary(String filename, byte[] fileBytes, StorageStrategy.Strategy strategy);

	boolean saveFile(String fileName, String folder, byte[] fileBytes, StorageStrategy.Strategy strategy);

	byte[] readBytes(String fileName, String folder, StorageStrategy.Strategy strategy) throws IOException;

}
