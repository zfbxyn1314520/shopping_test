package com.shopping.foundation.domain.api;

import java.util.List;

public class ApiOrderData {
    /**
     * cart_session : SqMoGma8Dra7nFRB8aKkUQeWwKaY0gtn
     * data : [{"price":"89","transport":"平邮","ship_price":"10","coupon_id":"","invoice":"","store_id":"32769","invoice_type":"0","msg":""},{"price":"858","transport":"卖家承担","ship_price":"0","coupon_id":"","invoice":"","store_id":"1","invoice_type":"0","msg":""}]
     * total_price : 947
     * addr_id : 4524606
     */

    private String cart_session;
    private String total_price;
    private String addr_id;
    private String goods_id;
    private List<DataBean> data;

    public String getCart_session() {
        return cart_session;
    }

    public void setCart_session(String cart_session) {
        this.cart_session = cart_session;
    }

    public String getTotal_price() {
        return total_price;
    }

    public void setTotal_price(String total_price) {
        this.total_price = total_price;
    }

    public String getAddr_id() {
        return addr_id;
    }

    public void setAddr_id(String addr_id) {
        this.addr_id = addr_id;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * price : 89
         * transport : 平邮
         * ship_price : 10
         * coupon_id :
         * invoice :
         * store_id : 32769
         * invoice_type : 0
         * msg :
         */

        //总价
        private String price;
        //配送方式
        private String transport;
        //运费价格
        private String ship_price;
        //优惠券id
        private String coupon_id;
        //发票抬头
        private String invoice;
        //店铺id
        private String store_id;
        //发票类型
        private String invoice_type;
        //留言
        private String msg;

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getTransport() {
            return transport;
        }

        public void setTransport(String transport) {
            this.transport = transport;
        }

        public String getShip_price() {
            return ship_price;
        }

        public void setShip_price(String ship_price) {
            this.ship_price = ship_price;
        }

        public String getCoupon_id() {
            return coupon_id;
        }

        public void setCoupon_id(String coupon_id) {
            this.coupon_id = coupon_id;
        }

        public String getInvoice() {
            return invoice;
        }

        public void setInvoice(String invoice) {
            this.invoice = invoice;
        }

        public String getStore_id() {
            return store_id;
        }

        public void setStore_id(String store_id) {
            this.store_id = store_id;
        }

        public String getInvoice_type() {
            return invoice_type;
        }

        public void setInvoice_type(String invoice_type) {
            this.invoice_type = invoice_type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }


}
