package com.shopping.view.app.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.shopping.view.web.tools.DateJsonValueProcessor;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Advert;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.IntegralGoods;
import com.shopping.foundation.domain.IntegralGoodsCart;
import com.shopping.foundation.domain.IntegralGoodsOrder;
import com.shopping.foundation.domain.IntegralLog;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.api.ApiAddress;
import com.shopping.foundation.domain.api.ApiArea;
import com.shopping.foundation.domain.api.ApiIntegralGoods;
import com.shopping.foundation.domain.api.ApiUser;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAddressService;
import com.shopping.foundation.service.IAdvertService;
import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.IIntegralGoodsCartService;
import com.shopping.foundation.service.IIntegralGoodsOrderService;
import com.shopping.foundation.service.IIntegralGoodsService;
import com.shopping.foundation.service.IIntegralLogService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.foundation.service.impl.AccessoryService;
import com.shopping.manage.admin.tools.PaymentTools;

import net.sf.json.JSONArray;

@Controller
public class ApiIntegralGoodsViewAction {

    @Autowired
    private IIntegralGoodsService integralGoodsService;
    @Autowired
    private IIntegralGoodsOrderService integralGoodsOrderService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IAccessoryService accessoryService;
    @Autowired
    private IAdvertService advertService;
    @Autowired
    private IIntegralGoodsCartService igcService;
    @Autowired
    private IIntegralGoodsOrderService igoService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IAreaService areaService;
    @Autowired
    private PaymentTools paymentTools;
    @Autowired
    private IIntegralLogService integralLogService;


