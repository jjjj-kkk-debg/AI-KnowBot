package com.aiknowbot.service;

import com.aiknowbot.entity.KnowledgeBase;
import com.aiknowbot.repository.KnowledgeBaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
    }

    public List<KnowledgeBase> getAll() {
        return knowledgeBaseRepository.findAll();
    }

    public KnowledgeBase getById(Long id) {
        return knowledgeBaseRepository.findById(id).orElse(null);
    }

    public KnowledgeBase save(KnowledgeBase kb) {
        return knowledgeBaseRepository.save(kb);
    }

    public void delete(Long id) {
        knowledgeBaseRepository.deleteById(id);
    }

    public List<KnowledgeBase> search(String keyword) {
        return knowledgeBaseRepository.searchByKeyword(keyword);
    }

    public List<KnowledgeBase> getByCategory(String category) {
        return knowledgeBaseRepository.findByCategory(category);
    }
}
