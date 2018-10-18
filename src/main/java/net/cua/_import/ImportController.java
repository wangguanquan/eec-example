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
        return service.show(name, sheet);
    }

    /**
     * eq: curl
     * -d "from=Map%26Object.xlsx.Object%E6%B5%8B%E8%AF%95"
     * -d "where=%E6%B8%B8%E6%88%8F%3DLOL"
     * -d "limit=5"
     * -d "select=%E6%B8%B8%E6%88%8F%2Caccount%2C%E6%B3%A8%E5%86%8C%E6%97%B6%E9%97%B4%2C%E6%98%AF%E5%90%A6%E6%BB%A130%E7%BA%A7"
     * "http://localhost:8080/search"
     * 数据查询
     * @param select 要查看的结果字段
     * @param from 查看哪个Excel，也可以使用excel.sheet指定到具体的sheet
     * @param where 查询条件
     * @param limit limit
     * @return
     */
    @RequestMapping("/search")
    public String search(@RequestParam(value = "select", required = false) String select
            , @RequestParam("from") String from
            , @RequestParam(value = "where", required = false) String where
            , @RequestParam(value = "limit", defaultValue = "-1") int limit
    ) throws IOException {
        return service.search(select, from, where, limit);
    }

}
