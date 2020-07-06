package com.example.mail.repository;

import com.example.mail.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Set<Photo> findByContactId(Long contactId);
}
