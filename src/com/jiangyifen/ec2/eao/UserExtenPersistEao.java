package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.UserExtenPersist;


public interface UserExtenPersistEao extends BaseEao {

	public List<UserExtenPersist> getAll();

	public void updateExtenToUser(String exten, Long userId);

	public void removeExtenToUser(String exten);
}
