package com.example.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Arrays;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;
import javax.transaction.Transactional;

import com.example.mail.exception.AppException;
import com.example.mail.model.Account;
import com.example.mail.model.User;
import com.example.mail.payload.FolderConnection;
import com.example.mail.payload.FolderMessages;
import com.example.mail.repository.FolderRepository;
import com.example.mail.repository.MessageRepository;

@Service
public class MailService {

    private final Integer MAX_MESSAGES = 50;

    public final String FOLDER_INBOX = "INBOX";
    public final String FOLDER_SENT = "SENT";
    public final String[] SUPPORTED_FOLDERS = { FOLDER_INBOX, FOLDER_SENT };

    private final String DEFAULT_SESSION_PROTOCOL = "pop3s";

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageIndexService messageIndexService;

    public Session initSession(String host, String port, Boolean enableTtls) {
        Properties properties = new Properties();

        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);
        properties.put("mail.pop3.starttls.enable", enableTtls);

        return Session.getDefaultInstance(properties);
    }

    public FolderConnection connectToFolder(String folder, String host, String port, String user, String password)
            throws MessagingException {

        Session emailSession = this.initSession(host, port, true);

        Store store = emailSession.getStore(DEFAULT_SESSION_PROTOCOL);
        store.connect(host, user, password);

        Folder emailFolder = store.getFolder(folder);
        emailFolder.open(Folder.READ_ONLY);

        return new FolderConnection(emailFolder, store);
    }

    public FolderMessages getFolderMessages(com.example.mail.model.Folder folder, String host, String port, String user, String password) {
        ArrayList<com.example.mail.model.Message> parsedMessages = new ArrayList<>();
        Integer totalMessages = 0;

        try {
            FolderConnection folderConnection = this.connectToFolder(folder.getName(), host, port, user, password);

            Folder emailFolder = folderConnection.getFolder();
            Store store = folderConnection.getStore();

            totalMessages = emailFolder.getMessageCount();

            Message[] messages = emailFolder.getMessages(totalMessages - MAX_MESSAGES, totalMessages);
           
            for(Message message : messages) {
                parsedMessages.add(parseMessage(message, folder));
            }

            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new FolderMessages(totalMessages, parsedMessages);
    }

    public String getFirstAdress(Address[] adresses) {
        if (adresses != null && adresses.length > 0) {
            return adresses[0].toString();
        }

        return null;
    }
        
    public String getMessageContent(Message message) throws MessagingException {
        try {
            Object content = message.getContent();

            if (content instanceof Multipart) {
                StringBuffer messageContent = new StringBuffer();
                Multipart multipart = (Multipart) content;

                for (int i = 0; i < multipart.getCount(); i++) {
                    Part part = multipart.getBodyPart(i);

                    if (part.isMimeType("text/plain")) {
                        messageContent.append(part.getContent().toString());
                    }
                }
                return messageContent.toString();
            }

            return content.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public com.example.mail.model.Message parseMessage(Message message, com.example.mail.model.Folder folder) throws MessagingException, IOException {
        com.example.mail.model.Message modelMessage = new com.example.mail.model.Message();

        Address[] fromAdresses = message.getFrom();
        Address[] toAdresses = message.getRecipients(Message.RecipientType.TO);
        Address[] ccAdresses = message.getRecipients(Message.RecipientType.CC);
        Address[] bccAdresses = message.getRecipients(Message.RecipientType.BCC);
        Date date = message.getReceivedDate();
        String subject = message.getSubject();
        Boolean isRead = message.isSet(Flag.SEEN);
        String content = getMessageContent(message);
        modelMessage.setFrom(getFirstAdress(fromAdresses));
        modelMessage.setTo(getFirstAdress(toAdresses));
        modelMessage.setCc(getFirstAdress(ccAdresses));
        modelMessage.setBcc(getFirstAdress(bccAdresses));
        modelMessage.setDateTime(date);
        modelMessage.setSubject(subject);
        modelMessage.setContent(content);
        modelMessage.setRead(isRead);

        modelMessage.setAccount(folder.getAccount());
        modelMessage.setFolder(folder);

        return modelMessage;
    }

    public Boolean isFolderSupported(String folder) {
        List<String> supportedFolders = Arrays.asList(SUPPORTED_FOLDERS);

        if(!supportedFolders.contains(folder.toUpperCase())) {
            return false;
        }

        return true;
    }

    public FolderMessages getMessages(com.example.mail.model.Folder folder){
        Account account = folder.getAccount();
        
        if(account == null || !account.isValid()){
            return null;
        }

        return getFolderMessages(folder, account.getSmtpAdress(), Integer.toString(account.getSmtpPort()), account.getUsername(), account.getPassword());
    }

    public Integer getFolderMessageCount(Account account, String folder) {
        if(!account.isValid() || !this.isFolderSupported(folder)){
            return null;
        }

        Integer messageCount = 0;

        try {
            FolderConnection folderConnection = this.connectToFolder(
                folder, 
                account.getSmtpAdress(), 
                Integer.toString(account.getSmtpPort()), 
                account.getUsername(), 
                account.getPassword()
            );

            Folder emailFolder = folderConnection.getFolder();
            Store store = folderConnection.getStore();

            messageCount = emailFolder.getMessageCount();

            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return messageCount;
    }

    public FolderMessages mockSyncFolderMessages(com.example.mail.model.Folder folder) {
        com.example.mail.model.Message modelMessage1 = new com.example.mail.model.Message();
        modelMessage1.setSubject("subject 1");
        modelMessage1.setFolder(folder);
        modelMessage1.setAccount(folder.getAccount());

        com.example.mail.model.Message modelMessage2 = new com.example.mail.model.Message();
        modelMessage2.setSubject("subject 1");
        modelMessage2.setFolder(folder);
        modelMessage2.setAccount(folder.getAccount());

        ArrayList<com.example.mail.model.Message> messages = new ArrayList<>();
        messages.add(modelMessage1);
        messages.add(modelMessage2);

        FolderMessages folderMessages = new FolderMessages(2, messages);

        folder.setMessageCount(folderMessages.getMessageCount());
        folderRepository.save(folder);

        messageRepository.saveAll(folderMessages.getMessages());


        List<com.example.mail.model.Message> messagesToIndex = folderMessages.getMessages()
            .stream()
            .map(message -> {
                message.getFolder().setMessages(null);
                message.getFolder().getAccount().setFolders(null);
                message.getFolder().getAccount().setUser(null);

                return message;
            }).collect(Collectors.toList());

        messageIndexService.bulkIndex(messagesToIndex);

        return folderMessages;
    }

    @Transactional
    public FolderMessages syncFolderMessages(com.example.mail.model.Folder folder){
        if(!isFolderSupported(folder.getName())){
            throw new AppException("Looking for an unsupported folder, supported folders: " + String.join(", ", folder.SUPPORTED_FOLDERS));
        }

        if(folder.getMessageCount() >= getFolderMessageCount(folder.getAccount(), folder.getName())){
            throw new AppException("Folder already in sync!");
        }

        FolderMessages folderMessages = getMessages(folder);

        folder.setMessageCount(folderMessages.getMessageCount());
        folderRepository.save(folder);

        //messageRepository.saveAll(folderMessages.getMessages());

        /**
         *  Jackson nor spring-data-elasticsearch annotation cannot ommit serialization of fields at model level - but should according to the docs
         * 
         *  This could be an issue with the current version - this is a filthy hack to solve infinite recursion
         * 
         *  */ 
        List<com.example.mail.model.Message> messagesToIndex = folderMessages.getMessages()
            .stream()
            .map(message -> {
                message.getFolder().setMessages(null);  
                message.getFolder().getAccount().setFolders(null);
                message.getFolder().getAccount().getUser().setAccounts(null);
            
                return message;
            }).collect(Collectors.toList());

        messageIndexService.bulkIndex(messagesToIndex);

        return folderMessages;
    }
}
