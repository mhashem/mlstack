package co.rxstack.ml.core;

import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.extension.SpringExtension;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CoreApplication implements CommandLineRunner {

	private final ActorSystem actorSystem;
	private final SpringExtension springExtension;

	@Autowired
	public CoreApplication(ActorSystem actorSystem, SpringExtension springExtension) {
		this.actorSystem = actorSystem;
		this.springExtension = springExtension;
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder()
			.bannerMode(Banner.Mode.CONSOLE)
			.sources(CoreApplication.class)
			.run(args);
	}

	@Override
	public void run(String... strings) throws Exception {
		// add any dependencies needed at start!
		Ticket ticket = new Ticket();
		ticket.setType(Ticket.Type.TRAINING);
		ActorRef actorRef = actorSystem.actorOf(springExtension.props("trainingJob"), "trainer");
		actorRef.tell(ticket, null);
	}
}
