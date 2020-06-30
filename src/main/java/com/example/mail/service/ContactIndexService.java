package com.example.mail.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.apache.lucene.search.join.ScoreMode;
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

    @Override
    public List<Contact> search(String query, Long userId) {
        SearchHits<Contact> contactSearchResults = elasticsearchTemplate.search(
            buildQuery(query, userId), 
            Contact.class, 
            IndexCoordinates.of("contacts")
        );

        return contactSearchResults
            .getSearchHits()
            .stream()
            .map(contact -> modelMapper.map(contact.getContent(), Contact.class))
            .collect(Collectors.toList());
    }

    @Override
    public Contact upsert(Contact contact) {
        if (contact == null) {
            return null;
        }


        /**
         *  Jackson nor spring-data-elasticsearch annotation cannot ommit serialization of fields at model level - but should according to the docs
         * 
         *  This could be an issue with the current version - this is a filthy hack to solve infinite recursion
         * 
         *  */ 
        contact.getUser().setAccounts(null);

        Contact indexedContact = contactElasticRepository.findById(contact.getId()).orElse(null);

        if (contact.getId() != null && indexedContact != null) {
            modelMapper.map(contact, indexedContact);

            return contactElasticRepository.save(indexedContact);
        }

        return contactElasticRepository.save(contact);
    }

    @Override
    public void delete(Long modelId) {
        contactElasticRepository.deleteById(modelId);
    }

    @Override
    public NativeSearchQuery buildQuery(String query, Long userId) {
        QueryBuilder contactUserQuery = QueryBuilders.nestedQuery(
            "user", 
            QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("user.id", userId)), 
            ScoreMode.None
        );

        return new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(query)
                    .field("firstName")
                    .field("lastName")
                    .field("note")
                    .operator(Operator.OR)
                    .fuzziness(Fuzziness.AUTO)
                    .prefixLength(3)
                ).withFilter(contactUserQuery)
            .build();
    }

    @Override
    public void bulkIndex(List<Contact> contacts) {
        contactElasticRepository.saveAll(contacts);
    }
}
