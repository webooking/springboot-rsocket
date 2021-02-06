DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_account;

create table t_user (
    id varchar(36)  not null primary key,
    username    varchar(50)  not null,
    age         smallint not null,
    gender      varchar(10) not null,
    version     bigint default 0,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp
);

create table t_account (
    id varchar(36)  not null primary key,
    user_id varchar(36) not null,
    balance    int4  not null CHECK(balance >= 0),
    version     bigint default 0,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp
);

insert into t_account(id,user_id,balance) values
 (uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'user001', 90),
 (uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'user002', 0),
 (uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'user003', 10);

select * from t_account;
select * from t_user;