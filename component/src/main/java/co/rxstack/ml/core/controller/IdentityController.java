package co.rxstack.ml.core.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import co.rxstack.ml.faces.model.Identity;
import co.rxstack.ml.faces.service.IIdentityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityController {

	private static final Logger log = LoggerFactory.getLogger(IdentityController.class);

	private final IIdentityService identityService;

	@Autowired
	public IdentityController(IIdentityService identityService) {
		this.identityService = identityService;
	}

	@PostMapping("/api/v1/identity")
	public ResponseEntity saveIdentity(@RequestBody Identity identity) {
		log.info("intercepted {}", identity);
		Identity savedIdentity = identityService.save(identity);
		return ResponseEntity.ok(savedIdentity);
	}

	@GetMapping("/api/v1/identity/{id}")
	public ResponseEntity getIdentity(@PathVariable("id") int id, HttpServletRequest request) {
		log.info("intercepted {} from {}", id, request.getRemoteAddr());
		Optional<Identity> identityOptional = identityService.findById(id);
		if (identityOptional.isPresent()) {
			return ResponseEntity.ok(identityOptional.get());
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/api/v1/identity")
	public ResponseEntity getAllIdentities(HttpServletRequest request) {
		log.info("intercepted getAllIdentities(...) from {}", request.getRemoteAddr());
		return ResponseEntity.ok(identityService.findAll());
	}

}
