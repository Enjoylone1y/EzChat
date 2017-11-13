package com.ezreal.ezchat.utils;

import java.text.DecimalFormat;

/**
 * Created by wudeng on 2017/10/20.
 */

public class ConvertUtils {

    /**
     * 通过 byte 数值计算得到 String格式的 文件大小，如：11.1KB,10.12MB
     *
     * @param byteSize 文件大小 单位 byte
     * @return String格式的 文件大小
     */
    public static String getSizeString(long byteSize) {

        if (byteSize <= 0) {
            return "0B";
        }

        if (byteSize < 1024) {
            return String.valueOf(byteSize) + "B";
        }

        long k = byteSize / 1024;
        if (k < 1024) {
            double minK = byteSize % 1024 / 1024.0;
            return new DecimalFormat("#.00").format(k + minK) + "KB";
        }

        long r = 1024 * 1024;
        long m = byteSize / r;
        double minM = byteSize % r / (r * 1.0);
        return new DecimalFormat("#.00").format(m + minM) + "MB";
    }

    /**
     * 根据返回状态码得到提示信息
     *
     * @param code 状态码
     * @return 提示信息字符串
     */
    public static String code2String(int code) {

        switch (code) {
            case 302:
                return "用户不存在或密码错误";
            case 408:
                return "服务器无响应";
            case 415:
                return "网络中断，与服务器连接失败";
            case 416:
                return "请求过频，请稍后再试";
            case 417:
                return "自动登录失败，请手动尝试";
            case 1000:
                return "数据库未打开";
            case 422:
                return "账户被禁用";
            default:
                return "";
        }
    }
}
