package com.su.daar.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path="api/users")
@RestController    // to create rest endpoint
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // public List<User> getUsers(){
    //     return userService.getUsers();
    // }

    //endpoint for creating user
    @GetMapping("/{id}")
    public User findById(@PathVariable final String id){
        return userService.findById(id);
    }

    //endpoint for saving user
    @PostMapping
    public void save(@RequestBody final User user){
        userService.save(user);
    }

}
