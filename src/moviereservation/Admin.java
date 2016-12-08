package moviereservation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

public class Admin {
	
	private Statement stmt;
	ResultSet rs;
	private Scanner scanner;
	private Connection conn;

	public Admin(Connection conn) {
		this.conn = conn;
	}

	public void login() throws SQLException {
		scanner = new Scanner(System.in);
		String userId = "";
		String userPwd = "";
		String query = "";
		stmt = conn.createStatement();
		while (true) {
			System.out.println("관리자 로그인");
			System.out.print("ID : ");
			userId = scanner.nextLine();
			scanner.reset();
			// 아이디 저장하고
			System.out.print("PW : ");
			userPwd = scanner.nextLine();
			System.out.println(userId);
			query = "SELECT CUSTOMER_PASSWORD FROM CUSTOMER WHERE CUSTOMER_ID='" + userId + "'";
			try {
				rs = stmt.executeQuery(query);
				rs.next();
				System.out.println(rs.getString(1));
				System.out.println(userId);

				if (rs.getString(1).equals(userPwd)) {
					System.out.println("로그인 성공");
					break;
				} else {
					System.out.println("비밀번호를 잘못 입력하셨습니다");
				}
			} catch (Exception e) {
				System.out.println("아이디를 잘못 입력하셨습니다." + e.getMessage());
			}
		}

		adminMenu();
	}

	private void adminMenu() throws SQLException { // 메뉴 선택
		int menu = 0;

		while (true) {
			scanner.reset();

			System.out.println("\n1. 영화 관리");
			System.out.println("2. 영화관 관리");
			System.out.println("3. VIP 고객 관리");
			System.out.println("4. 영화 티켓 발권");
			System.out.println("0. 종료");
			System.out.print("어떤 기능을 이용하시겠습니까?");

			menu = scanner.nextInt();

			if (0 <= menu && menu <= 4)
				break;
			else
				continue;
		}

		switch (menu) {
		case 1:
			manageMovie();
			break;
		case 2:
			manageCinema();
			break;
		case 3:
			checkVIP();
			break;
		case 4:
			ticketing();
			break;
		case 5:
			System.out.println("프로그램이 종료되었습니다.");
			System.exit(0);
			break;
		}
	}

	private void manageMovie() throws SQLException {
		int mNum = 1;
		scanner.reset();
		int menu = 0;

		String query = "SELECT TITLE,MOVIE_ID  FROM MOVIE";
		ArrayList<String> mList = new ArrayList<String>();
		mList.add("쓰레기");

		System.out.println(" 영화 관리");

		try {
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(mNum + ". " + rs.getString(1)); // 영화 타이틀
																	// 출력되고
				mList.add(rs.getString(2)); // 리스트에 영화 아이디 넣는다. (1번부터 시작)
				mNum++;
			}
		} catch (Exception e) {
			System.out.println();
		}

		while (true) {
			scanner.reset();
			System.out.println("\n1. 영화 등록\t 2. 영화 수정\t 3. 영화 삭제\t0.뒤로가기");
			System.out.print("어떤 기능을 실행하시겠습니까? ");
			menu = scanner.nextInt();

			if (0 <= menu && menu <= 3)
				break;
			else
				continue;
		}

