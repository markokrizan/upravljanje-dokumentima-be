package com.example.mail.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import java.util.List;

import com.example.mail.payload.index.SearchResult;

public interface IndexService<IndexableType, MainModelType> {
    public List<SearchResult<MainModelType>> search(String query, Long ownerId, Pageable pageable);
    public NativeSearchQuery buildQuery(String query, Long ownerId, Pageable pageable);
    public IndexableType upsert(MainModelType model);
    public void delete(Long modelId);
    public void bulkIndex(List<IndexableType> models);
}
