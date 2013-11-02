# --- !Ups

alter table tweet convert to character set utf8mb4;
alter table job convert to character set utf8mb4;
alter table stream_config convert to character set utf8mb4;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

SET FOREIGN_KEY_CHECKS=1;

