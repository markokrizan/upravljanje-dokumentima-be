package com.example.mail.controller;

import java.util.List;
import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.mail.model.Account;
import com.example.mail.payload.AccountRequest;
import com.example.mail.repository.AccountRepository;
import com.example.mail.repository.UserRepository;
import com.example.mail.security.UserPrincipal;
import com.example.mail.service.AccountService;
import com.example.mail.security.CurrentUser;

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
     
        Account savedAccount = accountRepository.save(account);

        if(accountRequest.getIsActive()) {
            accountService.toggleActiveAccount(savedAccount.getId(), currentUser.getId());
        }

        return accountRepository.findByUserId(currentUser.getId());
    }

    @DeleteMapping("/accounts/{accountId}")
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("accountId") Long accountId) {    
        accountRepository.deleteById(accountId);
    }
}
