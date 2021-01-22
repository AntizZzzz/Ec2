package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.SipConfigType;
import com.jiangyifen.ec2.eao.SipConfigEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;

public class SipConfigEaoImpl extends BaseEaoImpl implements SipConfigEao {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void save(SipConfig sipConfig) {
		getEntityManager().persist(sipConfig);
	}
	
	@Override
	public SipConfig update(SipConfig sipConfig) {
		return getEntityManager().merge(sipConfig);
	}

	@Override
	public void delete(SipConfig sipConfig) {
		// jrh 如果当前删除的是外线，则需删除外线与黑名单之间的对应关系
		SipConfigType type = sipConfig.getSipType();
		if(type.equals(SipConfigType.sip_outline) || type.equals(SipConfigType.gateway_outline)) {
			// 删除外线与黑名单之间的对应关系
			String sql = "delete from ec2_outline_2_blacklist_item_link where outlineid = "+ sipConfig.getId();
			this.getEntityManager().createNativeQuery(sql).executeUpdate();
			// 清理外线与IVR 的关联关系
			String otilJpql = "delete from ec2_outline_to_ivr_link where outlineid = "+sipConfig.getId();
			this.getEntityManager().createNativeQuery(otilJpql).executeUpdate();
		}
		// 删分机或外线
		this.delete(sipConfig);
	}

	@Override
	public void deleteById(Object primaryKey) {
		// jrh 需删除外线与黑名单之间的对应关系[按理该判定当前删除的是不是外线]
		String sql = "delete from ec2_outline_2_blacklist_item_link where outlineid = " + primaryKey;
		this.getEntityManager().createNativeQuery(sql).executeUpdate();

		// 清理外线与IVR 的关联关系
		String otilJpql = "delete from ec2_outline_to_ivr_link where outlineid = "+primaryKey;
		this.getEntityManager().createNativeQuery(otilJpql).executeUpdate();
		
		// 删分机或外线
		this.delete(SipConfig.class, primaryKey);
	}
	
