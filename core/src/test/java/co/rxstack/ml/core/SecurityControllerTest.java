package co.rxstack.ml.core;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.core.controller.SecurityController;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@WebMvcTest(SecurityController.class)
public class SecurityControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IRekognitionService rekognitionService;

	@Test
	public void testIndexFace() throws Exception {
		when(rekognitionService.indexFaces(anyString(), any())).thenReturn(ImmutableList.of());
		MockMultipartFile mockMultipartFile = new MockMultipartFile("faceImage", new byte[] {});
		this.mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/security/index")
			.file(mockMultipartFile).param("collectionId", "test"))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

}
