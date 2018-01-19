package co.rxstack.ml.aggregator.impl;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import co.rxstack.ml.aggregator.IFaceExtractorService;
import co.rxstack.ml.aggregator.experimental.config.FaceDBConfig;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FaceExtractorService implements IFaceExtractorService {

	private static final Logger log = LoggerFactory.getLogger(FaceExtractorService.class);

	private FaceDBConfig faceDBConfig;
	private CascadeClassifier cascadeClassifier;

	@Autowired
	public FaceExtractorService(CascadeClassifier cascadeClassifier, FaceDBConfig faceDBConfig) {
		this.cascadeClassifier = cascadeClassifier;
		this.faceDBConfig = faceDBConfig;
	}

	@Override
	public Optional<byte[]> extractFace(byte[] imageBytes) throws IOException {
		log.info("extracting face from [image size: {} bytes]", imageBytes.length);
		MatOfRect faceDetections = new MatOfRect();
		Mat image = bufferedImageToMat(grayscale(ImageIO.read(new ByteArrayInputStream(imageBytes))));
		cascadeClassifier.detectMultiScale(image, faceDetections);
		return faceDetections.toList().stream().map(rect -> rectByteFunction.apply(rect, image))
			.findAny();
	}

	@Override
	public List<byte[]> detectFaces(byte[] imageBytes) throws IOException {
		log.info("starting face detection [image size: {} bytes]", imageBytes.length);
		MatOfRect faceDetections = new MatOfRect();
		Mat image = bufferedImageToMat(grayscale(ImageIO.read(new ByteArrayInputStream(imageBytes))));
		cascadeClassifier.detectMultiScale(image, faceDetections);
		return faceDetections.toList().stream().map(rect -> rectByteFunction.apply(rect, image))
			.collect(Collectors.toList());
	}

	private BiFunction<Rect, Mat, byte[]> rectByteFunction = new BiFunction<Rect, Mat, byte[]>() {
		@Override
		public byte[] apply(Rect rect, Mat image) {
			Rect rectangle = new Rect(rect.x, rect.y, rect.width, rect.height);
			Mat mat = new Mat(image, rectangle);
			Mat resizeImage = new Mat();
			Size sz = new Size(faceDBConfig.getStandardWidth(), faceDBConfig.getStandardHeight());
			Imgproc.resize(mat, resizeImage, sz);
			int length = (int) (mat.total() * mat.elemSize());
			byte buffer[] = new byte[length];
			mat.get(0, 0, buffer);
			return buffer;
		}
	};

	private Mat bufferedImageToMat(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", byteArrayOutputStream);
		byteArrayOutputStream.flush();
		return Imgcodecs
			.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
	}

	private BufferedImage grayscale(BufferedImage img) {
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				Color c = new Color(img.getRGB(j, i));

				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);

				Color newColor = new Color(red + green + blue, red + green + blue, red + green + blue);

				img.setRGB(j, i, newColor.getRGB());
			}
		}

		return img;
	}

	public static Mat img2Mat(BufferedImage in) {
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
