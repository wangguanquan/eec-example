package net.cua._import;

import net.cua.excel.manager.Const;
import net.cua.excel.reader.ExcelReader;
import net.cua.excel.reader.Row;
import net.cua.excel.reader.Sheet;
import net.cua.excel.util.FileUtil;
import net.cua.excel.util.StringUtil;
import net.cua.other.Fill;
import net.cua.other.Regist;
import net.cua.other.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create by guanquan.wang at 2018-10-14
 */
@Service
public class ImportService {
    @Autowired
    private DataSource dataSource;
    @Value("${excel.storage.path}")
    private String path;
    // storage excel path
    private Path basePath;

    private void createBase() throws IOException {
        if (basePath == null) {
            basePath = Paths.get(path);
            if (!Files.exists(basePath)) {
                FileUtil.mkdir(basePath);
            }
        }
    }

    /**
     * 文件上传
     * @param file
     */
    public void upload(MultipartFile file) throws IOException {
        createBase();
        String fileName = file.getOriginalFilename();
        Path storage_path = basePath.resolve(fileName);
        // Storage file
        FileUtil.cp(file.getInputStream(), storage_path);
    }

    /**
     * 查看文件
     * @param name
     * @param sheet sheet index
     * @return
     */
    public String show(String name, int sheet) throws IOException {
        createBase();
        Path path = basePath.resolve(name);
        if (path == null || !Files.exists(path)) {
            return "文件[" + name + "]不存在.";
        }
        StringJoiner joiner = new StringJoiner(Const.lineSeparator);
        try (ExcelReader reader = ExcelReader.read(path)) {
            if (sheet != -1) {
                reader.sheet(sheet).rows().forEach(row -> joiner.add(row.toString()));
            } else {
                reader.sheets().flatMap(Sheet::rows).forEach(row -> joiner.add(row.toString()));
            }
        }
        return joiner.toString();
    }

    /**
     * 查看
     * @param select 要查看的结果字段
     * @param from 查看哪个Excel，也可以使用excel.sheet指定到具体的sheet
     * @param where 查询条件
     * @param order 排序
     * @param limit limit
     * @return
     */
    public String search(String select, String from, String where, String order, int limit) throws IOException {
        if (StringUtil.isEmpty(from)) {
            return "请指定Excel文件名或文件名.Sheet名，eq: from=学生成绩.xlsx 或 from=学生成绩.xlsx.三年级一班";
        }
        int name_index = from.indexOf(Const.Suffix.EXCEL_07);
        String name;
        if (name_index < 0) {
            name_index = from.indexOf('.');
            name = from.substring(0, name_index) + Const.Suffix.EXCEL_07;
        } else {
            name_index += Const.Suffix.EXCEL_07.length();
            name = from.substring(0, name_index);
        }
        Path path = basePath.resolve(name);
        if (!Files.exists(path)) {
            return "文件[" + name + "]不存在.";
        }
        String sheetName = name_index <= from.length() ? from.substring(name_index + 1) : null;

        StringJoiner joiner = new StringJoiner(Const.lineSeparator);
        try (ExcelReader reader = ExcelReader.read(path)) {
            Stream<Row> stream;

            List<String> headers = new ArrayList<>();
            if (sheetName == null) {
                stream = reader.sheets().flatMap(sheet -> {
                    Stream<Row> rows = sheet.rowsWithOutHeader();
                    headers.add(sheet.getHeader().toString());
                    return rows;
                });
            } else {
                Sheet sheet = reader.sheet(sheetName);
                if (sheet == null)
                    return "文件[" + name + "]中不存在名为[" + sheetName + "]的Sheet";
                stream = sheet.rowsWithOutHeader();
                headers.add(sheet.getHeader().toString());
            }

            // check header
            if (headers.size() > 1) {
                boolean equals = headers.get(0).equals(headers.get(1));
                for (int i = 2, len = headers.size(); equals && i < len; i++) {
                    equals = headers.get(i - 1).equals(headers.get(i));
                }
                if (!equals) {
                    return name + "存在多个多Sheet并且各Sheet头部信息不一致.";
                }
            }

            String[] headerName = headers.get(0).split("[|]");

            int[] indexs = null;
            if (StringUtil.isNotEmpty(select) && select.charAt(0) != '*') {
                String[] fileds = select.split(",");
                indexs = new int[fileds.length];
                for (int i = 0; i < fileds.length; i++) {
                    int n = StringUtil.indexOf(headerName, fileds[i]);
                    if (n < 0) return fileds[i] + "在" + name + "中不存在.";
                    indexs[i] = n;
                }
            }

            // filter where
            if (StringUtil.isNotEmpty(where)) {
                String[] wheres = where.split(",");
                int n;
                for (int i = 0; i < wheres.length; i++) {
                    String s = wheres[i];
                    String k = s.substring(0, n = s.indexOf('=')).trim(), v = s.substring(n + 1).trim();
                    n = StringUtil.indexOf(headerName, k);
                    if (n < 0) return k + "在" + name + "中不存在.";
                    final int _n = n;
                    stream.filter(row -> v.equals(row.getString(_n)));
                }
            }

            // order
            if (StringUtil.isNotEmpty(order)) {
                int n;
                String k = order.substring(0, n = order.indexOf(' ')).trim(), v = order.substring(n + 1).trim();
                final int index = StringUtil.indexOf(headerName, k);
                if ("asc".equalsIgnoreCase(v)) {
                    stream.sorted(Comparator.comparing(row -> row.getString(index)));
                } else {
                    stream.sorted((x, y) -> y.getString(index).compareTo(x.getString(index)));
                }
            }

            // limit
            if (limit > 0) {
                stream.limit(limit);
            }

            // select *
            if (indexs == null) {
                stream.forEach(row -> joiner.add(row.toString()));
            } else {
                StringBuilder buf = new StringBuilder();
                final int[] findexs = indexs;
                stream.forEach(row -> {
                    buf.delete(0, buf.length());
                    for (int n : findexs) buf.append(row.getString(n));
                    joiner.add(buf.toString());
                });
            }
        }
        return joiner.toString();
    }

    /**
     * 使用while遍历所有行
     */
    public void readerWhile() {
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("单表－单Sheet-固定宽度.xlsx"))) {
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
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("多表-自动宽度-转化-样式-测试 (1).xlsx"))) {
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
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("New name.xlsx"))) {
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
             ExcelReader reader = ExcelReader.read(basePath.resolve("200万-自动宽度-转化-测试 (1).xlsx"))) {
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
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("New name.xlsx"))) {
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
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("New name.xlsx"))) {
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
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("New name.xlsx"))) {
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
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("200万注册.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rows).limit(100L).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 13~15
    public void line200wToObject() {
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("200万注册.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).map(row -> row.to(Regist.class)).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // < 13
    public void line200wTooObject() {
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("200万注册.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).map(row -> row.too(Regist.class)).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // < 30
    public void line1000w() {
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("1000万用户充值.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rows).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 32~34
    public void line1000wToObject() {
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("1000万用户充值.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).map(row -> row.to(Fill.class)).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 32~34
    public void line1000wTooObject() {
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("1000万用户充值.xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).map(row -> row.too(Fill.class)).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void lineCount() {
        try (ExcelReader reader = ExcelReader.read(basePath.resolve("多表-自动宽度-转化-样式-测试 (1).xlsx"))) {
            long count = reader.sheets().flatMap(Sheet::rowsWithOutHeader).count();
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
