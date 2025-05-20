drop table if exists T_REGISTER_REQUEST;
create memory table T_REGISTER_REQUEST (USERNAME_C varchar(100) not null, PASSWORD_C varchar(100) not null, EMAIL_C varchar(100) not null, REQUEST_DATE_D datetime not null, STATUS_C varchar(50) not null, primary key (USERNAME_C) );
create memory table T_FILE_PATH (ID_C varchar(100) not null, PATH_C varchar(1000) not null, primary key (ID_C) );