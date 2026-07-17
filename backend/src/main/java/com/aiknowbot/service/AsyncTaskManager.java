package com.aiknowbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AsyncTaskManager {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskManager.class);

    public enum TaskStatus { PENDING, COMPLETED, FAILED }

    public static class TaskResult {
        private TaskStatus status;
        private String content;

        public TaskResult(TaskStatus status, String content) {
            this.status = status;
            this.content = content;
        }

        public TaskStatus getStatus() { return status; }
        public String getContent() { return content; }
    }

    private final Map<Long, TaskResult> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(0);

    public long createTask() {
        long id = idGen.incrementAndGet();
        tasks.put(id, new TaskResult(TaskStatus.PENDING, null));
        return id;
    }

    public void completeTask(long taskId, String content) {
        tasks.put(taskId, new TaskResult(TaskStatus.COMPLETED, content));
    }

    public void failTask(long taskId, String error) {
        tasks.put(taskId, new TaskResult(TaskStatus.FAILED, error));
    }

    public TaskResult getTask(long taskId) {
        return tasks.get(taskId);
    }
}
