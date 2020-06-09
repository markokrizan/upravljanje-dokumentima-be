package com.example.mail.controller;

import java.util.List;
import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.mail.model.Account;
import com.example.mail.model.Folder;
import com.example.mail.payload.AccountRequest;
import com.example.mail.repository.AccountRepository;
import com.example.mail.repository.FolderRepository;
import com.example.mail.repository.UserRepository;
import com.example.mail.security.UserPrincipal;
import com.example.mail.service.AccountService;
import com.example.mail.service.MailService;
import com.example.mail.security.CurrentUser;
import com.example.mail.exception.BadRequestException;

@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('USER')")
    public List<Account> getMyAccounts(@CurrentUser UserPrincipal currentUser) {
        return accountRepository.findByUserId(currentUser.getId());
    }

    @GetMapping("/users/{userId}/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @PostMapping("/accounts")
    @PreAuthorize("hasRole('USER')")
    public List<Account> save(@Valid @RequestBody AccountRequest accountRequest, @CurrentUser UserPrincipal currentUser) {
        Account account = modelMapper.map(accountRequest, Account.class);
        account.setUser(userRepository.findById(currentUser.getId()).get());

        if(accountRepository.countByUserId(currentUser.getId()) == 0) {
            account.setIsActive(true);
        }

        if(accountRequest.getIsActive()) {
            accountService.toggleActiveAccount(account.getId(), currentUser.getId());
        }
        
        accountRepository.save(account);

        return accountRepository.findByUserId(currentUser.getId());
    }

    @DeleteMapping("/accounts/{accountId}")
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("accountId") Long accountId) {    
        accountRepository.deleteById(accountId);
    }

    @GetMapping("/accounts/{accountId}/sync/{folder}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> syncFolder(@CurrentUser UserPrincipal currentUser, @PathVariable("accountId") Long accountId, @PathVariable("folder") String folder)
        throws BadRequestException 
    {
        Account account = accountRepository.findById(accountId).get();
        Folder accountFolder = folderRepository.findByAccountFolderName(account.getId(), folder);
       
        if(accountFolder == null){
            throw new BadRequestException("Account folder not found!");
        }

        if(accountFolder.getMessageCount() >= mailService.getFolderMessageCount(account, folder)){
            return new ResponseEntity<String>("Folder already in sync!", HttpStatus.OK);
        }

        Boolean syncSuccessfull = mailService.syncFolder(accountFolder);

        if(!syncSuccessfull){
            throw new BadRequestException("Something went wrong while syncing folder!");
        };

        return new ResponseEntity<String>("Folder synced!", HttpStatus.OK);
    }
}
