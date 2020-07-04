package com.example.mail.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.mail.model.Contact;
import com.example.mail.model.Photo;
import com.example.mail.payload.ContactRequest;
import com.example.mail.payload.index.SearchResult;
import com.example.mail.repository.ContactRepository;
import com.example.mail.repository.PhotoRepository;
import com.example.mail.repository.UserRepository;
import com.example.mail.security.UserPrincipal;
import com.example.mail.security.CurrentUser;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.mail.service.ContactIndexService;
import com.example.mail.service.FileUploadService;

@RestController
@RequestMapping("/api")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactIndexService contactIndexService;

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
    public Page<SearchResult<Contact>> getMyContacts(@RequestParam(required = false) String query, @CurrentUser UserPrincipal currentUser, @PageableDefault(size = Contact.DEFAULT_PER_PAGE) Pageable pageable) {
        if(query == null) {
            List<Contact> contacts = contactRepository.findByUserId(currentUser.getId(), pageable).getContent();

            List<SearchResult<Contact>> results =  contacts.stream()
                .map(contact -> new SearchResult<Contact>(contact, Collections.emptyMap()))
                .collect(Collectors.toList());

                return new PageImpl<SearchResult<Contact>>(results, pageable, contactRepository.count());
        } 

        return contactIndexService.search(query, currentUser.getId(), pageable);
    }

    @GetMapping("/users/{userId}/contacts")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<SearchResult<Contact>> getUserContacts(Long userId, @RequestParam(required = false) String query, @PageableDefault(size = Contact.DEFAULT_PER_PAGE) Pageable pageable) {
        if(query == null) {
            List<Contact> contacts = contactRepository.findByUserId(userId, pageable).getContent();

            List<SearchResult<Contact>> results = contacts.stream()
                .map(contact -> new SearchResult<Contact>(contact, Collections.emptyMap()))
                .collect(Collectors.toList());
            
                return new PageImpl<SearchResult<Contact>>(results, pageable, contactRepository.count());
        } 

        return contactIndexService.search(query, userId, pageable);
    }

    @PostMapping("/contacts")
    @PreAuthorize("hasRole('USER')")
    public Contact save(@ModelAttribute ContactRequest contactRequest, @CurrentUser UserPrincipal currentUser)
            throws IOException {
        Contact contact = modelMapper.map(contactRequest, Contact.class);
        contact.setUser(userRepository.findById(currentUser.getId()).get());
        Contact savedContact = contactRepository.save(contact);          
        contactIndexService.upsert(savedContact);

        if(contactRequest.getPhoto() != null) {
            String savedImagePath = fileUploadService.uploadImage(contactRequest.getPhoto().getBytes());

            Photo photo = new Photo();
            photo.setContact(contact);
            photo.setPath(savedImagePath);
            photoRepository.save(photo);

            contact.getPhotos().add(photo);
        }

        return savedContact;
    }

    @DeleteMapping("/contacts/{contactId}")
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("contactId") Long contactId) {    
        for (Photo photo : contactRepository.findById(contactId).get().getPhotos()) {
            fileUploadService.removeImage(photo.getPath());
        }

        contactRepository.deleteById(contactId);
        contactIndexService.delete(contactId);
    }
}
