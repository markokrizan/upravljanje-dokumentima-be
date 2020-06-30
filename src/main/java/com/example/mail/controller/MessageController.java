package com.example.mail.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.mail.security.CurrentUser;
import com.example.mail.security.UserPrincipal;
import com.example.mail.model.Message;
import com.example.mail.repository.MessageRepository;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;
    
    @PutMapping("/messages")
    @PreAuthorize("hasRole('USER')")
    public Message save(@CurrentUser UserPrincipal currentUser, @ModelAttribute Message message) 
    {
        return messageRepository.save(message);
    }
}
