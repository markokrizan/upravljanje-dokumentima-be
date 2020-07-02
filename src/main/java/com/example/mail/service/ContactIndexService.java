package com.example.mail.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import com.example.mail.model.Contact;
import com.example.mail.payload.index.IndexableContact;
import com.example.mail.payload.index.SearchResult;
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

    @Override
    public List<SearchResult<Contact>> search(String query, Long userId, Pageable pageable) {
        SearchHits<IndexableContact> contactSearchResults = elasticsearchTemplate.search(
            buildQuery(query, userId, pageable != null ? pageable : Contact.defaultPaging), 
            IndexableContact.class, 
            IndexCoordinates.of("contacts")
        );

        return contactSearchResults
            .getSearchHits()
            .stream()
            .map(contactSearchResult -> new SearchResult<Contact>(indexableModelMapper.convertFromIndexable(contactSearchResult.getContent()), contactSearchResult.getHighlightFields()))
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

        QueryBuilder contactTermQuery = QueryBuilders.multiMatchQuery(query)
            .field("firstName")
            .field("lastName")
            .field("note")
            .operator(Operator.OR)
            .fuzziness(Fuzziness.AUTO)
            .prefixLength(3);

        return new NativeSearchQueryBuilder()
                .withQuery(contactTermQuery)
                .withHighlightFields(
                    new HighlightBuilder.Field("firstName"),
                    new HighlightBuilder.Field("lastName"),
                    new HighlightBuilder.Field("note")
                )
                .withFilter(contactUserQuery)
                .withPageable(pageable)
            .build();
    }

    @Override
    public void bulkIndex(List<IndexableContact> contacts) {
        contactElasticRepository.saveAll(contacts);
    }
}
