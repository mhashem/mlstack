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
import co.rxstack.ml.aggregator.model.db.Face;
import co.rxstack.ml.aggregator.service.IFaceExtractorService;
import co.rxstack.ml.aggregator.service.IFaceRecognitionService;
import co.rxstack.ml.aggregator.service.IFaceService;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.model.CognitiveIndexingResult;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.AggregateFaceIdentification;
import co.rxstack.ml.common.model.AggregateFaceIndexingResult;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.Constants;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.Recognizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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

	private IFaceService faceService;
	private ICognitiveService cognitiveService;
	private IRekognitionService awsRekognitionService;
	private IFaceExtractorService faceExtractorService;
	private IFaceRecognitionService faceRecognitionService;

	@Autowired
	public AggregatorService(IFaceService faceService, IFaceExtractorService faceExtractorService,
		IFaceRecognitionService faceRecognitionService,
		IRekognitionService rekognitionService,
		ICognitiveService cognitiveService) {

		Preconditions.checkNotNull(faceService);
		Preconditions.checkNotNull(faceExtractorService);
		Preconditions.checkNotNull(faceRecognitionService);
		Preconditions.checkNotNull(rekognitionService);
		Preconditions.checkNotNull(cognitiveService);

		this.faceService = faceService;
		this.cognitiveService = cognitiveService;
		this.awsRekognitionService = rekognitionService;
		this.faceExtractorService = faceExtractorService;
		this.faceRecognitionService = faceRecognitionService;
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
				return awsRekognitionService.searchFacesByImage(bytes);
			});

			CompletableFuture<Optional<FaceIdentificationResult>> cognitiveCompletableFuture =
				CompletableFuture.supplyAsync(() -> {
					log.info("Cognitive ---> async searching faces by image");
					return cognitiveService.identifyFace(bytes);
				});

			CompletableFuture.allOf(awsCompletableFuture, cognitiveCompletableFuture, openCVDetectionFuture).get();

			if (awsCompletableFuture.isDone()) {
				List<Candidate> awsCandidateList = awsCompletableFuture.get();
				awsCandidateList.forEach(candidate -> {
					Optional<Face> faceOptional = faceService.getFaceByAwsFaceId(candidate.getPersonId());
					if (faceOptional.isPresent()) {
						candidate.setDbPersonId(faceOptional.get().getPersonId());
					} else {
						log.warn("no face record found for aws person id {}", candidate.getPersonId());
					}
				});

				faceIdentification.setAwsCandidates(awsCompletableFuture.get());
			}

			if (openCVDetectionFuture.isDone()) {
				faceIdentification.setOpenCVCandidates(openCVDetectionFuture.get());
			}

			if (cognitiveCompletableFuture.isDone()) {

				Optional<FaceIdentificationResult> cognitiveResult = cognitiveCompletableFuture.get();
				if (cognitiveResult.isPresent()) {
					FaceIdentificationResult identificationResult = cognitiveResult.get();
					identificationResult.getCandidates().forEach(candidate -> {
						Optional<Face> faceOptional = faceService.getFaceByCognitivePersonId(candidate.getPersonId());
						if (faceOptional.isPresent()) {
							candidate.setDbPersonId(faceOptional.get().getPersonId());
						} else {
							log.warn("no face record found for cognitive person id {}", candidate.getPersonId());
						}

						candidate.setRecognizer(Recognizer.COGNITIVE_SERVICES);
					});
					faceIdentification.setCognitiveCandidates(identificationResult.getCandidates());
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

			Face face = null;
			Optional<Face> faceOptional = faceService.getFaceByPersonId(personId);
			if (!faceOptional.isPresent()) {
				log.warn("no corresponding face id found in database: {}", personId);
			} else {
				face = faceOptional.get();
				faceDbId = face.getPersonId();
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

}
