package net.cua.other;

import net.cua.excel.annotation.DisplayName;

import java.sql.Timestamp;

/**
 * Create by guanquan.wang at 2018-10-14
 */
public class Regist {
    @DisplayName("ID")
    private int id;
    @DisplayName("产品ID")
    private int pro_id;
    @DisplayName("渠道ID")
    private int channel_no;
    @DisplayName("AID")
    private int aid;
    @DisplayName("注册账号")
    private String account;
    @DisplayName("注册时间")
    private Timestamp regist_time;
    @DisplayName("CPS用户ID")
    private int uid;
    @DisplayName("状态")
    private String platform;

}
