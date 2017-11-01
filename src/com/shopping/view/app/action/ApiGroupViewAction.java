package com.shopping.view.app.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shopping.core.domain.virtual.FeeMap;
import net.sf.json.JSONArray;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.shopping.core.security.support.SecurityUserHolder;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.Evaluate;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.GoodsSpecProperty;
import com.shopping.foundation.domain.GoodsSpecification;
import com.shopping.foundation.domain.Group;
import com.shopping.foundation.domain.GroupClass;
import com.shopping.foundation.domain.GroupGoods;
import com.shopping.foundation.domain.StoreCart;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.api.ApiGoodsSpecification;
import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.IEvaluateService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IGroupClassService;
import com.shopping.foundation.service.IGroupGoodsService;
import com.shopping.foundation.service.IGroupService;
import com.shopping.foundation.service.IStoreCartService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.manage.seller.Tools.TransportTools;
import com.shopping.view.web.tools.GoodsViewTools;

@Controller
public class ApiGroupViewAction {


    @Autowired
    private IAreaService areaService;
    @Autowired
    private IGroupGoodsService ggService;
    @Autowired
    private IGroupClassService gcService;
    @Autowired
    private IGroupService groupService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IEvaluateService evaluateService;
    @Autowired
    private TransportTools transportTools;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IGoodsCartService goodsCartService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IStoreCartService storeCartService;
    @Autowired
    private GoodsViewTools goodsViewTools;


    private float cal_order_trans(String trans_json, int trans_type, Object goods_weight, Object goods_volume, String city_name) {
        float fee = 0.0F;
        boolean cal_flag = false;
        List<Map> list = (List) Json.fromJson(ArrayList.class, trans_json);
        if ((list != null) && (list.size() > 0)) {
            for (Map map : list) {
                String[] city_list = CommUtil.null2String(map.get("city_name"))
                        .split("、");
                for (String city : city_list) {
                    if ((city.equals(city_name)) || (city_name.indexOf(city) == 0)) {
                        cal_flag = true;
                        float trans_weight = CommUtil.null2Float(map
                                .get("trans_weight"));
                        float trans_fee = CommUtil.null2Float(map
                                .get("trans_fee"));
                        float trans_add_weight = CommUtil.null2Float(map
                                .get("trans_add_weight"));
                        float trans_add_fee = CommUtil.null2Float(map
                                .get("trans_add_fee"));
                        if (trans_type == 0) {
                            fee = trans_fee;
                        }
                        if ((trans_type == 1) &&
                                (CommUtil.null2Float(goods_weight) > 0.0F)) {
                            fee = trans_fee;
                            float other_price = 0.0F;
                            if (trans_add_weight > 0.0F) {
                                other_price = trans_add_fee *
                                        (float) Math.round(Math.ceil(
                                                CommUtil.subtract(goods_weight,
                                                        Float.valueOf(trans_weight)))) /
                                        trans_add_fee;
                            }
                            fee += other_price;
                        }

                        if ((trans_type != 2) ||
                                (CommUtil.null2Float(goods_volume) <= 0.0F)) break;
                        fee = trans_fee;
                        float other_price = 0.0F;
                        if (trans_add_weight > 0.0F) {
                            other_price = trans_add_fee *
                                    (float) Math.round(Math.ceil(
                                            CommUtil.subtract(goods_volume,
                                                    Float.valueOf(trans_weight)))) /
                                    trans_add_fee;
                        }
                        fee += other_price;

                        break;
                    }
                }
            }
            if (!cal_flag) {
                for (Map map : list) {
                    String[] city_list = CommUtil.null2String(
                            map.get("city_name")).split("、");
                    for (String city : city_list) {
                        if (city.equals("全国")) {
                            float trans_weight = CommUtil.null2Float(map
                                    .get("trans_weight"));
                            float trans_fee = CommUtil.null2Float(map
                                    .get("trans_fee"));
                            float trans_add_weight = CommUtil.null2Float(map
                                    .get("trans_add_weight"));
                            float trans_add_fee = CommUtil.null2Float(map
                                    .get("trans_add_fee"));
                            if (trans_type == 0) {
                                fee = trans_fee;
                            }
                            if ((trans_type == 1) &&
                                    (CommUtil.null2Float(goods_weight) > 0.0F)) {
                                fee = trans_fee;
                                float other_price = 0.0F;
                                if (trans_add_weight > 0.0F) {
                                    other_price = trans_add_fee *
                                            (float) Math.round(Math.ceil(
                                                    CommUtil.subtract(goods_weight,
                                                            Float.valueOf(trans_weight)))) /
                                            trans_add_fee;
                                }
                                fee += other_price;
                            }

                            if ((trans_type != 2) ||
                                    (CommUtil.null2Float(goods_volume) <= 0.0F)) break;
                            fee = trans_fee;
                            float other_price = 0.0F;
                            if (trans_add_weight > 0.0F) {
                                other_price = trans_add_fee *
                                        (float) Math.round(Math.ceil(
                                                CommUtil.subtract(goods_volume,
                                                        Float.valueOf(trans_weight)))) /
                                        trans_add_fee;
                            }
                            fee += other_price;

                            break;
                        }
                    }
                }
            }
        }
        return fee;
    }

