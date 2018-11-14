package co.rxstack.ml.client.preprocessor.v2;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import co.rxstack.ml.client.preprocessor.IPreprocessorClient;
import co.rxstack.ml.common.model.FaceBox;
import com.google.common.collect.Lists;
import net.tzolov.cv.mtcnn.FaceAnnotation;
import net.tzolov.cv.mtcnn.MtcnnService;
import org.datavec.image.loader.Java2DNativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;

public class CNNPreprocessorClient implements IPreprocessorClient<byte[], List<BufferedImage>> {

	private static final Logger logger = getLogger(CNNPreprocessorClient.class);
	
	private MtcnnService mtcnnService;
	private Java2DNativeImageLoader imageLoader;
	
	public CNNPreprocessorClient() {
		this.mtcnnService = new MtcnnService(30, 0.709, new double[] { 0.6, 0.7, 0.7 });
		this.imageLoader = new Java2DNativeImageLoader();
	}

	@Override
	public List<BufferedImage> align(byte[] imageBytes) {
		List<BufferedImage> bufferedImages = Lists.newArrayList();
		try {
			INDArray originalImage = imageLoader.asMatrix(imageBytes).get(point(0), all(), all(), all()).dup();
			FaceAnnotation[] faceAnnotations = mtcnnService.faceDetection(originalImage);
			for (FaceAnnotation faceAnnotation : faceAnnotations) {
				INDArray alignedFace = mtcnnService.faceAlignment(originalImage, faceAnnotation, 44, 160, true);
				bufferedImages.add(imageLoader.asBufferedImage(alignedFace));
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return bufferedImages;
	}

	@Override
	public List<FaceBox> detectFaces(byte[] imageBytes) {
		return null;
	}
	
	
}
