package com.shopping.view.app.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsBrand;
import com.shopping.foundation.service.IGoodsBrandService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.ISysConfigService;

@Controller
public class ApiBrandViewAction {

	@Autowired
	private IGoodsBrandService gbService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsService goodsService;
	
	
	/**
	 * 品牌的类型
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping({"/api/brandCategory.htm"})
	@ResponseBody
	public String brandCategory(HttpServletRequest request,HttpServletResponse response){
		Map params = new HashMap();
		params.put("recommend", Boolean.valueOf(true));
		params.put("audit", Integer.valueOf(1));
		List<GoodsBrand> gbs = this.gbService.query("select obj from GoodsBrand obj where obj.recommend=:recommend and obj.audit=:audit order by obj.sequence asc",
				params, -1, -1);
		String url = CommUtil.getURL(request);
		if(!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))){
			url = this.configService.getSysConfig().getImageWebServer();
		}
		List list = new ArrayList();
		for(GoodsBrand gb : gbs){
			Map map = new HashMap();
			map.put("gb_id", gb.getId());
			map.put("gb_name", gb.getName());
			map.put("gb_category_id", gb.getCategory().getId());
			String image = url+"/"+this.configService.getSysConfig().getGoodsImage().getPath()+"/"
					+this.configService.getSysConfig().getGoodsImage().getName();
			if(gb.getBrandLogo()!=null){
				image=url+"/"+gb.getBrandLogo().getPath()+"/"
						+gb.getBrandLogo().getName();
				map.put("image", image);
			}
			list.add(map);
		}
		
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		if(list.size()>0 && list !=null){
			String data = Json.toJson(list);
			return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":"+data+"}";
		}else{
			return "{\"statusCode\":\"500\",\"msg\":\"加载失败!\"}";
		}
	}
	
	/**
	 * 具体品牌商品的清单列表
	 * @param request
	 * @param response
	 * @param gb_id	商品品牌的ID
	 * @param currentPage   当前页
	 * @param pageSize  每页几条数据
	 * @return
	 */
	@RequestMapping({"/api/goodsBrandList.htm"})
	@ResponseBody
	public String goodsBrandList(HttpServletRequest request,HttpServletResponse response,String gb_id,
			String currentPage,String pageSize){
		
		int x = CommUtil.null2Int(currentPage);
		int y = CommUtil.null2Int(pageSize);
		int size;
		int pages=0;
		
		Map params = new HashMap();
		params.put("gb_id", CommUtil.null2Long(gb_id));
		String url = CommUtil.getURL(request);
		if(!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))){
			url = this.configService.getSysConfig().getImageWebServer();
		}
		List<Goods> goods = this.goodsService.query("select obj from Goods obj where 1=1 and obj.goods_brand.id=:gb_id",
				params, -1, -1);
		List list = new ArrayList();
		size = goods.size();
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
				for(int i = begin;i < end;i++){
					Map map = new HashMap();
					map.put("goodsId", goods.get(i).getId());
					map.put("goodsName", goods.get(i).getGoods_name());
					map.put("oldGoodsPrice", goods.get(i).getGoods_price());
					map.put("storePrice", goods.get(i).getStore_price());
					
					String image = url+"/"+this.configService.getSysConfig().getGoodsImage().getPath()+"/"
							+this.configService.getSysConfig().getGoodsImage().getName();
					if(goods.get(i).getGoods_main_photo()!=null){
						image = url+"/"+goods.get(i).getGoods_main_photo().getPath()+"/"
								+goods.get(i).getGoods_main_photo().getName();
						map.put("image", image);
					}
					list.add(map);
				}
			}
		}
		
			return "{\"statusCode\":200,\"rowCount\":"+size+",\"msg\":\"加载成功!\",\"data\":"+JSON.toJSONString(list)+","
					+ "\"pageCurrent\":"+x+",\"pages\":"+pages+"}";
	}
	
	
	
	
	
/*	@RequestMapping({"/api/goodsBrandList2.htm"})
	@ResponseBody
	public String goodsBrandList2(HttpServletRequest request,HttpServletResponse response,String gb_id,
			String currentPage,String pageSize){
		
		Map params = new HashMap();
		params.put("gb_id", CommUtil.null2Long(gb_id));
		List<Goods> goodsList = this.goodsService.query("select obj from Goods obj where obj.goods_brand.id=:gb_id", params, -1, -1);
		int total = CommUtil.null2Int(currentPage)*CommUtil.null2Int(pageSize);
		if(CommUtil.null2Int(currentPage)<=0 || goodsList.size()<total){
			return "{\"statusCode\":555,\"msg\":\"你所传的参数有误!\"}";
		}
		
		List list = new ArrayList();
		String url = CommUtil.getURL(request);
		if(!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))){
			url = this.configService.getSysConfig().getImageWebServer();
		}
		Map<String,Object> map = new HashMap<String,Object>();
		GoodsBrand gb = this.gbService.getObjById(CommUtil.null2Long(gb_id));
		GoodsQueryObject gqo = new GoodsQueryObject(currentPage,map,null,null);
		gqo.addQuery("obj.goods_brand.id", new SysMap("goods_brand_id",gb.getId()),"=");
		gqo.addQuery("obj.goods_status", new SysMap("goods_status",Integer.valueOf(0)),"=");
		gqo.setPageSize(CommUtil.null2Int(pageSize));
		IPageList pList = this.goodsService.list(gqo);
		if(pList.getResult()!=null){
			List<Goods> goods_list = pList.getResult();
			if(goods_list.size()>0){
				for(Goods goods : goods_list){
					Map map2 = new HashMap();
					String image = url+"/"+this.configService.getSysConfig().getGoodsImage().getPath()+"/"
							+this.configService.getSysConfig().getGoodsImage().getName();
					if(goods.getGoods_main_photo()!=null){
						image = url+"/"+goods.getGoods_main_photo().getPath()+"/"
								+goods.getGoods_main_photo().getName();
					}
					map2.put("goods_id", goods.getId());
					map2.put("goods_name", goods.getGoods_name());
					map2.put("old_price", goods.getGoods_price());
					map2.put("store_price", goods.getStore_price());
					map2.put("image", image);
					list.add(map2);
				}
			}
			String data = JSON.toJSONString(list);
			//String data = Json.toJson(list, JsonFormat.compact());
			return "{\"statusCode\":200,\"rowCount\":"+goodsList.size()+",\"msg\":\"数据加载成功!\",\"data\":"+data+"}";
		}else{
			String data = JSON.toJSONString(list);
			return "{\"statusCode\":200,\"msg\":\"数据为空!\",\"data\":"+data+"}";
		}
	}
	*/  
	
	
	
	
}