    /**
     * 积分商品列表显示功能
     *
     * @param request
     * @param response
     * @param begin    起始积分数
     * @param end      结束积分数
     * @return
     */
    @RequestMapping({"/api/integral.htm"})
    @ResponseBody
    public String integralView(HttpServletRequest request, HttpServletResponse response, String begin, String end,
                               String currentPage, String pageSize) {

        int x = CommUtil.null2Int(currentPage);
        int y = CommUtil.null2Int(pageSize);
        int size;
        int pages = 0;
        List<IntegralGoods> recommend_igs = new ArrayList<IntegralGoods>();
        Map params = new HashMap();
        params.put("recommend", Boolean.valueOf(true));
        params.put("show", Boolean.valueOf(true));
        List list = new ArrayList();

        String url = CommUtil.getURL(request);
        if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
            url = this.configService.getSysConfig().getImageWebServer();
        }
        if ((begin != null) && (!begin.equals("")) && (end != null) && (!end.equals(""))) {
            if (end.equals("0")) {
                params.put("begin", Integer.valueOf(CommUtil.null2Int(begin)));
                recommend_igs = this.integralGoodsService
                        .query("select obj from IntegralGoods obj where obj.ig_recommend=:recommend and obj.ig_show=:show and obj.ig_goods_integral>=:begin order by obj.ig_sequence asc",
                                params, -1, -1);
                size = recommend_igs.size();
                if (size > 0) {
                    pages = size / y;
                    if (size % y != 0) {
                        pages++;
                    }
                    int begin2 = (x - 1) * y;
                    int end2 = x * y;
                    if (begin2 < size) {
                        if (end2 > size) {
                            end2 = size;
                        }
                        for (int i = begin2; i < end2; i++) {
                            Map map = new HashMap();
                            map.put("igs_id", recommend_igs.get(i).getId());
                            map.put("igs_name", recommend_igs.get(i).getIg_goods_name());
                            map.put("igs_integral", recommend_igs.get(i).getIg_goods_integral());
                            String image = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
                                    + this.configService.getSysConfig().getGoodsImage().getName();
                            if (recommend_igs.get(i).getIg_goods_img() != null) {
                                image = url + "/" + recommend_igs.get(i).getIg_goods_img().getPath() + "/"
                                        + recommend_igs.get(i).getIg_goods_img().getName() + "_small." + recommend_igs.get(i).getIg_goods_img().getExt();
                                map.put("image", image);
                            }
                            list.add(map);
                        }
                    }
                }
            } else {
                params.put("begin", Integer.valueOf(CommUtil.null2Int(begin)));
                params.put("end", Integer.valueOf(CommUtil.null2Int(end)));
                recommend_igs = this.integralGoodsService
                        .query("select obj from IntegralGoods obj where obj.ig_recommend=:recommend and obj.ig_show=:show and obj.ig_goods_integral>=:begin and obj.ig_goods_integral<:end order by obj.ig_sequence asc",
                                params, -1, -1);
                size = recommend_igs.size();
                if (size > 0) {
                    pages = size / y;
                    if (size % y != 0) {
                        pages++;
                    }
                    int begin2 = (x - 1) * y;
                    int end2 = x * y;
                    if (begin2 < size) {
                        if (end2 > size) {
                            end2 = size;
                        }
                        for (int i = begin2; i < end2; i++) {
                            Map map = new HashMap();
                            map.put("igs_id", recommend_igs.get(i).getId());
                            map.put("igs_name", recommend_igs.get(i).getIg_goods_name());
                            map.put("igs_integral", recommend_igs.get(i).getIg_goods_integral());
                            String image = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
                                    + this.configService.getSysConfig().getGoodsImage().getName();
                            if (recommend_igs.get(i).getIg_goods_img() != null) {
                                image = url + "/" + recommend_igs.get(i).getIg_goods_img().getPath() + "/"
                                        + recommend_igs.get(i).getIg_goods_img().getName() + "_small." + recommend_igs.get(i).getIg_goods_img().getExt();
                                map.put("image", image);
                            }
                            list.add(map);
                        }
                    }
                }
            }
        } else {
            recommend_igs = this.integralGoodsService
                    .query("select obj from IntegralGoods obj where obj.ig_recommend=:recommend and obj.ig_show=:show order by obj.ig_sequence asc",
                            params, -1, -1);
            size = recommend_igs.size();
            if (size > 0) {
                pages = size / y;
                if (size % y != 0) {
                    pages++;
                }
                int begin2 = (x - 1) * y;
                int end2 = x * y;
                if (begin2 < size) {
                    if (end2 > size) {
                        end2 = size;
                    }
                    for (int i = begin2; i < end2; i++) {
                        Map map = new HashMap();
                        map.put("igs_id", recommend_igs.get(i).getId());
                        map.put("igs_name", recommend_igs.get(i).getIg_goods_name());
                        map.put("igs_integral", recommend_igs.get(i).getIg_goods_integral());
                        //Accessory accessory = this.accessoryService.getObjById(CommUtil.null2Long(igs.getIg_goods_img()));
                        String image = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
                                + this.configService.getSysConfig().getGoodsImage().getName();
                        if (recommend_igs.get(i).getIg_goods_img() != null) {
                            image = url + "/" + recommend_igs.get(i).getIg_goods_img().getPath() + "/"
                                    + recommend_igs.get(i).getIg_goods_img().getName() + "_small." + recommend_igs.get(i).getIg_goods_img().getExt();
                            map.put("image", image);
                        }
                        list.add(map);
                    }
                }
            }
        }
        String data = Json.toJson(list, JsonFormat.compact());
        return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":" + data + ",\"currentPage\":" + x + ","
                + "\"pages\":" + pages + ",\"rowCount\":" + size + "}";

    }

    /**
     * 用户的积分显示--暂停使用
     * @param request
     * @param response
     * @return
     */
    /*@RequestMapping({"/api/userIntegral.htm"})
    @ResponseBody
	public String userInegral(HttpServletRequest request,HttpServletResponse response){
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		
		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		List list = new ArrayList();
		if(user==null){
			return "{\"statusCode\":500,\"msg\":\"请登录账号!\"}";
		}else{
			Map map = new HashMap();
			map.put("userIntegral", user.getIntegral());
			list.add(map);
			String data = JSON.toJSONString(list);
			return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":"+data+"}";
		}
	}*/


    /**
     * 积分商品的详情
     *
     * @param request
     * @param response
     * @param ig_id    积分商品的id
     * @return
     */
    @RequestMapping({"/api/integralGoodsDetail.htm"})
    @ResponseBody
    public String integralGoodsDetail(HttpServletRequest request, HttpServletResponse response,
                                      String ig_id) {

        String url = CommUtil.getURL(request);
        if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
            url = this.configService.getSysConfig().getImageWebServer();
        }
        IntegralGoods ig = this.integralGoodsService.getObjById(CommUtil.null2Long(ig_id));
        List list = new ArrayList();
        Map map = new HashMap();
        String image = url + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
                + this.configService.getSysConfig().getGoodsImage().getName();
        if (ig.getIg_goods_img() != null) {
            image = url + "/" + ig.getIg_goods_img().getPath() + "/"
                    + ig.getIg_goods_img().getName();
        }
        map.put("ig_id", ig.getId());
        map.put("ig_name", ig.getIg_goods_name());
        map.put("ig_goods_integral", ig.getIg_goods_integral());
        map.put("image", image);
        list.add(map);
        if (list != null && list.size() > 0) {
            return "{\"statusCode\":200,\"msg\":\"数据加载成功!\",\"data\":" + JSON.toJSONString(map) + "}";
        } else {
            return "{\"statusCode\":500,\"msg\":\"数据请求失败!\"}";
        }
    }


    /**
     * 积分商品兑换第一步和第二步合并成这一步.
     *
     * @param request
     * @param response
     * @param id             商品id
     * @param exchange_count 兑换数量
     * @return
     */
    @RequestMapping({"/api/integralExchange.htm"})
    @ResponseBody
    public String integralExchange(HttpServletRequest request, HttpServletResponse response,
                                   String id, String exchange_count) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "{\"statusCode\":300,\"msg\":\"请登录账户 ! \"}";
        } else {
            Map<String, Object> map = new HashMap<String, Object>();
            if (this.configService.getSysConfig().isIntegralStore()) {
                IntegralGoods obj = this.integralGoodsService.getObjById(CommUtil.null2Long(id));

                int exchange_status = 0;
                if (obj != null) {
                    if ((exchange_count == null) || (exchange_count.equals(""))) {
                        exchange_count = "1";
                    }
                    if (obj.getIg_goods_count() < CommUtil.null2Int(exchange_count)) {
                        exchange_status = -1;
                        //map.put("op_title", "库存数量不足,请重新选择兑换数量");
                        return "{\"statusCode\":201,\"msg\":\"库存数量不足,请重新选择兑换数量\"}";
                    }
                    if (obj.isIg_limit_type()) {
                        if (obj.getIg_limit_count() < CommUtil.null2Int(exchange_count)) {
                            exchange_status = -2;
                            //map.put("op_title", "限制最多兑换"+obj.getIg_limit_count()+",重新选择兑换数量");
                            return "{\"statusCode\":201,\"msg\":\"限制最多兑换" + obj.getIg_limit_count() + ",重新选择兑换数量\"}";
                        }
                    }
                    int cart_total_integral = obj.getIg_goods_integral() * CommUtil.null2Int(exchange_count);
                    if (user.getIntegral() < cart_total_integral) {
                        exchange_status = -3;
                        //map.put("op_title", "您的积分不足");
                        return "{\"statusCode\"201,\"msg\":\"您的积分不足\"}";
                    }
                    if ((obj.getIg_begin_time() != null) && (obj.getIg_end_time() != null)
                            && (obj.getIg_begin_time().after(new Date()) ||
                            (obj.getIg_end_time().before(new Date())))) {
                        exchange_status = -4;
                        //map.put("op_title", "兑换已经过期");
                        return "{\"statusCode\":201,\"msg\":\"兑换已经过期\"}";
                    }
                }
                if (exchange_status == 0) {
                    List<IntegralGoodsCart> integral_goods_cart = (List) session.getAttribute("integral_goods_cart");
                    if (integral_goods_cart == null) {
                        integral_goods_cart = new ArrayList();
                    }
                    boolean add = obj != null;
                    for (IntegralGoodsCart igc : integral_goods_cart) {
                        if (igc.getGoods().getId().toString().equals(id)) {
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        IntegralGoodsCart gc = new IntegralGoodsCart();
                        gc.setAddTime(new Date());
                        gc.setCount(CommUtil.null2Int(exchange_count));
                        gc.setGoods(obj);
                        gc.setTrans_fee(obj.getIg_transfee());
                        gc.setIntegral(obj.getIg_goods_integral() * CommUtil.null2Int(exchange_count));
                        integral_goods_cart.add(gc);
                    }
                    session.setAttribute("integral_goods_cart", integral_goods_cart);
                }
            } else {
                map.put("op_title", "系统未开启积分商城");
                return "{\"statusCode\":201,\"msg\":\"系统未开启积分商城\"}";
            }

            int size = 0;
            List<IntegralGoodsCart> igc_list = (List) session.getAttribute("integral_goods_cart");
            size = igc_list.size();
            int total_integral = 0;
            double trans_fee = 0.0D;
            List integralGoodsCart_list = new ArrayList();
            for (IntegralGoodsCart igc : igc_list) {
                total_integral += igc.getIntegral();
                trans_fee += CommUtil.null2Double(igc.getTrans_fee());

                Map igc_map = new HashMap();
                igc_map.put("igc__goods_name", igc.getGoods().getIg_goods_name());
                igc_map.put("igc__goods_id", igc.getGoods().getId());
                igc_map.put("igc_goods_integral", igc.getIntegral());
                igc_map.put("igc_count", igc.getCount());
                igc_map.put("igc_goods_trans_fee", igc.getTrans_fee());
                igc_map.put("image", CommUtil.getURL(request) + "/" + igc.getGoods().getIg_goods_img().getPath() + "/" +
                        igc.getGoods().getIg_goods_img().getName());
                integralGoodsCart_list.add(igc_map);
            }
            map.put("igc", integralGoodsCart_list);
            if (igc_list != null) {
                map.put("total_integral", total_integral);
                map.put("trans_fee", trans_fee);
                String integral_order_session = CommUtil.randomString(32);
                map.put("integral_order_session", integral_order_session);
                request.getSession(false).setAttribute("integral_order_session", integral_order_session);
            } else {
                map.put("op_title", "兑换购物车为空!");
            }

            return "{\"statusCode\":200,\"msg\":\"数据加载成功!\",\"data\":" + JSON.toJSONString(map) + ","
                    + "\"rowCount\":" + size + "}";
        }
    }


    /**
     * 积分购物车里--积分商品的删除功能
     *
     * @param request
     * @param response
     * @param id       积分商品的id
     * @return
     */
    @RequestMapping({"/api/integral_remove.htm"})
    @ResponseBody
    public String integralRemove(HttpServletRequest request, HttpServletResponse response,
                                 String id) {

        HttpSession session = request.getSession(false);
        List<IntegralGoodsCart> igc_list = (List) session.getAttribute("integral_goods_cart");
        for (IntegralGoodsCart igc : igc_list) {
            if (igc.getGoods().getId().toString().equals(id)) {
                igc_list.remove(igc);
                break;
            }
        }
        int total_integral = 0;
        for (IntegralGoodsCart igc : igc_list) {
            total_integral += igc.getIntegral();
        }
        session.setAttribute("integral_goods_cart", igc_list);
        Object map = new HashMap();
        ((Map) map).put("total_integral", Integer.valueOf(total_integral));
        ((Map) map).put("size", Integer.valueOf(igc_list.size()));
        ((Map) map).put("status", Integer.valueOf(100));
        return "{\"statusCode\":200,\"msg\":\"删除成功!\",\"data\":" + JSON.toJSONString(map) + "}";
    }


    /**
     * 积分商品兑换--更改商品兑换数量
     *
     * @param request
     * @param response
     * @param id       积分商品的id
     * @param count    想要兑换的数量
     * @return
     */
    @RequestMapping({"/api/add_integral_count.htm"})
    @ResponseBody
    public String add_integral_count(HttpServletRequest request, HttpServletResponse response,
                                     String id, String count) {

        List<IntegralGoodsCart> igc_list = (List) request.getSession(false).getAttribute("integral_goods_cart");
        IntegralGoodsCart obj = null;
        int num = CommUtil.null2Int(count);
        IntegralGoods ig;
        for (IntegralGoodsCart igc : igc_list) {
            if (igc.getGoods().getId().toString().equals(id)) {
                ig = igc.getGoods();
                if (num > ig.getIg_goods_count()) {
                    num = ig.getIg_goods_count();
                }
                if ((ig.isIg_limit_type()) && (num > ig.getIg_limit_count())) {
                    num = ig.getIg_limit_count();
                }
                igc.setCount(num);
                igc.setIntegral(ig.getIg_goods_integral() * CommUtil.null2Int(Integer.valueOf(num)));
                obj = igc;
                break;
            }
        }
        int total_integral = 0;
        for (IntegralGoodsCart igc : igc_list) {
            total_integral += igc.getIntegral();
        }
        request.getSession(false).setAttribute("integral_goods_cart", igc_list);
        Object map = new HashMap();
        ((Map) map).put("total_integral", Integer.valueOf(total_integral));
        ((Map) map).put("integral", Integer.valueOf(obj.getIntegral()));
        ((Map) map).put("count", Integer.valueOf(num));
        return "{\"statusCode\":200,\"msg\":\"添加数量成功!\",\"data\":" + JSON.toJSONString(map) + "}";
    }


    /**
     * 积分兑换第三步
     *
     * @param request
     * @param response
     * @param addr_id                收货地址的id
     * @param igo_msg                留言
     * @param integral_order_session 在第二步中存入session的order值.32位的
     * @return
     */
    @RequestMapping({"/api/integralExchangeThree.htm"})
    @ResponseBody
    public String integralExchangeThree(HttpServletRequest request, HttpServletResponse response,
                                        String addr_id, String igo_msg, String integral_order_session) {

        Map<String, Object> map = new HashMap<String, Object>();
        if (this.configService.getSysConfig().isIntegralStore()) {
            List<IntegralGoodsCart> igc_list = (List) request.getSession(false).getAttribute("integral_goods_cart");
            String integral_order_session1 = CommUtil.null2String(request.getSession(false).getAttribute("integral_order_session"));
            if (integral_order_session1.equals("")) {
                map = new HashMap<String, Object>();
                map.put("op_title", "表单已经过期!");
            } else if (integral_order_session.equals(integral_order_session.trim())) {
                if (igc_list != null) {
                    int total_integral = 0;
                    double trans_fee = 0.0D;
                    for (IntegralGoodsCart igc : igc_list) {
                        total_integral += igc.getIntegral();
                        trans_fee += CommUtil.null2Double(igc.getTrans_fee());
                    }
                    IntegralGoodsOrder order = new IntegralGoodsOrder();
                    Address address = this.addressService.getObjById(CommUtil.null2Long(addr_id));
                    order.setAddTime(new Date());
                    order.setIgo_msg(igo_msg);
                    order.setIgo_addr(address);
                    order.setIgo_gcs(igc_list);
                    User user = (User) request.getSession(false).getAttribute("user");
                    order.setIgo_order_sn("igo" + CommUtil.formatTime("yyyyMMddHHmmss", new Date()) + user.getId());
                    order.setIgo_user(user);
                    order.setIgo_trans_fee(BigDecimal.valueOf(trans_fee));
                    order.setIgo_total_integral(total_integral);
                    for (IntegralGoodsCart igc : igc_list) {
                        igc.setOrder(order);
                    }
                    if (trans_fee == 0.0D) {
                        order.setIgo_status(20);
                        order.setIgo_pay_time(new Date());
                        order.setIgo_payment("no_fee");
                        this.integralGoodsOrderService.save(order);
                        for (IntegralGoodsCart igc : order.getIgo_gcs()) {
                            IntegralGoods goods = igc.getGoods();
                            goods.setIg_goods_count(goods.getIg_goods_count() - igc.getCount());
                            goods.setIg_exchange_count(goods.getIg_exchange_count() + igc.getCount());
                            this.integralGoodsService.update(goods);
                        }
                        request.getSession(false).removeAttribute("integral_goods_cart");
                        //map.put("order", order);
                        map.put("igo_order_sn", order.getIgo_order_sn());
                    } else {
                        order.setIgo_status(0);
                        this.integralGoodsOrderService.save(order);
                        map = new HashMap<String, Object>();
                        //map.put("obj", order);
                        map.put("igo_order_sn", order.getIgo_order_sn());
                        map.put("paymentTools", this.paymentTools);
                    }
                    user.setIntegral(user.getIntegral() - order.getIgo_total_integral());
                    this.userService.update(user);

                    IntegralLog log = new IntegralLog();
                    log.setAddTime(new Date());
                    log.setContent("兑换商品消耗积分!");
                    log.setIntegral(order.getIgo_total_integral());
                    log.setIntegral_user(user);
                    log.setType("integral_order");
                    this.integralLogService.save(log);
                    request.getSession(false).removeAttribute("integral_goods_cart");
                    ;
                } else {
                    map = new HashMap<String, Object>();
                    map.put("op_title", "兑换购物车为空!");
                }
            } else {
                map = new HashMap<String, Object>();
                map.put("op_title", "参数错误,提交表单失败!");
            }
        } else {
            map.put("op_title", "系统未开启积分商城!");
        }
        return "{\"statusCode\":200,\"msg\":\"数据加载成功!\",\"data\":" + JSON.toJSONString(map) + "}";
    }


    /**
     * 积分商品兑换记录
     *
     * @param request
     * @param response
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping({"/api/integralExchangeRecord.htm"})
    @ResponseBody
    public String integralExchangeRecord(HttpServletRequest request, HttpServletResponse response
            , String currentPage, String pageSize) {

        String url = CommUtil.getURL(request);
        if (!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))) {
            url = this.configService.getSysConfig().getImageWebServer();
        }

        List order_list = new ArrayList();
        int x = CommUtil.null2Int(currentPage);
        int y = CommUtil.null2Int(pageSize);
        int size;
        int pages = 0;
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        ApiIntegralGoods aigoods;
        Map params = new HashMap();
        params.put("user_id", user.getId());
        List<IntegralGoodsOrder> igc_list =
                this.igoService.query("select obj from IntegralGoodsOrder obj where obj.igo_user.id=:user_id order by obj.addTime desc",
                        params, -1, -1);
        size = igc_list.size();
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
                    List list = new ArrayList();
                    for (IntegralGoodsCart igc : igc_list.get(i).getIgo_gcs()) {
                        aigoods = new ApiIntegralGoods();
                        aigoods.setGoods_id(igc.getGoods().getId());
                        aigoods.setGoods_order_id(igc_list.get(i).getId());
                        aigoods.setGoods_order_sn(igc_list.get(i).getIgo_order_sn());
                        aigoods.setGoods_name(igc.getGoods().getIg_goods_name());
                        aigoods.setGoods_count(igc.getCount());
                        aigoods.setGoods_integral(igc.getIntegral());
                        aigoods.setImage(CommUtil.getURL(request) + "/" + igc.getGoods().getIg_goods_img().getPath() +
                                "/" + igc.getGoods().getIg_goods_img().getName());
                        list.add(aigoods);
                    }
                    map.put("order_id", igc_list.get(i).getId());
                    map.put("order_sn", igc_list.get(i).getIgo_order_sn());
                    map.put("order_status", igc_list.get(i).getIgo_status());
                    map.put("addTime", CommUtil.formatTime("yyyy-MM-dd HH:mm:ss", igc_list.get(i).getAddTime()));
                    map.put("payment", igc_list.get(i).getIgo_payment());
                    map.put("trans_fee", igc_list.get(i).getIgo_trans_fee());
                    map.put("integral_goods", list);
                    order_list.add(map);
                }
            }
        }
        return "{\"statusCode\":200,\"msg\":\"兑换记录查询成功!\",\"data\":" + JSONArray.fromObject(order_list) + ","
                + "\"pages\":" + pages + ",\"currentPage\":" + x + ",\"rowCount\":" + size + "}";
    }


    /**
     * 积分兑换--兑换详情
     * igo_status==-1:  "已取消"
     * igo_status==0:   "待付款"
     * igo_status==10:  "线下支付待审核"
     * igo_status==20:  "已付款，待发货"
     * igo_status==30:	"已发货"
     * igo_status==40:	 "已收货完成"
     * <p>
     * igo_payment=="alipay" : 	"支付宝"
     * igo_payment=="tenpay" :	 "财付通"
     * igo_payment=="bill"	:	"快钱"
     * igo_payment=="chinabank"	:	"网银在线"
     * igo_payment=="outline"	:	"线下支付"
     * igo_payment=="balance"	:	"预存款支付"
     * igo_payment=="no_fee"	:	"无运费订单"
     * igo_payment		:		"未支付"
     *
     * @param request
     * @param response
     * @param id       订单的id,不是订单编号.
     * @return
     */
    @RequestMapping({"/api/integral_order_detail.htm"})
    @ResponseBody
    public String integral_order_detail(HttpServletRequest request, HttpServletResponse response,
                                        String id) {

        Map<String, Object> map = new HashMap<String, Object>();
        IntegralGoodsOrder igo = this.igoService.getObjById(CommUtil.null2Long(id));
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        JsonConfig jf = new JsonConfig();
        DateJsonValueProcessor djvp = new DateJsonValueProcessor();
        djvp.setFormate("yyyy-MM-dd HH:mm:ss");
        jf.registerJsonValueProcessor(Date.class, djvp);

        if (igo != null) {
            if (igo.getIgo_user().getId().equals(user.getId())) {
                IntegralGoodsOrder igo1 = new IntegralGoodsOrder();
                igo1.setIgo_order_sn(igo.getIgo_order_sn());
                igo1.setIgo_status(igo.getIgo_status());
                igo1.setIgo_total_integral(igo.getIgo_total_integral());
                igo1.setIgo_trans_fee(igo.getIgo_trans_fee());
                igo1.setAddTime(igo.getAddTime());
                igo1.setIgo_payment(igo.getIgo_payment());
                igo1.setIgo_pay_time(igo.getIgo_pay_time());
                igo1.setIgo_pay_msg(igo.getIgo_pay_msg());
                igo1.setIgo_ship_time(igo.getIgo_ship_time());//发货时间
                igo1.setIgo_ship_code(igo.getIgo_ship_code());//物流单号
                igo1.setIgo_ship_content(igo.getIgo_ship_content());//发货说明
                map.put("igo1", igo1);

                ApiUser user1 = new ApiUser();
                user1.setUserName(igo.getIgo_user().getUserName());
                user1.setEmail(igo.getIgo_user().getEmail());
                user1.setMemo(igo.getIgo_msg());
                map.put("user", user1);

                Address addr1 = new Address();
                addr1.setTrueName(igo.getIgo_addr().getTrueName());
                map.put("addr_area", igo.getIgo_addr().getArea().getParent().getParent().getAreaName() + " " +
                        igo.getIgo_addr().getArea().getParent().getAreaName() + " " + igo.getIgo_addr().getArea().getAreaName());
                addr1.setZip(igo.getIgo_addr().getZip());
                addr1.setArea_info(igo.getIgo_addr().getArea_info());
                addr1.setTelephone(igo.getIgo_addr().getTelephone());
                addr1.setMobile(igo.getIgo_addr().getMobile());
                map.put("address", addr1);

                List igc_list = new ArrayList();
                for (IntegralGoodsCart igc : igo.getIgo_gcs()) {
                    ApiIntegralGoods apiIntegralGoods = new ApiIntegralGoods();
                    apiIntegralGoods.setGoods_id(igc.getGoods().getId());
                    apiIntegralGoods.setGoods_name(igc.getGoods().getIg_goods_name());
                    apiIntegralGoods.setImage(CommUtil.getURL(request) + "/" + igc.getGoods().getIg_goods_img().getPath() + "/" +
                            igc.getGoods().getIg_goods_img().getName());
                    apiIntegralGoods.setGoods_count(igc.getCount());
                    apiIntegralGoods.setGoods_integral(igc.getIntegral());
                    igc_list.add(apiIntegralGoods);
                    map.put("apiIntegralGoods", igc_list);
                }
            }
        } else {
            map = new HashMap<String, Object>();
            map.put("message", "参数错误,无该订单!");
        }

        return "{\"statusCode\":200,\"msg\":\"数据加载成功!\",\"data\":" + JSONObject.fromObject(map, jf) + "}";
    }


    /**
     * 点击--(确认收货)-按钮
     *
     * @param request
     * @param id      订单的id号码,不是订单的编码
     * @return
     */
    @RequestMapping({"/api/integral_order_confirm.htm"})
    @ResponseBody
    public String integral_order_confirm(HttpServletRequest request, String id) {

        Map<String, Object> map = new HashMap<String, Object>();
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        IntegralGoodsOrder igo = this.igoService.getObjById(CommUtil.null2Long(id));
        if (igo != null) {
            if (igo.getIgo_user().getId().equals(user.getId())) {
                map.put("op_title", "数据加载成功!");
                map.put("integral_order_sn", igo.getIgo_order_sn());
            }
        } else {
            map = new HashMap<String, Object>();
            map.put("op_title", "参数错误,无该订单!");
        }
        return "{\"statusCode\":200,\"data\":" + JSONArray.fromObject(map).toString() + "}";
    }


    /**
     * 在弹出页面点击--(确认)按钮--收货保存
     *
     * @param request
     * @param id      订单的id号码,不是订单的编码
     * @return
     */
    @RequestMapping({"/api/integral_order_confirm_save.htm"})
    @ResponseBody
    public String integral_order_confirm_save(HttpServletRequest request, String id) {

        Map<String, Object> map = new HashMap<String, Object>();
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        IntegralGoodsOrder igo = this.igoService.getObjById(CommUtil.null2Long(id));
        if (igo != null) {
            if (igo.getIgo_user().getId().equals(user.getId())) {
                igo.setIgo_status(40);
                this.igoService.update(igo);
                map.put("op_title", "确认收货成功!");
            }
        } else {
            map = new HashMap<String, Object>();
            map.put("op_title", "参数错误,无该订单!");
        }
        return "{\"statusCode\":200,\"data\":" + JSONArray.fromObject(map).toString() + "}";
    }


