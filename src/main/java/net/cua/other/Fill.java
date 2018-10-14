package net.cua.other;

import net.cua.excel.annotation.DisplayName;

import java.sql.Timestamp;

/**
 * Create by guanquan.wang at 2018-10-14
 */
public class Fill {
    @DisplayName("ID")
    private int id;
    @DisplayName("AID")
    private int aid;
    @DisplayName("游戏编号")
    private int pro_id;
    @DisplayName("充值金额")
    private int fill_amoun;
    @DisplayName("充值时间")
    private Timestamp fill_time;
    @DisplayName("是否使用")
    private boolean use_flag;
}
