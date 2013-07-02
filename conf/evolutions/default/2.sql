# --- !Ups


CREATE TABLE `tweet` (
  `id` bigint(20) NOT NULL,
  `date` datetime DEFAULT NULL,
  `from_user` varchar(255) DEFAULT NULL,
  `in_reply_to` varchar(255) DEFAULT NULL,
  `text` text,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_tweet_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table tweet;

SET FOREIGN_KEY_CHECKS=1;

