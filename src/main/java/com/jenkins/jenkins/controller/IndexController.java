package com.jenkins.jenkins.controller;

import com.jenkins.jenkins.entity.User;
import com.jenkins.jenkins.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.javafaker.Faker;

import java.util.List;

@RestController
public class IndexController {

    @Autowired
    private UserRepository repository;
    @GetMapping
    public String index() {
        // return "Jenkins Ci/CD";
        return "Hello World!";
    }

    @PostMapping("/create")
    @Transactional
    public void create() {
        Faker faker = new Faker();
        for (int i = 0; i < 3; i++)
        {
            User user = new User();
            user.setName(faker.name().fullName());
            repository.save(user);
        }
    }

    @GetMapping("/users")
    public List<User> getUsers()
    {
        return repository.findAll();
    }

    @DeleteMapping("/delete")
    public void delete()
    {
        repository.deleteAll();
    }
}
