package co.rxstack.ml.aws.rekognition.model;

public class BoundingBox {
	private double height;
	private double width;
	private double left;
	private double top;

	public void setHeight(double height) {
		this.height = height;
	}

	public double getHeight() {
		return height;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getWidth() {
		return width;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getLeft() {
		return left;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getTop() {
		return top;
	}

	@Override
	public String toString() {
		return "BoundingBox{" + "height=" + height + ", width=" + width + ", left=" + left + ", top=" + top + '}';
	}
}
