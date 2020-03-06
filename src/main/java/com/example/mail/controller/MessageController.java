package com.example.mail.controller;

import com.example.mail.security.CurrentUser;
import com.example.mail.security.UserPrincipal;
import com.example.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.mail.Message;
import javax.mail.MessagingException;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MailService mailService;

    @GetMapping("/messages/{folder}")
    @PreAuthorize("hasRole('USER')")
    public Message[] getFolderMessages(@CurrentUser UserPrincipal currentUser, String folder) throws MessagingException {
        return mailService.test();
    }
}
