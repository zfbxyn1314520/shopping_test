package com.shopping.foundation.domain.api;

public class ApiGoodsSpecProperty {
		
	private long id;
	
	private String value;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ApiGoodsSpecProperty(long id, String value) {
		super();
		this.id = id;
		this.value = value;
	}

	public ApiGoodsSpecProperty() {
		super();
	}

	@Override
	public String toString() {
		return "ApiGoodsSpecProperty [id=" + id + ", value=" + value + "]";
	}
	
	
}
