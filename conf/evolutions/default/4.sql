# --- !Ups

create table stream_config (
  id     bigint auto_increment not null,
  terms  text,
  constraint pk_stream_config primary key (id)
);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

DROP table stream_config;

SET FOREIGN_KEY_CHECKS=1;

