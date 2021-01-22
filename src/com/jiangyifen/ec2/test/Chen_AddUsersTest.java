package com.jiangyifen.ec2.test;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;

public class Chen_AddUsersTest {
	private static EntityManagerFactory entityManagerFactory;
	private static Random rand = new java.util.Random();
	public static void setUp() {
		entityManagerFactory = Persistence.createEntityManagerFactory("ec2");
	}

	public static void tearDown() {
		entityManagerFactory.close();
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		Chen_AddUsersTest.setUp();
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		
		Chen_AddUsersTest.createUsers(em);
		
		em.getTransaction().commit();
		em.close();
		Chen_AddUsersTest.tearDown();
	}

	public static void createUsers(EntityManager em) {
		
		Domain domain=new Domain();
		domain.setName("domain1");
		
		Department department=new Department();
		department.setDomain(domain);
		department.setName("department1");

		em.persist(domain);
		em.persist(department);
		
		for (int count = 0; count< 200; count++) {
			User user = new User();
			user.setRealName(generateInfo(0)+count);
			user.setUsername(generateInfo(1)+count);
			user.setPassword(generateInfo(2));
			user.setPhoneNumber(generateInfo(3));
			user.setRegistedDate(new Date());
			user.setEmailAddress(generateInfo(4)+count);
			
			user.setDomain(domain);
			user.setDepartment(department);
			em.persist(user);
		}


	}
	private static String generateInfo(int colNum) {
		String value="";
		switch(colNum){
		case 0: value=generateRealName();break;
		case 1: value=generateUserName();break;
		case 2: value=generatePassword();break;
		case 3: value=generatePhoneNumber();break;
		case 4: value=generateEmailAddress();break;
		
		default: value="illigal";
		}
		return value;
	}
	private static String generateRealName() {
		String names[]={ "John", "Mary", "Joe", "Sarah", "Jeff", "Jane", "Peter", "Marc",
				"Robert", "Paula", "Lenny", "Kenny", "Nathan", "Nicole",
				"Laura", "Jos", "Josie", "Linus", "Torvalds", "Smith",
				"Adams", "Black", "Wilson", "Richards", "Thompson",
				"McGoff", "Halas", "Jones", "Beck", "Sheridan", "Picard",
				"Hill", "Fielding", "Einstein" };
		return names[rand.nextInt(names.length)];
	}
	private static String generateUserName() {
		String names[]={ "John", "Mary", "Joe", "Sarah", "Jeff", "Jane", "Peter", "Marc",
				"Robert", "Paula", "Lenny", "Kenny", "Nathan", "Nicole",
				"Laura", "Jos", "Josie", "Linus", "Torvalds", "Smith",
				"Adams", "Black", "Wilson", "Richards", "Thompson",
				"McGoff", "Halas", "Jones", "Beck", "Sheridan", "Picard",
				"Hill", "Fielding", "Einstein" };
		return names[rand.nextInt(names.length)];
	}
	private static String generatePassword() {
		String number = "";
		for (int j = 0; j < 7; j++) {
			number += rand.nextInt(10);
		}
		return number;
}
	private static String generatePhoneNumber() {
		String number = "1" + (rand.nextInt(7) + 3);
		for (int j = 0; j < 9; j++) {
			number += rand.nextInt(10);
		}
		return number;
}

	private static String generateEmailAddress() {
		String names[]={ "John", "Mary", "Joe", "Sarah", "Jeff", "Jane", "Peter", "Marc",
				"Robert", "Paula", "Lenny", "Kenny", "Nathan", "Nicole",
				"Laura", "Jos", "Josie", "Linus", "Torvalds", "Smith",
				"Adams", "Black", "Wilson", "Richards", "Thompson",
				"McGoff", "Halas", "Jones", "Beck", "Sheridan", "Picard",
				"Hill", "Fielding", "Einstein" };
		String emails[]={"163","126","qq","sina","yahoo","gmail"};
		String name= names[rand.nextInt(names.length)];
		String email= emails[rand.nextInt(emails.length)];
		
//		String addressA=getCharAndNumr(4);
//		String addressB=getCharAndNumr(9);
		
		return name+"@"+email+".com";
	}
	
	public static String getCharAndNumr(int length)     
	{     
	    String val = "";     
	    Random random = new Random();     
	    for(int i = 0; i < length; i++)     
	    {     
	        String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字     
	        if("char".equalsIgnoreCase(charOrNum)) // 字符串     
	        {     
	            int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; //取得大写字母还是小写字母     
	            val += (char) (choice + random.nextInt(26));     
	        }     
	        else if("num".equalsIgnoreCase(charOrNum)) // 数字     
	        {     
	            val += String.valueOf(random.nextInt(10));     
	        }     
	    }     
	    return val;     
	}   

//	private static String generateBirthday(){
//		GregorianCalendar calendar=new GregorianCalendar();
//		calendar.add(GregorianCalendar.DAY_OF_YEAR,-(rand.nextInt(40*365)+20*365));
//		return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
//	}
		


/*	Department department1 = new Department();
	department1.setName("销售部");
	Department department2 = new Department();
	department2.setName("研发部");
	Department department3 = new Department();
	department3.setName("人力部");
	Department department4 = new Department();
	department4.setName("财务部");
	Department department5 = new Department();
	department5.setName("人_财部");

	// 设置用户所在部门
	user1.setDepartment(department1);

*/
	
	
}
