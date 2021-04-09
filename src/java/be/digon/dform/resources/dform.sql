-- MySQL dump 10.17  Distrib 10.3.25-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: dformtest
-- ------------------------------------------------------
-- Server version	10.3.25-MariaDB-0ubuntu0.20.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `forms`
--

DROP TABLE IF EXISTS `forms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forms` (
  `uuid` char(40) NOT NULL DEFAULT '',
  `created_by` int(11) DEFAULT NULL,
  `last_cng` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created` datetime DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `active` int(11) NOT NULL DEFAULT 1,
  `formsource` blob DEFAULT NULL,
  `row_id` int(11) NOT NULL AUTO_INCREMENT,
  `formname` varchar(255) DEFAULT NULL,
  `formversion` int(11) DEFAULT NULL,
  `is_registrationform` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `row_id` (`row_id`),
  KEY `formname` (`formname`),
  KEY `regform` (`active`,`is_registrationform`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `outputtransformations`
--

DROP TABLE IF EXISTS `outputtransformations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `outputtransformations` (
  `uuid` char(40) NOT NULL,
  `last_cng` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created` datetime DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `xslt` longblob DEFAULT NULL,
  `row_id` int(11) NOT NULL AUTO_INCREMENT,
  `outputtype` char(10) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `row_id` (`row_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `parameters`
--

DROP TABLE IF EXISTS `parameters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `parameters` (
  `mp_hash` varbinary(100) DEFAULT NULL,
  `mp_salt` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patients` (
  `uuid` char(40) NOT NULL DEFAULT '',
  `name` varchar(100) DEFAULT NULL,
  `street` varchar(100) DEFAULT NULL,
  `zip` varchar(10) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `row_id` int(11) NOT NULL AUTO_INCREMENT,
  `version` int(11) DEFAULT NULL,
  `segment` varchar(100) DEFAULT NULL,
  `alfacode` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `row_id` (`row_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reports` (
  `layout` blob DEFAULT NULL,
  `title` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `row_id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL DEFAULT '',
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `row_id` (`row_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `submissions`
--

DROP TABLE IF EXISTS `submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `submissions` (
  `created_by` int(11) DEFAULT NULL,
  `formdata` blob DEFAULT NULL,
  `uuid` char(40) NOT NULL DEFAULT '',
  `instance` varchar(255) DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT 0,
  `last_cng` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created` datetime DEFAULT NULL,
  `status` enum('edit','complete') DEFAULT 'edit',
  `subject_type` char(10) DEFAULT 'patient',
  `row_id` int(11) NOT NULL AUTO_INCREMENT,
  `subject_id` int(11) DEFAULT NULL,
  `formsource_rowid` int(11) NOT NULL,
  `formname` varchar(255) DEFAULT NULL,
  `formversion` int(11) DEFAULT NULL,
  `formdata_aes` blob DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `row_id` (`row_id`),
  KEY `form_sub_in_stat` (`formname`,`subject_id`,`instance`,`status`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `useraccess`
--

DROP TABLE IF EXISTS `useraccess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `useraccess` (
  `user_id` int(11) DEFAULT NULL,
  `subject_id` int(11) DEFAULT NULL,
  `rights` char(2) DEFAULT NULL,
  `row_id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`row_id`),
  UNIQUE KEY `user_subject` (`user_id`,`subject_id`,`rights`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usergroups`
--

DROP TABLE IF EXISTS `usergroups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usergroups` (
  `group_id` varchar(64) DEFAULT NULL,
  `row_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_login` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`row_id`),
  KEY `login` (`user_login`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `uuid` char(40) NOT NULL DEFAULT '',
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `street` varchar(100) DEFAULT NULL,
  `zip` varchar(10) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `login` varchar(20) DEFAULT NULL,
  `passwd_hash` varchar(64) DEFAULT NULL,
  `row_id` int(11) NOT NULL AUTO_INCREMENT,
  `affiliation` varchar(100) DEFAULT NULL,
  `segment` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `row_id` (`row_id`),
  UNIQUE KEY `login` (`login`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-04-09 15:00:47
