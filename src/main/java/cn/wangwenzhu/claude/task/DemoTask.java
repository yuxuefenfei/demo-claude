package cn.wangwenzhu.claude.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DemoTask {

    private final Random random = new Random();

    @Async("taskExecutor")
    public void executeRandomTask(String taskName) {
        try {
            // 生成1-10秒的随机执行时间
            int executionTime = random.nextInt(10) + 1;

            log.info("开始执行任务: {} (预计执行时间: {}秒)", taskName, executionTime);

            // 模拟任务执行
            TimeUnit.SECONDS.sleep(executionTime);

            log.info("任务完成: {} (实际执行时间: {}秒)", taskName, executionTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("任务被中断: {}", taskName, e);
        } catch (Exception e) {
            log.error("任务执行失败: {}", taskName, e);
        }
    }

    @Async("taskExecutor")
    public void executeLongRunningTask(String taskName) {
        try {
            // 生成长任务，10-30秒
            int executionTime = random.nextInt(21) + 10;

            log.info("开始执行长任务: {} (预计执行时间: {}秒)", taskName, executionTime);

            TimeUnit.SECONDS.sleep(executionTime);

            log.info("长任务完成: {} (实际执行时间: {}秒)", taskName, executionTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("长任务被中断: {}", taskName, e);
        } catch (Exception e) {
            log.error("长任务执行失败: {}", taskName, e);
        }
    }

    @Async("taskExecutor")
    public void executeQuickTask(String taskName) {
        try {
            // 快速任务，0.5-2秒
            int executionTime = random.nextInt(1500) + 500; // 毫秒

            log.info("开始执行快速任务: {} (预计执行时间: {}毫秒)", taskName, executionTime);

            TimeUnit.MILLISECONDS.sleep(executionTime);

            log.info("快速任务完成: {} (实际执行时间: {}毫秒)", taskName, executionTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("快速任务被中断: {}", taskName, e);
        } catch (Exception e) {
            log.error("快速任务执行失败: {}", taskName, e);
        }
    }
}