package co.rxstack.ml.aggregator.model.db;

import java.time.Instant;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;

@Entity(name = "Identity")
@Table(name = "identity")
public class Identity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;

	@JsonIgnore
	@OneToMany(mappedBy = "identity", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Face> faces = Sets.newHashSet();

	@Column(name = "create_date", nullable = false, updatable = false)
	private Instant createDate;

	@Column(name = "modify_date")
	private Instant modifyDate;

	public Identity() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Face> getFaces() {
		return faces;
	}

	public void setFaces(Set<Face> faces) {
		this.faces = faces;
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

}
