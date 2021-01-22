package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.eao.MemoryMethodEao;
import com.jiangyifen.ec2.entity.MemoryMethod;
import com.jiangyifen.ec2.service.eaoservice.MemoryMethodService;

/**
 * Service实现类：内存数据信息
 * 
 * @author JHT
 */
public class MemoryMethodServiceImpl implements MemoryMethodService {

	private MemoryMethodEao memoryMethodEao;
	
	@Override
	public Long findUserIdByEmpnoOrRealname(String keyWord) {
		StringBuffer sqlBuffer = new StringBuffer("select id from ec2_user where empno='"+keyWord+"' or realname='"+keyWord+"' ");
		try{
			Long id = Long.parseLong(keyWord);
			sqlBuffer.append(" or id="+id);
		} catch (Exception e){}
		return memoryMethodEao.getUserIdByEmpnoOrRealname(sqlBuffer.toString());
	}
	
	@Override
	public List<MemoryMethod> findAllMemoryMethods() {
		return memoryMethodEao.getAllMemoryMethods();
	}

	public  List<MemoryMethod> findByKeyWord(String keyName){
		List<MemoryMethod> memoryMethodList = new ArrayList<MemoryMethod>();
		StringBuffer sqlBuffer = new StringBuffer("select id,abbrname,description,keyprompt,methodname,name,parameter,simpledemo,singleprompt,type,valueprompt  from ec2_memory_method ");
		if(keyName != null && keyName.trim().length() > 0){
			sqlBuffer.append(" where name ~* '.*"+keyName+".*' or description like '%"+keyName+"%' or abbrname='"+keyName+"' ");
		}
		List<Object[]> objList = memoryMethodEao.getSqlByMemoryMethods(sqlBuffer.toString());
		for(Object[] objs : objList){
			MemoryMethod memoryMethod = new MemoryMethod();
			memoryMethod.setId(Long.parseLong(objs[0].toString()));
			memoryMethod.setAbbrName(objs[1].toString());
			memoryMethod.setDescription(objs[2].toString());
			memoryMethod.setKeyPrompt(objs[3].toString());
			memoryMethod.setMethodName(objs[4].toString());
			memoryMethod.setName(objs[5].toString());
			memoryMethod.setParameter(objs[6].toString());
			memoryMethod.setSimpleDemo(objs[7].toString());
			memoryMethod.setSinglePrompt(objs[8].toString());
			memoryMethod.setType(objs[9].toString());
			memoryMethod.setValuePrompt(objs[10].toString());
			memoryMethodList.add(memoryMethod);
		}
		return memoryMethodList;
	}
	
	public MemoryMethodEao getMemoryMethodEao() {
		return memoryMethodEao;
	}

	public void setMemoryMethodEao(MemoryMethodEao memoryMethodEao) {
		this.memoryMethodEao = memoryMethodEao;
	}
}
