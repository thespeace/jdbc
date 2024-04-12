package thespeace.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import thespeace.jdbc.connection.ConnectionConst;
import thespeace.jdbc.domain.Member;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static thespeace.jdbc.connection.ConnectionConst.*;

/**
 * <h1>DataSource 적용</h1>
 * <p>
 * <h2>DriverManagerDataSource</h2>
 * DriverManagerDataSource 를 사용하면 conn0~5 번호를 통해서 항상 새로운 커넥션이 생성되어서
 * 사용되는 것을 확인할 수 있다.
 * <p>
 * <ul><h2>HikariDataSource 사용</h2>
 *     <li>커넥션 풀 사용시 conn0 커넥션이 재사용 된 것을 확인할 수 있다.</li>
 *     <li>테스트는 순서대로 실행되기 때문에 커넥션을 사용하고 다시 돌려주는 것을 반복한다.
 *         따라서 conn0 만 사용된다.</li>
 *     <li>웹 애플리케이션에 동시에 여러 요청이 들어오면 여러 쓰레드에서 커넥션 풀의 커넥션을 다양하게 가져가는
 *         상황을 확인할 수 있다.</li>
 * </ul>
 * <h2>DI</h2>
 * {@code DriverManagerDataSource} -> {@code HikariDataSource} 로 변경해도 MemberRepositoryV1 의 코드는 전혀
 * 변경하지 않아도 된다. MemberRepositoryV1 는 DataSource 인터페이스에만 의존하기 때문이다. 이것이
 * DataSource 를 사용하는 장점이다.(DI(의존관계 주입) + OCP(개방-폐쇄 원칙))
 */
@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach() {
        //기본 DriverManager - 항상 새로운 커넥션을 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        //커넥션 풀링: HikariProxyConnection -> JdbcConnection
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);
    }

    @Test
    void crud() throws SQLException {
        //save
        Member member = new Member("memberV100", 10000);
        repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(member);

        //update: money: 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updateMember = repository.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000);

        //delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
    }
}