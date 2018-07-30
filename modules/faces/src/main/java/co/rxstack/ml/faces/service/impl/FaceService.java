package co.rxstack.ml.faces.service.impl;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import co.rxstack.ml.faces.dao.FaceDao;
import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.service.IFaceService;
import co.rxstack.ml.faces.util.MultimapCollector;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.BiMaps;
import org.eclipse.collections.impl.list.mutable.SynchronizedMutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FaceService implements IFaceService {

	private static final Logger log = LoggerFactory.getLogger(FaceService.class);

	private static final int INITIAL_DELAY = 10;
	private static final int REFRESH_PERIOD = 15;

	private FaceDao faceDao;

	private MutableList<Face> facesCache;

	private Disposable disposable;

	private Observable<Integer> refreshingFaces;

	@Autowired
	public FaceService(FaceDao faceDao) {
		this.faceDao = faceDao;

		facesCache = SynchronizedMutableList.of(new ArrayList<>());

		scheduleFaceCache();

		/*refreshingFaces = Observable.create(emitter -> {
			disposable = Schedulers.io().createWorker().schedulePeriodically(() -> {
				log.info("Refreshing faces");
				facesCache.clear();
				List<Face> latestFacesList = faceDao.findAll();

				log.info("Found {} faces in database", latestFacesList.size());
				facesCache.addAll(latestFacesList);

				emitter.onNext(1);

			}, INITIAL_DELAY, REFRESH_PERIOD, SECONDS);
		});*/
	}

	private void scheduleFaceCache() {
		Schedulers.io().createWorker().schedulePeriodically(() -> {
			log.info("Refreshing faces");
			facesCache.clear();
			List<Face> latestFacesList = faceDao.findAll();
			log.info("Found {} faces in database", latestFacesList.size());
			facesCache.addAll(latestFacesList);
		}, INITIAL_DELAY, REFRESH_PERIOD, SECONDS);
	}

	@Deprecated
	@Override
	public Observable<Integer> getRefreshingFacesObservable() {
		return refreshingFaces;
	}

	@Override
	public List<Face> findAll() {
		return facesCache.clone();
	}

	@Override
	public List<Face> findByIdentityId(int identityId) {
		return faceDao.findAllByIdentityId(identityId);
	}

	@Override
	public Map<Integer, double[]> findAllFaceIdEmbeddingsMap() {
		return this.findAll().stream()
			.collect(Collectors.toMap(Face::getId, Face::getEmbeddingsVector));
	}

	@Override
	public BiMap<Integer, Integer> findFaceIdentityBiMap() {
		return this.findAll().stream().collect(HashBiMap::create, // Supplier
			(map, face) -> map.putIfAbsent(face.getId(), face.getIdentity().getId()), // BiConsumer
			AbstractMap::putAll); // combiner
	}

	@Override
	public Multimap<Integer, double[]> findAllEmbeddingsForIdentity() {
		return this.findAll().stream()
			.collect(MultimapCollector.toMultimap(
					f -> f.getIdentity().getId(), // key
					Face::getEmbeddingsVector)); // value
	}

	@PreDestroy
	public void destroy() {
		disposable.dispose();
	}

}
