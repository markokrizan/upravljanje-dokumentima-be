package com.example.mail.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.example.mail.elastic.MessageElasticRepository;
import com.example.mail.exception.BadRequestException;
import com.example.mail.model.Folder;
import com.example.mail.model.Message;
import com.example.mail.repository.AccountRepository;
import com.example.mail.repository.FolderRepository;
import com.example.mail.repository.MessageRepository;
import com.example.mail.security.CurrentUser;
import com.example.mail.security.UserPrincipal;
import com.example.mail.service.MailService;
import com.example.mail.service.MessageIndexService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.mail.payload.FolderMessages;
import com.example.mail.payload.FolderRequest;
import com.example.mail.payload.index.SearchResult;

@RestController
@RequestMapping("/api")
public class FolderController {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageElasticRepository messageElasticRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MessageIndexService messageIndexService;

    @GetMapping("/accounts/{accountId}/folders")
    @PreAuthorize("hasRole('USER')")
    public List<Folder> getAccountFolders(@PathVariable("accountId") Long accountId) {    
        return folderRepository.findByAccountId(accountId);
    }

    @PostMapping("/accounts/{accountId}/folders")
    @PreAuthorize("hasRole('USER')")
    public List<Folder> save(@Valid @RequestBody FolderRequest folderRequest, @PathVariable("accountId") Long accountId) {
        Folder folder = modelMapper.map(folderRequest, Folder.class);
        folder.setAccount(accountRepository.findById(accountId).get());

        if(folderRequest.getParentFolderId() != null && folderRepository.existsById(folderRequest.getParentFolderId())) {
            folder.setParentFolder(folderRepository.findById(folderRequest.getParentFolderId()).get());
        }

        folderRepository.save(folder);

        return folderRepository.findByAccountId(accountId);
    }

    @DeleteMapping("/accounts/{accountId}/folders/{folderId}")
    @PreAuthorize("hasRole('USER')")
    public List<Folder> delete(@PathVariable("accountId") Long accountId, @PathVariable("folderId") Long folderId) {    
        folderRepository.deleteById(folderId);
        messageElasticRepository.deleteByFolderId(folderId);

        return folderRepository.findByAccountId(accountId); 
    }

    @PutMapping("/accounts/{accountId}/folders/{folderId}/sync")
    @PreAuthorize("hasRole('USER')")
    public FolderMessages syncFolder(@CurrentUser UserPrincipal currentUser, @PathVariable("accountId") Long accountId, @PathVariable("folderId") Long folderId)
        throws BadRequestException 
    {
        Folder folder = folderRepository.findById(folderId).get();
       
        return mailService.syncFolderMessages(folder);
    }

    @GetMapping("/accounts/{accountId}/folders/{folderId}/messages")
    @PreAuthorize("hasRole('USER')")
    public Page<SearchResult<Message>> getFolderMessages(
        @CurrentUser UserPrincipal currentUser, 
        @PathVariable("accountId") Long accountId, 
        @PathVariable("folderId") Long folderId, 
        @RequestParam(required = false) 
        String query,
        @PageableDefault(size = Message.DEFAULT_PER_PAGE) Pageable pageable
    ) {
        if(query == null) {
            List<Message> messages = messageRepository.findByFolderId(folderId, pageable).getContent();

            List<SearchResult<Message>> results =  messages.stream()
                .map(message -> new SearchResult<Message>(message, Collections.emptyMap()))
                .collect(Collectors.toList());

            return new PageImpl<SearchResult<Message>>(results, pageable, messageRepository.count());
        } 

        return messageIndexService.search(query, folderId, pageable);
    }
}
