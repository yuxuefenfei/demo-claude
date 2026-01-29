package cn.wangwenzhu.claude.service;

import cn.wangwenzhu.claude.task.DemoTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class DemoTaskService {

    private final DemoTask demoTask;
    private final Random random = new Random();
    private final AtomicInteger taskCounter = new AtomicInteger(0);

    public DemoTaskService(DemoTask demoTask) {
        this.demoTask = demoTask;
    }

    @Scheduled(fixedRate = 2000) // 每2秒提交一个新任务
    public void submitRandomTasks() {
        int taskNumber = taskCounter.incrementAndGet();
        int taskType = random.nextInt(3);

        switch (taskType) {
            case 0:
                demoTask.executeQuickTask("Quick-Task-" + taskNumber);
                break;
            case 1:
                demoTask.executeRandomTask("Random-Task-" + taskNumber);
                break;
            case 2:
                demoTask.executeLongRunningTask("Long-Task-" + taskNumber);
                break;
        }

        log.debug("已提交任务 #{} (类型: {})", taskNumber, getTaskTypeName(taskType));
    }

    @Scheduled(cron = "0 0/2 * * * ?") // 每2分钟提交一批任务
    public void submitBatchTasks() {
        log.info("开始提交批量任务...");

        // 提交5-10个快速任务
        int quickTasks = random.nextInt(6) + 5;
        for (int i = 0; i < quickTasks; i++) {
            int taskNumber = taskCounter.incrementAndGet();
            demoTask.executeQuickTask("Batch-Quick-Task-" + taskNumber);
        }

        // 提交2-5个随机任务
        int randomTasks = random.nextInt(4) + 2;
        for (int i = 0; i < randomTasks; i++) {
            int taskNumber = taskCounter.incrementAndGet();
            demoTask.executeRandomTask("Batch-Random-Task-" + taskNumber);
        }

        log.info("批量任务提交完成: {}个快速任务, {}个随机任务", quickTasks, randomTasks);
    }

    private String getTaskTypeName(int taskType) {
        switch (taskType) {
            case 0: return "快速任务";
            case 1: return "随机任务";
            case 2: return "长任务";
            default: return "未知";
        }
    }
}