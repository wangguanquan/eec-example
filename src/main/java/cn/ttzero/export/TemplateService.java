package cn.ttzero.export;

import org.ttzero.excel.entity.Sheet;
import org.ttzero.excel.entity.Workbook;
import org.ttzero.excel.entity.style.Styles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by guanquan.wang at 2018-10-13 16:40
 */
@Service
public class TemplateService {
    @Value("${excel.creator}")
    private String creator;
    @Value("${excel.company}")
    private String company;
    @Value("${excel.storage.path}")
    private String path;

    /**
     * 创建模版
     */
    public void createTemplate() {
        List<Map<String, Object>> mapData = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("title", "${name } 同学，在本次期末考试的成绩是 ${score}。\n" +
                "                            ${date }");
        mapData.add(map);

        try {
            new Workbook("template", creator)
                // head columns 决定导出顺序
                .addSheet("Student info", mapData
                    , new Sheet.Column("通知书", "title").setCellStyle(Styles.defaultStringStyle())
                ).writeTo(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    Path tempPath = Paths.get(path, "template.xlsx");
    private boolean checkTempOrCreate() {
        if (!Files.exists(tempPath)) {
            createTemplate();
        }
        return true;
    }

    /**
     * 导出
     */
    public void mapTemp(Map<String, ?> map, OutputStream os) {
        checkTempOrCreate();
        try (InputStream is = Files.newInputStream(tempPath)) {
            new Workbook("Map模板导出", creator).withTemplate(is, map).writeTo(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 导出
     */
    public void objectTemp(BindEntity entity, OutputStream os) {
        checkTempOrCreate();
        try (InputStream is = Files.newInputStream(tempPath)) {
            new Workbook("Object模板导出", creator).withTemplate(is, entity).writeTo(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class BindEntity {
        private String name;
        private int score;
        private Timestamp date;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public Timestamp getDate() {
            return date;
        }

        public void setDate(Timestamp date) {
            this.date = date;
        }
    }
}
