# MySQL 数据库

标签： mysql

## 存储过程

### Demo

	DROP PROCEDURE IF EXISTS TEST;
	DELIMITER //
	CREATE PROCEDURE TEST()
	BEGIN
	DECLARE is_state_insert INT DEFAULT 0;
		WHILE is_state_insert = 0 DO
			INSERT INTO TEST(`name`, `age`) VALUES('zhangsan', 20);
			IF (SELECT COUNT(1) FROM TEST WHERE `name`='zhangsan') = 1 THEN
				SET is_state_insert=1;
			END IF;
		END WHILE;
	END //
	DELIMITER ;

	CALL TEST();

## 授权

给用户授权：
> GRANT ALL PRIVILEGES ON *.* TO 'root'@'192.168.119.69' IDENTIFIED BY 'root';
FLUSH PRIVILEGES;

安装客户端：
> yum install mysql

安装服务端：
> yum install mysql-server mysql-devel

设置 root 用户密码：
> mysqladmin -u root password "newpass"

添加用户：
> create user 'sentry'@'192.168.99.134' identified by 'sentry';