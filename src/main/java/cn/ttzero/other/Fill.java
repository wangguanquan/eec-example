package cn.ttzero.other;

import cn.ttzero.excel.annotation.DisplayName;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public int getPro_id() {
        return pro_id;
    }

    public void setPro_id(int pro_id) {
        this.pro_id = pro_id;
    }

    public int getFill_amoun() {
        return fill_amoun;
    }

    public void setFill_amoun(int fill_amoun) {
        this.fill_amoun = fill_amoun;
    }

    public Timestamp getFill_time() {
        return fill_time;
    }

    public void setFill_time(Timestamp fill_time) {
        this.fill_time = fill_time;
    }

    public boolean isUse_flag() {
        return use_flag;
    }

    public void setUse_flag(boolean use_flag) {
        this.use_flag = use_flag;
    }
}
