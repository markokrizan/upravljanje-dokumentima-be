package com.example.mail.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import com.example.mail.payload.index.SearchResult;

public interface IndexService<IndexableType, MainModelType> {
    public PageImpl<SearchResult<MainModelType>> search(String query, Long ownerId, Pageable pageable);
    public NativeSearchQuery buildQuery(String query, Long ownerId, Pageable pageable);
    public IndexableType upsert(MainModelType model);
    public void delete(Long modelId);
    public void bulkIndex(List<IndexableType> models);
}
