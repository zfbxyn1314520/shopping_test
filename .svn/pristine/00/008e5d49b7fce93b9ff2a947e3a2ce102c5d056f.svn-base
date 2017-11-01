package com.shopping.view.app.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.shopping.core.domain.virtual.SysMap;
import com.shopping.core.query.support.IPageList;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.query.StoreQueryObject;
import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;

@Controller
public class ApiNearStoreAction {

    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IAreaService areaService;
    @Autowired
    private IStoreService storeService;

    /**
     * 附近店铺列表
     *
     * @param request
     * @param response
     * @param currentPage 当前页
     * @param pageSize    每页显示几条
     * @return
     */
/*    @RequestMapping({"/api/userNearStore.htm"})
    @ResponseBody
    public String UserNearStore(HttpServletRequest request, HttpServletResponse response,
                                String currentPage, String pageSize) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        Area area = this.areaService.getObjById(CommUtil.null2Long(user.getArea().getId()));
        String area_name = area.getParent().getParent().getAreaName() + " " + area.getParent().getAreaName() + " " + area.getAreaName() + " ";

        Map params = new HashMap();
        params.put("area_id", user.getArea().getId());
        List<Store> stores = this.storeService.query("select obj from Store obj where obj.area.id=:area_id", params, -1, -1);

        String url = CommUtil.getURL(request);
        if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
            url = this.configService.getSysConfig().getImageWebServer();
        }
        List list = new ArrayList();
        Map<String, Object> map = new HashMap<String, Object>();
        StoreQueryObject sqo = new StoreQueryObject(currentPage, map, null, null);
        sqo.addQuery("obj.area.id", new SysMap("area_id", user.getArea().getId()), "=");
        sqo.setPageSize(Integer.valueOf(CommUtil.null2Int(pageSize)));
        IPageList pList = storeService.list(sqo);
        if (pList.getResult() != null) {
            List<Store> store_list = pList.getResult();
            if (store_list.size() > 0) {
                for (Store store : store_list) {
                    Map map2 = new HashMap();
                    map2.put("store_id", store.getId());
                    map2.put("store_name", store.getStore_name());
                    map2.put("store_info", store.getStore_info());
                    map2.put("store_address", area_name + store.getStore_address());
                    map2.put("store_tel", store.getStore_telephone());

                    String logo = url + "/" + this.configService.getSysConfig().getStoreImage().getPath() + "/"
                            + this.configService.getSysConfig().getStoreImage().getName();
                    if (store.getStore_logo() != null) {
                        logo = url + "/" + store.getStore_logo().getPath() + "/"
                                + store.getStore_logo().getName();
                    }
                    map2.put("logo", logo);
                    list.add(map2);
                }
            }
            String data = JSON.toJSONString(list);
            return "{\"statusCode\":200,\"rowCount\":" + stores.size() + ",\"msg\":\"数据加载成功!\",\"data\":" + data + "}";
        } else {
            return "{\"statusCode\":500,\"msg\":\"数据加载失败!\"}";
        }
    }*/
    
    
    
    /**
     * 附近店铺
     * @param request
     * @param response
     * @param longitude		经度
     * @param latitude			维度
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping({"/api/userNearStore.htm"})
    @ResponseBody
    public String UserNearStore_test(HttpServletRequest request, HttpServletResponse response,
          String longitude,String latitude,String currentPage, String pageSize) {
    	
    	double r = 6371;
    	double dis = 5;
    	double dlng =  2*Math.asin(Math.sin(dis/(2*r))/Math.cos(CommUtil.null2Double(latitude)*Math.PI/180)); 
    	dlng = dlng*180/Math.PI;
    	double dlat = dis/r;  
        dlat = dlat*180/Math.PI;          
        double minlat = CommUtil.null2Double(latitude)-dlat;  
        double maxlat = CommUtil.null2Double(latitude)+dlat;  
        double minlng = CommUtil.null2Double(longitude) -dlng;  
        double maxlng = CommUtil.null2Double(longitude) + dlng;  
    	
        int x = CommUtil.null2Int(currentPage);
        int y = CommUtil.null2Int(pageSize);
        int size;
        int pages=0;
        
        
        String url = CommUtil.getURL(request);
        if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
            url = this.configService.getSysConfig().getImageWebServer();
        }
        List list = new ArrayList();
        Map params = new HashMap();
        params.clear();
        params.put("minlng", BigDecimal.valueOf(minlng));
        params.put("maxlng", BigDecimal.valueOf(maxlng));
        params.put("minlat", BigDecimal.valueOf(minlat));
        params.put("maxlat", BigDecimal.valueOf(maxlat));
        List<Store> store_list = this.storeService.query("select obj from Store obj where 1=1 and obj.store_lng>=:minlng and obj.store_lng<=:maxlng "
        		+ "and obj.store_lat>=:minlat and obj.store_lat<=:maxlat",
        		params, -1, -1);
        
        size = store_list.size();
        if(size>0){
        	pages = size / y;
        	if(size % y != 0){
        		pages++;
        	}
        	int begin = (x - 1) * y;
        	int end = x * y;
        	if(begin < size){
        		if(end > size){
        			end = size;
        		}
                     for (int i=begin;i<end;i++) {
                         Map map2 = new HashMap();
                         String area_name = store_list.get(i).getArea().getParent().getParent().getAreaName() + " " + store_list.get(i).getArea().getParent().getAreaName() + " " + store_list.get(i).getArea().getAreaName() + " ";
                         map2.put("store_id", store_list.get(i).getId());
                         map2.put("store_name", store_list.get(i).getStore_name());
                         map2.put("store_info", store_list.get(i).getStore_info());
                         map2.put("store_address", area_name + store_list.get(i).getStore_address());
                         map2.put("store_tel", store_list.get(i).getStore_telephone());
                         String logo = url + "/" + this.configService.getSysConfig().getStoreImage().getPath() + "/"
                                 + this.configService.getSysConfig().getStoreImage().getName();
                         if (store_list.get(i).getStore_logo() != null) {
                             logo = url + "/" + store_list.get(i).getStore_logo().getPath() + "/"
                                     + store_list.get(i).getStore_logo().getName();
                         }
                         map2.put("logo", logo);
                         list.add(map2);
                     }
        	}
        }
           if(list.size()>0 && list!=null){
        	   return "{\"statusCode\":200,\"rowCount\":" + size + ",\"msg\":\"数据加载成功!\",\"data\":" + JSON.toJSONString(list).toString() + "}";
           }else{
        	   return "{\"statusCode\":201,\"msg\":\"抱歉 , 该附近没有店铺 ! \"}";
           }
    }
    


}
