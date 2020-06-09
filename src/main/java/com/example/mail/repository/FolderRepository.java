package com.example.mail.repository;

import com.example.mail.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByAccountId(Long accountId);
    Folder findByName(String name);

    @Query("FROM Folder f WHERE f.account.id = ?1 and f.name = ?2")
    Folder findByAccountFolderName(Long accountId, String name);
}
