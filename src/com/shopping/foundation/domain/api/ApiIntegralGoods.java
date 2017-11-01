package com.shopping.foundation.domain.api;

import java.math.BigDecimal;

public class ApiIntegralGoods {

    private long goods_id;

    private String goods_name;

    private int goods_integral;

    private int goods_count;

    private String image;

    private BigDecimal ig_trans_fee;

    private long goods_order_id;

    private String goods_order_sn;

    public long getGoods_order_id() {
        return goods_order_id;
    }

    public void setGoods_order_id(long goods_order_id) {
        this.goods_order_id = goods_order_id;
    }

    public String getGoods_order_sn() {
        return goods_order_sn;
    }

    public void setGoods_order_sn(String goods_order_sn) {
        this.goods_order_sn = goods_order_sn;
    }

    public long getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(long goods_id) {
        this.goods_id = goods_id;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public int getGoods_integral() {
        return goods_integral;
    }

    public void setGoods_integral(int goods_integral) {
        this.goods_integral = goods_integral;
    }

    public int getGoods_count() {
        return goods_count;
    }

    public void setGoods_count(int goods_count) {
        this.goods_count = goods_count;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public BigDecimal getIg_trans_fee() {
        return ig_trans_fee;
    }

    public void setIg_trans_fee(BigDecimal ig_trans_fee) {
        this.ig_trans_fee = ig_trans_fee;
    }

    public ApiIntegralGoods() {
        super();
    }

    public ApiIntegralGoods(long goods_id, String goods_name, int goods_integral, int goods_count, String image,
                            BigDecimal ig_trans_fee) {
        super();
        this.goods_id = goods_id;
        this.goods_name = goods_name;
        this.goods_integral = goods_integral;
        this.goods_count = goods_count;
        this.image = image;
        this.ig_trans_fee = ig_trans_fee;
    }

    @Override
    public String toString() {
        return "ApiIntegralGoods [goods_id=" + goods_id + ", goods_name=" + goods_name + ", goods_integral="
                + goods_integral + ", goods_count=" + goods_count + ", image=" + image + ", ig_trans_fee="
                + ig_trans_fee + "]";
    }

}
