package cn.ttzero.other;

import org.ttzero.excel.annotation.ExcelColumn;

import java.sql.Timestamp;

/**
 * Create by guanquan.wang at 2018-10-14
 */
public class Fill {
    @ExcelColumn("ID")
    private int id;
    @ExcelColumn("AID")
    private int aid;
    @ExcelColumn("游戏编号")
    private int pro_id;
    @ExcelColumn("充值金额")
    private int fill_amoun;
    @ExcelColumn("充值时间")
    private Timestamp fill_time;
    @ExcelColumn("是否使用")
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
