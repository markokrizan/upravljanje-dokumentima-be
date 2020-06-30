package com.example.mail.elastic;

import com.example.mail.payload.index.IndexableContact;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContactElasticRepository extends ElasticsearchRepository<IndexableContact, Long> {

}
