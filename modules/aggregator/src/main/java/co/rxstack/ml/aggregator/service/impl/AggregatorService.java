package co.rxstack.ml.aggregator.service.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import co.rxstack.ml.aggregator.model.PotentialFace;
import co.rxstack.ml.aggregator.service.IFaceExtractorService;
import co.rxstack.ml.aggregator.service.IFaceRecognitionService;
import co.rxstack.ml.aggregator.service.IStorageService;
import co.rxstack.ml.aggregator.service.StorageStrategy;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.client.preprocessor.PreprocessorClient;
import co.rxstack.ml.cognitiveservices.model.CognitiveIndexingResult;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.AggregateFaceIdentification;
import co.rxstack.ml.common.model.AggregateFaceIndexingResult;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.Constants;
import co.rxstack.ml.common.model.FaceBox;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRecognitionResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.Recognizer;
import co.rxstack.ml.faces.model.Identity;
import co.rxstack.ml.faces.service.IIdentityService;
import co.rxstack.ml.tensorflow.TensorFlowResult;
import co.rxstack.ml.tensorflow.service.IFaceNetService;
import co.rxstack.ml.tensorflow.service.impl.InceptionService;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/29/2017.
 */
@Service
public class AggregatorService {

	private static final Logger log = LoggerFactory.getLogger(FaceExtractorService.class);

	private IIdentityService identityService;
	private ICognitiveService cognitiveService;
	private IRekognitionService awsRekognitionService;
	private IFaceExtractorService faceExtractorService;
	private IFaceRecognitionService faceRecognitionService;

	private InceptionService inceptionService;
	private PreprocessorClient preprocessorClient;

	private IFaceNetService faceNetService;

	private IStorageService storageService;

