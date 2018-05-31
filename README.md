# autoconfig-spring-boot-starter

统一配置中心
1.采用Redis订阅发布机制，实现实时通知业务系统。
2.无入侵业务系统、低耦合。
3.在线修改业务配置项，无需重启业务系统。
4.后台未实现
5.开箱即用，提供starter，注解编程。


例如：
引入pom.xm
<dependency>
			<groupId>com.july</groupId>
			<artifactId>autoconfig-spring-boot-starter</artifactId>
			<version>0.0.1-SNAPSHOT</version>
</dependency>

在application配置文件指定Redis集群  
redis.host: *******
redis.port: 6379

当后台修改配置项调用Redis指令
groupId：组ID（建议每个业务系统统一分配）
key：配置项变量名称
value：更新的值
publish testXConfig '{"groupId":"testXConfig","key":"mycount","value":"500"}'
