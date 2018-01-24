package co.rxstack.ml.core.jobs.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "face")
public class Face {

	@Id
	@Column(name = "person_id")
	private String personId;

	@Column(name = "aws_face_id")
	private String awsFaceId;

	@Column(name = "cognitive_person_id")
	private String cognitivePersonId;

	@Column(name = "creation_date")
	private Instant creationDate;

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getAwsFaceId() {
		return awsFaceId;
	}

	public void setAwsFaceId(String awsFaceId) {
		this.awsFaceId = awsFaceId;
	}

	public String getCognitivePersonId() {
		return cognitivePersonId;
	}

	public void setCognitivePersonId(String cognitivePersonId) {
		this.cognitivePersonId = cognitivePersonId;
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}
}
