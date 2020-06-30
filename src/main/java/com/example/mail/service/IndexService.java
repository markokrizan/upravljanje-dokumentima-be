package com.example.mail.service;

import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import java.util.List;

public interface IndexService<T> {
    public List<T> search(String query, Long userId);
    public NativeSearchQuery buildQuery(String query, Long userId);
    public T upsert(T model);
    public void delete(Long modelId);
    public void bulkIndex(List<T> models);
}
