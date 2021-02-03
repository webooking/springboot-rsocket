DROP TABLE IF EXISTS t_user;

create table t_user (
    id varchar(36)  not null constraint t_user_pk primary key,
    username    varchar(50)  not null,
    age         smallint not null,
    gender      varchar(10) not null,
    version     bigint,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp
);


select * from t_user;