DROP TABLE IF EXISTS cra;
DROP TABLE IF EXISTS user;

create table user
(
    id       bigint auto_increment
        primary key,
    email    varchar(255) null,
    password varchar(255) null,
    role     varchar(255) null
);

create table cra
(
    user_id  bigint       not null,
    project  varchar(255) not null,
    cra_date date         not null,
    value    float       null,
    primary key (user_id, project, cra_date),
    constraint fk_user foreign key (user_id) references user (id)
);

insert into user (id, email, password, role)
values (1, 'default@gmail.com', '$2a$12$AGDUP7m0Wu.tGW2ALEXbW.RPNbXe0r86HJwXNBeDe/xdfogik5V2G', 'Default'),
       (2, 'admin@gmail.com', '$2a$12$AGDUP7m0Wu.tGW2ALEXbW.RPNbXe0r86HJwXNBeDe/xdfogik5V2G', 'Admin');

insert into cra (user_id, project, cra_date, value)
values  (1, 'toto', '2023-06-22', 1);