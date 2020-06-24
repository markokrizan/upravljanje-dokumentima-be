package com.example.mail.elastic;

import com.example.mail.model.Contact;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContactElasticRepository extends ElasticsearchRepository<Contact, String> {

}
