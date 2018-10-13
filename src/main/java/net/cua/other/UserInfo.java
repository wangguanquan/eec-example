package net.cua.other;

import net.cua.excel.annotation.DisplayName;
import net.cua.excel.annotation.NotExport;

import java.sql.Timestamp;

/**
 * Create by guanquan.wang at 2018-10-13 15:59
 */
public class UserInfo {
    @DisplayName("渠道ID")
    private int channelId;
    @DisplayName(value = "游戏", share = true)
    private String pro;
    @DisplayName
    private String account;
    @DisplayName("注册时间")
    private Timestamp registered;
    @DisplayName("是否满30级")
    private boolean up30;
    @NotExport("敏感信息不导出")
    private int id; // not export
    private String address;
    @DisplayName("VIP")
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
}
