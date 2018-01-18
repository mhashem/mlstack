package co.rxstack.ml.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class FileUtilsTest {

	@Test
	public void testRegex() {
		Pattern pattern = Pattern.compile("^[A-Za-z_]+[A-Za-z]\\S*$");
		Matcher matcher = pattern.matcher("label_name.png");
		Assert.assertTrue(matcher.find());
	}

	@Test
	public void testReadImageStream() {

	}

}
