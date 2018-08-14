package co.rxstack.ml.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	/**
	 * Here we define the endpoint our client will use to connect to the server
	 * and what origins server can receive connections from
	 *
	 * @param registry {@link StompEndpointRegistry}
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/socket")
			.setAllowedOrigins("*")
			.withSockJS();
	}

	/**
	 * Here we define App prefix when sending requests, and one subscription /recognitions!
	 *
	 * @param registry {@link StompEndpointRegistry}
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app")
			.enableSimpleBroker("/recognitions");
	}
}

