package com.example.mail.controller;

import java.util.List;
import com.example.mail.model.Account;
import com.example.mail.repository.AccountRepository;
import com.example.mail.security.UserPrincipal;
import com.example.mail.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

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
}
