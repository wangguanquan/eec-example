package cn.ttzero.export;

import org.ttzero.excel.entity.WaterMark;
import org.ttzero.excel.entity.EmptySheet;
import org.ttzero.excel.entity.Sheet;
import org.ttzero.excel.entity.Workbook;
import org.ttzero.excel.entity.style.Border;
import org.ttzero.excel.entity.style.BorderStyle;
import org.ttzero.excel.entity.style.Charset;
import org.ttzero.excel.entity.style.Fill;
import org.ttzero.excel.entity.style.Font;
import org.ttzero.excel.entity.style.Horizontals;
import org.ttzero.excel.entity.style.NumFmt;
import org.ttzero.excel.entity.style.PatternType;
import org.ttzero.excel.entity.style.Styles;
import org.ttzero.excel.manager.Const;
import cn.ttzero.other.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Create by guanquan.wang at 2018-10-13 14:02
 */
@Service
public class ExportService {
    @Autowired
    private DataSource dataSource;

    @Value("${excel.creator}")
    private String creator;
    @Value("${excel.company}")
    private String company;
    @Value("${excel.storage.path}")
    private String path;

    /* 产口列表 */
    private final String[] pros = {"", "LOL", "WOW", "极品飞车", "守望先锋", "怪物世界", "天堂", "吃鸡", "炉石传说", "星际2", "魔兽世界"};
    /* 设置值是否共享 */
    private final boolean share = true;

