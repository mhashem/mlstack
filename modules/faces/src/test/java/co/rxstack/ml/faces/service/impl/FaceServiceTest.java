package co.rxstack.ml.faces.service.impl;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;

import co.rxstack.ml.faces.dao.FaceDao;
import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.model.Identity;
import co.rxstack.ml.faces.service.IFaceService;
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
	
}
