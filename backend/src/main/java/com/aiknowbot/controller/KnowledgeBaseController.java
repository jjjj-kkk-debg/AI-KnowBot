package com.aiknowbot.controller;

import com.aiknowbot.dto.ApiResult;
import com.aiknowbot.entity.KnowledgeBase;
import com.aiknowbot.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public ApiResult<List<KnowledgeBase>> getAll() {
        return ApiResult.success(knowledgeBaseService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResult<KnowledgeBase> getById(@PathVariable Long id) {
        KnowledgeBase kb = knowledgeBaseService.getById(id);
        if (kb == null) {
            return ApiResult.error("知识条目不存在");
        }
        return ApiResult.success(kb);
    }

    @GetMapping("/search")
    public ApiResult<List<KnowledgeBase>> search(@RequestParam String keyword) {
        return ApiResult.success(knowledgeBaseService.search(keyword));
    }

    @GetMapping("/category/{category}")
    public ApiResult<List<KnowledgeBase>> getByCategory(@PathVariable String category) {
        return ApiResult.success(knowledgeBaseService.getByCategory(category));
    }

    @PostMapping
    public ApiResult<KnowledgeBase> create(@RequestBody KnowledgeBase knowledgeBase) {
        return ApiResult.success(knowledgeBaseService.save(knowledgeBase));
    }

    @PutMapping("/{id}")
    public ApiResult<KnowledgeBase> update(@PathVariable Long id, @RequestBody KnowledgeBase knowledgeBase) {
        KnowledgeBase existing = knowledgeBaseService.getById(id);
        if (existing == null) {
            return ApiResult.error("知识条目不存在");
        }
        knowledgeBase.setId(id);
        return ApiResult.success(knowledgeBaseService.save(knowledgeBase));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        knowledgeBaseService.delete(id);
        return ApiResult.success(null);
    }
}
