# 체크 예외 활용
언제 체크 예외를 사용하고 언제 언체크(런타임) 예외를 사용하면 좋을까?

#### 기본 원칙은 다음 2가지를 기억하자.
* 기본적으로 언체크(런타임) 예외를 사용하자.
* 체크 예외는 비즈니스 로직상 의도적으로 던지는 예외에만 사용하자.
  * 이 경우 해당 예외를 잡아서 반드시 처리해야 하는 문제일 때만 체크 예외를 사용해야 한다. 예를 들어서 다음과 같은 경우가 있다.
  * 체크 예외 예)
    * 계좌 이체 실패 예외
    * 결제시 포인트 부족 예외
    * 로그인 ID, PW 불일치 예외
  * 물론 이 경우에도 100% 체크 예외로 만들어야 하는 것은 아니다. 다만 계좌 이체 실패처럼 매우 심각한 문제는 개발자가 실수로 예외를 놓치면 안된다고 판단할 수 있다.
    이 경우 체크 예외로 만들어 두면 컴파일러를 통해 놓친 예외를 인지할 수 있다.

<br>

### 체크 예외의 문제점
체크 예외는 컴파일러가 예외 누락을 체크해주기 때문에 개발자가 실수로 예외를 놓치는 것을 막아준다.
그래서 항상 명시적으로 예외를 잡아서 처리하거나, 처리할 수 없을 때는 예외를 던지도록 ```method() throws 예외```로 선언해야 한다.
지금까지 이야기를 들어보면 체크 예외가 런타임 예외보다 더 안전하고 좋아보이는데, 왜 체크 예외를 기본으로 사용하는 것이 문제가 될까?

그림과 코드로 문제점을 이해해보자.

![Using_checked_exceptions](Using_checked_exceptions1.PNG)

* 리포지토리는 DB에 접근해서 데이터를 저장하고 관리한다. 여기서는 SQLException 체크 예외를 던진다.
* ```NetworkClient``` 는 외부 네트워크에 접속해서 어떤 기능을 처리하는 객체이다. 여기서는 ```ConnectException``` 체크 예외를 던진다.
* 서비스는 리포지토리와 ```NetworkClient``` 를 둘다 호출한다.
  * 따라서 두 곳에서 올라오는 체크 예외인 ```SQLException``` 과 ```ConnectException``` 을 처리해야 한다.
  * 그런데 서비스는 이 둘을 처리할 방법을 모른다. ```ConnectException``` 처럼 연결이 실패하거나, ```SQLException``` 처럼 데이터베이스에서 발생하는 문제처럼
    심각한 문제들은 대부분 애플리케이션 로직에서 처리할 방법이 없다.
* 서비스는 ```SQLException``` 과 ```ConnectException``` 를 처리할 수 없으므로 둘다 밖으로 던진다.
  * 체크 예외이기 때문에 던질 경우 다음과 같이 선언해야 한다.
  * ```method() throws SQLException, ConnectException```
* 컨트롤러도 두 예외를 처리할 방법이 없다.
  * 다음을 선언해서 예외를 밖으로 던진다.
  * ```method() throws SQLException, ConnectException```
* 웹 애플리케이션이라면 서블릿의 오류 페이지나, 또는 스프링 MVC가 제공하는 ```ControllerAdvice``` 에서 이런 예외를 공통으로 처리한다.
  * 이런 문제들은 보통 사용자에게 어떤 문제가 발생했는지 자세히 설명하기가 어렵다. 그래서 사용자에게는 "서비스에 문제가 있습니다." 라는 일반적인 메시지를 보여준다.
    ("데이터베이스에 어떤 오류가 발생했어요"라고 알려주어도 일반 사용자가 이해할 수 없다. 그리고 보안에도 문제가 될 수 있다.)
  * API라면 보통 HTTP 상태코드 500(내부 서버 오류)을 사용해서 응답을 내려준다.
  * 이렇게 해결이 불가능한 공통 예외는 별도의 오류 로그를 남기고, 개발자가 오류를 빨리 인지할 수 있도록 메일, 알림(문자, 슬랙)등을 통해서 전달 받아야 한다.
    예를 들어서 SQLException 이 잘못된 SQL을 작성해서 발생했다면, 개발자가 해당 SQL을 수정해서 배포하기 전까지 사용자는 같은 문제를 겪게 된다.

```java
package thespeace.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.net.ConnectException;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class CheckedAppTest {
    
    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call() throws SQLException {
            throw new SQLException("ex");
        }
    }
    
}
```
* 서비스
  * 체크 예외를 처리하지 못해서 밖으로 던지기 위해 logic() throws SQLException,ConnectException 를 선언했다.
* 컨트롤러
  * 체크 예외를 처리하지 못해서 밖으로 던지기 위해 request() throws SQLException,ConnectException 를 선언했다.

<br>

### 2가지 문제
지금까지 설명한 예시와 코드를 보면 크게 2가지 문제를 알 수 있다.
1. 복구 불가능한 예외
2. 의존 관계에 대한 문제

<br>

