package co.rxstack.ml.faces.service.impl;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import co.rxstack.ml.faces.dao.FaceDao;
import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.model.Identity;
import co.rxstack.ml.faces.service.IFaceService;

import com.google.common.collect.Multimap;
import org.assertj.core.api.Assertions;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FaceServiceTest {
	
	@Mock
	private FaceDao faceDao;
	private IFaceService faceService;
	
	private ImmutableList<Face> faceList;
	
	@Before
	public void setup() {
		faceService = new FaceService(faceDao);

		Identity identity = new Identity();
		identity.setId(1);
		
		Face f1 = new Face();
		f1.setIdentity(identity);
		
		faceList = Lists.immutable.with(f1);
	}
	
	@Test
	public void testFindByIdentityId() {
		when(faceDao.findAllByIdentityId(anyInt())).thenReturn(faceList.castToList());
		List<Face> resultFaceList = faceService.findByIdentityId(1);
		Assertions.assertThat(resultFaceList).isNotEmpty().hasSize(1);
	}


	@Test
	public void testFindAllEmbeddingsForIdentity() {

		Identity identity1 = createIdentity(1);
		Identity identity2 = createIdentity(2);

		Face face1 = createFace(1, identity1);
		Face face2 = createFace(2, identity1);
		Face face3 = createFace(2, identity1);
		Face face4 = createFace(3, identity2);

		when(faceDao.findAll()).thenReturn(Lists.immutable.of(face1, face2, face3, face4).castToList());

		Multimap<Integer, double[]> embeddingsForIdentity = faceService.findAllEmbeddingsForIdentity();

		Collection<double[]> embeddingsForIdentity1 = embeddingsForIdentity.get(1);

		Assertions.assertThat(embeddingsForIdentity1)
			.hasSize(3)
			.contains(face1.getEmbeddingsVector(),
				face2.getEmbeddingsVector(),
				face3.getEmbeddingsVector());
	}

	private Identity createIdentity(int id) {
		Identity identity = new Identity();
		identity.setId(id);
		return identity;
	}

	private Face createFace(int id, Identity identity) {
		Face face = new Face();
		face.setId(id);
		face.setEmbeddingsVector(generateRandomDoubleArray());
		face.setIdentity(identity);
		return face;
	}

	private double[] generateRandomDoubleArray() {
		Random random = new Random();
		double[] doubles = new double[5];
		IntStream.range(0, 5).forEach(i -> doubles[i] = random.nextDouble());
		return doubles;
	}
	
}
