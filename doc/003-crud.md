---
typora-root-url: ./assets
---

# 1 ORM

## 1.1 什么是ORM

ORM全称是：Object Relational Mapping(对象关系映射)。

## 1.2 r2dbc

Reactive Relational Database Connectivity

# 2 配置开发环境

## 2.1 model
- DTO
Data Transfer Object

- DAO
Data access object

- Entity
- VO
View Object

![image-20210201023751700](/image-20210201023751700.png)



## 2.2 docker ce



## 2.3 postgresql

![preview](/v2-6ea814c44f127504e82a538c0d8d7237_r.jpg)

```
1. 下载postgresql镜像
docker pull postgres:12.5-alpine

2. 查看镜像文件
docker images --filter "reference=postgres"
REPOSITORY   TAG           IMAGE ID       CREATED      SIZE
postgres     12.5-alpine   e07060185412   3 days ago   158MB

3. 使用镜像
docker container run --name postgres -e POSTGRES_PASSWORD=mysecretpassword -d -p 5432:5432 postgres:12.5-alpine

```

## 2.4 配置PostgreSQL

```
1. 查看正在运行的容器
docker container ls -a
CONTAINER ID   IMAGE                    STATUS          PORTS      NAMES
487cb17f2e2a   postgres:12.5-alpine     Up 15 minutes   0.0.0.0:5432->5432/tcp   postgres

2. 进入容器
docker container exec -it postgres bash

3. 进入默认用户postgres
bash-5.1# psql -U postgres
psql (12.5)
Type "help" for help.

4. 查看数据库
postgres=# \l
                                 List of databases
   Name    |  Owner   | Encoding |  Collate   |   Ctype    |   Access privileges   
-----------+----------+----------+------------+------------+-----------------------
 postgres  | postgres | UTF8     | en_US.utf8 | en_US.utf8 | 
 template0 | postgres | UTF8     | en_US.utf8 | en_US.utf8 | =c/postgres          +
           |          |          |            |            | postgres=CTc/postgres
 template1 | postgres | UTF8     | en_US.utf8 | en_US.utf8 | =c/postgres          +
           |          |          |            |            | postgres=CTc/postgres
(3 rows)

5. 创建测试用的数据库rsocket
postgres=# create USER admin with PASSWORD '123456';
CREATE ROLE
postgres=# create database rsocket owner admin;
CREATE DATABASE
postgres=# grant all privileges on database rsocket to admin;
GRANT
postgres=# alter user admin with CREATEROLE;
ALTER ROLE

6. 切换database & user
postgres=# \q
bash-5.1# psql -d rsocket -U admin;
psql (12.5)
Type "help" for help.

rsocket=> select * from pg_user;
 usename  | usesysid | usecreatedb | usesuper | userepl | usebypassrls |  passwd  | valuntil | useconfig 
----------+----------+-------------+----------+---------+--------------+----------+----------+-----------
 postgres |       10 | t           | t        | t       | t            | ******** |          | 
 admin    |    16398 | f           | f        | f       | f            | ******** |          | 
(2 rows)

rsocket=> \du
                                   List of roles
 Role name |                         Attributes                         | Member of 
-----------+------------------------------------------------------------+-----------
 admin     | Create role                                                | {}
 postgres  | Superuser, Create role, Create DB, Replication, Bypass RLS | {}


7. 为account和order分别创建独立的schema
rsocket=> create user account with password '123456';
CREATE ROLE
rsocket=> create user orders with password '123456';
CREATE ROLE
rsocket=> create schema account;
CREATE SCHEMA
rsocket=> create schema orders;
CREATE SCHEMA
rsocket=> 
rsocket=> grant all privileges on schema account to account;
GRANT
rsocket=> grant all privileges on schema orders to orders;
GRANT
rsocket=> grant usage on schema account to orders;
GRANT
rsocket=> grant usage on schema orders to account;
GRANT

```

## 2.5 intellij idea database tools

![image-20210201045541194](/image-20210201045541194.png)



![image-20210201144510663](/image-20210201144510663.png)

## 2.6 timezone

### 2.6.1 database

```
1. 进入容器
docker container exec -it postgres bash

2. 使用user account访问database rsocket
bash-5.1# psql -d rsocket -U account;

3. 查看默认的时区
rsocket=> show timezone;
 TimeZone 
----------
 UTC
(1 row)
```

### 2.6.2 docker container

以alpine linux为例：

```
bash-5.1# cat /proc/version
Linux version 4.19.121-linuxkit (root@buildkitsandbox) (gcc version 9.2.0 (Alpine 9.2.0)) #1 SMP Tue Dec 1 17:50:32 UTC 2020
bash-5.1# date -R
Tue, 02 Feb 2021 04:57:26 +0000
```


### 2.6.3 JVM

 ```
java -Duser.timezone=UTC -jar app.jar
 ```

### 2.6.4 springboot

```
TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
```

### 2.6.5 entity

| Database  | 描述           | 示例                        | Entity        | Vo     | DTO                          |
| --------- | -------------- | --------------------------- | ------------- | ------ | ---------------------------- |
| Timestamp | 日期+时间      | 2021-02-02T05:27:40.948881Z | LocalDateTime | String | datetimeFormat<br />timezone |
| Date      | 日期           | 2013-05-17                  | LocalDate     | String |                              |
| Time      | 一天内的时间点 | 12:52:51                    | LocalTime     | String |                              |



### 2.6.6 JSON encoder and decoder

```
val utcZone = ZoneId.of("UTC")
val beijingZone = ZoneId.of("Asia/Shanghai")
val vancouverZone = ZoneId.of("America/Vancouver")

TimeZone.setDefault(TimeZone.getTimeZone(utcZone))

val now = LocalDateTime.now()
val vancouver = now.atZone(utcZone)
			.withZoneSameInstant(vancouverZone)
			.format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z"))
println(vancouver) // 02/01/2021 - 22:46:29 -0800

val beijing = now.atZone(utcZone)
			.withZoneSameInstant(beijingZone)
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"))
println(beijing) // 2021-02-02 14:46:29 +0800
```


# 3 单表操作

## 3.1 dependencies

```
implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
runtimeOnly("io.r2dbc:r2dbc-postgresql")
```

## 3.2 init database

```
1. 新建目录 resources/db/migration/V1_init.sql
2. SQL
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
```

## 3.3 configuration

```
1. application-dev.yml
spring:
  r2dbc:
    url: r2dbc:pool:postgresql://127.0.0.1:5432/rsocket?currentSchema=account
    username: account
    password: 123456
    pool:
      enabled: true
      initial-size: 1
      max-size: 3
2. ConnectionFactoryInitializer
@Bean
fun initializer(connectionPool: ConnectionPool): ConnectionFactoryInitializer {
    val initializer = ConnectionFactoryInitializer()
    initializer.setConnectionFactory(connectionPool)
    initializer.setDatabasePopulator(
    	ResourceDatabasePopulator(ClassPathResource("db/migration/V1_init.sql"))
    )
    return initializer
}
```

## 3.4 CURD

### 3.4.1 model

```
package org.study.account.model

import java.time.LocalDateTime
import java.util.*

enum class Gender {
    Male, Female, Neutral
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val age: Int,
    val gender: Gender,
    val createTime: LocalDateTime = LocalDateTime.now(),
    val updateTime: LocalDateTime? = null
)
```









# 4 多表操作



# 5 复杂查询



