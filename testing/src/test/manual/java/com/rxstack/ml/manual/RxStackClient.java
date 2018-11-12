package com.rxstack.ml.manual;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.rxstack.ml.common.model.FaceRecognitionResult;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.util.UriComponentsBuilder;

public class RxStackClient {

	private static final Logger logger = getLogger(RxStackClient.class);

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	private URI host;
	private SockJsClient sockJsClient;
	private StompSession stompSession;

	private RxStackClient(String host) {
		this.host = URI.create(host);
	}

	public static RxStackClient createDefaultClient() {
		return new RxStackClient("http://localhost:8082/mlstack");
	}

	public static RxStackClient createClient(String host) {
		return new RxStackClient(host);
	}

	public void index(int id, String name, Path imagePath) throws UnirestException {
		String url =
			UriComponentsBuilder.fromUri(host).path("/api/v1/faces/{personId}/index").buildAndExpand(id).toString();
		HttpResponse<JsonNode> response =
			Unirest.post(url).field("personName", name).field("faceImage", imagePath.toFile()).asJson();
	}

	public void recognize(Path imagePath) throws UnirestException {
		HttpResponse<JsonNode> response =
			Unirest.post(UriComponentsBuilder.fromUri(host).path("/api/v1/faces/recognition").toUriString())
				.field("image", imagePath.toFile()).asJson();
		logger.info("Response: {}", response.getStatus());
	}

	public void startWebSocketConnection() {
		executorService.submit(() -> {
			this.webSocket();
			new Scanner(System.in).nextLine(); // Don't close immediately.
		});
	}
	
	public void stopWebSocketConnection() {
		stompSession.disconnect();
		sockJsClient.stop();
	}

	private void webSocket() {
		logger.info("Starting WebSocket connections");

		List<Transport> transports = new ArrayList<Transport>(2);
		transports.add(new WebSocketTransport(new StandardWebSocketClient()));
		transports.add(new RestTemplateXhrTransport());

		sockJsClient = new SockJsClient(transports);
		WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		StompSessionHandler sessionHandler = new CustomStompMessageHandler();
		try {
			stompSession =
				stompClient.connect(UriComponentsBuilder.fromUri(host).path("/socket").toUriString(), sessionHandler)
					.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage(), e);
		}
	}

	class CustomStompMessageHandler implements StompSessionHandler {

		@Override
		public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
			stompSession.subscribe("/recognitions", this);
		}

		@Override
		public void handleException(StompSession stompSession, StompCommand stompCommand, StompHeaders stompHeaders,
			byte[] bytes, Throwable throwable) {
			logger.error("Got an exception", throwable);
			System.err.println(throwable);
		}

		@Override
		public void handleTransportError(StompSession stompSession, Throwable throwable) {
			logger.error("Got an exception", throwable);
			System.err.println(throwable);
		}

		@Override
		public Type getPayloadType(StompHeaders stompHeaders) {
			return FaceRecognitionResult[].class;
		}

		@Override
		public void handleFrame(StompHeaders stompHeaders, Object o) {
			System.err.println("W " + ((FaceRecognitionResult[]) o)[0].toString());
		}
	}

}
