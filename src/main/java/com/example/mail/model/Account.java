package com.example.mail.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "accounts")
public class Account {   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String smtpAdress;

    private Integer smtpPort;
    private Integer inServerType;

    @Column(length = 255)
    private String inServerAdress;

    private Integer inServerPort;

    @Column(length = 255)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(length = 255)
    private String displayName;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    @JsonIgnore
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSmtpAdress() {
        return smtpAdress;
    }

    public void setSmtpAdress(String smtpAdress) {
        this.smtpAdress = smtpAdress;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    public Integer getInServerType() {
        return inServerType;
    }

    public void setInServerType(Integer inServerType) {
        this.inServerType = inServerType;
    }

    public String getInServerAdress() {
        return inServerAdress;
    }

    public void setInServerAdress(String inServerAdress) {
        this.inServerAdress = inServerAdress;
    }

    public Integer getInServerPort() {
        return inServerPort;
    }

    public void setInServerPort(Integer inServerPort) {
        this.inServerPort = inServerPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
