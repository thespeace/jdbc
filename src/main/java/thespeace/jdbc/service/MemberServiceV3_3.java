package thespeace.jdbc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import thespeace.jdbc.domain.Member;
import thespeace.jdbc.repository.MemberRepositoryV3;

import java.sql.SQLException;

/**
 * <h2>트랜잭션 - @Transactional AOP</h2>
 */
@Slf4j
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * <h2>스프링이 제공하는 트랜잭션 AOP를 적용</h2>
     * <ul>
     *     <li>순수한 비즈니스 로직만 남기고, 트랜잭션 관련 코드는 모두 제거.</li>
     *     <li>스프링이 제공하는 트랜잭션 AOP를 적용하기 위해 @Transactional 애노테이션을 추가.</li>
     *     <li>@Transactional 애노테이션은 메서드에 붙여도 되고, 클래스에 붙여도 된다.
     *         클래스에 붙이면 외부에서 호출 가능한 public 메서드가 AOP 적용 대상이 된다.</li>
     * </ul>
     */
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
