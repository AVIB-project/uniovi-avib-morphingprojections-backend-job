package es.uniovi.avib.morphing.projections.backend.job.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.uniovi.avib.morphing.projections.backend.job.domain.Job;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
}