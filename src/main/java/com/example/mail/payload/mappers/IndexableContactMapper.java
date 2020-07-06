package com.example.mail.payload.mappers;

import java.util.List;
import java.util.stream.Collectors;

import com.example.mail.model.Contact;
import com.example.mail.payload.index.IndexableContact;
import com.example.mail.repository.PhotoRepository;
import com.example.mail.repository.UserRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexableContactMapper implements IndexableMapper<IndexableContact, Contact> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Override
    public IndexableContact convertToIndexable(Contact contact) {
        IndexableContact indexableContact = modelMapper.map(contact, IndexableContact.class);

        indexableContact.setUserId(contact.getUser().getId());

        return indexableContact;
    }

    @Override
    public Contact convertFromIndexable(IndexableContact indexableContact) {
        Contact contact = modelMapper.map(indexableContact, Contact.class);

        contact.setUser(userRepository.findById(indexableContact.getUserId()).orElse(null));
        contact.setPhotos(photoRepository.findByContactId(indexableContact.getId()));

        return contact;
    }

    @Override
    public List<IndexableContact> convertToIndexables(List<Contact> mainTypeList) {
        return mainTypeList
            .stream()
            .map(contact -> convertToIndexable(contact))
            .collect(Collectors.toList());
    }

    @Override
    public List<Contact> convertFromIndexables(List<IndexableContact> indexableTypeList) {
        return indexableTypeList
            .stream()
            .map(indexableContact -> convertFromIndexable(indexableContact))
            .collect(Collectors.toList());
    }
}
