package com.shopping.foundation.domain.api;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.List;

public class ApiGoodsBalance {

    //店铺id
    private long store_id;

    //店铺名称
    private String store_name;

    //商品所在购物id
    private long storeCart_id;

    //商品总价
    @Column(precision = 12, scale = 2)
    private BigDecimal total_price;

    //商品总数
    private int count;

    //商品列表
    private List<ApiGoodsCart> gcs;

    public long getStore_id() {
        return store_id;
    }

    public void setStore_id(long store_id) {
        this.store_id = store_id;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public long getStoreCart_id() {
        return storeCart_id;
    }

    public void setStoreCart_id(long storeCart_id) {
        this.storeCart_id = storeCart_id;
    }

    public BigDecimal getTotal_price() {
        return total_price;
    }

    public void setTotal_price(BigDecimal total_price) {
        this.total_price = total_price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ApiGoodsCart> getGcs() {
        return gcs;
    }

    public void setGcs(List<ApiGoodsCart> gcs) {
        this.gcs = gcs;
    }

}
