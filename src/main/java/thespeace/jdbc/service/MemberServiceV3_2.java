package thespeace.jdbc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import thespeace.jdbc.domain.Member;
import thespeace.jdbc.repository.MemberRepositoryV3;

import java.sql.SQLException;

/**
 * <h2>트랜잭션 - 트랜잭션 템플릿</h2>
 * 템플릿 콜백 패턴을 적용하려면 템플릿을 제공하는 클래스를 작성해야 하는데, 스프링은 {@code TransactionTemplate}
 * 라는 템플릿 클래스를 제공한다.
 * <blockquote><pre>
 *     public class TransactionTemplate {
 *         private PlatformTransactionManager transactionManager;
 *
 *         public <T> T execute(TransactionCallback<T> action){..}
 *         void executeWithoutResult(Consumer<TransactionStatus> action){..}
 *     }
 * </pre></blockquote>
 * <ul>
 *     <li>execute() : 응답 값이 있을 때 사용한다.</li>
 *     <li>executeWithoutResult() : 응답 값이 없을 때 사용한다.</li>
 * </ul>
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    // TransactionTemplate 을 사용하려면 transactionManager 가 필요하다. PlatformTransactionManager를 주입받고 내부에서는 TransactionTemplate 사용.
    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    /**
     * <h2>트랜잭션 템플릿 사용 로직</h2>
     * <ul>
     *     <li>트랜잭션 템플릿 덕분에 트랜잭션을 시작하고, 커밋하거나 롤백하는 코드가 모두 제거되었다.</li>
     *     <li>트랜잭션 템플릿의 기본 동작은 다음과 같다.
     *         <ul>
     *             <li>비즈니스 로직이 정상 수행되면 커밋한다.</li>
     *             <li>언체크 예외가 발생하면 롤백한다. 그 외의 경우 커밋한다.</li>
     *         </ul>
     *     </li>
     *     <li>코드에서 예외를 처리하기 위해 try~catch 가 들어갔는데, bizLogic() 메서드를 호출하면
     *         SQLException 체크 예외를 넘겨준다. 해당 람다에서 체크 예외를 밖으로 던질 수 없기 때문에 언체크 예외로
     *         바꾸어 던지도록 예외를 전환했다.</li>
     * </ul>
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        txTemplate.executeWithoutResult((status) -> {
            try {
                //비즈니스 로직
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
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
