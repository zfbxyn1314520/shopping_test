package com.shopping.view.app.action;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.shopping.foundation.domain.GoodsClass;
import com.shopping.foundation.service.IGoodsClassService;


/**
 *@param String id 商品类别的id 
 *@Param String level 商品类别的级别
 *商品分为3级 : level=null(不用输入任何参数)时,查询出第1级, level=0,id=*时,查询出第2级 
 *,level=1,id=*时,查询出第3级.
 */
@Controller
public class ApiNavViewAction {
	@Autowired
	private IGoodsClassService goodsClassService;
		
	//左边的导航视图
				@RequestMapping(value="/api/navView.htm",produces = "text/html;charset=UTF-8")
				@ResponseBody
				public String navViewFour(HttpServletRequest request,HttpServletResponse response,String id,String level){
					if(level==null){
						Map<String, Object> params = new HashMap<String, Object>();
						params.put( "display", Boolean.valueOf( true ) );
						List<GoodsClass> gcs = this.goodsClassService.query( "select obj from GoodsClass obj where obj.parent.id is null and obj.display=:display order by obj.sequence asc", params, 0, 14 );
						//List<GoodsClass> childs = ((GoodsClass)gcs.get(0)).getChilds();
						List list = new ArrayList();
						for(GoodsClass goodsclass : gcs){	
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("id", goodsclass.getId());
							map.put("className", goodsclass.getClassName());
							map.put("level", goodsclass.getLevel());
							map.put("icon_sys","resources/style/common/images/icon/icon_"+goodsclass.getIcon_sys()+".png");
							/*list.add(goodsclass.getId());
							list.add(goodsclass.getClassName());
							list.add(goodsclass.getLevel());
							list.add(goodsclass.getIcon_acc());
							list.add(goodsclass.getIcon_sys());*/
							list.add(map);
						}
					    // JsonConfig jsonConfig = new JsonConfig();
						if(list!=null && list.size()>0){
							String data = Json.toJson(list, JsonFormat.compact());
							//JSONObject data = JSONObject.fromObject(map);
							//JSONObject data = JSONObject.fromObject(list);
							//JSONArray data = JSONArray.fromObject(list, jsonConfig);
							//String data = JSON.toJSONString(list);
							//String data = JSONArray.toJSONString(list);
							return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":"+data+"}";
						}else{
							return "{\"statusCode\":500,\"msg\":\"系统错误！\"}";
						}
					}else if(level.equals("0")){
						Map params = new HashMap();
						params.put("id", Long.valueOf(Long.parseLong(id)));
						List<GoodsClass> childsOne  = this.goodsClassService.query("select obj from GoodsClass obj where obj.id=:id", params, 0, 14);
						List list = new ArrayList();
						for(GoodsClass goodsClass : childsOne.get(0).getChilds()){
							Map<String,Object> map = new HashMap<String,Object>();
							map.put("id", goodsClass.getId());
							map.put("className", goodsClass.getClassName());
							map.put("level", goodsClass.getLevel());
							list.add(map);
						}
						if(list!=null && list.size()>0){
							String data = Json.toJson(list, JsonFormat.compact());
							return "{\"statusCode\":201,\"msg\":\"加载成功!\",\"data\":"+data+"}";
						}else{
							return "{\"statusCode\":501,\"msg\":\"系统错误！\"}";
						}
					}else if(level.equals("1")){
						Map params = new HashMap();
						params.put("id", Long.valueOf(Long.parseLong(id)));
						List<GoodsClass> childsOne  = this.goodsClassService.query("select obj from GoodsClass obj where obj.id=:id", params, 0, 14);
						List<GoodsClass> childsTwo = childsOne.get(0).getChilds();
						List list = new ArrayList();
						for(GoodsClass goodsClass : childsTwo){
							Map<String,Object> map = new HashMap<String,Object>();
							map.put("id", goodsClass.getId());
							map.put("className", goodsClass.getClassName());
							map.put("level", goodsClass.getLevel());
							list.add(map);
						}
						if(list!=null && list.size()>0){
							String data = Json.toJson(list, JsonFormat.compact());
							return "{\"statusCode\":202,\"msg\":\"加载成功!\",\"data\":"+data+"}";
						}else{
							return "{\"statusCode\":502,\"msg\":\"系统错误！\"}";
						}
					}else{
						return "{\"statusCode\":503,\"msg\":\"查询到底层了！\"}";
					}
				}
	
}
