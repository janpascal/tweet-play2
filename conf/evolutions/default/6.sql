# --- !Ups

ALTER TABLE `job` ADD COLUMN `finished` boolean;
ALTER TABLE `job` ADD COLUMN `num_tweets` bigint;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `job` DROP COLUMN `finished`;
ALTER TABLE `job` DROP COLUMN `num_tweets`;

SET FOREIGN_KEY_CHECKS=1;

