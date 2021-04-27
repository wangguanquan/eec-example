package cn.ttzero._import;

import org.ttzero.excel.manager.Const;
import org.ttzero.excel.reader.Drawings;
import org.ttzero.excel.reader.ExcelReader;
import org.ttzero.excel.reader.Row;
import org.ttzero.excel.reader.Sheet;
import org.ttzero.excel.util.FileUtil;
import org.ttzero.excel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

            for (Drawings.Picture pic : reader.listPictures()) {
                Files.copy(pic.getLocalPath(), Paths.get("/Users/wangguanquan/Documents/", pic.getLocalPath().getFileName().toString()));
            }

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
     * @param limit limit
     * @return
     */
    public String search(String select, String from, String where, int limit) throws IOException {
        if (StringUtil.isEmpty(from)) {
            return "请指定Excel文件名或文件名.Sheet名，eq: from=学生成绩.xlsx 或 from=学生成绩.xlsx.三年级一班";
        }
        createBase();
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
        String sheetName = name_index < from.length() ? from.substring(name_index + 1) : null;

        StringJoiner joiner = new StringJoiner(Const.lineSeparator);
        try (ExcelReader reader = ExcelReader.read(path)) {
            Stream<Row> stream;

            List<String> headers = new ArrayList<>();
            if (sheetName == null) {
                for (Sheet sheet : reader.all())
                    headers.add(sheet.getHeader().toString());
            } else {
                Sheet sheet = reader.sheet(sheetName);
                if (sheet == null)
                    return "文件[" + name + "]中不存在名为[" + sheetName + "]的Sheet";
                headers.add(sheet.getHeader().toString());
            }

            if (sheetName == null) {
                stream = reader.sheets().flatMap(Sheet::dataRows);
            } else {
                stream = reader.sheet(sheetName).dataRows();
            }

            if (headers.isEmpty()) {
                return "文件[" + name + "]内容为空.";
            }

            // check header
            if (headers.size() > 1) {
                boolean equals = headers.get(0).equals(headers.get(1));
                for (int i = 2, len = headers.size(); equals && i < len; i++) {
                    equals = headers.get(i - 1).equals(headers.get(i));
                }
                if (!equals) {
                    return name + "存在多个多Sheet并且各Sheet头部信息不一致.请指定具体的Sheet页再操作。eq: from=学生成绩.xlsx.三年级一班";
                }
            }

            String[] headerName = Arrays.stream(headers.get(0).split("[|]")).map(String::trim).toArray(String[]::new);

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
                    stream = stream.filter(row -> v.equals(row.getString(_n)));
                }
            }

            // order
            // 行数据是内存共享的，所以不能排序，内存中只保留一行数据

            // limit
            if (limit > 0) {
                stream = stream.limit(limit);
            }

            // select *
            if (indexs == null) {
                // append header
                joiner.add(headers.get(0));
                // append rows
                stream.forEach(row -> joiner.add(row.toString()));
            } else {
                // append header
                StringBuilder buf = new StringBuilder();
                buf.append(headerName[indexs[0]]);
                for (int i = 1; i < indexs.length; i++) {
                    buf.append(" | ").append(headerName[indexs[i]]);
                }
                joiner.add(buf.toString());
                // append rows
                final int[] findexs = indexs;
                stream.forEach(row -> {
                    buf.delete(0, buf.length());
                    buf.append(row.getString(findexs[0]));
                    for (int i = 1; i < findexs.length; i++) {
                        buf.append(" | ").append(row.getString(findexs[i]));
                    }
                    joiner.add(buf.toString());
                });
            }
            stream.close();
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
            for (Iterator<Row> iterator = sheet.dataIterator(); iterator.hasNext(); ) {
                row = iterator.next();
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

}
