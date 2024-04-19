package thespeace.jdbc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import thespeace.jdbc.domain.Member;
import thespeace.jdbc.repository.MemberRepositoryV2;
import thespeace.jdbc.repository.MemberRepositoryV3;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    //트랜잭션 매니저를 주입(지금은 JDBC 기술을 사용하기 때문에 DataSourceTransactionManager 구현체를 주입)
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작, 현재 트랜잭션의 상태 정보가 포함되어 있는 TransactionStatus 반환.
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // new DefaultTransactionDefinition() : 트랜잭션과 관련된 옵션을 지정할 수 있다. 자세한 내용은 나중에 확인.
        try {
            //비즈니스 로직
            bizLogic(fromId, toId, money);
            transactionManager.commit(status); //성공시 커밋.
        } catch (Exception e) {
            transactionManager.rollback(status); //실패시 롤백
            throw new IllegalStateException(e);
        }
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money); //update sql 실행.
        validation(toMember); //예외 상황 테스트용.
        memberRepository.update(toId, toMember.getMoney() + money); //update sql 실행.
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