### 1. 복구 불가능한 예외
대부분의 예외는 복구가 불가능하다. 일부 복구가 가능한 예외도 있지만 아주 적다.<br>
```SQLException``` 을 예를 들면 데이터베이스에 무언가 문제가 있어서 발생하는 예외이다. SQL 문법에 문제가 있을 수도 있고, 데이터베이스 자체에 뭔가 문제가 발생했을 수도 있다.
데이터베이스 서버가 중간에 다운 되었을 수도 있다. 이런 문제들은 대부분 복구가 불가능하다. 특히나 대부분의 서비스나 컨트롤러는 이런 문제를 해결할 수는 없다.
따라서 이런 문제들은 일관성 있게 공통으로 처리해야 한다. 오류 로그를 남기고 개발자가 해당 오류를 빠르게 인지하는 것이 필요하다.
서블릿 필터, 스프링 인터셉터, 스프링의 ```ControllerAdvice``` 를 사용하면 이런 부분을 깔끔하게 공통으로 해결할 수 있다.

### 2. 의존 관계에 대한 문제
체크 예외의 또 다른 심각한 문제는 예외에 대한 의존 관계 문제이다.<br>
앞서 대부분의 예외는 복구 불가능한 예외라고 했다. 그런데 체크 예외이기 때문에 컨트롤러나 서비스 입장에서는 본인이 처리할 수 없어도 어쩔 수 없이 ```throws``` 를 통해 던지는 예외를 선언해야 한다.

<br>

### 체크 예외 throws 선언
```java
class Controller {
    public void request() throws SQLException, ConnectException {
        service.logic();
    }
}
class Service {
    public void logic() throws SQLException, ConnectException {
        repository.call();
        networkClient.call();
    }
}
```
```throws SQLException```, ```ConnectException``` 처럼 예외를 던지는 부분을 코드에 선언하는 것이 왜 문제가 될까?<br>
바로 서비스, 컨트롤러에서 ```java.sql.SQLException``` 을 의존하기 때문에 문제가 된다.

향후 리포지토리를 JDBC 기술이 아닌 다른 기술로 변경한다면, 그래서 ```SQLException``` 이 아니라 예를 들어서 ```JPAException```으로 예외가 변경된다면 어떻게 될까?
```SQLException```에 의존하던 모든 서비스, 컨트롤러의 코드를 ```JPAException```에 의존하도록 고쳐야 한다.

서비스나 컨트롤러 입장에서는 어차피 본인이 처리할 수 도 없는 예외를 의존해야 하는 큰 단점이 발생하게 된다.<br>
결과적으로 OCP, DI를 통해 클라이언트 코드의 변경 없이 대상 구현체를 변경할 수 있다는 장점이 체크 예외 때문에 발목을 잡게 된다.

<br>

### 체크 예외 구현 기술 변경시 파급 효과
![Using_checked_exceptions](Using_checked_exceptions2.PNG)
* JDBC JPA 같은 기술로 변경하면 예외도 함께 변경해야한다. 그리고 해당 예외를 던지는 모든 다음 부분도 함께 변경해야 한다.
  * ```logic() throws SQLException``` -> ```logic() throws JPAException```
  * (참고로 JPA 예외는 실제 이렇지는 않고, 이해하기 쉽게 예를 든 것이다.)

<br>

### 정리
* 처리할 수 있는 체크 예외라면 서비스나 컨트롤러에서 처리하겠지만, 지금처럼 데이터베이스나 네트워크 통신처럼 시스템 레벨에서 올라온 예외들은 대부분 복구가 불가능하다.
  그리고 실무에서 발생하는 대부분의 예외들은 이런 시스템 예외들이다.
* 문제는 이런 경우에 체크 예외를 사용하면 아래에서 올라온 복구 불가능한 예외를 서비스, 컨트롤러 같은 각각의 클래스가 모두 알고 있어야 한다. 그래서 불필요한 의존관계 문제가 발생하게 된다.

<br>

### throws Exception
```SQLException``` , ```ConnectException``` 같은 시스템 예외는 컨트롤러나 서비스에서는 대부분 복구가 불가능하고 처리할 수 없는 체크 예외이다. 따라서 다음과 같이 처리해주어야 한다.<br>
```java
void method() throws SQLException, ConnectException {..}
```

그런데 다음과 같이 최상위 예외인 Exception 을 던져도 문제를 해결할 수 있다.<br>
```java
void method() throws Exception {..}
```
이렇게 하면 ```Exception``` 은 물론이고 그 하위 타입인 ```SQLException``` , ```ConnectException``` 도 함께 던지게 된다.
코드가 깔끔해지는 것 같지만, ```Exception``` 은 최상위 타입이므로 모든 체크 예외를 다 밖으로 던지는 문제가 발생한다.


결과적으로 체크 예외의 최상위 타입인 ```Exception```을 던지게 되면 다른 체크 예외를 체크할 수 있는 기능이 무효화되고, 중요한 체크 예외를 다 놓치게 된다.
중간에 중요한 체크 예외가 발생해도 컴파일러는 ```Exception``` 을 던지기 때문에 문법에 맞다고 판단해서 컴파일 오류가 발생하지 않는다.
이렇게 하면 모든 예외를 다 던지기 때문에 체크 예외를 의도한 대로 사용하는 것이 아니다. 따라서 꼭 필요한 경우가 아니면 이렇게 ```Exception```자체를 밖으로 던지는 것은 좋지 않은 방법이다.