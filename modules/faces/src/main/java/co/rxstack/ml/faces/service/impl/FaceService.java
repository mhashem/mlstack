package co.rxstack.ml.faces.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.rxstack.ml.faces.dao.FaceDao;
import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.service.IFaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FaceService implements IFaceService {

	private FaceDao faceDao;

	@Autowired
	public FaceService(FaceDao faceDao) {
		this.faceDao = faceDao;
	}

	@Override
	public List<Face> findByIdentityId(int identityId) {
		return faceDao.findAllByIdentityId(identityId);
	}

	@Override
	public Map<Integer, float[]> findAllEmbeddings() {
		return faceDao.findAll().stream()
			.collect(Collectors.toMap(Face::getId, Face::getEmbeddingsVector));
	}

}