	@Override
	public boolean existBySipname(String sipname) {
		Long count = (long)0;
		try {
			count = (Long) getEntityManager().createQuery("select count(sip) from SipConfig as sip where sip.name = :sipname")
					.setParameter("sipname", sipname).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		}
		if(count > 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean existBySipname(String sipname, Domain domain) {
		Long count = (long)0;
		try {
			count = (Long) getEntityManager().createQuery("select count(sip) from SipConfig as sip where sip.name = :sipname and sip.domain.id = :domainId")
					.setParameter("sipname", sipname).setParameter("domainId", domain.getId()).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		}
		if(count > 0) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SipConfig> getAllExtsByDomain(Domain domain) {
		String sipTypeSql = getSipTypeSql(SipConfigType.exten);
		return getEntityManager().createQuery("select sip from SipConfig as sip where sip.sipType = " +sipTypeSql+ " and sip.domain.id = " + domain.getId() + " order by sip.name asc").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SipConfig> getAllOutlinesByDomain(Domain domain) {
		String sipTypeSql = "(" +getSipTypeSql(SipConfigType.sip_outline) +"," +getSipTypeSql(SipConfigType.gateway_outline) +")";
		return getEntityManager().createQuery("select sip from SipConfig as sip where sip.sipType in " +sipTypeSql+ " and  sip.domain.id = " + domain.getId() + " order by sip.name asc").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SipConfig getDefaultOutlineByDomain(Domain domain) {
		String sipTypeSql = "(" +getSipTypeSql(SipConfigType.sip_outline) +"," +getSipTypeSql(SipConfigType.gateway_outline) +")";
		 List<SipConfig> sips = getEntityManager().createQuery("select sip from SipConfig as sip where sip.sipType in " +sipTypeSql+ " and sip.isDefaultOutline = 'true' and  sip.domain.id = " + domain.getId()).getResultList();
		 if(sips.size() > 0) {
			 return sips.get(0);
		 }
		 return null;
	}
	

	/**
	 * jrh 根据SipConfig对象，得到用于查询sip 的类型创建收索语句语句
	 * @param 	sipConfigType 	sip类型
	 * @return 	String
	 */
	private String getSipTypeSql(SipConfigType sipConfigType) {
		String statuStr = sipConfigType.getClass().getName() + ".";
		if (sipConfigType.getIndex() == 0) {
			statuStr += "exten";
		} else if (sipConfigType.getIndex() == 1) {
			statuStr += "sip_outline";
		} else if (sipConfigType.getIndex() == 2) {
			statuStr += "gateway_outline";
		}
		return statuStr;
	}
	
	/**
	 * chb 
	 * 根据外线取得domain
	 * @return
	 */
	@Override
	public Domain getDomainByOutLine(String outline) {
		String sipTypeSql = "(" +getSipTypeSql(SipConfigType.sip_outline) +"," +getSipTypeSql(SipConfigType.gateway_outline) +")";
		String sql="select sip.domain from SipConfig sip where sip.sipType in " +sipTypeSql+ " and sip.name= '"+outline+"'";
		//一定有且仅有一个Domain符合条件，如果没有，则传入的参数错误
		Domain domain = null;
		try {
			domain=(Domain)getEntityManager().createQuery(sql).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		}
		
		return domain;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Long getMaxSipnameInExt() {
		String sipTypeSql = getSipTypeSql(SipConfigType.exten);
		List<SipConfig> sips = getEntityManager().createQuery("select sip from SipConfig as sip where sip.name = (select max(sip2.name) from SipConfig as sip2 where sip2.sipType = " +sipTypeSql+ ")").getResultList();
		if(sips.size() > 0) {
			return Long.parseLong(sips.get(0).getName());
		}
		return 800000L;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Long getMaxSipnameInExt6() {
		String sipTypeSql = getSipTypeSql(SipConfigType.exten);
		List<SipConfig> sips = getEntityManager().createQuery("select sip from SipConfig as sip where sip.name = (select max(sip2.name) from SipConfig as sip2 where sip2.sipType = " +sipTypeSql+ ")").getResultList();
		if(sips.size() > 0) {
			return Long.parseLong(sips.get(0).getName());
		}
		return 60000L;
	}
	
	/**
	 * chb 
	 * 根据domain 取得所有没有被其他项目使用的外线
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SipConfig> getAllUseableOutlinesByDomain(Domain domain) {
		String sipTypeSql = "(" +getSipTypeSql(SipConfigType.sip_outline) +"," +getSipTypeSql(SipConfigType.gateway_outline) +")";
		return getEntityManager().createQuery("select sip from SipConfig as sip where sip.sipType in " +sipTypeSql+ " and sip.marketingProject=null and sip.domain.id = " + domain.getId() + " order by sip.name asc").getResultList();
	}
	
	/**
	 * chb 
	 *根据外线的名字取出一条外线
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SipConfig getOutlineByOutlineName(String outlineName) {
		List<SipConfig> outlines=getEntityManager().createQuery("select sip from SipConfig as sip where sip.name='"+outlineName+"'").getResultList();
		if(outlines.size()>0){
			return outlines.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkDeleteAbleByIvrAction(boolean isExten, String sipName, Long domainId) {
		// 先查出所有模板IVR 的编号
		String menuJpql = "select id from ec2_ivr_menu where domain_id = "+domainId+" and ivrmenutype = "+IVRMenuType.template.getIndex()+" order by id desc";
		List<Long> tempMenuIds = this.getEntityManager().createNativeQuery(menuJpql).getResultList();
		tempMenuIds.add(0L);
		String tempMenuIdSql = StringUtils.join(tempMenuIds, ",");
		
		String nativeSql = "select count(*) from ec2_ivr_action where domain_id = "+domainId+" and extenname = '"+sipName+"' and ivrmenu_id not in ("+tempMenuIdSql+")";
		if(!isExten) {
			nativeSql = "select count(*) from ec2_ivr_action where domain_id = "+domainId+" and outlinename = '"+sipName+"' and ivrmenu_id not in ("+tempMenuIdSql+")";
		}
		
		Long count = (Long) this.getEntityManager().createNativeQuery(nativeSql).getSingleResult();
		if(count > 0) {
			return false;
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void checkIvrActionAndUpdate(boolean isExten, String sipName, Long domainId) {
		String jpql = "select i from IVRAction as i where i.domain.id = "+domainId+" and i.outlineName = '"+sipName+"'";
		if(isExten) {
			jpql = "select i from IVRAction as i where i.domain.id = "+domainId+" and i.extenName = '"+sipName+"'";
		}
		
		List<IVRAction> actions = this.getEntityManager().createQuery(jpql).getResultList();
		for(IVRAction action : actions) {
			if(isExten) {
				action.setExtenName(sipName);
			} else {
				action.setOutlineName(sipName);
			}
			this.getEntityManager().merge(action);
		}
		
	}
	
}
