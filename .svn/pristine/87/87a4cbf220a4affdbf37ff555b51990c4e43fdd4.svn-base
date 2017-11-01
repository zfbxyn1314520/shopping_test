 package com.shopping.view.web.action;
 
 import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.StoreClass;
 import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.IStoreClassService;

 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.nutz.json.Json;
 import org.nutz.json.JsonFormat;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class LoadAction
 {
 
   @Autowired
   private IAreaService areaService;
   
   @Autowired
   private IStoreClassService storeClassService;
 
   @RequestMapping({"/load_area.htm"})
   public void load_area(HttpServletRequest request, HttpServletResponse response, String pid)
   {
     Map params = new HashMap();
     params.put("pid", Long.valueOf(Long.parseLong(pid)));
     List<Area> areas = this.areaService.query(
       "select obj from Area obj where obj.parent.id=:pid", params, 
       -1, -1);
     List list = new ArrayList();
     for (Area area : areas) {
       Map map = new HashMap();
       map.put("id", area.getId());
       map.put("areaName", area.getAreaName());
       list.add(map);
     }
     String temp = Json.toJson(list, JsonFormat.compact());
     response.setContentType("text/plain");
     response.setHeader("Cache-Control", "no-cache");
     response.setCharacterEncoding("UTF-8");
     try
     {
       PrintWriter writer = response.getWriter();
       writer.print(temp);
     }
     catch (IOException e) {
       e.printStackTrace();
     }
   }
   
   @RequestMapping({"/load_area2.htm"})
   public void load_storeClass(HttpServletRequest request, HttpServletResponse response, String pid)
   {
     Map params = new HashMap();
     params.put("pid", Long.valueOf(Long.parseLong(pid)));
     List<StoreClass> storeClasses = this.storeClassService.query(
       "select obj from StoreClass obj where obj.parent.id=:pid", params, 
       -1, -1);
     List list = new ArrayList();
     for (StoreClass storeClass : storeClasses) {
       Map map = new HashMap();
       map.put("id", storeClass.getId());
       map.put("className", storeClass.getClassName());
       list.add(map);
     }
     String temp2 = Json.toJson(list, JsonFormat.compact());
     response.setContentType("text/plain");
     response.setHeader("Cache-Control", "no-cache");
     response.setCharacterEncoding("UTF-8");
     try
     {
       PrintWriter writer = response.getWriter();
       writer.print(temp2);
     }
     catch (IOException e) {
       e.printStackTrace();
     }
   }
   
   
 }


 
 
 