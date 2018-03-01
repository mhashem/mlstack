package co.rxstack.ml.aggregator.utils;

import java.awt.image.BufferedImage;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect;

public class Preprocess {
	
	public static Mat mirror(BufferedImage bufferedImage) {
		Mat dst = new Mat();
		
		return dst;
	}
	
	public static Mat mirror(Mat src) {
		Mat dst = new Mat();
		opencv_core.flip(src, dst, 1);
		return dst;
	}
	
	public static void hogDescription() {

		//opencv_objdetect.HOGDescriptor hogDescriptor = new opencv_objdetect.HOGDescriptor();

	}
	
	
}
