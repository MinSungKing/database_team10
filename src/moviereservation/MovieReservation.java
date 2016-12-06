package moviereservation;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class MovieReservation {

	public static void main(String[] args) {
		String DRIVER = "oracle.jdbc.driver.OracleDriver";
		String URL = "jdbc:oracle:thin:@127.0.0.1:1521:DBSERVER";
		String USER = "KWON";
		String PASS = "flrhs00";
		
		Connection conn = null;
		try{
			Class.forName(DRIVER);
			conn = DriverManager.getConnection(URL,USER,PASS);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		try {
			DatabaseMetaData meta = conn.getMetaData();
			System.out.println("time data: " + meta.getTimeDateFunctions());
			System.out.println("user: " + meta.getUserName());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		User user = new User(conn);
		Scanner s = new Scanner(System.in);
		int select;
		System.out.println("1. 회원 로그인, 2. 관리자 로그인, 3. 회원가입");
		select = s.nextInt();
		
		switch(select) {
			case 1:
				user.login();
				break;
			case 2:
				break;
			case 3:
				break;
			default :
				
		}
		
		s.close();
	}

}
