drop table assignment;
drop table qinfo;

create table assignment (assignment_id SERIAL,  start_time timestamp, end_time timestamp, status varchar(6), primary key(assignment_id));

--Initialize status with 'NC'

create table qinfo (assignment_id int, q_id int, query varchar(500), english_desc varchar(500), primary key(assignment_id,q_id));
