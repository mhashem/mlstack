package co.rxstack.ml.core;

import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TableTest {

	private Table<String, String, String> indexedTable;

	@Before
	public void setup() {
		indexedTable = HashBasedTable.create();
		indexedTable.put("aws-1", "cognitive-1", "p1");
		indexedTable.put("aws-2", "cognitive-2", "p2");
		indexedTable.put("aws-3", "cognitive-3", "p3");
	}

	@Test
	public void testSearchRow() {
		Optional<String> keyOptional = indexedTable.row("aws-1").values().stream().findAny();
		Assert.assertTrue(keyOptional.isPresent());
		Assert.assertEquals("p1", keyOptional.get());
	}

	@Test
	public void testSearchColumn() {
		Optional<String> keyOptional = indexedTable.column("cognitive-3").values().stream().findAny();
		Assert.assertTrue(keyOptional.isPresent());
		Assert.assertEquals("p3", keyOptional.get());
	}

	/*@Test
	public void testSearchSpeed() {
		IntStream.range(1, 1_000_000).forEach(value -> {
			indexedTable.put(value + "", UUID.randomUUID().toString(), UUID.randomUUID().toString());
		});

		Stopwatch stopwatch = Stopwatch.createStarted();
		Optional<String> keyOptional = indexedTable.row("7000").values().stream().findAny();
		System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}*/


}
