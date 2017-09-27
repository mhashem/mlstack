package co.rxstack.ml.core;

import static org.slf4j.LoggerFactory.getLogger;

import javax.ws.rs.PathParam;

import co.rxstack.ml.cognitiveservices.services.IPersonGroupService;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mhachem on 9/27/2017.
 */
@RestController("/api/v1/persongroup")
public class PersonGroupController {

	private static final Logger log = getLogger(PersonGroupController.class);
	
	private final IPersonGroupService personGroupService;

	@Autowired
	public PersonGroupController(IPersonGroupService personGroupService) {
		this.personGroupService = personGroupService;
	}

	@Timed
	public ResponseEntity createGroup(@PathParam("personGroupId") String personGroupId) {
		boolean isCreated = personGroupService.createPersonGroup(personGroupId, "");
		return isCreated ? ResponseEntity.ok("") : ResponseEntity.badRequest().build();
	}
	
}
