package com.example.mail.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class FileUploadService {

    private final String STORAGE_LOCATION = "images/";
    private final String FILE_TYPE_JPG = ".jpg";

    public String uploadImage(byte[] fileToUpload) throws IOException {
        String randomName = UUID.randomUUID().toString();
        String imagePath = STORAGE_LOCATION + randomName + FILE_TYPE_JPG;
    
        File image = new File(imagePath);
        image.createNewFile();
        
        FileOutputStream fout = new FileOutputStream(image);
        fout.write(fileToUpload);
        fout.close();

        return imagePath;
    }
}