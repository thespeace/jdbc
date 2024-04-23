package thespeace.jdbc.service;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import thespeace.jdbc.domain.Member;
import thespeace.jdbc.repository.MemberRepositoryV3;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static thespeace.jdbc.connection.ConnectionConst.*;

/**
 * <h2>트랜잭션 - @Transactional AOP</h2>
 * <ul>
 *     <li>{@code @SpringBootTest} : 스프링 AOP를 적용하려면 스프링 컨테이너가 필요하다. 이 애노테이션이 있으면
 *         테스트시 스프링 부트를 통해 스프링 컨테이너를 생성한다. 그리고 테스트에서 @Autowired 등을 통해
 *         스프링 컨테이너가 관리하는 빈들을 사용할 수 있다.</li>
 *     <li>{@code @TestConfiguration} : 테스트 안에서 내부 설정 클래스를 만들어서 사용하면서 이 에노테이션을
 *         붙이면, 스프링 부트가 자동으로 만들어주는 빈들에 추가로 필요한 스프링 빈들을 등록하고 테스트를 수행할 수 있다.</li>
 *     <li>{@code TestConfig}
 *         <ul>
 *             <li>{@code DataSource} 스프링에서 기본으로 사용할 데이터소스를 스프링 빈으로 등록한다.
 *                 추가로 트랜잭션 매니저에서도 사용한다.</li>
 *             <li>{@code DataSourceTransactionManager} 트랜잭션 매니저를 스프링 빈으로 등록한다.
 *                 <ul>
 *                     <li>스프링이 제공하는 트랜잭션 AOP는 스프링 빈에 등록된 트랜잭션 매니저를 찾아서 사용하기 때문에
 *                         트랜잭션 매니저를 스프링 빈으로 등록해두어야 한다.</li>
 *                 </ul>
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 */
@Slf4j
@SpringBootTest
class MemberServiceV3_3Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;

    @Autowired
    private MemberServiceV3_3 memberService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    /**
     * <h2>AOP 프록시 적용 확인</h2>
     * <ul>
     *     <li>먼저 AOP 프록시가 적용되었는지 확인해보자. {@code AopCheck()} 의 실행 결과를 보면 {@code memberService}에
     *         {@code EnhancerBySpringCGLIB..} 라는 부분을 통해 프록시(CGLIB)가 적용된 것을 확인할 수 있다.
     *         {@code memberRepository}에는 AOP를 적용하지 않았기 때문에 프록시가 적용되지 않는다.</li>
     *     <li>나머지 테스트 코드들을 실행해보면 트랜잭션이 정상 수행되고, 실패시 정상 롤백된 것을 확인할 수 있다.</li>
     * </ul>
     */
    @Test
    void AopCheck() {
        log.info("memberService class={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        //when
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());

        //트랜잭션 롤백으로 복구
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}