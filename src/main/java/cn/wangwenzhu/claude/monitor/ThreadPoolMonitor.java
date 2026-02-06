package cn.wangwenzhu.claude.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ThreadPoolMonitor {

    private final ThreadPoolTaskExecutor taskExecutor;

    @Scheduled(fixedRate = 5000) // 每5秒输出一次线程池状态
    public void monitorThreadPool() {
        var executor = taskExecutor.getThreadPoolExecutor();

        var corePoolSize = executor.getCorePoolSize();
        var maximumPoolSize = executor.getMaximumPoolSize();
        var poolSize = executor.getPoolSize();
        var activeCount = executor.getActiveCount();
        var taskCount = executor.getTaskCount();
        var completedTaskCount = executor.getCompletedTaskCount();
        var queueSize = executor.getQueue().size();
        var queueRemainingCapacity = executor.getQueue().remainingCapacity();

        String status = "\n=== 线程池状态监控 ===" +
                "\n核心线程数:" + corePoolSize +
                "\n最大线程数: " + maximumPoolSize +
                "\n当前线程数: " + poolSize +
                "\n活跃线程数: " + activeCount +
                "\n总任务数: " + taskCount +
                "\n已完成任务数: " + completedTaskCount +
                "\n队列中任务数: " + queueSize +
                "\n队列剩余容量: " + queueRemainingCapacity +
                "\n任务完成率: " + (taskCount > 0 ? String.format("%.2f%%", (completedTaskCount * 100.0 / taskCount)) : "0.00%") +
                "\n==================\n";

        log.info(status);
    }

    @Scheduled(fixedRate = 30000) // 每30秒输出详细统计信息
    public void detailedStatistics() {
        var executor = taskExecutor.getThreadPoolExecutor();

        var taskCount = executor.getTaskCount();
        var completedTaskCount = executor.getCompletedTaskCount();
        var rejectedTaskCount = taskCount - completedTaskCount - executor.getActiveCount() - executor.getQueue().size();

        log.info("=== 线程池详细统计 (30秒) ===");
        log.info("总提交任务数: {}", taskCount);
        log.info("已完成任务数: {}", completedTaskCount);
        log.info("处理中任务数: {}", executor.getActiveCount());
        log.info("队列中任务数: {}", executor.getQueue().size());
        log.info("估计拒绝任务数: {}", Math.max(0, rejectedTaskCount));
        log.info("平均任务处理速度: {} 任务/分钟", taskCount > 0 ? (completedTaskCount * 2.0) : 0.0);
        log.info("===============================\n");
    }
}