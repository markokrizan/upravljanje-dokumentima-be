package com.example.mail.payload.index;

import java.util.Map;
import java.util.List;

public class SearchResult<Model> {
   
    private Model model;
    private Map<String, List<String>> highlightedFields;

    public SearchResult() {

    }

    public SearchResult(Model model, Map<String, List<String>> highlightedFields) {
        this.model = model;
        this.highlightedFields = highlightedFields;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Map<String, List<String>> getHighlightedFields() {
        return highlightedFields;
    }

    public void setHighlightedFields(Map<String, List<String>> highlightedFields) {
        this.highlightedFields = highlightedFields;
    }
}
