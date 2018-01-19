package co.rxstack.ml.core;

import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.core.jobs.FaceModelTrainingJob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CoreApplication implements CommandLineRunner {

	@Autowired
	private IFaceRecognitionService faceRecognitionService;

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder()
			.bannerMode(Banner.Mode.CONSOLE)
			.sources(CoreApplication.class)
			.run(args);
	}

	@Override
	public void run(String... strings) throws Exception {
		// add any dependencies needed at start!
		new FaceModelTrainingJob(faceRecognitionService);
	}
}
