package com.example.mail.controller;

import java.util.List;

import javax.validation.Valid;

import com.example.mail.model.Folder;
import com.example.mail.repository.AccountRepository;
import com.example.mail.repository.FolderRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.mail.payload.FolderRequest;

@RestController
@RequestMapping("/api")
public class FolderController {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/accounts/{accountId}/folders")
    @PreAuthorize("hasRole('USER')")
    public List<Folder> getAccountFolders(@PathVariable("accountId") Long accountId) {    
        return folderRepository.findByAccountId(accountId);
    }

    @PostMapping("/accounts/{accountId}/folders")
    @PreAuthorize("hasRole('USER')")
    public Folder save(@Valid @RequestBody FolderRequest folderRequest, @PathVariable("accountId") Long accountId) {
        Folder folder = modelMapper.map(folderRequest, Folder.class);
        folder.setAccount(accountRepository.findById(accountId).get());

        if(folderRequest.getParentFolderId() != null && folderRepository.existsById(folderRequest.getParentFolderId())) {
            folder.setParentFolder(folderRepository.findById(folderRequest.getParentFolderId()).get());
        }

        return folderRepository.save(folder);
    }
}
