package com.example.mail.payload;

import java.util.ArrayList;

import com.example.mail.model.Message;

public class FolderMessages {

    private Integer messageCount = 0;
    private ArrayList<Message> messages = new ArrayList<>();

    public FolderMessages(Integer messageCount, ArrayList<Message> messages) {
        this.messageCount = messageCount;
        this.messages = messages;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
