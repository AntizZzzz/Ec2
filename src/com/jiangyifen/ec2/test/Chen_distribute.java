package com.jiangyifen.ec2.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;

public class Chen_distribute {
	public static void main(String[] args) {
//		distribute();
	}
	//负责存储每次调用distribute，再调用caculateToDistributeNum方法时计算出来的分配平均数值
	/**
	 * 分配操作
	 */
	@SuppressWarnings("unused")
	public static Long distribute(Long distributeNumLine){
		//如果输入上限超过1000，则分配加载资源并分配出现异常！如有需求请调整分批加载步长
		Long loadTaskStep=1000L;
		if(distributeNumLine>1000L){
			throw new RuntimeException("输入数值过大！分配上限不能超过1000条");
		}
		//根据用户目前含有的资源数对用户进行排序，资源最少的排在前面
		HashMap<User, Long> userNumMap = null;//wrapToMap(users, marketingProject, domain);
		
		//按照用户资源数由小到大进行排序的用户集合
		List<User> soredUser=getSortedUser(userNumMap);
		
		//对应用户现在有的已分配资源数量
		List<Long> numList=getSortedNum(userNumMap);
		//按照numList由大到小的排序
		List<Long> numListReverse=new ArrayList<Long>(numList);
		Collections.reverse(numListReverse);
		//得出应该分配的数量
		List<Long> toDistributeNum = caculateToDistributeNum(numListReverse, 500L,distributeNumLine);
		
		//允许分配的资源的集合
		List<MarketingProjectTask> assignableTasks=new ArrayList<MarketingProjectTask>();
		
		//以应该分配的数量为标准对用户进行资源分配
		//如果用户数少于要分配的数量，抛出异常
		if(soredUser.size()<toDistributeNum.size()) throw new RuntimeException("用户总数量不应该小于要分配资源的用户数量！");
		for(int i=0;i<toDistributeNum.size();i++){
			//如果资源的数量少于平均值，主要考虑如果界面没有大小限制，管理员输入的数值过大
			if(assignableTasks.size()<distributeNumLine){
				//加载 loadTaskStep 条数据
			}
			//取得i对应的用户
			User user=soredUser.get(i);
			//为该用户分配指定数量的任务
			for(Long j=0L;j<toDistributeNum.get(i);j++){
//				MarketingProjectTask 
			}
		}
		
		//实际分配的数量
		Long allDistributeNum=0L;
		for(Long num:toDistributeNum){
			allDistributeNum+=num;
		}
		return allDistributeNum;
	}
	/**
	 * 取出用户现在拥有的资源数的集合,并按照由小到大排序
	 * @param userNumMap
	 * @return
	 */
	private static List<Long> getSortedNum(HashMap<User, Long> userNumMap) {
		List<Long> numList=new ArrayList<Long>();
		for(User user:userNumMap.keySet()){
			numList.add(userNumMap.get(user));
		}
		Collections.sort(numList);
		return numList;
	}

	/**
	 * 取得按照用户的资源数由少到多的排序的一个集合
	 * @return
	 */
	private static List<User> getSortedUser(final HashMap<User, Long> userNumMap) {
		List<User> sortedUser=new ArrayList<User>(userNumMap.keySet());
		Collections.sort(sortedUser,new Comparator<User>() {
			//如果用户1对应的资源数大于用户2对应的资源数则为大于，以此类推
			@Override
			public int compare(User user1, User user2) {
				return (int)(userNumMap.get(user1)-userNumMap.get(user2));
			}
		});
		return sortedUser;
	}

	/**
	 * 据每个用户拥有的资源数和全部资源数计算应该为每个用户分配的数值
	 * @param numList 由大到小排序的用户现有资源数集合
	 * @param allNum 全部资源数
	 * @param assignNum	管理员指派的资源数
	 * @return 返回一个按用户资源从少到多排序的应该分配的资源数量的集合，如果所有用户资源数量都已经达到管理员指派的值，则返回空集合
	 */
	public static List<Long> caculateToDistributeNum(List<Long> numList,Long allNum,Long assignNum){
		Long avgNum=0L;//平均值
		Long remainNum=0L;
		
		//计算List中总数量是不是大于总数量，如果大于，抛出异常
		Long allListNum=0L;
		for(Long num:numList){
			allListNum+=num;
		}
		if(allListNum>allNum){
			throw new RuntimeException("numList 中的数量不应该大于 allNum 数量！");
		}
		
		//记录不符合条件的将要被移除的
		List<Long> toRemoveList=new ArrayList<Long>();
		//通过对所有用户已经分配的数量循环，找出应该分配的数量线
		for(int i=0;i<numList.size();i++){
			//当前的用户已分配的资源数量
			Long currentNum=numList.get(i);
			//如果当前用户已分配的资源数大于管理员指派的数量，将移除（不为此用户分配资源）
			if(currentNum>assignNum){
				toRemoveList.add(currentNum);
				allNum-=currentNum;//从总数中减去这个不用分配资源的用户数量
				continue;
			}
			
			//剩下的应该分配资源的用户数量
			int toDistributeUserNum=numList.size()-toRemoveList.size();
			//如果剩下应该分配数大于总数量，说明资源不足，应该继续减少分配资源的用户
			if(toDistributeUserNum*currentNum>allNum){
				toRemoveList.add(currentNum);
				allNum-=currentNum;//从总数中减去这个不用分配资源的用户数量
				continue;
			}else if(toDistributeUserNum*assignNum<=allNum){//说明资源足够可以分到管理员指派的水平
				avgNum=assignNum;//平均值
				remainNum=allNum-toDistributeUserNum*assignNum;//余数
				break;//退出循环
			}else if(toDistributeUserNum*currentNum<=allNum){//说明到达临界点，可以在此步确定能分配的平均值和按平均值分配后的余数，此时可能有一个用户不需要再分配资源
				avgNum=allNum/toDistributeUserNum;
				remainNum=allNum%toDistributeUserNum;
				break;//退出循环
			}
		}
		
		//对不需要分配资源的用户进行移除操作,得到计划分配资源的用户目前资源状态集合
		for(Long num:toRemoveList){
			numList.remove(num);
		}
		
		//存储每个用户应该分配的数量
		List<Long> toDistributeNums=new ArrayList<Long>();
		//按照avgNum计算每个用户应该分配的资源数量
		for(int i=0;i<numList.size();i++){
			toDistributeNums.add(avgNum-numList.get(i));
		}
		//如果平均数量达不到管理员指派的数量，并且余数大于0，则对余数的处理
		//将余数分配给原来含有资源数相对较高的几个用户
		if((avgNum!=assignNum)&&(remainNum>0)){
			if(toDistributeNums.size()<remainNum) throw new RuntimeException("分配余数，分配的用户数量不应该小于余数！");
			for(int i=0;i<remainNum;i++){
				//将原来的部分数值加1
				toDistributeNums.set(i, toDistributeNums.get(i)+1);
			}
		}
		//目前的应分配数量是由按用户资源数由多到少的排序，将之变为由少到多的排序
		Collections.reverse(toDistributeNums);
		return toDistributeNums;
	}
}
