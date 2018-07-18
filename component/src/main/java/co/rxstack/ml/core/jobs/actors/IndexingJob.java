package co.rxstack.ml.core.jobs.actors;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.faces.dao.FaceDao;
import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.model.Identity;
import co.rxstack.ml.faces.service.IIdentityService;
import co.rxstack.ml.aggregator.service.IStorageService;
import co.rxstack.ml.aggregator.service.StorageStrategy;
import co.rxstack.ml.aggregator.service.impl.AggregatorService;
import co.rxstack.ml.aggregator.service.impl.StorageService;
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
	private IStorageService storageService;

	@Autowired
	public IndexingJob(FaceDao faceDao, IIdentityService identityService, AggregatorService aggregatorService,
		IndexingQueue indexingQueue, IStorageService storageService) {
		this.aggregatorService = aggregatorService;
		this.indexingQueue = indexingQueue;
		this.faceDao = faceDao;
		this.identityService = identityService;
		this.storageService = storageService;
	}

	@Override
	public void onReceive(Object message) {
		log.info("IndexingJob started with message {}", message);
		Stopwatch stopwatch = Stopwatch.createStarted();
		List<Ticket> tickets = indexingQueue.getTickets();
		indexingQueue.clear();
		if (tickets.isEmpty()) {
			log.info("found 0 indexing tickets in queue, no action needed");
			return;
		}
		log.info("found {} tickets in indexing queue", tickets.size());
		for (Ticket ticket : tickets) {

			try {
				byte[] imageBytes = storageService
					.readBytes(ticket.getImageName(), StorageService.TEMPORARY_FOLDER,
						StorageStrategy.Strategy.DISK);

				ImmutableMap<String, String> bundleMap = ImmutableMap.of(
					Constants.PERSON_ID, ticket.getPersonId(),
					Constants.PERSON_NAME, ticket.getPersonName(),
					Constants.IMAGE_NAME, ticket.getImageName());

				Optional<AggregateFaceIndexingResult> faceIndexingResultOptional =
					aggregatorService.indexFaces(imageBytes, bundleMap).stream().findAny();

				if (faceIndexingResultOptional.isPresent()) {
					log.info("Indexing result {}", faceIndexingResultOptional.get());
					AggregateFaceIndexingResult faceIndexingResult = faceIndexingResultOptional.get();

					Identity identity = null;
					Optional<Identity> identityOptional =
						identityService.findById(Integer.parseInt(ticket.getPersonId()));
					if (!identityOptional.isPresent()) {
						identity = new Identity();
						identity.setId(Integer.parseInt(ticket.getPersonId()));
						identity.setName(ticket.getPersonName());
						identity = identityService.save(identity);
						// fixme: Bug - save may fail if cache in identity service haven't completed
						// fixme: and thus duplicate key will fail the operation, however the DAO used
						// fixme: seems to be safe as it runs save or update -- should check only
					} else {
						identity= identityOptional.get();
					}

					Face face = new Face();
					face.setIdentity(identity);
					face.setImage(ticket.getImageName());
					face.setAwsFaceId(faceIndexingResult.awsFaceId);
					face.setCognitivePersonId(faceIndexingResult.cognitivePersonId);
					face.setEmbeddingsVector(faceIndexingResult.embeddingsVector);
					faceDao.save(face);
					log.info("face {} saved successfully", face);
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		log.info("IndexingJob completed in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

}
