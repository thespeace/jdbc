# 예외 포함과 스택 트레이스
예외를 전환할 때는 꼭! 기존 예외를 포함해야 한다. 그렇지 않으면 스택 트레이스를 확인할 때 심각한 문제가 발생한다.
```java
@Test
void printEx(){
    Controller controller = new Controller();
    try{
        controller.request();
    }catch(Exception e){
        //e.printStackTrace();
        log.info("ex",e);
    }
}
```
* 로그를 출력할 때 마지막 파라미터에 예외를 넣어주면 로그에 스택 트레이스를 출력할 수 있다.
  * 예) ```log.info("message={}", "message", ex)``` , 여기에서 마지막에 ex 를 전달하는 것을 확인할수 있다. 이렇게 하면 스택 트레이스에 로그를 출력할 수 있다.
  * 예) ```log.info("ex", ex)```, 지금 예에서는 파라미터가 없기 때문에, 예외만 파라미터에 전달하면 스택 트레이스를 로그에 출력할 수 있다.
* ```System.out``` 에 스택 트레이스를 출력하려면 ```e.printStackTrace()``` 를 사용하면 된다.
  * 실무에서는 항상 로그를 사용해야 한다는 점을 기억하자.

<br>

### 기존 예외를 포함하는 경우
```java
public void call(){
    try{
       runSQL();
    }catch(SQLException e){
        throw new RuntimeSQLException(e); //기존 예외(e) 포함
    }
}
```
예외를 포함해서 기존에 발생한 ```java.sql.SQLException``` 과 스택 트레이스를 확인할 수 있다.

<br>

### 기존 예외를 포함하지 않는 경우
```java
public void call(){
    try{
        runSQL();
    }catch(SQLException e){
        throw new RuntimeSQLException(); //기존 예외(e) 제외
    }
}
```
예외를 포함하지 않아서 기존에 발생한 ```java.sql.SQLException``` 과 스택 트레이스를 확인할 수 없다. 변환한 ```RuntimeSQLException``` 부터 예외를 확인할 수 있다.
만약 실제 DB에 연동했다면 DB에서 발생한 예외를 확인할 수 없는 심각한 문제가 발생한다.

### 예외를 전환할 때는 꼭! 기존 예외(root cause)를 포함하자!