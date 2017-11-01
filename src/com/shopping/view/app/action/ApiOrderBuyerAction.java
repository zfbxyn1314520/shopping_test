package com.shopping.view.app.action;

import com.shopping.core.domain.virtual.SysMap;
import com.shopping.core.query.support.IPageList;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.*;
import com.shopping.foundation.domain.api.ApiGoodsCart;
import com.shopping.foundation.domain.api.ApiOrderForm;
import com.shopping.foundation.domain.query.OrderFormQueryObject;
import com.shopping.foundation.service.*;
import com.shopping.view.web.tools.KdniaoTrackQueryAPI;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.DefaultDefaultValueProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.*;

import static com.shopping.core.tools.CommUtil.isImg;

@Controller
public class ApiOrderBuyerAction {

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IOrderFormService orderFormService;

    @Autowired
    private IAccessoryService accessoryService;

    @Autowired
    private IComplaintService complaintService;

    @Autowired
    private IOrderFormLogService orderFormLogService;

    @Autowired
    private IEvaluateService evaluateService;

    @Autowired
    private IStoreService storeService;

    @Autowired
    private IStorePointService storePointService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private IPredepositLogService predepositLogService;

    @Autowired
    private IPaymentService paymentService;


    /**
     * 用户订单列表
     *
     * @param request
     * @param currentPage  当前页面大小
     * @param order_status null || "" ==>全部
     * @param order_status order_submit ==>待支付
     * @param order_status order_shipping ==>待收货
     * @param order_status order_cancel ==>已取消
     * @param order_status order_finish ==>已完成
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/order.htm?currentPage=*&order_status=*
     */
    @RequestMapping({"/api/order.htm"})
    @ResponseBody
    public String order(HttpServletRequest request, String currentPage, String order_status) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        List<ApiOrderForm> apiOrderForms = new ArrayList<ApiOrderForm>();
        List<ApiGoodsCart> apiGoodsCarts;
        ApiOrderForm apiOrderForm;
        ApiGoodsCart apiGoodsCart;
        if (user != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            OrderFormQueryObject ofqo = new OrderFormQueryObject(currentPage, map, "addTime", "desc");
            ofqo.addQuery("obj.user.id", new SysMap("user_id", user.getId()), "=");
            if (!CommUtil.null2String(order_status).equals("")) {
                // 待支付
                if (order_status.equals("order_submit")) {
                    ofqo.addQuery("obj.order_status",
                            new SysMap("order_status", Integer.valueOf(10)), "=");
                }
                //待收货
                if (order_status.equals("order_shipping")) {
                    ofqo.addQuery("obj.order_status in(20,30)", null);
                }
                //已完成
                if (order_status.equals("order_finish")) {
                    ofqo.addQuery("obj.order_status in(40,45,46,47,48,49,50,60,65)", null);
                }
                //已取消
                if (order_status.equals("order_cancel")) {
                    ofqo.addQuery("obj.order_status",
                            new SysMap("order_status", Integer.valueOf(0)), "=");
                }
            }

            IPageList pList = this.orderFormService.list(ofqo);
            if (pList.getResult() != null) {
                List<OrderForm> lists = pList.getResult();
                int size = lists.size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        List<GoodsCart> goodsCarts = lists.get(i).getGcs();
                        apiGoodsCarts = new ArrayList<ApiGoodsCart>();
                        int goods_count = 0;
                        for (int j = 0; j < goodsCarts.size(); j++) {
                            apiGoodsCart = new ApiGoodsCart();
                            Accessory accessory = goodsCarts.get(j).getGoods().getGoods_main_photo();
                            if (accessory != null) {
                                apiGoodsCart.setGoods_img(CommUtil.getURL(request) + "/" + accessory.getPath() + "/" + accessory.getName());
                            }
                            //判断该用户是否已经评价该商品
                            if (lists.get(i).getEvas() != null && lists.get(i).getEvas().size() > 0) {
                                for (Evaluate eva : lists.get(i).getEvas()) {
                                    if (user.getId() == eva.getEvaluate_user().getId()
                                            && lists.get(i).getId() == eva.getOf().getId()) {
                                        apiGoodsCart.setEvaluate(true);
                                        break;
                                    }
                                }
                            }
                            //判断该用户是否已经投诉该商品
                            if (lists.get(i).getComplaints() != null && lists.get(i).getComplaints().size() > 0) {
                                for (Complaint complaint : lists.get(i).getComplaints()) {
                                    if (user.getId() == complaint.getFrom_user().getId()
                                            && lists.get(i).getId() == complaint.getOf().getId()) {
                                        apiGoodsCart.setComplaint(true);
                                        break;
                                    }
                                }
                            }

                            goods_count += goodsCarts.get(j).getCount();
                            apiGoodsCart.setGoods_id(goodsCarts.get(j).getGoods().getId());
                            apiGoodsCart.setGoods_name(goodsCarts.get(j).getGoods().getGoods_name());
                            apiGoodsCart.setCount(goodsCarts.get(j).getCount());
                            apiGoodsCart.setPrice(goodsCarts.get(j).getPrice());
                            apiGoodsCart.setGoods_price(goodsCarts.get(j).getGoods().getGoods_price());
                            apiGoodsCart.setStore_price(goodsCarts.get(j).getGoods().getStore_price());
                            apiGoodsCart.setSpec_info(goodsCarts.get(j).getSpec_info());
                            apiGoodsCarts.add(apiGoodsCart);
                        }

                        apiOrderForm = new ApiOrderForm();
                        int orderStatus = lists.get(i).getOrder_status();
                        String button_text = "";

                        if (lists.get(i).getAddTime() != null) {
                            apiOrderForm.setAddTime(CommUtil.formatLongDate(lists.get(i).getAddTime()));
                        }
                        if (lists.get(i).getShipTime() != null) {
                            apiOrderForm.setReturn_shipTime(CommUtil.formatLongDate(lists.get(i).getShipTime()));
                        }

                        if (orderStatus == 0) {
                            button_text = "已取消";
                        } else if (orderStatus == 10) {
                            button_text = "待支付";
                        } else if (orderStatus == 20 || orderStatus == 30) {
                            button_text = "待收货";
                        } else if (orderStatus >= 40 && orderStatus <= 65) {
                            button_text = "已完成";
                        }
                        apiOrderForm.setButton_text(button_text);

                        if (lists.get(i).getPayment() != null) {
                            if (lists.get(i).getPayment().getMark().equals("alipay")) {
                                apiOrderForm.setPayment_mark("支付宝");
                            } else if (lists.get(i).getPayment().getMark().equals("alipay")) {
                                apiOrderForm.setPayment_mark("手机网页支付宝");
                            } else if (lists.get(i).getPayment().getMark().equals("alipay_wap")) {
                                apiOrderForm.setPayment_mark("支付宝");
                            } else if (lists.get(i).getPayment().getMark().equals("wx_app_pay")) {
                                apiOrderForm.setPayment_mark("微信App支付");
                            } else if (lists.get(i).getPayment().getMark().equals("wxcodepay")) {
                                apiOrderForm.setPayment_mark("微信扫码支付");
                            } else if (lists.get(i).getPayment().getMark().equals("bill")) {
                                apiOrderForm.setPayment_mark("快钱");
                            } else if (lists.get(i).getPayment().getMark().equals("tenpay")) {
                                apiOrderForm.setPayment_mark("财付通");
                            } else if (lists.get(i).getPayment().getMark().equals("chinabank")) {
                                apiOrderForm.setPayment_mark("网银在线");
                            } else if (lists.get(i).getPayment().getMark().equals("outline")) {
                                apiOrderForm.setPayment_mark("线下支付");
                            } else if (lists.get(i).getPayment().getMark().equals("chinabank")) {
                                apiOrderForm.setPayment_mark("网银在线");
                            } else if (lists.get(i).getPayment().getMark().equals("paypal")) {
                                apiOrderForm.setPayment_mark("paypal");
                            }
                        } else {
                            apiOrderForm.setPayment_mark("未支付");
                        }

                        if (lists.get(i).getInvoiceType() == 0) {
                            apiOrderForm.setInvoiceType("个人");
                        } else {
                            apiOrderForm.setInvoiceType("单位");
                        }

                        if (lists.get(i).getCi() != null) {
                            apiOrderForm.setCoupon_sn(lists.get(i).getCi().getCoupon_sn());
                            apiOrderForm.setCoupon_amount(lists.get(i).getCi().getCoupon().getCoupon_amount());
                        }
                        if (lists.get(i).getAddr() != null) {
                            apiOrderForm.setAddr_trueName(lists.get(i).getAddr().getTrueName());
                            apiOrderForm.setAddr_info(
                                    lists.get(i).getAddr().getArea().getParent().getParent().getAreaName() +
                                            lists.get(i).getAddr().getArea().getParent().getAreaName() +
                                            lists.get(i).getAddr().getArea().getAreaName()
                            );
                            apiOrderForm.setAddr_mobile(lists.get(i).getAddr().getMobile());
                        }

                        apiOrderForm.setId(lists.get(i).getId());
                        apiOrderForm.setOrder_id(lists.get(i).getOrder_id());
                        apiOrderForm.setOrder_type(lists.get(i).getOrder_type());
                        apiOrderForm.setGoods_count(goods_count);
                        apiOrderForm.setTotalPrice(lists.get(i).getTotalPrice());
                        apiOrderForm.setOrder_status(lists.get(i).getOrder_status());
                        apiOrderForm.setMsg(lists.get(i).getMsg());
                        apiOrderForm.setTransport(lists.get(i).getTransport());
                        apiOrderForm.setShipCode(lists.get(i).getShipCode());
                        apiOrderForm.setShip_price(lists.get(i).getShip_price());
                        apiOrderForm.setShipTime(CommUtil.formatLongDate(lists.get(i).getShipTime()));
                        apiOrderForm.setReturn_shipCode(lists.get(i).getReturn_shipCode());
                        apiOrderForm.setReturn_content(lists.get(i).getReturn_content());
                        apiOrderForm.setStore_id(lists.get(i).getStore().getId());
                        apiOrderForm.setStore_name(lists.get(i).getStore().getStore_name());
                        apiOrderForm.setTo_user_id(lists.get(i).getStore().getUser().getId());
                        apiOrderForm.setGcs(apiGoodsCarts);
                        apiOrderForms.add(apiOrderForm);
                    }
                }
            }
            String content = "{\"statusCode\":\"200\",\"msg\":\"数据加载成功！\",\"rowCount\":" + pList.getRowCount() + ",\"pages\":" + pList.getPages()
                    + ",\"pageCurrent\":" + pList.getCurrentPage() + ",\"list\":";
            String liststr = JSONArray.fromObject(apiOrderForms).toString();
            content += liststr + "}";
            return content;
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }

    /**
     * 查询订单物流
     *
     * @param id 物流订单id
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/query_ship.htm?id=*
     */
    @RequestMapping({"/api/query_ship.htm"})
    @ResponseBody
    public String query_ship(String id) {
        String result;
        OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
        KdniaoTrackQueryAPI api = new KdniaoTrackQueryAPI();
        try {
            result = api.getOrderTracesByJson(obj.getEc() != null ? obj.getEc().getCompany_mark() : "", obj.getShipCode());
        } catch (Exception e) {
            result = "{\"EBusinessID\":\"" + api.getEBusinessID() + "\",\"ShipperCode\":\"" + obj.getShipCode() + "\"," +
                    "\"Success\":true,\"Reason\":\"查询过程中遇到问题\",\"LogisticCode\":\"" + obj.getEc().getCompany_mark() + "\"," +
                    "\"State\":\"0\",\"Traces\": []}";
        }
        return result;
    }

    /**
     * 申请退货
     *
     * @param request
     * @param id             订单id
     * @param return_content 申请退货理由
     * @return
     * @throws Exception
     * @apiNote http://192.168.1.223:8080/shopping/api/order_return_apply_save.htm?id=*&return_content=*
     */
    @RequestMapping({"/api/order_return_apply_save.htm"})
    @ResponseBody
    public String order_return_apply_save(HttpServletRequest request, String id, String return_content) throws Exception {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        boolean result = false;
        if (user != null) {
            OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
            if (obj.getUser().getId().equals(user.getId())) {
                obj.setOrder_status(45);
                obj.setReturn_content(return_content);
                result = this.orderFormService.update(obj);
            }
            if (result) {
                return "{\"statusCode\":\"200\",\"msg\":\"提交退货申请成功！\"}";
            } else {
                return "{\"statusCode\":\"201\",\"msg\":\"提交退货申请失败！\"}";
            }
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }

    /**
     * 商品评价保存
     *
     * @param request
     * @param id                   订单id
     * @param goods_id             购买商品id
     * @param evaluate_info        评价内容
     * @param evaluate_buyer_val   好评（1） 中评（0） 差评（-1）
     * @param description_evaluate 描述相符
     * @param spec_info            规格说明
     * @param service_evaluate     服务态度
     * @param ship_evaluate        发货速度
     * @return
     * @throws Exception
     * @apiNote http://192.168.1.223:8081/shopping/api/order_evaluate_save.htm?id=&goods_id=&spec_info=&evaluate_info=&evaluate_buyer_val=&description_evaluate=&service_evaluate=&ship_evaluate=
     */
    @RequestMapping({"/api/order_evaluate_save.htm"})
    @ResponseBody
    public String order_evaluate_save(HttpServletRequest request, String id, String goods_id, String evaluate_info,
                                      String evaluate_buyer_val, String description_evaluate, String spec_info,
                                      String service_evaluate, String ship_evaluate) throws Exception {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        String content = "";
        if (user != null) {
            OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));
            if (obj.getUser().getId().equals(user.getId())) {
                if (obj.getOrder_status() == 40) {
                    obj.setOrder_status(50);
                    this.orderFormService.update(obj);
                    OrderFormLog ofl = new OrderFormLog();
                    ofl.setAddTime(new Date());
                    ofl.setLog_info("评价订单");
                    ofl.setLog_user(user);
                    ofl.setOf(obj);
                    this.orderFormLogService.save(ofl);

                    Evaluate eva = new Evaluate();
                    eva.setAddTime(new Date());
                    eva.setEvaluate_goods(this.goodsService.getObjById(Long.valueOf(Long.parseLong(goods_id))));
                    eva.setEvaluate_info(evaluate_info);
                    eva.setEvaluate_buyer_val(CommUtil.null2Int(evaluate_buyer_val));
                    eva.setDescription_evaluate(BigDecimal.valueOf(
                            CommUtil.null2Double(description_evaluate)));
                    eva.setService_evaluate(BigDecimal.valueOf(CommUtil.null2Double(service_evaluate)));
                    eva.setShip_evaluate(BigDecimal.valueOf(CommUtil.null2Double(ship_evaluate)));
                    eva.setEvaluate_type("goods");
                    eva.setEvaluate_user(user);
                    eva.setOf(obj);
                    eva.setGoods_spec(spec_info);
                    boolean result = this.evaluateService.save(eva);
                    if (result) {
                        Map params = new HashMap();
                        params.put("store_id", obj.getStore().getId());
                        List<Evaluate> evas = this.evaluateService.query("select obj from Evaluate obj where obj.of.store.id=:store_id", params, -1, -1);
                        double store_evaluate1 = 0.0D;
                        double store_evaluate1_total = 0.0D;
                        double description_evaluate1 = 0.0D;
                        double description_evaluate_total = 0.0D;
                        double service_evaluate1 = 0.0D;
                        double service_evaluate_total = 0.0D;
                        double ship_evaluate1 = 0.0D;
                        double ship_evaluate_total = 0.0D;
                        DecimalFormat df = new DecimalFormat("0.0");
                        for (Evaluate eva1 : evas) {
                            store_evaluate1_total = store_evaluate1_total + eva1.getEvaluate_buyer_val();

                            description_evaluate_total = description_evaluate_total + CommUtil.null2Double(eva1.getDescription_evaluate());

                            service_evaluate_total = service_evaluate_total + CommUtil.null2Double(eva1.getService_evaluate());

                            ship_evaluate_total = ship_evaluate_total + CommUtil.null2Double(eva1.getShip_evaluate());
                        }
                        store_evaluate1 = CommUtil.null2Double(df.format(store_evaluate1_total / evas.size()));
                        description_evaluate1 = CommUtil.null2Double(df.format(description_evaluate_total / evas.size()));
                        service_evaluate1 = CommUtil.null2Double(df.format(service_evaluate_total / evas.size()));
                        ship_evaluate1 = CommUtil.null2Double(df.format(ship_evaluate_total / evas.size()));
                        Store store = obj.getStore();
                        store.setStore_credit(store.getStore_credit() + eva.getEvaluate_buyer_val());
                        this.storeService.update(store);
                        params.clear();
                        params.put("store_id", store.getId());
                        List sps = this.storePointService.query("select obj from StorePoint obj where obj.store.id=:store_id", params, -1, -1);
                        StorePoint point = null;
                        if (sps.size() > 0)
                            point = (StorePoint) sps.get(0);
                        else {
                            point = new StorePoint();
                        }
                        point.setAddTime(new Date());
                        point.setStore(store);
                        point.setDescription_evaluate(BigDecimal.valueOf(description_evaluate1));
                        point.setService_evaluate(BigDecimal.valueOf(service_evaluate1));
                        point.setShip_evaluate(BigDecimal.valueOf(ship_evaluate1));
                        point.setStore_evaluate1(BigDecimal.valueOf(store_evaluate1));
                        if (sps.size() > 0)
                            this.storePointService.update(point);
                        else {
                            this.storePointService.save(point);
                        }
                        user.setIntegral(user.getIntegral() + this.configService.getSysConfig().getIndentComment());
                        this.userService.update(user);
                        content = "{\"statusCode\":\"200\",\"msg\":\"订单评价成功！\"}";
                    } else {
                        content = "{\"statusCode\":\"201\",\"msg\":\"订单评价失败！\"}";
                    }
                } else {
                    content = "{\"statusCode\":\"300\",\"msg\":\"当前订单不可评论！\"}";
                }
            }
            return content;
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }

    /**
     * 订单取消确认
     *
     * @param request
     * @param id               订单id
     * @param state_info       取消理由
     * @param other_state_info 其他原因取消理由
     * @return
     * @throws Exception
     * @apiNote http://192.168.1.223:8080/shopping/api/order_cancel_save.htm?id=*&state_info=*&other_state_info=*
     */
    @RequestMapping({"/api/order_cancel_save.htm"})
    @ResponseBody
    public String order_cancel_save(HttpServletRequest request, String id, String state_info, String other_state_info) throws Exception {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));

            if (obj.getUser().getId().equals(user.getId())) {
                obj.setOrder_status(0);
                this.orderFormService.update(obj);
                OrderFormLog ofl = new OrderFormLog();
                ofl.setAddTime(new Date());
                ofl.setLog_info("取消订单");
                ofl.setLog_user(user);
                ofl.setOf(obj);
                if (state_info.equals("other"))
                    ofl.setState_info(other_state_info);
                else {
                    ofl.setState_info(state_info);
                }
                this.orderFormLogService.save(ofl);
            }
            return "{\"statusCode\":\"200\",\"msg\":\"取消订单成功！\"}";
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }

    /**
     * 买家确认收货
     *
     * @param request
     * @param id      订单id
     * @return
     * @throws Exception
     * @apiNote http://192.168.1.223:8080/shopping/api/order_cofirm_save.htm?id=*
     */
    @RequestMapping({"/api/order_cofirm_save.htm"})
    @ResponseBody
    public String order_cofirm_save(HttpServletRequest request, String id) throws Exception {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            OrderForm obj = this.orderFormService.getObjById(CommUtil.null2Long(id));

            if (obj.getUser().getId().equals(user.getId())) {
                obj.setOrder_status(40);
                boolean ret = this.orderFormService.update(obj);
                if (ret) {
                    OrderFormLog ofl = new OrderFormLog();
                    ofl.setAddTime(new Date());
                    ofl.setLog_info("确认收货");
                    ofl.setLog_user(user);
                    ofl.setOf(obj);
                    this.orderFormLogService.save(ofl);
                    if (obj.getPayment().getMark().equals("balance")) {
                        User seller = this.userService.getObjById(obj.getStore().getUser().getId());
                        if (this.configService.getSysConfig().getBalance_fenrun() == 1) {
                            Map params = new HashMap();
                            params.put("type", "admin");
                            params.put("mark", "balance");
                            List payments = this.paymentService.query("select obj from Payment obj where obj.type=:type and obj.mark=:mark", params, -1, -1);
                            Payment shop_payment = new Payment();
                            if (payments.size() > 0) {
                                shop_payment = (Payment) payments.get(0);
                            }

                            double shop_availableBalance = CommUtil.null2Double(obj.getTotalPrice()) * CommUtil.null2Double(shop_payment.getBalance_divide_rate());
                            User admin = this.userService.getObjByProperty("userName", "admin");
                            admin.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(admin.getAvailableBalance(), Double.valueOf(shop_availableBalance))));
                            this.userService.update(admin);
                            PredepositLog log = new PredepositLog();
                            log.setAddTime(new Date());
                            log.setPd_log_user(seller);
                            log.setPd_op_type("分润");
                            log.setPd_log_amount(BigDecimal.valueOf(shop_availableBalance));
                            log.setPd_log_info("订单" + obj.getOrder_id() + "确认收货平台分润获得预存款");
                            log.setPd_type("可用预存款");
                            this.predepositLogService.save(log);

                            double seller_availableBalance = CommUtil.null2Double(obj.getTotalPrice()) - shop_availableBalance;
                            seller.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(seller.getAvailableBalance(), Double.valueOf(seller_availableBalance))));
                            this.userService.update(seller);
                            PredepositLog log1 = new PredepositLog();
                            log1.setAddTime(new Date());
                            log1.setPd_log_user(seller);
                            log1.setPd_op_type("增加");
                            log1.setPd_log_amount(BigDecimal.valueOf(seller_availableBalance));
                            log1.setPd_log_info("订单" + obj.getOrder_id() + "确认收货增加预存款");
                            log1.setPd_type("可用预存款");
                            this.predepositLogService.save(log1);

                            User buyer = obj.getUser();
                            buyer.setFreezeBlance(BigDecimal.valueOf(CommUtil.subtract(buyer.getFreezeBlance(), obj.getTotalPrice())));
                            this.userService.update(buyer);
                        } else {
                            seller.setAvailableBalance(BigDecimal.valueOf(CommUtil.add(seller.getAvailableBalance(), obj.getTotalPrice())));
                            this.userService.update(seller);
                            PredepositLog log = new PredepositLog();
                            log.setAddTime(new Date());
                            log.setPd_log_user(seller);
                            log.setPd_op_type("增加");
                            log.setPd_log_amount(obj.getTotalPrice());
                            log.setPd_log_info("订单" + obj.getOrder_id() + "确认收货增加预存款");
                            log.setPd_type("可用预存款");
                            this.predepositLogService.save(log);

                            User buyer = obj.getUser();
                            buyer.setFreezeBlance(BigDecimal.valueOf(CommUtil.subtract(buyer.getFreezeBlance(), obj.getTotalPrice())));
                            this.userService.update(buyer);
                        }
                    }
                }
            }
            return "{\"statusCode\":\"200\",\"msg\":\"成功确认收货！\"}";
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }


    /**
     * 用户投诉商品保存
     *
     * @param request
     * @param id                订单id
     * @param goods_id          商品id
     * @param issue_description 问题描述
     * @param complaint_content 投诉内容
     * @param to_user_id        投诉至用户id
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/complaint_save.htm?id=*&goods_id=*&to_user_id=*&issue_description=*&complaint_content=*
     */
    @RequestMapping({"/api/complaint_save.htm"})
    @ResponseBody
    public String complaint_save(HttpServletRequest request, @RequestParam("mFile") MultipartFile[] files, String id, String goods_id,
                                 String issue_description, String complaint_content, String to_user_id) {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        Complaint obj = new Complaint();
        ComplaintGoods cg = new ComplaintGoods();

        if (user != null) {
            OrderForm of = this.orderFormService.getObjById(CommUtil.null2Long(id));
            obj.setAddTime(new Date());
            obj.setFrom_user_content(complaint_content);
            obj.setFrom_user(user);
            obj.setTo_user(this.userService.getObjById(CommUtil.null2Long(to_user_id)));
            obj.setType("buyer");
            obj.setOf(of);

            Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
            cg.setAddTime(new Date());
            cg.setComplaint(obj);
            cg.setGoods(goods);
            cg.setContent(CommUtil.null2String(issue_description));

            obj.getCgs().add(cg);


            String uploadFilePath = this.configService.getSysConfig().getUploadFilePath();
            String saveFilePathName = request.getSession().getServletContext().getRealPath("/") +
                    uploadFilePath + File.separator + "complaint";

            try {
                String str = this.mutilFileUpload(files, saveFilePathName, null, uploadFilePath);
                JSONObject jsonObject = JSONObject.fromObject(str);

                if (jsonObject.getString("statusCode").equals("200")) {
                    JSONArray jsonArray = JSONArray.fromObject(jsonObject.getString("data"));
                    List<Accessory> accessories = JSONArray.toList(jsonArray, Accessory.class);
                    if (accessories != null && accessories.size() > 0) {
                        for (int i = 0; i < accessories.size(); i++) {
                            this.accessoryService.save(accessories.get(i));
                            switch (i) {
                                case 0:
                                    obj.setFrom_acc1(accessories.get(i));
                                    break;
                                case 1:
                                    obj.setFrom_acc2(accessories.get(i));
                                    break;
                                case 2:
                                    obj.setFrom_acc3(accessories.get(i));
                                    break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean result = this.complaintService.save(obj);
            if (result) {
                return "{\"statusCode\":\"200\",\"msg\":\"投诉提交成功！\"}";
            } else {
                return "{\"statusCode\":\"201\",\"msg\":\"投诉提交失败！\"}";
            }

        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }


    /**
     * 上传多张图片
     *
     * @param files            多张图片文件
     * @param saveFilePathName 保存文件路径
     * @param saveFileName     保存文件夹名称
     * @return
     */
    public String mutilFileUpload(MultipartFile[] files, String saveFilePathName, String saveFileName, String uploadFilePath)
            throws IOException {
        Map map = new HashMap();
        int i = 0;
        if (files.length > 0) {
            try {
                saveFilePathName = URLDecoder.decode(saveFilePathName, "utf-8");
                File f = new File(saveFilePathName);
                if (!f.exists() && !f.isDirectory()) {
                    f.mkdirs();
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String[] extendes = {"image/bmp", "image/gif", "image/jpg", "image/jpeg", "image/png", "image/webp"};
            List<Accessory> from_acc = new ArrayList<Accessory>();
            Accessory accessory;

            for (MultipartFile file : files) {
                i++;
                if (i > 3) {
                    break;
                }
                if (!file.isEmpty()) {
                    String extend = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
                    if ((saveFileName == null) || (saveFileName.trim().equals(""))) {
                        saveFileName = UUID.randomUUID().toString() + "." + extend;
                    }
                    if (saveFileName.lastIndexOf(".") < 0) {
                        saveFileName = saveFileName + "." + extend;
                    }
                    float fileSize = Float.valueOf((float) file.getSize()).floatValue();
                    List errors = new ArrayList();
                    boolean flag = true;

                    if (extendes != null) {
                        for (String s : extendes) {
                            if (extend.toLowerCase().equals(s))
                                flag = true;
                        }
                    }

                    if (flag) {
                        File path = new File(saveFilePathName);
                        if (!path.exists()) {
                            path.mkdir();
                        }
                        DataOutputStream out = new DataOutputStream(
                                new FileOutputStream(saveFilePathName + File.separator + saveFileName));
                        InputStream is = null;
                        try {
                            is = file.getInputStream();
                            int size = (int) fileSize;
                            byte[] buffer = new byte[size];
                            while (is.read(buffer) > 0)
                                out.write(buffer);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                        }
                        if (isImg(extend)) {
                            File img = new File(saveFilePathName + File.separator + saveFileName);
                            try {
                                BufferedImage bis = ImageIO.read(img);
                                int w = bis.getWidth();
                                int h = bis.getHeight();
                                map.put("width", Integer.valueOf(w));
                                map.put("height", Integer.valueOf(h));
                            } catch (Exception localException) {
                                localException.printStackTrace();
                            }
                        }
                        map.put("mime", extend);
                        map.put("fileName", saveFileName);
                        map.put("fileSize", Float.valueOf(fileSize));
                        map.put("error", errors);
                        map.put("oldName", file.getOriginalFilename());
                    } else {
                        errors.add("不允许的扩展名");
                    }

                    if (map.get("fileName") != "") {
                        accessory = new Accessory();
                        accessory.setName(CommUtil.null2String(map.get("fileName")));
                        accessory.setExt(CommUtil.null2String(map.get("mime")));
                        accessory.setSize(CommUtil.null2Float(map.get("fileSize")));
                        accessory.setPath(uploadFilePath + "/complaint");
                        accessory.setWidth(CommUtil.null2Int(map.get("width")));
                        accessory.setHeight(CommUtil.null2Int(map.get("height")));
                        accessory.setAddTime(new Date());
                        from_acc.add(accessory);
                    }
                    saveFileName = "";
                }
            }
//            解决JSONObject.fromObject数字为null时被转换为0;
            JsonConfig jsonConfig = new JsonConfig();
            jsonConfig.registerDefaultValueProcessor(Long.class, new DefaultDefaultValueProcessor() {
                public Object getDefaultValue(Class type) {
                    return null;
                }
            });
            return "{\"statusCode\":\"200\",\"msg\":\"上传图片成功！\",\"data\":" + JSONArray.fromObject(from_acc, jsonConfig).toString() + "}";
        } else {
            return "{\"statusCode\":\"202\",\"msg\":\"未选择上传图片！\"}";
        }
    }


}
