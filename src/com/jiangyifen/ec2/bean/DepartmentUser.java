package com.jiangyifen.ec2.bean;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.User;

public class DepartmentUser {
	private Department deparement;
	private User user;
	
	public Department getDeparement() {
		return deparement;
	}
	public void setDeparement(Department deparement) {
		this.deparement = deparement;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
}
