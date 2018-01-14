package co.rxstack.ml.common.model;

import java.util.Locale;

/**
 * @author mhachem on 9/27/2017.
 */
public class FaceRectangle {

	private float width;
	private float height;
	private float left;
	private float top;

	public FaceRectangle() {
		// needed for Object Mapper
	}

	public FaceRectangle(float left, float top, float width, float height) {
		this.width = width;
		this.height = height;
		this.left = left;
		this.top = top;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getLeft() {
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	public float getTop() {
		return top;
	}

	public void setTop(float top) {
		this.top = top;
	}

	public String encodeAsQueryParam() {
		return String.format(Locale.getDefault(), "%f,%f,%f,%f", left, top, height, width);
	}

	@Override
	public String toString() {
		return "FaceRectangle{" + "width=" + width + ", height=" + height + ", left=" + left + ", top=" + top + '}';
	}
}
