package co.rxstack.ml.core.controller;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobsController {

	private static final Logger logger = getLogger(JobsController.class);
	
	@GetMapping("/api/v1/jobs")
	public ResponseEntity getLastNJobs() {
		// todo implement jobs api!
		List<String> jobs = Lists.newArrayList();
		IntStream.range(1, 10).forEach(value -> jobs.add(UUID.randomUUID().toString()));
		return ResponseEntity.ok(ImmutableMap.of("jobs", jobs));
	}
	
}
