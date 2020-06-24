package com.example.mail.service;

import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import java.util.List;

public interface IndexService<T> {
    public List<T> search(String query);
    public NativeSearchQuery buildQuery(String query);
    public T upsert(T model);
    public void delete(String modelId);
}
