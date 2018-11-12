package co.rxstack.ml.tensorflow;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import co.rxstack.ml.faces.service.IFaceService;
import co.rxstack.ml.faces.service.IIdentityService;
import co.rxstack.ml.tensorflow.config.FaceNetConfig;
import co.rxstack.ml.tensorflow.exception.GraphLoadingException;
import co.rxstack.ml.tensorflow.service.IFaceNetService;
import co.rxstack.ml.tensorflow.service.impl.FaceNetService;
import co.rxstack.ml.utils.ResourceHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import nu.pattern.OpenCV;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import smile.classification.AdaBoost;
import smile.classification.KNN;
import smile.classification.SVM;
import smile.math.distance.EuclideanDistance;
import smile.math.kernel.GaussianKernel;

@RunWith(MockitoJUnitRunner.class)
public class FaceNetServiceIT {
	
	@Mock
	private IFaceService faceService;
	@Mock
	private IIdentityService identityService;

	private FaceNetConfig faceNetConfig;
	private IFaceNetService faceNetService;
	
	private Map<Integer, List<BufferedImage>> imagesMap;
	private Map<Integer, BufferedImage> testImagesMap;
	
	@Before
	public void setup() throws GraphLoadingException, InterruptedException {
		
		OpenCV.loadLocally();
		
		faceNetConfig = new FaceNetConfig();
		faceNetConfig.setFaceNetGraphPath("C:/etc/mlstack/models/20180408-102900.pb"); // 20170511-185253.pb , 20180408-102900.pb
		faceNetConfig.setFeatureVectorSize(512);
		
		faceNetService = new FaceNetService(faceService, identityService, faceNetConfig);
		TimeUnit.SECONDS.sleep(3); // wait to load Graph file
		
		imagesMap = Maps.newHashMap();
		imagesMap.put(1, loadImages(ImmutableList.of("faces/mahmoud/1.jpg", "faces/mahmoud/2.jpg")));
		imagesMap.put(2, loadImages(ImmutableList.of("faces/miled/1.jpg", "faces/miled/2.jpg")));
		imagesMap.put(3, loadImages(ImmutableList.of("faces/jad/1.jpg", "faces/jad/2.jpg")));
		imagesMap.put(4, loadImages(ImmutableList.of("faces/hadi/1.jpg", "faces/hadi/2.jpg")));
		
		testImagesMap = Maps.newHashMap();
		testImagesMap.put(1, loadImage("faces/mahmoud/3.jpg"));
		testImagesMap.put(2, loadImage("faces/miled/3.jpg"));
		testImagesMap.put(3, loadImage("faces/jad/3.jpg"));
		testImagesMap.put(4, loadImage("faces/hadi/3.jpg"));
	}

	@After
	public void tearDown() {
		
	}
	
	@Test
	public void testComputeEmbeddingsFeaturesVector() throws IOException {
		int index = 0;
		double[][] features = new double[8][];
		for (Integer id : imagesMap.keySet()) {
			List<BufferedImage> imageList = imagesMap.get(id);
			List<double[]> vectors =
				imageList.stream()
					//.map(this::normalize)
					.filter(Objects::nonNull)
					.map(faceNetService::computeEmbeddingsFeaturesVector)
					.collect(Collectors.toList());
			for (double[] vector : vectors) {
				features[index] = vector;
				index++;
			}
		}

		int[] labels = new int[] { 0, 0, 1, 1, 2, 2, 3, 3 };
		
		AdaBoost classifier = adaBoost(features, labels);
		KNN<double[]> knn = knn(features, labels);
		
		SVM<double[]> svm = svm();
		svm.learn(features, labels);
		svm.finish();
		svm.trainPlattScaling(features, labels);


		for (int i = 0; i < testImagesMap.keySet().size(); i++) {
			double[] posteriori = new double[4];
			int predicted =
				svm.predict(faceNetService.computeEmbeddingsFeaturesVector(testImagesMap.get(i + 1)), posteriori);
			List<Double> posterioriList = Arrays.asList(ArrayUtils.toObject(posteriori));
			int maxIndex = posterioriList.indexOf(Collections.max(posterioriList));
			System.out.println("Predicted " + maxIndex + ", Actual " + i + " Confidences: " + Arrays.toString(posteriori) );
		}
	}

	private BufferedImage loadImage(String image) {
		try {
			return ResourceHelper.loadBufferedImage(FaceNetServiceIT.class, "/" + image);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private List<BufferedImage> loadImages(List<String> images) {
		return images.stream()
			.map(this::loadImage)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
	
	private BufferedImage normalize(BufferedImage image) {
		Mat m = new Mat();
		Core.normalize(img2Mat(image), m, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC3);
		return matToBufferedImage(m);
	}
	
	private KNN<double[]> knn(double[][] features, int[] labels) {
		return new KNN<>(features, labels, new EuclideanDistance(), 3);
	}
	
	private SVM<double[]> svm() {
		return new SVM<>(new GaussianKernel(1.0), 0.75, 4, SVM.Multiclass.ONE_VS_ALL);
	}
	
	private AdaBoost adaBoost(double[][] features, int[] labels) {
		return new AdaBoost(features, labels, labels.length);
	}

	private BufferedImage matToBufferedImage(Mat matrix) {
		MatOfByte mb = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mb);
		try {
			return ImageIO.read(new ByteArrayInputStream(mb.toArray()));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Mat img2Mat(BufferedImage in) {
		Mat out = new Mat(in.getHeight(), in.getWidth(), CV_8UC3);
		byte[] data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
		int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
		for (int i = 0; i < dataBuff.length; i++) {
			data[i * 3] = (byte) ((dataBuff[i]));
			data[i * 3 + 1] = (byte) ((dataBuff[i]));
			data[i * 3 + 2] = (byte) ((dataBuff[i]));
		}
		out.put(0, 0, data);
		return out;
	}


}
