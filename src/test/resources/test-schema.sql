CREATE SCHEMA IF NOT EXISTS `orbroker`;
USE `orbroker`;

DROP TABLE IF EXISTS `company`;
CREATE TABLE  `company` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `company_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `employee`;
CREATE TABLE  `employee` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `birth` date NOT NULL,
  `active` tinyint(1) NOT NULL,
  `salary` int(11) NOT NULL,
  `currency` char(1) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `company_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `unit`;
CREATE TABLE  `unit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `employee_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `company` VALUES (1,'SampleLab','via burti, 12 - Milano'), (2,'Zikzak','via dei pazzi, 10 - Milano');
INSERT INTO `employee` VALUES (1,'John Doe','jdoe@samplelab.com', FROM_UNIXTIME(294080400), 1,15000,'€',1,2), (2,'Super Manager','aaa.bbb@samplelab.com', FROM_UNIXTIME(294080400),0,20000,'€',0,1);
INSERT INTO `unit` VALUES (1,'Research and Development',2);
