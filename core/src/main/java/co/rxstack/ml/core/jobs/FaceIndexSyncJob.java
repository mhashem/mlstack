package co.rxstack.ml.core.jobs;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.client.IStackClient;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FaceIndexSyncJob {

	private static final Logger log = LoggerFactory.getLogger(FaceIndexSyncJob.class);

	@Value("${face-collection-id}")
	private String tableName;
	private Stopwatch stopwatch;
	private final IStackClient stackClient;
	private final ICloudStorageService cloudStorageService;

	@Autowired
	public FaceIndexSyncJob(ICloudStorageService cloudStorageService, IStackClient stackClient) {
		this.cloudStorageService = cloudStorageService;
		this.stackClient = stackClient;
		this.stopwatch = Stopwatch.createUnstarted();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(this::execute, 1000, 60000, TimeUnit.MILLISECONDS);
	}

	private void execute() {
		try {
			stopwatch.start();
			log.info("Fired FaceIndexSyncJob to get indexed facesIds");
			Map<String, String> cloudIndexFaceIds = cloudStorageService.getCloudIndexFaceIds(tableName);
			log.info("Found {} indexed facesIds", cloudIndexFaceIds.size());
			stackClient.pushIndexedFacesIds(cloudIndexFaceIds);
			log.info("Pushed indexed facesIds successfully in {}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
			stopwatch.reset();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
