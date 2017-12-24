package co.rxstack.ml.aggregator.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import co.rxstack.ml.aggregator.IOpenCVService;

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
public class OpenCVService implements IOpenCVService {

	private static final Logger log = LoggerFactory.getLogger(OpenCVService.class);

	private CascadeClassifier faceDetector;

	@Autowired
	public OpenCVService(CascadeClassifier faceDetector) {
		this.faceDetector = faceDetector;
	}

	@Override
	public List<byte[]> detectFaces(byte[] imageBytes) throws IOException {
		log.info("starting face detection [image size: {} bytes]", imageBytes.length);
		MatOfRect faceDetections = new MatOfRect();
		Mat image = bufferedImageToMat(grayscale(ImageIO.read(new ByteArrayInputStream(imageBytes))));
		faceDetector.detectMultiScale(image, faceDetections);
		return faceDetections.toList().stream().map(rect -> {
			Rect rectangle = new Rect(rect.x, rect.y, rect.width, rect.height);
			Mat mat = new Mat(image, rectangle);
			Mat resizeImage = new Mat();
			Size sz = new Size(140, 140);
			Imgproc.resize(mat, resizeImage, sz);
			int length = (int) (mat.total() * mat.elemSize());
			byte buffer[] = new byte[length];
			mat.get(0, 0, buffer);
			return buffer;
		}).collect(Collectors.toList());
	}

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

}
