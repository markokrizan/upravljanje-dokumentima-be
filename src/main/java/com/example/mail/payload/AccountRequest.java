package com.example.mail.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AccountRequest {

    @NotBlank
    private String smtpAdress;

    @NotNull
    private Integer smtpPort;

    @NotNull
    private Integer inServerType;

    @NotBlank
    private String inServerAdress;

    @NotNull
    private Integer inServerPort;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String displayName;

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
}
