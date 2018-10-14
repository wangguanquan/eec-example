package net.cua.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Excel Export controller
 * Create by guanquan.wang at 2018-10-13 14:01
 */
@RestController
@RequestMapping("/export")
public class Controller {
    @Autowired
    ExportService service;
    @Autowired
    TemplateService template;

    /**
     * 指定行导出
     * @param limit limit
     * @return
     */
    @RequestMapping("/{limit}")
    public void limit(@PathVariable int limit, HttpServletResponse response) throws IOException {
        String fileName = java.net.URLEncoder.encode("用户充值" + limit + ".xlsx", "UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=utf-8''" + fileName);
        service.fill(limit, response.getOutputStream());
    }

    /**
     * 重置隔行变色
     * @param response
     * @throws IOException
     */
    @RequestMapping("/resetOddColor")
    public void resetOddColor(HttpServletResponse response) throws IOException {
        String fileName = java.net.URLEncoder.encode("重置隔行颜色.xlsx", "UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=utf-8''" + fileName);
        service.changeOddStyle(response.getOutputStream());
    }

    /**
     * 取消隔行变色
     * @param response
     * @throws IOException
     */
    @RequestMapping("/cancelOddStyle")
    public void cancelOddStyle(HttpServletResponse response) throws IOException {
        String fileName =  java.net.URLEncoder.encode("取消隔行颜色.xlsx", "UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=utf-8''" + fileName);
        service.cancelOddStyle(response.getOutputStream());
    }

    /**
     * 对象数组 & Map数组 导出
     * @param response
     * @throws IOException
     */
    @RequestMapping("/listObj")
    public void listObj(HttpServletResponse response) throws IOException {
        String fileName =  java.net.URLEncoder.encode("对象数组 & Map数组.xlsx", "UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=utf-8''" + fileName);
        service.objectAndMapSheet(response.getOutputStream());
    }

    /**
     * Map数组 导出
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/mapTemp", method = RequestMethod.POST)
    public void mapTemp(@RequestBody Map<String, ?> map, HttpServletResponse response) throws IOException {
        String fileName =  java.net.URLEncoder.encode("Map数组模板.xlsx", "UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=utf-8''" + fileName);
        template.mapTemp(map, response.getOutputStream());
    }

    /**
     * Map数组 导出
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/objectTemp", method = RequestMethod.POST)
    public void objectTemp(@RequestBody TemplateService.BindEntity entity, HttpServletResponse response) throws IOException {
        String fileName =  java.net.URLEncoder.encode("Object数组模板.xlsx", "UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=utf-8''" + fileName);
        template.objectTemp(entity, response.getOutputStream());
    }
}
