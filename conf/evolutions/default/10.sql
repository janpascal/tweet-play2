# --- !Ups

alter table tweet add index date_conform (`conforms_to_terms`,`date`);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table tweet drop index date_conform;

SET FOREIGN_KEY_CHECKS=1;

