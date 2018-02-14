package co.rxstack.ml.core.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.service.impl.JobService;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

	private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

	private final JobService jobService;

	@Autowired
	public NotificationController(JobService jobService) {
		this.jobService = jobService;
	}

	@PostMapping("/api/v1/notification/push")
	public ResponseEntity pushNotification(
		@RequestParam("notificationType")
			int notificationType, HttpServletRequest request) {
		log.info("Push Notification received from {} with type {}", request.getRemoteAddr(), notificationType);
		if (notificationType == 1001) {
			// new image added
			log.info("new image added");
		} else if (notificationType == 1002) {
			log.info("start training job notification");
			Ticket ticket = new Ticket(UUID.randomUUID().toString());
			ticket.setType(Ticket.Type.TRAINING);
			jobService.startJob(ticket);
			return ResponseEntity.accepted().body(ImmutableMap.of("ticket", ticket.getId()));
		} else {
			log.warn("Unknown notification type, discarding message");
		}
		return ResponseEntity.ok(ImmutableMap.of("received", "ok"));
	}

}
