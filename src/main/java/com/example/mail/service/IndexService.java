package com.example.mail.service;

import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import java.util.List;

public interface IndexService<IndexableType, MainModelType> {
    public List<MainModelType> search(String query, Long ownerId);
    public NativeSearchQuery buildQuery(String query, Long ownerId);
    public IndexableType upsert(MainModelType model);
    public void delete(Long modelId);
    public void bulkIndex(List<IndexableType> models);
}
