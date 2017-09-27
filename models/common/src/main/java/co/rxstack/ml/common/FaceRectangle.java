package co.rxstack.ml.common;

/**
 * @author mhachem on 9/27/2017.
 */
public class FaceRectangle {
	
	private int width;
	private int height;
	private int left;
	private int top;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int right) {
		this.top = right;
	}

	@Override
	public String toString() {
		return "FaceRectangle{" + "width=" + width + ", height=" + height + ", left=" + left + ", top=" + top + '}';
	}
}
