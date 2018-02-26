package co.rxstack.ml.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreprocessorClient {

	private static final Logger log = LoggerFactory.getLogger(PreprocessorClient.class);

	private final String serviceUri;

	public PreprocessorClient(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	/**
	 * Align the provided face image (as byte array) to a constant standard
	 *
	 * @param imageBytes byte[]
	 * @return {@link Optional}
	 */
	public Optional<byte[]> align(byte[] imageBytes) {
		File tempFile = null;
		try {
			tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
			tempFile.deleteOnExit();
			FileUtils.writeByteArrayToFile(tempFile, imageBytes);
			HttpResponse<InputStream> jsonResponse = Unirest.post(serviceUri + "/api/v1/faces/alignment")
				.header("accept", "application/json")
				.field("image", tempFile)
				.asObject(InputStream.class);

			if (jsonResponse.getStatus() == 200) {
				log.info("image aligned successfully");
				return Optional.of(IOUtils.toByteArray(jsonResponse.getRawBody()));
			}
		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (tempFile != null)
				tempFile.delete();
		}
		return Optional.empty();
	}

	/*public static void main(String[] args) throws IOException {
		PreprocessorClient client = new PreprocessorClient("http://localhost:5000");
		byte[] bytes = FileUtils.readFileToByteArray(new File("C:\\Users\\mahmoud\\Pictures\\People\\sheikh_akram.jpg"));

		Optional<byte[]> alignOptional = client.align(bytes);

		if (alignOptional.isPresent()) {
			System.out.println("Present");
			FileUtils.writeByteArrayToFile(new File("C:\\Users\\mahmoud\\Pictures\\People\\sheikh_akram_aligned.jpg"), alignOptional.get(), false);
		}
	}*/

}
