package com.aiknowbot.repository;

import com.aiknowbot.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    List<KnowledgeBase> findByCategory(String category);

    @Query("SELECT k FROM KnowledgeBase k WHERE k.title LIKE %:keyword% OR k.content LIKE %:keyword%")
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword);
}
