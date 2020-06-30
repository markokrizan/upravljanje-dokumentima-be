package com.example.mail.payload;

import java.util.ArrayList;
import java.util.List;

import com.example.mail.model.Message;

public class FolderMessages {

    private Integer messageCount = 0;
    private List<Message> messages = new ArrayList<>();

    public FolderMessages(Integer messageCount, List<Message> messages) {
        this.messageCount = messageCount;
        this.messages = messages;
    }

    public FolderMessages() {

    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
