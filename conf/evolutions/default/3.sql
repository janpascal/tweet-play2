# --- !Ups

ALTER TABLE `tweet` ADD COLUMN `from_user_id` bigint(20);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `tweet` DROP COLUMN `from_user_id`;

SET FOREIGN_KEY_CHECKS=1;

