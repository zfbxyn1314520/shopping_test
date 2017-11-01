package com.shopping.view.app.action;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shopping.foundation.domain.*;
import com.shopping.foundation.domain.api.ApiCouponInfo;
import com.shopping.foundation.domain.api.ApiGoodsBalance;
import com.shopping.foundation.domain.api.ApiOrderData;
import com.shopping.foundation.service.*;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.WebForm;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.CouponInfo;
import com.shopping.foundation.domain.Goods;
import com.shopping.foundation.domain.GoodsCart;
import com.shopping.foundation.domain.GoodsSpecProperty;
import com.shopping.foundation.domain.GroupGoods;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.StoreCart;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.api.ApiGoodsCart;
import com.shopping.foundation.service.IAddressService;
import com.shopping.foundation.service.ICouponInfoService;
import com.shopping.foundation.service.IGoodsCartService;
import com.shopping.foundation.service.IGoodsService;
import com.shopping.foundation.service.IGoodsSpecPropertyService;
import com.shopping.foundation.service.IStoreCartService;
import com.shopping.foundation.service.IStoreService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;

@Controller
public class ApiCartViewAction {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IStoreCartService storeCartService;
    @Autowired
    private IGoodsCartService goodsCartService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IGoodsSpecPropertyService goodsSpecPropertyService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private ICouponInfoService couponInfoService;
    @Autowired
    private IStoreService storeService;
    @Autowired
    private IOrderFormLogService orderFormLogService;
    @Autowired
    private IOrderFormService orderFormService;


    private List<StoreCart> cart_calc(HttpServletRequest request) {
        List<StoreCart> cart = new ArrayList<StoreCart>();
        List<StoreCart> user_cart = new ArrayList<StoreCart>();
        List<StoreCart> cookie_cart = new ArrayList<StoreCart>();
        User user = (User) request.getSession(false).getAttribute("user");
        if (user != null) {
            user = this.userService.getObjById(CommUtil.null2Long(user.getId()));
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
            Iterator<GoodsCart> it = sc.getGcs().iterator();
            while (it.hasNext()) {
                GoodsCart item = it.next();
                if (item.getOf() != null) {
                    it.remove();
                }
            }
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
     * 购物车的物品清单
     *
     * @param request
     * @return 会返回一个购物车的id号(storeCart_id), 这个很重要, 在计算运费的时候需要它
     */
    @RequestMapping({"/api/cart_menu_detail.htm"})
    @ResponseBody
    public String cart_menu_detail(HttpServletRequest request) {
        List<ApiGoodsBalance> apiGoodsBalances = new ArrayList<ApiGoodsBalance>();
        List<ApiGoodsCart> apiGoodsCarts;
        ApiGoodsBalance apiGoodsBalance;
        ApiGoodsCart apiGoodsCart;
        int goods_count = 0;

        User user = (User) request.getSession(false).getAttribute("user");
        List<StoreCart> cart = cart_calc(request);
        String url = CommUtil.getURL(request);

        if (user != null) {
            if (cart != null) {
                for (StoreCart sc : cart) {
                    apiGoodsCarts = new ArrayList<ApiGoodsCart>();
                    apiGoodsBalance = new ApiGoodsBalance();
                    BigDecimal total_price = new BigDecimal(0);
                    int count = 0;
                    for (GoodsCart gc : sc.getGcs()) {
                        apiGoodsCart = new ApiGoodsCart();
                        apiGoodsCart.setGoods_id(gc.getId());
                        apiGoodsCart.setCount(gc.getCount());
                        apiGoodsCart.setGoods_name(gc.getGoods().getGoods_name());
                        apiGoodsCart.setGoods_price(gc.getGoods().getGoods_price());
                        apiGoodsCart.setStore_price(gc.getGoods().getStore_price());
                        apiGoodsCart.setPrice(gc.getPrice().multiply(new BigDecimal(gc.getCount())));

                        apiGoodsCart.setSpec_info(gc.getSpec_info());
                        apiGoodsCart.setStore_id(sc.getStore().getId());
                        apiGoodsCart.setGoods_img(
                                url + "/" + gc.getGoods().getGoods_main_photo().getPath() + "/"
                                        + gc.getGoods().getGoods_main_photo().getName() + "_small."
                                        + gc.getGoods().getGoods_main_photo().getExt());
                        total_price = total_price.add(gc.getPrice().multiply(new BigDecimal(gc.getCount())));
                        count += gc.getCount();
                        apiGoodsCarts.add(apiGoodsCart);

                    }
                    goods_count += count;
                    apiGoodsBalance.setStore_id(sc.getStore().getId());
                    apiGoodsBalance.setStore_name(sc.getStore().getStore_name());
                    apiGoodsBalance.setStoreCart_id(sc.getId());
                    apiGoodsBalance.setTotal_price(total_price);
                    apiGoodsBalance.setCount(count);
                    apiGoodsBalance.setGcs(apiGoodsCarts);
                    apiGoodsBalances.add(apiGoodsBalance);
                }
            }
//            for (GoodsCart gc : list) {
//                Goods goods = this.goodsService.getObjById(gc.getGoods().getId());
//                if (CommUtil.null2String(gc.getCart_type()).equals("combin"))
//                    total_price = CommUtil.null2Float(goods.getCombin_price());
//                else {
//                    total_price = CommUtil.null2Float(
//                            Double.valueOf(CommUtil.mul(Integer.valueOf(gc.getCount()), gc.getPrice()))) + total_price;
//                }
//            }

            return "{\"statusCode\":200,\"rowCount\":" + goods_count
                    + ",\"msg\":\"加载成功!\",\"data\":" + JSONArray.fromObject(apiGoodsBalances).toString() + "}";
        } else {
            return "{\"statusCode\":300,\"msg\":\"请登录账号!\"}";
        }

    }


    /**
     * 添加购物车
     *
     * @param request
     * @param response
     * @param id       商品的id
     * @param count    商品的数量
     * @param gsp      商品的型号,例如:鞋子什么颜色,尺码,衣服什么尺寸,颜色等等,可以为空
     * @param buy_type 购买类型....combin 与其他商品组合销售
     */
    @RequestMapping(value = "/api/add_goods_cart.htm")
    @ResponseBody
    public String add_goods_cart(HttpServletRequest request, HttpServletResponse response, String id, String count,
                                 String gsp, String buy_type) {

        BigDecimal price;
        String cart_session_id = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("cart_session_id")) {
                    cart_session_id = CommUtil.null2String(cookie.getValue());
                }
            }
        }

