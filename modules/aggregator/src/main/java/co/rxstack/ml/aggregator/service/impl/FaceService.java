package co.rxstack.ml.aggregator.service.impl;

import java.util.List;
import java.util.Map;

import co.rxstack.ml.aggregator.dao.FaceDao;
import co.rxstack.ml.aggregator.model.db.Face;
import co.rxstack.ml.aggregator.service.IFaceService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FaceService {

	private static final Logger log = LoggerFactory.getLogger(FaceService.class);

	private FaceDao faceDao;
	private Map<Integer, Face> facesMap = Maps.newConcurrentMap();

	@Autowired
	public FaceService(FaceDao faceDao) {
		this.faceDao = faceDao;
	}

}
