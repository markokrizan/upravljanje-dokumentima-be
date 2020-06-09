package com.example.mail.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.List;
import java.util.Arrays;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;

import com.example.mail.model.Account;
import com.example.mail.payload.FolderConnection;
import com.example.mail.payload.FolderMessages;

@Service
public class MailService {

    private final Integer MAX_MESSAGES = 50;

    public final String FOLDER_INBOX = "INBOX";
    public final String FOLDER_SENT = "SENT";
    public final String[] SUPPORTED_FOLDERS = { FOLDER_INBOX, FOLDER_SENT };

    private final String DEFAULT_SESSION_PROTOCOL = "pop3s";

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

    public FolderMessages getFolderMessages(String folder, String host, String port, String user, String password) {
        ArrayList<com.example.mail.model.Message> parsedMessages = new ArrayList<>();
        Integer totalMessages = 0;

        try {
            FolderConnection folderConnection = this.connectToFolder(folder, host, port, user, password);

            Folder emailFolder = folderConnection.getFolder();
            Store store = folderConnection.getStore();

            totalMessages = emailFolder.getMessageCount();

            Message[] messages = emailFolder.getMessages(totalMessages - MAX_MESSAGES, totalMessages);
           
            for(Message message : messages) {
                parsedMessages.add(parseMessage(message));
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

    public com.example.mail.model.Message parseMessage(Message message) throws MessagingException, IOException {
        com.example.mail.model.Message modelMessage = new com.example.mail.model.Message();

        Address[] fromAdresses = message.getFrom();
        Address[] toAdresses = message.getRecipients(Message.RecipientType.TO);
        Address[] ccAdresses = message.getRecipients(Message.RecipientType.CC);
        Address[] bccAdresses = message.getRecipients(Message.RecipientType.BCC);
        Date date = message.getReceivedDate();
        String subject = message.getSubject();
        String content = message.getContent().toString();
        Boolean isRead = message.isSet(Flag.SEEN);

        modelMessage.setFrom(getFirstAdress(fromAdresses));
        modelMessage.setTo(getFirstAdress(toAdresses));
        modelMessage.setCc(getFirstAdress(ccAdresses));
        modelMessage.setBcc(getFirstAdress(bccAdresses));
        modelMessage.setDateTime(date);
        modelMessage.setSubject(subject);
        modelMessage.setContent(content);
        modelMessage.setRead(isRead);

        return modelMessage;
    }

    public Boolean isFolderSupported(String folder) {
        List<String> supportedFolders = Arrays.asList(SUPPORTED_FOLDERS);

        if(!supportedFolders.contains(folder.toUpperCase())) {
            return false;
        }

        return true;
    }

    public FolderMessages getMessages(Account account, String folder){
        if(!account.isValid()){
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

    public Boolean syncFolder(com.example.mail.model.Folder folder){
        //Sync folder logika:

        //u folderu belezis broj poruka

        //dohvatis i poruke i broj poruka - snimis u bazu sve poruke i zabelezis count na folder

        //snimi u bazu i indeksiraj kroz es

        return true;
    }
}
