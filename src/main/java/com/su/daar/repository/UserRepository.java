package com.su.daar.repository;

import com.su.daar.user.User;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserRepository extends ElasticsearchRepository<User, String>{
    
}
