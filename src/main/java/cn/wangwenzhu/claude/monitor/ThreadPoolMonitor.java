package cn.wangwenzhu.claude.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ThreadPoolMonitor {

    private final ThreadPoolTaskExecutor taskExecutor;

    public ThreadPoolMonitor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Scheduled(fixedRate = 5000) // 每5秒输出一次线程池状态
    public void monitorThreadPool() {
        ThreadPoolExecutor executor = taskExecutor.getThreadPoolExecutor();

        int corePoolSize = executor.getCorePoolSize();
        int maximumPoolSize = executor.getMaximumPoolSize();
        int poolSize = executor.getPoolSize();
        int activeCount = executor.getActiveCount();
        long taskCount = executor.getTaskCount();
        long completedTaskCount = executor.getCompletedTaskCount();
        int queueSize = executor.getQueue().size();
        int queueRemainingCapacity = executor.getQueue().remainingCapacity();

        StringBuilder status = new StringBuilder();
        status.append("\n=== 线程池状态监控 ===")
                .append("\n核心线程数:").append(corePoolSize)
                .append("\n最大线程数: ").append(maximumPoolSize)
                .append("\n当前线程数: ").append(poolSize)
                .append("\n活跃线程数: ").append(activeCount)
                .append("\n总任务数: ").append(taskCount)
                .append("\n已完成任务数: ").append(completedTaskCount)
                .append("\n队列中任务数: ").append(queueSize)
                .append("\n队列剩余容量: ").append(queueRemainingCapacity)
                .append("\n任务完成率: ").append(taskCount > 0 ? String.format("%.2f%%", (completedTaskCount * 100.0 / taskCount)) : "0.00%")
                .append("\n==================\n");

        log.info(status.toString());
    }

    @Scheduled(fixedRate = 30000) // 每30秒输出详细统计信息
    public void detailedStatistics() {
        ThreadPoolExecutor executor = taskExecutor.getThreadPoolExecutor();

        long taskCount = executor.getTaskCount();
        long completedTaskCount = executor.getCompletedTaskCount();
        long rejectedTaskCount = taskCount - completedTaskCount - executor.getActiveCount() - executor.getQueue().size();

        log.info("=== 线程池详细统计 (30秒) ===");
        log.info("总提交任务数: {}", taskCount);
        log.info("已完成任务数: {}", completedTaskCount);
        log.info("处理中任务数: {}", executor.getActiveCount());
        log.info("队列中任务数: {}", executor.getQueue().size());
        log.info("估计拒绝任务数: {}", Math.max(0, rejectedTaskCount));
        log.info("平均任务处理速度: {:.2f} 任务/分钟",
                taskCount > 0 ? (completedTaskCount * 2.0) : 0.0);
        log.info("===============================\n");
    }
}