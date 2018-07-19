package co.rxstack.ml.tensorflow.service.impl;

import co.rxstack.ml.faces.service.IFaceService;
import co.rxstack.ml.tensorflow.config.FaceNetConfig;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FaceNetServiceTest {

	@Mock
	private IFaceService faceService;
	@Mock
	private FaceNetConfig faceNetConfig;
	@Mock
	private FaceNetService faceNetService;

	@Test
	public void doTest() {
		Assertions.assertThat(1 == 1).isTrue();
	}

}
