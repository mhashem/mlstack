package co.rxstack.ml.core.jobs.service.impl;

import java.util.EnumMap;
import java.util.Map;

import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.extension.SpringExtension;
import co.rxstack.ml.core.jobs.service.IJobService;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobService implements IJobService {

	private ActorSystem actorSystem;
	private SpringExtension springExtension;
	private Map<Ticket.Type, ActorRef> actorRefMap;

	@Autowired
	public JobService(ActorSystem actorSystem, SpringExtension springExtension) {
		Preconditions.checkNotNull(actorSystem);
		Preconditions.checkNotNull(springExtension);
		this.actorSystem = actorSystem;
		this.springExtension = springExtension;
		
		this.actorRefMap = new EnumMap<>(Ticket.Type.class);
		
		ActorRef indexingActorRef = actorSystem.actorOf(springExtension.props("indexingJob"), "indexer");
		ActorRef trainingActorRef = actorSystem.actorOf(springExtension.props("trainingJob"), "trainer");
		actorRefMap.put(Ticket.Type.INDEXING, indexingActorRef);
		actorRefMap.put(Ticket.Type.TRAINING, trainingActorRef);
	}

	public void startJob(Ticket ticket) {
		ActorRef actorRef = actorRefMap.get(ticket.getType());
		if (actorRef != null) {
			actorRef.tell(ticket, ActorRef.noSender());
		}
	}

}
