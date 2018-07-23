package co.rxstack.ml.faces.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import co.rxstack.ml.faces.dao.FaceDao;
import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.model.Identity;
import co.rxstack.ml.faces.service.IFaceService;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class FaceService implements IFaceService {

	private FaceDao faceDao;

	@Autowired
	public FaceService(FaceDao faceDao) {
		this.faceDao = faceDao;
	}

	@Override
	//@Cacheable()
	public List<Face> findAll() {
		// todo add cache
		return faceDao.findAll();
	}

	@Override
	public List<Face> findByIdentityId(int identityId) {
		return faceDao.findAllByIdentityId(identityId);
	}

	@Override
	public Map<Integer, double[]> findAllEmbeddings() {
		return faceDao.findAll().stream()
			.collect(Collectors.toMap(Face::getId, Face::getEmbeddingsVector));
	}

	@Override
	public Multimap<Integer, double[]> findAllEmbeddingsForIdentity() {
		return faceDao.findAll().stream()
			.collect(MultimapCollector.toMultimap(f -> f.getIdentity().getId(), Face::getEmbeddingsVector));
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
