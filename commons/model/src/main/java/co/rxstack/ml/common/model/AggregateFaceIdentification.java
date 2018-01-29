package co.rxstack.ml.common.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class AggregateFaceIdentification {

	@JsonProperty("candidates")
	private final CopyOnWriteArrayList<Candidate> candidates;

	public AggregateFaceIdentification() {
		candidates = Lists.newCopyOnWriteArrayList();
	}

	public List<Candidate> getCandidates() {
		return ImmutableList.copyOf(candidates);
	}

	public void add(Candidate candidate) {
		this.candidates.add(candidate);
	}

	public void addAll(List<Candidate> candidateList) {
		this.candidates.addAll(candidateList);
	}
	
}
