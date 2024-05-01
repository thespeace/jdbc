# DB 락 - 변경
실습을 위해 기본 데이터를 입력
```sql
set autocommit true;
delete from member;
insert into member(member_id, money) values ('memberA',10000);
```

<br>

### 변경과 락

1. 세션 1
```sql
set autocommit false;
update member set money=500 where member_id = 'memberA';
```
* 세션 1이 트랜잭션을 시작하고, memberA의 데이터를 500원으로 업데이트 했다. 아직 커밋은 하지 않았다.
* memberA 로우의 락은 세션 1이 가지게 된다.

<br>

2. 세션 2
```sql
SET LOCK_TIMEOUT 60000;
set autocommit false;
update member set money=1000 where member_id = 'memberA';
```
* 세션2는 memberA 의 데이터를 1000원으로 수정하려 한다.
* 세션1이 트랜잭션을 커밋하거나 롤백해서 종료하지 않았으므로 아직 세션1이 락을 가지고 있다. 따라서 세션2는 락을 획득하지 못하기 때문에 데이터를 수정할 수 없다. 세션2는 락이 돌아올 때 까지 대기하게 된다.
* ```SET LOCK_TIMEOUT 60000``` : 락 획득 시간을 60초로 설정한다. 60초 안에 락을 얻지 못하면 예외가 발생한다.
  * 참고로 H2 데이터베이스에서는 딱 60초에 예외가 발생하지는 않고, 시간이 조금 더 걸릴 수 있다.

<br>

3. 세션 2 락 획득
* 세션1을 커밋하면 세션1이 ```commit```되면서 락을 반납한다. 이후에 대기하던 세션2가 락을 획득하게 된다. 따라서 락을 획득한 세션 2의 업데이트가 반영되는 것을 확인할 수 있다. 물론 이후에 세션2도 ```commit```을 호출해서 락을 반납해야 한다.

<br>

### 세션 2 락 타임아웃
* ```SET LOCK_TIMEOUT <milliseconds>``` : 락 타임아웃 시간을 설정한다.
  예) ```SET LOCK_TIMEOUT 10000``` 10초, 세션2에 설정하면 세션2가 10초 동안 대기해도 락을 얻지 못하면 락 타임아웃 오류가 발생한다.

