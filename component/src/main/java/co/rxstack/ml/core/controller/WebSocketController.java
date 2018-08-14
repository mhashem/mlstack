package co.rxstack.ml.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

	private final SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	public WebSocketController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	@MessageMapping("/send/message")
	public void onReceiveMessage(String message) {
		this.simpMessagingTemplate
			.convertAndSend("/recognitions", message);
	}

}
