package com.example.mail.model;

import javax.persistence.*;

@Entity
@Table(name = "attachments")
public class Attachment {   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String path;

    @Column(length = 255)
    private String mimeType;

    @Column(length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name="message_id", nullable=true)
    private Message message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
