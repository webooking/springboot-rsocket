DROP Type IF EXISTS gender;
DROP TABLE IF EXISTS t_user;

CREATE TYPE gender AS ENUM ('Male', 'Female', 'Neutral');
create table t_user (
    id varchar(36)  not null constraint t_user_pk primary key,
    username    varchar(50)  not null,
    age         smallint not null,
    gender      gender not null,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp
);
