drop table if exists movies;
create table movies (
    id bigserial not null primary key,
    name text not null,
    director text not null,
    duration_seconds int not null
);
update movies set name = 'updated' where id=1;
insert into movies (name, director, duration_seconds)
values ('movie 1', 'director 1', 3600),
       ('movie 2', 'director 2', 2800);