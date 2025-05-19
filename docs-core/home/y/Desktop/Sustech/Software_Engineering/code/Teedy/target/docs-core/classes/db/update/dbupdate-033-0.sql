drop table if exists T_REGISTER_REQUEST;
create memory table if not exists T_REGISTER_REQUEST (URR_ID_C varchar(36) not null, USERNAME_C varchar(100) not null, PASSWORD_C varchar(100) not null, EMAIL_C varchar(100) not null, REQUEST_DATE_D datetime not null, STATUS_C varchar(50) not null, primary key (URR_ID_C) );
