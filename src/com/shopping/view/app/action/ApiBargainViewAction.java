package com.shopping.view.app.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.BargainGoods;
import com.shopping.foundation.service.IBargainGoodsService;
import com.shopping.foundation.service.ISysConfigService;

@Controller
public class ApiBargainViewAction {

	@Autowired
	private IBargainGoodsService bargainGoodsService;
	@Autowired
	private ISysConfigService configService;

	
	/**
	 * 特价商品的列表
	 * @param request
	 * @param response
	 * @param bg_time	特价活动的时间
	 * @param currentPage	当前页
	 * @param pageSize			每页显示几条数据
	 * @return
	 */
	@RequestMapping({"/api/bargainGoods.htm"})
	@ResponseBody
	public String bargainGoods(HttpServletRequest request,HttpServletResponse response,
			String bg_time,String currentPage,String pageSize){
		
		int x = CommUtil.null2Int(currentPage);
		int y = CommUtil.null2Int(pageSize);
		int size;
		int pages=0;
		List list = new ArrayList();
		
		String url =CommUtil.getURL(request);
		if(!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))){
			url=this.configService.getSysConfig().getImageWebServer();
		}
		 Map params = new HashMap();
	     Calendar cal = Calendar.getInstance();
	     if (CommUtil.null2String(bg_time).equals("")) {
	       bg_time = CommUtil.formatShortDate(new Date());
	     }
	     cal.setTime(CommUtil.formatDate(bg_time));
	    // cal.add(6, 1);这个方法的作用是在原来的基础上增加1天
	     params.put("bg_time", 
	       CommUtil.formatDate(CommUtil.formatShortDate(cal.getTime())));
	     params.put("bg_status", Integer.valueOf(1));
	     List<BargainGoods> bgs = this.bargainGoodsService
	       .query("select obj from BargainGoods obj where obj.bg_time=:bg_time and obj.bg_status=:bg_status order by audit_time desc", 
	       params, -1, -1);
	     size = bgs.size();
	     if(size > 0){
	    	 pages = size / y;
	    	 if(size % y != 0){
	    		 pages++;
	    	 }
	    	 int begin = (x-1) * y;
	    	 int end = x * y;
	    	 if(begin<size){
	    		 if(end>size){
	    			 end = size;
	    		 }
	    		 for(int i=begin;i<end;i++){
	 				Map map = new HashMap();
	 				String image = url+"/"+this.configService.getSysConfig().getGoodsImage().getPath()+"/"
	 						+this.configService.getSysConfig().getGoodsImage().getName();
	 				if(bgs.get(i).getBg_goods().getGoods_main_photo()!=null){
	 					image = url+"/"+bgs.get(i).getBg_goods().getGoods_main_photo().getPath()+"/"
	 							+bgs.get(i).getBg_goods().getGoods_main_photo().getName();
	 				}
	 				map.put("image", image);
	 				map.put("goods_id", bgs.get(i).getBg_goods().getId());
	 				map.put("goods_name", bgs.get(i).getBg_goods().getGoods_name());
	 				map.put("old_price", bgs.get(i).getBg_goods().getGoods_price());
	 				map.put("store_price", bgs.get(i).getBg_goods().getStore_price());
	 				list.add(map);
	 			}
	    	 }
	     }
			if(list!=null && list.size()>0){
				String data = JSON.toJSONString(list);
				return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":"+data+",\"pages\":"+pages+","
						+ "\"currentPgae\":"+x+",\"rowCount\":"+size+"}";
			}else{
				return "{\"statusCode\":500,\"msg\":\"今天没有特价商品!\"}";
			}
	}
	
	
	
}
