package co.rxstack.ml.aggregator.experimental;

import static org.bytedeco.javacpp.opencv_dnn.createCaffeImporter;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_dnn;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaffeGoogleNet {

	private static final Logger log = LoggerFactory.getLogger(CaffeGoogleNet.class);

	private opencv_dnn.Net caffeGoogleNet;

	public CaffeGoogleNet() {
		String modelTxt = "bvlc_googlenet.prototxt";
		String modelBin = "bvlc_googlenet.caffemodel";

		try {
			caffeGoogleNet = new opencv_dnn.Net();
/*
			File protobuf = new File(getClass().getResource("/caffe/bvlc_googlenet.prototxt").toURI());
			File caffeModel = new File(getClass().getResource("/caffe/bvlc_googlenet.caffemodel").toURI());
*/

			File protobuf = new File("C:\\Users\\mahmoud\\Downloads\\deep-learning-face-detection\\deep-learning-face-detection\\deploy.prototxt");
			File caffeModel = new File("C:\\Users\\mahmoud\\Downloads\\deep-learning-face-detection\\deep-learning-face-detection\\res10_300x300_ssd_iter_140000.caffemodel");

			//File vggProbuf = new File(getClass().getResource("/caffe/VGG_ILSVRC_16_layers_deploy.prototxt").toURI());
			//File vggCaffeModel = new File("C:\\Users\\mahmoud\\Downloads\\VGG_ILSVRC_16_layers.caffemodel");

			opencv_dnn.Importer importer =
				createCaffeImporter(protobuf.getAbsolutePath(), caffeModel.getAbsolutePath());
			importer.populateNet(caffeGoogleNet);
			importer.close(); // We don't need importer anymore
		} catch (Exception e) {
			log.error("Error reading prototxt", e);
			throw new IllegalStateException("Unable to start CNNGenderDetector", e);
		}
	}

	public void predict(opencv_core.Mat img) {
		opencv_imgproc.resize(img, img, new opencv_core.Size(300, 300)); //GoogLeNet accepts only 224x224 RGB-images
		opencv_dnn.Blob inputBlob = opencv_dnn.Blob.fromImages(img);//Convert Mat to 4-dimensional dnn::Blob from image
		//! [Prepare blob]

		//! [Set input blob]
		caffeGoogleNet.setBlob(".data", inputBlob);      //set the network input
		//! [Set input blob]

		//opencv_dnn.DictValue dictValue = new opencv_dnn.DictValue("detection_out");

		//! [Make forward pass]
		caffeGoogleNet.forward();                        //compute output
		//! [Make forward pass]

		//! [Gather output]
		opencv_dnn.Blob prob = caffeGoogleNet.getBlob("detection_out");      //gather output of "prob" layer

		opencv_core.Point classId = new opencv_core.Point();
		double[] classProb = new double[1];
		getMaxClass(prob, classId, classProb);//find the best class
		//! [Gather output]

		//! [Print results]
		//List<String> classNames = readClassNames();

		//System.out.println("Best class: #" + classId.x() + " '" + classNames.get(classId.x()) + "'");
		System.out.println("Best class: #" + classId.x());
		System.out.println("Probability: " + classProb[0] * 100 + "%");
		//! [Print results]

	}

	public List<String> readClassNames() {
		String filename = "synset_words.txt";
		List<String> classNames = null;

		try (BufferedReader br = new BufferedReader(
			new FileReader(new File(getClass().getResource("/caffe/synset_words.txt").toURI())))) {
			classNames = new ArrayList<>();
			String name = null;
			while ((name = br.readLine()) != null) {
				classNames.add(name.substring(name.indexOf(' ') + 1));
			}
		} catch (IOException ex) {
			System.err.println("File with classes labels not found " + filename);
			System.exit(-1);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return classNames;
	}

	/* Find best class for the blob (i. e. class with maximal probability) */
	public static void getMaxClass(opencv_dnn.Blob probBlob, opencv_core.Point classId, double[] classProb) {
		opencv_core.Mat probMat = probBlob.matRefConst().reshape(1, 1); //reshape the blob to 1x1000 matrix
		opencv_core.minMaxLoc(probMat.getUMat(opencv_core.ACCESS_READ), null, classProb, null, classId, null);
	}

	public static void main(String[] args) throws IOException {
		// CNNGenderDetector.class.getClassLoader().getResourceAsStream("books.jpg")
		BufferedImage image = ImageIO.read(new File("C:\\Users\\mahmoud\\Pictures\\People\\mohammad_shokor-hussein-khazzal.jpg"));
		CaffeGoogleNet caffeGoogleNet = new CaffeGoogleNet();
		caffeGoogleNet.predict(Java2DFrameUtils.toMat(image));
	}

}
