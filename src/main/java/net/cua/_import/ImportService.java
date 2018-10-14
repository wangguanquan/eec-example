package net.cua._import;

import net.cua.excel.reader.ExcelReader;
import net.cua.excel.reader.Row;
import net.cua.excel.reader.Sheet;
import net.cua.other.Fill;
import net.cua.other.Regist;
import net.cua.other.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by guanquan.wang at 2018-10-14
 */
@Service
public class ImportService {
    @Autowired
    private DataSource dataSource;
    @Value("${excel.storage.path}")
    private String path;

    Path defaultPath = Paths.get(path);

    /**
     * 使用while遍历所有行
     */
    public void readerWhile() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("单表－单Sheet-固定宽度.xlsx"))) {
            Row row;
            // get first sheet
            Sheet sheet = reader.sheet(0);
            // skip header row
            sheet.nextRow();

            while ((row = sheet.nextRow()) != null) {
                System.out.println(row.getInt(0) + " | "
                        + row.getInt(1) + " | "
                        + row.getInt(2) + " | "
                        + row.getInt(3) + " | "
                        + row.getString(4) + " | "
                        + row.getDate(5) + " | "
                        + row.getInt(6) + " | "
                        + row.getInt(7));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用iterator遍历所有行
     */
    public void readerIterator() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("多表-自动宽度-转化-样式-测试 (1).xlsx"))) {
            // get first sheet
            Sheet sheet = reader.sheet(0);

            for (
                    Iterator<Row> ite = sheet.iterator();
                    ite.hasNext();
                    System.out.println(ite.next())
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 多个sheet页读取
     */
    public void reader() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("New name.xlsx"))) {
            reader.sheets().flatMap(sheet -> {
                System.out.println();
                System.out.println("第" + sheet.getIndex() + "个sheet页：" + sheet.getName());
                return sheet.rows();
            }).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将Excel数据导入到数据库
     */
    public void import0() {
        try (Connection con = dataSource.getConnection();
             ExcelReader reader = ExcelReader.read(defaultPath.resolve("200万-自动宽度-转化-测试 (1).xlsx"))) {
            // close auto commit
            boolean autoCommit;
            if (autoCommit = con.getAutoCommit()) {
                con.setAutoCommit(false);
            }
            reader.sheets().forEach(s -> {
                System.out.println("开始导入" + s.getName());
                try {
                    PreparedStatement ps = con.prepareStatement("insert into wh_regist(pro_id,channel_no,aid,account,regist_time,uid,platform_type) values(?,?,?,?,?,?,?)");
                    // 丢弃表头
                    s.nextRow();
                    Row row;
                    int n = 0;
                    while ((row = s.nextRow()) != null) {
                        // set parameters
                        // skip id column row.get(0)
                        ps.setInt(1, row.getInt(1));
                        ps.setString(2, row.getString(2));
                        ps.setInt(3, row.getInt(3));
                        ps.setString(4, row.getString(4));
                        ps.setTimestamp(5, row.getTimestamp(5));
                        ps.setInt(6, row.getInt(6));

                        int platform;
                        switch (row.getString(7)) {
                            case "iOS":
                                platform = 1;
                                break;
                            case "Android":
                                platform = 2;
                                break;
                            case "PC":
                                platform = 3;
                                break;
                            default:
                                platform = 0;
                        }
                        ps.setInt(7, platform);
                        ps.addBatch();
                        if (n++ % 100_000 == 0) {
                            ps.executeBatch();
                            con.commit();
                        }
                    }
                    ps.executeBatch();
                    ps.close();
                    con.commit();
                    System.out.println("导入{}成功." + s.getName());
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            });
            if (autoCommit) {
                con.setAutoCommit(true);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * read excel to object array
     */
    public void excelToArray() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("New name.xlsx"))) {
            UserInfo[] array = reader.sheet(2)
                    .rowsWithOutHeader()
                    .map(row -> row.to(UserInfo.class))
                    .toArray(UserInfo[]::new);

            for (UserInfo e : array) {
                System.out.println(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * read with filter
     */
    public void readWithFilter() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("New name.xlsx"))) {
            List<UserInfo> list = reader.sheet(2)
                    .rowsWithOutHeader()
                    .map(row -> row.to(UserInfo.class))
                    .filter(UserInfo::isUp30)
                    .collect(Collectors.toList());

            for (UserInfo e : list) {
                System.out.println(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * memory share object
     */
    public void readToOnceObject() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("New name.xlsx"))) {
            reader.sheet(2)
                    .rowsWithOutHeader()
                    .map(row -> row.too(UserInfo.class))
                    .filter(UserInfo::isUp30)
                    .forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // <13
    public void line200w() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("200万注册.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rows).limit(100L).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 13~15
    public void line200wToObject() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("200万注册.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).map(row -> row.to(Regist.class)).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // < 13
    public void line200wTooObject() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("200万注册.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).map(row -> row.too(Regist.class)).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // < 30
    public void line1000w() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("1000万用户充值.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rows).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 32~34
    public void line1000wToObject() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("1000万用户充值.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).map(row -> row.to(Fill.class)).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 32~34
    public void line1000wTooObject() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("1000万用户充值.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).map(row -> row.too(Fill.class)).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void lineCount() {
        try (ExcelReader reader = ExcelReader.read(defaultPath.resolve("多表-自动宽度-转化-样式-测试 (1).xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
