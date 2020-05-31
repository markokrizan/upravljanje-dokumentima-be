package com.example.mail.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @OneToMany(mappedBy="parentFolder")
    private Set<Folder> subFolders;

    @ManyToOne
    private Folder parentFolder;

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
}