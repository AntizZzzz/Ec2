package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.KbInfoTypeEao;
import com.jiangyifen.ec2.entity.KbInfoType;

import com.jiangyifen.ec2.service.eaoservice.KbInfoTypeService;
/**
* Service实现类：知识类型管理
* 
* @author lxy
* 
*/
public class KbInfoTypeServiceImpl implements KbInfoTypeService {

	
	/** 需要注入的Eao */
	private KbInfoTypeEao kbInfoTypeEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<KbInfoType> loadPageEntities(int start, int length, String sql) {
		return kbInfoTypeEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return kbInfoTypeEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得知识类型
	@Override
	public KbInfoType getKbInfoTypeById(Long id){
		return kbInfoTypeEao.get(KbInfoType.class, id);
	}
	
	// 保存知识类型
	@Override
	public void saveKbInfoType(KbInfoType kbInfoType){
		kbInfoTypeEao.save(kbInfoType);
	}
	
	// 更新知识类型
	@Override
	public KbInfoType updateKbInfoType(KbInfoType kbInfoType){
		return (KbInfoType)kbInfoTypeEao.update(kbInfoType);
	}
	
	// 删除知识类型
	@Override
	public void deleteKbInfoType(KbInfoType kbInfoType){
		kbInfoTypeEao.delete(kbInfoType);
	}
	
	@Override
	public List<KbInfoType> getKbInfoTypeListByDomain(Long domainid) {
		String jpql = "select s from KbInfoType as s where s.parenetType is null and s.domain.id = "+domainid + " order by s.id asc";
		List<KbInfoType> list = kbInfoTypeEao.loadKbInfoTypeList(jpql);
		return list;
		 
	}

	@Override
	public List<KbInfoType> getKbInfoTypeListByParenetId(Long id, Long domainid) {
		String jpql = "select s from KbInfoType as s where s.parenetType.id =  "+id + " and s.domain.id = "+domainid+" order by s.id asc";
		List<KbInfoType> list = kbInfoTypeEao.loadKbInfoTypeList(jpql);
		return list;
	}

	@Override
	public int getKbInfoTypeCountByParenetId(Long id, Long domainid) {
		String jpql = "select count(s) from KbInfoType as s where s.parenetType.id =  "+id +" and s.domain.id = "+domainid;
	 	return this.getEntityCount(jpql);
	}
	
	@Override
	public int getKbInfoTypeCountByName(String name, Long domainid) {
		String jpql = "select count(s) from KbInfoType as s where s.parenetType is null and s.name =  '"+name + "' and s.domain.id = "+domainid;
	 	return this.getEntityCount(jpql);
	}
	
	@Override
	public int getKbInfoTypeCountByNameAndParent(String name, long selectType,
			Long domainid) {
		String jpql = "select count(s) from KbInfoType as s where s.name =  '"+name + "' and s.parenetType.id= "+selectType+"  and s.domain.id = "+domainid;
	 	return this.getEntityCount(jpql);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from KbInfoType as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from KbInfoType as s where s. = "++" order by s.ordernumber asc ";
		List<KbInfoType> list = kbInfoTypeEao.loadKbInfoTypeList(jpql);
		return list;
	*
	*/
	
	
	//Eao注入
	public KbInfoTypeEao getKbInfoTypeEao() {
		return kbInfoTypeEao;
	}
	
	//Eao注入
	public void setKbInfoTypeEao(KbInfoTypeEao kbInfoTypeEao) {
		this.kbInfoTypeEao = kbInfoTypeEao;
	}

}