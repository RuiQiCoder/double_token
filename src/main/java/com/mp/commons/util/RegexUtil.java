package com.mp.commons.util;

import java.util.regex.Pattern;

/**
 * 用户名和密码的格式校验工具。
 * 仅检查格式，不负责用户名查重、密码哈希或登录时的密码比对。
 *
 * @author 刘瑞麒
 * @date 2026/11/2 14:27
 */
public final class RegexUtil {

    // Pattern 是编译后的正则表达式，定义为常量，避免每次校验时重复编译。
    // [A-Za-z]：第一位必须是英文字母。
    // [A-Za-z0-9_]：后面的字符只能是英文字母、数字或下划线。
    // {3,63}：后面允许 3～63 个字符，加上第一位，总长度为 4～64 位。
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("[A-Za-z][A-Za-z0-9_]{3,63}");

    // (?=.*[A-Za-z])：向后检查，要求整个密码至少包含一个英文字母。
    // (?=.*[0-9])：向后检查，要求整个密码至少包含一个数字。
    // 上面两个 (?=...) 只检查条件，本身不消耗字符。
    // [!-~]：ASCII 中从 ! 到 ~ 的可见字符，包括字母、数字和英文符号，不包含空格。
    // {8,64}：密码长度必须是 8～64 位。
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("(?=.*[A-Za-z])(?=.*[0-9])[!-~]{8,64}");

    // 工具类只提供静态方法，不需要通过 new RegexUtil() 创建对象。
    private RegexUtil() {
    }

    /**
     * 用户名：4～64 位，以英文字母开头，仅允许英文字母、数字和下划线。
     * 不自动去除空白，null 或格式不符时返回 false。
     *
     * @param userName 待检查的用户名，例如 user_01
     * @return true 表示格式符合规则，false 表示不符合
     */
    public static boolean checkUserName(String userName) {
        // && 会短路：如果为 null，后面的 length() 和 matcher() 就不会执行。
        // 先检查长度，避免对过长输入执行正则；matches() 要求整个字符串匹配。
        return userName != null
                && userName.length() >= 4 && userName.length() <= 64
                && USERNAME_PATTERN.matcher(userName).matches();
    }

    /**
     * 密码：8～64 位，至少包含一个英文字母和一个数字。
     * 允许 ASCII 可见字符（含英文符号），不允许空格、中文或换行。
     * 不裁剪或修改密码，null 或格式不符时返回 false。
     *
     * @param password 待检查的原始密码
     * @return true 表示格式符合规则，false 表示不符合
     */
    public static boolean checkPassWord(String password) {
        // 不对密码执行 trim()，避免悄悄改变用户输入的密码。
        // 先排除 null 和长度不符的输入，再检查字符范围以及字母、数字条件。
        return password != null
                && password.length() >= 8 && password.length() <= 64
                && PASSWORD_PATTERN.matcher(password).matches();
    }
}
