package co.rxstack.ml.core.jobs;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.UUID;

import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.dao.JobDao;
import co.rxstack.ml.core.jobs.model.Job;

import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TrainingJob extends UntypedActor {

	private static final Logger log = getLogger(TrainingJob.class);

	private JobDao jobDao;
	private final IFaceRecognitionService faceRecognitionService;

	@Autowired
	public TrainingJob(IFaceRecognitionService faceRecognitionService, JobDao jobDao) {
		this.faceRecognitionService = faceRecognitionService;
		this.jobDao = jobDao;
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		log.info("fired onReceive({})", message);

		Job job = new Job();
		job.setName("Training Job");
		job.setStartDate(Instant.now());
		job.setStatus("running...");
		Job savedJob = jobDao.save(job);
		log.info("Job Id {}", savedJob.getId());
		Thread.sleep(10000L);
		try {
			handle(message);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("finished onReceive(...)");
		jobDao.delete(savedJob);
	}

	private void handle(Object message) {
		log.info("training actor received message");
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
