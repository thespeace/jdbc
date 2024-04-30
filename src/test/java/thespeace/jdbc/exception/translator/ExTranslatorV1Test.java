package thespeace.jdbc.exception.translator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import thespeace.jdbc.domain.Member;
import thespeace.jdbc.repository.ex.MyDbException;
import thespeace.jdbc.repository.ex.MyDuplicateKeyException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static thespeace.jdbc.connection.ConnectionConst.*;

/**
 * <h1>직접 만든 데이터 접근 예외 적용 예시</h1>
 * <ul>
 *     <li>SQL ErrorCode로 데이터베이스에 어떤 오류가 있는지 확인할 수 있게 되었다.</li>
 *     <li>예외 변환을 통해 SQLException 을 특정 기술에 의존하지 않는 직접 만든 예외인
 *         {@code MyDuplicateKeyException}로 변환 할 수 있다.</li>
 *     <li>리포지토리 계층이 예외를 변환해준 덕분에 서비스 계층은 특정 기술에 의존하지 않는
 *         {@code MyDuplicateKeyException}을 사용해서 문제를 복구하고, 서비스 계층의 순수성도 유지할 수 있다.</li>
 * </ul><br>
 *
 * <h2>남은 문제</h2>
 * <ul>
 *     <li>SQL ErrorCode는 각각의 데이터베이스 마다 다르다. 결과적으로 데이터베이스가 변경될 때 마다
 *         ErrorCode도 모두 변경해야 한다. ex) 키 중복 오류 코드 (H2: 23505, MySQL: 1062)</li>
 *     <li>데이터베이스가 전달하는 오류는 키 중복 뿐만 아니라 락이 걸린 경우, SQL 문법에 오류 있는 경우 등등
 *         수십 수백가지 오류 코드가 있다. 이 모든 상황에 맞는 예외를 지금처럼 다 만들어야 할까? 추가로 앞서
 *         이야기한 것 처럼 데이터베이스마다 이 오류 코드는 모두 다르다. 스프링이 제공해주는 예외 추상화를 사용하자!</li>
 * </ul>
 */
@Slf4j
public class ExTranslatorV1Test {

    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId"); //같은 ID 저장 시도
    }

    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String memberId) {
            try {
                repository.save(new Member(memberId, 0));
                log.info("saveId={}", memberId);
            } catch (MyDuplicateKeyException e) { //MyDuplicateKeyException 예외 발생시, 새로운 아이디를 부여해서 예외 복구.
                log.info("키 중복, 복구 시도");
                String retryId = generateNewId(memberId);
                log.info("retryId={}", retryId);
                repository.save(new Member(retryId, 0));
            } catch (MyDbException e) { //예시를 위해 작성한 것일 뿐 여기서 예외로그를 남기지 않아도 되고, 복구할 수 없는 예외는 예외를 공통으로 처리하는 부분까지 전달해서 처리하면 된다.
                log.info("데이터 접근 계층 예외", e);
                throw e;
            }
        }

        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }
    }

    @RequiredArgsConstructor
    static class Repository {
        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values(?,?)";
            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;
            } catch (SQLException e) {
                //h2 db
                //오류 코드가 키 중복 오류( 23505 )인 경우 `MyDuplicateKeyException`을 새로 만들어서 서비스 계층에 던진다.
                if (e.getErrorCode() == 23505) {
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDbException(e);
            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(con);
            }
        }

    }
}
