package com.jiangyifen.ec2.eao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.eao.IvrMenuEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;

public class IvrMenuEaoImpl extends BaseEaoImpl implements IvrMenuEao {
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IVRMenu> getAllByDomain(Long domainId) {
		return getEntityManager().createQuery("select i from IVRMenu as i where i.domain.id = "+domainId+" order by i.id desc").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IVRMenu> getAllByDomain(Long domainId, IVRMenuType menuType) {
		String jpql = "select m from IVRMenu as m where m.domain.id = "+domainId+" and m.ivrMenuType = "+getIvrMenuTypeSql(IVRMenuType.customize)+" order by m.id desc";
		return this.getEntityManager().createQuery(jpql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getEntities(Class<T> entityClass, String jpql) {
		return getEntityManager().createQuery(jpql).getResultList();
	}

	/** 删除IVRMenu 时，该方法内部还将与该IVRMenu 相关的 IVRAction、IVROption, 以及OutlineToIvrLink 一同删除 */
	@Override
	public void delete(IVRMenu ivrMenu) {
		Long ivrMenuId = ivrMenu.getId();
		String optionDeleteSql = "delete from ec2_ivr_option where ivrmenu_id = "+ivrMenuId;
		String actionDeleteSql = "delete from ec2_ivr_action where ivrmenu_id = "+ivrMenuId;
		String menuDeleteJpql = "delete from IVRMenu as m where m.id = "+ivrMenuId;
		String menuToOulintDeleteSql = "delete from ec2_outline_to_ivr_link where ivrmenuid = "+ ivrMenuId;
		
		this.getEntityManager().createNativeQuery(optionDeleteSql).executeUpdate();
		this.getEntityManager().createNativeQuery(actionDeleteSql).executeUpdate();
		this.getEntityManager().createQuery(menuDeleteJpql).executeUpdate();
		this.getEntityManager().createNativeQuery(menuToOulintDeleteSql).executeUpdate();
	}

	/** 删除IVRMenu 时，该方法内部还将与该IVRMenu 相关的 IVRAction、IVROption, 以及OutlineToIvrLink 一同删除 */
	@Override
	public void deleteById(Long ivrMenuId) {
		String optionDeleteSql = "delete from ec2_ivr_option where ivrmenu_id = "+ivrMenuId;
		String actionDeleteSql = "delete from ec2_ivr_action where ivrmenu_id = "+ivrMenuId;
		String menuDeleteJpql = "delete from IVRMenu as m where m.id = "+ivrMenuId;
		String menuToOulintDeleteSql = "delete from ec2_outline_to_ivr_link where ivrmenuid = "+ ivrMenuId;
		
		this.getEntityManager().createNativeQuery(optionDeleteSql).executeUpdate();
		this.getEntityManager().createNativeQuery(actionDeleteSql).executeUpdate();
		this.getEntityManager().createQuery(menuDeleteJpql).executeUpdate();
		this.getEntityManager().createNativeQuery(menuToOulintDeleteSql).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SipConfig> getRelatedOutlineByIVRMenuId(Long ivrMenuId) {
		String outlineIdsSql = "select outlineid from ec2_outline_to_ivr_link where ivrmenuid = "+ivrMenuId + " order by outlineid desc";
		List<Long> outlineIds =  this.getEntityManager().createNativeQuery(outlineIdsSql).getResultList();
		if(outlineIds.size() == 0) {
			return new ArrayList<SipConfig>();
		} 
		
		String jpql = "select e from SipConfig as e where e.id in ("+StringUtils.join(outlineIds, ",")+") order by e.id desc";
		List<SipConfig> outlines = this.getEntityManager().createQuery(jpql).getResultList();
		return outlines;
	}

	/** 根据IVR 模板，创建一个新指定类型的IVR */
	@SuppressWarnings("unchecked")
	@Override
	public boolean createNewIvrByIvrTemplate(IVRMenu ivrMenu, IVRActionType ivrActionType) {
		Domain domain = ivrMenu.getDomain();
		Long domainId = domain.getId();
		EntityManager em = this.getEntityManager();
		HashSet<IVRAction> newActionsSet = new HashSet<IVRAction>();
		
		// 1、根据ivrActionType 获取 IVR 模板
		List<IVRMenu> menuTemps = em.createQuery("select m from IVRMenu as m where m.domain.id = "+domainId
				+" and m.ivrMenuType = "+getIvrMenuTypeSql(IVRMenuType.template)+" and m.rootActionType = "+getIvrActionTypeSql(ivrActionType)).getResultList();
		IVRMenu templateMenu = null;
		if(menuTemps.size() > 0) {
			templateMenu = menuTemps.get(0);
		} else {
			throw new RuntimeException("IVRMenu 原始表的 中没有初始化 模板数据, 请先写入初始化IVRMenu数据!");
		}
		
		List<IVRAction> actionTemps = em.createQuery("select a from IVRAction as a where a.domain.id = "+domainId+" and a.ivrMenu.id = "+templateMenu.getId()+" and a.isRootAction = true").getResultList();
		IVRAction tempRootAction = null;
		if(actionTemps.size() > 0) {
			tempRootAction = actionTemps.get(0);
		} else {
			throw new RuntimeException("IVRAction 原始表的 中没有初始化 模板数据, 请先写入初始化IVRAction数据!");
		}

		// 2、保存ivrMenu 对象
		em.persist(ivrMenu);
		
		// 3、创建 Root Action 
		HashMap<Long, IVRAction> tempId2NewActionMap = new HashMap<Long, IVRAction>();	// 所有已经被复制的 模板action的编号，与新建的action的对应关系
		IVRAction rootAction = createIvrAction(domain, tempRootAction, ivrMenu, tempId2NewActionMap, em);
		newActionsSet.add(rootAction);

		// 级联去创建 rootAction 下的子action 及 option 
		cascadeCreateActionAndOption(domain, ivrMenu, rootAction, tempRootAction, newActionsSet, tempId2NewActionMap, em);

		return true;
	}

	/**
	 * jrh 根据模板中上级 IVRAction，查询其对应的所有按键IVROption， 使用 递归算法，来创建 按键对应的下一步 action。
	 *  
	 * @param domain				域
	 * @param ivrMenu				新建的IVRMenu
	 * @param parentAction			上级IVRAction
	 * @param tempParentAction		对应的模板中上级IVRAction
	 * @param newActionsSet			新建的IVRAction 集合， 用于管理ivrMenu 下所有新建的 action 
	 * @param tempId2NewActionMap	所有已经被复制的 模板action的编号，与新建的action的对应关系
	 * @param em					entityManager
	 */
	@SuppressWarnings("unchecked")
	private void cascadeCreateActionAndOption(Domain domain, IVRMenu ivrMenu, IVRAction parentAction, IVRAction tempParentAction, 
			HashSet<IVRAction> newActionsSet, HashMap<Long, IVRAction> tempId2NewActionMap, EntityManager em) {
		
		tempId2NewActionMap.put(tempParentAction.getId(), parentAction);	// 这里加入的action 的类型是 toRead 【播放语音，等待按键】，这个添加操作必须放在这里
		
		// 如果当前的 tempRootAction 有按键配置，则根据按键配置创建 action、option
		HashSet<IVROption> newOptions = new HashSet<IVROption>();

		String jpql = "select o from IVROption as o where o.domain.id = "+domain.getId()+" and o.currentIvrAction.id = "+tempParentAction.getId();
		List<IVROption> optionTemps = this.getEntityManager().createQuery(jpql).getResultList();
		optionTemps = sortOptionsByPressKey(optionTemps);		// 将传入的IVROption 的集合，按 option 的按键进行排序 ，排序的顺序按数字由小到大，如 0、1、2、3、4、5、6、7、8、9、*
		
		for(IVROption optionTemp : optionTemps) {	// 遍历，获取按键 option 对应的 下一级 action，进行创建新的action
			IVRAction nextActionTemp = optionTemp.getNextIvrAction();
			Long nextActId = nextActionTemp.getId();
			
			IVRAction newNextAction = tempId2NewActionMap.get(nextActId);
			if(newNextAction == null) {	// 如果下一步的 action 还没有被创建，则重新创建
				newNextAction = createIvrAction(domain, nextActionTemp, ivrMenu, tempId2NewActionMap, em);
			}
			newActionsSet.add(newNextAction);	// 加入到 新建的action 集合中
			
			// 根据目标option ： optionTemp， 创建新的 按键Option 对象
			IVROption newOption = createIvrOption(domain, optionTemp, parentAction, newNextAction, ivrMenu, em);
			newOptions.add(newOption);
			
			// 如果当前按键对应的下一级 action 的类型是 toRead 【播放语音，等待按键】, 并且下一级 action 的ID 值还没有被加入到 tempId2NewActionMap,则需要递归执行
			if( IVRActionType.toRead.equals(nextActionTemp.getActionType()) && !tempId2NewActionMap.keySet().contains(nextActId) ) {
				cascadeCreateActionAndOption(domain, ivrMenu, newNextAction, nextActionTemp, newActionsSet, tempId2NewActionMap, em);
			}
		}
	}

	/**
	 * jrh 根据模板中的IVRAction : tempAction 来创建新的action对象
	 * 
	 * @param domain				域
	 * @param tempAction			模板中对应的action对象
	 * @param ivrMenu				当前创建的IVRMenu对象
	 * @param tempId2NewActionMap	所有已经被复制的 模板action的编号，与新建的action的对应关系
	 * @param em					entityManager
	 * @return IVRAction
	 */
	private IVRAction createIvrAction(Domain domain, IVRAction tempAction, IVRMenu ivrMenu, HashMap<Long, IVRAction> tempId2NewActionMap, EntityManager em) {
		IVRAction ivrAction = new IVRAction();
		ivrAction.setActionType(tempAction.getActionType());
		ivrAction.setDescription(tempAction.getDescription());
		ivrAction.setDomain(domain);
		ivrAction.setErrorOpportunity(tempAction.getErrorOpportunity());
		ivrAction.setExtenName(tempAction.getExtenName());
		ivrAction.setIsRootAction(tempAction.getIsRootAction());
		ivrAction.setIvrActionName(tempAction.getIvrActionName());
		ivrAction.setIvrMenu(ivrMenu);
		ivrAction.setMobileNumber(tempAction.getMobileNumber());
		ivrAction.setOutlineName(tempAction.getOutlineName());
		ivrAction.setQueueName(tempAction.getQueueName());
		ivrAction.setSoundFile(tempAction.getSoundFile());
		em.persist(ivrAction);
		if( !IVRActionType.toRead.equals(tempAction.getActionType()) ) {	// 如果类型是 toRead 【播放语音，等待按键】，则不再该方法中，添加对应关系，因为它还需要调用递归 cascadeCreateActionAndOption(...)
			tempId2NewActionMap.put(tempAction.getId(), ivrAction);
		}
		return ivrAction;
	}

	/**
	 * jrh 根据模板中的IVROption : tempOption 来创建新的option对象
	 * 
	 * @param domain				域
	 * @param tempOption			模板中对应的IVRAction
	 * @param currentIvrAction	            按键对应的当前action
	 * @param nextIvrAction			按键对应的下一步action
	 * @param ivrMenu				新建的IVRMenu
	 * @param em					entityManager
	 * @return IVROption
	 */
	private IVROption createIvrOption(Domain domain, IVROption tempOption, IVRAction currentIvrAction, IVRAction nextIvrAction, IVRMenu ivrMenu, EntityManager em) {
		IVROption ivrOption = new IVROption();
		ivrOption.setDescription(tempOption.getDescription());
		ivrOption.setIvrOptionName(tempOption.getIvrOptionName());
		ivrOption.setLayerNumber(tempOption.getLayerNumber());
		ivrOption.setPressNumber(tempOption.getPressNumber());
		ivrOption.setIvrOptionType(tempOption.getIvrOptionType());
		ivrOption.setDomain(domain);
		ivrOption.setIvrMenu(ivrMenu);
		ivrOption.setCurrentIvrAction(currentIvrAction);
		ivrOption.setNextIvrAction(nextIvrAction);
		em.persist(ivrOption);
		return ivrOption;
	}
	
	
	/**
	* jrh 根据IVRMenu 类型对象，得到用于查询IVRMenu 类型的字符串语句
	* 
	* @param IVRMenuType
	*            IVRMenu类型
	* @return
	*/
	private String getIvrMenuTypeSql(IVRMenuType ivrActionType) {
		String typeStr = ivrActionType.getClass().getName() + ".";
		
		if (ivrActionType.getIndex() == 0) {
			typeStr += "customize";
		} else if (ivrActionType.getIndex() == 1) {
			typeStr += "template";
		} 
		return typeStr;
	}
			
	/**
	 * jrh 根据IVRAction 类型对象，得到用于查询IVRAction 类型的字符串语句
	 * 
	 * @param IVRActionType
	 *            IVRAction类型
	 * @return
	 */
	private String getIvrActionTypeSql(IVRActionType ivrActionType) {
		String typeStr = ivrActionType.getClass().getName() + ".";
		
		if (ivrActionType.getIndex() == 0) {
			typeStr += "toExten";
		} else if (ivrActionType.getIndex() == 1) {
			typeStr += "toQueue";
		} else if (ivrActionType.getIndex() == 2) {
			typeStr += "toMobile";
		} else if (ivrActionType.getIndex() == 3) {
			typeStr += "toPlayback";
		} else if (ivrActionType.getIndex() == 4) {
			typeStr += "toRead";
		} else if (ivrActionType.getIndex() == 5) {
			typeStr += "toReadForAgi";
		}
		return typeStr;
	}

	/** 将传入的IVROption 的集合，按 option 的按键进行排序 ，排序的顺序按数字由小到大，如 0、1、2、3、4、5、6、7、8、9、 * 等  */
	@Override
	public List<IVROption> sortOptionsByPressKey(List<IVROption> ivrOptions) {
		Collections.sort(ivrOptions, new Comparator<IVROption>() {
			@Override
			public int compare(IVROption o1, IVROption o2) {
				String key1 = pickOutNumbers(o1.getPressNumber());
				String key2 = pickOutNumbers(o2.getPressNumber());
				
				if("".equals(key1)) {	// 如果第一个是的按键非数字，则将 o1 往后排
					return 1;
				} else if("".equals(key2)) {	// 如果比较的第二个为非数字，则 o1 往前排 
					return -1;
				}
				
				return Integer.parseInt(key1) - Integer.parseInt(key2);
			}

		    /**
		     * 取出按键中的数字，如果没有数字，则返回 空字符串
		     * @param originalData 原始数据
		     * @return
		     */
		    private String pickOutNumbers(String originalData) {
		          String numbers = "";
		          originalData = StringUtils.trimToEmpty(originalData);
		          for(int i = 0; i < originalData.length(); i++) {
		               char c = originalData.charAt(i);
		               int assiiCode = (int) c;
		               if(assiiCode >= 48 && assiiCode <= 57) {
		                    numbers = numbers + c;
		               }
		          }
		          return numbers;
		    }
		    
		});
		
		return ivrOptions;
	}
	
}
