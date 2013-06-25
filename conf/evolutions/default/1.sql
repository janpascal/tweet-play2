# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table job (
  id                        bigint not null,
  name                      varchar(255),
  datum                     timestamp,
  constraint pk_job primary key (id))
;

create table job_description (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_job_description primary key (id))
;

create table job_output (
  id                        bigint not null,
  job_description_id        bigint not null,
  filename                  varchar(255),
  constraint pk_job_output primary key (id))
;

create table job_query (
  id                        bigint not null,
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
create sequence job_seq;

create sequence job_description_seq;

create sequence job_output_seq;

create sequence job_query_seq;

alter table job_output add constraint fk_job_output_job_description_1 foreign key (job_description_id) references job_description (id) on delete restrict on update restrict;
create index ix_job_output_job_description_1 on job_output (job_description_id);
alter table job_query add constraint fk_job_query_job_description_2 foreign key (job_description_id) references job_description (id) on delete restrict on update restrict;
create index ix_job_query_job_description_2 on job_query (job_description_id);



alter table job_query_job_output add constraint fk_job_query_job_output_job_q_01 foreign key (job_query_id) references job_query (id) on delete restrict on update restrict;

alter table job_query_job_output add constraint fk_job_query_job_output_job_o_02 foreign key (job_output_id) references job_output (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists job;

drop table if exists job_description;

drop table if exists job_output;

drop table if exists job_query_job_output;

drop table if exists job_query;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists job_seq;

drop sequence if exists job_description_seq;

drop sequence if exists job_output_seq;

drop sequence if exists job_query_seq;

