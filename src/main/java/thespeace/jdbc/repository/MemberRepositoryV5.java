package thespeace.jdbc.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import thespeace.jdbc.domain.Member;

import javax.sql.DataSource;

/**
 * <h1>JDBC 반복 문제 해결 - JdbcTemplate</h1>
 * repository의 각각의 메서드를 살펴보면 상당히 많은 부분이 반복된다.
 * 이런 반복을 효과적으로 처리하는 방법이 바로 `템플릿 콜백 패턴`이다.<br>
 * 스프링은 JDBC의 반복 문제를 해결하기 위해 {@code JdbcTemplate}이라는 템플릿을 제공한다.<br>
 * 전체 구조와 이 기능을 사용해서 반복 코드를 제거할 수 있다는 것에 초점을 맞춰 살펴보자.<br><br>
 *
 * JdbcTemplate 은 JDBC로 개발할 때 발생하는 반복을 대부분 해결해준다.
 * 그 뿐만 아니라 트랜잭션을 위한 커넥션 동기화는 물론이고, 예외 발생시 스프링 예외 변환기도 자동으로 실행해준다.<br><br><br>
 *
 * <h2>정리</h2>
 * <ul>
 *     <li>서비스 계층의 순수성
 *         <ul>
 *             <li>트랜잭션 추상화 + 트랜잭션 AOP 덕분에 서비스 계층의 순수성을 최대한 유지하면서 서비스 계층에서 트랜잭션을 사용할 수 있다.</li>
 *             <li>스프링이 제공하는 예외 추상화와 예외 변환기 덕분에, 데이터 접근 기술이 변경되어도 서비스 계층의 순수성을 유지하면서
 *                 예외도 사용할 수 있다.</li>
 *             <li>서비스 계층이 리포지토리 인터페이스에 의존한 덕분에 향후 리포지토리가 다른 구현 기술로 변경되어도 서비스 계층을
 *                 순수하게 유지할 수 있다.</li>
 *         </ul>
 *     </li>
 *     <li>리포지토리에서 JDBC를 사용하는 반복 코드가 {@code JdbcTemplate}으로 대부분 제거되었다.</li>
 * </ul>
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository{

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";
        template.update(sql,member.getMemberId(), member.getMoney()); //반환값은 업데이트된 레코드 수.
        return member;
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }


    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }

    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }
}