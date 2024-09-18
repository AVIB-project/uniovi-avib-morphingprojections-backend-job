package es.uniovi.avib.morphing.projections.backend.job.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import es.uniovi.avib.morphing.projections.backend.job.domain.Job;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
	List<Job> findByName(String name);
	
	@Query(sort="{'creation_date':-1}")
	List<Job> findByCaseId(ObjectId caseId);
}