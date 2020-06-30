package com.example.mail.model;

import java.util.Set;
import java.util.HashSet;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Entity
@Table(name = "contacts")
@Document(indexName="contacts")
public class Contact {   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(length = 255)
    private String firstName;

    @NotNull
    @Column(length = 255)
    private String lastName;

    @NotNull
    @Column(length = 255)
    private String displayName;

    @NotNull
    @Column(length = 255)
    private String email;

    @Lob
    private String note;

    @JsonIgnoreProperties("contact")
    @OneToMany(mappedBy="contact", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Field(type = FieldType.Nested, includeInParent=false, ignoreFields = {"contact"})
    private Set<Photo> photos =  new HashSet<>();

    @JsonIgnoreProperties("accounts")
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    @JsonIgnore
    @Field(type = FieldType.Nested, includeInParent=true, ignoreFields = {"accounts"})
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Set<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(Set<Photo> photos) {
        this.photos = photos;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }    
}
