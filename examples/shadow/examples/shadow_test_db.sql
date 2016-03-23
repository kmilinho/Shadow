CREATE DATABASE  IF NOT EXISTS `shadow_test_db` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `shadow_test_db`;
-- MySQL dump 10.13  Distrib 5.7.9, for linux-glibc2.5 (x86_64)
--
-- Host: localhost    Database: shadow_test_db
-- ------------------------------------------------------
-- Server version	5.5.47-0ubuntu0.14.04.1

DROP TABLE IF EXISTS `dog_table`;

CREATE TABLE `dog_table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `column_name` varchar(45) DEFAULT NULL,
  `column_age` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

