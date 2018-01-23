package co.rxstack.ml.core.context;

import co.rxstack.ml.core.extension.SpringExtension;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActorContext {
	
	private final ApplicationContext applicationContext;

	@Autowired
	public ActorContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Bean
	public ActorSystem actorSystem() {
		ActorSystem actorSystem = ActorSystem.create("akka-spring-actors");
		SpringExtension.SPRING_EXTENSION_PROVIDER.get(actorSystem)
			.initialize(applicationContext);
		return actorSystem;
	}
	
}
