# 트랜잭션 문제 해결 - 트랜잭션 AOP 이해
* 지금까지 트랜잭션을 편리하게 처리하기 위해서 트랜잭션 추상화도 도입하고, 추가로 반복적인 트랜잭션 로직을
  해결하기 위해 트랜잭션 템플릿도 도입했다.
* 트랜잭션 템플릿 덕분에 트랜잭션을 처리하는 반복 코드는 해결할 수 있었다.
  하지만 서비스 계층에 순수한 비즈니스 로직만 남긴다는 목표는 아직 달성하지 못했다.
* 이럴 때 스프링 AOP를 통해 프록시를 도입하면 문제를 깔끔하게 해결할 수 있다.

> 참고<br>
> 스프링 AOP와 프록시에 대해서 지금은 자세히 이해하지 못해도 괜찮다. 지금은 @Transactional 을 사용하면 스프링이 AOP를 사용해서 트랜잭션을 편리하게 처리해준다 정도로 이해해도 된다.<br>
> 스프링 AOP와 프록시에 대해서는 따로 알아보자.

<br>

### 프록시를 통한 문제 해결
![Spring_and_problem_solving(transactions)-AOP](Spring_and_problem_solving(transactions)-AOP1.PNG)

프록시를 도입하기 전에는 기존처럼 서비스의 로직에서 트랜잭션을 직접 시작한다.

"서비스 계층의 트랜잭션 사용 코드 예시"
```java
//트랜잭션 시작
TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

try {
    //비즈니스 로직
    bizLogic(fromId, toId, money);
    transactionManager.commit(status); //성공시 커밋
} catch (Exception e) {
    transactionManager.rollback(status); //실패시 롤백
    throw new IllegalStateException(e);
}
```

<br>

#### "프록시 도입 후"
![Spring_and_problem_solving(transactions)-AOP](Spring_and_problem_solving(transactions)-AOP2.PNG)

프록시를 사용하면 트랜잭션을 처리하는 객체와 비즈니스 로직을 처리하는 서비스 객체를 명확하게 분리할 수 있다.

"트랜잭션 프록시 코드 예시"
```java
public class TransactionProxy {
    private MemberService target;

    public void logic() {
        //트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(..);
        try {
            //실제 대상 호출
            target.logic();
            transactionManager.commit(status); //성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); //실패시 롤백
            throw new IllegalStateException(e);
        }
    }
}
```

<br>

#### "트랜잭션 프록시 적용 후 서비스 코드 예시"
```java
public class Service {
    public void logic() {
        //트랜잭션 관련 코드 제거, 순수 비즈니스 로직만 남음
        bizLogic(fromId, toId, money);
    }
}
```
* 프록시 도입 전: 서비스에 비즈니스 로직과 트랜잭션 처리 로직이 함께 섞여있다.
* 프록시 도입 후: 트랜잭션 프록시가 트랜잭션 처리 로직을 모두 가져간다. 그리고 트랜잭션을 시작한 후에 실제 서비스를 대신 호출한다.
  트랜잭션 프록시 덕분에 서비스 계층에는 순수한 비즈니즈 로직만 남길 수 있다.

<br>

### 스프링이 제공하는 트랜잭션 AOP
* 스프링이 제공하는 AOP 기능을 사용하면 프록시를 매우 편리하게 적용할 수 있다. AOP의 @Aspect , @Advice , @Pointcut 를 사용해서 트랜잭션 처리용 AOP를 어떻게 만들지 머리속으로 그림이 그려질 것이다.
* 물론 스프링 AOP를 직접 사용해서 트랜잭션을 처리해도 되지만, 트랜잭션은 매우 중요한 기능이고, 전세계 누구나 다 사용하는 기능이다.
  스프링은 트랜잭션 AOP를 처리하기 위한 모든 기능을 제공한다. 스프링 부트를 사용하면 트랜잭션 AOP를 처리하기 위해 필요한 스프링 빈들도 자동으로 등록해준다.
* 개발자는 트랜잭션 처리가 필요한 곳에 @Transactional 애노테이션만 붙여주면 된다. 스프링의 트랜잭션 AOP는 이 애노테이션을 인식해서 트랜잭션 프록시를 적용해준다.

> #### @Transactional ```org.springframework.transaction.annotation.Transactional```

> 참고<br>
> 스프링 AOP를 적용하려면 어드바이저, 포인트컷, 어드바이스가 필요하다. 스프링은 트랜잭션 AOP 처리를 위해
> 다음 클래스를 제공한다.<br>스프링 부트를 사용하면 해당 빈들은 스프링 컨테이너에 자동으로 등록된다.<br><br>
> 
> 어드바이저: ```BeanFactoryTransactionAttributeSourceAdvisor```<br>
> 포인트컷: ```TransactionAttributeSourcePointcut```<br>
> 어드바이스: ```TransactionInterceptor```