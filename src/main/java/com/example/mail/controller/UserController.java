package com.example.mail.controller;

import java.util.Optional;

import com.example.mail.exception.ResourceNotFoundException;
import com.example.mail.model.User;
import com.example.mail.payload.*;
import com.example.mail.repository.UserRepository;
import com.example.mail.security.UserPrincipal;
import com.example.mail.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users/me")
    @PreAuthorize("hasRole('USER')")
    public Optional<User> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        return userRepository.findById(currentUser.getId());
    }

    @GetMapping("/users/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/users/{username}")
    public User getUserProfile(@PathVariable(value = "username") String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return user;
    }
}
