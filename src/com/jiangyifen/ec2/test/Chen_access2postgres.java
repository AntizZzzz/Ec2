package com.jiangyifen.ec2.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.jiangyifen.ec2.entity.MobileLoc;
/**
 * 运行 C:\Windows\System32\odbcad32.exe ，配置odbc32位数据源
 * 安装32位jdk，将java程序配置为32位jdk的编译器
 * 将程序和程序类编码设置为gbk
 * @author chb
 *
 */
public class Chen_access2postgres {
	private static Connection con;
	private static Statement st;
	static{
		try {
			Class.forName("org.postgresql.Driver").newInstance(); 
			String url = "jdbc:postgresql://192.168.1.172:5432/asterisk" ; 
			con= DriverManager.getConnection(url,"postgres","postgres"); 
			st = con.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");  
        String url = "jdbc:odbc:dbsrc";  
        Connection conn = DriverManager.getConnection(url);  
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from Dm_Mobile");

        Long i=0L;
        while(rs.next()){
        	i++;
//        	String id=rs.getString("ID");
        	String mobileNumber=rs.getString("MobileNumber");
        	String mobileArea=rs.getString("MobileArea");
        	String mobileType=rs.getString("MobileType");
        	String areaCode=rs.getString("AreaCode");
        	String postCode=rs.getString("PostCode");
        	
        	MobileLoc mobileLoc=new MobileLoc();
        	mobileLoc.setMobileNumber(mobileNumber);
        	mobileLoc.setMobileArea(mobileArea);
        	mobileLoc.setMobileType(mobileType);
        	mobileLoc.setAreaCode(areaCode);
        	mobileLoc.setPostCode(postCode);

        	con.setAutoCommit(true);
        	String sql="insert into ec2_mobileloc values("+i+",'"+areaCode+"','"+mobileArea+"','"+mobileNumber+"','"+mobileType+"','"+postCode+"')";
        	st.executeUpdate(sql);
        }

        stmt.close();  
        conn.close();  
	}
}
