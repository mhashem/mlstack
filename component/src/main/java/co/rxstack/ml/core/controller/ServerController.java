package co.rxstack.ml.core.controller;

import java.time.LocalDateTime;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {

	@Value("${version}")
	private String version;

	@GetMapping("/")
	public ResponseEntity me() {
		return ResponseEntity.ok(ImmutableMap.of("version", "mlstack is running version " + version));
	}

	@GetMapping("/ready")
	public ResponseEntity ready() {
		return ResponseEntity.ok("ready");
	}

	@GetMapping("/ping")
	public ResponseEntity ping() {
		return ResponseEntity.ok(ImmutableMap.of(
			"ping", "pong",
			"status", "working",
			"time", LocalDateTime.now()));
	}

}
