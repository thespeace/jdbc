# 트랜잭션 - DB 예제 3 - 트랜잭션 실습

<br>

### 1. 기본 데이터 입력
시작하기에 앞서 데이터베이스 연결 콘솔 창을 2개 연결하도록 하자.
연결 세션 값은 꼭 달라야한다.

```sql
#데이터 초기화
set autocommit true;
delete from member;
insert into member(member_id, money) values ('oldId',10000);
```

이렇게 데이터를 초기화하고, 세션 1, 세션 2 콘솔에서 다음 쿼리를 실행해서 결과를 확인하자.
```sql
select * from member;
```
세션 1, 세션 2 콘솔에서 동일하게 하나의 행만 출력되는 것을 확인 할 수 있다.

<br>

### 2. 신규 데이터 추가 - 커밋 전
세션 1에서 신규 데이터를 추가해보자. 아직 커밋은 하지 않을 것이다.

```sql
#트랜잭션 시작
set autocommit false; #수동 커밋 모드
insert into member(member_id, money) values ('newId1',10000);
insert into member(member_id, money) values ('newId2',10000);
```

세션 1, 세션 2에서 다음 쿼리를 실행해서 결과를 확인해보자.
아직 세션 1이 커밋을 하지 않은 상태이기 때문에 세션 1에서는 입력한 데이터가 보이지만, 세션 2에서는 입력한 데이터가 보이지 않는 것을 확인할 수 있다.

<br>

### 3. 커밋 - ```commit```
세션 1에서 신규 데이터를 입력했는데, 아직 커밋은 하지 않았다. 이제 커밋해서 데이터베이스에 결과를 반영해보자.
```sql
commit; #데이터베이스에 반영
```
세션 1, 세션 2에서 ```select * from member;``` 해당 쿼리로 결과를 비교해보자. 세션 1이 트랜잭션을 커밋했기 때문에 데이터베이스에 실제 데이터가 반영된다. 커밋 이후에는 모든 세션에서 데이터를 조회할 수 있다.

<br>

### 4. 롤백 - ```rollback```
예제를 처음으로 돌리기 위해 데이터를 초기화하자.
```sql
#데이터 초기화
set autocommit true;
delete from member;
insert into member(member_id, money) values ('oldId',10000);
```

세션1에서 트랜잭션을 시작 상태로 만든 다음에 데이터를 추가하자. 
```sql
#트랜잭션 시작
set autocommit false; #수동 커밋 모드
insert into member(member_id, money) values ('newId1',10000);
insert into member(member_id, money) values ('newId2',10000);
```

세션1, 세션2에서 해당 쿼리(```select * from member;```)를 실행해서 결과를 확인하자.
결과를 확인해보면, 아직 세션 1이 커밋을 하지 않은 상태이기 때문에 세션 1에서는 입력한 데이터가 보이지만, 세션 2에서는 입력한 데이터가 보이지 않는 것을 확인할 수 있다.

세션 1에서 롤백(```rollback```)을 하면 세션 1, 세션 2에서 롤백으로 인해 데이터가 DB에 반영되지 않은 것을 확인할 수 있다.```select * from member;```