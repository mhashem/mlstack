package co.rxstack.ml.core.controller;

import java.time.LocalDateTime;

import com.google.common.collect.ImmutableMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {

	@GetMapping("/api/v1/ping")
	public ResponseEntity ping() {
		return ResponseEntity.ok(ImmutableMap.of(
			"ping", "pong",
			"status", "working",
			"time", LocalDateTime.now()));
	}

}