    private List<StoreCart> cart_calc(HttpServletRequest request) {
        List<StoreCart> cart = new ArrayList<StoreCart>();
        List<StoreCart> user_cart = new ArrayList<StoreCart>();
        List<StoreCart> cookie_cart = new ArrayList<StoreCart>();
        User user = null;
        if (SecurityUserHolder.getCurrentUser() != null) {
            user = this.userService.getObjById(SecurityUserHolder.getCurrentUser().getId());
        }
        String cart_session_id = "";
        Map params = new HashMap();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("cart_session_id")) {
                    cart_session_id = CommUtil.null2String(cookie.getValue());
                }
            }
        }
        if (user != null) {
            if (!cart_session_id.equals("")) {
                if (user.getStore() != null) {
                    params.clear();
                    params.put("cart_session_id", cart_session_id);
                    params.put("user_id", user.getId());
                    params.put("sc_status", Integer.valueOf(0));
                    params.put("store_id", user.getStore().getId());
                    List<StoreCart> store_cookie_cart = this.storeCartService.query(
                            "select obj from StoreCart obj where (obj.cart_session_id=:cart_session_id or obj.user.id=:user_id) and obj.sc_status=:sc_status and obj.store.id=:store_id",
                            params, -1, -1);
                    for (StoreCart sc : store_cookie_cart) {
                        // sc = (StoreCart)localIterator1.next();
                        for (GoodsCart gc : ((StoreCart) sc).getGcs()) {
                            gc.getGsps().clear();
                            this.goodsCartService.delete(gc.getId());
                        }
                        this.storeCartService.delete(((StoreCart) sc).getId());
                    }
                }

                params.clear();
                params.put("cart_session_id", cart_session_id);
                params.put("sc_status", Integer.valueOf(0));
                cookie_cart = this.storeCartService.query(
                        "select obj from StoreCart obj where obj.cart_session_id=:cart_session_id and obj.sc_status=:sc_status",
                        params, -1, -1);

                params.clear();
                params.put("user_id", user.getId());
                params.put("sc_status", Integer.valueOf(0));
                user_cart = this.storeCartService.query(
                        "select obj from StoreCart obj where obj.user.id=:user_id and obj.sc_status=:sc_status", params,
                        -1, -1);
            } else {
                params.clear();
                params.put("user_id", user.getId());
                params.put("sc_status", Integer.valueOf(0));
                user_cart = this.storeCartService.query(
                        "select obj from StoreCart obj where obj.user.id=:user_id and obj.sc_status=:sc_status", params,
                        -1, -1);
            }

        } else if (!cart_session_id.equals("")) {
            params.clear();
            params.put("cart_session_id", cart_session_id);
            params.put("sc_status", Integer.valueOf(0));
            cookie_cart = this.storeCartService.query(
                    "select obj from StoreCart obj where obj.cart_session_id=:cart_session_id and obj.sc_status=:sc_status",
                    params, -1, -1);
        }

        for (StoreCart sc : user_cart) {
            boolean sc_add = true;
            for (StoreCart sc1 : cart) {
                if (sc1.getStore().getId().equals(sc.getStore().getId())) {
                    sc_add = false;
                }
            }
            if (sc_add) {
                cart.add(sc);
            }
        }
        for (StoreCart sc : cookie_cart) {
            boolean sc_add = true;
            for (StoreCart sc1 : cart) {
                if (sc1.getStore().getId().equals(sc.getStore().getId())) {
                    sc_add = false;
                    for (GoodsCart gc : sc.getGcs()) {
                        gc.setSc(sc1);
                        this.goodsCartService.update(gc);
                    }
                    this.storeCartService.delete(sc.getId());
                }
            }
            if (sc_add) {
                cart.add(sc);
            }
        }
        return (List<StoreCart>) cart;
    }


    /**
     * 团购的结束时间
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping({"/api/groupTime.htm"})
    @ResponseBody
    public String groupTime(HttpServletRequest request, HttpServletResponse response) {
        Map params = new HashMap();
        List list = new ArrayList();

        params.put("beginTime", new Date());
        params.put("endTime", new Date());
        List<Group> groups = this.groupService
                .query("select obj from Group obj where obj.beginTime<=:beginTime and obj.endTime>=:endTime",
                        params, -1, -1);
        if (groups != null & groups.size() > 0) {
            Map map = new HashMap();
            map.put("groupId", groups.get(0).getId());
            map.put("groupName", groups.get(0).getGroup_name());
            map.put("groupTime", groups.get(0).getEndTime());

            list.add(map);
            return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + JSON.toJSONString(list) + "}";
        } else {
            return "{\"statusCode\":201,\"msg\":\"当前暂无团购信息!\"}";
        }

    }


    /**
     * 团购商品有哪些分类
     *
     * @param response
     * @return
     */
    @RequestMapping({"/api/groupClass.htm"})
    @ResponseBody
    public String groupClass(HttpServletResponse response) {

        List<GroupClass> gcs = this.gcService.query("select obj from GroupClass obj where obj.parent.id is null order by obj.gc_sequence asc",
                null, -1, -1);
        List list = new ArrayList();
        for (GroupClass gc : gcs) {
            Map map = new HashMap();
            map.put("groupClassId", gc.getId());
            map.put("groupClassName", gc.getGc_name());
            list.add(map);
        }
        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        if (list != null && list.size() > 0) {
            String data = JSON.toJSONString(list);
            return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + data + "}";
        } else {
            return "{\"statusCode\":500,\"msg\":\"加载失败!\"}";
        }
    }


    /**
     * 团购中每一个商品类型下的具体商品的列表
     *
     * @param request
     * @param groupClassId 团购商品的分类的id
     * @param currentPage  当前页
     * @param pageSize     每页显示几条数据
     * @return
     */
    @RequestMapping({"/api/groupGoodsList.htm"})
    @ResponseBody
    public String groupGoodsList(HttpServletRequest request, String groupClassId, String currentPage, String groupId, String pageSize) {
        Group group = this.groupService.getObjById(CommUtil.null2Long(groupId));
        if (group != null) {


            long endTime = group.getEndTime().getTime();
            long nowTime = new Date().getTime();
            int x = CommUtil.null2Int(currentPage);
            int y = CommUtil.null2Int(pageSize);
            int size;
            int pages = 0;

            if (nowTime < endTime) {
                Map params = new HashMap();
                params.put("groupClassId", CommUtil.null2Long(groupClassId));
                params.put("ggStatus", Integer.valueOf(1));
                List<GroupGoods> ggs = this.ggService.query("select obj from GroupGoods obj where obj.gg_status=:ggStatus and obj.gg_gc.id=:groupClassId order by obj.addTime asc",
                        params, -1, -1);
                size = ggs.size();

                String url = CommUtil.getURL(request);
                if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
                    url = this.configService.getSysConfig().getImageWebServer();
                }

                List list = new ArrayList();
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
                            map.put("group_goods_id", ggs.get(i).getId());
                            map.put("group_goods_name", ggs.get(i).getGg_name());
                            map.put("group_goods_count", ggs.get(i).getGg_count());
                            map.put("group_goods_price", ggs.get(i).getGg_price());
                            map.put("old_price", ggs.get(i).getGg_goods().getGoods_price());
                            String image = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
                                    + this.configService.getSysConfig().getGoodsImage().getName();
                            if (ggs.get(i).getGg_img() != null) {
                                image = url + "/" + ggs.get(i).getGg_img().getPath() + "/" + ggs.get(i).getGg_img().getName();
                            }
                            map.put("image", image);
                            list.add(map);
                        }
                    }
                }
                return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"rowCount\":" + size + ",\"pages\":" + pages
                        + ",\"pageCurrent\":" + x + ",\"data\":" + JSON.toJSONString(list) + "}";
            } else {
                return "{\"statusCode\":501,\"msg\":\"团购活动时间已经结束!\"}";
            }
        } else {
            return "{\"statusCode\":202,\"msg\":\"暂无团购信息!\"}";
        }
    }


    /**
     * 具体团购商品的详情
     *
     * @param request
     * @param response
     * @param gg_id    团购商品的id
     * @return
     */
    @RequestMapping({"/api/groupGoodsDetail.htm"})
    @ResponseBody
    public String groupGoodsDetail(HttpServletRequest request, HttpServletResponse response, String gg_id) {
        String url = CommUtil.getURL(request);
        if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
            url = this.configService.getSysConfig().getImageWebServer();
        }

        GroupGoods groupGood = this.ggService.getObjById(CommUtil.null2Long(gg_id));
        Goods goods = this.goodsService.getObjById(CommUtil.null2Long(groupGood.getGg_goods().getId()));
        List<GoodsSpecification> gsfs = goodsViewTools.generic_spec(String.valueOf(groupGood.getGg_goods().getId()));
        List list_gsp = new ArrayList();
        for (GoodsSpecification gsf : gsfs) {
            ApiGoodsSpecification agsf = new ApiGoodsSpecification();
            agsf.setSpec_id(gsf.getId());
            agsf.setSpec_name(gsf.getName());
            List list_gsp2 = new ArrayList();
            for (GoodsSpecProperty gsp : goods.getGoods_specs()) {
                long spec_id = gsp.getSpec().getId();
                if (spec_id == gsf.getId()) {
                    Map map2 = new HashMap();
                    map2.put("property_id", gsp.getId());
                    map2.put("property_value", gsp.getValue());
                    list_gsp2.add(map2);
                }
            }
            agsf.setAproperty(list_gsp2);
            list_gsp.add(agsf);
        }

        List list = new ArrayList();
        Map map = new HashMap();
        map.put("group_good_id", groupGood.getId());
        map.put("group_good_name", groupGood.getGg_name());
        map.put("group_good_price", groupGood.getGg_price());
        map.put("old_price", groupGood.getGg_goods().getGoods_price());
        map.put("boughtPepleCount", groupGood.getGg_vir_count());
        map.put("group_good_count", groupGood.getGg_count());
        map.put("gg_for_goods_id", groupGood.getGg_goods().getId());
        map.put("guige", list_gsp);

        String image = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
                + this.configService.getSysConfig().getGoodsImage().getName();
        if (groupGood.getGg_img() != null) {
            image = url + "/" + groupGood.getGg_img().getPath() + "/"
                    + groupGood.getGg_img().getName();
        }
        map.put("image", image);
        list.add(map);

        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        if (list != null && list.size() > 0) {
            String data = JSON.toJSONString(list);
            return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + data + "}";
        } else {
            return "{\"statusCode\":500,\"msg\":\"加载失败!\"}";
        }
    }

    /**
     * 商品的评价
     *
     * @param request
     * @param response
     * @param goods_id    具体商品的id
     * @param currentPage 当前是哪一页
     * @param pageSize    每页显示几条数据
     * @return
     */
    @RequestMapping({"/api/goodsEvaluate.htm"})
    @ResponseBody
    public String goodsEvaluate(HttpServletRequest request, HttpServletResponse response
            , String goods_id, String currentPage, String pageSize) {

        int x = CommUtil.null2Int(currentPage);
        int y = CommUtil.null2Int(pageSize);
        int size;
        int pages = 0;
        List list = new ArrayList();

        String url = CommUtil.getURL(request);
        if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
            url = this.configService.getSysConfig().getImageWebServer();
        }
        Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
        List<Evaluate> evaluates_list = goods.getEvaluates();
        size = evaluates_list.size();
        if (size == 0) {
            return "{\"statusCode\":300,\"msg\":\"该商品还没有评价内容!\"}";
        }
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
                    map.put("evaluate_info", evaluates_list.get(i).getEvaluate_info());
                    map.put("addTime", evaluates_list.get(i).getAddTime());
                    map.put("userName", evaluates_list.get(i).getEvaluate_user().getUserName());
                    int service_evaluate_value = evaluates_list.get(i).getService_evaluate().intValue();
                    int discription_evaluate_value = evaluates_list.get(i).getDescription_evaluate().intValue();
                    int ship_evaluate_value = evaluates_list.get(i).getShip_evaluate().intValue();
                    int evaluate_value = (service_evaluate_value + discription_evaluate_value + ship_evaluate_value) / 3;
                    map.put("evaluate_value", evaluate_value);
                    String userIcon = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
                            + this.configService.getSysConfig().getGoodsImage().getName();
                    if (evaluates_list.get(i).getEvaluate_user().getPhoto() != null) {
                        userIcon = url + "/" + evaluates_list.get(i).getEvaluate_user().getPhoto().getPath() + "/"
                                + evaluates_list.get(i).getEvaluate_user().getPhoto().getName();
                    }
                    map.put("userIcon", userIcon);
                    list.add(map);
                }
            }
        }
        return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"rowCount\":" + size + ",\"pages\":" + pages
                + ",\"pageCurrent\":" + x + ",\"data\":" + JSON.toJSONString(list) + "}";
    }

    /**
     * 运费的计算
     *
     * @param storeCart_id 商品购物车的id
     * @param area_id      收货地址区域id
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/ship_fee.htm?storeCart_id=*&area_id=*
     */
    @RequestMapping({"/api/ship_fee.htm"})
    @ResponseBody
    public String query_cart_trans(String storeCart_id, String area_id) {
        StoreCart sCart = this.storeCartService.getObjById(CommUtil.null2Long(storeCart_id));
        List feeList = new ArrayList();
        if ((area_id != null) && (!area_id.equals(""))) {
            Area area = this.areaService.getObjById(CommUtil.null2Long(area_id)).getParent();
            String city_name = area.getAreaName();
            float mail_fee = 0.0F;
            float express_fee = 0.0F;
            float ems_fee = 0.0F;
            for (GoodsCart gc : sCart.getGcs()) {
                Goods goods = this.goodsService.getObjById(gc.getGoods().getId());
                if (goods.getGoods_transfee() == 0) {
                    if (goods.getTransport() != null) {
                        mail_fee = mail_fee +
                                cal_order_trans(goods.getTransport()
                                        .getTrans_mail_info(), goods
                                        .getTransport().getTrans_type(), goods
                                        .getGoods_weight(), goods
                                        .getGoods_volume(), city_name);

                        express_fee = express_fee +
                                cal_order_trans(goods.getTransport()
                                        .getTrans_express_info(), goods
                                        .getTransport().getTrans_type(), goods
                                        .getGoods_weight(), goods
                                        .getGoods_volume(), city_name);

                        ems_fee = ems_fee +
                                cal_order_trans(goods.getTransport()
                                        .getTrans_ems_info(), goods
                                        .getTransport().getTrans_type(), goods
                                        .getGoods_weight(), goods
                                        .getGoods_volume(), city_name);
                    } else {
                        mail_fee = mail_fee +
                                CommUtil.null2Float(goods.getMail_trans_fee());

                        express_fee = express_fee +
                                CommUtil.null2Float(goods
                                        .getExpress_trans_fee());

                        ems_fee = ems_fee +
                                CommUtil.null2Float(goods.getEms_trans_fee());
                    }
                }
            }
            if ((mail_fee == 0.0F) && (express_fee == 0.0F) && (ems_fee == 0.0F)) {
                feeList.add(new FeeMap("卖家承担", new BigDecimal(0)));
            } else {
                feeList.add(new FeeMap("平邮", new BigDecimal(mail_fee)));
                feeList.add(new FeeMap("快递", new BigDecimal(express_fee)));
                feeList.add(new FeeMap("EMS", new BigDecimal(ems_fee)));
            }
        }
        if (feeList != null && feeList.size() > 0) {
            return "{\"statusCode\":200,\"msg\":\"运费计算成功!\",\"data\":" + JSONArray.fromObject(feeList).toString() + "}";
        } else {
            return "{\"statusCode\":500,\"msg\":\"运费计算失败!\"}";
        }
    }


}
