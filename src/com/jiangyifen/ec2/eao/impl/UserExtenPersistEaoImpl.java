package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.UserExtenPersistEao;
import com.jiangyifen.ec2.entity.UserExtenPersist;

public class UserExtenPersistEaoImpl extends BaseEaoImpl implements UserExtenPersistEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<UserExtenPersist> getAll() {
		return (List<UserExtenPersist>) getEntityManager().createQuery("select uep from UserExtenPersist as uep").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateExtenToUser(String exten, Long userId) {
		List<UserExtenPersist> userExtens=(List<UserExtenPersist>)getEntityManager().createQuery("select uep from UserExtenPersist as uep where uep.exten="+exten).getResultList();
		//如果存在则更新
		if(userExtens.size()>0){
			UserExtenPersist userExtenPersist=userExtens.get(0);
			userExtenPersist.setUserId(userId);
			this.update(userExtenPersist);
		}else{//否则存储新对象
			UserExtenPersist userExtenPersist=new UserExtenPersist();
			userExtenPersist.setExten(exten);
			userExtenPersist.setUserId(userId);
			this.update(userExtenPersist);
		}
	}

	@Override
	public void removeExtenToUser(String exten) {
		getEntityManager().createQuery("delete from UserExtenPersist as uep where uep.exten="+exten).executeUpdate();
	}
}
