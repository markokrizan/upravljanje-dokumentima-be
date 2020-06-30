package com.example.mail.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.mail.elastic.MessageElasticRepository;
import com.example.mail.model.Message;
import com.example.mail.payload.index.IndexableMessage;
import com.example.mail.payload.mappers.IndexableMessageMapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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
    public List<Message> search(String query, Long folderId) {
        SearchHits<IndexableMessage> messageSearchResults = elasticsearchTemplate.search(
            buildQuery(query, folderId), 
            IndexableMessage.class, 
            IndexCoordinates.of("messages")
        );

        return messageSearchResults
            .getSearchHits()
            .stream()
            .map(contact -> indexableModelMapper.convertFromIndexable(contact.getContent()))
            .collect(Collectors.toList());
    }

    @Override
    public NativeSearchQuery buildQuery(String query, Long folderId) {
        QueryBuilder messageFolderQuery = QueryBuilders.termQuery("folderId", folderId);

        return new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(query)
                    .field("from")
                    .field("to")
                    .field("subject")
                    .field("content")
                    .operator(Operator.OR)
                    .fuzziness(Fuzziness.AUTO)
                    .prefixLength(3)
                ).withFilter(messageFolderQuery)
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
