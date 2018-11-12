package co.rxstack.ml.client.aws.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.Optional;

import co.rxstack.ml.client.aws.IMachineLearningClient;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.machinelearning.AmazonMachineLearning;
import com.amazonaws.services.machinelearning.AmazonMachineLearningClientBuilder;
import com.amazonaws.services.machinelearning.model.GetMLModelRequest;
import com.amazonaws.services.machinelearning.model.GetMLModelResult;
import com.amazonaws.services.machinelearning.model.PredictRequest;
import com.amazonaws.services.machinelearning.model.PredictResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

public class MachineLearningClient implements IMachineLearningClient {

	private static final Logger logger = getLogger(MachineLearningClient.class);
	
	private AmazonMachineLearning client;

	private final String PREDICTION_ENDPOINT;
	private final String MODEL_ID = "ml-MTQYNY9qdjZ"; //e3 "ml-bhYeZclMVQW"; //e2 "ml-I32M31QvWVg"; // e1 "ml-2HsB78gKdwy";
	
	public MachineLearningClient(String awsRegion, AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		Preconditions.checkNotNull(awsRegion);
		Preconditions.checkNotNull(awsStaticCredentialsProvider);
		client = AmazonMachineLearningClientBuilder.standard()
			.withCredentials(awsStaticCredentialsProvider)
			.withRegion(awsRegion)
			.build();
		
		this.PREDICTION_ENDPOINT = getModelEndpoint();
	}
	
	@Override
	public Optional<PredictResult> predict(double[] vector) {
		logger.info("Launching Prediction for vector {}", vector);
		PredictRequest request = new PredictRequest();
		request.withMLModelId(MODEL_ID)
			.withPredictEndpoint(PREDICTION_ENDPOINT)
			.withRecord(getValuesMap(vector));
		return Optional.ofNullable(client.predict(request));
	}
	
	private Map<String, String> getValuesMap(double[] vector) {
		int index = 2;
		Map<String, String> values = Maps.newHashMap();
		for (double v: vector) {
			values.put("Var" + getNumberPadded(index), String.valueOf(v));
			index++;
		}
		return values;
	}
	
	private String getModelEndpoint() { 
		GetMLModelRequest request = new GetMLModelRequest().withMLModelId(MODEL_ID); 
		GetMLModelResult model = client.getMLModel(request);
		return model.getEndpointInfo().getEndpointUrl();
	}

	private String getNumberPadded(int n) {
		int length = String.valueOf(n).length();
		switch (length) {
		case 1:
			return "00" + n;
		case 2:
			return "0" + n;
		default:
			return String.valueOf(n);
		}
	}
}
