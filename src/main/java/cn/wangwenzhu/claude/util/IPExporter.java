package cn.wangwenzhu.claude.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * IP地址导出工具类
 * 将IP地址和对应的十进制数值导出到CSV文件
 */
@Slf4j
public class IPExporter {

    /**
     * IP地址数据记录
     */
    public record IPEntry(String fileName, String ipAddress, String decimalValue) {
    }

    private final List<IPEntry> ipEntries = new ArrayList<>();

    /**
     * 添加IP地址记录
     *
     * @param fileName     源Excel文件名
     * @param ipAddress    IP地址
     * @param decimalValue 十进制数值
     */
    public void addIPEntry(String fileName, String ipAddress, String decimalValue) {
        ipEntries.add(new IPEntry(fileName, ipAddress, decimalValue));
    }

    /**
     * 导出到CSV文件（优化格式：每个文件只显示一次文件名）
     *
     * @param outputPath 输出文件路径
     * @return 是否导出成功
     */
    public boolean exportToCSV(String outputPath) {
        if (ipEntries.isEmpty()) {
            System.out.println("没有数据可导出");
            return false;
        }

        try (var writer = new OutputStreamWriter(
                new FileOutputStream(outputPath), StandardCharsets.UTF_8)) {
            // 写入BOM标记，让Excel正确识别UTF-8编码
            writer.write('\uFEFF');
            // 写入CSV标题行
            writer.write("源文件名,IP地址,十进制数值\n");

            // 按文件名分组导出
            var currentFileName = (String) null;
            for (IPEntry entry : ipEntries) {
                if (!entry.fileName().equals(currentFileName)) {
                    // 新文件，先写入文件标题行
                    writer.write(String.format("\"%s\",,\n", entry.fileName()));
                    // 再写入第一个IP地址
                    writer.write(String.format(",\"%s\",\"%s\"\n",
                            entry.ipAddress(),
                            entry.decimalValue()));
                    currentFileName = entry.fileName();
                } else {
                    // 同一文件的其他IP地址，文件名留空
                    writer.write(String.format(",\"%s\",\"%s\"\n",
                            entry.ipAddress(),
                            entry.decimalValue()));
                }
            }

            System.out.println("成功导出 " + ipEntries.size() + " 条记录到: " + outputPath);

            // 生成SQL查询语句
            generateSQLQuery();

            return true;

        } catch (IOException e) {
            log.error("导出文件时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成SQL查询语句
     */
    private void generateSQLQuery() {
        if (ipEntries.isEmpty()) {
            log.info("没有数据可生成SQL查询");
            return;
        }

        var sql = new StringBuilder();
        sql.append("SELECT * FROM sop_position_info WHERE device_no IN (");

        // 添加所有十进制数值
        for (int i = 0; i < ipEntries.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("'").append(ipEntries.get(i).decimalValue()).append("'");
        }

        sql.append(");");

        log.info("\n=== SQL查询语句 ===");
        log.info(sql.toString());
        log.info("==================\n");
    }

    /**
     * 获取记录数量
     *
     * @return 记录数量
     */
    public int getRecordCount() {
        return ipEntries.size();
    }

    /**
     * 清空所有记录
     */
    public void clear() {
        ipEntries.clear();
    }
}