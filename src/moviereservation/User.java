package moviereservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class User {
	private String userId;
	private String userPwd;
	private Scanner scanner;
	private Connection conn;
	private int select;
	
	public User(Connection conn) {
		this.conn = conn;
		scanner = new Scanner(System.in);
	}
	
	public void login() {
		while(true) {
			System.out.println("회원 로그인");
			System.out.print("ID : ");
			userId = scanner.nextLine();
			System.out.print("PW : ");
			userPwd = scanner.nextLine();
			
			try {
				PreparedStatement pstmt = conn.prepareStatement(
						"SELECT CUSTOMER_PASSWORD FROM CUSTOMER WHERE CUSTOMER_ID = ?");
				pstmt.setString(1, userId);
				ResultSet rs = pstmt.executeQuery();
				String pw = null;
				
				if(rs.next())
					pw = rs.getString(1);
				
				if(pw.equals(userPwd)) {
					System.out.println("로그인 성공");
					break;
				}
				
				else 
					System.out.println("비밀번호를 잘못 입력하셨습니다.");
			} catch(SQLException e) {
				System.out.println("아이디를 잘못 입력하셨습니다.");
				e.printStackTrace();
			}
		}
		
		userManu();
	}
	
	private void userManu() {
		while(true) {
			System.out.println("0. 종료, 1. 영화 예매, 2. 예매 현황, 3. 영화 검색, 4. 회원 정보 수정");
			select = scanner.nextInt();
			
			switch(select) {
				case 1:
					selectMovie();
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
				default:	
			}
			
			if(select == 0)
				break;
		}
	}

	private void selectMovie() {
		int movieNumber = 1;
		String movieId = null;
		ArrayList<String> movieList = new ArrayList<>();
		movieList.add(null);
		
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT TITLE, MOVIE_ID FROM MOVIE");
			
			while(rs.next()) {
				movieList.add(rs.getString(2));
				System.out.println(movieNumber++ + ". " + rs.getString(1));				
			}
			
			System.out.print("예매할 영화 번호 : ");
			select = scanner.nextInt();
			
			while(0 >= select || select >= movieNumber) {
				System.out.print("잘못된 번호입니다. 다시 입력하세요. : ");
				select = scanner.nextInt();
			}
			
			movieId = movieList.get(select);
			
		} catch(SQLException e) {
			System.out.println("영화 목록을 가져오지 못했습니다.");
			e.printStackTrace();
		}
		
		selectCinema(movieId);
	}

	private void selectCinema(String movieId) {
		int cinemaNumber = 1;
		String cinemaName = null;
		ArrayList<String> cinemaList = new ArrayList<>();
		cinemaList.add(null);
				
		try {
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT CINEMA_NAME FROM THEATER "
					+ "WHERE MOVIE_ID = ?");
			pstmt.setString(1, movieId);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				cinemaList.add(rs.getString(1));
				System.out.println(cinemaNumber++ + ". " + rs.getString(1));
			}
			
			System.out.print("예매할 영화관 번호 : ");
			select = scanner.nextInt();
			
			while(0 >= select || select >= cinemaNumber) {
				System.out.print("잘못된 번호입니다. 다시 입력하세요. : ");
				select = scanner.nextInt();
			}
			
			cinemaName = cinemaList.get(select);
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		selectDate(movieId, cinemaName);
	}

	private void selectDate(String movieId, String cinemaName) {
		int dateNumber = 1;
		String date = null;
		ArrayList<String> dateList = new ArrayList<>();
		dateList.add(null);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd");
		
		for(int i = 0; i < 3; i ++) {
			dateList.add(format.format(cal.getTime()));
			System.out.println(dateNumber++ + ". " + format.format(cal.getTime()));
			cal.add(Calendar.DATE, 1);
		}
		
		System.out.print("예매할 날짜 번호 : ");
		select = scanner.nextInt();
		
		while(0 >= select || select >= dateNumber) {
			System.out.print("잘못된 번호입니다. 다시 입력하세요. : ");
			select = scanner.nextInt();
		}
		
		date = dateList.get(select);
		
		selectTheather(movieId, cinemaName, date);
	}

	private void selectTheather(String movieId, String cinemaName, String date) {
		int Number = 1;
		String theaterNumber = null;
		ArrayList<String> theaterList = new ArrayList<>();
		theaterList.add(null);
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT THEATER_NUMBER FROM THEATER "
					+ "WHERE CINEMA_NAME = ? AND MOVIE_ID = ?");
			pstmt.setString(1, cinemaName);
			pstmt.setString(2, movieId);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				theaterList.add(rs.getString(1));
				System.out.println(Number++ + ". " + rs.getString(1));
			}
			
			System.out.print("예매할 상영관 번호 : ");
			select = scanner.nextInt();
			
			while(0 >= select || select >= Number) {
				System.out.print("잘못된 번호입니다. 다시 입력하세요. : ");
				select = scanner.nextInt();
			}
			
			theaterNumber = theaterList.get(select);
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		selectTime(cinemaName, theaterNumber, date);
	}

	private void selectTime(String cinemaName, String theaterNumber, String date) {
		int Number = 1;
		String startTime = null;
		ArrayList<String> timeList = new ArrayList<>();
		timeList.add(null);
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT START_TIME FROM SCHEDULE "
					+ "WHERE CINEMA_NAME = ? AND THEATER_NUMBER = ?");
			pstmt.setString(1, cinemaName);
			pstmt.setString(2, theaterNumber);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				timeList.add(rs.getString(1));
				System.out.println(Number++ + ". " + rs.getString(1));
			}
			
			System.out.print("예매할 시간 번호 : ");
			select = scanner.nextInt();
			
			while(0 >= select || select >= Number) {
				System.out.print("잘못된 번호입니다. 다시 입력하세요. : ");
				select = scanner.nextInt();
			}
			
			startTime = timeList.get(select);
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		selectSeat(cinemaName, theaterNumber, date, startTime);
	}

	private void selectSeat(String cinemaName, String theaterNumber, String date, String startTime) {
		int seatingCapacity = 0;
		int reservedSeatCount = 0;
		int seatCount = 0;
		int payment = 0;
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT SEATING_CAPACITY FROM THEATER "
					+ "WHERE CINEMA_NAME = ? AND THEATER_NUMBER = ?");
			pstmt.setString(1, cinemaName);
			pstmt.setString(2, theaterNumber);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) 
				seatingCapacity = Integer.parseInt(rs.getString(1));
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT SUM(SEAT_COUNT) FROM TICKET "
					+ "WHERE TICKET_NUMBER = "
					+ "(SELECT TICKET_NUMBER FROM RESERVATION "
					+ "WHERE CINEMA_NAME = ? AND THEATER_NUMBER = ?) "
					+ "AND START_TIME = TO_DATE(?, 'YY/MM/DD/HH24')");
			pstmt.setString(1, cinemaName);
			pstmt.setString(2, theaterNumber);
			pstmt.setString(3, date + "/" + startTime);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) { 
				if(rs.getString(1) == null)
					reservedSeatCount = 0;
				else
					reservedSeatCount = Integer.parseInt(rs.getString(1));
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		if(seatingCapacity - reservedSeatCount == 0)
			System.out.println("예약 가능한 자리가 없습니다.");
		else {
			System.out.println("예약 가능한 자리 : " + (seatingCapacity - reservedSeatCount));
			System.out.println("예약할 자리 수 : ");
			seatCount = scanner.nextInt();
			System.out.println("1. 인터넷 결제, 2. 현장 결제, 3. 예매 취소");
			payment = scanner.nextInt();
			
			while(payment < 1 || payment > 3) {
				System.out.println("잘못된 명령입니다. 다시 입력하세요 : ");
				payment = scanner.nextInt();
			}
			
			switch(payment) {
				case 1:
					break;
				case 2:
					break;
				case 3:
					System.out.println("예매를 취소합니다.");
					break;
				default:	
			}
		}
	}
	
}
