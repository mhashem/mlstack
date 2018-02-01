package co.rxstack.ml.awsrekognition;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import co.rxstack.ml.context.TestContext;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class CloudWatchTest {

	@Autowired
	private AWSStaticCredentialsProvider awsStaticCredentialsProvider;

	@Test
	public void doTest() {
		final AmazonCloudWatch cw =
			AmazonCloudWatchClientBuilder.standard().withRegion(Regions.fromName(TestContext.AWS_REGION))
				.withCredentials(awsStaticCredentialsProvider).build();

		Dimension dimension = new Dimension();
		dimension.setName("Operation");
		dimension.setValue("CompareFaces");

		DimensionFilter filter = new DimensionFilter();
		filter.setName("Operation");
		filter.setValue("SearchFacesByImage");

		/*ListMetricsRequest request =
			new ListMetricsRequest().withMetricName("SuccessfulRequestCount").withNamespace("AWS/Rekognition");

		boolean done = false;

		while (!done) {
			ListMetricsResult response = cw.listMetrics(request);

			for (Metric metric : response.getMetrics()) {
				System.out.printf("Retrieved metric %s", metric.getDimensions());
				System.out.println("------------------> " + metric);
			}

			request.setNextToken(response.getNextToken());

			if (response.getNextToken() == null) {
				done = true;
			}
		}*/

		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		GetMetricStatisticsRequest metricStatistics =
			new GetMetricStatisticsRequest().withPeriod(60).withNamespace("AWS/Rekognition")
				.withStatistics("Sum")
				.withStartTime(Date.from(LocalDateTime.now().minusDays(3).toInstant(ZoneOffset.UTC)))
				.withEndTime(Date.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)))
				.withMetricName("SuccessfulRequestCount").withDimensions(dimension);
		// SuccessfulRequestCount, DetectedFaceCount metric name
		GetMetricStatisticsResult result = cw.getMetricStatistics(metricStatistics);

		for (Datapoint datapoint : result.getDatapoints()) {
			System.out.printf("***************** Datapoint %s", datapoint);
		}
	}

}
