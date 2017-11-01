package com.shopping.view.app.action;

import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Advert;
import com.shopping.foundation.domain.AdvertPosition;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.api.ApiAdvert;
import com.shopping.foundation.domain.api.ApiRecommendGoods;
import com.shopping.foundation.service.IAdvertPositionService;
import com.shopping.foundation.service.IGoodsService;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class ApiAdvertViewAction {

    @Autowired
    private IAdvertPositionService advertPositionService;

    @Autowired
    private IGoodsService goodsService;

    /**
     * 获取轮播图图片
     *
     * @param request
     * @param advertType 1==>首页轮播图 2==>积分商城轮播图
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/advert_invoke.htm?advertType=*
     */
    @RequestMapping({"/api/advert_invoke.htm"})
    @ResponseBody
    public String advert_invoke(HttpServletRequest request, String advertType) {
        if (advertType == null || advertType.equals("")) {
            advertType = "1";
        }
        if (advertType.equals("2")) {
            advertType = "229376";
        }
        List<ApiAdvert> advs = new ArrayList<ApiAdvert>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ApiAdvert apiAdvert;
        AdvertPosition ap = this.advertPositionService.getObjById(CommUtil.null2Long(advertType));
        if (ap != null) {
            List<Advert> lists = ap.getAdvs();
            for (int i = 0; i < lists.size(); i++) {
                apiAdvert = new ApiAdvert();
                apiAdvert.setAd_id(lists.get(i).getId());
                apiAdvert.setAd_title(lists.get(i).getAd_title());
                apiAdvert.setAd_begin_time(simpleDateFormat.format(lists.get(i).getAd_begin_time()));
                apiAdvert.setAd_end_time(simpleDateFormat.format(lists.get(i).getAd_end_time()));
                apiAdvert.setAd_status(lists.get(i).getAd_status());
                apiAdvert.setAd_text(lists.get(i).getAd_text());
                apiAdvert.setAd_slide_sequence(lists.get(i).getAd_slide_sequence());
                apiAdvert.setAd_url(lists.get(i).getAd_url());
                apiAdvert.setAd_click_num(lists.get(i).getAd_click_num());
                apiAdvert.setAd_path(CommUtil.getURL(request) + "/" + lists.get(i).getAd_acc().getPath() + "/" + lists.get(i).getAd_acc().getName());
                advs.add(apiAdvert);
            }
        }
        return JSONArray.fromObject(advs).toString();
    }

    /**
     * 获取精选特卖商品列表
     *
     * @param request
     * @param pageSize    页面数据记录数
     * @param pageCurrent 当前页
     * @param field       商品类别为推荐时传推荐，为其他类别时传不传
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/store_reommend_goods.htm?pageSize=*&pageCurrent=*&field=推荐
     */
    @RequestMapping({"/api/store_reommend_goods.htm"})
    @ResponseBody
    public String store_reommend_goods(HttpServletRequest request, String pageSize, String pageCurrent, String field) {
        List<ApiRecommendGoods> store_reommend_goods = new ArrayList();
        Map params = new HashMap();
        ApiRecommendGoods apiRecommendGoods;
        int x = CommUtil.null2Int(pageCurrent);
        int y = CommUtil.null2Int(pageSize);
        int size = 0;
        int pages = 0;
        if (field == null)
            field = "";
        if (field.equals("推荐")) {
            params.put("store_recommend", Boolean.valueOf(true));
            params.put("goods_status", Integer.valueOf(0));
            List<Goods> store_reommend_goods_list = this.goodsService.query("select obj from Goods obj where obj.store_recommend=:store_recommend and obj.goods_status=:goods_status order by obj.store_recommend_time desc", params, -1, -1);

            size = store_reommend_goods_list.size();

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
                        apiRecommendGoods = new ApiRecommendGoods();
                        apiRecommendGoods.setGoods_id(store_reommend_goods_list.get(i).getId());
                        apiRecommendGoods.setGoods_name(store_reommend_goods_list.get(i).getGoods_name());
                        apiRecommendGoods.setGoods_price(store_reommend_goods_list.get(i).getGoods_price());
                        apiRecommendGoods.setGoods_weight(store_reommend_goods_list.get(i).getGoods_weight());
                        apiRecommendGoods.setGoods_img(CommUtil.getURL(request) + "/" +
                                store_reommend_goods_list.get(i).getGoods_main_photo().getPath() + "/" +
                                store_reommend_goods_list.get(i).getGoods_main_photo().getName());
                        apiRecommendGoods.setStore_price(store_reommend_goods_list.get(i).getStore_price());
                        store_reommend_goods.add(apiRecommendGoods);
                    }
                }
            }
        }

        String content = "{\"statusCode\":\"200\",\"msg\":\"数据加载成功！\",\"rowCount\":" + size + ",\"pages\":" + pages
                + ",\"pageCurrent\":" + x + ",\"list\":";
        String liststr = JSONArray.fromObject(store_reommend_goods).toString();
        content += liststr + "}";
        return content;
    }


}
