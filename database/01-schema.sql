whenever sqlerror exit sql.sqlcode

create table sys_role (
  id number(19) primary key,
  role_code varchar2(30) not null unique,
  role_name varchar2(60) not null,
  created_at timestamp default current_timestamp not null
);

create table sys_user (
  id number(19) primary key,
  username varchar2(50) not null unique,
  password_hash varchar2(100) not null,
  display_name varchar2(80) not null,
  enabled number(1) default 1 not null check (enabled in (0, 1)),
  created_at timestamp default current_timestamp not null,
  updated_at timestamp default current_timestamp not null
);

create table sys_user_role (
  user_id number(19) not null references sys_user(id),
  role_id number(19) not null references sys_role(id),
  constraint pk_sys_user_role primary key (user_id, role_id)
);

create table reader (
  id number(19) primary key,
  user_id number(19) unique references sys_user(id),
  reader_no varchar2(30) not null unique,
  phone varchar2(30),
  email varchar2(120),
  max_borrow_count number(3) default 5 not null,
  status varchar2(20) default 'ACTIVE' not null check (status in ('ACTIVE', 'SUSPENDED', 'CLOSED')),
  created_at timestamp default current_timestamp not null
);

create table book_category (
  id number(19) primary key,
  category_name varchar2(80) not null unique,
  parent_id number(19) references book_category(id),
  description varchar2(500)
);

create table book (
  id number(19) primary key,
  isbn varchar2(20) unique,
  title varchar2(200) not null,
  author varchar2(150),
  publisher varchar2(150),
  publish_date date,
  category_id number(19) references book_category(id),
  description varchar2(1000),
  created_at timestamp default current_timestamp not null,
  updated_at timestamp default current_timestamp not null
);

create table book_copy (
  id number(19) primary key,
  book_id number(19) not null references book(id),
  barcode varchar2(50) not null unique,
  shelf_location varchar2(80),
  status varchar2(20) default 'AVAILABLE' not null
    check (status in ('AVAILABLE', 'BORROWED', 'LOST', 'DAMAGED', 'MAINTENANCE')),
  acquired_at date default sysdate not null
);

create table library_rule (
  id number(19) primary key,
  rule_code varchar2(50) not null unique,
  rule_value varchar2(200) not null,
  description varchar2(500),
  updated_at timestamp default current_timestamp not null
);

create table borrow_record (
  id number(19) primary key,
  reader_id number(19) not null references reader(id),
  book_copy_id number(19) not null references book_copy(id),
  borrowed_at timestamp default current_timestamp not null,
  due_at timestamp not null,
  returned_at timestamp,
  renew_count number(3) default 0 not null,
  status varchar2(20) default 'BORROWED' not null
    check (status in ('BORROWED', 'RETURNED', 'OVERDUE', 'LOST')),
  handled_by number(19) references sys_user(id)
);

create table fine_record (
  id number(19) primary key,
  borrow_record_id number(19) not null unique references borrow_record(id),
  amount number(10,2) default 0 not null,
  paid_amount number(10,2) default 0 not null,
  status varchar2(20) default 'UNPAID' not null check (status in ('UNPAID', 'PARTIAL', 'PAID', 'WAIVED')),
  paid_at timestamp,
  created_at timestamp default current_timestamp not null
);

create table operation_log (
  id number(19) primary key,
  user_id number(19) references sys_user(id),
  action varchar2(80) not null,
  target_type varchar2(80),
  target_id varchar2(80),
  detail varchar2(1000),
  ip_address varchar2(64),
  created_at timestamp default current_timestamp not null
);

create sequence seq_sys_role start with 100 increment by 1;
create sequence seq_sys_user start with 100 increment by 1;
create sequence seq_reader start with 100 increment by 1;
create sequence seq_book_category start with 100 increment by 1;
create sequence seq_book start with 100 increment by 1;
create sequence seq_book_copy start with 100 increment by 1;
create sequence seq_library_rule start with 100 increment by 1;
create sequence seq_borrow_record start with 100 increment by 1;
create sequence seq_fine_record start with 100 increment by 1;
create sequence seq_operation_log start with 100 increment by 1;

create index idx_book_title on book(title);
create index idx_book_author on book(author);
create index idx_book_copy_book on book_copy(book_id);
create index idx_borrow_reader_status on borrow_record(reader_id, status);
create index idx_borrow_copy_status on borrow_record(book_copy_id, status);
create index idx_operation_log_created on operation_log(created_at);

commit;
exit

