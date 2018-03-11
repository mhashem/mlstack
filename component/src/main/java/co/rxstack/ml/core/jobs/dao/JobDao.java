package co.rxstack.ml.core.jobs.dao;

import co.rxstack.ml.core.jobs.model.Job;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface JobDao extends CrudRepository<Job, Long> {
}
