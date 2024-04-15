# 트랜잭션 - DB 예제 2 - 자동 커밋, 수동 커밋
이전에 설명한 예제를 돌려보기 전에 먼저 자동 커밋, 수동 커밋에 대해 알아보자.

예제에 사용되는 스키마는 다음과 같다.
```sql
drop table member if exists;
create table member (
    member_id varchar(10),
    money integer not null default 0,
    primary key (member_id)
);
```

<br>

### 자동 커밋
트랜잭션을 사용하려면 먼저 자동 커밋과 수동 커밋을 이해해야 한다.
자동 커밋으로 설정하면 각각의 쿼리 실행 직후에 자동으로 커밋을 호출한다. 따라서 커밋이나 롤백을 직접 호출하지 않아도 되는 편리함이 있다. 하지만 쿼리를 하나하나 실행할 때 마다 자동으로 커밋이 되어버리기 때문에 우리가 원하는 트랜잭션 기능을 제대로 사용할 수 없다.

<br>

### 자동 커밋 설정
```sql
set autocommit true; #자동 커밋 모드 설정
insert into member(member_id, money) values ('data1',10000); #자동 커밋
insert into member(member_id, money) values ('data2',10000); #자동 커밋
```
따라서 ```commit```,```rollback```을 직접 호출하면서 트랜잭션 기능을 제대로 수행하려면 자동 커밋을 끄고 수동 커밋을 사용해야 한다.

<br>

### 수동 커밋 설정
```sql
set autocommit false; #수동 커밋 모드 설정
insert into member(member_id, money) values ('data3',10000);
insert into member(member_id, money) values ('data4',10000);
commit; #수동 커밋
```
보통 자동 커밋 모드가 기본으로 설정된 경우가 많기 때문에, '수동 커밋 모드로 설정한느 것을 트랜잭션을 시작'한다고 표현할 수 있다. 수동 커밋 설정을 하면 이후에 꼭 ```commit```,```rollback```을 호출해야 한다.

참고로 수동 커밋 모드나 자동 커밋 모드는 한번 설정하면 해당 세션에서는 계속 유지된다. 중간에 변경하는 것은 가능하다.