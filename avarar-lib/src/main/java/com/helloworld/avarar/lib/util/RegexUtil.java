package com.helloworld.avarar.lib.util;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    //邮箱
    private static  final String REGEX_EMAIL = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";

    //判断是否包含汉字（不包括中文标点）
    public static final String REGEX_CHINESE_CONTAIN = "[\u4E00-\u9FA5]";

    /**
     * TODO 是否是合法邮件地址,为避免ANR暂时不用正则表达式
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        return !TextUtils.isEmpty(email) && email.contains("@") && !email.startsWith("@") && !email.endsWith("@") && getCountOfChar(email,'@') == 1;
        // return Pattern.matches(REGEX_EMAIL, email);
    }


    public static int getCountOfChar(String str , char c){
        if(str == null || "".equals(str)){
            return 0;
        }

        int count = 0;
        for (int i =0;i < str.length();i++){
            if(str.charAt(i) == c) {
                count++;
            }
        }

        return count;
    }


    //判断是否包含汉字（不包括中文标点）
    public static boolean containChinese(String str){
        if(str == null || "".equals(str)){
            return false;
        }

        Pattern p = Pattern.compile(REGEX_CHINESE_CONTAIN);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    //判断是否包含英文,数字
    public static boolean containEnglish(String str){
        if(str == null || "".equals(str)){
            return false;
        }

        Pattern p = Pattern.compile("[a-zA-Z0-9]");
        Matcher m = p.matcher(str);
        if(m.find()){
            return true;
        }

        return false;
    }

    /**
     * 是否是合法的密码
     * @param password 待验证的密码
     * @return true: 合法的密码   false:  不合法的密码
     *
     * 注: 密码规则是6-16从位的字母+数字
     */
    public static boolean isValidPassword(String password){
        if(TextUtils.isEmpty(password)){
            return false;
        }

        String s = "^(?=.*[A-Za-z])(?=.*\\d)[\\x21-\\x7E]{6,20}$";
        Pattern p = Pattern.compile(s);
        Matcher m = p.matcher(password);
        if(m.find()){
            return true;
        }

        return false;
    }

    /**
     * 是否是有效的手机号
     * @return
     */
    public static boolean isValidPhone(String phone){
        Pattern p = Pattern.compile("^1[3456789]\\d{9}$");
        Matcher m = p.matcher(phone);
        if(m.find()){
            return true;
        }

        return false;

//        if(phone == null || phone.length() != 9 || phone.charAt(0) != '1'){
//            return false;
//        }
//
//        char second = phone.charAt(1);
//        if(second == '0' || second == '1' || second == '2'){
//            return false;
//        }
//
//        try {
//            Long.parseLong(phone);
//        }catch (Exception e){
//            return false;
//        }
//
//        return true;
    }

}
