---
typora-root-url: ./assets
---

# 1 centos8 安装docker

```
# 1. 清理
[opc@instance-20210616-0814 ~]$ sudo dnf remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

# 2. 添加repo
[opc@instance-20210616-0814 ~]$ sudo dnf config-manager --add-repo \
https://download.docker.com/linux/centos/docker-ce.repo

# 3. 安装docker-ce
[opc@instance-20210616-0814 ~]$ sudo dnf install -y docker-ce docker-ce-cli containerd.io

# 4. 启动docker
[opc@instance-20210616-0814 ~]$ sudo systemctl start docker

# 5. 设置docker开机启动
[opc@instance-20210616-0814 ~]$ sudo systemctl enable docker
```



# 2 nexus

```
1. 下载nexus镜像
docker pull sonatype/nexus3:3.29.2

2. 启动容器
docker container run -d -p 5433:8081 --name nexus sonatype/nexus3:3.29.2

3. 进入容器
docker container exec -it nexus bash

4. 查看密码
bash-4.4$ cat nexus-data/admin.password 
07f716e1-9299-470e-bcfd-7764dd3732d4
216077b5-0e47-4555-9159-b4c23fc5441e

5. 访问网址 http://localhost:5433/
用户名: admin
密码: 216077b5-0e47-4555-9159-b4c23fc5441e

6. 修改登录密码（请记住密码）
admin
Yurilee1986.
```

