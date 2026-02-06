package cn.wangwenzhu.claude.runner;

import cn.wangwenzhu.claude.service.ExcelProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelProcessingRunner implements ApplicationRunner {

    private final ExcelProcessingService excelService;

    private final ThreadPoolTaskExecutor commonTaskExecutor;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始执行Excel处理任务");

        Future<?> future = commonTaskExecutor.submit(() -> {
            try {
                excelService.processExcelFiles();
                log.info("Excel处理任务正常结束");
            } catch (Exception e) {
                log.error("Excel处理任务异常结束，异常信息：{}", e.getMessage(), e);
                throw e;
            }
        });

        try {
            // 等待任务完成
            future.get();

            // 需要手动shutdown，否则应用会一直运行
            commonTaskExecutor.shutdown();
            log.info("Excel处理任务已完成，准备结束应用");
        } catch (Exception e) {
            log.error("Excel处理任务执行失败", e);
            throw e;
        }
    }
}