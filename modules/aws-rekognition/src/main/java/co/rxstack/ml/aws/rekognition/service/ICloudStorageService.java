package co.rxstack.ml.aws.rekognition.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author mhachem 9/30/2017.
 */
public interface ICloudStorageService {

	void uploadImage(String personName, InputStream inputStream);

	void uploadImage(String uploadFileName, InputStream inputStream, Map<String, String> metaDataMap);

	byte[] getObjectAsByteArray(String fileName) throws IOException;

	void deleteObject(String fileName);

	Map<String, String> getCloudIndexFaceIds(String tableName);
}
