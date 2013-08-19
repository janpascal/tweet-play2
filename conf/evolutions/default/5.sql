# --- !Ups

ALTER TABLE `tweet` ADD COLUMN `conforms_to_terms` boolean;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `tweet` DROP COLUMN `conforms_to_terms`;

SET FOREIGN_KEY_CHECKS=1;

