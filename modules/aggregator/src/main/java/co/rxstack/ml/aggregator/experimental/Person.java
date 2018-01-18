package co.rxstack.ml.aggregator.experimental;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class Person {

	private String name;
	private Path imagePath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Path getImagePath() {
		return imagePath;
	}

	public void setImagePath(Path imagePath) {
		this.imagePath = imagePath;
	}
}
