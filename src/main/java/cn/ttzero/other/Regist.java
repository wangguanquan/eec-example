package cn.ttzero.other;

import org.ttzero.excel.annotation.ExcelColumn;

import java.sql.Timestamp;

/**
 * Create by guanquan.wang at 2018-10-14
 */
public class Regist {
    @ExcelColumn("ID")
    private int id;
    @ExcelColumn("产品ID")
    private int pro_id;
    @ExcelColumn("渠道ID")
    private int channel_no;
    @ExcelColumn("AID")
    private int aid;
    @ExcelColumn("注册账号")
    private String account;
    @ExcelColumn("注册时间")
    private Timestamp regist_time;
    @ExcelColumn("CPS用户ID")
    private int uid;
    @ExcelColumn("状态")
    private String platform;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPro_id() {
        return pro_id;
    }

    public void setPro_id(int pro_id) {
        this.pro_id = pro_id;
    }

    public int getChannel_no() {
        return channel_no;
    }

    public void setChannel_no(int channel_no) {
        this.channel_no = channel_no;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Timestamp getRegist_time() {
        return regist_time;
    }

    public void setRegist_time(Timestamp regist_time) {
        this.regist_time = regist_time;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
