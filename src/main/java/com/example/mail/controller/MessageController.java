package com.example.mail.controller;

import com.example.mail.security.CurrentUser;
import com.example.mail.security.UserPrincipal;
import com.example.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import javax.mail.MessagingException;
import com.example.mail.exception.BadRequestException;
import com.example.mail.model.User;
import com.example.mail.payload.FolderMessages;
import com.example.mail.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/messages/{folder}")
    @PreAuthorize("hasRole('USER')")
    public FolderMessages getFolderMessages(@CurrentUser UserPrincipal currentUser, @PathVariable("folder") String folder) throws MessagingException,
        BadRequestException 
    {
        User user = userRepository.findById(currentUser.getId()).get();
       
        if(!mailService.isFolderSupported(folder)){
            throw new BadRequestException("Looking for an unsupported folder, supported folders: " + String.join(", ", mailService.SUPPORTED_FOLDERS));
        }
    
        if (user.getActivatedAccount() == null) {
            throw new BadRequestException("Default account not set!");
        }

        return mailService.getMessages(user.getActivatedAccount(), folder.toUpperCase());
    }
}
