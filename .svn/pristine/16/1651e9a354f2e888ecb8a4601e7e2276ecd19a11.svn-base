package com.shopping.foundation.domain.api;

import javax.persistence.Column;
import java.math.BigDecimal;

public class ApiGoodsCart {

    //商品id
    private long goods_id;
    //店铺的id
    private long store_id;
    //商品名称
    private String goods_name;
    //商品图片
    private String goods_img;
    //数量
    private int count;
    //最后成交价格
    @Column(precision = 12, scale = 2)
    private BigDecimal price;
    //商品原价
    @Column(precision = 12, scale = 2)
    private BigDecimal goods_price;
    //商品现价
    @Column(precision = 12, scale = 2)
    private BigDecimal store_price;
    //商品规格
    private String spec_info;
    //是否被用户评价
    private boolean evaluate;
    //是否被用户投诉
    private boolean complaint;


    public long getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(long goods_id) {
        this.goods_id = goods_id;
    }

    public long getStore_id() {
        return store_id;
    }

    public void setStore_id(long store_id) {
        this.store_id = store_id;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public String getGoods_img() {
        return goods_img;
    }

    public void setGoods_img(String goods_img) {
        this.goods_img = goods_img;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getGoods_price() {
        return goods_price;
    }

    public void setGoods_price(BigDecimal goods_price) {
        this.goods_price = goods_price;
    }

    public BigDecimal getStore_price() {
        return store_price;
    }

    public void setStore_price(BigDecimal store_price) {
        this.store_price = store_price;
    }

    public String getSpec_info() {
        return spec_info;
    }

    public void setSpec_info(String spec_info) {
        this.spec_info = spec_info;
    }

    public boolean isEvaluate() {
        return evaluate;
    }

    public void setEvaluate(boolean evaluate) {
        this.evaluate = evaluate;
    }

    public boolean isComplaint() {
        return complaint;
    }

    public void setComplaint(boolean complaint) {
        this.complaint = complaint;
    }

    public ApiGoodsCart() {
        super();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
