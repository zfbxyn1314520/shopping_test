package com.shopping.view.app.action;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.shopping.core.domain.virtual.SysMap;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.query.support.IPageList;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreClass;
import com.shopping.foundation.domain.query.StoreQueryObject;
import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IStoreClassService;
import com.shopping.foundation.service.IStoreGradeService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.lucene.LuceneResult;
import com.shopping.lucene.LuceneUtil;
import com.shopping.lucene.LuceneVo;
import com.shopping.view.web.tools.StoreViewTools;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class ApiLuceneViewAction {
	
	 @Autowired
	   private ISysConfigService configService;

	   @Autowired
	   private IUserConfigService userConfigService;

	   @Autowired
	   private IStoreService storeService;

	   @Autowired
	   private IStoreClassService storeClassService;

	   @Autowired
	   private IGoodsService goodsService;

	   @Autowired
	   private StoreViewTools storeViewTools;

	   @Autowired
	   private IStoreGradeService storeGradeService;

	   @Autowired
	   private IAreaService areaService;
	   
	   
	   /**
	    * 店铺，商品的搜索功能
	    * @param request
	    * @param response
	    * @param type
	    * @param keyword
	    * @param currentPage
	    * @param orderBy	:	包含:		默认，收藏（favorite_count），信用（store_credit）
	    * @param orderType
	    * @param store_price_begin
	    * @param store_price_end
	    * @param view_type
	    * @param sc_id
	    * @param storeGrade_id
	    * @param checkbox_id	: card_approve(实名认证 1：是，0：否)	,realstore_approve(实体认证 1：是，0：否)
	    * 										: store_recommend(商城推荐 1:是，0：否)
	    * @param storepoint	:评分
	    * @param area_id
	    * @param area_name
	    * @param goods_view
	    * @return
	    */
	   @RequestMapping({"/api/search.htm"})
	   @ResponseBody
	   public String search(HttpServletRequest request, HttpServletResponse response, String type, String keyword, String currentPage, String orderBy, String orderType, String store_price_begin, String store_price_end, String view_type, String sc_id, String storeGrade_id, String checkbox_id, String storepoint, String area_id, String area_name, String goods_view) {
		   
		   Map map_list = new HashMap();
		   
		   ModelAndView mv = new JModelAndView("search_goods_list.html",
			       this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response);
			     if ((type == null) || (type.equals("")))
			       type = "goods";
			     keyword = CommUtil.decode(keyword);

			     String shopping_view_type = CommUtil.null2String(request.getSession(false).getAttribute("shopping_view_type"));

				 if ((shopping_view_type != null) && (!shopping_view_type.equals("")) && (shopping_view_type.equals("wap"))) {
					 mv = new JModelAndView("wap/search.html",
						       this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response);
				 }

			     if (type.equals("store")) {
			       mv = new JModelAndView("store_list.html", this.configService.getSysConfig(),
			         this.userConfigService.getUserConfig(), 1, request, response);

			       if ((shopping_view_type != null) && (!shopping_view_type.equals("")) && (shopping_view_type.equals("wap"))) {
			  		 mv = new JModelAndView("wap/store_list.html",
			  			       this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response);
			  	   }
			       StoreQueryObject sqo = new StoreQueryObject(currentPage, mv, "addTime", "desc");
			       if ((keyword != null) && (!keyword.equals(""))) {
			         sqo.addQuery("obj.store_name", new SysMap("store_name", "%" + keyword + "%"), "like");
			         mv.addObject("store_name", keyword);
			       }
			       if ((sc_id != null) && (!sc_id.equals(""))) {
			         StoreClass storeclass = this.storeClassService.getObjById(CommUtil.null2Long(sc_id));
			         Set ids = getStoreClassChildIds(storeclass);
			         Map map = new HashMap();
			         map.put("ids", ids);
			         sqo.addQuery("obj.sc.id in (:ids)", map);
			         mv.addObject("sc_id", sc_id);
			       }
			       if ((storeGrade_id != null) && (!storeGrade_id.equals(""))) {
			         sqo.addQuery("obj.grade.id", new SysMap("grade_id", CommUtil.null2Long(storeGrade_id)), "=");
			         mv.addObject("storeGrade_id", storeGrade_id);
			       }
			       if ((orderBy != null) && (!orderBy.equals(""))) {
			         sqo.setOrderBy(orderBy);
			         if (orderBy.equals("addTime"))
			           orderType = "asc";
			         else {
			           orderType = "desc";
			         }
			         sqo.setOrderType(orderType);
			         mv.addObject("orderBy", orderBy);
			         mv.addObject("orderType", orderType);
			       }
			       if ((checkbox_id != null) && (!checkbox_id.equals(""))) {
			         sqo.addQuery("obj." + checkbox_id, new SysMap("obj_checkbox_id", Boolean.valueOf(true)), "=");
			         mv.addObject("checkbox_id", checkbox_id);
			       }
			       if ((storepoint != null) && (!storepoint.equals(""))) {
			         sqo.addQuery("obj.sp.store_evaluate1", new SysMap("sp_store_evaluate1", new BigDecimal(storepoint)), ">=");
			         mv.addObject("storepoint", storepoint);
			       }
			       if ((area_id != null) && (!area_id.equals(""))) {
			         mv.addObject("area_id", area_id);
			         Area area = this.areaService.getObjById(CommUtil.null2Long(area_id));
			         Set area_ids = getAreaChildIds(area);
			         Map params = new HashMap();
			         params.put("ids", area_ids);
			         sqo.addQuery("obj.area.id in (:ids)", params);
			       }
			       if ((area_name != null) && (!area_name.equals(""))) {
			         mv.addObject("area_name", area_name);
			         sqo.addQuery("obj.area.areaName", new SysMap("areaName", "%" + area_name.trim() + "%"), "like");
			         sqo.addQuery("obj.area.parent.areaName", new SysMap("areaName", "%" + area_name.trim() + "%"), "like", "or");
			         sqo.addQuery("obj.area.parent.parent.areaName", new SysMap("areaName", "%" + area_name.trim() + "%"), "like", "or");
			       }
			       sqo.addQuery("obj.store_status", new SysMap("store_status", Integer.valueOf(2)), "=");
			       sqo.setPageSize(Integer.valueOf(20));
			       
			       IPageList pList = this.storeService.list(sqo);
			       CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
			       
			       System.out.println("result:"+pList.getResult());
			       System.out.println("type:"+type+",keyword:"+keyword+",currentPage:"+currentPage+",orderBy:"+orderBy+",orderType:"+orderType+",store_price_begin:"+store_price_begin+",store_price_end:"+store_price_end+",view_type:"+view_type+",sc_id:"
			    		   +sc_id+",storeGrade_id:"+storeGrade_id+",checkbox_id:"+checkbox_id+",storepoint:"+storepoint+",area_id:"+area_id+",area_name:"+area_name+",goods_view:"+goods_view);
			       List<Store> stores = pList.getResult();
			       List s_list = new ArrayList();
			       for(Store store : stores) {
			    	   Map s_map = new HashMap();
			    	   s_map.put("store_id", store.getId());
			    	   s_map.put("store_name", store.getStore_name());
			    	   s_map.put("store_owner", store.getStore_ower());
			    	   s_map.put("store_class", store.getSc().getClassName());
			    	   s_map.put("store_img", CommUtil.getURL(request)+"/"+this.configService.getSysConfig().getStoreImage().getPath()+"/"+
			    	   this.configService.getSysConfig().getStoreImage().getName());
			    	   s_map.put("store_credit", store.getStore_credit());
			    	   s_map.put("store_goods_size", store.getGoods_list().size());
			    	   s_map.put("store_evaluate", store.getPoint().getStore_evaluate1());
			    	   s_map.put("store_description_evaluate", store.getPoint().getDescription_evaluate());
			    	   s_map.put("store_service_evaluate", store.getPoint().getService_evaluate());
			    	   s_map.put("store_ship_evaluate", store.getPoint().getShip_evaluate());
			    	   s_map.put("stroe_area", store.getArea().getParent().getParent().getAreaName()+" "+store.getArea().getParent().getAreaName()+" "+
			    			   store.getArea().getAreaName());
			    	   s_list.add(s_map);
			    	   //map_list.put("store", s_list);	取消店铺搜索
			       }
			     }
			     
			     List g_list = new ArrayList();
			     LuceneResult pList = null;
			     if ((type.equals("goods")) && (!CommUtil.null2String(keyword).equals(""))) {
			         String path = System.getProperty("user.dir") + File.separator + "luence" + File.separator + "goods";
			         LuceneUtil lucene = LuceneUtil.instance();
			         LuceneUtil.setIndex_path(path);
			         boolean order_type = true;
			         String order_by = "";
			         if (CommUtil.null2String(orderType).equals("asc")) {
			           order_type = false;
			         }
			         if (CommUtil.null2String(orderType).equals("")) {
			           orderType = "desc";
			         }
			         if (CommUtil.null2String(orderBy).equals("store_price")) {
			           order_by = "store_price";
			         }
			         if (CommUtil.null2String(orderBy).equals("goods_salenum")) {
			           order_by = "goods_salenum";
			         }
			         if (CommUtil.null2String(orderBy).equals("goods_collect")) {
			           order_by = "goods_collect";
			         }
			         if (CommUtil.null2String(orderBy).equals("goods_addTime")) {
			           order_by = "addTime";
			         }
			         Sort sort = null;
			         if (!CommUtil.null2String(order_by).equals("")) {
			           sort = new Sort(new SortField(order_by, 7, order_type));
			         }
			         	pList = lucene.search(keyword,
			           CommUtil.null2Int(currentPage),
			           CommUtil.null2Int(store_price_begin),
			           CommUtil.null2Int(store_price_end), null, sort);
			         for (LuceneVo vo : pList.getVo_list()) {
			           Goods goods = this.goodsService.getObjById(vo.getVo_id());
			           Map g_map = new HashMap();
			           g_map.put("goods_id", goods.getId());
			           g_map.put("goods_name", goods.getGoods_name());
			           g_map.put("old_price", goods.getGoods_price());
			           g_map.put("goods_storePrice", goods.getStore_price());
			           g_map.put("img", CommUtil.getURL(request)+"/"+goods.getGoods_main_photo().getPath()+"/"
			        		   +goods.getGoods_main_photo().getName());    
			           g_map.put("goods_salenum", goods.getGoods_salenum());
			           g_map.put("goods_evaluate", goods.getDescription_evaluate());
			           g_map.put("store_name", goods.getGoods_store().getStore_name());
			           g_map.put("total_evaluate", goods.getEvaluates().size());
			           g_map.put("store_grade", goods.getGoods_store().getGrade().getGradeName());
			           g_list.add(g_map);
			         //  map_list.put("goods", g_list);
			         }
			     }
		   if(g_list.size()>0 && g_list!=null) {
			   return "{\"statusCode\":200,\"msg\":\"查询成功!\",\"data\":"+JSONArray.fromObject(g_list).toString()+",\"totalPages\":"+pList.getPages()+"}";			   
		   }else {
			   return "{\"statusCode\":300,\"msg\":\"对不起，没有对应的数据!\",\"data\":"+JSONArray.fromObject(map_list).toString()+"}";
		   }
	   }
	   
	   /**
	    * 热搜词语的显示
	    * @return
	    */
	   @RequestMapping({"/api/hotSearch.htm"})
	   @ResponseBody
	   public String getHotSearch() {
		   String hotSearchs = this.configService.getSysConfig().getHotSearch();
		   String[] hotSearch = hotSearchs.split(",");
//		   List list = new ArrayList();
//		   for(int i=0;i<hotSearch.length;i++) {
//			   Map map = new HashMap();
//			   map.put("number"+(i+1), hotSearch[i]);
//			   list.add(map);
//		   }
		   if(hotSearch.length>0) {
//			   return "{\"statusCode\":200,\"msg\":\"热搜词加载成功!\",\"data\":"+JSONArray.fromObject(list).toString()+"}";
			   return "{\"statusCode\":200,\"msg\":\"热搜词加载成功!\",\"data\":"+JSONArray.fromObject(hotSearch).toString()+"}";
		   }else {
			   return "{\"statusCode\":300,\"msg\":\"通知后台设置热搜词!\"}";
		   }
	   }
	   
	   
	   
	   
	   private Set<Long> getStoreClassChildIds(StoreClass sc) {
		     Set ids = new HashSet();
		     ids.add(sc.getId());
		     for (StoreClass storeclass : sc.getChilds()) {
		       Set<Long> cids = getStoreClassChildIds(storeclass);
		       for (Long cid : cids) {
		         ids.add(cid);
		       }
		     }
		     return ids;
		   }

		   private Set<Long> getAreaChildIds(Area area) {
		     Set ids = new HashSet();
		     ids.add(area.getId());
		     for (Area are : area.getChilds()) {
		       Set<Long> cids = getAreaChildIds(are);
		       for (Long cid : cids) {
		         ids.add(cid);
		       }
		     }
		     return ids;
		   }
	
	
	
}
