package com.example.mail.controller;

import java.io.IOException;
import java.util.List;
import com.example.mail.model.Contact;
import com.example.mail.model.Photo;
import com.example.mail.payload.ContactRequest;
import com.example.mail.repository.ContactRepository;
import com.example.mail.repository.PhotoRepository;
import com.example.mail.repository.UserRepository;
import com.example.mail.security.UserPrincipal;
import com.example.mail.security.CurrentUser;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.mail.service.FileUploadService;

@RestController
@RequestMapping("/api")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/contacts")
    @PreAuthorize("hasRole('USER')")
    public List<Contact> getMyContacts(@CurrentUser UserPrincipal currentUser) {
        return contactRepository.findByUserId(currentUser.getId());
    }

    @GetMapping("/users/{userId}/contacts")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Contact> getUserContacts(Long userId) {
        return contactRepository.findByUserId(userId);
    }

    @PostMapping("/contacts")
    @PreAuthorize("hasRole('USER')")
    public Contact save(@ModelAttribute ContactRequest contactRequest, @CurrentUser UserPrincipal currentUser)
            throws IOException {
        Contact contact = modelMapper.map(contactRequest, Contact.class);
        contact.setUser(userRepository.findById(currentUser.getId()).get());
        contactRepository.save(contact);

        if(contactRequest.getPhoto() != null) {
            String savedImagePath = fileUploadService.uploadImage(contactRequest.getPhoto().getBytes());

            Photo photo = new Photo();
            photo.setContact(contact);
            photo.setPath(savedImagePath);
            photoRepository.save(photo);
        }

        return contact;
    }

    @DeleteMapping("/contacts/{contactId}")
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("contactId") Long contactId) {    
        contactRepository.deleteById(contactId);
    }

}
