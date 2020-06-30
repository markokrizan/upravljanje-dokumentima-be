package com.example.mail.payload.mappers;

import java.util.List;

public interface IndexableMapper<IndexableType, MainType> {
    public IndexableType convertToIndexable(MainType mainType);
    public MainType convertFromIndexable(IndexableType indexableType);
    
    public List<IndexableType> convertToIndexables(List<MainType> mainTypeList);
    public List<MainType> convertFromIndexables(List<IndexableType> indexableTypeList);
}
