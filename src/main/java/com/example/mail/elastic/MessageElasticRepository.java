package com.example.mail.elastic;

import com.example.mail.payload.index.IndexableMessage;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessageElasticRepository extends ElasticsearchRepository<IndexableMessage, Long> {
    
}
