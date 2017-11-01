package com.shopping.view.app.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shopping.foundation.domain.api.ApiGoodsSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsClass;
import com.shopping.foundation.domain.GoodsSpecProperty;
import com.shopping.foundation.domain.GoodsSpecification;
import com.shopping.foundation.domain.Store;
import com.shopping.foundation.domain.StoreClass;
import com.shopping.foundation.domain.api.ApiIndexGoods;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IGoodsClassService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IStoreClassService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.manage.admin.tools.UserTools;
import com.shopping.view.web.tools.GoodsViewTools;

import net.sf.json.JSONArray;

@Controller
public class ApiShoppingIndexViewAction {

	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private UserTools userTools;
	@Autowired
	private IStoreClassService storeClassService;


	/**
	 * 主页面物品分类
	 * @return
	 */
	@RequestMapping({ "/api/goods_class_list.htm" })
	@ResponseBody
	public String goods_floor() {

		Map params = new HashMap();
		params.put("level", Integer.valueOf(0));
		params.put("recommend", Boolean.valueOf(false));
		String query = "select obj from GoodsClass obj where obj.level=:level and obj.recommend=:recommend";

		List map_list = new ArrayList();
		List<GoodsClass> goods_class_list;
		Map goods_class_map;

		List<GoodsClass> goods_class_lists = this.goodsClassService.query(query, params, -1, -1);

		if (goods_class_lists.size() > 0 && goods_class_lists.get(0).getChilds().size() > 0) {
			goods_class_list = goods_class_lists.get(0).getChilds().get(0).getChilds();
			for (int i = 0; i < goods_class_list.size(); i++) {
				goods_class_map = new HashMap();
				goods_class_map.put("goods_class_id", goods_class_list.get(i).getId());
				goods_class_map.put("goods_class_name", goods_class_list.get(i).getClassName());
				goods_class_map.put("goods_class_level", goods_class_list.get(i).getLevel());
				map_list.add(goods_class_map);
			}
		}
		return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + JSON.toJSONString(map_list) + "}";
	}

	/**
	 * 主分类商品的列表
	 *
	 * @param request
	 * @param goodsClassId   商品主分类名称的id
	 * @param pageCurrent    当前页
	 * @param pageSize			 每页显示几条
	 */
	@RequestMapping({ "/api/index_goods_detail.htm" })
	@ResponseBody
	public String showGoodsDetail(HttpServletRequest request, String goodsClassId, String pageCurrent,
			String pageSize) {
		Map params = new HashMap();
		List map_list = new ArrayList();
		int x = CommUtil.null2Int(pageCurrent);
		int y = CommUtil.null2Int(pageSize);
		int size;
		int pages = 0;
		String url = CommUtil.getURL(request);
		params.put("goodsClassId", CommUtil.null2Long(goodsClassId));

		if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
			url = this.configService.getSysConfig().getImageWebServer();
		}

		List<Goods> goods = this.goodsService.query("select obj from Goods obj where obj.gc.id=:goodsClassId", params,
				-1, -1);
		size = goods.size();

