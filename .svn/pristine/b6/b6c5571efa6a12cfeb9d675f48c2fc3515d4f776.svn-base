package com.shopping.view.app.action;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.WxCommonUtil;
import com.shopping.foundation.domain.OrderForm;
import com.shopping.foundation.domain.Payment;
import com.shopping.foundation.service.IOrderFormService;
import com.shopping.foundation.service.IPaymentService;
import com.shopping.pay.alipay.config.AlipayConfig;
import com.shopping.pay.alipay.util.AlipayCore;
import com.shopping.pay.alipay.util.UtilDate;
import com.shopping.view.web.action.CartViewAction;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Dugbud
 * @implNote 部署正式服务器请修改生成预付订单接口支付金额和支付回调接口的手动操作订单支付状态代码加注释
 */

@Controller
public class ApiPaymentViewAction {

    @Autowired
    private IOrderFormService orderFormService;

    @Autowired
    private IPaymentService paymentService;

    private static Logger logger = LoggerFactory.getLogger(CartViewAction.class);

    /**
     * 生成微信订单数据以及微信支付需要的签名等信息，传输到前端，发起调用App支付
     *
     * @param request
     * @param id          订单号
     * @param productName 商品名称 示例：指讯通-商城交易
     * @throws Exception
     * @apiNote http://192.168.1.223:8080/shopping/api/wxpay.htm?id=*&productName=*
     */
    @RequestMapping({"/api/wxpay.htm"})
    @ResponseBody
    public String wxpay(HttpServletRequest request, String id, String productName) throws Exception {

        String UNI_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String siteURL = CommUtil.getURL(request);
        BigDecimal total_price = new BigDecimal(0);
        SortedMap<Object, Object> params = new TreeMap<Object, Object>();
        boolean b = true;
        String APPID;
        String MCH_ID;
        String API_KEY;
        OrderForm of;
        String[] ids;

        //微信支付使用统一商户账户
        Map m = new HashMap();
        m.put("mark", "wx_app_pay");
        List payments = this.paymentService.query(
                "select obj from Payment obj where obj.mark=:mark", m, -1, -1);

        if (payments != null && payments.size() > 0) {
            Payment payment = (Payment) payments.get(0);
            APPID = payment.getWeixin_appId();
            MCH_ID = payment.getWeixin_partnerId();
            API_KEY = payment.getWeixin_paySignKey();

            if (id.contains(",")) {
                ids = id.split(",");
            } else {
                ids = new String[]{id};
            }

            for (int i = 0; i < ids.length; i++) {
                of = this.orderFormService.getObjById(CommUtil.null2Long(ids[i]));
                if (of != null) {
                    if (of.getOrder_status() == 10) {
                        of.setPayment(payment);
                        this.orderFormService.update(of);

                        if (ids.length == 1) {
                            id = of.getOrder_id();
                        } else {
                            id = id.replace(",", "|");
                        }

                        // 将金额单位元转换为分
                        total_price = total_price.add(of.getTotalPrice().multiply(new BigDecimal(100)).setScale(0));
                    }
                } else {
                    b = false;
                    break;
                }
            }

            if (b) {
                SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
                parameters.put("appid", APPID);
                parameters.put("mch_id", MCH_ID);
                parameters.put("nonce_str", WxCommonUtil.createNoncestr());
                parameters.put("body", productName);// 商品名称

                /** 订单号 */
                parameters.put("out_trade_no", id);
                /** 订单金额以分为单位，只能为整数 */
                parameters.put("total_fee", "1");//测试用的金额1分钱
//        parameters.put("total_fee", total_price.toString());
                System.out.println("支付金额=========" + total_price.toString());
                /** 客户端本地ip */
                parameters.put("spbill_create_ip", request.getRemoteAddr());
                /** 支付回调地址 */
                parameters.put("notify_url", siteURL + "/wechat/paynotify.htm");
                /** 支付方式为App支付 */
                parameters.put("trade_type", "APP");
                /** 使用MD5进行签名，编码必须为UTF-8 */
                String sign = WxCommonUtil.createSignMD5("UTF-8", parameters, API_KEY);
                /**将签名结果加入到map中，用于生成xml格式的字符串*/
                parameters.put("sign", sign);
                /** 生成xml结构的数据，用于统一下单请求的xml请求数据 */
                String requestXML = WxCommonUtil.getRequestXml(parameters);
                logger.info("请求统一支付requestXML：" + requestXML);

                /** 1、使用POST请求统一下单接口，获取预支付单号prepay_id */
                String result = WxCommonUtil.httpsRequestString(UNI_URL, "POST", requestXML);
                logger.info("请求统一支付result:" + result);
                //解析微信返回的信息，以Map形式存储便于取值
                Map<String, String> map = WxCommonUtil.doXMLParse(result);
                /**
                 * 统一下单接口返回正常的prepay_id，再按签名规范重新生成签名后，将数据传输给APP。
                 * 参与签名的字段名为appid，partnerid，prepayid，noncestr，timestamp，package。
                 * 注意：package的值格式为Sign=WXPay
                 */
                if ("SUCCESS".equals(map.get("return_code"))) {
                    if (map.get("result_code").equals("SUCCESS")) {//因测试阶段使用局域网，微信无法访问支付回调地址，这里手动简要操作
                        params.put("appid", APPID);//app_id
                        params.put("partnerid", MCH_ID);//微信商户账号
                        params.put("prepayid", map.get("prepay_id"));//预付订单id
                        params.put("package", "Sign=WXPay");//默认sign=WXPay
                        params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000)); //时间戳
                        params.put("noncestr", WxCommonUtil.createNoncestr()); //随机字符串
                        params.put("sign", WxCommonUtil.createSignMD5("UTF-8", params, API_KEY));
                        params.put("returnCode", "SUCCESS");//返回状态码
                        params.put("returnMsg", map.get("return_msg"));//返回信息
                        logger.info("预支付单号prepay_id为:" + map.get("prepay_id"));
                    } else {
                        params.put("returnCode", "FAIL");//返回状态码
                        params.put("returnMsg", map.get("err_code_des"));//返回信息
                    }
                } else {
                    params.put("returnCode", "FAIL");//返回状态码
                    params.put("returnMsg", map.get("return_msg"));//返回信息
                }
            } else {
                params.put("returnCode", "FAIL");//返回状态码
                params.put("returnMsg", "订单信息有误");//返回信息
            }
        } else

        {
            params.put("returnCode", "FAIL");//返回状态码
            params.put("returnMsg", "微信支付数据出错，请选择其他方式支付");//返回信息
        }
        return JSONObject.fromObject(params).toString();
    }


    /**
     * 商户后台查询微信支付结果
     *
     * @param id 订单id
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/wxPayOrderQuery.htm?id=*
     */
    @RequestMapping({"/api/wxPayOrderQuery.htm"})
    @ResponseBody
    public String wxPayOrderQuery(String id) {

        String UNI_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
        SortedMap<Object, Object> params = new TreeMap<Object, Object>();
        String[] ids;
        OrderForm order;
        boolean b = true;
        String APPID;
        String MCH_ID;
        String API_KEY;

        if (id.contains(",")) {
            ids = id.split(",");
        } else {
            ids = new String[]{id};
        }

        //微信支付使用统一商户账户
        Map m = new HashMap();
        m.put("mark", "wx_app_pay");
        List payments = this.paymentService.query(
                "select obj from Payment obj where obj.mark=:mark", m, -1, -1);

        if (payments != null && payments.size() > 0) {
            Payment payment = (Payment) payments.get(0);
            APPID = payment.getWeixin_appId();
            MCH_ID = payment.getWeixin_partnerId();
            API_KEY = payment.getWeixin_paySignKey();

            String transaction_id = "";

            for (int i = 0; i < ids.length; i++) {
                order = this.orderFormService.getObjById(CommUtil.null2Long(ids[i]));
                if (order != null) {

                    if (order.getOut_order_id() == null) {
                        order.setOut_order_id("");
                    }

                    if (transaction_id.equals("")) {
                        transaction_id = order.getOut_order_id().trim();
                    } else {
                        if (!transaction_id.equals(order.getOut_order_id().trim())) {
                            b = false;
                            break;
                        }
                    }

                    if (ids.length == 1) {
                        id = order.getOrder_id();
                    } else {
                        id = id.replace(",", "|");
                    }
                } else {
                    b = false;
                    break;
                }
            }

            if (b) {

                SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
                parameters.put("appid", APPID);
                parameters.put("mch_id", MCH_ID);
                //建议使用微信订单号，这儿由于局域网微信无法回调，使用商品订单号
                parameters.put("transaction_id", transaction_id);
                parameters.put("out_trade_no", id);
                parameters.put("nonce_str", WxCommonUtil.createNoncestr());
                String sign = WxCommonUtil.createSignMD5("UTF-8", parameters, API_KEY);
                parameters.put("sign", sign);
                String requestXML = WxCommonUtil.getRequestXml(parameters);
                String result = WxCommonUtil.httpsRequestString(UNI_URL, "POST", requestXML);
                try {
                    Map<String, String> map = WxCommonUtil.doXMLParse(result);
                    if ("SUCCESS".equals(map.get("return_code"))) {
                        if (map.get("result_code").equals("SUCCESS")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String trade_state = "";
                            String trade_type = "";
                            String bank_type = "";

                            switch (map.get("trade_state").toString()) {
                                case "SUCCESS":
                                    trade_state = "支付成功";
                                    break;
                                case "REFUND":
                                    trade_state = "转入退款";
                                    break;
                                case "NOTPAY":
                                    trade_state = "未支付";
                                    break;
                                case "CLOSED":
                                    trade_state = "已关闭";
                                    break;
                                case "REVOKED":
                                    trade_state = "已撤销（刷卡支付）";
                                    break;
                                case "USERPAYING":
                                    trade_state = "用户支付中";
                                    break;
                                case "PAYERROR":
                                    trade_state = "支付失败(其他原因，如银行返回失败)";
                                    break;
                            }
                            if ("支付成功".equals(trade_state)) {

                                //因测试阶段使用局域网，微信无法访问支付回调地址，这里手动简要操作
                                for (int i = 0; i < ids.length; i++) {
                                    OrderForm of = this.orderFormService.getObjById(CommUtil.null2Long(ids[i]));
                                    if (of.getOut_order_id() != null && !of.getOut_order_id().equals("")) {
                                        of.setOrder_status(20);
                                        of.setOut_order_id(map.get("transaction_id"));
                                        of.setPayTime(new Date());
                                        this.orderFormService.update(of);
                                    }
                                }
                                //end

                                if (map.get("trade_type") != null) {//交易类型
                                    switch (map.get("trade_type").toString()) {
                                        case "JSAPI":
                                            trade_type = "公众号支付";
                                            break;
                                        case "NATIVE":
                                            trade_type = "扫码支付";
                                            break;
                                        case "APP":
                                            trade_type = "App支付";
                                            break;
                                        case "MICROPAY":
                                            trade_type = "刷卡支付";
                                            break;
                                    }
                                    params.put("trade_type", trade_type);
                                }

                                if (map.get("bank_type") != null) {//付款银行
                                    String str = map.get("bank_type");
                                    if (str.endsWith("_DEBIT")) {
                                        bank_type = "借记卡";
                                    } else if (str.endsWith("_CREDIT")) {
                                        bank_type = "信用卡";
                                    } else if (str.endsWith("CFT")) {
                                        bank_type = "零钱";
                                    } else {
                                        bank_type = "未知方式";
                                    }
                                    params.put("bank_type", bank_type);
                                }

                                if (map.get("openid") != null) {//用户标识
                                    params.put("openid", map.get("openid"));
                                }
                                if (map.get("total_fee") != null) {//总金额
                                    params.put("total_fee", new BigDecimal(map.get("total_fee")).divide(new BigDecimal(100)).setScale(2).toString());
                                }
                                if (map.get("cash_fee") != null) {//现金支付金额
                                    params.put("cash_fee", new BigDecimal(map.get("cash_fee")).divide(new BigDecimal(100)).setScale(2).toString());
                                }
                                if (map.get("transaction_id") != null) {//微信支付订单号
                                    params.put("transaction_id", map.get("transaction_id"));
                                }
                                if (map.get("out_trade_no") != null) {//商户订单号
                                    params.put("out_trade_no", id);
                                }
                                if (map.get("time_end") != null) {//支付完成时间
                                    params.put("time_end", sdf.format(CommUtil.formatDate(map.get("time_end"), "yyyyMMddHHmmss")));
                                }

                                params.put("returnCode", "SUCCESS");//返回状态码
                                params.put("returnMsg", trade_state);//返回信息

                            } else {
                                params.put("returnCode", "FAIL");//返回状态码
                                params.put("returnMsg", trade_state);//返回信息
                            }
                        } else {
                            params.put("returnCode", "FAIL");//返回状态码
                            params.put("returnMsg", map.get("err_code_des"));//返回信息
                        }
                    } else {
                        params.put("returnCode", "FAIL");//返回状态码
                        params.put("returnMsg", map.get("return_msg"));//返回信息
                    }
                } catch (Exception e) {
                    params.put("returnCode", "FAIL");//返回状态码
                    params.put("returnMsg", "微信订单信息有误");//返回信息
                }
            } else {
                params.put("returnCode", "FAIL");//返回状态码
                params.put("returnMsg", "商品订单信息有误");//返回信息
            }
        } else {
            params.put("returnCode", "FAIL");//返回状态码
            params.put("returnMsg", "微信支付数据出错，请选择其他方式支付");//返回信息
        }
        return JSONObject.fromObject(params).toString();
    }


    /**
     * 生成支付宝订单数据以及支付宝支付需要的签名等信息，传输到前端，发起调用App支付
     *
     * @param id      订单号
     * @param subject 商品的标题/交易标题/订单标题/订单关键字等 示例：指讯通-商城交易
     * @throws Exception
     * @apiNote http://192.168.1.223:8081/shopping/api/aliPay.htm?id=*&subject=*
     */
    @RequestMapping({"/api/aliPay.htm"})
    @ResponseBody
    public String aliPay(HttpServletRequest request, String id, String subject) {

        String serverUrl = "https://openapi.alipay.com/gateway.do";
        String siteURL = CommUtil.getURL(request);
        BigDecimal total_amount = new BigDecimal(0);
        SortedMap<Object, Object> params = new TreeMap<Object, Object>();
        boolean b = true;
        String out_trade_no = "";
        String body = "";
        OrderForm of;
        String[] ids;

        //支付宝支付使用统一商户账户
        Map mp = new HashMap();
        mp.put("mark", "alipay_app");
        List payments = this.paymentService.query("select obj from Payment obj where obj.mark=:mark", mp, -1, -1);

        if (payments != null && payments.size() > 0) {
            Payment payment = (Payment) payments.get(0);

            if (id.contains(",")) {
                ids = id.split(",");
            } else {
                ids = new String[]{id};
            }

            for (int i = 0; i < ids.length; i++) {
                of = this.orderFormService.getObjById(CommUtil.null2Long(ids[i]));
                if (of != null) {
                    if (of.getOrder_status() == 10) {
                        of.setPayment(payment);
                        this.orderFormService.update(of);
                        body += of.getGcs().get(0).getGoods().getGoods_name() + "/";
                        out_trade_no += of.getOrder_id().trim() + "_";
                        total_amount = total_amount.add(of.getTotalPrice());
                    }
                } else {
                    b = false;
                    break;
                }
            }
            out_trade_no = out_trade_no.substring(0, out_trade_no.length() - 1);

            if (b) {

                //实例化客户端
                AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, AlipayConfig.getApp_id(), AlipayConfig.private_key, "json", "utf-8", AlipayConfig.ali_public_key, "RSA2");
                //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
                AlipayTradeAppPayRequest req = new AlipayTradeAppPayRequest();
                //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
                AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
                model.setBody(body.substring(0, body.length() - 1));
                model.setSubject(subject); //商品标题
                model.setOutTradeNo(out_trade_no); //商家订单编号
                model.setTimeoutExpress("30m"); //超时关闭该订单时间
//                model.setTotalAmount(total_amount.toString());  //订单总金额
                System.out.println("支付金额=========" + total_amount.toString());
                model.setTotalAmount("0.01");  //订单总金额
                model.setProductCode("QUICK_MSECURITY_PAY"); //销售产品码，商家和支付宝签约的产品码，为固定值QUICK_MSECURITY_PAY
                req.setBizModel(model);
                req.setNotifyUrl(siteURL + "/alipay/alipay_notify.htm");  //回调地址
                String orderStr = "";
                try {
                    //这里和普通的接口调用不同，使用的是sdkExecute
                    AlipayTradeAppPayResponse response = alipayClient.sdkExecute(req);
                    orderStr = response.getBody();
                } catch (AlipayApiException e) {
                    e.printStackTrace();
                }

                System.out.println("orderStr==" + orderStr);
                params.put("returnCode", "SUCCESS");//返回状态码
                params.put("returnMsg", "加载成功");//返回信息
                params.put("returnData", orderStr);//返回信息
            } else {
                params.put("returnCode", "FAIL");//返回状态码
                params.put("returnMsg", "商品订单信息有误");//返回信息
            }
        } else {
            params.put("returnCode", "FAIL");//返回状态码
            params.put("returnMsg", "支付宝支付数据出错，请选择其他方式支付");//返回信息
        }
        return JSONObject.fromObject(params).toString();
    }

    /**
     * 商户后台查询支付宝支付结果
     *
     * @param id 订单id
     * @return
     * @apiNote http://192.168.1.223:8081/shopping/api/aliPayOrderQuery.htm?id=*
     */
    @RequestMapping("/api/aliPayOrderQuery.htm")
    @ResponseBody
    public String aliPayOrderQuery(String id) {

        String serverUrl = "https://openapi.alipay.com/gateway.do";
        AlipayTradeQueryResponse response = null;
        String out_trade_no = "";
        String transaction_id = "";
        boolean b = true;
        OrderForm of;
        String[] ids;

        if (id.contains(",")) {
            ids = id.split(",");
        } else {
            ids = new String[]{id};
        }

        for (int i = 0; i < ids.length; i++) {
            of = this.orderFormService.getObjById(CommUtil.null2Long(ids[i]));
            if (of != null) {

                if (of.getOut_order_id() == null) {
                    of.setOut_order_id("");
                }

                if (transaction_id.equals("")) {
                    transaction_id = of.getOut_order_id().trim();
                } else {
                    if (!transaction_id.equals(of.getOut_order_id().trim())) {
                        b = false;
                        break;
                    }
                }
                out_trade_no += of.getOrder_id().trim() + "_";
            } else {
                b = false;
                break;
            }
        }
        if (out_trade_no != null && !"".equals(out_trade_no)) {
            out_trade_no = out_trade_no.substring(0, out_trade_no.length() - 1);
        }

        if (b) {
            AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, AlipayConfig.getApp_id(), AlipayConfig.getPrivate_key(), "json", "utf-8", AlipayConfig.getAli_public_key(), "RSA2");
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(out_trade_no);
//            model.setTradeNo();
            request.setBizModel(model);

            try {
                response = alipayClient.execute(request);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }

            if (response != null) {
                if (response.getCode().equals("10000")) {
                    //因测试阶段使用局域网，微信无法访问支付回调地址，这里手动简要操作
                    for (int i = 0; i < ids.length; i++) {
                        OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(ids[i]));
                        if (order.getOut_order_id() != null && !order.getOut_order_id().equals("")) {
                            order.setOrder_status(20);
                            order.setOut_order_id(response.getTradeNo());
                            order.setPayTime(new Date());
                            this.orderFormService.update(order);
                        }
                    }
                    //end
                }
                return response.getBody();
            } else {
                return "{\"alipay_trade_query_response\": {\"code\": \"50000\",\"msg\": \"ALI Pay Data ERROR\",\"sub_code\": \"EOLLSE.CQ.PAY_DATA_ERROR\"," +
                        "\"sub_msg\": \"支付宝支付数据出错\",\"buyer_pay_amount\": \"0.00\",\"invoice_amount\": \"0.00\",\"out_trade_no\": \"" + out_trade_no + "\"," +
                        "\"point_amount\": \"0.00\",\"receipt_amount\": \"0.00\"},\"sign\":\"" + CommUtil.randomString(32) + "\"}";
            }
        } else {
            return "{\"alipay_trade_query_response\": {\"code\": \"60000\",\"msg\": \"Order Data ERROR\",\"sub_code\": \"EOLLSE.CQ.ORDER_DATA_ERROR\"," +
                    "\"sub_msg\": \"商品订单信息错误\",\"buyer_pay_amount\": \"0.00\",\"invoice_amount\": \"0.00\",\"out_trade_no\": \"\"," +
                    "\"point_amount\": \"0.00\",\"receipt_amount\": \"0.00\"},\"sign\":\"" + CommUtil.randomString(32) + "\"}";
        }
    }


}
