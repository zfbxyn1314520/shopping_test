package com.shopping.foundation.domain.api;

import com.shopping.foundation.domain.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eollse on 2017/5/26.
 */
public class ApiUser {

    private Long id;

    //用户名
    private String userName;

    //用户角色
    private String userRole;

    //照片路径
    private String path;

    //电话
    private String telephone;

    //地区名称
    private String areaName;

    //积分
    private int integral;

    //购物车数量
    private int cartGoodsCount;

    //邮箱
    private String email;

    //备注
    private String memo;

    public int getIntegral() {
        return integral;
    }

    public void setIntegral(int integral) {
        this.integral = integral;
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getPath() {
        return path;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public int getCartGoodsCount() {
        return cartGoodsCount;
    }

    public void setCartGoodsCount(int cartGoodsCount) {
        this.cartGoodsCount = cartGoodsCount;
    }
}
