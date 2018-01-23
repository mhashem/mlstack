package co.rxstack.ml.core.context;

import co.rxstack.ml.core.extension.SpringExtension;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActorContext {

	private final SpringExtension springExtension;
	private final ApplicationContext applicationContext;

	@Autowired
	public ActorContext(ApplicationContext applicationContext, SpringExtension springExtension) {
		this.applicationContext = applicationContext;
		this.springExtension = springExtension;
	}

	@Bean
	public Config akkaConfiguration() {
		return ConfigFactory.load();
	}

	@Bean
	public ActorSystem actorSystem() {
		ActorSystem actorSystem = ActorSystem.create("akka-spring-actors", akkaConfiguration());
		springExtension.initialize(applicationContext);
		return actorSystem;
	}
	
}
