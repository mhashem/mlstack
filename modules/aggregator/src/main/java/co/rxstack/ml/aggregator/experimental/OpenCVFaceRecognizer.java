package co.rxstack.ml.aggregator.experimental;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.opencv.core.MatOfByte;

// import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
// import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

/**
 * I couldn't find any tutorial on how to perform face recognition using OpenCV and Java,
 * so I decided to share a viable solution here. The solution is very inefficient in its
 * current form as the training model is built at each run, however it shows what's needed
 * to make it work.
 * The class below takes two arguments: The path to the directory containing the training
 * faces and the path to the image you want to classify. Not that all images has to be of
 * the same size and that the faces already has to be cropped out of their original images
 * (Take a look here http://fivedots.coe.psu.ac.th/~ad/jg/nui07/index.html if you haven't
 * done the face detection yet).
 * For the simplicity of this post, the class also requires that the training images have
 * filename format: <label>-rest_of_filename.png. For example:
 * 1-jon_doe_1.png
 * 1-jon_doe_2.png
 * 2-jane_doe_1.png
 * 2-jane_doe_2.png
 * ...and so on.
 * Source: http://pcbje.com/2012/12/doing-face-recognition-with-javacv/
 *
 * @author Petter Christian Bjelland
 */
public class OpenCVFaceRecognizer {
	public static void main(String[] args) {
		String trainingDir = args[0];

		Mat testImage1 = imread(args[1], CV_LOAD_IMAGE_GRAYSCALE);
		Mat testImage2 = imread(args[2], CV_LOAD_IMAGE_GRAYSCALE);
		Mat testImage3 = imread(args[3], CV_LOAD_IMAGE_GRAYSCALE);

		File root = new File(trainingDir);

		FilenameFilter imgFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
			}
		};

		File[] imageFiles = root.listFiles(imgFilter);

		MatVector images = new MatVector(imageFiles.length);

		Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
		IntBuffer labelsBuf = labels.createBuffer();

		int counter = 0;

		for (File image : imageFiles) {
			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
			int label = Integer.parseInt(image.getName().split("\\-")[0]);
			images.put(counter, img);
			labelsBuf.put(counter, label);
			counter++;
		}

		//FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
		FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
		// FaceRecognizer faceRecognizer = createLBPHFaceRecognizer()

		faceRecognizer.train(images, labels);

		faceRecognizer.load("model.yml");

		IntPointer label = new IntPointer(1);
		DoublePointer confidence = new DoublePointer(1);
		faceRecognizer.predict(testImage1, label, confidence);
		int predictedLabel = label.get(0);

		System.out.println("Predicted label: " + predictedLabel + " with confidence " + confidence.get() / 100);

		faceRecognizer.predict(testImage2, label, confidence);
		System.out.println("Predicted label: " + label.get(0) + " with confidence " + confidence.get() / 100);

		faceRecognizer.predict(testImage3, label, confidence);
		System.out.println("Predicted label: " + label.get(0) + " with confidence " + confidence.get() / 100);


		// faceRecognizer.save("model.yml");

	}
}
