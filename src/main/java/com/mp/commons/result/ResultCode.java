package com.mp.commons.result;

/**
 * 统一响应结果码。code 为响应体中的业务状态码。
 *
 * @author 刘瑞麒
 * @date 2026/11/5 12:16
 */
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "请求资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源状态冲突"),
    ERROR(500, "系统异常，请稍后重试"),

    /** 与现有登录接口约定一致：刷新令牌过期或校验失败，需要重新登录。 */
    RELOGIN_REQUIRED(40102, "校验失败，要求重新登录");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
