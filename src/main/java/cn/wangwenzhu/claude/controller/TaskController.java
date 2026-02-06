package cn.wangwenzhu.claude.controller;

import cn.wangwenzhu.claude.task.DemoTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Slf4j
@RequiredArgsConstructor
public class TaskController {

    private final DemoTask demoTask;

    private final ThreadPoolTaskExecutor taskExecutor;

    @PostMapping("/quick")
    public ResponseEntity<Map<String, String>> submitQuickTask(@RequestParam String taskName) {
        demoTask.executeQuickTask(taskName);
        var response = new HashMap<String, String>();
        response.put("message", "快速任务已提交: " + taskName);
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/random")
    public ResponseEntity<Map<String, String>> submitRandomTask(@RequestParam String taskName) {
        demoTask.executeRandomTask(taskName);
        var response = new HashMap<String, String>();
        response.put("message", "随机任务已提交: " + taskName);
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/long")
    public ResponseEntity<Map<String, String>> submitLongTask(@RequestParam String taskName) {
        demoTask.executeLongRunningTask(taskName);
        var response = new HashMap<String, String>();
        response.put("message", "长任务已提交: " + taskName);
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getThreadPoolStatus() {
        var executor = taskExecutor.getThreadPoolExecutor();

        var status = new HashMap<String, Object>();
        status.put("corePoolSize", executor.getCorePoolSize());
        status.put("maximumPoolSize", executor.getMaximumPoolSize());
        status.put("poolSize", executor.getPoolSize());
        status.put("activeCount", executor.getActiveCount());
        status.put("taskCount", executor.getTaskCount());
        status.put("completedTaskCount", executor.getCompletedTaskCount());
        status.put("queueSize", executor.getQueue().size());
        status.put("queueRemainingCapacity", executor.getQueue().remainingCapacity());
        status.put("taskCompletionRate",
                executor.getTaskCount() > 0 ?
                        String.format("%.2f%%", (executor.getCompletedTaskCount() * 100.0 / executor.getTaskCount())) :
                        "0.00%");

        return ResponseEntity.ok(status);
    }
}