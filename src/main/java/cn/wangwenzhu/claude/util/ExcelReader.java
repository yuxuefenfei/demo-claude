package cn.wangwenzhu.claude.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class ExcelReader {

    private static final IPExporter exporter = new IPExporter();

    public static void readExcelFile(String filePath) {
        // 提取文件名
        var fileName = new File(filePath).getName();
        try (var fis = new FileInputStream(filePath);
             var workbook = new XSSFWorkbook(fis)) {

            log.info("开始读取Excel文件: {}", fileName);

            // 获取第一个工作表
            var sheet = workbook.getSheetAt(0);
            log.info("工作表名称: {}", sheet.getSheetName());

            // 获取标题行，找到"通讯主机号"列的索引（也可能是第一列）
            var headerRow = sheet.getRow(0);
            int commHostColumnIndex = -1;

            if (headerRow != null) {
                // 首先尝试找到确切的"通讯主机号"列
                for (Cell cell : headerRow) {
                    String cellValue = getCellValueAsString(cell);
                    if ("通讯主机号".equals(cellValue)) {
                        commHostColumnIndex = cell.getColumnIndex();
                        break;
                    }
                }

                // 如果没找到确切的匹配，检查是否包含"通讯"关键字
                if (commHostColumnIndex == -1) {
                    for (Cell cell : headerRow) {
                        String cellValue = getCellValueAsString(cell);
                        if (cellValue.contains("通讯")) {
                            commHostColumnIndex = cell.getColumnIndex();
                            break;
                        }
                    }
                }

                // 如果还是没找到，默认使用第一列
                if (commHostColumnIndex == -1 && headerRow.getCell(0) != null) {
                    commHostColumnIndex = 0;
                    log.info("提示: 使用第一列作为通讯主机号列");
                }
            }

            if (commHostColumnIndex == -1) {
                log.warn("无法确定通讯主机号列");
                return;
            }

            log.info("通讯主机号列表:");

            // 遍历所有数据行，只打印通讯主机号列
            var isFirstRow = true;
            var dataRowCount = 0;
            for (var row : sheet) {
                // 跳过标题行
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // 尝试多种方式获取单元格数据
                var cellValue = "";
                var cell = row.getCell(commHostColumnIndex);

                if (cell != null) {
                    cellValue = getCellValueAsString(cell);
                } else {
                    // 如果getCell返回null，尝试使用getCell的另一种方式
                    try {
                        cell = row.getCell(commHostColumnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        if (cell != null) {
                            cellValue = getCellValueAsString(cell);
                        }
                    } catch (Exception e) {
                        // 忽略异常
                    }
                }

                // 如果还是为空，尝试遍历该行所有非空单元格来查找可能的IP地址
                if (cellValue.isEmpty() || cellValue.trim().isEmpty()) {
                    for (Cell rowCell : row) {
                        String rowCellValue = getCellValueAsString(rowCell);
                        // 检查是否是IP地址格式
                        if (rowCellValue.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
                            cellValue = rowCellValue;
                            break;
                        }
                    }
                }

                if (!cellValue.trim().isEmpty()) {
                    var ip = cellValue.trim();
                    var decimal = IPConverter.ipToDecimal(ip);
                    var formattedOutput = ip + " -> " + decimal;
                    log.info("  {}", formattedOutput);

                    // 添加到导出列表
                    exporter.addIPEntry(fileName, ip, decimal);
                    dataRowCount++;
                }
            }
            log.info("共找到 {} 个通讯主机号", dataRowCount);

            log.info("=== 文件读取完成 ===\n");

        } catch (IOException e) {
            log.error("读取Excel文件时发生错误: {}", e.getMessage(), e);
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return cell.toString();
        }
    }

    public void list() {
        var templatesPath = "src/main/resources/templates/";
        var templatesDir = new File(templatesPath);

        if (!templatesDir.exists() || !templatesDir.isDirectory()) {
            log.warn("templates目录不存在");
            return;
        }

        // 获取所有xlsx文件
        var xlsxFiles = templatesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xlsx"));

        if (xlsxFiles == null || xlsxFiles.length == 0) {
            log.info("没有找到xlsx文件");
            return;
        }

        log.info("找到 {} 个Excel文件", xlsxFiles.length);

        // 读取每个Excel文件
        for (var file : xlsxFiles) {
            readExcelFile(file.getAbsolutePath());
        }

        // 导出到CSV文件
        var outputPath = "IP地址导出.csv";
        if (exporter.exportToCSV(outputPath)) {
            log.info("\n=== 导出完成 ===");
            log.info("文件已保存到: {}", new java.io.File(outputPath).getAbsolutePath());
        }
    }
}
