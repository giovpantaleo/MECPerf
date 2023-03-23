-- MySQL dump 10.13  Distrib 5.7.29, for Linux (x86_64)
--
-- Host: localhost    Database: MECPerf
-- ------------------------------------------------------
-- Server version	5.7.29-0ubuntu0.18.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `BandwidthMeasure`
--

DROP TABLE IF EXISTS `BandwidthMeasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BandwidthMeasure` (
  `id` int(11) NOT NULL,
  `sub_id` int(11) NOT NULL,
  `nanoTimes` bigint(22) unsigned DEFAULT NULL,
  `kBytes` decimal(10,4) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`,`sub_id`),
  CONSTRAINT `fk_Test_BandwidthMeasure_id` FOREIGN KEY (`id`) REFERENCES `Test` (`ID`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PassiveBandwidthMeasure`
--

DROP TABLE IF EXISTS `PassiveBandwidthMeasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PassiveBandwidthMeasure` (
  `ID` int(11) NOT NULL,
  `Timestamp` bigint(22) NOT NULL,
  `Bytes` int(11) NOT NULL,
  PRIMARY KEY (`ID`,`Timestamp`),
  CONSTRAINT `PassiveBandwidthMeasure_ibfk_1` FOREIGN KEY (`ID`) REFERENCES `PassiveTest` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PassiveRttMeasure`
--

DROP TABLE IF EXISTS `PassiveRttMeasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PassiveRttMeasure` (
  `ID` int(11) NOT NULL,
  `Timestamp` bigint(22) NOT NULL,
  `latency` double unsigned NOT NULL,
  PRIMARY KEY (`ID`,`Timestamp`),
  CONSTRAINT `PassiveRttMeasure_ibfk_1` FOREIGN KEY (`ID`) REFERENCES `PassiveTest` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PassiveTest`
--

DROP TABLE IF EXISTS `PassiveTest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PassiveTest` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Timestamp` datetime NOT NULL,
  `ClientIP` varchar(15) NOT NULL,
  `ClientPort` int(11) NOT NULL,
  `ServerIP` varchar(15) NOT NULL,
  `ServerPort` int(11) NOT NULL,
  `Keyword` varchar(512) DEFAULT NULL,
  `Direction` varchar(45) NOT NULL,
  `Protocol` varchar(45) NOT NULL,
  `Mode` varchar(45) NOT NULL,
  `Type` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=213 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RttMeasure`
--

DROP TABLE IF EXISTS `RttMeasure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RttMeasure` (
  `id` int(11) NOT NULL,
  `latency` double unsigned DEFAULT NULL,
  `sub_id` int(11) NOT NULL,
  PRIMARY KEY (`id`,`sub_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Test`
--

DROP TABLE IF EXISTS `Test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Test` (
  `TestNumber` int(11) NOT NULL,
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Timestamp` datetime NOT NULL,
  `Direction` varchar(45) NOT NULL,
  `Command` varchar(45) NOT NULL,
  `SenderIdentity` varchar(45) NOT NULL,
  `ReceiverIdentity` varchar(45) NOT NULL,
  `SenderIPv4Address` varchar(15) NOT NULL,
  `ReceiverIPv4Address` varchar(15) NOT NULL,
  `Keyword` varchar(512) DEFAULT NULL,
  `PackSize` int(11) NOT NULL,
  `NumPack` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=60389 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-02-29 14:17:36
