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
	private static int ticketNumber;
	
	public User(Connection conn) {
		this.conn = conn;
		String initTicketNumber;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT TICKET_NUMBER FROM TICKET "
					+ "ORDER BY TICKET_NUMBER DESC");
			if(rs.next())
				initTicketNumber = rs.getString(1);
			else
				initTicketNumber = "T0000";
			
			ticketNumber = Integer.parseInt(initTicketNumber.substring(1, initTicketNumber.length())) + 1;
			System.out.println(ticketNumber);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	

	public void signUp(){
		String userId = "";
		String userPwd = "";
		String userName = "";
		String userBirthday = "";
		String userPhoneNumber = "";
		String userAddress = "";
		int userPoint = 0;

		while (true) {
			System.out.println("회원가입");
			System.out.print("ID : ");
			userId = scanner.nextLine();
			System.out.print("PW : ");
			userPwd = scanner.nextLine();
			System.out.print("Name : ");
			userName = scanner.nextLine();
			System.out.print("BirthDay : ");
			userBirthday = scanner.nextLine();
			System.out.print("Phone Number : ");
			userPhoneNumber = scanner.nextLine();
			System.out.print("Address : ");
			userAddress = scanner.nextLine();
			
			while(true){
				System.out.println("\n가입을 완료하시겠습니까?");
				System.out.println("1. 가입완료, 2. 취소");
				int select = scanner.nextInt();
				if(select == 1){
					try {
						Statement stmt = conn.createStatement();
						String query = "INSERT INTO CUSTOMER VALUES('"+ userId + "', '" + userPwd + "', '" + userName + "', '" + userBirthday
								+ "', '" + userPhoneNumber + "', '" + userAddress + "', '" + userPoint + "')";
						System.out.println(query);
						int rowCount = stmt.executeUpdate(query);
						if(rowCount == 0) {
							System.out.println("데이터 삽입 실패");
						} else {
							System.out.println("데이터 삽입 성공");
							break;
						}
					} catch (Exception e) {
						System.out.println("[*]	INSERT 오류 발생: \n" + e.getMessage());
					}
				}
				else if(select == 2){
					System.out.println("가입이 취소되었습니다.");
					break;
				}
				else{
					System.out.println("잘못된 명령입니다.");
				}
			}
			break;			
		}
	}
	
	public void login() {
		while(true) {
			scanner = new Scanner(System.in);
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
			int select = scanner.nextInt();
			
			switch(select) {
				case 1:
					selectMovie();
					break;
				case 2:
					checkReservation();
					break;
				case 3:
					searchMovieInfo();
					break;
				case 4:
					updateCustomerInfo();
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
			int select = scanner.nextInt();
			
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
	

	private ResultSet select(Connection conn, String query) {
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			
			// 질의 결과 반환
			return rs;
		} catch (Exception e) {
			System.out.println("[*]	SELECT 오류 발생: \n" + e.getMessage());
		}
		
		return rs;
	}
	
	
	private void checkReservation(){
		int number = 1;
		String ticketNumber, seatCount, payment;
		String startTime;
		SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd/");
		ArrayList<String> ticketNumberList = new ArrayList<>();
		ticketNumberList.add(null);
		ArrayList<String> dateList = new ArrayList<>();
		dateList.add(null);
		ArrayList<Integer> seatCountList = new ArrayList<>();
		seatCountList.add(null);
		
		try {
			Statement stmt = conn.createStatement();
			
			ResultSet rs = select(conn, "SELECT * FROM TICKET WHERE (PAYMENT = 'INTERNET' OR PAYMENT = 'DIRECT') "
					+ "AND TICKET_NUMBER IN (SELECT TICKET_NUMBER FROM RESERVATION WHERE CUSTOMER_ID = '" + userId + "')");
			System.out.println("예매 현황입니다.");
			
			System.out.println("  영화제목       티켓 번호                         시작 시간                          좌석 수        결제여부 ");
			
			
			while(rs.next()) {
				ticketNumber = rs.getString(1);
				startTime = rs.getString(2);
				seatCount = rs.getString(3);
				payment = rs.getString(4);
				String title = "";
				ticketNumberList.add(ticketNumber);
				dateList.add(format.format(rs.getDate(2)));
				seatCountList.add(rs.getInt(3));
				try {
					String query = "SELECT CINEMA_NAME, THEATER_NUMBER FROM RESERVATION WHERE TICKET_NUMBER='" + ticketNumber + "'";
					rs.close();
					rs = stmt.executeQuery(query);
					rs.next();
								
					String cinemaName = rs.getString(1);
					String theaterNumber = rs.getString(2);
					
					query = "SELECT MOVIE_ID FROM THEATER WHERE CINEMA_NAME = '" + cinemaName
							+ "' AND THEATER_NUMBER = '" + theaterNumber +"'";
					
					rs = stmt.executeQuery(query);
					rs.next();
					String movie_id = rs.getString(1);
					
					query = "SELECT TITLE FROM MOVIE WHERE MOVIE_ID = '" + movie_id + "'";

					rs = stmt.executeQuery(query);
					rs.next();
					title = rs.getString(1);
					
				} catch (Exception e) {
					System.out.println("없는 티켓 번호입니다." + e.getMessage());
				}
				
				System.out.println( number++ + ". " + title + "    " + ticketNumber + "    " + startTime + "       " + seatCount + "       " + payment);
			}
		} catch (Exception e) {
			System.out.println("[*]	질의 결과 출력 오류 발생: \n" + e.getMessage());
		}
		
		System.out.println("0. 종료, 1. 예약 정보 수정");
		int select = scanner.nextInt();
		
		while(select < 0 || select > 1) {
			System.out.print("잘못된 입력입니다. 다시 입력하세요. : ");
			select = scanner.nextInt();
		}
		
		if(select == 1) {
			System.out.println("수정할 티켓 번호를 선택해 주세요.");
			select = scanner.nextInt();
			
			while(select < 1 || select >= number) {
				System.out.print("잘못된 입력입니다. 다시 입력하세요. : ");
				select = scanner.nextInt();
			}
			
			fixReservation(ticketNumberList.get(select), dateList.get(select), seatCountList.get(select));
		}
	}
	

	private void fixReservation(String ticketNumber, String date, int seatCount) {
		int number = 1;
		String cinemaName = null;
		ArrayList<String> theaterList = new ArrayList<>();
		theaterList.add(null);
		ArrayList<String> startTimeList = new ArrayList<>();
		startTimeList.add(null);
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT T.THEATER_NUMBER, T.CINEMA_NAME FROM RESERVATION R, THEATER T "
					+ "WHERE R.TICKET_NUMBER = ? AND T.CINEMA_NAME = R.CINEMA_NAME");
			pstmt.setString(1, ticketNumber);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				theaterList.add(rs.getString(1));
				cinemaName = rs.getString(2);
				System.out.println(number++ + ". " + rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("상영관을 선택해 주세요.");
		int select = scanner.nextInt();
		
		while(select < 1 || select >= number) {
			System.out.print("잘못된 입력입니다. 다시 입력하세요. : ");
			select = scanner.nextInt();
		}
		
		String theaterNumber = theaterList.get(select);
		number = 1;
		try {
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT START_TIME FROM SCHEDULE "
					+ "WHERE CINEMA_NAME = ? AND THEATER_NUMBER = ?");
			pstmt.setString(1, cinemaName);
			pstmt.setString(2, theaterNumber);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				startTimeList.add(rs.getString(1));
				System.out.println(number++ + ". " + rs.getString(1));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		System.out.println("시간을 선택해 주세요.");
		select = scanner.nextInt();
		
		while(select < 1 || select >= number) {
			System.out.print("잘못된 입력입니다. 다시 입력하세요. : ");
			select = scanner.nextInt();
		}
		
		String movieStartTime = date + startTimeList.get(select);
		int seatingCapacity = 0;
		int reservedSeatCount = 0;
		
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
					+ "WHERE TICKET_NUMBER IN "
					+ "(SELECT TICKET_NUMBER FROM RESERVATION "
					+ "WHERE CINEMA_NAME = ? AND THEATER_NUMBER = ?) "
					+ "AND START_TIME = TO_DATE(?, 'YY/MM/DD/HH24')");
			pstmt.setString(1, cinemaName);
			pstmt.setString(2, theaterNumber);
			pstmt.setString(3, movieStartTime);
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
		
		if(seatingCapacity - reservedSeatCount < seatCount)
			System.out.println("예약 가능한 자리가 없습니다.");
		else {
			try{
				PreparedStatement pstmt = conn.prepareStatement(
						"UPDATE TICKET SET START_TIME = TO_DATE(?, 'YY/MM/DD/HH24'), SEAT_COUNT = ? "
								+ "WHERE TICKET_NUMBER = ?");
				
				pstmt.setString(1, movieStartTime);
				pstmt.setInt(2, seatCount);
				pstmt.setString(3, ticketNumber);
				int rowCount = pstmt.executeUpdate();
				if(rowCount == 0)
					System.out.println("티켓 정보 수정 실패");
			} catch(SQLException e) {
				e.printStackTrace();
			}
			
			try{
				PreparedStatement pstmt = conn.prepareStatement(
						"UPDATE RESERVATION SET THEATER_NUMBER = ? "
								+ "WHERE TICKET_NUMBER = ?");
				
				pstmt.setString(1,theaterNumber);
				pstmt.setString(2, ticketNumber);
				int rowCount = pstmt.executeUpdate();
				if(rowCount == 0)
					System.out.println("예매 정보 수정 실패");
			} catch(SQLException e) {
				e.printStackTrace();
			}
			
			System.out.println("예매 정보가 성공적으로 수정되었습니다.");
		}
	}


	private void searchMovieInfo() {		
		String movieTitle = "";
		String[] movieIdList = new String[50];
		int i = 1;
		try {
			
			ResultSet rs = select(conn,
					"SELECT * FROM MOVIE ORDER BY (SELECT SUM(SEAT_COUNT) "
					+ "FROM RESERVATION R, MOVIE M, THEATER T, TICKET K "
					+ "WHERE M.MOVIE_ID = T.MOVIE_ID "
					+ "AND T.CINEMA_NAME = R.CINEMA_NAME AND T.THEATER_NUMBER = R.THEATER_NUMBER "
					+ "AND R.TICKET_NUMBER = K.TICKET_NUMBER "
					+ "AND K.START_TIME >= TO_DATE(SYSDATE, 'YY/MM/DD/HH24'))");
			System.out.println("현재 상영중인 영화 목록입니다..");
			while (rs.next()) {
				movieTitle = rs.getString(3);
				movieIdList[i] = rs.getString(1); //MOVIE_ID
				System.out.println(i + ". " + movieTitle);
				i++;
			}
		} catch (Exception e) {
			System.out.println("[*]	질의 결과 출력 오류 발생: \n" + e.getMessage());
		}
		
		int select;
		while(true){
			System.out.println("\n영화를 선택하면 상세 정보가 출력됩니다. (종료 0)");
			select = scanner.nextInt();
			if(select < i && select != 0){
				String movieId = movieIdList[select];
				try {
					ResultSet rs = select(conn,
							"SELECT * FROM MOVIE WHERE MOVIE_ID = '" + movieId +"'");
					ResultSet rs2 = select(conn,
							"SELECT ACTOR FROM ACTOR WHERE MOVIE_ID = '" + movieId + "'");

					String runningTime = "";
					String rating = "";
					String director= "";
					String movieInfo = "";
					
					while(rs.next()){
						runningTime = rs.getString(2);
						movieTitle = rs.getString(3);
						rating = rs.getString(4);
						director = rs.getString(5);
						movieInfo = rs.getString(6);
					}
										
					String[] actorList = new String[50];
					int j = 0;
					while(rs2.next()){
						actorList[j] = rs2.getString(1);
						j++;
					}
					
					System.out.println("영화 : " + movieTitle);
					System.out.println("감독 : " + director);
					System.out.println("관람 가능 연령 : " + rating);
					System.out.println("러닝타임 : " + runningTime);
					System.out.print("출연진 : ");
					
					for(int index = 0; index < j; index++){
						System.out.print(actorList[index] + " ");
					}
					
					System.out.println("\n줄거리 : " + movieInfo);

				} catch (Exception e) {
					System.out.println("[*]	질의 결과 출력 오류 발생: \n" + e.getMessage());
				}

			}
			else if(select == 0){
				System.out.println("");
				break;
			}
			else{
				System.out.println("잘못된 입력입니다.");
			}
		}
	}
	

	public void updateCustomerInfo() {
		int select;
		
		while(true){
			System.out.println("\n회원정보를 수정합니다. 원하시는 작업을 선택하세요.");
			System.out.println("1. 회원정보 수정, 2. 회원 탈퇴");
			select = scanner.nextInt();
			
			//1. 회원정보 수정을 선택한 경우
			if(select == 1){

				String newDate = "";
				while(true){
					System.out.println("\n수정하실 정보를 선택하세요.");
					System.out.println("1. 비밀번호, 2. 휴대폰 번호, 3. 주소");
					select = scanner.nextInt();
					scanner.nextLine();
					if(select == 1){
						System.out.print("변경할 비밀번호를 입력하세요 : ");
						newDate = scanner.nextLine();
						
						try {
							PreparedStatement pstmt = conn.prepareStatement(
									"UPDATE CUSTOMER SET CUSTOMER_PASSWORD = ? WHERE CUSTOMER_ID = ?");
							
							pstmt.setString(1, newDate);
							pstmt.setString(2, userId);
							
							int rowCount = pstmt.executeUpdate();
							if(rowCount == 0) {
								System.out.println("비밀번호 수정 실패");
							} else {
								System.out.println("비밀번호 수정 성공");
							}
						} catch (Exception e) {
							System.out.println("[*]	UPDATE 오류 발생: \n" + e.getMessage());
						}	
						
						break;
					}

					else if(select == 2){
						System.out.print("변경할 휴대폰 번호를 입력하세요 : ");
						newDate = scanner.nextLine();
						
						try {
							PreparedStatement pstmt = conn.prepareStatement(
									"UPDATE CUSTOMER SET PHONE_NUMBER = ? WHERE CUSTOMER_ID = ?");
							
							pstmt.setString(1, newDate);
							pstmt.setString(2, userId);
							
							int rowCount = pstmt.executeUpdate();
							if(rowCount == 0) {
								System.out.println("휴대폰 번호 수정 실패");
							} else {
								System.out.println("휴대폰 번호 수정 성공");
							}
						} catch (Exception e) {
							System.out.println("[*]	UPDATE 오류 발생: \n" + e.getMessage());
						}
						
						break;
					}
					else if(select == 3){
						System.out.print("변경할 주소를 입력하세요 : ");
						newDate = scanner.nextLine();
						
						try {
							PreparedStatement pstmt = conn.prepareStatement(
									"UPDATE CUSTOMER SET ADDRESS = ? WHERE CUSTOMER_ID = ?");
							
							pstmt.setString(1, newDate);
							pstmt.setString(2, userId);
							
							int rowCount = pstmt.executeUpdate();
							if(rowCount == 0) {
								System.out.println("주소 수정 실패");
							} else {
								System.out.println("주소 수정 성공");
							}
						} catch (Exception e) {
							System.out.println("[*]	UPDATE 오류 발생: \n" + e.getMessage());
						}
						break;
					}
					else{
						System.out.println("잘못된 입력입니다.");
					}
				}
				break;
			}
			//2.탈퇴하기를 선택한 경우
			else if(select == 2){
				while(true){
					System.out.println("\n정말 탈퇴하시겠습니까?");
					System.out.println("1.탈퇴하기 , 2.취소");
					select = scanner.nextInt();
					if(select == 1){
						try {
							Statement stmt = conn.createStatement();
							String query = "DELETE FROM CUSTOMER WHERE CUSTOMER_ID = '" + userId + "' ";
							int rowCount = stmt.executeUpdate(query);
							if(rowCount == 0) {
								System.out.println("데이터 삭제 실패");
							} else {
								System.out.println("데이터 삭제 성공");
							}
						} catch (Exception e) {
							System.out.println("[*]	DELETE 오류 발생: \n" + e.getMessage());
						}
						break;
					}
					else if(select == 2){
						System.out.println("탈퇴가 취소되었습니다.");
						break;
					}
					else{
						System.out.println("잘못된 입력입니다.");
					}
				}
				break;
			}
			else{
				System.out.println("잘못된 입력입니다.");
			}
		}
	}

	private void selectCinema(String movieId) {
		int cinemaNumber = 1;
		String cinemaName = null;
		ArrayList<String> cinemaList = new ArrayList<>();
		cinemaList.add(null);
				
		try {
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT DISTINCT CINEMA_NAME FROM THEATER "
					+ "WHERE MOVIE_ID = ?");
			pstmt.setString(1, movieId);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				cinemaList.add(rs.getString(1));
				System.out.println(cinemaNumber++ + ". " + rs.getString(1));
			}
			
			System.out.print("예매할 영화관 번호 : ");
			int select = scanner.nextInt();
			
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
		int select = scanner.nextInt();
		
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
			int select = scanner.nextInt();
			
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
			int select = scanner.nextInt();
			
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
		String movieStartTime = date + "/" + startTime;
		String payment;
		
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
					+ "WHERE TICKET_NUMBER IN "
					+ "(SELECT TICKET_NUMBER FROM RESERVATION "
					+ "WHERE CINEMA_NAME = ? AND THEATER_NUMBER = ?) "
					+ "AND START_TIME = TO_DATE(?, 'YY/MM/DD/HH24')");
			pstmt.setString(1, cinemaName);
			pstmt.setString(2, theaterNumber);
			pstmt.setString(3, movieStartTime);
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
			
			if(seatCount < 1 || seatCount > (seatingCapacity - reservedSeatCount)) {
				System.out.println("잘못된 명령입니다. 다시 입력하세요. : ");
				seatCount = scanner.nextInt();
			}
			System.out.println("1. 인터넷 결제, 2. 현장 결제, 3. 예매 취소");
			int select = scanner.nextInt();
			
			while(select < 1 || select > 3) {
				System.out.println("잘못된 명령입니다. 다시 입력하세요 : ");
				select = scanner.nextInt();
			}
			
			switch(select) {
				case 1:
					payment = "INTERNET";
					selectPoint(cinemaName, theaterNumber, movieStartTime, seatCount, payment);
					break;
				case 2:
					payment = "DIRECT";
					reservate(cinemaName, theaterNumber, movieStartTime, seatCount, payment, 0);
					break;
				case 3:
					System.out.println("예매를 취소합니다.");
					break;
				default:	
			}
		}
	}

	private void selectPoint(String cinemaName, String theaterNumber, String movieStartTime, int seatCount, String payment) {
		int availablePoint = 0;
		int pointToUse = 0;
		
		System.out.println("1. 포인트 사용하기, 2. 포인트 사용하지 않기");
		int select = scanner.nextInt();

		while(select < 1 || select > 2) {
			System.out.println("잘못된 명령입니다. 다시 입력하세요 : ");
			select = scanner.nextInt();
		}
		
		switch(select) {
			case 1:
				try {
					PreparedStatement pstmt = conn.prepareStatement("SELECT CUSTOMER_POINT FROM CUSTOMER WHERE CUSTOMER_ID = ?");
					pstmt.setString(1, userId);
					ResultSet rs = pstmt.executeQuery();
					
					if(rs.next())
						availablePoint = rs.getInt(1);					
					
					if(availablePoint < 1000) {
						System.out.println("포인트가 부족하여 사용할 수 없습니다.");
						pointToUse = 0;
					}
					
					else {
						System.out.println("사용 가능한 포인트 : " + availablePoint);
						System.out.print("사용할 포인트 : ");
						pointToUse = scanner.nextInt();
					}
					
					pay(cinemaName, theaterNumber, movieStartTime, seatCount, payment, pointToUse);
					
				} catch(SQLException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				pointToUse = 0;
				pay(cinemaName, theaterNumber, movieStartTime, seatCount, payment, pointToUse);
				break;
			default:
		}
	}
	
	private void pay(String cinemaName, String theaterNumber, String movieStartTime, int seatCount, String payment, int pointToUse) {
		
		int price = 0;
		String query = "SELECT PRICE FROM THEATER WHERE CINEMA_NAME = '" + cinemaName
				+ "' AND THEATER_NUMBER = '" + theaterNumber +"'";

		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			
			int theaterPrice = 0;
			if(rs.next())
				theaterPrice = rs.getInt(1);	
				
			price = seatCount * theaterPrice;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (price >= pointToUse)
			price -= pointToUse;
		else {
			pointToUse = price;
			price = 0;
		}

		System.out.println("총액 : " + price);
		System.out.println("1. 결제, 2. 취소");
		System.out.print("결제를 하시겠습니까? : ");
		int select = scanner.nextInt();

		while (select < 1 || select > 2) {
			System.out.println("잘못된 명령입니다. 다시 입력하세요 : ");
			select = scanner.nextInt();
		}
		
		switch(select) {
		case 1:
			System.out.println("결제가 완료되었습니다.");
			reservate(cinemaName, theaterNumber, movieStartTime, seatCount, payment, pointToUse);
			break;
		case 2:
			System.out.println("결제를 취소합니다.");
			break;
		default:
		}
	}
	
	private void reservate(String cinemaName, String theaterNumber, String movieStartTime, int seatCount, String payment, int pointToUse) {
		System.out.println("1. 예매, 2. 취소");
		System.out.print("예매를 하시겠습니까? : ");
		int select = scanner.nextInt();
		
		while(select < 1 || select > 2) {
			System.out.println("잘못된 명령입니다. 다시 입력하세요 : ");
			select = scanner.nextInt();
		}
		
		switch(select) {
			case 1:
				try{
					PreparedStatement pstmt = conn.prepareStatement(
							"UPDATE CUSTOMER SET CUSTOMER_POINT = CUSTOMER_POINT - ? "
							+ "WHERE CUSTOMER_ID = ?");
					pstmt.setInt(1, pointToUse);
					pstmt.setString(2, userId);
					int rowCount = pstmt.executeUpdate();
					if(rowCount == 0)
						System.out.println("고객 포인트 차감 실패");
					
					pstmt = conn.prepareStatement(
							"UPDATE CUSTOMER SET CUSTOMER_POINT = CUSTOMER_POINT + ? * 100 "
							+ "WHERE CUSTOMER_ID = ?");
					pstmt.setInt(1, seatCount);
					pstmt.setString(2, userId);
					rowCount = pstmt.executeUpdate();
					if(rowCount == 0)
						System.out.println("고객 포인트 증가 실패");
					
					pstmt = conn.prepareStatement(
							"INSERT INTO TICKET VALUES (?, TO_DATE(?, 'YY/MM/DD/HH24'), ?, ?)");
					pstmt.setString(1, "T" + ticketNumber);
					pstmt.setString(2, movieStartTime);
					pstmt.setInt(3, seatCount);
					pstmt.setString(4, payment);
					rowCount = pstmt.executeUpdate();
					if(rowCount == 0)
						System.out.println("티켓 정보 삽입 실패");
					
					pstmt = conn.prepareStatement(
							"INSERT INTO RESERVATION VALUES (?, ?, ?, ?)");
					pstmt.setString(1, userId);
					pstmt.setString(2, "T" + ticketNumber++);
					pstmt.setString(3, cinemaName);
					pstmt.setString(4, theaterNumber);
					rowCount = pstmt.executeUpdate();
					if(rowCount == 0)
						System.out.println("예약 정보 삽입 실패");
					
					System.out.println("예매가 완료되었습니다.");
				} catch(SQLException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				System.out.println("예매를 취소합니다.");
				break;
			default:
		}
		
	}
	
}

