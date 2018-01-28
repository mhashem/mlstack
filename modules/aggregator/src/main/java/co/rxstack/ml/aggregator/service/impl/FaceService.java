package co.rxstack.ml.aggregator.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.rxstack.ml.aggregator.dao.FaceDao;
import co.rxstack.ml.aggregator.model.db.Face;
import co.rxstack.ml.aggregator.service.IFaceService;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FaceService implements IFaceService {

	private static final Logger log = LoggerFactory.getLogger(FaceService.class);

	private FaceDao faceDao;

	private final Map<String, Face> faceMap = Maps.newConcurrentMap();
	private final Map<String, String> awsFaceIdMap = Maps.newConcurrentMap();
	private final Map<String, String> cognitivePersonIdMap = Maps.newConcurrentMap();

	@Autowired
	public FaceService(FaceDao faceDao) {
		this.faceDao = faceDao;
	}

	@Scheduled(fixedRate = 60000, initialDelay = 10000)
	private void refreshCache() {
		log.info("Refreshing face(s) cache");
		List<Face> faceList = faceDao.findAll();
		faceList.forEach(face -> {
			Optional.ofNullable(face.getAwsFaceId())
				.ifPresent(awsFaceId -> awsFaceIdMap.put(awsFaceId, face.getPersonId()));

			Optional.ofNullable(face.getCognitivePersonId())
				.ifPresent(cognitivePersonId -> cognitivePersonIdMap.put(cognitivePersonId, face.getPersonId()));
			faceMap.put(face.getPersonId(), face);
		});
		log.info("Refresh complete, found {} faces in db", faceMap.size());
	}

	@Override
	public Optional<Face> getFaceByAwsFaceId(String awsFaceId) {
		return Optional.ofNullable(awsFaceIdMap.get(awsFaceId)).map(faceMap::get);
	}

	@Override
	public Optional<Face> getFaceByCognitivePersonId(String cognitivePersonId) {
		return Optional.ofNullable(cognitivePersonIdMap.get(cognitivePersonId)).map(faceMap::get);
	}

	@Override
	public Optional<Face> getFaceByPersonId(String personId) {
		return Optional.ofNullable(faceMap.get(personId));
	}
	
	/*private Map<String, Face> getFacesMap() {
		return indexedFaceCacheTable.values().stream()
			.collect(Collectors.toMap(Face::getPersonId, o -> o, 
				(oldValue, newValue) -> oldValue));
		// in case of duplicate prefer old value to prevent exception
	}*/

}
