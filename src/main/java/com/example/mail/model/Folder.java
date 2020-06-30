package com.example.mail.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "folders")
public class Folder {

    @Transient
    @JsonIgnore
    public final String FOLDER_INBOX = "INBOX";

    @Transient
    @JsonIgnore
    public final String FOLDER_SENT = "SENT";

    @Transient
    @JsonIgnore
    public final String[] SUPPORTED_FOLDERS = { FOLDER_INBOX, FOLDER_SENT };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @JsonIgnoreProperties("parentFolder")
    @OneToMany(mappedBy="parentFolder")
    private Set<Folder> subFolders;

    @ManyToOne
    private Folder parentFolder;

    @JsonIgnoreProperties("folder")
    @OneToMany(mappedBy="folder", cascade = CascadeType.PERSIST)
    @JsonIgnore
    private List<Message> messages;

    @JsonIgnoreProperties("folders")
    @ManyToOne
    @JoinColumn(name="account_id", nullable=false)
    private Account account;

    private Integer messageCount = 0;

    @Transient
    private Boolean isSupported;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Folder> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(Set<Folder> subFolders) {
        this.subFolders = subFolders;
    }

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public Boolean getIsSupported() {
        List<String> supportedFolders = Arrays.asList(SUPPORTED_FOLDERS);

        if(!supportedFolders.contains(getName().toUpperCase())) {
            return false;
        }

       return true;
    }

    public void setIsSupported(Boolean isSupported) {
        this.isSupported = isSupported;
    }
}
