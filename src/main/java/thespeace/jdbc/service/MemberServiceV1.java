package thespeace.jdbc.service;

import lombok.RequiredArgsConstructor;
import thespeace.jdbc.domain.Member;
import thespeace.jdbc.repository.MemberRepositoryV1;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    /**
     * <h2>계좌이체 비즈니스 로직</h2>
     * formId 의 회원을 조회해서 toId 의 회원에게 money 만큼의 돈을 계좌이체 하는 로직.
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money); //update sql 실행, fromId 회원의 돈 감소.
        validation(toMember); //예외 상황 테스트용, toId가 "ex"인 경우 예외 발생.
        memberRepository.update(toId, toMember.getMoney() + money); //update sql 실행, toId 회원의 돈 증가.
    }

    private static void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
