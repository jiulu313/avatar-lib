package com.helloworld.avarar.lib.util;

import android.net.Uri;
import android.text.TextUtils;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static String getStringFromArray(String[] array) {
        if (array == null || array.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i == array.length - 1) {
                sb.append(array[i]);
            } else {
                sb.append(array[i] + ",");
            }
        }

        return sb.toString();
    }

    public static String getUserNameFromEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "";
        }

        int index = email.indexOf("@");
        if (index != -1) {
            return email.substring(0, index);
        }

        return email;
    }

    /**
     * 获取邮箱前缀
     *
     * @param email
     * @return
     */
    public static String getPrefixFromEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "";
        }

        int index = email.indexOf("@");
        if (index != -1) {
            return email.substring(0, index);
        }

        return email;
    }


    /**
     * 获取邮箱后缀
     *
     * @param email 邮箱
     * @return 邮箱后缀
     */
    public static String getPostfixFromEmail(String email) {
        try {
            if (TextUtils.isEmpty(email)) {
                return "";
            }
            int index = email.indexOf("@");
            int length = email.length();
            if (index != -1) {
                return email.substring(index, length);
            }
        } catch (Exception e) {
            return email;
        }
        return "";
    }

    /**
     * 手机号保留前三位和后两位,中间六位打码
     *
     * @return
     */
    public static String addMaskToPhoneNo(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return "";
        }

        if (phone.length() > 9) {
            return phone.substring(0, 3).concat("******").concat(phone.substring(9));
        } else {
            return "";
        }
    }

    /**
     * 手机号邮箱中手机号保留前三位和后两位,中间六位打码
     *
     * @return
     */
    public static String addMaskToMobileEmail(String mobileEmail) {
        if (TextUtils.isEmpty(mobileEmail)) {
            return "";
        }
        String maskEmail = mobileEmail;
        if (mobileEmail.contains("@")) {
            String phoneNo = mobileEmail.substring(0, mobileEmail.indexOf("@"));
            maskEmail = addMaskToPhoneNo(phoneNo);
            maskEmail = maskEmail.concat(mobileEmail.substring(mobileEmail.indexOf("@")));
        }
        return maskEmail;
    }

    /**
     * 判断是否是字母
     *
     * @param str 传入字符串
     * @return 是字母返回true，否则返回false
     */
    public static boolean isAlpha(String str) {
        if (str == null) return false;
        return str.matches("[a-zA-Z]+");
    }

    /**
     * 获取字符串长度
     *
     * @param str
     * @return
     */
    public static int getLength(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        } else {
            return str.length();
        }
    }


    //从中英文都包含的字符串中提取最后2个中文
    public static String getChineseFromAllString(String text) {
        if (text == null || "".equals(text)) {
            return "";
        }

        char[] charArray = text.toCharArray();
        Stack<Character> stack = new Stack<>();
        for (int i = charArray.length - 1; i >= 0; i--) {
            char ch = charArray[i];
            if (ch > 0x4e00 && ch < 0x9fbb || (ch == '零' || ch == '一' || ch == '二' || ch == '三' || ch == '四' || ch == '五' || ch == '六' || ch == '七' || ch == '八' || ch == '九' || ch == '十')) {
                if (stack.size() < 2) {
                    stack.push(ch);
                } else {
                    break;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }

        return sb.toString();
    }

    /**
     * 获取中文个数
     *
     * @param text
     * @return
     */
    public static int getChineseCount(String text) {
        int count = 0;

        if (text == null || "".equals(text)) {
            return count;
        }
        String regEx = "[\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        while (m.find()) {
            count++;
        }

        return count;
    }

    public static int getEnglishCount(String text) {
        if (text == null || "".equals(text)) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isDigit(ch) || Character.isLowerCase(ch)) {
                count++;
            }
        }

        return count;
    }


    /**
     * "杜一凡（duyifan602647 - 新业务五部）" <duyifan602647@pwrd.com>
     *
     * @param str
     * @return 获取名字和email
     */
    public static String[] getNameAndEmail(String str) {
        String[] result = new String[2];
        result[0] = "";
        result[1] = "";
        if (str == null || "".equals(str)) {
            return result;
        }

        try {
            int i = str.indexOf('"');
            int j = str.lastIndexOf('"');
            if (i != -1 && j != -1) {
                result[0] = str.substring(i + 1, j);
            }

            int k = str.indexOf('<');
            int p = str.lastIndexOf('>');
            if (k != -1 && p != -1) {
                result[1] = str.substring(k + 1, p);
            }

            if (i == -1 || j == -1 || k == -1 || p == -1) {
                result[1] = str;
            }
        } catch (Exception e) {

        }

        return result;
    }

    public static String getOverFlowReason(String overflowReason) {
//        "pref_smtp_max_send_mail_size" : "邮件发送仅支持49.2MB以内大小",
//        "pref_smtp_max_num_rcpts" : "收件人太多了，留下几个下次再发吧",
//        "pref_trs_max_file_count" : "云附件空间已满，请使用电脑PC浏览器前往文件中心进行清理"

        String reason = "";
        if (TextUtils.isEmpty(overflowReason)) {
            reason = "";
        }
        if (overflowReason.equals("pref_smtp_max_send_mail_size")) {
            reason = "信件大小超过限制";
        } else if (overflowReason.equals("pref_trs_max_file_count")) {
            reason = "文件上传失败，云附件数量超过限制";
        } else if (overflowReason.equals("pref_smtp_max_num_rcpts")) {
            reason = "收件人数超过限制";
        }
        return reason;
    }

    public static boolean validPrefix(String text) {
        boolean b1 = text.startsWith(":");
        boolean b2 = text.startsWith(",");
        boolean b3 = text.startsWith("%");
        boolean b4 = text.startsWith("\"");
        boolean b5 = text.startsWith("\\");
        boolean b6 = text.startsWith("/");
        boolean b7 = text.startsWith("；");
        boolean b8 = text.startsWith("|");
        boolean b9 = text.startsWith("&");
        boolean b10 = text.startsWith(".");
        boolean b11 = text.startsWith("*");
        boolean b12 = text.startsWith("$");
        boolean b13 = text.startsWith("!");
        boolean b14 = text.startsWith("@");
        boolean b15 = text.startsWith("#");
        boolean b16 = text.startsWith("^");
        boolean b17 = text.startsWith("(");
        boolean b18 = text.startsWith(")");
        boolean b19 = text.startsWith("，");
        boolean b20 = text.startsWith("．");
        boolean b21 = text.startsWith("？");
        boolean b22 = text.startsWith("！");
        boolean b23 = text.startsWith("＠");
        boolean b24 = text.startsWith("＃");
        boolean b25 = text.startsWith("＄");
        boolean b26 = text.startsWith("％");
        boolean b27 = text.startsWith("＾");
        boolean b28 = text.startsWith("＆");
        boolean b29 = text.startsWith("＊");
        boolean b30 = text.startsWith("（");
        boolean b31 = text.startsWith("）");
        boolean b32 = text.startsWith("＿");
        boolean b33 = text.startsWith("＋");
        boolean b34 = text.startsWith("'");
        boolean b35 = text.startsWith(";");
        boolean b36 = text.startsWith("&");
        boolean b37 = text.startsWith("。");
        boolean b38 = text.startsWith("?");

        return !(b1 || b2 || b3 || b4 || b5 || b6 || b7 || b8 || b9 || b10
                || b11 || b12 || b13 || b14 || b15 || b16 || b17 || b18 || b19 || b20
                || b21 || b22 || b23 || b24 || b25 || b26 || b27 || b28 || b29 || b30
                || b31 || b32 || b33 || b34 || b35 || b36 || b37 || b38);
    }

    /**
     * String转Int
     *
     * @param number 将要转换的string值
     * @return 转换后的int值
     */
    public static int string2Int(String number) {
        if (TextUtils.isEmpty(number)) {
            return 1;
        }
        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            return 1;
        }
    }

    public static boolean isValidBlank(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }

        String str = content.trim();
        if (TextUtils.isEmpty(str)) {
            return false;
        }

        boolean allBlank = true;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') {
                allBlank = false;
                break;
            }
        }

        return !allBlank;
    }

    /**
     * 获取后缀
     *
     * @param format
     * @return
     */
    public static String getFormat(String format) {

        try {
            if (TextUtils.isEmpty(format)) {
                return "";
            }
            String ext = format.substring(format.lastIndexOf(".") + 1).toLowerCase();
            return ext;
        } catch (Exception e) {
            return format;
        }
    }

    public static String getDomainFromHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return "";
        }

        int index = host.indexOf(".");
        if (index == -1) {
            return "";
        }

        String domain = host.substring(index + 1);
        return domain;
    }

    /**
     * email.cn类的邮箱中获取子域名
     * 比如 aa@114.email.cn
     * 返回 114
     * @param email
     * @return
     */
    public static String getSubDomainForEmailCn(String email){
        if(TextUtils.isEmpty(email)){
            return "";
        }

        int count = getCountForChar(email,'.');
        if(count == 2){
            int i = email.indexOf("@");
            int j = email.indexOf(".");
            if(i > 0 && j > 0 && i + 1 < j){
                return email.substring(i + 1,j);
            }
        }
        return "";
    }

    public static String getDomainFromEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "";
        }

        int index = email.lastIndexOf("@");
        if (index == -1) {
            return "";
        }

        String domain = email.substring(index + 1);
        int count = getCountForChar(domain,'.');
        if(count == 2){
            index = domain.indexOf(".");
            if(index != -1){
                return domain.substring(index + 1);
            }
        }

        return domain;
    }

    /**
     * 获取测试环境的账户域名,测试专用，勿作他用！！！
     *
     * @param email
     * @return
     */
    public static String getTestDomainFromEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "";
        }

        int index = email.lastIndexOf("@");
        if (index == -1) {
            return "";
        }

        return email.substring(index + 1);
    }

    public static String replaceBlankBr(String s) {
        if (TextUtils.isEmpty(s)) {
            return "";
        }
        return s.trim().replaceAll("\n", "");
    }

    /**
     * 判断两个字符串是否相等且非空
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equalsAndNotEmpty(String a, String b) {
        return !TextUtils.isEmpty(a) && TextUtils.equals(a, b);
    }

    /**
     * 隐藏邮箱
     *
     * @param email
     * @return
     */
    public static String hideEmail(String email) {
        if (TextUtils.isEmpty(email) || !email.contains("@")) {
            return email;
        }
        String[] strs = email.split("@");
        String str0 = strs[0];
        String str1 = strs[1];
        if (str0.length() > 3) {
            str0 = str0.substring(str0.length() - 3);
        }
        return "***" + str0 + "@" + str1;

    }

    public static int getCountForChar(String s , char c){
        if(s == null){
            return 0;
        }

        int count = 0;
        for (int i =0;i < s.length();i++){
            char ss = s.charAt(i);
            if(ss == c){
                count++;
            }
        }
        return count;
    }



    /**
     * 根据传入的URL获取一级域名
     *
     * @param url
     * @return
     */
    public static String getDomain(String url) {
        String domain = "";
        if (!TextUtils.isEmpty(url) && (url.startsWith("http") || url.startsWith("https"))) {
            try {
                String host = Uri.parse(url).getHost();
                if (!TextUtils.isEmpty(host) && host.contains(".")) {
                    domain = host.substring(host.indexOf("."));
                }
            } catch (Exception ex) {
            }
        }
        return domain;
    }

    public static boolean isEmail(String email) {
        return !TextUtils.isEmpty(email) && email.contains("@") && !email.startsWith("@") && !email.endsWith("@");
        // return Pattern.matches(REGEX_EMAIL, email);
    }
}
