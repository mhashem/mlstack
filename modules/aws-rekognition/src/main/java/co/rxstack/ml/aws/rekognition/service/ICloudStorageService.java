package co.rxstack.ml.aws.rekognition.service;

import java.io.File;

/**
 * @author mhachem 9/30/2017.
 */
public interface ICloudStorageService {

	void uploadImage(String bucketName, String keyName, String uploadFileName, File image);

}
