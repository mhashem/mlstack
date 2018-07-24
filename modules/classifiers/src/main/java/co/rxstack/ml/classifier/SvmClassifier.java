package co.rxstack.ml.classifier;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import smile.classification.NeuralNetwork;
import smile.classification.SVM;
import smile.math.kernel.GaussianKernel;

/**
 * Check http://haifengl.github.io/smile/index.html
 * Check http://haifengl.github.io/
 * Check https://github.com/haifengl/smile
 */
public class SvmClassifier {

	private static double gamma = 1.0;
	private static double C = 1.0;

	private static int[] y = { 0, 0, 0, 1, 1, 1, 2, 2, 2 };

	private static double[][] x = { { 3.5, 1, 1, 1, 0, 1, 0, 1, 247537 }, { 2.5, 0, 0, 0, 1, 1, 0, 0, 73960 },
		{ 3.5, 0, 0, 0, 0, 1, 1, 0, 82671 }, { 4.0, 1, 1, 0, 0, 1, 0, 0, 137456 }, { 5.0, 1, 0, 0, 0, 1, 0, 0, 96810 },
		{ 4.5, 0, 1, 1, 0, 1, 1, 0, 138090 }, { 4.0, 0, 0, 0, 0, 1, 1, 0, 77204 }, { 3.5, 0, 0, 0, 1, 1, 0, 1, 86838 },
		{ 3.5, 0, 1, 0, 1, 1, 0, 1, 146886 }
	};

	public static void main(String[] args) throws IOException {
		SVM<double[]> classifier = new SVM<>(new GaussianKernel(gamma), C, 3, SVM.Multiclass.ONE_VS_ONE);

		Map<String, Integer> classesMap = new HashMap<>();
		classesMap.put("Iris-setosa", 0);
		classesMap.put("Iris-versicolor", 1);
		classesMap.put("Iris-virginica", 2);

		List<IrisSpecs> irisSpecs = new ArrayList<>();
		List<Integer> classes = new ArrayList<>();
		List<double[]> features = new ArrayList<>();

		CSVFormat.DEFAULT.withFirstRecordAsHeader().withSkipHeaderRecord()
			.parse(new FileReader(SvmClassifier.class.getClassLoader().getResource("Iris.csv").getFile()))
			.forEach(record -> {

				IrisSpecs specs = new IrisSpecs();
				specs.sepalLengthCm = Double.parseDouble(record.get(1));
				specs.sepalWidthCm = Double.parseDouble(record.get(2));
				specs.petalLengthCm = Double.parseDouble(record.get(3));
				specs.petalWidthCm = Double.parseDouble(record.get(4));
				specs.cls = classesMap.get(record.get(5));
				
				irisSpecs.add(specs);
			});
		
		Collections.shuffle(irisSpecs);

		for (IrisSpecs irisSpec : irisSpecs) {
			features.add(irisSpec.toArray());
			classes.add(irisSpec.getCLs());
		}
		
		
		double[][] featuresArray = features.stream().map(vector -> vector).toArray(double[][]::new);
		int[] labelsArray = classes.stream().mapToInt(ii -> ii).toArray();

		double[][] trainingFeaturesArray = Arrays.copyOfRange(featuresArray, 0, featuresArray.length - 10);
		int[] trainingLabelsArray = Arrays.copyOfRange(labelsArray, 0, labelsArray.length - 10);
		
		double[][] testFeaturesArray =
			Arrays.copyOfRange(featuresArray, featuresArray.length - 10, featuresArray.length);

		int[] testLablesArray = Arrays.copyOfRange(labelsArray, labelsArray.length - 10, labelsArray.length);
		
		for (int i = 0; i < 1; i++) {
			classifier.learn(trainingFeaturesArray, trainingLabelsArray);
		}

		classifier.finish();

		int p = classifier.predict(new double[] { 5.9, 3.0, 5.1, 1.8 });

		System.out.println(p);

		System.out.println(errorRate(new int[] { 2 }, new int[] { p }));

		System.out.println("----------------------------------------------------------");

		NeuralNetwork net =
			new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, 
				NeuralNetwork.ActivationFunction.SOFTMAX, 4,
				100, labelsArray.length);

		for (int i = 0; i < 500; i++) {
			if (i %  5 == 0) {
				System.out.println("Training Epoch " + i + " [" + net.getLearningRate() + "]");
			}
			net.learn(trainingFeaturesArray, trainingLabelsArray);
		}

		int[] pp = new int[testFeaturesArray.length];

		for (int i = 0; i < pp.length; i++) {
			pp[i] = classifier.predict(testFeaturesArray[i]);
		}

		System.out.println(errorRate(testLablesArray, pp));
	}

	/**
	 * Returns the errorRate rate.
	 */
	static double errorRate(int[] x, int[] y) {
		int e = 0;
		for (int i = 0; i < x.length; i++) {
			if (x[i] != y[i]) {
				e++;
			}
		}
		return (double) e / x.length;
	}

	static class IrisSpecs {
		double sepalLengthCm;
		double sepalWidthCm;
		double petalLengthCm;
		double petalWidthCm;
		int cls;

		double[] toArray() {
			return new double[] { sepalLengthCm, sepalWidthCm, petalLengthCm, petalWidthCm };
		}
		
		int getCLs() {
			return cls;
		}

	}

}
