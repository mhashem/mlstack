package co.rxstack.ml.aggregator.model;

import static org.bytedeco.javacpp.opencv_core.IplImage;

import java.awt.Rectangle;

public class PotentialFaceWithImage extends PotentialFace {

	private IplImage image;

	public PotentialFaceWithImage(Rectangle box, int label, double confidence) {
		super(box, label, confidence);
	}

	public IplImage getImage() {
		return image;
	}

	public void setImage(IplImage image) {
		this.image = image;
	}

	public static PotentialFaceWithImage newUndefinedFaceWithImage(Rectangle box, IplImage iplImage) {
		PotentialFaceWithImage p = new PotentialFaceWithImage(box, 0, Double.NaN);
		p.setImage(iplImage);
		return p;
	}
}
