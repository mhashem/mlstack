package co.rxstack.ml.faces.service.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import co.rxstack.ml.faces.dao.FaceDao;
import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.service.IFaceService;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.SynchronizedMutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FaceService implements IFaceService {

	private static final Logger log = LoggerFactory.getLogger(FaceService.class);

	private static final int INITIAL_DELAY = 20_000;
	private static final int REFRESH_PERIOD = 15_0000;

	private FaceDao faceDao;

	private MutableList<Face> facesCache;

	private Disposable disposable;

	private Observable<Integer> refreshingFaces;

	@Autowired
	public FaceService(FaceDao faceDao) {
		this.faceDao = faceDao;

		facesCache = SynchronizedMutableList.of(new ArrayList<>());

		refreshingFaces = Observable.create(emitter -> {
			disposable = Schedulers.io().createWorker().schedulePeriodically(() -> {
				log.info("Refreshing faces");
				facesCache.clear();
				List<Face> latestFacesList = faceDao.findAll();

				log.info("Found {} faces in database", latestFacesList.size());
				facesCache.addAll(latestFacesList);

				emitter.onNext(1);

			}, INITIAL_DELAY, REFRESH_PERIOD, MILLISECONDS);
		});

	}

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
	public Map<Integer, double[]> findAllEmbeddings() {
		return this.facesCache.stream()
			.collect(Collectors.toMap(Face::getId, Face::getEmbeddingsVector));
	}

	@Override
	public Multimap<Integer, double[]> findAllEmbeddingsForIdentity() {
		return this.facesCache.stream()
			.collect(MultimapCollector.toMultimap(f -> f.getIdentity().getId(), Face::getEmbeddingsVector));
	}

	@PreDestroy
	public void destroy() {
		disposable.dispose();
	}

	static class MultimapCollector<T, K, V> implements Collector<T, Multimap<K, V>, Multimap<K, V>> {

		private final Function<T, K> keyGetter;
		private final Function<T, V> valueGetter;

		public MultimapCollector(Function<T, K> keyGetter, Function<T, V> valueGetter) {
			this.keyGetter = keyGetter;
			this.valueGetter = valueGetter;
		}

		public static <T, K, V> MultimapCollector<T, K, V> toMultimap(Function<T, K> keyGetter, Function<T, V> valueGetter) {
			return new MultimapCollector<>(keyGetter, valueGetter);
		}

		public static <T, K, V> MultimapCollector<T, K, T> toMultimap(Function<T, K> keyGetter) {
			return new MultimapCollector<>(keyGetter, v -> v);
		}

		@Override
		public Supplier<Multimap<K, V>> supplier() {
			return ArrayListMultimap::create;
		}

		@Override
		public BiConsumer<Multimap<K, V>, T> accumulator() {
			return (map, element) -> map.put(keyGetter.apply(element), valueGetter.apply(element));
		}

		@Override
		public BinaryOperator<Multimap<K, V>> combiner() {
			return (map1, map2) -> {
				map1.putAll(map2);
				return map1;
			};
		}

		@Override
		public Function<Multimap<K, V>, Multimap<K, V>> finisher() {
			return map -> map;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
		}
	}

}
