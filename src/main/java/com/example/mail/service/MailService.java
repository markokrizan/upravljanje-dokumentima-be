package com.example.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

@Service
public class MailService {

    private final String FOLDER_INBOX = "INBOX";
    private final String LOWEST_DATE_RECEIVED = "January 1, 2020";
    private final String DEFAULT_SESSION_PROTOCOL = "pop3s";

    @Autowired
    private UtilService utilService;

    public Session initSession(String host, String port, Boolean enableTtls) {
        Properties properties = new Properties();

        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);
        properties.put("mail.pop3.starttls.enable", enableTtls);

        return Session.getDefaultInstance(properties);
    }

    public Message[] getFolderMessages(
            String folder, 
            String host, 
            String port, 
            String username, 
            String password,
            Date olderThanDate
    ) {
        Session emailSession = null;
        Store store = null;
        Folder emailFolder = null;
        Message[] messages = null;

        try {
            emailSession = this.initSession(host, port, true);
            store = emailSession.getStore(DEFAULT_SESSION_PROTOCOL);
            store.connect(host, username, password);

            emailFolder = store.getFolder(folder);
            emailFolder.open(Folder.READ_ONLY);

            SearchTerm olderThan = new ReceivedDateTerm(ComparisonTerm.LT, olderThanDate);
            SearchTerm newerThan = new ReceivedDateTerm(
                ComparisonTerm.GT, 
                utilService.generateDateFromString(LOWEST_DATE_RECEIVED)
            );

            SearchTerm andTerm = new AndTerm(olderThan, newerThan);

            messages = emailFolder.search(andTerm);
        } catch (Exception e) {
            try {
                emailFolder.close(false);
                store.close();
            } catch (MessagingException ee) {
                ee.printStackTrace();
            }
        }

        return messages;
    }

    public Message[] test(){
        String host = "pop.gmail.com";
        String port = "995";
        String username = "markokrizan64@gmail.com";
        String password = "lgoptimusl5poslenapada123";
        Date olderThan = new Date();

        return this.getFolderMessages(FOLDER_INBOX, host, port, username, password, olderThan);
    }
}