        if (cart_session_id.equals("")) {
            cart_session_id = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("cart_session_id", cart_session_id);
            cookie.setDomain(CommUtil.generic_domain(request));
            response.addCookie(cookie);
        }
        List<StoreCart> cart = new ArrayList<StoreCart>();
        List<StoreCart> user_cart = new ArrayList<StoreCart>();
        List<StoreCart> cookie_cart = new ArrayList<StoreCart>();
        User user = (User) request.getSession(false).getAttribute("user");

        Map params = new HashMap();
        StoreCart sc;
        if (user != null) {
            if (!cart_session_id.equals("")) {
                if (user.getStore() != null) {
                    params.clear();
                    params.put("cart_session_id", cart_session_id);
                    params.put("user_id", user.getId());
                    params.put("sc_status", Integer.valueOf(0));
                    params.put("store_id", user.getStore().getId());
                    List store_cookie_cart = this.storeCartService.query(
                            "select obj from StoreCart obj where (obj.cart_session_id=:cart_session_id or obj.user.id=:user_id) and obj.sc_status=:sc_status and obj.store.id=:store_id",
                            params, -1, -1);
                    for (Iterator localIterator1 = store_cookie_cart.iterator(); localIterator1.hasNext(); ) {
                        sc = (StoreCart) localIterator1.next();
                        for (GoodsCart gc : sc.getGcs()) {
                            gc.getGsps().clear();
                            this.goodsCartService.delete(gc.getId());
                        }
                        this.storeCartService.delete(sc.getId());
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

        for (StoreCart sc12 : user_cart) {
            boolean sc_add = true;
            for (StoreCart sc1 : cart) {
                if (sc1.getStore().getId().equals(sc12.getStore().getId())) {
                    sc_add = false;
                }
            }
            if (sc_add) {
                cart.add(sc12);
            }
        }
        for (StoreCart sc11 : cookie_cart) {
            boolean sc_add = true;
            for (StoreCart sc1 : cart) {
                if (sc11.getStore().getId().equals(sc1.getStore().getId())) {
                    sc_add = false;
                    for (GoodsCart gc : sc1.getGcs()) {
                        gc.setSc(sc1);
                        this.goodsCartService.update(gc);
                    }
                    this.storeCartService.delete(sc1.getId());
                }
            }
            if (sc_add) {
                cart.add(sc11);
            }
        }

        String[] gsp_ids = gsp.split(",");
        Arrays.sort(gsp_ids);
        boolean add = true;
        double total_price = 0.0D;
        int total_count = 0;
        String[] gsp_ids1;
        for (StoreCart sc1 : cart)
            for (GoodsCart gc : sc1.getGcs())
                if ((gsp_ids != null) && (gsp_ids.length > 0) && (gc.getGsps() != null) && (gc.getGsps().size() > 0)) {
                    gsp_ids1 = new String[gc.getGsps().size()];
                    for (int i = 0; i < gc.getGsps().size(); i++) {
                        gsp_ids1[i] = (gc.getGsps().get(i) != null ? ((GoodsSpecProperty) gc.getGsps().get(i)).getId().toString() : "");
                    }
                    Arrays.sort(gsp_ids1);
                    if ((!gc.getGoods().getId().toString().equals(id)) || (!Arrays.equals(gsp_ids, gsp_ids1)))
                        continue;
                    add = false;
                    if (Arrays.equals(gsp_ids, gsp_ids1))
                        add = false;
                    return "{\"statusCode\":505,\"msg\":\"您的商品已添加进购物车,不需要重复添加!\"}";

                } else if (gc.getGoods().getId().toString().equals(id)) {
                    add = false;
                    return "{\"statusCode\":505,\"msg\":\"您的商品已添加进购物车,不需要重复添加!\"}";
                }

        Object obj;
        if (add) {
            Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
            String type = "save";
            StoreCart sc33 = new StoreCart();

            if (goods.getGroup_goods_list() != null & goods.getGroup_goods_list().size() > 0) {
                price = goods.getGroup_goods_list().get(0).getGg_price();
            } else if (goods.getBargainGoods_list() != null & goods.getBargainGoods_list().size() > 0) {
                price = goods.getBargainGoods_list().get(0).getBg_price();
            } else {
                price = goods.getStore_price();
            }

            for (StoreCart sc1 : cart) {
                if (sc1.getStore().getId().equals(goods.getGoods_store().getId())) {
                    sc33 = sc1;
                    type = "update";
                    break;
                }
            }
            sc33.setStore(goods.getGoods_store());
            if (((String) type).equals("save")) {
                sc33.setAddTime(new Date());
                this.storeCartService.save(sc33);
            } else {
                this.storeCartService.update(sc33);
            }

            obj = new GoodsCart();
            ((GoodsCart) obj).setAddTime(new Date());
            if (CommUtil.null2String(buy_type).equals("")) {
                ((GoodsCart) obj).setCount(CommUtil.null2Int(count));
                ((GoodsCart) obj).setPrice(price);
            }
            if (CommUtil.null2String(buy_type).equals("combin")) {
                ((GoodsCart) obj).setCount(1);
                ((GoodsCart) obj).setCart_type("combin");
                ((GoodsCart) obj).setPrice(goods.getCombin_price());
            }
            ((GoodsCart) obj).setGoods(goods);
            String spec_info = "";
            GoodsSpecProperty spec_property;
            for (String gsp_id : gsp_ids) {
                spec_property = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(gsp_id));
                ((GoodsCart) obj).getGsps().add(spec_property);
                if (spec_property != null) {
                    spec_info = spec_property.getSpec().getName() + ":" + spec_property.getValue() + " " + spec_info;
                }
            }
            ((GoodsCart) obj).setSc(sc33);
            ((GoodsCart) obj).setSpec_info(spec_info);
            this.goodsCartService.save((GoodsCart) obj);
            sc33.getGcs().add((GoodsCart) obj);

            double cart_total_price = 0.0D;

            for (GoodsCart gc1 : sc33.getGcs()) {
                // GoodsCart gc1 = (GoodsCart)((Iterator)???).next();
                if (CommUtil.null2String(gc1.getCart_type()).equals("")) {
                    /*cart_total_price = cart_total_price + CommUtil.null2Double(gc1.getGoods().getGoods_current_price()) * gc1.getCount();*/
                    cart_total_price = cart_total_price + CommUtil.null2Double(gc1.getPrice()) * gc1.getCount();
                }
                if (!CommUtil.null2String(gc1.getCart_type()).equals("combin"))
                    continue;
                cart_total_price = cart_total_price
                        + CommUtil.null2Double(gc1.getGoods().getCombin_price()) * gc1.getCount();
            }

            sc33.setTotal_price(BigDecimal.valueOf(CommUtil.formatMoney(Double.valueOf(cart_total_price))));
            if (user == null)
                sc33.setCart_session_id(cart_session_id);
            else {
                sc33.setUser(user);
            }
            if (((String) type).equals("save")) {
                sc33.setAddTime(new Date());
                this.storeCartService.save(sc33);
            } else {
                this.storeCartService.update(sc33);
            }
            boolean cart_add = true;
            for (StoreCart sc1 : cart) {
                if (sc1.getStore().getId().equals(sc33.getStore().getId())) {
                    cart_add = false;
                }
            }
            if (cart_add) {
                cart.add(sc33);
            }
        }
        for (Object type = cart.iterator(); ((Iterator) type).hasNext(); ) {
            StoreCart sc1 = (StoreCart) ((Iterator) type).next();

            total_count += sc1.getGcs().size();
            for (obj = sc1.getGcs().iterator(); ((Iterator) obj).hasNext(); ) {
                GoodsCart gc1 = (GoodsCart) ((Iterator) obj).next();

                total_price = total_price + CommUtil.mul(gc1.getPrice(), Integer.valueOf(gc1.getCount()));
            }
        }
        Map map = new HashMap();
        map.put("count", Integer.valueOf(total_count));
        map.put("total_price", Double.valueOf(total_price));

        List list = new ArrayList();
        list.add(map);
        if (list.size() > 0 && list != null) {
            return "{\"statusCode\":200,\"msg\":\"添加商品成功!\",\"data\":" + JSONArray.fromObject(list).toString() + "}";
        } else {
            return "{\"statusCode\":500,\"msg\":\"添加商品失败!\"}";
        }
    }


    /**
     * 从购物车删除商品
     *
     * @param request
     * @param goodsCart_id 商品在购物车里的id
     * @param store_id     店铺的id
     */
    @RequestMapping({"/api/remove_goods_cart.htm"})
    @ResponseBody
    public String remove_goods_cart(HttpServletRequest request, String goodsCart_id, String store_id) {

        GoodsCart gc = this.goodsCartService.getObjById(CommUtil.null2Long(goodsCart_id));
        StoreCart the_sc = gc.getSc();
        gc.getGsps().clear();

        this.goodsCartService.delete(CommUtil.null2Long(goodsCart_id));
        if (the_sc.getGcs().size() == 0) {
            this.storeCartService.delete(the_sc.getId());
        }
        List<StoreCart> cart = cart_calc(request);
        double total_price = 0.0D;
        double sc_total_price = 0.0D;
        double count = 0.0D;
        for (StoreCart sc2 : cart) {
            for (GoodsCart gc1 : sc2.getGcs()) {
                total_price = CommUtil.null2Double(gc1.getPrice()) * gc1.getCount() + total_price;
                count += 1.0D;
                if ((store_id == null) || (store_id.equals(""))
                        || (!sc2.getStore().getId().toString().equals(store_id)))
                    continue;
                sc_total_price = sc_total_price + CommUtil.null2Double(gc1.getPrice()) * gc1.getCount();
                sc2.setTotal_price(BigDecimal.valueOf(sc_total_price));
            }

            this.storeCartService.update(sc2);
        }
        request.getSession(false).setAttribute("cart", cart);
        Map map = new HashMap();
        map.put("count", Double.valueOf(count));
        map.put("total_price", Double.valueOf(total_price));
        map.put("sc_total_price", Double.valueOf(sc_total_price));

        if (map.isEmpty()) {
            return "{\"statusCode\":500,\"msg\":\"删除失败!\"}";
        } else {
            return "{\"statusCode\":200,\"msg\":\"删除成功!\",\"data\":" + JSONArray.fromObject(map).toString() + "}";
        }
    }


    /**
     * 购物车内-商品数量的变更
     *
     * @param request
     * @param cart_goods_id 购物车内-商品的id
     * @param count         商品的数量
     * @param store_id      店铺的id
     * @return
     */
    @RequestMapping({"/api/add_goods_count.htm"})
    @ResponseBody
    public String add_count(HttpServletRequest request, String cart_goods_id, String count, String store_id) {

        List<StoreCart> cart = cart_calc(request);

        double goods_total_price = 0.0D;
        String error = "100";
        Goods goods = null;
        String cart_type = "";
        GoodsCart gc;
        for (StoreCart sc : cart)
            for (Iterator localIterator2 = sc.getGcs().iterator(); localIterator2.hasNext(); ) {
                gc = (GoodsCart) localIterator2.next();
                if (gc.getId().toString().equals(cart_goods_id)) {
                    goods = gc.getGoods();
                    cart_type = CommUtil.null2String(gc.getCart_type());
                }
            }
        Object sc;
        if (cart_type.equals("")) {
            if (goods.getGroup_buy() == 2) {
                GroupGoods gg = new GroupGoods();
                for (GroupGoods gg1 : goods.getGroup_goods_list()) {
                    if (gg1.getGg_goods().equals(goods.getId())) {
                        gg = gg1;
                    }
                }
                if (gg.getGg_count() >= CommUtil.null2Int(count)) {
                    for (StoreCart sc1 : cart) {
                        for (int i = 0; i < ((StoreCart) sc1).getGcs().size(); i++) {
                            GoodsCart art = (GoodsCart) ((StoreCart) sc1).getGcs().get(i);
                            GoodsCart gc1 = art;
                            if (art.getId().toString().equals(cart_goods_id)) {
                                ((StoreCart) sc1).setTotal_price(BigDecimal.valueOf(CommUtil.add(((StoreCart) sc1).getTotal_price(),
                                        Double.valueOf((CommUtil.null2Int(count) - art.getCount()) *
                                                CommUtil.null2Double(art.getPrice())))));
                                art.setCount(CommUtil.null2Int(count));
                                gc1 = art;
                                ((StoreCart) sc1).getGcs().remove(art);
                                ((StoreCart) sc1).getGcs().add(gc1);
                                goods_total_price = CommUtil.null2Double(gc1.getPrice()) * gc1.getCount();
                                this.storeCartService.update((StoreCart) sc1);
                            }
                        }
                    }
                } else {
                    error = "300";
                }
            } else if (goods.getGoods_inventory() >= CommUtil.null2Int(count)) {
                for (StoreCart scart : cart) {
                    for (int i = 0; i < scart.getGcs().size(); i++) {
                        GoodsCart gcart = (GoodsCart) scart.getGcs().get(i);
                        GoodsCart gc1 = gcart;
                        if (gcart.getId().toString().equals(cart_goods_id)) {
                            scart.setTotal_price(BigDecimal.valueOf(CommUtil.add(scart.getTotal_price(),
                                    Double.valueOf((CommUtil.null2Int(count) - gcart.getCount()) *
                                            Double.parseDouble(gcart.getPrice().toString())))));
                            gcart.setCount(CommUtil.null2Int(count));
                            gc1 = gcart;
                            scart.getGcs().remove(gcart);
                            scart.getGcs().add(gc1);
                            goods_total_price = Double.parseDouble(gc1.getPrice().toString()) * gc1.getCount();
                            this.storeCartService.update(scart);
                        }
                    }
                }
            } else {
                error = "200";
            }
        }
        if (cart_type.equals("combin")) {
            if (goods.getGoods_inventory() >= CommUtil.null2Int(count))
                for (StoreCart sscart : cart) {
                    for (int i = 0; i < sscart.getGcs().size(); i++) {
                        gc = (GoodsCart) sscart.getGcs().get(i);
                        GoodsCart gc1 = (GoodsCart) gc;
                        if (((GoodsCart) gc).getId().toString().equals(cart_goods_id)) {
                            sscart.setTotal_price(BigDecimal.valueOf(CommUtil.add(sscart.getTotal_price(),
                                    Float.valueOf((CommUtil.null2Int(count) - ((GoodsCart) gc).getCount()) *
                                            CommUtil.null2Float(((GoodsCart) gc).getGoods().getCombin_price())))));
                            ((GoodsCart) gc).setCount(CommUtil.null2Int(count));
                            gc1 = (GoodsCart) gc;
                            sscart.getGcs().remove(gc);
                            sscart.getGcs().add(gc1);
                            goods_total_price = Double.parseDouble(gc1.getPrice().toString()) * gc1.getCount();
                            this.storeCartService.update(sscart);
                        }
                    }
                }
            else {
                error = "200";
            }
        }
        DecimalFormat df = new DecimalFormat("0.00");
        Object map = new HashMap();
        ((Map) map).put("count", count);
        for (StoreCart ssscart : cart) {
            if (ssscart.getStore().getId().equals(CommUtil.null2Long(store_id))) {
                ((Map) map).put("sc_total_price", Float.valueOf(CommUtil.null2Float(ssscart.getTotal_price())));
            }
        }
        ((Map) map).put("goods_total_price", Double.valueOf(df.format(goods_total_price)));
        ((Map) map).put("error", error);

        return JSONArray.fromObject(map).toString();
    }


    /**
     * 商品结算
     *
     * @param request
     * @param goods_id 商品id，结算多个商品时请按","分割
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/goods_balance.htm?goods_id=*
     */
    @RequestMapping({"/api/goods_balance.htm"})
    @ResponseBody
    public String confirmOrderForm(HttpServletRequest request, String goods_id) {

        User user = (User) request.getSession(false).getAttribute("user");
        List<ApiGoodsBalance> apiGoodsBalances = new ArrayList<ApiGoodsBalance>();
        List<ApiGoodsCart> apiGoodsCarts;
        List<ApiCouponInfo> apiCouponInfos = new ArrayList<ApiCouponInfo>();
        String url = CommUtil.getURL(request);
        ApiGoodsCart apiGoodsCart;
        ApiGoodsBalance apiGoodsBalance;
        boolean b = false;

        String img = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
                + this.configService.getSysConfig().getGoodsImage().getName();
        String[] ids = goods_id.split(",");

        if (user != null) {
            List<StoreCart> storeCarts = cart_calc(request);
            if (storeCarts != null) {
                for (StoreCart storeCart : storeCarts) {
                    List<GoodsCart> cartlist = storeCart.getGcs();
                    apiGoodsBalance = new ApiGoodsBalance();
                    BigDecimal total_price = new BigDecimal(0);
                    apiGoodsCarts = new ArrayList<ApiGoodsCart>();
                    int count = 0;
                    if (cartlist != null && cartlist.size() > 0) {
                        for (GoodsCart goodsCart : cartlist) {

                            for (int i = 0; i < ids.length; i++) {
                                if (CommUtil.null2Long(ids[i]).longValue() == goodsCart.getId().longValue()) {
                                    b = true;
                                    break;
                                }
                            }

                            if (b) {
                                if (goodsCart.getGoods().getGoods_main_photo() != null) {
                                    img = url + "/" + goodsCart.getGoods().getGoods_main_photo().getPath() + "/"
                                            + goodsCart.getGoods().getGoods_main_photo().getName();
                                }

                                BigDecimal price = goodsCart.getPrice().multiply(new BigDecimal(goodsCart.getCount()));
                                count += goodsCart.getCount();
                                total_price = total_price.add(price);
                                apiGoodsCart = new ApiGoodsCart();
                                apiGoodsCart.setGoods_id(goodsCart.getGoods().getId());
                                apiGoodsCart.setGoods_name(goodsCart.getGoods().getGoods_name());
                                apiGoodsCart.setCount(goodsCart.getCount());
                                apiGoodsCart.setStore_price(goodsCart.getGoods().getStore_price());
                                apiGoodsCart.setGoods_price(goodsCart.getGoods().getGoods_price());
                                apiGoodsCart.setPrice(price);
                                apiGoodsCart.setSpec_info(goodsCart.getSpec_info());
                                apiGoodsCart.setGoods_img(img);
                                apiGoodsCart.setStore_id(goodsCart.getSc().getStore().getId());
                                apiGoodsCarts.add(apiGoodsCart);
                            }
                            b = false;
                        }
                    }

                    if (apiGoodsCarts != null && apiGoodsCarts.size() > 0) {
                        apiGoodsBalance.setStore_id(storeCart.getStore().getId());
                        apiGoodsBalance.setStore_name(storeCart.getStore().getStore_name());
                        apiGoodsBalance.setCount(count);
                        apiGoodsBalance.setStoreCart_id(storeCart.getId());
                        apiGoodsBalance.setTotal_price(total_price);
                        apiGoodsBalance.setGcs(apiGoodsCarts);
                        apiGoodsBalances.add(apiGoodsBalance);
                    }
                }
            }

            if (apiGoodsBalances != null && apiGoodsBalances.size() > 0) {

                Map params = new HashMap();
                ApiCouponInfo apiCouponInfo;

                params.put("user_id", user.getId());
                params.put("coupon_begin_time", new Date());
                params.put("coupon_end_time", new Date());
                params.put("status", Integer.valueOf(0));
                List<CouponInfo> couponinfos = this.couponInfoService.query(
                        "select obj from CouponInfo obj where obj.status=:status and obj.user.id=:user_id and obj.coupon.coupon_begin_time<=:coupon_begin_time and obj.coupon.coupon_end_time>=:coupon_end_time",
                        params, -1, -1);

                for (int i = 0; i < couponinfos.size(); i++) {
                    apiCouponInfo = new ApiCouponInfo();
                    apiCouponInfo.setCoupon_order_amount(couponinfos.get(i).getCoupon().getCoupon_order_amount());
                    apiCouponInfo.setCoupon_id(couponinfos.get(i).getId());
                    apiCouponInfo.setCoupon_sn(couponinfos.get(i).getCoupon_sn());
                    apiCouponInfo.setCoupon_name(couponinfos.get(i).getCoupon().getCoupon_name());
                    apiCouponInfo.setCoupon_amount(couponinfos.get(i).getCoupon().getCoupon_amount());
                    apiCouponInfo.setCoupon_begin_time(CommUtil.formatLongDate(couponinfos.get(i).getCoupon().getCoupon_begin_time()));
                    apiCouponInfo.setCoupon_end_time(CommUtil.formatLongDate(couponinfos.get(i).getCoupon().getCoupon_end_time()));
                    apiCouponInfos.add(apiCouponInfo);
                }
                String cart_session = CommUtil.randomString(32);
                request.getSession(false).setAttribute("cart_session", cart_session);

                return "{\"statusCode\":\"200\",\"msg\":\"加载成功！\",\"cart_session\":\"" + cart_session + "\"," +
                        "\"coupon\":" + JSONArray.fromObject(apiCouponInfos).toString() + "," +
                        "\"data\":" + JSONArray.fromObject(apiGoodsBalances).toString() + "}";
            } else {
                return "{\"statusCode\":\"201\",\"msg\":\"购物车信息为空！\"}";
            }
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }


    /**
     * 确认下单支付
     *
     * @param request
     * @param data    数据格式如下
     *                {
     *                "addr_id": 1,               收货地址
     *                "total_price": 1,           总价
     *                "goods_id":"",              商品id
     *                "cart_session": "",         订单信息
     *                "data": [
     *                {
     *                "store_id": 1,              店铺id
     *                "invoice_type": 1,          发票类型
     *                "coupon_id": 1,             优惠券id
     *                "invoice":"",               发票抬头
     *                "transport": "",            配送方式
     *                "ship_price": 1,            运费
     *                "price":1,                  商品价格
     *                "msg": ""                   留言
     *                },
     *                {
     *                ....
     *                }
     *                ]
     *                }
     * @apiNote http://192.168.1.223:8080/shopping/api/confirm_order.htm?data=*
     */
    @RequestMapping({"/api/confirm_order.htm"})
    @ResponseBody
    public String order_right_now(HttpServletRequest request, String data) {

        User user = (User) request.getSession(false).getAttribute("user");

        if (user != null) {
            System.out.println("数据=====" + data);
            ApiOrderData apiOrderData = com.alibaba.fastjson.JSON.parseObject(data, ApiOrderData.class);
            String cart_session1 = (String) request.getSession(false).getAttribute("cart_session");
            List<StoreCart> cart = cart_calc(request);

            long addr_id = CommUtil.null2Long(apiOrderData.getAddr_id());
            BigDecimal total_price = new BigDecimal(apiOrderData.getTotal_price());
            String cart_session = apiOrderData.getCart_session();
            String[] ids = apiOrderData.getGoods_id().split(",");
            String order_id = "";

            if (!apiOrderData.getAddr_id().equals("") && apiOrderData.getAddr_id() != null) {
                if (cart != null) {
                    if (CommUtil.null2String(cart_session1).equals(cart_session)) {
                        if (apiOrderData.getData() != null && apiOrderData.getData().size() > 0) {
                            for (int i = 0; i < apiOrderData.getData().size(); i++) {

                                WebForm wf = new WebForm();
                                OrderForm of = (OrderForm) wf.toPo(request, OrderForm.class);
                                of.setAddTime(new Date());

                                of.setInvoiceType(Integer.parseInt(apiOrderData.getData().get(i).getInvoice_type()));
                                of.setInvoice(apiOrderData.getData().get(i).getInvoice());
                                of.setTransport(apiOrderData.getData().get(i).getTransport());
                                of.setShip_price(new BigDecimal(apiOrderData.getData().get(i).getShip_price()));
                                of.setGoods_amount(new BigDecimal(apiOrderData.getData().get(i).getPrice()));
                                of.setMsg(apiOrderData.getData().get(i).getMsg());

                                of.setOrder_id(user.getId() + CommUtil.formatTime("yyyyMMddHHmmss", new Date()) + CommUtil.randomInt(5));
                                Address addr = this.addressService.getObjById(CommUtil.null2Long(addr_id));
                                of.setAddr(addr);
                                of.setOrder_status(10);
                                of.setUser(user);
                                of.setStore(this.storeService.getObjById(CommUtil.null2Long(apiOrderData.getData().get(i).getStore_id())));
                                of.setTotalPrice(BigDecimal.valueOf(CommUtil.add(of.getGoods_amount(), of.getShip_price())));
                                if (!"".equals(CommUtil.null2String(apiOrderData.getData().get(i).getCoupon_id())) && !"0".equals(CommUtil.null2String(apiOrderData.getData().get(i).getCoupon_id()))) {
                                    CouponInfo ci = this.couponInfoService.getObjById(CommUtil.null2Long(apiOrderData.getData().get(i).getCoupon_id()));
                                    ci.setStatus(1);
                                    this.couponInfoService.update(ci);
                                    of.setCi(ci);
                                    of.setTotalPrice(BigDecimal.valueOf(CommUtil.subtract(of.getTotalPrice(), ci.getCoupon().getCoupon_amount())));
                                }

                                System.out.println("价格对比：" + total_price + "===" + of.getTotalPrice());

                                of.setOrder_type("app");
                                this.orderFormService.save(of);
                                BigDecimal price;
                                BigDecimal total_amount;
                                for (StoreCart sc : cart) {
                                    if (sc.getStore().getId().toString().equals(apiOrderData.getData().get(i).getStore_id())) {
                                        price = new BigDecimal(0);
                                        total_amount = new BigDecimal(0);
                                        for (GoodsCart goodsCart : sc.getGcs()) {
                                            for (int j = 0; j < ids.length; j++) {
                                                if (CommUtil.null2Long(ids[j]).longValue() == goodsCart.getId().longValue()) {
                                                    goodsCart.setOf(of);
                                                    this.goodsCartService.update(goodsCart);
                                                    price = price.add(goodsCart.getPrice());
                                                    break;
                                                }
                                            }
                                        }
                                        if (sc.getTotal_price().subtract(price).compareTo(BigDecimal.ZERO) == 0) {
                                            sc.setSc_status(1);
                                            sc.setCart_session_id(null);
                                            sc.setUser(user);
                                            Map params = new HashMap();
                                            params.put("sc_id", sc.getId());
                                            List<GoodsCart> goodsCarts = this.goodsCartService.query(
                                                    "select obj from GoodsCart obj where obj.sc.id=:sc_id", params, -1, -1);
                                            for (int j = 0; j < goodsCarts.size(); j++) {
                                                total_amount = total_amount.add(goodsCarts.get(j).getPrice());
                                            }
                                            sc.setTotal_price(total_amount);
                                        } else {
                                            sc.setTotal_price(sc.getTotal_price().subtract(price));
                                        }
                                        this.storeCartService.update(sc);
                                    }
                                }

                                order_id += String.valueOf(of.getId()).trim() + ",";
                                OrderFormLog ofl = new OrderFormLog();
                                ofl.setAddTime(new Date());
                                ofl.setOf(of);
                                ofl.setLog_info("提交订单");
                                ofl.setLog_user(user);
                                this.orderFormLogService.save(ofl);
                            }
                            return "{\"statusCode\":\"200\",\"msg\":\"下单成功\",\"   \":\"" + order_id.substring(0, order_id.length() - 1) + "\"}";
                        } else {
                            return "{\"statusCode\":\"201\",\"msg\":\"订单未选择商品\"}";
                        }
                    } else {
                        return "{\"statusCode\":\"202\",\"msg\":\"订单已经失效\"}";
                    }
                } else {
                    return "{\"statusCode\":\"203\",\"msg\":\"订单信息错误\"}";
                }
            } else {
                return "{\"statusCode\":\"204\",\"msg\":\"请选择收货地址！\"}";
            }
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }


}
