package thespeace.jdbc.repository;

import lombok.extern.slf4j.Slf4j;
import thespeace.jdbc.connection.DBConnectionUtil;
import thespeace.jdbc.domain.Member;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    /**
     * <h2>JDBC 개발 - 등록</h2>
     */
    public Member save(Member member) throws SQLException {

        //데이터베이스에 전달할 SQL을 정의한다. 여기서는 데이터를 등록해야 하므로 `insert sql`을 준비.
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection(); //데이터베이스 커넥션을 획득.

            //데이터베이스에 전달할 SQL과 파라미터로 전달할 데이터들을 준비.
            //PreparedStatement 는 Statement 의 자식 타입인데, ? 를 통한 파라미터 바인딩을 가능하게 해준다.
            //참고로 SQL Injection 공격을 예방하려면 PreparedStatement 를 통한 파라미터 바인딩 방식을 사용해야한다.
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId()); //SQL의 첫번째 ? 에 값을 지정한다. 문자이므로 `setString`을 사용.
            pstmt.setInt(2, member.getMoney()); //SQL의 두번째 ? 에 값을 지정한다. Int 형 숫자이므로 `setInt`를 지정.
            pstmt.executeUpdate(); //Statement 를 통해 준비된 SQL을 커넥션을 통해 실제 데이터베이스에 전달, 이후 영향받은 DB row 수를 반환한다.
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            //리소스는 꼭 정리해주어야 리소스 누수가 발생하지 않는다. 때문에 예외가 발생하든, 하지 않든 꼭 수행되게 finally 구문에 작성해야한다.
            //만약 이 부분을 놓치게 되면 커넥션이 끊어지지 않고 계속 유지되어 커넥션 부족으로 장애가 발생할 수 있다.
            close(con, pstmt, null);
        }
    }

    /**
     * <h2>JDBC 개발 - 조회</h2>
     * <p>
     * <h2>ResultSet</h2>
     * <ul>
     *     <li>ResultSet 은 테이블과 같은 데이터 구조이다. 보통 select 쿼리의 결과가 순서대로 들어간다.
     *         <ul>
     *             <li>예를 들어서 select member_id, money 라고 지정하면 member_id , money 라는 이름으로
     *                 데이터가 저장된다.</li>
     *             <li>참고로 select * 을 사용하면 테이블의 모든 컬럼을 다 지정한다.</li>
     *         </ul>
     *     </li>
     *     <li>ResultSet 내부에 있는 커서( cursor )를 이동해서 다음 데이터를 조회할 수 있다.</li>
     *     <li>rs.next() : 이것을 호출하면 커서가 다음으로 이동한다. 참고로 최초의 커서는 데이터를 가리키고 있지 않기
     *         때문에 rs.next() 를 최초 한번은 호출해야 데이터를 조회할 수 있다.
     *         <ul>
     *             <li>rs.next() 의 결과가 true 면 커서의 이동 결과 데이터가 있다는 뜻이다.</li>
     *             <li>rs.next() 의 결과가 false 면 더이상 커서가 가리키는 데이터가 없다는 뜻이다.</li>
     *         </ul>
     *     </li>
     *     <li>rs.getString("member_id") : 현재 커서가 가리키고 있는 위치의 member_id 데이터를
     *         String 타입으로 반환한다.</li>
     *     <li>rs.getInt("money") : 현재 커서가 가리키고 있는 위치의 money 데이터를 int 타입으로 반환한다</li>
     * </ul>
     */
    public Member findById(String memberId) throws SQLException {

        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery(); // executeQuery() : 데이터 조회를 할때 사용.

            if(rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    /**
     * <h2>JDBC 개발 - 수정</h2>
     */
    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize); // 쿼리를 실행하고 영향받은 row수를 반환.
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * <h2>JDBC 개발 - 삭제</h2>
     */
    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * <h2>리소스 정리</h2>
     * 쿼리를 실행하고 나면 리소스를 정리해야 한다. 여기서는 Connection , PreparedStatement 를 사용했다.
     * 리소스를 정리할 때는 항상 역순으로 해야한다. Connection 을 먼저 획득하고 Connection 을 통해
     * PreparedStatement 를 만들었기 때문에 리소스를 반환할 때는 PreparedStatement 를 먼저 종료하고, 그 다음에
     * Connection 을 종료하면 된다. 참고로 여기서 사용하지 않은 ResultSet 은 결과를 조회할 때 사용한다.
     */
    private void close(Connection con, Statement stmt, ResultSet rs) {
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if(stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if(con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    private static Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
