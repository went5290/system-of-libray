whenever sqlerror exit sql.sqlcode

insert into sys_role (id, role_code, role_name) values (1, 'SYSTEM_ADMIN', '系统管理员');
insert into sys_role (id, role_code, role_name) values (2, 'LIBRARIAN', '图书管理员');
insert into sys_role (id, role_code, role_name) values (3, 'READER', '读者');

insert into sys_user (id, username, password_hash, display_name)
values (1, 'admin', 'pbkdf2$120000$TiIXn/ioj++UBH3u56TneQ==$zPy1D8mVrkpDEeEdSqvvvjFhrcpy7A5QxEgVWX8DR3U=', '系统管理员');
insert into sys_user_role (user_id, role_id) values (1, 1);

insert into book_category (id, category_name, description) values (1, '计算机', '计算机科学与软件工程');
insert into book_category (id, category_name, description) values (2, '文学', '中外文学作品');
insert into book_category (id, category_name, description) values (3, '历史', '历史与传记');

insert into book (id, isbn, title, author, publisher, publish_date, category_id, description)
values (1, '9787111213826', 'Java 编程思想', 'Bruce Eckel', '机械工业出版社', date '2007-06-01', 1, 'Java 编程经典读物');
insert into book (id, isbn, title, author, publisher, publish_date, category_id, description)
values (2, '9787115428028', '深入理解计算机系统', 'Randal E. Bryant', '人民邮电出版社', date '2016-11-01', 1, '计算机系统基础');
insert into book (id, isbn, title, author, publisher, publish_date, category_id, description)
values (3, '9787020002207', '红楼梦', '曹雪芹', '人民文学出版社', date '1996-12-01', 2, '中国古典文学名著');

insert into book_copy (id, book_id, barcode, shelf_location) values (1, 1, 'LIB-000001', 'A-01-01');
insert into book_copy (id, book_id, barcode, shelf_location) values (2, 1, 'LIB-000002', 'A-01-01');
insert into book_copy (id, book_id, barcode, shelf_location) values (3, 2, 'LIB-000003', 'A-01-02');
insert into book_copy (id, book_id, barcode, shelf_location) values (4, 3, 'LIB-000004', 'B-01-01');

insert into library_rule (id, rule_code, rule_value, description) values (1, 'BORROW_DAYS', '30', '默认借阅天数');
insert into library_rule (id, rule_code, rule_value, description) values (2, 'MAX_RENEW_COUNT', '1', '最大续借次数');
insert into library_rule (id, rule_code, rule_value, description) values (3, 'FINE_PER_DAY', '0.50', '每日逾期费用');

commit;
exit
