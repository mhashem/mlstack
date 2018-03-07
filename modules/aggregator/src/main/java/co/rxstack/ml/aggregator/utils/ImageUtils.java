package co.rxstack.ml.aggregator.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtils {

	public static byte[] bufferedImageToByteArray(BufferedImage bufferedImage) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", baos);
		return baos.toByteArray();
	}

	public static BufferedImage bytesToBufferedImage(byte[] imageBytes) throws IOException {
		InputStream inStream = new ByteArrayInputStream(imageBytes);
		return ImageIO.read(inStream);
	}

}
