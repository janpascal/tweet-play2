# --- !Ups

ALTER TABLE `job` ADD COLUMN `seconds_to_wait` integer;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `job` DROP COLUMN `seconds_to_wait`;

SET FOREIGN_KEY_CHECKS=1;

