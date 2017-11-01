package com.shopping.view.app.action;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.shopping.foundation.domain.StoreClass;
import com.shopping.foundation.service.IStoreClassService;

@Controller
public class ApiStoreClassAction {

    @Autowired
    private IStoreClassService storeClassService;

    @RequestMapping(value = "/api/loadLevelOne.htm", produces = "text/html;charset=UTF-8")
    @ResponseBody
    /**
     * 一级商品店铺分类
     * storeClassList  :  一级商品店铺的集合
     * @param response
     */
    public String firstLevleClass(HttpServletResponse response) {
        List<StoreClass> storeClassList =
                this.storeClassService.query(
                        "select obj from StoreClass obj where obj.parent.id is null", null, -1, -1);
        response.setContentType("text/json");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        if (storeClassList != null && storeClassList.size() > 0) {
            String data = Json.toJson(storeClassList, JsonFormat.compact());
            return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + data + "}";
        } else {
            return "{\"statusCode\":500,\"msg\":\"系统错误！\"}";
        }
    }

    @RequestMapping(value = "/api/loadLevelTwo.htm", produces = "text/html;charset=UTF-8")
    @ResponseBody
    /**
     * 二级商品店铺分类
     * @param request
     * @param response
     * @param pid    父分类的id
     */
    public String TwoLevelClass(HttpServletResponse response, String pid) {
        Map params = new HashMap();
        params.put("pid", Long.valueOf(Long.parseLong(pid)));
        List<StoreClass> storeClasses = this.storeClassService.query(
                "select obj from StoreClass obj where obj.parent.id=:pid", params, -1, -1);
        response.setContentType("text/json");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        if (storeClasses != null && storeClasses.size() > 0) {
            String data = JSON.toJSONString(storeClasses);
            return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + data + "}";
        } else {
            return "{\"statusCode\":500,\"msg\":\"系统错误！\"}";
        }
    }

}
