package com.example.mail.payload.mappers;

import java.util.List;
import java.util.stream.Collectors;

import com.example.mail.model.Folder;
import com.example.mail.model.Message;
import com.example.mail.payload.index.IndexableMessage;
import com.example.mail.repository.FolderRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexableMessageMapper implements IndexableMapper<IndexableMessage, Message> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FolderRepository folderRepository;

    @Override
    public IndexableMessage convertToIndexable(Message message) {
        IndexableMessage indexableMessage = modelMapper.map(message, IndexableMessage.class);

        indexableMessage.setFolderId(message.getFolder().getId());

        return indexableMessage;
    }

    @Override
    public Message convertFromIndexable(IndexableMessage indexableMessage) {
        Message message = modelMapper.map(indexableMessage, Message.class);

        Folder messageFolder = folderRepository.findById(indexableMessage.getFolderId()).orElse(null);

        message.setFolder(messageFolder);
        message.setAccount(messageFolder.getAccount());

        return message;
    }

    @Override
    public List<IndexableMessage> convertToIndexables(List<Message> mainTypeList) {
        return mainTypeList
            .stream()
            .map(contact -> convertToIndexable(contact))
            .collect(Collectors.toList());
    }

    @Override
    public List<Message> convertFromIndexables(List<IndexableMessage> indexableTypeList) {
        return indexableTypeList
            .stream()
            .map(contact -> convertFromIndexable(contact))
            .collect(Collectors.toList());
    }
    
}
