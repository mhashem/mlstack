package co.rxstack.ml.core.jobs;

import static org.slf4j.LoggerFactory.getLogger;

import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.common.model.Ticket;

import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrainingActor extends UntypedActor {

	private static final Logger logger = getLogger(TrainingActor.class);

	private final IFaceRecognitionService faceRecognitionService;

	@Autowired
	public TrainingActor(IFaceRecognitionService faceRecognitionService) {
		this.faceRecognitionService = faceRecognitionService;
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		logger.info("training actor received message");
		if (message instanceof Ticket) {
			Ticket ticket = (Ticket) message;
			switch (ticket.getType()) {
			case TRAINING:
				faceRecognitionService.trainModel();
				break;
			case INDEXING:
				break;
			case SEARCH:
				break;
			default:
				break;
			}
		}
	}
}
