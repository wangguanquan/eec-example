package cn.ttzero.other;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Create by guanquan.wang at 2018-10-16 13:48
 */
@Component
public class CreateTableRunner implements ApplicationRunner {
    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        create();
    }

    private void create() {
     try (Connection con = dataSource.getConnection()) {
        if (!tableExists("t_brokerage_rate")) {
            System.out.println("创建渠道表");
            PreparedStatement ps = con.prepareStatement(
                    "CREATE TABLE `t_brokerage_rate` (\n" +
                            "  `id` integer primary key,\n" +
                            "  `uid` integer NOT NULL,\n" +
                            "  `pro_id` intteger DEFAULT NULL,\n" +
                            "  `platform_type` intteger DEFAULT NULL,\n" +
                            "  `rate` decimal(12,2) DEFAULT NULL,\n" +
                            "  `down_link` varchar(100) DEFAULT NULL,\n" +
                            "  `update_time` datetime DEFAULT NULL,\n" +
                            "  `update_emp` varchar(45) DEFAULT NULL\n" +
                            ")");
            ps.executeUpdate();
            ps.close();
        }

        if (!tableExists("wh_regist")) {
            System.out.println("创建注册表");
            PreparedStatement ps = con.prepareStatement(
                    "CREATE TABLE `wh_regist` (\n" +
                            "  `id` integer primary key,\n" +
                            "  `pro_id` int(11) NOT NULL,\n" +
                            "  `channel_no` char(4) DEFAULT NULL,\n" +
                            "  `aid` int(11) NOT NULL,\n" +
                            "  `account` varchar(45) NOT NULL,\n" +
                            "  `regist_time` datetime NOT NULL,\n" +
                            "  `platform_type` int(11) DEFAULT NULL,\n" +
                            "  `uid` int(11) DEFAULT NULL\n" +
                            ")");
            ps.executeUpdate();
            ps.close();
        }

        if (!tableExists("t_user")) {
            System.out.println("创建用户表");
            PreparedStatement ps = con.prepareStatement(
                    "CREATE TABLE `t_user` (\n" +
                            "  `id` integer primary key,\n" +
                            "  `name` varchar(45) DEFAULT NULL,\n" +
                            "  `account` varchar(45) NOT NULL,\n" +
                            "  `status` tinyint(1) DEFAULT NULL,\n" +
                            "  `city` varchar(45) DEFAULT NULL\n" +
                            ")");
            ps.executeUpdate();
            ps.close();
        }

        if (!tableExists("wh_fill")) {
            System.out.println("创建充值表");
            PreparedStatement ps = con.prepareStatement(
                    "CREATE TABLE `wh_fill` (\n" +
                            "  `id` integer primary key,\n" +
                            "  `aid` int(11) NOT NULL,\n" +
                            "  `pro_id` int(11) NOT NULL,\n" +
                            "  `fill_amount` int(11) DEFAULT 0,\n" +
                            "  `fill_time` datetime NOT NULL,\n" +
                            "  `use_flag` tinyint(1) DEFAULT NULL\n" +
                            ")");
            ps.executeUpdate();
            ps.close();
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    boolean tableExists(String tableName) {
        try (Connection con = dataSource.getConnection()) {
            PreparedStatement ps = con.prepareStatement("select 1 from " + tableName + " limit 1");
            return ps.execute();
        } catch (SQLException e) {
            return false;
        }
    }
}
