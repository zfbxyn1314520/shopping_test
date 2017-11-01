package com.shopping.foundation.domain.api;


import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ApiOrderForm {

    private long id;

    //订单编号
    private String order_id;
    //订单类型
    private String order_type;
    //下单时间
    private String addTime;
    //总价
    @Column(precision = 12, scale = 2)
    private BigDecimal totalPrice;
    //总数量
    private int goods_count;
    //订单状态
    private int order_status;
    //信息
    private String msg;
    //操作内容
    private String button_text;
    //运送方式
    private String transport;
    //收货人
    private String addr_trueName;
    //收货地址
    private String addr_info;
    //收货人手机号码
    private String addr_mobile;
    //配送单号
    private String shipCode;
    //配送时间
    private String shipTime;
    //支付方式
    private String payment_mark;
    //优惠券码
    private String coupon_sn;
    //优惠券金额
    @Column(precision = 12, scale = 2)
    private BigDecimal coupon_amount;
    //运费
    @Column(precision = 12, scale = 2)
    private BigDecimal ship_price;
    //退货配送单号
    private String return_shipCode;
    //退货配送时间
    private String return_shipTime;
    //退货原因
    private String return_content;
    //店铺名称
    private String store_name;
    //店铺id
    private long store_id;
    //投诉至用户id
    private long to_user_id;
    //发票类型
    private String invoiceType;
    //订单商品集合
    List<ApiGoodsCart> gcs = new ArrayList();

    public int getGoods_count() {
        return goods_count;
    }

    public void setGoods_count(int goods_count) {
        this.goods_count = goods_count;
    }

    public String getReturn_shipTime() {
        return return_shipTime;
    }

    public long getStore_id() {
        return store_id;
    }

    public void setStore_id(long store_id) {
        this.store_id = store_id;
    }

    public long getTo_user_id() {
        return to_user_id;
    }

    public void setTo_user_id(long to_user_id) {
        this.to_user_id = to_user_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getOrder_status() {
        return order_status;
    }

    public void setOrder_status(int order_status) {
        this.order_status = order_status;
    }

    public List<ApiGoodsCart> getGcs() {
        return gcs;
    }

    public void setGcs(List<ApiGoodsCart> gcs) {
        this.gcs = gcs;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getShipCode() {
        return shipCode;
    }

    public void setShipCode(String shipCode) {
        this.shipCode = shipCode;
    }

    public BigDecimal getShip_price() {
        return ship_price;
    }

    public void setShip_price(BigDecimal ship_price) {
        this.ship_price = ship_price;
    }

    public String getReturn_shipCode() {
        return return_shipCode;
    }

    public void setReturn_shipCode(String return_shipCode) {
        this.return_shipCode = return_shipCode;
    }


    public void setReturn_shipTime(String return_shipTime) {
        this.return_shipTime = return_shipTime;
    }

    public String getReturn_content() {
        return return_content;
    }

    public void setReturn_content(String return_content) {
        this.return_content = return_content;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getShipTime() {
        return shipTime;
    }

    public void setShipTime(String shipTime) {
        this.shipTime = shipTime;
    }

    public String getPayment_mark() {
        return payment_mark;
    }

    public void setPayment_mark(String payment_mark) {
        this.payment_mark = payment_mark;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getButton_text() {
        return button_text;
    }

    public void setButton_text(String button_text) {
        this.button_text = button_text;
    }

    public String getAddr_trueName() {
        return addr_trueName;
    }

    public void setAddr_trueName(String addr_trueName) {
        this.addr_trueName = addr_trueName;
    }

    public String getAddr_info() {
        return addr_info;
    }

    public void setAddr_info(String addr_info) {
        this.addr_info = addr_info;
    }

    public String getAddr_mobile() {
        return addr_mobile;
    }

    public void setAddr_mobile(String addr_mobile) {
        this.addr_mobile = addr_mobile;
    }

    public BigDecimal getCoupon_amount() {
        return coupon_amount;
    }

    public String getCoupon_sn() {
        return coupon_sn;
    }

    public void setCoupon_sn(String coupon_sn) {
        this.coupon_sn = coupon_sn;
    }

    public void setCoupon_amount(BigDecimal coupon_amount) {
        this.coupon_amount = coupon_amount;
    }
}
