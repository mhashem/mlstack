package co.rxstack.ml.aggregator.impl;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvEqualizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_ROUGH_SEARCH;
import static org.bytedeco.javacpp.opencv_objdetect.cvHaarDetectObjects;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import co.rxstack.ml.aggregator.IFaceExtractorService;
import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.model.PotentialFace;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FaceExtractorService implements IFaceExtractorService {

	private static final Logger log = LoggerFactory.getLogger(FaceExtractorService.class);
	private static final opencv_core.CvMemStorage storage = opencv_core.CvMemStorage.create();
	private static final int F = 4;

	private FaceDBConfig faceDBConfig;
	private CvHaarClassifierCascade cascadeClassifier;

	@Autowired
	public FaceExtractorService(CvHaarClassifierCascade cascadeClassifier, FaceDBConfig faceDBConfig) {
		this.cascadeClassifier = cascadeClassifier;
		this.faceDBConfig = faceDBConfig;
	}

	@Override
	public Optional<byte[]> extractFace(byte[] imageBytes) throws IOException {
		log.info("extracting face from [image size: {} bytes]", imageBytes.length);
		MatOfRect faceDetections = new MatOfRect();
		Mat image = bufferedImageToMat(grayscale(ImageIO.read(new ByteArrayInputStream(imageBytes))));
		//cascadeClassifier.detectMultiScale(image, faceDetections);
		return faceDetections.toList().stream().map(rect -> rectByteFunction.apply(rect, image))
			.findAny();
	}

	@Override
	public List<byte[]> detectFaces(byte[] imageBytes) throws IOException {
		log.info("starting face detection [image size: {} bytes]", imageBytes.length);
		MatOfRect faceDetections = new MatOfRect();
		Mat image = bufferedImageToMat(grayscale(ImageIO.read(new ByteArrayInputStream(imageBytes))));
		//cascadeClassifier.detectMultiScale(image, faceDetections);
		return faceDetections.toList().stream().map(rect -> rectByteFunction.apply(rect, image))
			.collect(Collectors.toList());
	}

	@Override
	public List<PotentialFace> detectFaces(BufferedImage image) {
		opencv_core.cvClearMemStorage(storage);
		final IplImage iplImage = toTinyGray(image, null);
		final CvSeq cvSeq = cvHaarDetectObjects(iplImage, cascadeClassifier, storage, 1.1, 3,
			CV_HAAR_DO_ROUGH_SEARCH);
		final int N = cvSeq.total();
		final List<PotentialFace> ret = Lists.newArrayListWithCapacity(N);
		for (int i = 0; i < N; i++) {
			final CvRect r = new CvRect(cvGetSeqElem(cvSeq, i));
			final Rectangle box = new Rectangle(r.x() * F, r.y() * F, r.width() * F, r.height() * F);
			ret.add(PotentialFace.newUnIdentifiedFace(box));
		}
		return ret;
	}

	/**
	 * Images should be gray-scaled and scaled-down for faster calculations
	 */
	private IplImage toTinyGray(BufferedImage image, Pair<Integer, Integer> scale) {
		OpenCVFrameConverter.ToIplImage toIplImage = new OpenCVFrameConverter.ToIplImage();
		Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
		final IplImage iplImage =
			new IplImage(toIplImage.convert(java2DFrameConverter.getFrame(image)));
		if (scale == null) {
			scale = Pair.of(iplImage.width() / F, iplImage.height() / F);
		}
		final IplImage gray =
			IplImage.create(iplImage.width(), iplImage.height(), opencv_core.IPL_DEPTH_8U, 1);
		final IplImage tiny =
			IplImage.create(scale.getLeft(), scale.getRight(), opencv_core.IPL_DEPTH_8U, 1);

		log.info("image depth ---------------> depth {}, channels {}", iplImage.depth(), iplImage.nChannels());

		cvCvtColor(iplImage, gray, opencv_imgproc.CV_BGR2GRAY);   //todo: do tiny before gray
		cvResize(gray, tiny, opencv_imgproc.CV_INTER_AREA);
		cvEqualizeHist(tiny, tiny);
		return tiny;
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

	private Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3); // CV_8UC1 gray scale!
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}

	private Mat bufferedImageToMat2(BufferedImage image) throws IOException {
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

	/*public boolean matToBufferedImage(Mat matrix) {
		MatOfByte mb=new MatOfByte();
		opencv_imgcodecs.imencode(".jpg", matrix, mb);
		try {
			this.image = ImageIO.read(new ByteArrayInputStream(mb.toArray()));
		} catch (IOException e) {
			e.printStackTrace();
			return false; // Error
		}
		return true; // Successful
	}*/

}
