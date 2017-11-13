package com.suntek.commonlibrary.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 输入校检工具类
 * Created by wudeng on 2017/7/31.
 */

public class RegexUtils {

    /**
     * Email正则表达式
     */
    private static final String EMAIL = "\\\\w+@\\\\w+\\\\.[a-z]+(\\\\.[a-z]+)?";
    /**
     * 身份证正则表达式
     */
    private static final String IDENTITY = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}";
    /**
     * 手机号正则表达式
     */
    private static final String PHONE = "^(13[0-9]|14[0-9]|15[0-9]|17[0-9]|18[0-9])\\d{8}$";
    /**
     * 中文正则表达式
     */
    private static final String CHINESE = "^[\u4E00-\u9FA5]+$";
    /**
     * 生日正则表达式
     */
    private static final String BIRTHDAY = "[1-9]{4}([-./])\\d{1,2}\\1\\d{1,2}";
    /**
     * URL正则表达式
     */
    private static final String URL = "(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?";
    /**
     * ip 加 端口的正则表达式
     */
    private static final String IP_AND_PORT = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{2,5}";
    /**
     * 年龄正则表达式 ^(?:[1-9][0-9]?|1[01][0-9]|120)$ 匹配0-120岁
     */
    private static final String AGE="^(?:[1-9][0-9]?|1[01][0-9]|120)$";
    /**
     * 邮编正则表达式 [0-9]\d{5}(?!\d) 国内6位邮编
     */
    private static final String CODE="[0-9]\\d{5}(?!\\d)";

    private static final String CAR_NUM = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$";
    /**
     * 匹配由数字、26个英文字母或者下划线组成的字符串 ^\w+$
     */
    private static final String STR_ENG_NUM_="^\\w+$";
    /**
     * 匹配由数字和26个英文字母组成的字符串 ^[A-Za-z0-9]+$
     */
    private static final String STR_ENG_NUM="^[A-Za-z0-9]+";
    /**
     * 匹配由26个英文字母组成的字符串 ^[A-Za-z]+$
     */
    private static final String STR_ENG="^[A-Za-z]+$";


    /**
     * 验证Email
     * @param email email地址，格式：zhangsan@zuidaima.com，zhangsan@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkEmail(String email) {
        return Pattern.matches(EMAIL, email);
    }

    /**
     * 验证身份证号码
     * @param identity 居民身份证号码15位或18位，最后一位可能是数字或字母
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkIdCard(String identity) {
        return (identity.length() == 15 || identity.length() == 18) && Pattern.matches(IDENTITY,identity);
    }

    /**
     * 验证手机号码
     * @param mobile 移动、联通、电信运营商的号码段
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkMobile(String mobile) {
        return mobile.length() == 11 &&  Pattern.matches(PHONE,mobile);
    }

    /**
     * 验证固定电话号码
     * @param phone 电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
     * <p><b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9 的一位或多位数字，
     * 数字之后是空格分隔的国家（地区）代码。</p>
     * <p><b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号——
     * 对不使用地区或城市代码的国家（地区），则省略该组件。</p>
     * <p><b>电话号码：</b>这包含从 0 到 9 的一个或多个数字 </p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkPhone(String phone) {
        String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
        return Pattern.matches(regex, phone);
    }

    /**
     * 验证整数（正整数和负整数）
     * @param digit 一位或多位0-9之间的整数
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkDigit(String digit) {
        String regex = "\\-?[1-9]\\d+";
        return Pattern.matches(regex,digit);
    }

    /**
     * 验证整数和浮点数（正负整数和正负浮点数）
     * @param decimals 一位或多位0-9之间的浮点数，如：1.23，233.30
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkDecimals(String decimals) {
        String regex = "\\-?[1-9]\\d+(\\.\\d+)?";
        return Pattern.matches(regex,decimals);
    }

    /**
     * 验证空白字符
     * @param blankSpace 空白字符，包括：空格、\t、\n、\r、\f、\x0B
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkBlankSpace(String blankSpace) {
        String regex = "\\s+";
        return Pattern.matches(regex,blankSpace);
    }

    /**
     * 验证中文
     * @param chinese 中文字符
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkChinese(String chinese) {
        return Pattern.matches(CHINESE,chinese);
    }

    /**
     * 验证日期（年月日）
     * @param birthday 日期，格式：1992-09-03，或1992.09.03
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkBirthday(String birthday) {
        return Pattern.matches(BIRTHDAY,birthday);
    }

    /**
     * 验证年龄
     * @param age 年龄
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkAge(String age){
        return Pattern.matches(AGE,age);
    }

    /**
     * 验证车牌，涵盖全国各省简称
     * @param carNum 完整车牌号，如 京A8888
     * @return
     */
    public static boolean chackCarNum(String carNum){
        return Pattern.matches(CAR_NUM,carNum);
    }
    /**
     * 验证URL地址
     * @param url 格式：http://blog.csdn.net:80/xyang81/article/details/7705960? 或 http://www.csdn.net:80
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkURL(String url) {
        return Pattern.matches(URL, url);
    }

    /**
     * 获取网址 URL 的一级域
     * @param url 地址
     * @return 一级域名
     */
    public static String getDomain(String url) {
        Pattern p = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(url);
        matcher.find();
        return matcher.group();
    }

    /**
     * 匹配中国邮政编码
     * @param postcode 邮政编码
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkPostcode(String postcode) {
        return Pattern.matches(CODE, postcode);
    }

    /**
     * 匹配IP地址（含端口）(简单匹配，格式，如：192.168.1.1:8080,单节点小于255，端口小于65535)
     * @param ipAddress IPv4标准地址
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkIpAddress(String ipAddress) {
        boolean ok = true;
        if (Pattern.matches(IP_AND_PORT, ipAddress)){
            String[] split = ipAddress.split(":");
            String ip = split[0];
            String port = split[1];
            String[] ips = ip.split("\\.");
            if (Integer.parseInt(port) > 65535){
                ok = false;
            }else {
                for (String i : ips){
                    if (Integer.parseInt(i) > 255){
                        ok = false;
                    }
                }
            }
        }else {
            ok = false;
        }
        return ok;
    }
}
