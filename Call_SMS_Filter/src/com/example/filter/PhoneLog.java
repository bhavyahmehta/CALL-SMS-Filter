package com.example.filter;

import android.app.Application;

public class PhoneLog extends Application
{
	String phone_no;

	public String getPhone_no() {
		return phone_no;
	}

	public void setPhone_no(String phone_no) {
		this.phone_no = phone_no;
	}

}
