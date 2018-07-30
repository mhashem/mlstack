package co.rxstack.ml.faces.util;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

public class BiMapCollector<T, K, V> implements Collector<T, BiMap<K, V>, BiMap<K, V>> {

	private final Function<T, K> keyGetter;
	private final Function<T, V> valueGetter;

	public BiMapCollector(Function<T, K> keyGetter, Function<T, V> valueGetter) {
		this.keyGetter = keyGetter;
		this.valueGetter = valueGetter;
	}

	public static <T, K, V> BiMapCollector<T, K, V> create(Function<T, K> keyGetter, Function<T, V> valueGetter) {
		return new BiMapCollector<>(keyGetter, valueGetter);
	}

	@Override
	public Supplier<BiMap<K, V>> supplier() {
		return HashBiMap::create;
	}

	@Override
	public BiConsumer<BiMap<K, V>, T> accumulator() {
		return (map, element) -> map.put(keyGetter.apply(element), valueGetter.apply(element));
	}

	@Override
	public BinaryOperator<BiMap<K, V>> combiner() {
		return ((kvBiMap, kvBiMap2) -> {
			kvBiMap.putAll(kvBiMap2);
			return kvBiMap;
		});
	}

	@Override
	public Function<BiMap<K, V>, BiMap<K, V>> finisher() {
		return kvBiMap -> kvBiMap;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
	}
}
