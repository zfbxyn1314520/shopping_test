package com.shopping.core.domain.virtual;

public class FeeMap {

    private Object fee_name;
    private Object fee_price;

    public FeeMap() {
    }

    public FeeMap(Object fee_name, Object fee_price) {
        this.fee_name = fee_name;
        this.fee_price = fee_price;
    }

    public Object getFee_name() {
        return fee_name;
    }

    public void setFee_name(Object fee_name) {
        this.fee_name = fee_name;
    }

    public Object getFee_price() {
        return fee_price;
    }

    public void setFee_price(Object fee_price) {
        this.fee_price = fee_price;
    }
}
