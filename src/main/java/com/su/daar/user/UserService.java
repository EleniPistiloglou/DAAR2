package com.su.daar.user;

import com.su.daar.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // for adding Autowired into Controller class. Now the service is a bean
public class UserService {

	private final UserRepository repository;

	@Autowired
	public UserService( UserRepository repository){
		this.repository = repository;
	}

	public void save(final User user){
		repository.save(user);
	}

	public User findById(final String id){
		return repository.findById(id).orElse(null);
	}

    // @GetMapping   // it's a rest endpoint
	// public List<User> getUsers(){
	// 	return List.of(new User());
	// }
    
}