		if (size > 0) {
			pages = size / y;
			if (size % y != 0) {
				pages++;
			}
			int begin = (x - 1) * y;
			int end = x * y;
			if (begin < size) {
				if (end > size) {
					end = size;
				}
				for (int i = begin; i < end; i++) {
					Map map = new HashMap();
					map.put("goodsId", goods.get(i).getId());
					map.put("goodsName", goods.get(i).getGoods_name());
					map.put("goodsPrice", goods.get(i).getGoods_price());
					map.put("currentPrice", goods.get(i).getGoods_current_price());
					map.put("goodsInventory", goods.get(i).getGoods_inventory());
					String image = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
							+ this.configService.getSysConfig().getGoodsImage().getName();
					if (goods.get(i).getGoods_main_photo() != null) {
						image = url + "/" + goods.get(i).getGoods_main_photo().getPath() + "/"
								+ goods.get(i).getGoods_main_photo().getName();
					}
					map.put("image", image);
					map_list.add(map);
				}
			}
		}
		return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"rowCount\":" + size + ",\"pages\":" + pages
				+ ",\"pageCurrent\":" + x + ",\"data\":" + JSON.toJSONString(map_list) + "}";
	}

	/**
	 * 具体商品的详情
	 * @param request
	 * @param response
	 * @param goodsId
	 * @return
	 */
	@RequestMapping({ "/api/goodsDetail.htm" })
	@ResponseBody
	public String goodsDetail(HttpServletRequest request, HttpServletResponse response, String goodsId) {

		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		String url = CommUtil.getURL(request);
		if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
			url = this.configService.getSysConfig().getImageWebServer();
		}
				
		List<GoodsSpecification> gsfs = goodsViewTools.generic_spec(goodsId);
		
		List list_gsp = new ArrayList();
		for(GoodsSpecification gsf : gsfs){
			ApiGoodsSpecification agsf  = new ApiGoodsSpecification();
			agsf.setSpec_id(gsf.getId());
			agsf.setSpec_name(gsf.getName());
			List list_gsp2 = new ArrayList();
			for(GoodsSpecProperty gsp : goods.getGoods_specs()){
				long spec_id = gsp.getSpec().getId();
				if(spec_id==gsf.getId()){
					Map map2 = new HashMap();
					map2.put("property_id", gsp.getId());
					map2.put("property_value", gsp.getValue());
					list_gsp2.add(map2);
				}
			}	
			agsf.setAproperty(list_gsp2);
			list_gsp.add(agsf);
		}
		String store_area = goods.getGoods_store().getArea().getParent().getParent().getAreaName() + " "
				+ goods.getGoods_store().getArea().getParent().getAreaName() + " "
				+ goods.getGoods_store().getArea().getAreaName();
		String image = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
				+ this.configService.getSysConfig().getGoodsImage().getName();
		List list4 = new ArrayList();
		if (goods.getGoods_main_photo() != null) {
			List<Accessory> images = goods.getGoods_photos();
			String main_image = url + "/" + goods.getGoods_main_photo().getPath() + "/"
					+ goods.getGoods_main_photo().getName();
			list4.add(main_image);
			for (int i = 0; i < images.size(); i++) {
				Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(images.get(i).getId()));
				image = url + "/" + accessory.getPath() + "/" + accessory.getName();
				String[] img = new String[images.size()];
				img[i] = image;
				list4.add(img[i]);
			}
		}
		
		List<ApiIndexGoods> list = new ArrayList<ApiIndexGoods>();	
		ApiIndexGoods indexGoods = new ApiIndexGoods();
		indexGoods.setId(goods.getId());
		indexGoods.setName(goods.getGoods_name());
		indexGoods.setGoods_price(goods.getGoods_price());
		indexGoods.setStore_price(goods.getStore_price());
		indexGoods.setArea_name(store_area);
		indexGoods.setGoods_salenum(goods.getGoods_salenum());
		indexGoods.setImage(list4);
		indexGoods.setGuige(list_gsp);
		
		list.add(indexGoods);
		if (list != null && list.size() > 0) {
			//String data = JSON.toJSONString(list);
			String data = JSONArray.fromObject(list).toString();
			//String data = JSONObject.fromObject(list).toString();
			return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + data + "}";
		} else {
			return "{\"statusCode\":500,\"msg\":\"加载失败!\"}";
		}
	}
	
	
	/**
	 * 商品的详细的介绍的内容
	 * @param request
	 * @param response
	 * @param goods_id	商品的id
	 * @param id				店铺的id
	 * @return
	 */
	@RequestMapping({"/api/goods_detail_content.htm"})
	   public ModelAndView goods_detail(HttpServletRequest request, HttpServletResponse response, String id, String goods_id) {
	     String template = "default";
	     Store store = this.storeService.getObjById(CommUtil.null2Long(id));
	     if (store != null) {
	       template = store.getTemplate();
	     }
	     ModelAndView mv = new JModelAndView(template + "/goods_detail.html",
	       this.configService.getSysConfig(),
	       this.userConfigService.getUserConfig(), 1, request, response);
	     Goods goods = this.goodsService
	       .getObjById(CommUtil.null2Long(goods_id));
	     mv.addObject("obj", goods);
	     generic_evaluate(goods.getGoods_store(), mv);
	     this.userTools.query_user();
	     return mv;
	   }


	 private void generic_evaluate(Store store, ModelAndView mv) {
	     double description_result = 0.0D;
	     double service_result = 0.0D;
	     double ship_result = 0.0D;
	     if (store.getSc() != null) {
	       StoreClass sc = this.storeClassService.getObjById(store.getSc()
	         .getId());
	       float description_evaluate = CommUtil.null2Float(sc
	         .getDescription_evaluate());
	       float service_evaluate = CommUtil.null2Float(sc
	         .getService_evaluate());
	       float ship_evaluate = CommUtil.null2Float(sc.getShip_evaluate());
	       if (store.getPoint() != null) {
	         float store_description_evaluate = CommUtil.null2Float(store
	           .getPoint().getDescription_evaluate());
	         float store_service_evaluate = CommUtil.null2Float(store
	           .getPoint().getService_evaluate());
	         float store_ship_evaluate = CommUtil.null2Float(store
	           .getPoint().getShip_evaluate());

	         description_result = CommUtil.div(Float.valueOf(store_description_evaluate -
	           description_evaluate), Float.valueOf(description_evaluate));
	         service_result = CommUtil.div(Float.valueOf(store_service_evaluate -
	           service_evaluate), Float.valueOf(service_evaluate));
	         ship_result = CommUtil.div(Float.valueOf(store_ship_evaluate - ship_evaluate),
	           Float.valueOf(ship_evaluate));
	       }
	     }
	     if (description_result > 0.0D) {
	       mv.addObject("description_css", "better");
	       mv.addObject("description_type", "高于");
	       mv.addObject("description_result",
	         CommUtil.null2String(Double.valueOf(CommUtil.mul(Double.valueOf(description_result), Integer.valueOf(100)))) +
	         "%");
	     }
	     if (description_result == 0.0D) {
	       mv.addObject("description_css", "better");
	       mv.addObject("description_type", "持平");
	       mv.addObject("description_result", "-----");
	     }
	     if (description_result < 0.0D) {
	       mv.addObject("description_css", "lower");
	       mv.addObject("description_type", "低于");
	       mv.addObject(
	         "description_result",
	         CommUtil.null2String(Double.valueOf(CommUtil.mul(Double.valueOf(-description_result), Integer.valueOf(100)))) +
	         "%");
	     }
	     if (service_result > 0.0D) {
	       mv.addObject("service_css", "better");
	       mv.addObject("service_type", "高于");
	       mv.addObject("service_result",
	         CommUtil.null2String(Double.valueOf(CommUtil.mul(Double.valueOf(service_result), Integer.valueOf(100)))) +
	         "%");
	     }
	     if (service_result == 0.0D) {
	       mv.addObject("service_css", "better");
	       mv.addObject("service_type", "持平");
	       mv.addObject("service_result", "-----");
	     }
	     if (service_result < 0.0D) {
	       mv.addObject("service_css", "lower");
	       mv.addObject("service_type", "低于");
	       mv.addObject("service_result",
	         CommUtil.null2String(Double.valueOf(CommUtil.mul(Double.valueOf(-service_result), Integer.valueOf(100)))) +
	         "%");
	     }
	     if (ship_result > 0.0D) {
	       mv.addObject("ship_css", "better");
	       mv.addObject("ship_type", "高于");
	       mv.addObject("ship_result",
	         CommUtil.null2String(Double.valueOf(CommUtil.mul(Double.valueOf(ship_result), Integer.valueOf(100)))) + "%");
	     }
	     if (ship_result == 0.0D) {
	       mv.addObject("ship_css", "better");
	       mv.addObject("ship_type", "持平");
	       mv.addObject("ship_result", "-----");
	     }
	     if (ship_result < 0.0D) {
	       mv.addObject("ship_css", "lower");
	       mv.addObject("ship_type", "低于");
	       mv.addObject("ship_result",
	         CommUtil.null2String(Double.valueOf(CommUtil.mul(Double.valueOf(-ship_result), Integer.valueOf(100)))) + "%");
	     }
	   }
	
	
	@RequestMapping({"/api/testGoodsDetail.htm"})
	@ResponseBody
	public String testGoodsDetail(HttpServletRequest request,HttpServletResponse response,
			String goodsId){
		
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goodsId));
		List<GoodsSpecification> gsfs = goodsViewTools.generic_spec(goodsId);
		
		List list_gsp = new ArrayList();
		for(GoodsSpecification gsf : gsfs){
			ApiGoodsSpecification agsf  = new ApiGoodsSpecification();
			agsf.setSpec_id(gsf.getId());
			agsf.setSpec_name(gsf.getName());
			List list_gsp2 = new ArrayList();
			for(GoodsSpecProperty gsp : goods.getGoods_specs()){
				long spec_id = gsp.getSpec().getId();
				if(spec_id==gsf.getId()){
					Map map2 = new HashMap();
					map2.put("property_id", gsp.getId());
					map2.put("property_value", gsp.getValue());
					list_gsp2.add(map2);
				}
			}	
			agsf.setAproperty(list_gsp2);
			list_gsp.add(agsf);
		}
		String data = JSONArray.fromObject(list_gsp).toString();
		//String data = JSONObject.fromObject(list).toString();
		return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + data + "}";
	}

	


}
