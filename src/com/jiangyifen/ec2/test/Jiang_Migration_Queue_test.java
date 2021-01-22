package com.jiangyifen.ec2.test;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.Queue;

public class Jiang_Migration_Queue_test {
	
	private static ArrayList<Queue> queueList = new ArrayList<Queue>();
	
	public static void main(String[] args) {
		migrationQueue();

	}
	
	public static void migrationQueue() {
		getQueueOldDataBase();
		importQueue();
	}

	@SuppressWarnings("unchecked")
	private static void getQueueOldDataBase() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec_old");
		EntityManager em_old = entityMangerFactory.createEntityManager();
		em_old.getTransaction().begin();
		
		List<Object[]> userStrs= em_old.createNativeQuery("select * from queue_table").getResultList();
		
		long i = 1;
		for(Object[] os : userStrs) {
			for(Object o : os) {
				System.out.println(o);
			}
			Queue queue = new Queue();
			queue.setId(i);
			queue.setName(os[0].toString());
			
			Long timeout = Integer.valueOf(os[4]+"").longValue();
			queue.setTimeout(timeout);
			queue.setMonitor_format(os[6].toString());
			
			Long announce_frequency = Integer.valueOf(os[16]+"").longValue();
			queue.setAnnounce_frequency(announce_frequency);
			
			Long announce_round_seconds = Integer.valueOf(os[17]+"").longValue();
			queue.setAnnounce_round_seconds(announce_round_seconds);
			
			Long wrapuptime = Integer.valueOf(os[20]+"").longValue();
			queue.setWrapuptime(wrapuptime);
			
			Long maxlen = Integer.valueOf(os[21]+"").longValue();
			queue.setMaxlen(maxlen);
			
			queue.setStrategy(os[23].toString());
			queue.setJoinempty(os[24].toString());
			queue.setSetinterfacevar((Boolean)os[32]);
			queue.setAutopause(os[33].toString());
			queue.setDescription(os[34].toString());
			queue.setDynamicmember((Boolean)os[37]);
			queueList.add(queue);
			
			System.out.println(queue.getId());
			System.out.println("---------------------" + (i++) + "----------------");
		}
		
		em_old.getTransaction().commit();
		em_old.close();
		entityMangerFactory.close();
	}
	
	@SuppressWarnings("unchecked")
	private static void importQueue() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em_new = entityMangerFactory.createEntityManager();
		
		em_new.getTransaction().begin();
		
		List<Domain> domains = em_new.createQuery("select d from Domain as d").getResultList();
//		List<MusicOnHold> mohs = (List<MusicOnHold>) em_new.createQuery("select moh from MusicOnHold as moh where moh.id = 11").getResultList();
//		MusicOnHold moh = mohs.get(0);
//		System.out.println(moh);
		for(Domain domain : domains) {

			MusicOnHold moh = new MusicOnHold();
			moh.setDomain(domain);
			moh.setDirectory("moh");
			moh.setMode("files");
			moh.setDescription("系统默认语音文件夹");
			moh.setName("default");
			em_new.persist(moh);

			for(int i=0; i<queueList.size(); i++) {
				Queue queue = (Queue) queueList.toArray()[i];
				queue.setDomain(domain);
				queue.setMusiconhold(moh);
				queue.setName(queue.getName());
				em_new.persist(queue);
			}
		}
		
		em_new.getTransaction().commit();
		em_new.close();
		entityMangerFactory.close();
	}
}