//	/**
//	 * 积分商城轮播用的图片
//	 * @param request
//	 * @param response
//	 * @return
//	 */
//	@RequestMapping({"/api/integralImage.htm"})
//	@ResponseBody
//	public String IntegralImage(HttpServletRequest request,HttpServletResponse response){
//		Map params = new HashMap();
//		params.put("ad_status", Integer.valueOf(1));
//		String url = CommUtil.getURL(request);
//		if(!"".equals(CommUtil.null2String(this.configService.getSysConfig().getImageWebServer()))){
//			url = this.configService.getSysConfig().getImageWebServer();
//		}
//
//		List<Advert> adverts = this.advertService.query("select obj from Advert obj where obj.ad_status=:ad_status and obj.id in (229376,229377,229378,229379)",
//				params, -1, -1);
//		List list = new ArrayList();
//		for(Advert ad : adverts){
//			Map map = new HashMap();
//			map.put("ad_id", ad.getId());
//			map.put("ad_title", ad.getAd_title());
//
//			String image = url+"/"+this.configService.getSysConfig().getGoodsImage().getPath()+"/"
//					+this.configService.getSysConfig().getGoodsImage().getName();
//			if(ad.getAd_acc()!=null){
//				image = url+"/"+ad.getAd_acc().getPath()+"/"
//						+ad.getAd_acc().getName();
//			}
//			map.put("image", image);
//			list.add(map);
//		}
//		response.setContentType("text/plain");
//		response.setHeader("Cache-Control", "no-cache");
//		response.setCharacterEncoding("UTF-8");
//		if(list.size()>0 && list!=null){
//			String data = JSON.toJSONString(list);
//			return "{\"statusCode\":200,\"msg\":\"加载成功!\",\"data\":"+data+"}";
//		}else{
//			return "{\"statusCode\":500,\"msg\":\"加载失败!\"}";
//		}
//	}


}
