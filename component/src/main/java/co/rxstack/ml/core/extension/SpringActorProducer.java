package co.rxstack.ml.core.extension;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

public class SpringActorProducer implements IndirectActorProducer {

	private String actorBeanName;
	private ApplicationContext applicationContext;

	public SpringActorProducer(ApplicationContext applicationContext, String actorBeanName) {
		this.applicationContext = applicationContext;
		this.actorBeanName = actorBeanName;
	}

	@Override
	public Actor produce() {
		return (Actor) applicationContext.getBean(actorBeanName);
	}

	@Override
	public Class<? extends Actor> actorClass() {
		return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
	}

}