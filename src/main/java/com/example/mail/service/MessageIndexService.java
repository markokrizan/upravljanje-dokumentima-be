package com.example.mail.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.mail.elastic.MessageElasticRepository;
import com.example.mail.model.Message;
import com.example.mail.payload.index.IndexableMessage;
import com.example.mail.payload.index.SearchResult;
import com.example.mail.payload.mappers.IndexableMessageMapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.common.unit.Fuzziness;

@Service
public class MessageIndexService implements IndexService<IndexableMessage, Message> {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private MessageElasticRepository messageElasticRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IndexableMessageMapper indexableModelMapper;

    @Override
    public List<SearchResult<Message>> search(String query, Long folderId, Pageable pageable) {
        SearchHits<IndexableMessage> messageSearchResults = elasticsearchTemplate.search(
            buildQuery(query, folderId, pageable != null ? pageable : Message.defaultPaging), 
            IndexableMessage.class, 
            IndexCoordinates.of("messages")
        );

        return messageSearchResults
            .getSearchHits()
            .stream()
            .map(messageSearchResult -> new SearchResult<Message>(indexableModelMapper.convertFromIndexable(messageSearchResult.getContent()), messageSearchResult.getHighlightFields()))
            .collect(Collectors.toList());
    }

    @Override
    public NativeSearchQuery buildQuery(String query, Long folderId, Pageable pageable) {
        QueryBuilder messageFolderQuery = QueryBuilders.termQuery("folderId", folderId);

        QueryBuilder messageTermQuery = QueryBuilders.multiMatchQuery(query)
            .field("from")
            .field("to")
            .field("subject")
            .field("content")
            .operator(Operator.OR)
            .fuzziness(Fuzziness.AUTO)
            .prefixLength(3);
 
        return new NativeSearchQueryBuilder()
                .withQuery(messageTermQuery)
                .withHighlightFields(
                    new HighlightBuilder.Field("from"),
                    new HighlightBuilder.Field("to"),
                    new HighlightBuilder.Field("subject"),
                    new HighlightBuilder.Field("content")
                )
                .withFilter(messageFolderQuery)
                .withPageable(pageable)
            .build();
    }

    @Override
    public IndexableMessage upsert(Message message) {
        if (message == null) {
            return null;
        }

        IndexableMessage indexedMessage = messageElasticRepository.findById(message.getId()).orElse(null);

        if (message.getId() != null && indexedMessage != null) {
            modelMapper.map(message, indexedMessage);

            return messageElasticRepository.save(indexedMessage);
        }

        return messageElasticRepository.save(indexableModelMapper.convertToIndexable(message));
    }

    @Override
    public void delete(Long modelId) {
        messageElasticRepository.deleteById(modelId);
    }

    @Override
    public void bulkIndex(List<IndexableMessage> messages) {
        messageElasticRepository.saveAll(messages);
    }
}
