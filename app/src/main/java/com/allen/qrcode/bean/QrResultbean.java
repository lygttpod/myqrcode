package com.allen.qrcode.bean;

public class QrResultbean {

	private String result;
	private String time;
	private String type;

	public QrResultbean() {
		super();
		// TODO Auto-generated constructor stub
	}

	public QrResultbean(String result, String time, String type) {
		super();
		this.result = result;
		this.time = time;
		this.type = type;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
