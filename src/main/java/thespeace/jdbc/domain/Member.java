package thespeace.jdbc.domain;

import lombok.Data;

/**
 * <h2>member table schema</h2>
 * <blockquote><pre>
 * drop table member if exists cascade;
 * create table member (
 *     member_id varchar(10),
 *     money integer not null default 0,
 *     primary key (member_id)
 * );
 * </pre></blockquote>
 */
@Data
public class Member {

    private String memberId;
    private int money;

    public Member() {
    }

    public Member(String memberId, int money) {
        this.memberId = memberId;
        this.money = money;
    }
}