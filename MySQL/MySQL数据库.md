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