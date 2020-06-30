package com.example.mail.elastic;

import com.example.mail.model.Message;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessageElasticRepository extends ElasticsearchRepository<Message, Long> {
    
}
