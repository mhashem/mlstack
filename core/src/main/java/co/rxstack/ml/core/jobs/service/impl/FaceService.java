package co.rxstack.ml.core.jobs.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.core.jobs.dao.FaceDao;
import co.rxstack.ml.core.jobs.model.Face;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FaceService {

	private static final Logger log = LoggerFactory.getLogger(FaceService.class);

	private FaceDao faceDao;
	private Table<String, String, Face> indexedFaceCacheTable;

	@Autowired
	public FaceService(FaceDao faceDao, IFaceRecognitionService faceRecognitionService) {
		this.faceDao = faceDao;
		this.indexedFaceCacheTable = HashBasedTable.create();
	}

	@Scheduled(fixedRate = 60000, initialDelay = 10000)
	private void refreshCache() {
		log.info("Refreshing face(s) cache");
		List<Face> faceList = faceDao.findAll();
		faceList.forEach(face -> indexedFaceCacheTable.put(face.getAwsFaceId(), face.getCognitivePersonId(), face));
		log.info("Refresh complete faces count [{}]", indexedFaceCacheTable.size());
	}

	public Optional<Face> getFaceByAwsFaceId(String awsFaceId) {
		return indexedFaceCacheTable.row(awsFaceId).values().stream().findAny();
	}

	public Optional<Face> getFaceByCognitivePersonId(String cognitivePersonId) {
		return indexedFaceCacheTable.column(cognitivePersonId).values().stream().findAny();
	}

	public Optional<Face> getFaceByPersonId(String personId) {
		return Optional.ofNullable(getFacesMap().get(personId));
	}
	
	private Map<String, Face> getFacesMap() {
		return indexedFaceCacheTable.values().stream()
			.collect(Collectors.toMap(Face::getPersonId, o -> o, 
				(oldValue, newValue) -> oldValue)); // in case of duplicate prefer old value to prevent exception
	}

}
