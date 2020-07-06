package com.example.mail.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.mail.security.CurrentUser;
import com.example.mail.security.UserPrincipal;
import com.example.mail.service.MessageIndexService;
import com.example.mail.exception.BadRequestException;
// import com.example.mail.model.Folder;
import com.example.mail.model.Message;
import com.example.mail.payload.MessageRequest;
import com.example.mail.repository.FolderRepository;
import com.example.mail.repository.MessageRepository;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MessageIndexService messageIndexService;

    // @Autowired
    // private FolderRepository folderRepository;
    
    @PostMapping("/messages")
    @PreAuthorize("hasRole('USER')")
    public Message save(@RequestBody MessageRequest messageRequest, @CurrentUser UserPrincipal currentUser) 
    {
        Message message = messageRepository.findById(messageRequest.getId()).orElse(null);

        if(message == null) {
            throw new BadRequestException("Message not found!");
        }

        // Folder folder = folderRepository.findById(messageRequest.getFolderId()).orElse(null);

        // if(folder == null) {
        //     throw new BadRequestException("Folder not found!");
        // }

        modelMapper.map(messageRequest, message);

        Message savedMessage = messageRepository.save(message);             
        messageIndexService.upsert(savedMessage);

        return message;
    }
}
