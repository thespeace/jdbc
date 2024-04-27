package thespeace.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * <h2>언체크 예외 기본 이해</h2>
 * <ul>
 *     <li>RuntimeException 과 그 하위 예외는 언체크 예외로 분류된다.</li>
 *     <li>언체크 예외는 말 그대로 컴파일러가 예외를 체크하지 않는다는 뜻이다.</li>
 *     <li>언체크 예외는 체크 예외와 기본적으로 동일하다. 차이가 있다면 예외를 던지는 throws 를 선언하지 않고, 생략
 *     할 수 있다. 이 경우 자동으로 예외를 던진다.</li>
 *     <li>체크 예외: 예외를 잡아서 처리하지 않으면 항상 throws 에 던지는 예외를 선언해야 한다.</li>
 *     <li>언체크 예외: 예외를 잡아서 처리하지 않아도 throws 를 생략할 수 있다.</li>
 * </ul><br><br>
 *
 * <h2>언체크 예외의 장단점</h2>
 * 언체크 예외는 예외를 잡아서 처리할 수 없을 때, 예외를 밖으로 던지는 throws 예외 를 생략할 수 있다. 이것 때문에
 * 장점과 단점이 동시에 존재한다.
 * <ul>
 *     <li>장점: 신경쓰고 싶지 않은 언체크 예외를 무시할 수 있다. 체크 예외의 경우 처리할 수 없는 예외를 밖으로 던지려면
 *     항상 throws 예외 를 선언해야 하지만, 언체크 예외는 이 부분을 생략할 수 있다. 이후에 설명하겠지만, 신경쓰고 싶지
 *     않은 예외의 의존관계를 참조하지 않아도 되는 장점이 있다.</li>
 *     <li>단점: 언체크 예외는 개발자가 실수로 예외를 누락할 수 있다. 반면에 체크 예외는 컴파일러를 통해 예외 누락을
 *     잡아준다.</li>
 * </ul>
 */
@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throw() {
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }

    /**
     * <h2>RuntimeException을 상속받은 예외는 언체크 예외가 된다.</h2>
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
             super(message);
        }
    }

    /**
     * UnChecked 예외는 예외를 잡거나, 던지지 않아도 된다.<br>
     * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */
    static class Service {
        Repository repository = new Repository();

        //언체크 예외도 필요한 경우 예외를 잡아서 처리 할 수 있다.
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }

        //체크 예외와 다르게 throws 예외 선언을 하지 않아도 된다.(선택사항)
        //예외를 잡지 않아도 자연스럽게 상위로 올라가는데, 말 그대로 컴파일러가 이런 부분을 체크하지 않기 때문에 언체크 예외이다.
        public void callThrow() {
            repository.call();
        }
    }

    static class Repository {
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }
}
