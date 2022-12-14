package co.rxstack.ml.faces.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import co.rxstack.ml.faces.dao.FaceDao;
import co.rxstack.ml.faces.dao.IdentityDao;
import co.rxstack.ml.faces.model.Face;
import co.rxstack.ml.faces.model.Identity;
import co.rxstack.ml.faces.service.IIdentityService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IdentityService implements IIdentityService {

	private static final Logger log = LoggerFactory.getLogger(IdentityService.class);

	private AtomicBoolean isRunning = new AtomicBoolean(false);

	private FaceDao faceDao;
	private IdentityDao identityDao;

	private final Map<Integer, List<Face>> identityFaceListMap = Maps.newConcurrentMap();
	private final Map<Integer, Identity> identityMap = Maps.newConcurrentMap();
	private final Map<Integer, Identity> faceIdentityMap = Maps.newConcurrentMap();
	private final Map<String, Integer> awsFaceIdMap = Maps.newConcurrentMap();
	private final Map<String, Integer> cognitivePersonIdMap = Maps.newConcurrentMap();

	@Autowired
	public IdentityService(IdentityDao identityDao, FaceDao faceDao) {
		this.faceDao = faceDao;
		this.identityDao = identityDao;
		refreshCache();
	}

	@Override
	public List<Identity> findAll() {
		return Lists.newArrayList(identityDao.findAll());
	}

	@Override
	public Optional<Identity> findIdentityByAwsFaceId(String awsFaceId) {
		return Optional.ofNullable(awsFaceIdMap.get(awsFaceId)).map(faceIdentityMap::get);
	}

	@Override
	public Optional<Identity> findIdentityByCognitivePersonId(String cognitivePersonId) {
		return Optional.ofNullable(cognitivePersonIdMap.get(cognitivePersonId)).map(faceIdentityMap::get);
	}

	@Override
	public Optional<Identity> findIdentityByFaceId(int faceId) {
		return Optional.ofNullable(faceIdentityMap.get(faceId));
	}

	/**
	 * Returns an Optional of Identity searched by id, in case not found in internal cache
	 * the method fallback to dao.
	 *
	 * @param id int
	 * @return Optional<Identity>
	 */
	@Override
	public Optional<Identity> findById(int id) {
		Optional<Identity> identityOptional = Optional.ofNullable(identityMap.get(id));
		return identityOptional.isPresent() ? identityOptional : identityDao.findById(id);
	}

	@Override
	public List<Face> findFaceListByIdentityId(int identityId) {
		return Optional.ofNullable(identityFaceListMap.get(identityId)).orElse(ImmutableList.of());
	}

	@Override
	public Identity save(Identity identity) {
		return identityDao.save(identity);
	}

	@Scheduled(fixedRate = 60000, initialDelay = 10000)
	private void refreshCache() {
		log.info("Refreshing Identities cache");
		if (!isRunning.getAndSet(true)) {
			log.info("Found no running lock will proceed");
			List<Face> faceList = faceDao.findAll();
			faceList.forEach(face -> {
				int faceId = face.getId();
				Optional.ofNullable(face.getAwsFaceId()).ifPresent(awsFaceId -> awsFaceIdMap.put(awsFaceId, faceId));

				Optional.ofNullable(face.getCognitivePersonId())
					.ifPresent(cognitivePersonId -> cognitivePersonIdMap.put(cognitivePersonId, faceId));

				faceIdentityMap.put(faceId, face.getIdentity());
			});

			identityFaceListMap.putAll(
				faceList.stream().collect(Collectors.groupingBy(o -> o.getIdentity().getId(), Collectors.toList())));

			Set<Integer> idSet = Sets.newHashSet();
			identityDao.findAll().forEach(identity -> {
				idSet.add(identity.getId());
				identityMap.put(identity.getId(), identity);
			});

			identityMap.keySet().retainAll(idSet);

			log.info("Identities internal cache refresh completed, found {} faces in db", faceList.size());
			isRunning.compareAndSet(true, false);
			log.info("Identities refresh lock removed");
		}

	}

	@Override
	public int delete(int id) {
		log.info("Deleting Faces associated with Identity {}", id);
		faceDao.deleteFaceByIdentityId(id);
		log.info("Deleting Identity associated with id {}", id);
		return identityDao.deleteById(id);
	}

	/*private Map<String, Face> getFacesMap() {
		return indexedFaceCacheTable.values().stream()
			.collect(Collectors.toMap(Face::getPersonId, o -> o, 
				(oldValue, newValue) -> oldValue));
		// in case of duplicate prefer old value to prevent exception
	}*/

}
