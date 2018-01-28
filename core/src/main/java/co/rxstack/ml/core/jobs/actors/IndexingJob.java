package co.rxstack.ml.core.jobs.actors;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.aggregator.service.impl.AggregatorService;
import co.rxstack.ml.common.model.AggregateFaceIndexingResult;
import co.rxstack.ml.common.model.Constants;
import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.IndexingQueue;
import co.rxstack.ml.aggregator.dao.FaceDao;
import co.rxstack.ml.aggregator.model.db.Face;

import akka.actor.UntypedActor;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexingJob extends UntypedActor {

	private static final Logger logger = getLogger(IndexingJob.class);

	private FaceDao faceDao;
	private IndexingQueue indexingQueue;
	private AggregatorService aggregatorService;

	@Autowired
	public IndexingJob(FaceDao faceDao, AggregatorService aggregatorService, IndexingQueue indexingQueue) {
		this.aggregatorService = aggregatorService;
		this.indexingQueue = indexingQueue;
		this.faceDao = faceDao;
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		logger.info("IndexingJob fired with message {}", message);
		Stopwatch stopwatch = Stopwatch.createStarted();
		List<Ticket> tickets = indexingQueue.getTickets();
		indexingQueue.clear();
		if (tickets.isEmpty()) {
			logger.info("no tickets found in indexing queue");
			return;
		}
		logger.info("found {} tickets in indexing queue", tickets.size());
		for (Ticket ticket : tickets) {
			Optional<AggregateFaceIndexingResult> faceIndexingResultOptional = aggregatorService
				.indexFaces(ticket.getImageBytes(), ImmutableMap
					.of(Constants.PERSON_ID, ticket.getPersonId(), Constants.PERSON_NAME, ticket.getPersonName()))
				.stream().findAny();
			if (faceIndexingResultOptional.isPresent()) {
				logger.info("Indexing result {}", faceIndexingResultOptional.get());
				AggregateFaceIndexingResult faceIndexingResult = faceIndexingResultOptional.get();
				Face face = new Face();
				face.setPersonId(ticket.getPersonId());
				face.setAwsFaceId(faceIndexingResult.awsFaceId);
				face.setCognitivePersonId(faceIndexingResult.cognitivePersonId);
				face.setCreationDate(Instant.now());
				faceDao.save(face);
			}
		}
		logger.info("IndexingJob completed in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

}