    /**
     * 导出充值表
     * @param limit
     */
    public void fill(int limit, OutputStream os) {
        try (Connection con = dataSource.getConnection()) {
            new Workbook("用户充值" + limit, creator)
                .setCompany(company)
                .setConnection(con)
                .setAutoSize(true)
                .addSheet("用户充值"
                    , "select id, aid, pro_id, fill_amount, fill_time, use_flag from wh_fill limit ?"
                    , ps -> ps.setInt(1, limit)
                    , new Sheet.Column("ID", int.class)
                    , new Sheet.Column("AID", int.class)
                    , new Sheet.Column("产品ID", int.class, i -> pros[i], share) // 设置共享
                    , new Sheet.Column("充值金额", int.class).setType(Const.ColumnType.RMB)
                    , new Sheet.Column("充值时间", Timestamp.class)
                    , new Sheet.Column("是否使用", boolean.class)
                )
                .writeTo(os);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 改变隔行颜色
     * use Workbook.setOddFill() or Sheet.setOddFill()
     */
    public void changeOddStyle(OutputStream os) {
        try (Connection con = dataSource.getConnection()) {
            new Workbook("重置隔行颜色", creator)
                .setConnection(con)
                .setAutoSize(true)
                .setCompany(company)
                .setOddFill(new Fill(PatternType.solid, Color.YELLOW))
                .addSheet("用户注册表"
                    , "select id,pro_id,channel_no,aid,account,regist_time,uid,platform_type from wh_regist limit 1000"
                    , new Sheet.Column("ID", int.class)
                    , new Sheet.Column("产品ID", int.class, i -> pros[i], share)
                    , new Sheet.Column("渠道ID", int.class)
                    , new Sheet.Column("AID", int.class)
                    , new Sheet.Column("注册账号", String.class)
                    , new Sheet.Column("注册时间", Timestamp.class)
                    , new Sheet.Column("CPS用户ID", int.class)
                    , new Sheet.Column("渠道类型", int.class)
                )
                .addSheet("用户表"
                    , "select id, name, account, status from t_user limit 200"
                    , new Sheet.Column("编号", int.class).setWidth(10) // 重新设定宽度
                    , new Sheet.Column("登录名", String.class)
                    , new Sheet.Column("通行证", String.class)
                    // 将状态码转为可识别的文字
                    , new Sheet.Column("状态", char.class, c -> c == 0 ? "正常" : "停用")
                        .setStyleProcessor((n, style, sst) -> {
                            if ((int)n != 0) {
                                // 背景设红,
                                style = Styles.clearFill(style) | sst.addFill(new Fill(Color.red));
                                // 字体改为斜体
                                int italic = sst.addFont(sst.getFont(style).clone().italic());
                                style = Styles.clearFont(style) | italic;
                            }
                            // 文本左右居中
                            style = Styles.clearHorizontal(style) | Horizontals.CENTER;
                            return style;
                        }).setWidth(6.38) // 重新设定宽度
                )
                .writeTo(os);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取消隔行变色
     * use Workbook.cancelOddStyle() or Sheet.cancelOddStyle()
     */
    public void cancelOddFill(OutputStream os) {
        try (Connection con = dataSource.getConnection()) {
            Workbook wb = new Workbook("单页-固定宽度-ThreeZero样式-测试", creator)
                    .setConnection(con)
                    .setCompany(company)
                    .cancelOddFill(); // 取消隔行变色

            // 申请number format
            int newFmt =  wb.getStyles().addNumFmt(new NumFmt("0.000_);[Red]\\(0.000\\)"));

            wb.addSheet("CPS渠道分成基础表"
                , "select uid, pro_id, platform_type, rate,down_link,update_time,update_emp from t_brokerage_rate"
                , new Sheet.Column("渠道编号", int.class)
                , new Sheet.Column("产品", int.class, i -> pros[i], share)
                , new Sheet.Column("渠道类型", int.class)
                , new Sheet.Column("分成比例", double.class)
                    // 小数点后保留3位
                    .setCellStyle(Styles.clearNumfmt(Styles.defaultDoubleBorderStyle()) | newFmt)// 方式1 效率更高
//                    .setStyleProcessor((o, style, sst) -> Styles.clearNumfmt(style) | sst.addNumFmt(new NumFmt("0.000_);[Red]\\(0.000\\)"))) // 方式2
                , new Sheet.Column("下载链接", String.class)
                , new Sheet.Column("更新时间", Timestamp.class)
                , new Sheet.Column("更新者", String.class)
            ).writeTo(os);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 对象数组 & Map数组 导出
     */
    public void objectAndMapSheet(OutputStream os) {
        long start = System.currentTimeMillis();
        Workbook wb = new Workbook("List<?>-Map<String, ?>-测试", creator)
            .setAutoSize(true) // Auto-size
            .setWaterMark(WaterMark.of("colvin"));
        wb.addSheet(new EmptySheet().hidden()); // 设置此Sheet为隐藏

        java.util.List<Map<String, Object>> mapData = new ArrayList<>();
        for (int i = 0; i < 251; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "colvin" + i);
            map.put("age", i);
            map.put("date", new Timestamp(start + 12000));
            mapData.add(map);
        }

        // head columns 决定导出顺序
        wb.addSheet("Map测试", mapData
            , new Sheet.Column("姓名", "name")
            , new Sheet.Column("年龄", "age")
                .setStyleProcessor((o, style, sst) -> {
                    if (((int) o) > 150) {
                        style = Styles.clearFill(style) | sst.addFill(Fill.parse("#ff0000"));
                    }
                    return style;
                })
            , new Sheet.Column("录入时间", "date")
        );
        Border border = new Border();
        border.setBorder(BorderStyle.DOTTED, Color.red);
        border.setBorderBottom(BorderStyle.NONE);

        Fill fill = new Fill();
        fill.setPatternType(PatternType.solid);
        fill.setFgColor(Color.GRAY);
        fill.setBgColor(Color.decode("#ccff00"));

        Font font = new Font("Klee", 14, Font.Style.bold, Color.white);
        font.setCharset(Charset.GB2312);
        wb.getSheet("Map测试").setHeadStyle(font, fill, border);

        Random random = ThreadLocalRandom.current();
        List<UserInfo> objectData = new ArrayList<>();
        int size = random.nextInt(100) + 1;
        String[] proArray = {"LOL", "WOW", "极品飞车", "守望先锋", "怪物世界"};
        UserInfo e;
        while (size-- > 0) {
            e = new UserInfo();
            e.setId(size);
            e.setChannelId(random.nextInt(10) + 1);
            e.setPro(proArray[random.nextInt(5)]);
            e.setAccount(getRandom(random));
            e.setRegistered(new Timestamp(start += random.nextInt(8000)));
            e.setUp30(random.nextInt(10) > 3);
            e.setAddress(getRandom(random));
            e.setC((char) ('A' + random.nextInt(26)));
            objectData.add(e);
        }
        wb.addSheet("Object测试", objectData);  //  方式1
        wb.getSheet("Object测试") // Set style
            .setHeadStyle(Font.parse("'under line' 11 Klee red")
                , Fill.parse("#666699 solid")
                , Border.parse("thin #ff0000").setDiagonalDown(BorderStyle.THIN, Color.CYAN));
        wb.setName("New name"); // Rename

        wb.addSheet("Object copy", objectData  // 方式2
            , new Sheet.Column("渠道ID", "id")
            , new Sheet.Column("游戏", "pro")
            , new Sheet.Column("账户", "account")
            , new Sheet.Column("是否满30级", "up30")
            , new Sheet.Column("渠道", "channelId", n -> n < 5 ? "自媒体" : "联众", true)
            , new Sheet.Column("注册时间", "registered")
        );
        try {
            wb.writeTo(os);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    ////////////////////////////////////////////性能测试/////////////////////////////////////////
    /**
     * 单SQL1000W导出
     */
    public void W1000_one_sql_LimitSheet() {
        try (Connection con = dataSource.getConnection()) {
            new Workbook("千万-单SQL", creator)
                .setConnection(con)
                .setAutoSize(true)
                .addSheet("用户充值"
                    , "select id, aid, pro_id, fill_amount, fill_time, use_flag from wh_fill"
                    , new Sheet.Column("ID", int.class)
                    , new Sheet.Column("AID", int.class)
                            .setCellStyle(Styles.clearHorizontal(Styles.defaultIntBorderStyle()) | Horizontals.CENTER)
                    , new Sheet.Column("产品ID", int.class)
                    , new Sheet.Column("充值金额", int.class)
                    , new Sheet.Column("充值时间", Timestamp.class)
                    , new Sheet.Column("是否使用", int.class)
                )
                .writeTo(Paths.get(path));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1000万提交分页
     */
    public void SplitK1000AutoSizeConverSheet() {
        try (Connection con = dataSource.getConnection()) {
            Workbook wb = new Workbook("1000万用户充值", creator)
                    .setConnection(con)
                    .setAutoSize(true);

            String sql = "select id,aid,pro_id,fill_amount,fill_time,use_flag from wh_fill where id between ? and ?";
            Sheet.Column[] columns = {
                new Sheet.Column("ID", int.class)
                , new Sheet.Column("AID", int.class)
                , new Sheet.Column("游戏编号", int.class)
                , new Sheet.Column("充值金额", int.class).setType(Const.ColumnType.RMB)
                , new Sheet.Column("充值时间", Timestamp.class)
                , new Sheet.Column("是否使用", int.class)
            };

            // 10 * 100_10000 1m 42s
            for (int i = 0; i < 10; i++) {
                final int n = i, step = 100_0000;
                wb.addSheet("用户充值"
                    , sql
                    , ps -> {
                        ps.setInt(1, n * step);
                        ps.setInt(2, (n+1) * step);
                    }
                    , columns
                );
            }
            wb.writeTo(Paths.get(path));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 1000万 join 200万 join 100查询15W+-数据
     */
    public void mutableSharedAutoSizeConverStyleSheet() {
        try (Connection con = dataSource.getConnection()) {
            new Workbook("1000Wx200Wx100", creator)
                    .setCompany(company)
                    .setConnection(con)
                    .setAutoSize(true)
                    .addSheet("充值明细", "select t1.aid, t1.pro_id, t1.fill_amount, t1.fill_time, t2.account, t2.platform_type, t3.uid, t1.fill_amount * t3.rate\n" +
                                    "from wh_fill t1, wh_regist t2, t_brokerage_rate t3 \n" +
                                    "where t1.aid = t2.aid\n" +
                                    "and t2.uid = t3.uid\n" +
                                    "and t1.fill_time > ?\n" +
                                    "order by t1.fill_time desc"
                            , ps -> ps.setString(1, "2018-09-15")
                            , new Sheet.Column("AID", int.class)
                            , new Sheet.Column("游戏", int.class, n -> pros[n], share)
                                    .setCellStyle(Styles.clearHorizontal(Styles.defaultStringBorderStyle()) | Horizontals.CENTER)
                            , new Sheet.Column("充值金额", int.class).setType(Const.ColumnType.RMB)
                            , new Sheet.Column("日期", Date.class)
                            , new Sheet.Column("帐号", String.class)
                            , new Sheet.Column("平台", int.class, i -> i == 1 ? "Android" : "iOS")
                                    .setCellStyle(Styles.clearHorizontal(Styles.defaultStringBorderStyle()) | Horizontals.CENTER)
                            , new Sheet.Column("分渠用户ID", String.class)
                            , new Sheet.Column("渠道分成", double.class)
                    )
                    .writeTo(Paths.get(path));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 200W测试
     */
    public void K200AutoSizeConverSheet() {
        // 4m 59s
        try (Connection con = dataSource.getConnection()) {
            new Workbook("200万-自动宽度-转化-测试", creator)
                    .setConnection(con)
                    .setAutoSize(true)
                    .addSheet("用户注册"
                            , "select id,pro_id,channel_no,aid,account,regist_time,uid,platform_type from wh_regist"
                            , new Sheet.Column("ID", int.class)
                            , new Sheet.Column("产品ID", int.class)
                            , new Sheet.Column("渠道ID", int.class)
                            , new Sheet.Column("AID", int.class)
                            , new Sheet.Column("注册账号", String.class)
                            , new Sheet.Column("注册时间", Timestamp.class)
                            , new Sheet.Column("CPS用户ID", int.class)
                            , new Sheet.Column("状态", char.class, c -> c == 0 ? "正常" : "停用")
                    )
                    .writeTo(Paths.get(path));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    char[] charArray = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
    char[][] cache = {new char[6], new char[7], new char[8], new char[9], new char[10]};
    public String getRandom(Random random) {
        int n = random.nextInt(5), size = charArray.length;
        char[] cs = cache[n];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = charArray[random.nextInt(size)];
        }
        return new String(cs);
    }
}
