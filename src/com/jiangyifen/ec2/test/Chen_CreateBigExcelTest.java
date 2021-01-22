package com.jiangyifen.ec2.test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Random;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class Chen_CreateBigExcelTest {
	private static Random rand = new java.util.Random();
	static int count = 1;
//	private static String[] keys={"爱好1","爱好2","爱好3","爱好4","爱好5"};
	
	public static void main(String[] args) throws Exception {
		File file = createNewFile();

		createExcel(file);
	
		System.out.println("成功生成Excel测试文件！");
	}

	private static void createExcel(File file) throws Exception {
		// create an Excel file
		WritableWorkbook book = Workbook.createWorkbook(file);
		for(int i = 1; i < 2; i++) {
			WritableSheet sheet = book.createSheet("第"+i+"页", 0);
			
			for (int rowNum = 0; rowNum < 10001; rowNum++) {
				if(rowNum==0){
					addTitle(sheet);
					continue;
				}
				for (int colNum = 0; colNum < 12; colNum++) {
					String info = generateInfo(colNum);
					Label label = new Label(colNum, rowNum, info);
					sheet.addCell(label);
				}
			}
		}

		book.write();
		book.close();
	}

	private static void addTitle(WritableSheet sheet) throws Exception {
		Label name = new Label(0, 0, "姓名");
		Label mobile = new Label(1, 0, "电话");
		Label phone = new Label(2, 0, "电话");
		Label sex = new Label(3, 0, "性别");
		Label birthday = new Label(4, 0, "生日");
		Label company = new Label(5, 0, "公司");
//		Label province = new Label(6, 0, "省份");
//		Label city = new Label(7, 0, "城市");
		Label address = new Label(8, 0, "地址");
//		Label note = new Label(9, 0, "备注");
//		Label descKey= new Label(10, 0, "KEY");
//		Label descValue= new Label(11, 0, "VALUE");
		
		sheet.addCell(name);
		sheet.addCell(mobile);
		sheet.addCell(phone);
		sheet.addCell(sex);
		sheet.addCell(birthday);
		sheet.addCell(company);
//		sheet.addCell(province);
//		sheet.addCell(city);
		sheet.addCell(address);
//		sheet.addCell(note);
//		sheet.addCell(descKey);
//		sheet.addCell(descValue);
	}

	private static String generateInfo(int colNum) {
		String value="";
		switch(colNum){
//		case 0: value=generateName();break;
		case 1: value=generateMobilNumber();break;
//		case 3: value=generateSexValue();break;
//		case 4: value=generateBirthday();break;
//		case 5: value=generateCompany();break;
//		case 8: value=generateAddress();break;

		
//		case 2: value=generatePhoneNumber();break; // 注释掉了
//		case 6: value=generateProvince();break; // 注释掉了
//		case 7: value=generateCity();break; // 注释掉了
//		case 9: value=generateNote();break; // 注释掉了
//		case 10: value=generateKey();break; // 注释掉了
//		case 11: value=generateValue();break; // 注释掉了
//		default: value="illigal"; // 注释掉了
		
		}
		return value;
	}
	
//	private static String generateKey() {
//		return "爱好"+rand.nextInt(5);
//	}
//
//	private static String generateValue() {
//		return "值"+rand.nextInt(40);
//	}

//	private static String generateNote() {
//		String noteA=getCharAndNumr(5);
//		String noteB=getCharAndNumr(5);
//		
//		return "note_"+noteA+"_"+noteB;
//	}
	@SuppressWarnings("unused")
	private static String generateAddress() {
		String addressA=getCharAndNumr(7);
		String addressB=getCharAndNumr(9);
		
		return "address_"+addressA+"_"+addressB;
	}
	
//	private static String generateCity() {
//		String cityA=getCharAndNumr(2);
//		String cityB=getCharAndNumr(3);
//		
//		return "city_"+cityA+"_"+cityB;
//	}

//	private static String generateProvince() {
//		String provinceA=getCharAndNumr(2);
//		String provinceB=getCharAndNumr(3);
//		
//		return "Province_"+provinceA+"_"+provinceB;
//	}
	@SuppressWarnings("unused")
	private static String generateCompany() {
		String companyA=getCharAndNumr(2);
		String companyB=getCharAndNumr(3);
		
		return "company_"+companyA+"_"+companyB;
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

	@SuppressWarnings("unused")
	private static String generateBirthday(){
		GregorianCalendar calendar=new GregorianCalendar();
		calendar.add(GregorianCalendar.DAY_OF_YEAR,-(rand.nextInt(40*365)+20*365));
		return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
	}
	@SuppressWarnings("unused")
	private static String generateSexValue(){
		String str[]={"男","女"};
		return str[Math.abs(rand.nextInt()%2)];
				
	}
	
//	static String phoneNo = "";
//	private static String generatePhoneNumber() {
//			String number = "";
//			for (int j = 0; j < 10; j++) {
//				number += rand.nextInt(10);
//				if (j == 2) {
//					number += "-";
//				}
//			}
//			phoneNo = number;
//			return number;
//	}
	
	static String mobilNo = "";
	private static String generateMobilNumber() {
		//TODO lc 注释
		
			String number = "1" + (rand.nextInt(7) + 3);
			for (int j = 0; j < 9; j++) {
				number += rand.nextInt(10);
			}
		
		 
		
// String	number = "8888"+String.valueOf(count);
//
//			mobilNo = number;
//			
//			count++;
	 
			return number;
	}
	
	@SuppressWarnings("unused")
	private static String generateName() {
		String names[]={ "John", "Mary", "Joe", "Sarah", "Jeff", "Jane", "Peter", "Marc",
				"Robert", "Paula", "Lenny", "Kenny", "Nathan", "Nicole",
				"Laura", "Jos", "Josie", "Linus", "Torvalds", "Smith",
				"Adams", "Black", "Wilson", "Richards", "Thompson",
				"McGoff", "Halas", "Jones", "Beck", "Sheridan", "Picard",
				"Hill", "Fielding", "Einstein" };
		return names[rand.nextInt(names.length)];
	}
	private static File createNewFile() throws Exception {
		// delete old file and create new
		File file = new File("E:/data.xls");
		if (file.exists()) {
			file.delete();
		} else {
			file.createNewFile();
		}
		return file;
	}
}
