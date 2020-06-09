package com.example.mail.payload;

import javax.mail.Folder;
import javax.mail.Store;

public class FolderConnection {
    
    private Folder folder;
    private Store store;

    public FolderConnection(Folder folder, Store store) {
        this.folder = folder;
        this.store = store;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
