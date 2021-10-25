package com.su.daar.repository;

import com.su.daar.document.Candidate;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface CandidateRepository extends ElasticsearchRepository<Candidate, String>{
    
}
