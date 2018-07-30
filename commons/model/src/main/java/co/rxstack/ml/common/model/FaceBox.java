package co.rxstack.ml.common.model;

import java.io.Serializable;

public class FaceBox implements Serializable {

	private int index;
	private int top;
	private int bottom;
	private int right;
	private int left;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}

	public int getRight() {
		return right;
	}

	public void setRight(int right) {
		this.right = right;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public FaceRectangle mapToFaceRectangle() {
		return new FaceRectangle(this.left, this.top, this.right, this.bottom);
	}

	@Override
	public String toString() {
		return "FaceBox{" + "index=" + index + ", top=" + top + ", bottom=" + bottom + ", right=" + right + ", left="
			+ left + '}';
	}
}
