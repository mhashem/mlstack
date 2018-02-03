package co.rxstack.ml.core.jobs.actors;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.aggregator.dao.FaceDao;
import co.rxstack.ml.aggregator.model.db.Face;
import co.rxstack.ml.aggregator.model.db.Identity;
import co.rxstack.ml.aggregator.service.IIdentityService;
import co.rxstack.ml.aggregator.service.impl.AggregatorService;
import co.rxstack.ml.common.model.AggregateFaceIndexingResult;
import co.rxstack.ml.common.model.Constants;
import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.IndexingQueue;

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

	private static final Logger log = getLogger(IndexingJob.class);

	private FaceDao faceDao;
	private IndexingQueue indexingQueue;
	private AggregatorService aggregatorService;
	private IIdentityService identityService;

	@Autowired
	public IndexingJob(FaceDao faceDao, IIdentityService identityService, AggregatorService aggregatorService,
		IndexingQueue indexingQueue) {
		this.aggregatorService = aggregatorService;
		this.indexingQueue = indexingQueue;
		this.faceDao = faceDao;
		this.identityService = identityService;
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		log.info("IndexingJob fired with message {}", message);
		Stopwatch stopwatch = Stopwatch.createStarted();
		List<Ticket> tickets = indexingQueue.getTickets();
		indexingQueue.clear();
		if (tickets.isEmpty()) {
			log.info("no tickets found in indexing queue");
			return;
		}
		log.info("found {} tickets in indexing queue", tickets.size());
		for (Ticket ticket : tickets) {
			Optional<AggregateFaceIndexingResult> faceIndexingResultOptional = aggregatorService
				.indexFaces(ticket.getImageBytes(), ImmutableMap
					.of(Constants.PERSON_ID, ticket.getPersonId(), Constants.PERSON_NAME, ticket.getPersonName()))
				.stream().findAny();
			if (faceIndexingResultOptional.isPresent()) {
				log.info("Indexing result {}", faceIndexingResultOptional.get());
				AggregateFaceIndexingResult faceIndexingResult = faceIndexingResultOptional.get();

				Identity identity = null;
				Optional<Identity> identityOptional =
					identityService.findIdentityById(Integer.parseInt(ticket.getPersonId()));
				if (!identityOptional.isPresent()) {
					identity = new Identity();
					identity.setName(ticket.getPersonName());
					identity = identityService.save(identity);
				} else {
					identity= identityOptional.get();
				}

				Face face = new Face();
				face.setIdentity(identity);
				face.setAwsFaceId(faceIndexingResult.awsFaceId);
				face.setCognitivePersonId(faceIndexingResult.cognitivePersonId);
				faceDao.save(face);
				log.info("face {} saved successfully", face);
			}
		}
		log.info("IndexingJob completed in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

}
