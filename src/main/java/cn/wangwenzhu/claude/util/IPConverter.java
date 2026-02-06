package cn.wangwenzhu.claude.util;

import java.math.BigInteger;

/**
 * IP地址转换工具类
 * 提供IP地址与十进制数值之间的相互转换功能
 */
public class IPConverter {

    /**
     * 将IP地址转换为十进制数值
     *
     * @param ipAddress IP地址字符串，格式如 "192.168.1.1"
     * @return 十进制数值字符串
     */
    public static String ipToDecimal(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return "0";
        }

        try {
            var parts = ipAddress.trim().split("\\.");
            if (parts.length != 4) {
                return "0";
            }

            var result = BigInteger.ZERO;
            for (var i = 0; i < 4; i++) {
                var part = Integer.parseInt(parts[i]);
                if (part < 0 || part > 255) {
                    return "0"; // 无效的IP地址段
                }
                result = result.multiply(BigInteger.valueOf(256)).add(BigInteger.valueOf(part));
            }

            return result.toString();
        } catch (NumberFormatException e) {
            return "0"; // 转换失败返回0
        }
    }

    /**
     * 将十进制数值转换为IP地址
     *
     * @param decimal 十进制数值字符串
     * @return IP地址字符串，格式如 "192.168.1.1"
     */
    public static String decimalToIp(String decimal) {
        if (decimal == null || decimal.trim().isEmpty()) {
            return "0.0.0.0";
        }

        try {
            var value = new BigInteger(decimal.trim());
            if (value.compareTo(BigInteger.ZERO) < 0 || value.compareTo(BigInteger.valueOf(4294967295L)) > 0) {
                return "0.0.0.0"; // 超出IP地址范围
            }

            var parts = new int[4];
            for (var i = 3; i >= 0; i--) {
                var divideAndRemainder = value.divideAndRemainder(BigInteger.valueOf(256));
                parts[i] = divideAndRemainder[1].intValue();
                value = divideAndRemainder[0];
            }

            return parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        } catch (NumberFormatException e) {
            return "0.0.0.0"; // 转换失败返回默认值
        }
    }

    /**
     * 验证IP地址格式是否有效
     *
     * @param ipAddress IP地址字符串
     * @return 是否有效
     */
    public static boolean isValidIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }

        var parts = ipAddress.trim().split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (var part : parts) {
                var value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 格式化输出IP地址及其对应的十进制数值
     *
     * @param ipAddress IP地址字符串
     * @return 格式化后的字符串
     */
    public static String formatIpWithDecimal(String ipAddress) {
        if (!isValidIp(ipAddress)) {
            return ipAddress + " -> 无效IP";
        }

        var decimal = ipToDecimal(ipAddress);
        return ipAddress + " -> " + decimal;
    }
}