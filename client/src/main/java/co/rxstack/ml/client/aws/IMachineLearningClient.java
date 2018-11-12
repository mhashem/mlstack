package co.rxstack.ml.client.aws;

import java.util.Optional;

import com.amazonaws.services.machinelearning.model.PredictResult;

public interface IMachineLearningClient {

	Optional<PredictResult> predict(double[] vector);
}
