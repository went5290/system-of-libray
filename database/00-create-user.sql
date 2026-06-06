whenever sqlerror exit sql.sqlcode

alter session set container = LJW4;

declare
  user_count number;
begin
  select count(*) into user_count from dba_users where username = 'LIBRARY_APP';
  if user_count = 0 then
    execute immediate 'create user LIBRARY_APP identified by "' || replace('&1', '"', '""') || '" default tablespace LJW_DATA';
  end if;
end;
/

grant create session, create table, create sequence, create trigger, create view, create procedure to LIBRARY_APP;
alter user LIBRARY_APP default tablespace LJW_DATA;
alter user LIBRARY_APP quota unlimited on LJW_DATA;

exit
