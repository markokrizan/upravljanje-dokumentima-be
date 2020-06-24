package com.example.mail.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import com.example.mail.model.Contact;
import com.example.mail.elastic.ContactElasticRepository;

@Service
public class ContactIndexService implements IndexService<Contact> {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private ContactElasticRepository contactElasticRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<Contact> search(String query, Long userId) {
        List<Contact> contacts = search(query);

        return contacts.stream().filter(contact -> contact.getUser().getId() == userId).collect(Collectors.toList());
    }

    @Override
    public List<Contact> search(String query) {
        SearchHits<Contact> contactSearchResults = elasticsearchTemplate.search(
            buildQuery(query), 
            Contact.class, 
            IndexCoordinates.of("mail")
        );

        return contactSearchResults
            .stream()
            .map(contact -> modelMapper.map(contact, Contact.class))
            .collect(Collectors.toList());
    }

    @Override
    public Contact upsert(Contact contact) {
        if (contact == null) {
            return null;
        }

        Contact indexedContact = contactElasticRepository.findById(contact.getId().toString()).orElse(null);

        if (contact.getId() != null && indexedContact != null) {
            modelMapper.map(indexedContact, contact);
            
            return contactElasticRepository.save(indexedContact);
        }

        return contactElasticRepository.save(contact);
    }

    @Override
    public void delete(String modelId) {
        contactElasticRepository.deleteById(modelId);
    }

    @Override
    public NativeSearchQuery buildQuery(String query) {
        return new NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.multiMatchQuery(query)
                .field("firstName")
                .field("lastName")
                .field("note")
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS))
            .build();
    }
}
