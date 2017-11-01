package com.shopping.foundation.domain.api;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;

import com.shopping.foundation.domain.GoodsSpecification;

public class ApiIndexGoods {
	
	private long id;
	
	private String name;
	
	@Column(precision=12,scale=2)
	private BigDecimal goods_price;
	
	@Column(precision=12,scale=2)
	private BigDecimal store_price;
	
	private int goods_salenum;
	
	private String area_name;
	
	private List<Object> image;
	
	private List<Object> guige;
	
	private List<GoodsSpecification> gsf;

	public ApiIndexGoods() {
		
	}
	
	public ApiIndexGoods(long id, String name, BigDecimal goods_price, BigDecimal store_price, int goods_salenum,
			String area_name, List<Object> image, List<Object> guige, List<GoodsSpecification> gsf) {
		super();
		this.id = id;
		this.name = name;
		this.goods_price = goods_price;
		this.store_price = store_price;
		this.goods_salenum = goods_salenum;
		this.area_name = area_name;
		this.image = image;
		this.guige = guige;
		this.gsf = gsf;
	}

	public List<GoodsSpecification> getGsf() {
		return gsf;
	}

	public void setGsf(List<GoodsSpecification> gsf) {
		this.gsf = gsf;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public int getGoods_salenum() {
		return goods_salenum;
	}

	public void setGoods_salenum(int goods_salenum) {
		this.goods_salenum = goods_salenum;
	}

	public String getArea_name() {
		return area_name;
	}

	public void setArea_name(String area_name) {
		this.area_name = area_name;
	}

	public List<Object> getImage() {
		return image;
	}

	public void setImage(List<Object> image) {
		this.image = image;
	}

	public List<Object> getGuige() {
		return guige;
	}

	public void setGuige(List<Object> guige) {
		this.guige = guige;
	}



	@Override
	public String toString() {
		return "ApiIndexGoods [id=" + id + ", name=" + name + ", goods_price=" + goods_price + ", store_price="
				+ store_price + ", goods_salenum=" + goods_salenum + ", area_name=" + area_name + ", image=" + image
				+ ", guige=" + guige + ", gsf=" + gsf + "]";
	}
	
	
	
}
