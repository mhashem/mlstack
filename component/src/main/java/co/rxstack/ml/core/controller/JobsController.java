package co.rxstack.ml.core.controller;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;

import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.service.impl.JobService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobsController {

	private static final Logger log = getLogger(JobsController.class);

	private final JobService jobService;

	@Autowired
	public JobsController(JobService jobService) {
		this.jobService = jobService;
	}

	@GetMapping("/api/v1/jobs")
	public ResponseEntity getLastNJobs() {
		// todo return running jobs info!
		return ResponseEntity.ok(ImmutableMap.of("jobs", ImmutableList.of()));
	}

	@PostMapping("/api/v1/jobs")
	public ResponseEntity pushNotification(
		@RequestParam("jobId")
			int jobId, HttpServletRequest request) {
		log.info("Request received from {} with type {}", request.getRemoteAddr(), jobId);
		if (jobId == 1002) {
			Ticket ticket = new Ticket(UUID.randomUUID().toString());
			ticket.setType(Ticket.Type.TRAINING);

			log.info("start training job");
			jobService.startJob(ticket);
			return ResponseEntity.accepted().body(ImmutableMap.of("ticket", ticket.getId()));
		} else {
			log.warn("Unknown jobId {}, discarding message", jobId);
		}
		return ResponseEntity.ok(ImmutableMap.of("received", "ok"));
	}


}
