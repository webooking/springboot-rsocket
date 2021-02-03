DROP TABLE IF EXISTS t_user;

create table t_user (
    id varchar(36)  not null constraint t_user_pk primary key,
    username    varchar(50)  not null,
    age         smallint not null,
    gender      varchar(10) not null,
    version     bigint default 0,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp
);


select * from t_user;
update t_user set gender='Male', version=0, update_time=null where id='67d8dda8-284f-4a4c-918d-29347139a4bd';