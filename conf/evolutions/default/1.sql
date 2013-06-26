# --- !Ups

create table job (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  datum                     datetime,
  constraint pk_job primary key (id))
;

create table job_description (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  constraint pk_job_description primary key (id))
;

create table job_output (
  id                        bigint auto_increment not null,
  job_description_id        bigint not null,
  filename                  varchar(255),
  constraint pk_job_output primary key (id))
;

create table job_query (
  id                        bigint auto_increment not null,
  job_description_id        bigint not null,
  name                      varchar(255),
  terms                     varchar(255),
  constraint pk_job_query primary key (id))
;


create table job_query_job_output (
  job_query_id                   bigint not null,
  job_output_id                  bigint not null,
  constraint pk_job_query_job_output primary key (job_query_id, job_output_id))
;
alter table job_output add constraint fk_job_output_job_description_1 foreign key (job_description_id) references job_description (id) on delete restrict on update restrict;
create index ix_job_output_job_description_1 on job_output (job_description_id);
alter table job_query add constraint fk_job_query_job_description_2 foreign key (job_description_id) references job_description (id) on delete restrict on update restrict;
create index ix_job_query_job_description_2 on job_query (job_description_id);



alter table job_query_job_output add constraint fk_job_query_job_output_job_query_01 foreign key (job_query_id) references job_query (id) on delete restrict on update restrict;

alter table job_query_job_output add constraint fk_job_query_job_output_job_output_02 foreign key (job_output_id) references job_output (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table job;

drop table job_description;

drop table job_output;

drop table job_query_job_output;

drop table job_query;

SET FOREIGN_KEY_CHECKS=1;