	private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	public AggregatorService(
		IIdentityService identityService,
		IFaceExtractorService faceExtractorService,
		IFaceRecognitionService faceRecognitionService,
		IRekognitionService rekognitionService,
		ICognitiveService cognitiveService,
		InceptionService inceptionService,
		PreprocessorClient preprocessorClient,
		IFaceNetService faceNetService,
		IStorageService storageService,
		SimpMessagingTemplate simpMessagingTemplate) {

		Preconditions.checkNotNull(identityService);
		Preconditions.checkNotNull(faceExtractorService);
		Preconditions.checkNotNull(faceRecognitionService);
		Preconditions.checkNotNull(rekognitionService);
		Preconditions.checkNotNull(cognitiveService);
		Preconditions.checkNotNull(inceptionService);
		Preconditions.checkNotNull(faceNetService);
		Preconditions.checkNotNull(storageService);

		this.identityService = identityService;
		this.cognitiveService = cognitiveService;
		this.awsRekognitionService = rekognitionService;
		this.faceExtractorService = faceExtractorService;
		this.faceRecognitionService = faceRecognitionService;
		this.inceptionService = inceptionService;
		this.preprocessorClient = preprocessorClient;
		this.faceNetService = faceNetService;
		this.storageService = storageService;

		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	// using Tensorflow
	public List<TensorFlowResult> recognize(byte[] imageBytes) {
		log.info("recognizing image with {} using tensorflow Inception",
			FileUtils.byteCountToDisplaySize(imageBytes.length));
		final List<TensorFlowResult> tensorFlowResults = Lists.newArrayList();
		try {
			BufferedImage bufferedImage = bytesToBufferedImage(imageBytes);
			List<FaceBox> faceBoxes = preprocessorClient.detectFaces(imageBytes);
			faceBoxes.forEach(faceBox -> {
				BufferedImage faceImage = subImage(bufferedImage, faceBox);
				try {
					Optional<byte[]> alignedImageByteArray =
						preprocessorClient.align(bufferedImageToByteArray(faceImage));
					if (alignedImageByteArray.isPresent()) {
						Optional<TensorFlowResult> tensorFlowResult =
							inceptionService.predictBest(alignedImageByteArray.get());
						tensorFlowResult.ifPresent(tfResult -> {
							tfResult.setFaceBox(faceBox);
							tensorFlowResults.add(tfResult);
						});
					}
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			});
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return tensorFlowResults;
	}

	public List<FaceRecognitionResult> faceNetRecognize(byte[] imageBytes) throws IOException {
		List<TensorFlowResult> tensorFlowResults = Lists.newArrayList();

		log.info("make an original image copy");
		BufferedImage originalImage = bytesToBufferedImage(imageBytes);

		log.info("detecting faces in the image");
		final List<FaceBox> detectedFaces = preprocessorClient.detectFaces(imageBytes);

		log.info("detected {}", detectedFaces.size());
		log.info("aligning each detected image ");
		detectedFaces.forEach(faceBox -> {
			BufferedImage faceImage = subImage(originalImage, faceBox);

			// align
			try {

				writeToDisk(faceImage, "subImage");

				Optional<byte[]> alignedImageBytesOptional =
					preprocessorClient.align(bufferedImageToByteArray(faceImage));

				if (alignedImageBytesOptional.isPresent()) {
					log.info("image is aligned successfully");

					byte[] alignedBytes = alignedImageBytesOptional.get();
					if (alignedBytes.length > 0) {

						writeToDisk(bytesToBufferedImage(alignedBytes), "aligned");

						double[] featuresVector = faceNetService
							.computeEmbeddingsFeaturesVector(bytesToBufferedImage(alignedBytes));

						Optional<TensorFlowResult> tfResultOptional =
							faceNetService.computeDistance(featuresVector);

						if (tfResultOptional.isPresent()) {
							TensorFlowResult tensorFlowResult = tfResultOptional.get();
							tensorFlowResult.setFaceBox(faceBox);

							log.info("Matching database Identity for the detected face");
							Optional<Identity> identityOptional =
								identityService.findIdentityByFaceId(tensorFlowResult.getFaceId());
							identityOptional.ifPresent(identity -> tensorFlowResult.setLabel(identity.getName()));

							tensorFlowResults.add(tensorFlowResult);
						}
					}
				}
			} catch (IOException e) {
				log.error("failed to align image");
				log.error(e.getMessage(), e);
			}
		});

		List<FaceRecognitionResult> faceRecognitionResults =
			tensorFlowResults.stream().map(mapTfResultToFaceRecognitionResult).collect(Collectors.toList());

		drawDetectedFaceRectangle(originalImage, faceRecognitionResults);
		writeToDisk(originalImage, "rectangles");

		return faceRecognitionResults;
	}

	private void writeToDisk(BufferedImage image, String desc) throws IOException {
		ImageIO.write(image, "jpg",
			new File("C:/etc/mlstack/output/processing/" + desc + "-"+ UUID.randomUUID().toString() +  ".jpg"));
	}

	private Function<Candidate, FaceRecognitionResult> mapCandidateToFaceRecognitionResult =
		(candidate) -> {
		// TODO not complete yet!
			return FaceRecognitionResult.builder()
				.confidence(candidate.getConfidence())
				.recognizer(candidate.getRecognizer())
				.faceRectangle(candidate.getFaceRectangle()).build();
	};

	private Function<TensorFlowResult, FaceRecognitionResult> mapTfResultToFaceRecognitionResult = (tfResult) ->
		FaceRecognitionResult.builder()
			.index(tfResult.getFaceBox().getIndex())
			.label(tfResult.getLabel())
			.confidence(tfResult.getConfidence())
			.recognizer(Recognizer.TENSOR_FLOW_FACE_NET)
			.faceRectangle(tfResult.getFaceBox().mapToFaceRectangle())
			.build();

	public List<AggregateFaceIndexingResult> indexFaces(byte[] imageBytes, Map<String, String> bundleMap) {
		log.info("indexFaces called with image {} bytes and bundleMap {}", imageBytes.length, bundleMap);

		AggregateFaceIndexingResult result = new AggregateFaceIndexingResult();

		try {

			List<FaceBox> faceBoxes = preprocessorClient.detectFaces(applyPNGNormalization(imageBytes));
			if (faceBoxes.isEmpty()) {
				log.warn("No face detected in provided image, processing aborted");
				return ImmutableList.of();
			}

			if (faceBoxes.size() > 1) {
				log.warn("***********************************************************************************");
				log.warn("Detected multiple faces at indexing step, process will not proceed, ignoring result");
				log.warn("***********************************************************************************");
				return  ImmutableList.of();
			}

			BufferedImage originalImage = bytesToBufferedImage(imageBytes);
			BufferedImage faceImage = subImage(originalImage, faceBoxes.get(0));

			byte[] faceImageBytes = bufferedImageToByteArray(faceImage);
			Optional<byte[]> alignedFaceImageOptional = preprocessorClient.align(faceImageBytes);

			alignedFaceImageOptional.ifPresent(alignedFaceImageBytes -> {
				try {
					BufferedImage alignedFaceImage = bytesToBufferedImage(alignedFaceImageBytes);
					result.embeddingsVector = faceNetService.computeEmbeddingsFeaturesVector(alignedFaceImage);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}

				storageService.saveFile(bundleMap.get(Constants.IMAGE_NAME),
					bundleMap.get(Constants.PERSON_ID), alignedFaceImageBytes, StorageStrategy.Strategy.DISK);
			});

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

		CompletableFuture<Optional<FaceIndexingResult>> awsCompletableFuture =
			CompletableFuture.supplyAsync(() -> awsRekognitionService.indexFace(imageBytes, bundleMap));
		CompletableFuture<Optional<CognitiveIndexingResult>> cognitiveCompletableFuture =
			CompletableFuture.supplyAsync(() -> cognitiveService.indexFace(imageBytes, bundleMap));

		try {
			CompletableFuture.allOf(awsCompletableFuture, cognitiveCompletableFuture).get();

			if (awsCompletableFuture.isDone() && cognitiveCompletableFuture.isDone()) {
				Optional<CognitiveIndexingResult> cognitiveIndexingResult = cognitiveCompletableFuture.get();
				Optional<FaceIndexingResult> faceIndexingResult = awsCompletableFuture.get();
				faceIndexingResult
					.ifPresent(faceIndexingResult1 -> result.awsFaceId = faceIndexingResult1.getFace().getFaceId());
				cognitiveIndexingResult.ifPresent(
					cognitiveIndexingResult1 -> result.cognitivePersonId = cognitiveIndexingResult1.getPersonId());
				return ImmutableList.of(result);
			}

		} catch (InterruptedException | ExecutionException e) {
			log.error(e.getMessage(), e);
		}
		return ImmutableList.of();
	}

	public void identify(byte[] imageBytes, Map<String, Object> bundleMap) {
		log.info("identify called with image {} bytes", imageBytes.length);

		try {
			String contentType = (String) bundleMap.get(Constants.CONTENT_TYPE);

			// build BufferedImage!
			InputStream inStream = new ByteArrayInputStream(imageBytes);
			BufferedImage targetImage = ImageIO.read(inStream);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(targetImage, contentType.split("/")[1], baos);

			byte[] bytes = baos.toByteArray();

			CompletableFuture.runAsync(() -> {
				log.info("Calling AWS Rekognition Service!");
				List<Candidate> awsCandidateList = awsRekognitionService.searchFacesByImage(bytes);
				awsCandidateList.forEach(candidate -> {
					Optional<Identity> identityOptional = identityService.findIdentityByAwsFaceId(candidate.getPersonId());
					if (identityOptional.isPresent()) {
						candidate.setDbPersonId(String.valueOf(identityOptional.get().getId()));
					} else {
						log.warn("No face record found for aws person id {}", candidate.getPersonId());
					}
				});
				log.info("Pushing recognition results to JMS");

				simpMessagingTemplate.convertAndSend("/recognitions",
					awsCandidateList.stream().map(mapCandidateToFaceRecognitionResult).collect(Collectors.toList()));
			});

			CompletableFuture.runAsync(() -> {
				log.info("Calling Microsoft Cognitive Service!");
				Optional<FaceIdentificationResult> faceIdentificationResult = cognitiveService.identifyFace(bytes);
				if (faceIdentificationResult.isPresent()) {
					FaceIdentificationResult identificationResult = faceIdentificationResult.get();
					identificationResult.getCandidates().forEach(candidate -> {
						Optional<Identity> identityOptional =
							identityService.findIdentityByCognitivePersonId(candidate.getPersonId());
						if (identityOptional.isPresent()) {
							candidate.setDbPersonId(String.valueOf(identityOptional.get().getId()));
						} else {
							log.warn("No face record found for cognitive person id {}", candidate.getPersonId());
						}

						candidate.setConfidence(candidate.getConfidence() * 100);
						candidate.setRecognizer(Recognizer.COGNITIVE_SERVICES);
					});
					simpMessagingTemplate.convertAndSend("/recognitions",
						identificationResult.getCandidates().stream().map(mapCandidateToFaceRecognitionResult)
							.collect(Collectors.toList()));
				}
			});
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private List<Candidate> openCVDetection(BufferedImage targetImage, String contentType) {
		List<PotentialFace> potentialFaces;
		String type = contentType.split("/")[1];
		if (type.equalsIgnoreCase("png")) {
			// FIXME this was a workaround because app crashed when image has one channel!
			BufferedImage newImage =
				new BufferedImage(targetImage.getWidth(), targetImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			newImage.createGraphics().drawImage(targetImage, 0, 0, Color.BLACK, null);
			potentialFaces = faceExtractorService.bruteDetectFaces(newImage);
		} else {
			potentialFaces = faceExtractorService.bruteDetectFaces(targetImage);
		}

		if (potentialFaces.isEmpty()) {
			log.warn("No face(s) detected in image by OpenCV");
			return ImmutableList.of();
		}

		List<PotentialFace> predict = faceRecognitionService.predict(targetImage, potentialFaces);
		return predict.stream().map(pFace -> {

			String faceDbId = "";
			String personId = String.valueOf(pFace.getLabel());

			Optional<Identity> identityOptional = identityService.findById(pFace.getLabel());
			if (!identityOptional.isPresent()) {
				log.warn("no corresponding identity id found in database: {}", personId);
			} else {
				faceDbId = String.valueOf(identityOptional.get().getId());
			}

			Candidate candidate =
				new Candidate(personId, pFace.getConfidence(), FaceRectangle.from(pFace.getBox()), Recognizer.OPEN_CV);
			candidate.setDbPersonId(faceDbId);
			return candidate;
		}).collect(Collectors.toList());
	}

	private void drawDetectedFaceRectangle(BufferedImage targetImage, List<FaceRecognitionResult> faceRecognitionResults) {
		Graphics2D graphics = (Graphics2D) targetImage.getGraphics();
		graphics.setColor(Color.GREEN);
		graphics.setStroke(new BasicStroke(4));
		graphics.setFont(new Font("default", Font.BOLD, 12));

		faceRecognitionResults.forEach(faceRecognitionResult -> {
			FaceRectangle faceRectangle = faceRecognitionResult.getFaceRectangle();
			graphics.drawRect(faceRectangle.getLeft(),
				faceRectangle.getTop(),
				faceRectangle.getWidth() - faceRectangle.getLeft(),
				faceRectangle.getHeight() - faceRectangle.getTop());
			graphics.drawString(faceRecognitionResult.getLabel(),
				faceRectangle.getLeft() - 1,
				faceRectangle.getTop() - 1);
		});
		graphics.dispose();
	}

	private static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	private BufferedImage bytesToBufferedImage(byte[] imageBytes) throws IOException {
		InputStream inStream = new ByteArrayInputStream(imageBytes);
		return ImageIO.read(inStream);
	}

	private BufferedImage toJPG(BufferedImage pngBufferedImage) {
		BufferedImage result = new BufferedImage(
			pngBufferedImage.getWidth(),
			pngBufferedImage.getHeight(),
			BufferedImage.TYPE_INT_RGB);
		result.createGraphics().drawImage(pngBufferedImage, 0, 0, Color.WHITE, null);
		return result;
	}

	private byte[] bufferedImageToByteArray(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		return baos.toByteArray();
	}

	private BufferedImage subImage(BufferedImage image, FaceBox faceBox) {
		log.info("FaceBox {} and image [w {}:h {}]", faceBox, image.getWidth(), image.getHeight());
		int x = (faceBox.getLeft() <= 0) ? 0 : faceBox.getLeft(); // x, in case left edge out of raster
		int y = (faceBox.getTop() <= 0) ? 0 : faceBox.getTop();
		return image.getSubimage(x, y, (faceBox.getRight() - x), (faceBox.getBottom() - y));
	}

	private byte[] applyPNGNormalization(byte[] imageBytes) throws IOException {
		return bufferedImageToByteArray(toJPG(bytesToBufferedImage(imageBytes)));
	}

}
