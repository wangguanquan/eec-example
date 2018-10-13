package net.cua.other;

/**
 * response body
 * Create by guanquan.wang at 2018-10-13 14:03
 */
public class Resp {
    /** 返回码 */
    private int code;
    /** 消息 */
    private String msg;
    /** */
    private Object body;

    public static Resp success() {
        return new Resp();
    }
    public static Resp fail(int code) {
        return new Resp().setCode(code);
    }
    public static Resp fail(int code, String msg) {
        return new Resp().setCode(code).setMsg(msg);
    }
    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }
    /**
     * @param code the code to set
     */
    public Resp setCode(int code) {
        this.code = code;
        return this;
    }
    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }
    /**
     * @param msg the msg to set
     */
    public Resp setMsg(String msg) {
        this.msg = msg;
        return this;
    }
    /**
     * @return the body
     */
    public Object getBody() {
        return body;
    }
    /**
     * @param body the body to set
     */
    public Resp setBody(Object body) {
        this.body = body;
        return this;
    }

    /**
     * @return error when code &gt; 0
     */
    public boolean hasError() {
        return code > 0;
    }
}
