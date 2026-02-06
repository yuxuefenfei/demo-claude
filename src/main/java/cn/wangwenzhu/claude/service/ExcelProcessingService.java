package cn.wangwenzhu.claude.service;

import cn.wangwenzhu.claude.util.ExcelReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Excel处理服务类
 * 负责协调Excel文件读取和数据处理业务逻辑
 */
@Slf4j
@Service
public class ExcelProcessingService {

    private final ExcelReader excelReader;

    public ExcelProcessingService() {
        this.excelReader = new ExcelReader();
    }

    /**
     * 执行Excel文件处理任务
     * 包括读取所有Excel文件和导出IP地址数据
     */
    public void processExcelFiles() {
        try {
            excelReader.list();
            log.info("Excel处理任务完成");
        } catch (Exception e) {
            log.error("Excel处理任务执行失败: {}", e.getMessage(), e);
        }
    }
}