package com.example.mail.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mail.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByFolderId(Long folderId, Pageable pageable);
}
