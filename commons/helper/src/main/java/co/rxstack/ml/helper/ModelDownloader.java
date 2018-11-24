package co.rxstack.ml.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import me.tongfei.progressbar.ProgressBar;

public class ModelDownloader {

	public static void main(String[] args) throws DbxException, IOException {
		ModelDownloader.downloadFile(args[0]);
	}
	
	public ModelDownloader() {
		
	}
	
	public static void downloadFile(String token) throws DbxException, IOException {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("tensorflow-service-client").build();
		DbxClientV2 dbxClientV2 = new DbxClientV2(config, token);
		
		try (FileOutputStream outputStream = new FileOutputStream(new File("C:/etc/mlstack/models/facenet-2.pb"))) {
			
			/*dbxClientV2.files().listFolder("/models").getEntries().forEach(metadata -> {
				
			});*/

			DbxDownloader<FileMetadata> dbxDownloader = dbxClientV2.files().download("/models/20180402-114759.pb");
			long size = dbxDownloader.getResult().getSize();

			try (ProgressBar progressBar = new ProgressBar("File Download", 100)) {
				dbxDownloader.download(outputStream, 
					completed -> progressBar.stepTo((int)((completed * 1.0 / size) * 100)));
			}
		}
	}
	
}
