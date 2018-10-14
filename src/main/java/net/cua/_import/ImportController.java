package net.cua._import;

import net.cua.excel.manager.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Excel Reader Controller
 * use curl -F "file-@{excel name}" "/upload" for upload
 * use curl "/show/{excel name}" for show excel data
 * Create by guanquan.wang at 2018-10-13
 */
@RestController
public class ImportController {

    @Autowired
    private ImportService service;

    /**
     * curl -F "file=@./1.xlsx"  http://localhost:8080/import/upload
     * Excel上传
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload")
    public String upload(@RequestBody MultipartFile file) throws IOException {
        service.upload(file);
        return "OK!" + Const.lineSeparator;
    }

    /**
     * curl "http://localhost:8080/show/1.xlsx"
     * 查看excel文件内容
     * @return
     */
    @RequestMapping("/show/{name}")
    public String show(@PathVariable String name, @RequestParam(value = "sheet", defaultValue = "-1") int sheet) throws IOException {
        if (!name.endsWith(Const.Suffix.EXCEL_07)) {
            name += Const.Suffix.EXCEL_07;
        }
        return service.show(name, sheet) + Const.lineSeparator;
    }

    /**
     *
     * 数据查询
     * @param select 要查看的结果字段
     * @param from 查看哪个Excel，也可以使用excel.sheet指定到具体的sheet
     * @param where 查询条件
     * @param order 排序
     * @param limit limit
     * @return
     */
    @RequestMapping("/search")
    public String search(@RequestParam(value = "select", required = false) String select
            , @RequestParam("from") String from
            , @RequestParam(value = "where", required = false) String where
            , @RequestParam(value = "order", required = false) String order
            , @RequestParam(value = "limit", required = false) int limit
    ) throws IOException {
        return service.search(select, from, where, order, limit) + Const.lineSeparator;
    }
}
