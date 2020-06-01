package com.example.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.mail.repository.AccountRepository;
import com.example.mail.model.Account;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public void toggleActiveAccount(Long accountId, Long userId) {
        if(accountId == null || userId == null) {
            return;
        }

        for (Account account : accountRepository.findByUserId(userId))  
        { 
            if (account.getId() == accountId) 
            { 
                account.setIsActive(true);
            } else {
               account.setIsActive(false);
            }

            accountRepository.save(account);
        } 
    }
}
