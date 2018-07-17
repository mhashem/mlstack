package co.rxstack.ml.client.preprocessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.rxstack.ml.common.model.FaceBox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreprocessorClient implements IPreprocessorClient {

	private static final Logger log = LoggerFactory.getLogger(PreprocessorClient.class);

	private final String serviceUri;

	private ObjectMapper objectMapper;

	public PreprocessorClient(String serviceUri) {
		this.serviceUri = serviceUri;
		objectMapper = new ObjectMapper();
	}

	/**
	 * Align the provided face image (as byte array) to a constant standard
	 *
	 * @param imageBytes byte[]
	 * @return {@link Optional}
	 */
	@Override
	public Optional<byte[]> align(byte[] imageBytes) {
		log.info("Calling preprocessor service to process image with {} bytes", imageBytes.length);
		File tempFile = null;
		try {
			tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
			tempFile.deleteOnExit();
			FileUtils.writeByteArrayToFile(tempFile, imageBytes);
			HttpResponse<InputStream> response = Unirest.post(serviceUri + "/api/v1/faces/alignment")
				.header("accept", "application/json")
				.queryString("cropDim", 160)
				.field("image", tempFile)
				.asObject(InputStream.class);

			if (response.getStatus() == 200) {
				log.info("image aligned successfully");
				return Optional.of(IOUtils.toByteArray(response.getRawBody()));
			}
		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (tempFile != null)
				tempFile.delete();
		}
		return Optional.empty();
	}

	/**
	 * This method has higher accuracy detecting faces in an image
	 *  
	 * @param imageBytes byte[]
	 * @return List<FaceBox> list of face boxes
	 */
	@Override
	public List<FaceBox> detectFaces(byte[] imageBytes) {
		log.info("Calling preprocessor service to detect faces in image with size {} bytes", imageBytes.length);
		File tempFile = null;
		try {
			tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
			tempFile.deleteOnExit();
			FileUtils.writeByteArrayToFile(tempFile, imageBytes);
			HttpResponse<JsonNode> jsonResponse = Unirest.post(serviceUri + "/api/v1/faces/detection")
				.header("accept", "application/json")
				.field("image", tempFile)
				.asJson();

			if (jsonResponse.getStatus() == 200) {
				log.info("image aligned successfully");
				return  objectMapper.readValue(jsonResponse.getRawBody(),
					new TypeReference<List<FaceBox>>(){});
			}
		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (tempFile != null)
				tempFile.delete();
		}
		return ImmutableList.of();
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

	/*public static void main(String[] args) throws IOException {
		PreprocessorClient client = new PreprocessorClient("http://localhost:5000");
		byte[] imageBytes = FileUtils.readFileToByteArray(new File("C:\\etc\\mlstack\\misc\\23167642_10155853806249108_1032955621687484260_n.jpg"));

		List<FaceBox> faceBoxes = client.detectFaces(imageBytes);
		for (FaceBox faceBox : faceBoxes) {
			System.out.println(faceBox);
		}

	}*/

}
