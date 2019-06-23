package cn.ttzero.other;

import org.ttzero.excel.annotation.ExcelColumn;
import org.ttzero.excel.annotation.IgnoreExport;

import java.sql.Timestamp;

/**
 * Create by guanquan.wang at 2018-10-13 15:59
 */
public class UserInfo {
    @ExcelColumn("渠道ID")
    private int channelId;
    @ExcelColumn(value = "游戏", share = true)
    private String pro;
    @ExcelColumn
    private String account;
    @ExcelColumn("注册时间")
    private Timestamp registered;
    @ExcelColumn("是否满30级")
    private boolean up30;
    @IgnoreExport("敏感信息不导出")
    private int id; // not export
    private String address;
    @ExcelColumn("VIP")
    private char c;

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public void setPro(String pro) {
        this.pro = pro;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setRegistered(Timestamp registered) {
        this.registered = registered;
    }

    public void setUp30(boolean up30) {
        this.up30 = up30;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setC(char c) {
        this.c = c;
    }

    public int getChannelId() {
        return channelId;
    }

    public String getPro() {
        return pro;
    }

    public String getAccount() {
        return account;
    }

    public Timestamp getRegistered() {
        return registered;
    }

    public boolean isUp30() {
        return up30;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public char getC() {
        return c;
    }
}
