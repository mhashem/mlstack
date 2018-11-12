package co.rxstack.ml.utils;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.amazonaws.util.IOUtils;

/**
 * @author mhachem on 10/2/2017.
 */
public class ResourceHelper {

	public static byte[] loadResourceAsByteArray(Class clazz, String imageName) throws IOException {
		return toByteArray(loadImage(clazz, imageName));
	}

	public static InputStream loadImage(Class clazz, String imageName) {
		return clazz.getClassLoader().getResourceAsStream(imageName);
	}

	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		return IOUtils.toByteArray(inputStream);
	}

	public static InputStream bytes2InputStream(byte[] imageBytes) {
		return new ByteArrayInputStream(imageBytes);
	}

	public static String getFullPath(Class clazz, String fileName) throws URISyntaxException {
		URL url = clazz.getClassLoader().getResource(fileName);
		return Paths.get(url.toURI()).toFile().getAbsolutePath();
	}
	
	public static BufferedImage loadBufferedImage(Class clazz, String path) throws IOException {
		return ImageIO.read(clazz.getResourceAsStream(path));
	}

	public static void main(String[] args) throws AWTException {
		Robot robot = new Robot();
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}

}

