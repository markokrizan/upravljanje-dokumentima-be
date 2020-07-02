package com.example.mail.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import com.example.mail.model.Contact;
import com.example.mail.payload.index.IndexableContact;
import com.example.mail.payload.mappers.IndexableContactMapper;
import com.example.mail.elastic.ContactElasticRepository;

@Service
public class ContactIndexService implements IndexService<IndexableContact, Contact> {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private ContactElasticRepository contactElasticRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IndexableContactMapper indexableModelMapper;

    private final Integer DEFAULT_PER_PAGE = 10;

    private Pageable defaultPaging = PageRequest.of(0, DEFAULT_PER_PAGE);

    @Override
    public List<Contact> search(String query, Long userId, Pageable pageable) {
        SearchHits<IndexableContact> contactSearchResults = elasticsearchTemplate.search(
            buildQuery(query, userId, pageable != null ? pageable : defaultPaging), 
            IndexableContact.class, 
            IndexCoordinates.of("contacts")
        );

        return contactSearchResults
            .getSearchHits()
            .stream()
            .map(contact -> indexableModelMapper.convertFromIndexable(contact.getContent()))
            .collect(Collectors.toList());
    }

    @Override
    public IndexableContact upsert(Contact contact) {
        if (contact == null) {
            return null;
        }

        IndexableContact indexedContact = contactElasticRepository.findById(contact.getId()).orElse(null);

        if (contact.getId() != null && indexedContact != null) {
            modelMapper.map(contact, indexedContact);

            return contactElasticRepository.save(indexedContact);
        }

        return contactElasticRepository.save(indexableModelMapper.convertToIndexable(contact));
    }

    @Override
    public void delete(Long modelId) {
        contactElasticRepository.deleteById(modelId);
    }

    @Override
    public NativeSearchQuery buildQuery(String query, Long userId, Pageable pageable) {
        QueryBuilder contactUserQuery = QueryBuilders.termQuery("userId", userId);

        return new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(query)
                    .field("firstName")
                    .field("lastName")
                    .field("note")
                    .operator(Operator.OR)
                    .fuzziness(Fuzziness.AUTO)
                    .prefixLength(3)
                ).withFilter(contactUserQuery)
                .withPageable(pageable)
            .build();
    }

    @Override
    public void bulkIndex(List<IndexableContact> contacts) {
        contactElasticRepository.saveAll(contacts);
    }
}
