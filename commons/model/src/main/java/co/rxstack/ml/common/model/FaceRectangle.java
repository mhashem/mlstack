package co.rxstack.ml.common.model;

import java.util.Locale;

/**
 * @author mhachem on 9/27/2017.
 */
public class FaceRectangle {

	private int width;
	private int height;
	private int left;
	private int top;

	public FaceRectangle() {
		// needed for Object Mapper
	}

	public FaceRectangle(int left, int top, int width, int height) {
		this.width = width;
		this.height = height;
		this.left = left;
		this.top = top;
	}

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

	public void setTop(int top) {
		this.top = top;
	}

	public String encodeAsQueryParam() {
		return String.format(Locale.ENGLISH, "%1d,%2d,%3d,%4d", left, top, height, width);
	}

	@Override
	public String toString() {
		return "FaceRectangle{" + "width=" + width + ", height=" + height + ", left=" + left + ", top=" + top + '}';
	}
}
