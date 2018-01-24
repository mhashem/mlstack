package co.rxstack.ml.core.jobs.actors;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.dao.JobDao;
import co.rxstack.ml.core.jobs.model.Job;
import co.rxstack.ml.core.jobs.model.JobStatus;

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

	private Job job;
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
		
		job = new Job();
		job.setName("Training Job");
		job.setStartDate(Instant.now());
		job.setStatus(JobStatus.RUNNING.getStatus());
		job.setTicketId(((Ticket)message).getId());
		job = jobDao.save(job);
		
		log.info("job id {}", job.getId());
		try {
			handle();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			handleFailure(e);
			return;
		}
		log.info("finished onReceive(...)");
		handleSuccess();
	}

	private void handle() {
		log.info("fired training actor");
		faceRecognitionService.trainModel();
	}

	private void handleSuccess() {
		job.setStatus(JobStatus.STOPPED.getStatus());
		job.setEndDate(Instant.now());
		jobDao.save(job);
	}
	
	private void handleFailure(Exception e) {
		job.setStatus(JobStatus.FAILED.getStatus());
		job.setData(e.getMessage());
		job.setEndDate(Instant.now());
		jobDao.save(job);
	}
}
