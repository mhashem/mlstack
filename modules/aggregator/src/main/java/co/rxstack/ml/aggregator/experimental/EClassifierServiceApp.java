package co.rxstack.ml.aggregator.experimental;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import co.rxstack.ml.aggregator.config.ClassifierConfig;
import co.rxstack.ml.aggregator.service.impl.EClassifierService;
import co.rxstack.ml.tensorflow.experimental.FaceNetLabeling;

public class EClassifierServiceApp {

	public static void main(String[] args) {

		ClassifierConfig config = new ClassifierConfig();
		config.setClassifierNamePrefix("svm");
		config.setClassifierPath("C:/etc/mlstack/model/svm");

		EClassifierService service = new EClassifierService(config);

		service.init();
	}

	public static Map<Integer, float[]> loadEmbeddings() throws FileNotFoundException {
		Map<Integer, float[]> embeddings = new HashMap<>();
		BufferedReader reader =
			new BufferedReader(new FileReader(
				new File("C:/etc/mlstack/models/tensorflow/facenet/embeddings/embeddings.csv")));
		reader.lines().forEach(line -> {
			String[] ee = line.split(",");
			float[] vector = new float[512];
			for (int i = 1; i < 513; i++) {
				vector[i - 1] = Float.valueOf(ee[i]);
			}
			embeddings.put(Integer.parseInt(ee[0]), vector);
		});
		return embeddings;
	}


}
