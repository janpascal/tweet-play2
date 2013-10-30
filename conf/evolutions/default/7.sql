# --- !Ups

ALTER TABLE `job` ADD COLUMN `status` integer;
UPDATE job SET status=4 WHERE finished!=0;
ALTER TABLE `job` DROP COLUMN `finished`;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `job` ADD COLUMN `finished` boolean;
UPDATE job SET finished=1 WHERE status=4;
ALTER TABLE `job` DROP COLUMN `status`;

SET FOREIGN_KEY_CHECKS=1;

