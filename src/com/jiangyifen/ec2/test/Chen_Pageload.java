package com.jiangyifen.ec2.test;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
/**
 * 一定记得order by id desc
 * <p>customerresources_id<"+recordId+" </p>
 */
public class Chen_Pageload {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("ec2");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
//=============================================================//
		Long recordId = Long.MAX_VALUE;
		int step=2;
		// 每次加载step步长条数据
		List<Long> records = null;
		for(;;){
			System.out.println("==============for================");
			
			String nativeSql = "SELECT customerresources_id FROM " +
					"ec2_customer_resource_ec2_customer_resource_batch " +
					"where customerresourcebatches_id=264 and customerresources_id<"+recordId+" " +
					"order by customerresources_id desc";
			records = em.createNativeQuery(nativeSql).setFirstResult(0).setMaxResults(step).getResultList();
			if(records.size()>0){
				//取得最小的Id值
				recordId=records.get(records.size()-1);
			}else{
				break;
			}
			//对于数据进行处理1
//			for(Long temp:records){
//				System.out.println(temp);
//			}
			
			//对于数据进行处理2
			StringBuilder sb = new StringBuilder();
			sb.append('(');
			Iterator<Long> recordsIter = records.iterator();
			while(recordsIter.hasNext()){
				Long id=recordsIter.next();
				sb.append(id);
				if(recordsIter.hasNext())
					sb.append(",");
			}
			sb.append(')');
			System.out.println(sb.toString());
		}
//=============================================================//
		em.getTransaction().commit();
	}
}
