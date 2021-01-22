package com.jiangyifen.ec2.utils;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

public class WebSessionHolder implements Serializable {
	private static final long serialVersionUID = -4440400201743448314L;

	private HttpSession httpSession;
	
	public HttpSession getHttpSession() {
		return httpSession;
	}

	public void setHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}
	
}

