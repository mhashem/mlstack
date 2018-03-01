package co.rxstack.ml.aggregator.service.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import co.rxstack.ml.aggregator.model.PotentialFace;
import co.rxstack.ml.aggregator.model.db.Identity;
import co.rxstack.ml.aggregator.service.IFaceExtractorService;
import co.rxstack.ml.aggregator.service.IFaceRecognitionService;
import co.rxstack.ml.aggregator.service.IIdentityService;
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
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.Recognizer;
import co.rxstack.ml.tensorflow.InceptionService;
import co.rxstack.ml.tensorflow.TensorFlowResult;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	public AggregatorService(IIdentityService identityService, IFaceExtractorService faceExtractorService,
		IFaceRecognitionService faceRecognitionService,
		IRekognitionService rekognitionService, ICognitiveService cognitiveService, InceptionService inceptionService,
		PreprocessorClient preprocessorClient) {

		Preconditions.checkNotNull(identityService);
		Preconditions.checkNotNull(faceExtractorService);
		Preconditions.checkNotNull(faceRecognitionService);
		Preconditions.checkNotNull(rekognitionService);
		Preconditions.checkNotNull(cognitiveService);
		Preconditions.checkNotNull(inceptionService);

		this.identityService = identityService;
		this.cognitiveService = cognitiveService;
		this.awsRekognitionService = rekognitionService;
		this.faceExtractorService = faceExtractorService;
		this.faceRecognitionService = faceRecognitionService;
		this.inceptionService = inceptionService;
		this.preprocessorClient = preprocessorClient;
	}

	// using Tensorflow
	public List<TensorFlowResult> recognize(byte[] imageBytes) {
		log.info("recognizing image with {} bytes using {}", imageBytes.length);
		final List<TensorFlowResult> tfResutls = Lists.newArrayList();
		try {
			BufferedImage bufferedImage = bytesToBufferedImage(imageBytes);
			List<FaceBox> faceBoxes = preprocessorClient.detectFaces(imageBytes);
			faceBoxes.forEach(faceBox -> {
				BufferedImage faceImage = bufferedImage
					.getSubimage(faceBox.getLeft(), faceBox.getTop(), (faceBox.getRight() - faceBox.getLeft()),
						(faceBox.getBottom() - faceBox.getTop()));
				try {
					Optional<byte[]> alignedImageByteArray =
						preprocessorClient.align(bufferedImageToByteArray(faceImage));
					if (alignedImageByteArray.isPresent()) {
						Optional<TensorFlowResult> tensorFlowResult =
							inceptionService.predictBest(alignedImageByteArray.get());
						tensorFlowResult.ifPresent(tfResutls::add);
					}
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			});
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return tfResutls;
	}

	public List<AggregateFaceIndexingResult> indexFaces(byte[] imageBytes, Map<String, String> bundleMap) {
		log.info("indexFaces called with image {} bytes and bundleMap {}", imageBytes.length, bundleMap);

		CompletableFuture<Optional<FaceIndexingResult>> awsCompletableFuture =
			CompletableFuture.supplyAsync(() -> awsRekognitionService.indexFace(imageBytes, bundleMap));
		CompletableFuture<Optional<CognitiveIndexingResult>> cognitiveCompletableFuture =
			CompletableFuture.supplyAsync(() -> cognitiveService.indexFace(imageBytes, bundleMap));

		try {
			CompletableFuture.allOf(awsCompletableFuture, cognitiveCompletableFuture).get();

			if (awsCompletableFuture.isDone() && cognitiveCompletableFuture.isDone()) {
				AggregateFaceIndexingResult result = new AggregateFaceIndexingResult();
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

	public AggregateFaceIdentification identify(byte[] imageBytes, Map<String, Object> bundleMap) {
		log.info("identify called with image {} bytes", imageBytes.length);
		AggregateFaceIdentification faceIdentification = new AggregateFaceIdentification();
		try {

			String contentType = (String) bundleMap.get(Constants.CONTENT_TYPE);

			// build BufferedImage!
			InputStream inStream = new ByteArrayInputStream(imageBytes);
			BufferedImage targetImage = ImageIO.read(inStream);

			BufferedImage targetImageCopy = deepCopy(targetImage);

			CompletableFuture<List<Candidate>> openCVDetectionFuture =
				CompletableFuture.supplyAsync(() -> openCVDetection(targetImageCopy, contentType));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(targetImage, contentType.split("/")[1], baos);

			byte[] bytes = baos.toByteArray();

			CompletableFuture<List<Candidate>> awsCompletableFuture = CompletableFuture.supplyAsync(() -> {
					log.info("AWS Rekognition ---> async searching faces by image");
				try {
					return awsRekognitionService.searchFacesByImage(bytes);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					return ImmutableList.of();
				}
			});

			CompletableFuture<Optional<FaceIdentificationResult>> cognitiveCompletableFuture =
				CompletableFuture.supplyAsync(() -> {
					log.info("Cognitive ---> async searching faces by image");
					try {
						return cognitiveService.identifyFace(bytes);
					} catch (Exception e){
						log.error(e.getMessage(), e);
						return Optional.empty();
					}
				});

			CompletableFuture.allOf(awsCompletableFuture, cognitiveCompletableFuture, openCVDetectionFuture).get();

			if (awsCompletableFuture.isDone()) {
				List<Candidate> awsCandidateList = awsCompletableFuture.get();
				awsCandidateList.forEach(candidate -> {
					Optional<Identity> identityOptional =
						identityService.findIdentityByAwsFaceId(candidate.getPersonId());
					if (identityOptional.isPresent()) {
						candidate.setDbPersonId(String.valueOf(identityOptional.get().getId()));
					} else {
						log.warn("no face record found for cognitive person id {}", candidate.getPersonId());
					}
				});

				faceIdentification.addAll(awsCompletableFuture.get());
			}

			if (openCVDetectionFuture.isDone()) {
				faceIdentification.addAll(openCVDetectionFuture.get());
			}

			if (cognitiveCompletableFuture.isDone()) {

				Optional<FaceIdentificationResult> cognitiveResult = cognitiveCompletableFuture.get();
				if (cognitiveResult.isPresent()) {
					FaceIdentificationResult identificationResult = cognitiveResult.get();
					identificationResult.getCandidates().forEach(candidate -> {
						Optional<Identity> identityOptional =
							identityService.findIdentityByCognitivePersonId(candidate.getPersonId());
						if (identityOptional.isPresent()) {
							candidate.setDbPersonId(String.valueOf(identityOptional.get().getId()));
						} else {
							log.warn("no face record found for cognitive person id {}", candidate.getPersonId());
						}

						candidate.setRecognizer(Recognizer.COGNITIVE_SERVICES);
					});
					faceIdentification.addAll(identificationResult.getCandidates());
				}
			}
		} catch (IOException | InterruptedException | ExecutionException e) {
			log.error(e.getMessage(), e);
		}
		return faceIdentification;
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

			Optional<Identity> identityOptional = identityService.findIdentityById(pFace.getLabel());
			if (!identityOptional.isPresent()) {
				log.warn("no corresponding identity id found in database: {}", personId);
			} else {
				faceDbId = String.valueOf(identityOptional.get().getId());
			}

			Candidate candidate = new Candidate(personId, pFace.getConfidence(),
				FaceRectangle.from(pFace.getBox()), Recognizer.OPEN_CV);
			candidate.setDbPersonId(faceDbId);
			return candidate;
		}).collect(Collectors.toList());
	}

	private void drawDetectedFaceRectangle(BufferedImage targetImage, Rectangle rectangle, int label) {
		Graphics2D graphics = (Graphics2D) targetImage.getGraphics();
		graphics.setColor(Color.GREEN);
		graphics.setStroke(new BasicStroke(3));
		graphics.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
		graphics.drawString(label == 0 ? "Unknown" : "" + label, rectangle.x - 1, rectangle.y - 1);
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

	private byte[] bufferedImageToByteArray(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		return baos.toByteArray();
	}


}
