package co.rxstack.ml.core.controller;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PushNotificationController {

	private static final Logger log = LoggerFactory.getLogger(PushNotificationController.class);

	@PostMapping("/api/v1/notification/push")
	public ResponseEntity<?> pushNotification(
		@RequestParam("notificationType")
			int notificationType, HttpServletRequest request) {
		log.info("Push Notification received from {} with type {}", request.getRemoteAddr(), notificationType);
		if (notificationType == 1001) {
			// new image added
			log.info("new image added");
		} else if (notificationType == 1002) {
			// start training job
			log.info("start training job notification");
		} else {
			log.warn("Unknown notification type, discarding message");
		}
		return ResponseEntity.ok(ImmutableMap.of("received", "ok"));
	}

}