		switch (menu) {
		case 1:
			registMovie();
			break;
		case 2:
			System.out.print("영화를 수정합니다. 위에 있는 영화의 번호를 입력해주세요 : ");
			int temp = scanner.nextInt();
			// mList.get(temp); 여기에 해당 영화 아이디가 담겨져있음 인자로 넘겨서 삭제
			modifyMovie(mList.get(temp));
			break;
		case 3:
			System.out.print("영화를 삭제합니다. 위에 있는 영화의 번호를 입력해주세요 : ");
			temp = scanner.nextInt();
			// mList.get(temp); 여기에 해당 영화 아이디가 담겨져있음 인자로 넘겨서 삭제
			deleteMovie(mList.get(temp));
			break;
		case 0:
			adminMenu();
			break;
		}

	}

	private void registMovie() throws SQLException { // 영화 등록
		// 관리자로부터 입력받을 값들
		String movId;
		String title;
		int runningTime;
		int rating;
		String director;
		String movInfo;
		String actor;

		String query;

		System.out.println("신규 영화를 등록합니다. 데이터를 입력해주세요.");

		scanner = new Scanner(System.in);
		System.out.print("영화 제목 : ");
		title = scanner.nextLine();

		System.out.print("영화 감독 : ");
		director = scanner.nextLine();

		System.out.print("러닝 타임 : ");
		runningTime = scanner.nextInt();

		System.out.print("제한 연령 : ");
		rating = scanner.nextInt();

		scanner.nextLine();

		System.out.print("영화 정보 : ");
		movInfo = scanner.nextLine();

		query = "SELECT MOVIE_ID FROM MOVIE";
		// 영화 아이디 등록할때 영화 아이디가 겹치는게 있는지 확인후에 등록.
		while (true) {
			try {
				boolean idNotInTable = true;
				rs = stmt.executeQuery(query); // 아이디 모두 얻어오고

				scanner.reset();
				System.out.print("영화 ID (3자리 숫자) : ");
				movId = "M-" + scanner.nextLine();

				while (rs.next()) {
					if (movId.equals(rs.getString(1))) {
						// 테이블 안에 id가 동일한것을 발견
						idNotInTable = false;
						System.out.println("이미 중복되는 ID가 존재합니다.");
						break;
					}
				}

				if (idNotInTable) {
					// 영화 삽입 쿼리
					query = "INSERT INTO MOVIE VALUES('" + movId + "'," + runningTime + ",'" + title + "'," + rating
							+ ",'" + director + "','" + movInfo + "')";
					stmt.executeQuery(query);
					System.out.println("영화 등록이 완료되었습니다.");
					break;
				}
			} catch (SQLException e) {
				System.out.println("영화 등록이 실패했습니다.");
			}
		}
		// 여기까지 영화 삽입 과정 밑에는 배우 삽입

		System.out.println("등록하신 영화에 출연하는 배우를 등록합니다. (종료를 원하실땐 0 입력)");

		while (true) {
			// 밑에 쿼리는 해당 영화에 출연하는 배우들 목록을 뽑아오는 쿼리 계속 갱신될수 있기때문에 반복문안에 위치한다.
			String actorQuery = "SELECT ACTOR FROM ACTOR WHERE MOVIE_ID='" + movId + "'";
			boolean actorNotInTable = true;
			try {
				rs = stmt.executeQuery(actorQuery);
				System.out.print("배우 이름 : ");
				actor = scanner.nextLine();
				// 입력값 0 일때 종료
				if (actor.equals("0"))
					break;

				while (rs.next()) {
					if (actor.equals(rs.getString(1))) {
						System.out.println("이미 중복되는 ID가 존재합니다.");
						actorNotInTable = false;
						break;
					}
				}

				if (actorNotInTable) {
					query = "INSERT INTO ACTOR VALUES ('" + movId + "', '" + actor + "')";
					stmt.executeQuery(query);
					System.out.println("배우 등록이 완료 되었습니다.");
				}

			} catch (Exception e) {
				System.out.println("배우 등록이 실패했습니다.");
			}

		}
		// 메뉴 선택으로 돌아간다.
		manageMovie();
	}

	private void modifyMovie(String movId) throws SQLException { // 영화 수정
		// 관리자로부터 입력받을 값들
		String title;
		int runningTime;
		int rating;
		String director;
		String movInfo;
		String query;
		int menu = 0;

		System.out.println("\n1. 영화 정보 수정\t 2. 배우 추가\t 3. 배우 삭제\t0. 뒤로가기");
		System.out.print("어떤 기능을 실행하시겠습니까? ");
		menu = scanner.nextInt();
		scanner.nextLine(); // 버퍼비우고

		switch (menu) {
		case 1:
			System.out.println("영화 정보를 수정합니다. 데이터를 입력해주세요.");

			scanner = new Scanner(System.in);
			System.out.print("영화 제목 : ");
			title = scanner.nextLine();

			System.out.print("영화 감독 : ");
			director = scanner.nextLine();

			System.out.print("러닝 타임 : ");
			runningTime = scanner.nextInt();

			System.out.print("제한 연령 : ");
			rating = scanner.nextInt();

			scanner.nextLine();

			System.out.print("영화 정보 : ");
			movInfo = scanner.nextLine();

			// 영화정보 수정 쿼리
			query = "UPDATE MOVIE SET TITLE='" + title + "', DIRECTOR ='" + director + "', RUNNINGTIME=" + runningTime
					+ "," + " RATING =" + rating + ", MOVIE_INFO='" + movInfo + "' WHERE MOVIE_ID='" + movId + "'";
			System.out.println(query);
			try {
				stmt.executeQuery(query);
				System.out.println("영화 정보가 수정되었습니다.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			break;
		case 2:
			addActor(movId);
			break;
		case 3:
			deleteActor(movId);
			break;
		case 0:
			manageMovie();
			break;
		}

		// 여기까지 영화 삽입 과정 밑에는 배우 삽입

		// 메뉴 선택으로 돌아간다.
		manageMovie();
	}

	private void addActor(String movId) throws SQLException {
		String actor;
		System.out.println("출연 배우를 추가합니다. (종료를 원하실땐 0 입력)");
		while (true) {
			// 밑에 쿼리는 해당 영화에 출연하는 배우들 목록을 뽑아오는 쿼리 계속 갱신될수 있기때문에 반복문안에 위치한다.
			String actorQuery = "SELECT ACTOR FROM ACTOR WHERE MOVIE_ID='" + movId + "'";
			boolean actorNotInTable = true;
			try {
				rs = stmt.executeQuery(actorQuery);
				System.out.print("배우 이름 : ");
				actor = scanner.nextLine();
				// 입력값 0 일때 종료
				if (actor.equals("0"))
					break;

				while (rs.next()) {
					if (actor.equals(rs.getString(1))) {
						System.out.println("이미 해당 배우가 존재합니다.");
						actorNotInTable = false;
						break;
					}
				}

				if (actorNotInTable) {
					String query = "INSERT INTO ACTOR VALUES ('" + movId + "', '" + actor + "')";
					stmt.executeQuery(query);
					System.out.println("배우 등록이 완료 되었습니다.");
				}

			} catch (Exception e) {
				System.out.println("배우 등록이 실패했습니다.");
			}

		}
	}

	private void deleteActor(String movId) {
		String actor;
		System.out.println("\n출연 배우를 삭제합니다. (종료를 원하실땐 0 입력)");
		while (true) {
			// 밑에 쿼리는 해당 영화에 출연하는 배우들 목록을 뽑아오는 쿼리 계속 갱신될수 있기때문에 반복문안에 위치한다.
			String actorQuery = "SELECT ACTOR FROM ACTOR WHERE MOVIE_ID='" + movId + "'";
			boolean actorInTable = true;
			try {
				rs = stmt.executeQuery(actorQuery);
				System.out.print("배우 이름 : ");
				actor = scanner.nextLine();
				// 입력값 0 일때 종료
				if (actor.equals("0"))
					break;

				while (rs.next()) {
					if (actor.equals(rs.getString(1))) {
						String query = "DELETE FROM ACTOR WHERE MOVIE_ID='" + movId + "' AND ACTOR='" + actor + "'";
						stmt.executeQuery(query);
						System.out.println("해당 배우가 삭제되었습니다.");
						actorInTable = false;
						break;
					}
				}

				if (actorInTable) {
					System.out.println("해당 배우가 등록되있지 않습니다.");
				}

			} catch (Exception e) {
				System.out.println("배우 삭제가 실패했습니다.");
			}
		}

	}

	private void deleteMovie(String movId) throws SQLException { // 영화 삭제
		String query = "DELETE FROM MOVIE WHERE MOVIE_ID='" + movId + "'";
		try {
			stmt.executeQuery(query);
			System.out.println("해당 영화가 삭제되었습니다.");
		} catch (Exception e) {
			System.out.println("삭제가 실패했습니다.");
		}
		// 삭제 실행하고. 다시 메뉴 선택으로 돌아간다.
		manageMovie();
	}

	private void checkVIP() throws SQLException { // vip 고객 관리
		// vip를 순서대로 뽑아오는 작업
		String query = "SELECT SUM(T.SEAT_COUNT) AS TOTAL_SEAT_COUNT, R.CUSTOMER_ID " + "FROM TICKET T, RESERVATION R "
				+ "WHERE T.START_TIME >= TO_DATE(SYSDATE -30, 'YY/MM/DD/HH24') AND R.TICKET_NUMBER = T.TICKET_NUMBER "
				+ "GROUP BY R.CUSTOMER_ID " + "ORDER BY TOTAL_SEAT_COUNT DESC";

		int i = 1;
		System.out.println("\n VIP 고객 명단 ");
		try {
			rs = stmt.executeQuery(query);

			while (rs.next() && i <= 10) {
				System.out.println(i + ". " + rs.getString(2) + "\t" + rs.getString(1));
				i++;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		adminMenu();
	}

	private void manageCinema() throws SQLException {
		int cinemaNumber = 1;
		String query = "SELECT CINEMA_NAME FROM CINEMA";
		ArrayList<String> cinemaList = new ArrayList<String>();
		cinemaList.add("쓰레기");

		System.out.println("\n영화관 관리를 시작합니다.");
		try {
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(cinemaNumber + ". " + rs.getString(1));
				cinemaList.add(rs.getString(1));
				cinemaNumber++;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		int menu = 0;

		while (true) {
			System.out.println("\n1. 영화관 등록\t 2. 영화관 수정\t 3. 영화관 삭제\t4. 상영관 관리\t0. 뒤로가기");
			System.out.print("어떤 기능을 이용하시겠습니까?");
			menu = scanner.nextInt();
			scanner.nextLine();
			if (0 <= menu && menu <= 4) {
				break;
			}
		}

		switch (menu) {
		case 1:
			registCinema();
			break;
		case 2:
			System.out.print("수정할 영화관의 번호를 입력해주세요 : ");
			int num = scanner.nextInt();
			scanner.nextLine();
			// 리스트에 해당 영화관 이름이 담겨져있음 인자로 넘겨서 수정
			modifyCinema(cinemaList.get(num));
			break;
		case 3:
			System.out.print("수정할 영화관의 번호를 입력해주세요 : ");
			num = scanner.nextInt();
			scanner.nextLine();
			// 리스트에 해당 영화관 이름이 담겨져있음 인자로 넘겨서 수정
			deleteCinema(cinemaList.get(num));
			break;
		case 4:
			System.out.println("어떤 영화관의 상영관을 관리할지 번호를 입력해주세요 : ");
			num = scanner.nextInt();
			scanner.nextLine();
			manageTheater(cinemaList.get(num));
			break;
		case 0:
			adminMenu();
			break;
		}
	}

	private void registCinema() throws SQLException {
		String cinemaName;
		String phoneNum;
		String address;

		String query = "SELECT CINEMA_NAME FROM CINEMA";

		System.out.println("\n영화관을 신규 등록합니다.");

		// 영화 아이디 등록할때 영화 아이디가 겹치는게 있는지 확인후에 등록.
		while (true) {
			try {
				boolean idNotInTable = true;
				rs = stmt.executeQuery(query); // 영화관 모두 얻어오고

				System.out.print("영화관 이름 : ");
				cinemaName = scanner.nextLine() + "CGV";

				while (rs.next()) {
					if (cinemaName.equals(rs.getString(1))) {
						// 테이블 안에 있는 영화관중 동일한것을 발견
						idNotInTable = false;
						System.out.println("이미 중복되는 영화관이 존재합니다.");
						break;
					}
				}

				if (idNotInTable) {
					// 영화관 삽입 쿼리
					System.out.print("영화관 전화번호 : ");
					phoneNum = scanner.nextLine();

					System.out.print("영화관 주소 : ");
					address = scanner.nextLine();

					query = "INSERT INTO CINEMA VALUES('" + cinemaName + "','" + phoneNum + "','" + address + "')";
					stmt.executeQuery(query);
					System.out.println("영화관 등록이 완료되었습니다.");
					break;
				}
			} catch (SQLException e) {
				System.out.println("영화관 등록이 실패했습니다.");
			}
		}

		manageCinema();
	}

	private void modifyCinema(String cinemaName) throws SQLException {
		// 영화관 수정
		String phoneNum;
		String address;

		System.out.println("\n 영화관 정보를 수정합니다.");
		System.out.print("영화관 전화번호 : ");
		phoneNum = scanner.nextLine();

		System.out.print("영화관 주소 : ");
		address = scanner.nextLine();

		String query = "UPDATE CINEMA SET PHONE_NUMBER='" + phoneNum + "', ADDRESS='" + address
				+ "' WHERE CINEMA_NAME='" + cinemaName + "'";
		try {
			stmt.executeQuery(query);
			System.out.println("해당 영화관 정보가 수정되었습니다.");
		} catch (Exception e) {
			System.out.println("영화관 정보 수정이 실패했습니다.");
		}
		manageCinema();
	}

	private void deleteCinema(String cinemaName) throws SQLException { // 영화관 삭제
		System.out.println("\n 영화관을 삭제합니다.");
		String query = "DELETE FROM CINEMA WHERE CINEMA_NAME='" + cinemaName + "'";
		try {
			stmt.executeQuery(query);
			System.out.println("해당 영화관이 삭제되었습니다.");
		} catch (Exception e) {
			System.out.println("영화관 삭제가 실패했습니다.");
		}
		manageCinema();
	}

	private void manageTheater(String cName) throws SQLException {

		String query = "SELECT THEATER_NUMBER FROM THEATER WHERE CINEMA_NAME='" + cName + "'";
		ArrayList<String> theaterList = new ArrayList<String>();
		theaterList.add("쓰레기");
		int cNum = 1;

		System.out.println("\n상영관 관리를 시작합니다.");
		System.out.println("상영관 목록");

		try {
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(cNum + ". " + rs.getString(1) + "관");
				theaterList.add(rs.getString(1));
				cNum++;
			}
		} catch (Exception e) {
			System.out.println("오류 발생");
			System.out.println(e.getMessage());
		}

		int menu = 0;
		while (true) {
			System.out.println("\n1. 상영관 등록\t2. 상영관 수정\t3. 상영관 삭제\t0. 뒤로가기");
			System.out.print("어떤 기능을 이용하시겠습니까?");
			menu = scanner.nextInt();
			scanner.nextLine();

			if (0 <= menu && menu <= 3)
				break;
		}
		switch (menu) {
		case 1:
			registTheater(cName);
			break;
		case 2:
			System.out.print("수정할 상영관의 영화의 번호를 입력해주세요 : ");
			int temp = scanner.nextInt();
			modifyTheater(cName, theaterList.get(temp));
			break;
		case 3:
			System.out.print("삭제할 상영관의 영화의 번호를 입력해주세요 : ");
			temp = scanner.nextInt();
			deleteTheater(cName, theaterList.get(temp));
			break;
		case 0:
			manageCinema();
			break;
		}
	}

	private void registTheater(String cName) throws SQLException {
		String theaterNum;
		int seatCapacity;
		int price;

		System.out.println("\n상영관을 추가합니다.");

		String query = "SELECT THEATER_NUMBER FROM THEATER WHERE CINEMA_NAME='" + cName + "'";
		// 영화 아이디 등록할때 영화 아이디가 겹치는게 있는지 확인후에 등록.
		while (true) {
			try {
				boolean idNotInTable = true;
				rs = stmt.executeQuery(query); // 영화관 모두 얻어오고

				System.out.print("상영관 번호 : ");
				theaterNum = scanner.nextLine();

				while (rs.next()) {
					if (theaterNum.equals(rs.getString(1))) {
						// 테이블 안에 있는 영화관중 동일한것을 발견
						idNotInTable = false;
						System.out.println("이미 중복되는 상영관이 존재합니다.");
						break;
					}
				}

				if (idNotInTable) {
					// 영화관 삽입 쿼리
					System.out.print("좌석 수 : ");
					seatCapacity = scanner.nextInt();

					System.out.print("좌석 가격 : ");
					price = scanner.nextInt();
					scanner.nextLine();

					query = "INSERT INTO THEATER (CINEMA_NAME, THEATER_NUMBER, SEATING_CAPACITY, PRICE) VALUES " + "( '"
							+ cName + "', '" + theaterNum + "', " + seatCapacity + ", " + price + ")";
					stmt.executeQuery(query);
					System.out.println("상영관 등록이 완료되었습니다.");
					break;
				}
			} catch (SQLException e) {
				System.out.println("상영관 등록이 실패했습니다.");
			}
		}
		manageTheater(cName);
	}

	private void deleteTheater(String cName, String tNum) throws SQLException {
		boolean isDel = true;
		System.out.println("\n 상영관을 삭제합니다.");
		String query = "DELETE FROM THEATER WHERE CINEMA_NAME='" + cName + "' AND THEATER_NUMBER='" + tNum + "'";

		try {
			stmt.executeQuery(query);
			System.out.println("해당 상영관이 삭제되었습니다.");
		} catch (Exception e) {
			System.out.println("상영관 삭제가 실패했습니다.");
			isDel = false;
		}
		if (isDel) {
			query = "DELETE FROM SCHEDULE WHERE CINEMA_NAME='" + cName + "' AND THEATER_NUMBER='" + tNum + "'";
			try {
				stmt.executeQuery(query);
				System.out.println("스케쥴이 삭제되었습니다.");
			} catch (Exception e) {
				System.out.println("스케쥴 삭제가 실패했습니다.");
			}
		}
		manageTheater(cName);
	}

	private void modifyTheater(String cName, String tNum) throws SQLException {
		int menu = 0;

		System.out.println("\n상영관 정보를 수정합니다.");

		while (true) {
			System.out.println("1. 상영관 기본정보 수정\t2. 상영 영화 수정\t3. 스케쥴 수정\t0. 뒤로가기");
			System.out.print("어떤 기능을 이용하시겠습니까?");
			menu = scanner.nextInt();
			scanner.nextLine();
			if (0 <= menu && menu <= 3)
				break;
		}

		switch (menu) {
		case 1:
			modifyBasicInfoTheater(cName, tNum);
			break;
		case 2:
			modifyMovieInTheater(cName, tNum);
			break;
		case 3:
			modifySchedule(cName, tNum);
			break;
		case 0:
			manageTheater(cName);
			break;
		}
	}

	private void modifyBasicInfoTheater(String cName, String tNum) throws SQLException {

		int seatCapacity;
		int price;

		System.out.println("\n상영관 기본 정보를 수정합니다.");

		System.out.print("좌석 수 : ");
		seatCapacity = scanner.nextInt();

		System.out.print("좌석 가격 : ");
		price = scanner.nextInt();

		String query = "UPDATE THEATER SET PRICE=" + price + ", SEATING_CAPACITY=" + seatCapacity
				+ " WHERE CINEMA_NAME='" + cName + "' AND THEATER_NUMBER='" + tNum + "'";
		try {
			stmt.executeQuery(query);
			System.out.println("해당 상영관 정보가 수정되었습니다.");
		} catch (Exception e) {
			System.out.println("상영관 정보 수정이 실패했습니다.");
		}
		modifyTheater(cName, tNum);
	}

	private void modifyMovieInTheater(String cName, String tNum) throws SQLException {
		ArrayList<String> movieList = new ArrayList<String>();
		movieList.add("쓰레기");
		int mNum = 1;
		boolean isDel = true;

		int userChoice = 0;
		System.out.println("\n상영관에서 상영하는 영화를 수정합니다. 스케쥴 정보도 삭제되니 주의하세요");
		// 상영 영화 목록을 출력해주고 선택하게 하려고함
		String query = "SELECT TITLE, MOVIE_ID FROM MOVIE";
		try {
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(mNum + ". " + rs.getString(1));
				movieList.add(rs.getString(2));
				mNum++;
			}
		} catch (Exception e) {
		}

		System.out.print("\n상영관에서 새로 상영할 영화의 번호를 입력하세요 ( 0 입력시 취소 ) : ");
		userChoice = scanner.nextInt();
		scanner.nextLine();

		query = "UPDATE THEATER SET MOVIE_ID='" + movieList.get(userChoice) + "' WHERE CINEMA_NAME='" + cName
				+ "' AND THEATER_NUMBER='" + tNum + "'";
		if (0 < userChoice && userChoice < movieList.size()) {
			try {
				stmt.executeQuery(query);
				System.out.println("상영 영화가 수정되었습니다^^");
			} catch (Exception e) {
				System.out.println("상영 영화 수정에 실패했습니다.");
				isDel = false;
			}
		} else
			modifyTheater(cName, tNum);
		// 여기까지 상영관에 영화 id수정과정 밑으론 스케쥴 삭제하는 과정
		if (isDel) {
			query = "DELETE FROM SCHEDULE WHERE CINEMA_NAME='" + cName + "' AND THEATER_NUMBER='" + tNum + "'";
			try {
				stmt.executeQuery(query);
				System.out.println("스케쥴이 삭제되었습니다.");
			} catch (Exception e) {
				System.out.println("스케쥴 삭제가 실패했습니다.");
			}
		}
		modifyTheater(cName, tNum);
	}

	private void modifySchedule(String cName, String tNum) throws SQLException {
		String query = "SELECT START_TIME FROM SCHEDULE WHERE CINEMA_NAME='" + cName + "' AND THEATER_NUMBER='" + tNum
				+ "'";

		int menu = 0;
		System.out.println("\n스케쥴 수정을 실행했습니다.");
		try {
			System.out.println(" " + cName + " " + tNum + "관 스케쥴");
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
		} catch (Exception e) {
		}

		System.out.println("1. 상영 시간 추가\t2. 상영 시간 삭제\t0. 뒤로가기");
		System.out.print("어떤 기능을 이용하시겠습니까?");

		menu = scanner.nextInt();
		scanner.nextLine();

		switch (menu) {
		case 1:
			addSchedule(cName, tNum);
			break;
		case 2:
			deleteSchedule(cName, tNum);
			break;
		case 0:
			modifyTheater(cName, tNum);
			break;
		}
	}

	public void deleteSchedule(String cinemaName, String theaterNumber) throws SQLException {
		String query = "SELECT TITLE FROM MOVIE WHERE MOVIE_ID = "
				+ "(SELECT MOVIE_ID FROM THEATER WHERE CINEMA_NAME = '" + cinemaName + "' AND THEATER_NUMBER = '"
				+ theaterNumber + "')";

		String query2 = "SELECT START_TIME FROM SCHEDULE WHERE CINEMA_NAME = '" + cinemaName
				+ "' AND THEATER_NUMBER = '" + theaterNumber + "'";

		try {

			rs = stmt.executeQuery(query);

			String movieTitle = "";

			while (rs.next()) {
				movieTitle = rs.getString(1);
			}

			ResultSet rs2 = stmt.executeQuery(query2);

			int[] startTimeList = new int[24];

			System.out.println("\n상영중인 영화 : " + movieTitle);

			int i = 0;

			System.out.print("해당 상영관의 상영 시간 : ");
			while (rs2.next()) {
				startTimeList[i] = rs2.getInt(1);
				i++;
				System.out.print(rs2.getInt(1) + " ");
			}

			int timeForDelete = 0;

			System.out.print("\n삭제할 상영 시간을 입력하세요 : ");
			timeForDelete = scanner.nextInt();

			if (timeForDelete < 0 || timeForDelete > 23) {
				System.out.println("상영 시간은 0시부터 23시 사이의 시간입니다.");
			}

			String startTime;
			if(timeForDelete < 10)
				startTime = "0" + timeForDelete;
			else
				startTime = "" + timeForDelete;
			boolean find = false;
			for (int index = 0; index < i; index++) {
				if (startTimeList[index] == timeForDelete) {
					try {
						Statement stmt = conn.createStatement();
						String query3 = "DELETE FROM SCHEDULE WHERE CINEMA_NAME = '" + cinemaName
								+ "' AND THEATER_NUMBER = '" + theaterNumber + "' AND START_TIME = '" + startTime
								+ "'";
						int rowCount = stmt.executeUpdate(query3);
						if (rowCount == 0) {
							System.out.println("데이터 삭제 실패");
						} else {
							System.out.println("데이터 삭제 성공");
						}
					} catch (Exception e) {
						System.out.println("[*]   DELETE 오류 발생: \n" + e.getMessage());
					}

					find = true;
				}
			}

			if (!find) {
				System.out.println("해당 상영 시간이 존재하지 않습니다.");
			}
		} catch (Exception e) {
			System.out.println("[*]   질의 결과 출력 오류 발생: \n" + e.getMessage());
		}
		modifySchedule(cinemaName, theaterNumber);
	}

	public void addSchedule(String cinemaName, String theaterNumber) throws SQLException {
		String query1 = "SELECT RUNNINGTIME, TITLE FROM MOVIE WHERE MOVIE_ID = "
				+ "(SELECT MOVIE_ID FROM THEATER WHERE CINEMA_NAME = '" + cinemaName + "' AND THEATER_NUMBER = '"
				+ theaterNumber + "')";

		// 해당 상영관의 상영시간들을 받아온다.
		String query2 = "SELECT START_TIME FROM SCHEDULE WHERE CINEMA_NAME = '" + cinemaName
				+ "' AND THEATER_NUMBER = '" + theaterNumber + "'";
		try {

			rs = stmt.executeQuery(query1);

			int runningTime = 0;
			String movieTitle = "";

			while (rs.next()) {
				runningTime = rs.getInt(1);
				movieTitle = rs.getString(2);
			}

			ResultSet rs2 = stmt.executeQuery(query2);

			int[] startTimeList = new int[24];

			int i = 0;

			System.out.println("\n상영중인 영화 : " + movieTitle + " , 러닝타임 : " + runningTime + "분");

			System.out.print("해당 상영관의 상영 시간 : ");
			while (rs2.next()) {
				startTimeList[i] = rs2.getInt(1);
				i++;
				System.out.print(rs2.getInt(1) + " ");
			}

			int timeForAdd = 0;
			while (true) {
				System.out.print("\n추가할 상영 시간을 입력하세요 : ");
				timeForAdd = scanner.nextInt();
				if (timeForAdd < 0 || timeForAdd > 23) {
					System.out.println("상영 시간은 0시부터 23시 사이의 시간입니다.");
				} else {
					break;
				}
			}
			int index;
			boolean insert = false;
			String startTime;
			if(timeForAdd < 10)
				startTime = "0" + timeForAdd;
			else
				startTime = "" + timeForAdd;
			if (i == 0) {
				try {
					Statement stmt = conn.createStatement();
					String query = "INSERT INTO SCHEDULE VALUES('" + cinemaName + "', '" + theaterNumber + "', '"
							+ startTime + "')";
					System.out.println(query);
					int rowCount = stmt.executeUpdate(query);
					if (rowCount == 0) {
						System.out.println("데이터 삽입 실패");
					} else {
						System.out.println("데이터 삽입 성공");
					}
				} catch (Exception e) {
					System.out.println("[*]   INSERT 오류 발생: \n" + e.getMessage());
				}
			} else {
				for (index = 0; index < i; index++) {
					if (startTimeList[index] > timeForAdd) {
						if (index == 0) {
							if ((startTimeList[index] - timeForAdd) * 60 - runningTime > 0) {
								try {
									Statement stmt = conn.createStatement();
									String query = "INSERT INTO SCHEDULE VALUES('" + cinemaName + "', '" + theaterNumber
											+ "', '" + startTime + "')";
									System.out.println(query);
									int rowCount = stmt.executeUpdate(query);
									if (rowCount == 0) {
										System.out.println("데이터 삽입 실패");
									} else {
										System.out.println("데이터 삽입 성공");
										insert = true;
									}
								} catch (Exception e) {
									System.out.println("[*]   INSERT 오류 발생: \n" + e.getMessage());
								}
							} else {
								System.out.println("다른 상영시간과 겹칩니다.");
							}
						} else {
							if ((startTimeList[index] - timeForAdd) * 60 - runningTime > 0
									&& (timeForAdd - startTimeList[index - 1]) * 60 - runningTime > 0) {
								try {
									Statement stmt = conn.createStatement();
									String query = "INSERT INTO SCHEDULE VALUES('" + cinemaName + "', '" + theaterNumber
											+ "', '" + startTime + "')";
									System.out.println(query);
									int rowCount = stmt.executeUpdate(query);
									if (rowCount == 0) {
										System.out.println("데이터 삽입 실패");
									} else {
										System.out.println("데이터 삽입 성공");
										insert = true;
									}
								} catch (Exception e) {
									System.out.println("[*]   INSERT 오류 발생: \n" + e.getMessage());
								}
							} else {
								System.out.println("다른 상영시간과 겹칩니다.");
							}
						}
						break;
					}
				}
				if(index == i && !insert){
					if ((timeForAdd - startTimeList[index-1]) * 60 - runningTime > 0) {
						try {
							Statement stmt = conn.createStatement();
							String query = "INSERT INTO SCHEDULE VALUES('" + cinemaName + "', '" + theaterNumber
									+ "', '" + startTime + "')";
							System.out.println(query);
							int rowCount = stmt.executeUpdate(query);
							if (rowCount == 0) {
								System.out.println("데이터 삽입 실패");
							} else {
								System.out.println("데이터 삽입 성공");
								insert = true;
							}
						} catch (Exception e) {
							System.out.println("[*]   INSERT 오류 발생: \n" + e.getMessage());
						}
					} else {
						System.out.println("다른 상영시간과 겹칩니다.");
					}
				}
			}
		} catch (Exception e) {
			System.out.println("[*]   질의 결과 출력 오류 발생: \n" + e.getMessage());
		}
		modifySchedule(cinemaName, theaterNumber);
	}

	private void ticketing() throws SQLException {
		String ticketNumber;

		System.out.println("\n영화 티켓 발권을 시작합니다.");
		System.out.print("티켓 번호를 입력해주세요 : ");
		scanner.nextLine();
		ticketNumber = scanner.nextLine();

		String query = "SELECT PAYMENT, SEAT_COUNT FROM TICKET WHERE TICKET_NUMBER='" + ticketNumber + "'";

		try {
			rs = stmt.executeQuery(query);
			rs.next();
			
			String payment = rs.getString(1);
			int seatCount = rs.getInt(2);
			query = "SELECT * FROM RESERVATION WHERE TICKET_NUMBER = '" + ticketNumber + "'";

			rs = stmt.executeQuery(query);
			rs.next();
			
			String userId = rs.getString(1);			
			String cinemaName = rs.getString(3);
			String theaterNumber = rs.getString(4);
			
			query = "SELECT PRICE FROM THEATER WHERE CINEMA_NAME = '" + cinemaName
					+ "' AND THEATER_NUMBER = '" + theaterNumber +"'";
			
			rs = stmt.executeQuery(query);
			rs.next();
			int price = rs.getInt(1);
			
			switch (payment) {
			case "X":
				System.out.println("이미 발권된 티켓입니다.");
				break;	
			case "INTERNET":
				System.out.println("결제 완료된 티켓입니다.");
				System.out.println("티켓이 발권되었습니다.");
				query = "UPDATE TICKET SET PAYMENT='X' WHERE TICKET_NUMBER='" + ticketNumber + "'";
				stmt.executeQuery(query);
				break;
			case "DIRECT":
				System.out.println("결제가 완료되지 않은 티켓입니다. 현장결제를 진행합니다.");
				System.out.println("\n총액 : " + price * seatCount);
				System.out.println("1. 결제, 2. 취소");
				System.out.println("결제를 하시겠습니까? : ");
				
				
				while(true){
					int select = scanner.nextInt();
					
					if(select == 1){
						selectPoint(price * seatCount, userId);
						query = "UPDATE TICKET SET PAYMENT='X' WHERE TICKET_NUMBER='" + ticketNumber + "'";
						stmt.executeQuery(query);
						break;
					}
					else if(select == 2){
						System.out.println("결제가 취소됩니다.");
						break;
					}
					else{
						System.out.println("잘못된 입력입니다.");
					}
				}


				break;
			}
		} catch (Exception e) {
			System.out.println("없는 티켓 번호입니다." + e.getMessage());
		}
		adminMenu();
	}
	
	private void selectPoint(int price, String userId) {
		int availablePoint = 0;
		int pointToUse = 0;
		
		int select = 0;
		
		System.out.println("1. 포인트 사용하기, 2. 포인트 사용하지 않기");
		select = scanner.nextInt();
		
		while(select < 1 || select > 2) {
			System.out.println("잘못된 명령입니다. 다시 입력하세요 : ");
			select = scanner.nextInt();
		}
		
		switch(select) {
			case 1:
				try {
					
					String query = "SELECT CUSTOMER_POINT FROM CUSTOMER WHERE CUSTOMER_ID = '" + userId + "'";
					
					rs = stmt.executeQuery(query);
					
					rs.next();
					availablePoint = rs.getInt(1);
					System.out.println("사용 가능한 포인트 : " + availablePoint);
					if(availablePoint < 1000) {
						System.out.println("포인트가 부족하여 사용할 수 없습니다.");
						pointToUse = 0;
					}
					
					else {
						System.out.print("사용할 포인트 : ");
						pointToUse = scanner.nextInt();
					}
					
					pay(price, pointToUse);
					
				} catch(SQLException e) {
					e.printStackTrace();
				}
				
				break;
			case 2:
				pointToUse = 0;
				pay(price, pointToUse);
				break;
			default:
		}
		
	}
	
	private void pay(int priceForPay, int pointToUse) {
		int price = priceForPay;
		if (price >= pointToUse)
			price -= pointToUse;
		else {
			pointToUse = price;
			price = 0;
		}
		
		int select = 0;

		System.out.println("총액 : " + price);
		System.out.println("1. 결제, 2. 취소");
		System.out.print("결제를 하시겠습니까? : ");
		select = scanner.nextInt();

		while (select < 1 || select > 2) {
			System.out.println("잘못된 명령입니다. 다시 입력하세요 : ");
			select = scanner.nextInt();
		}
		
		switch(select) {
		case 1:
			System.out.println("결제가 완료되었습니다.");
			break;
		case 2:
			System.out.println("결제를 취소합니다.");
			break;
		default:
	}
	}

}
