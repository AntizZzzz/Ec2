package com.jiangyifen.ec2.test;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.bean.SipConfigType;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;

public class Jiang_Migration_SipConf_test {
	
	private static ArrayList<SipConfig> sipConfigs = new ArrayList<SipConfig>();
	
	public static void main(String[] args) {
		migrationSip();

	}
	
	public static void migrationSip() {
		getSipConfigFromOldDataBase();
		
		importSipConfig();
		
	}

	@SuppressWarnings("unchecked")
	private static void getSipConfigFromOldDataBase() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec_old");
		EntityManager em_old = entityMangerFactory.createEntityManager();
		em_old.getTransaction().begin();
		
		List<Object[]> userStrs= em_old.createNativeQuery("select * from sip_conf as c").getResultList();
		
		int i = 1;
		for(Object[] os : userStrs) {
			for(Object o : os) {
				System.out.println(o);
			}
			SipConfig sipConfig = new SipConfig();
//			Long id = Integer.valueOf(os[0]+"").longValue();
//			sipConfig.setId(id);
			sipConfig.setName(os[1].toString());
			sipConfig.setHost(os[2].toString());
			sipConfig.setNat(os[3].toString());
			sipConfig.setType(os[4].toString());
			sipConfig.setCall_limit((Integer) os[7]);
			sipConfig.setCancallforward(os[10].toString());
			sipConfig.setCanreinvite(os[11].toString());
			sipConfig.setContext(os[12].toString());
			sipConfig.setQualify(os[26].toString());
			sipConfig.setRegexten(os[27].toString());
			sipConfig.setSecret(os[31].toString());
			sipConfig.setDisallow(os[33].toString());
			sipConfig.setAllow(os[34].toString());
			sipConfig.setFullcontact(os[35].toString());
			sipConfig.setIpaddr(os[36].toString());
			sipConfig.setPort(os[37].toString());
			if(os[38] == null) {
				sipConfig.setRegserver("");
			} else {
				sipConfig.setRegserver(os[38].toString());
			}
			sipConfig.setRegseconds((Long)os[39]);
			sipConfig.setLastms((Integer)os[40]);
			sipConfig.setUsername(os[41].toString());
			sipConfig.setDefaultuser(os[42].toString());
			sipConfig.setSubscribecontext(os[43].toString());
			sipConfig.setSipType(SipConfigType.sip_outline);
			sipConfigs.add(sipConfig);
			
			System.out.println("---------------------" + (i++) + "----------------");
		}
		
		em_old.getTransaction().commit();
		em_old.close();
		entityMangerFactory.close();
	}

	@SuppressWarnings("unchecked")
	private static void importSipConfig() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em_new = entityMangerFactory.createEntityManager();
		em_new.getTransaction().begin();
		
		List<Domain> domains = em_new.createQuery("select d from Domain as d").getResultList();
		for(Domain domain : domains) {
			for(int i=0; i<sipConfigs.size(); i++) {
				SipConfig sip = (SipConfig) sipConfigs.toArray()[i];
				sip.setName(sip.getName());
				sip.setDomain(domain);
				em_new.persist(sip);
			}
		}
		
		em_new.getTransaction().commit();
		em_new.close();
		entityMangerFactory.close();
	}

}
