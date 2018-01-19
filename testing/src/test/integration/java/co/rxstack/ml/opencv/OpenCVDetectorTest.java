package co.rxstack.ml.opencv;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import co.rxstack.ml.context.TestContext;
import co.rxstack.ml.utils.ResourceHelper;

import com.google.common.base.Stopwatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class OpenCVDetectorTest {

	@Autowired
	private CascadeClassifier cascadeClassifier;

	private InputStream imageStream;

	@Before
	public void setup() throws URISyntaxException {
		imageStream = ResourceHelper.loadImage(OpenCVDetectorTest.class, "multi/multi-faces-4f-1.jpg");
	}

	@Test
	public void testDetectFaces() throws IOException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Mat image = BufferedImage2Mat(grayscale(ImageIO.read(imageStream)));
		MatOfRect faceDetections = new MatOfRect();
		cascadeClassifier.detectMultiScale(image, faceDetections);
		System.out
			.printf("-------> detected %d faces in %dms\n", faceDetections.toArray().length, stopwatch.elapsed(TimeUnit.MILLISECONDS));

		int i = 0;
		for (Rect rect : faceDetections.toList()) {
			//Imgproc.rectangle(image, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 2);
			Rect rectangle = new Rect(rect.x, rect.y, rect.width, rect.height);
			Mat mat = new Mat(image, rectangle);
			Mat resizeimage = new Mat();
			Size sz = new Size(140, 140);
			Imgproc.resize(mat, resizeimage, sz);
			Imgcodecs.imwrite(
				"C:\\Users\\mahmoud\\Documents\\Projects\\mlstack\\testing\\src\\test\\integration\\resources\\output\\face_"
					+ i + ".jpg", resizeimage);
			i++;
		}

		Imgcodecs.imwrite(
			"C:\\Users\\mahmoud\\Documents\\Projects\\mlstack\\testing\\src\\test\\integration\\resources\\multi\\multi-faces-detections.jpg",
			image);
	}

	private Mat BufferedImage2Mat(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", byteArrayOutputStream);
		byteArrayOutputStream.flush();
		return Imgcodecs
			.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
	}

	public BufferedImage grayscale(BufferedImage img) {
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
