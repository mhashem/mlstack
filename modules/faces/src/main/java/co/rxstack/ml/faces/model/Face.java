package co.rxstack.ml.faces.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

@Entity(name = "Face")
@Table(name = "face", indexes = {
	@Index(name = "aws_face_ids_idx", columnList = "aws_face_id"),
	@Index(name = "cognitive_person_ids_idx", columnList = "cognitive_person_id")
})
public class Face {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "faceIdSequenceGenerator")
	@SequenceGenerator(name = "faceIdSequenceGenerator")
	private int id;

	@Column(name = "aws_face_id")
	private String awsFaceId;

	@Column(name = "cognitive_person_id")
	private String cognitivePersonId;

	@Column(name = "embeddings_vector")
	private float[] embeddingsVector;

	@Column(name = "image")
	private String image;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="identity_id", nullable=false)
	private Identity identity;

	@Column(name = "create_date", nullable = false, updatable = false)
	private Instant createDate;

	@Column(name = "modify_date")
	private Instant modifyDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public float[] getEmbeddingsVector() {
		return embeddingsVector;
	}

	public void setEmbeddingsVector(float[] embeddingsVector) {
		this.embeddingsVector = embeddingsVector;
	}

	public Instant getCreateDate() {
		return createDate;
	}

	@PrePersist
	public void setCreateDate() {
		this.createDate = Instant.now();
	}

	public Instant getModifyDate() {
		return modifyDate;
	}

	@PreUpdate
	public void setModifyDate() {
		this.modifyDate = Instant.now();
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Face.class)
			.add("id", id)
			.add("aws_face_id", awsFaceId)
			.add("cognitive_person_id", cognitivePersonId)
			.add("identity", identity)
			.add("image", image)
			.add("embeddings_vector (size)", embeddingsVector.length)
			.add("create_date", createDate)
			.add("modify_date", modifyDate).toString();
	}
}
