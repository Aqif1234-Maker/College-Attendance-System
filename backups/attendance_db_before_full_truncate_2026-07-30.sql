-- MySQL dump 10.13  Distrib 9.3.0, for Win64 (x86_64)
--
-- Host: localhost    Database: attendance_db
-- ------------------------------------------------------
-- Server version	9.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `academic_years`
--

DROP TABLE IF EXISTS `academic_years`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_years` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(20) NOT NULL,
  `is_current` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `label` (`label`),
  UNIQUE KEY `ux_academic_years_current` (((case when `is_current` then 1 else NULL end)))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `academic_years`
--

LOCK TABLES `academic_years` WRITE;
/*!40000 ALTER TABLE `academic_years` DISABLE KEYS */;
INSERT INTO `academic_years` VALUES (3,'2026-2027',1,NULL);
/*!40000 ALTER TABLE `academic_years` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance_records`
--

DROP TABLE IF EXISTS `attendance_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `status` enum('PRESENT','ABSENT') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_ar` (`session_id`,`student_id`),
  KEY `ix_records_student` (`student_id`),
  CONSTRAINT `fk_ar_session` FOREIGN KEY (`session_id`) REFERENCES `attendance_sessions` (`id`),
  CONSTRAINT `fk_ar_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance_records`
--

LOCK TABLES `attendance_records` WRITE;
/*!40000 ALTER TABLE `attendance_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance_session_slot_locks`
--

DROP TABLE IF EXISTS `attendance_session_slot_locks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_session_slot_locks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `session_date` date NOT NULL,
  `period_unit` tinyint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_assl` (`class_id`,`session_date`,`period_unit`),
  KEY `fk_assl_session` (`session_id`),
  CONSTRAINT `fk_assl_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`),
  CONSTRAINT `fk_assl_session` FOREIGN KEY (`session_id`) REFERENCES `attendance_sessions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance_session_slot_locks`
--

LOCK TABLES `attendance_session_slot_locks` WRITE;
/*!40000 ALTER TABLE `attendance_session_slot_locks` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance_session_slot_locks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance_sessions`
--

DROP TABLE IF EXISTS `attendance_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  `batch_id` bigint NOT NULL,
  `session_date` date NOT NULL,
  `slot` int NOT NULL,
  `locked_by` bigint NOT NULL,
  `locked_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `submitted` tinyint(1) NOT NULL DEFAULT '0',
  `submitted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_session` (`class_id`,`subject_id`,`batch_id`,`session_date`,`slot`),
  KEY `fk_as_subject` (`subject_id`),
  KEY `fk_as_batch` (`batch_id`),
  KEY `fk_as_teacher` (`locked_by`),
  KEY `ix_sessions_lookup` (`class_id`,`subject_id`,`batch_id`,`session_date`),
  CONSTRAINT `fk_as_batch` FOREIGN KEY (`batch_id`) REFERENCES `batches` (`id`),
  CONSTRAINT `fk_as_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`),
  CONSTRAINT `fk_as_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `fk_as_teacher` FOREIGN KEY (`locked_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance_sessions`
--

LOCK TABLES `attendance_sessions` WRITE;
/*!40000 ALTER TABLE `attendance_sessions` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `batches`
--

DROP TABLE IF EXISTS `batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_id` bigint NOT NULL,
  `label` varchar(20) NOT NULL,
  `is_whole_class` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_batches_label` (`subject_id`,`label`),
  CONSTRAINT `fk_batches_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batches`
--

LOCK TABLES `batches` WRITE;
/*!40000 ALTER TABLE `batches` DISABLE KEYS */;
INSERT INTO `batches` VALUES (31,19,'B1',0),(32,19,'B2',0),(33,19,'B3',0),(34,19,'B4',0),(35,14,'Entire Class',1),(36,16,'Entire Class',1),(37,21,'B1',0),(38,21,'B2',0),(39,21,'B3',0),(40,21,'B4',0),(41,23,'B1',0),(42,23,'B2',0),(43,23,'B3',0),(44,23,'B4',0),(45,24,'B1',0),(46,24,'B2',0),(47,24,'B3',0),(48,24,'B4',0),(49,17,'Entire Class',1),(50,22,'B1',0),(51,22,'B2',0),(52,22,'B3',0),(53,22,'B4',0),(54,18,'Entire Class',1),(55,13,'Entire Class',1),(56,15,'Entire Class',1),(57,20,'B1',0),(58,20,'B2',0),(59,20,'B3',0),(60,20,'B4',0);
/*!40000 ALTER TABLE `batches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_coordinators`
--

DROP TABLE IF EXISTS `class_coordinators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_coordinators` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `assigned_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `class_id` (`class_id`),
  KEY `fk_cc_user` (`user_id`),
  CONSTRAINT `fk_cc_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`),
  CONSTRAINT `fk_cc_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_coordinators`
--

LOCK TABLES `class_coordinators` WRITE;
/*!40000 ALTER TABLE `class_coordinators` DISABLE KEYS */;
INSERT INTO `class_coordinators` VALUES (3,3,4,'2026-07-30 01:40:26'),(4,4,5,'2026-07-30 01:40:26');
/*!40000 ALTER TABLE `class_coordinators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classes`
--

DROP TABLE IF EXISTS `classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_year_id` bigint NOT NULL,
  `name` varchar(50) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_classes_name_year` (`academic_year_id`,`name`),
  CONSTRAINT `fk_classes_year` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classes`
--

LOCK TABLES `classes` WRITE;
/*!40000 ALTER TABLE `classes` DISABLE KEYS */;
INSERT INTO `classes` VALUES (3,3,'TE-A','2026-07-30 01:40:26'),(4,3,'TE-B','2026-07-30 01:40:26');
/*!40000 ALTER TABLE `classes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','init','SQL','V1__init.sql',-1228907016,'root','2026-07-27 11:20:59',418,1),(2,'2','fix attendance session slot type','SQL','V2__fix_attendance_session_slot_type.sql',-431668369,'root','2026-07-27 11:22:00',79,1),(3,'3','add attendance slot locks','SQL','V3__add_attendance_slot_locks.sql',-1710154982,'root','2026-07-29 17:21:42',104,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `roll_no` varchar(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `batch_label` varchar(20) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_students_roll` (`class_id`,`roll_no`),
  CONSTRAINT `fk_students_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (21,4,'B-501','DADHEECH ARYAN AJAY','B1',1),(22,4,'B-502','JAIN HANSHIKA VIKAS','B1',1),(23,4,'B-503','KOLAPATE VEDANG AJAY ','B1',1),(24,4,'B-504','KOTHARI SALONI NARENDRA','B1',1),(25,4,'B-505','NARKAR MANASVI MANGESH','B1',1),(26,4,'B-506','NARVEL YUMNA MASROOR','B2',1),(27,4,'B-507','NATHBANJAN MAHESH ARUN','B2',1),(28,4,'B-508','NAVLE SIDDHESH DATTATRAY','B2',1),(29,4,'B-509','PADALA RAKSHITH MAHESH','B2',1),(30,4,'B-510','PADYAR AMEY SANJAY','B2',1),(31,4,'B-511','PALAN ADITYA JAYA','B3',1),(32,4,'B-512','PANCHAL MAHEK DEEPAK','B3',1),(33,4,'B-513','PANDA ADITYARAJ PRAFULLA','B3',1),(34,4,'B-514','PANDEY AVISHA  RAJENDRA ','B3',1),(35,4,'B-515','PATEL KABIR MINESH','B3',1),(36,4,'B-516','PATEL UNNMIL RAJEEV ','B4',1),(37,4,'B-517','PATIL ANUSHKA JAGDISH','B4',1),(38,4,'B-518','PATIL CHAITRA YOGESH','B4',1),(39,4,'B-519','PATIL PRATHAM SUNIL','B4',1),(40,4,'B-520','PATIL RITESH MANOJ','B4',1);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subjects`
--

DROP TABLE IF EXISTS `subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subjects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `name` varchar(100) NOT NULL,
  `type` enum('TH','PR') NOT NULL,
  `created_by` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_subjects_name_class` (`class_id`,`name`),
  KEY `fk_subjects_creator` (`created_by`),
  CONSTRAINT `fk_subjects_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`),
  CONSTRAINT `fk_subjects_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subjects`
--

LOCK TABLES `subjects` WRITE;
/*!40000 ALTER TABLE `subjects` DISABLE KEYS */;
INSERT INTO `subjects` VALUES (13,4,'Software Engineering and Agile Practices','TH',5),(14,4,'Artificial Intelligence and Machine Learning','TH',5),(15,4,'Web Technology','TH',5),(16,4,'Cyber Security','TH',5),(17,4,'Indian Knowledge System','TH',5),(18,4,'OE','TH',5),(19,4,'AI & ML Lab','PR',5),(20,4,'Web Technology Lab','PR',5),(21,4,'Cyber Security lab','PR',5),(22,4,'MDM Lab','PR',5),(23,4,'DevOps Lab','PR',5),(24,4,'IKS Lab','PR',5);
/*!40000 ALTER TABLE `subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_assignments`
--

DROP TABLE IF EXISTS `teacher_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  `batch_id` bigint NOT NULL,
  `assigned_by` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_ta` (`teacher_id`,`class_id`,`subject_id`,`batch_id`),
  KEY `fk_ta_class` (`class_id`),
  KEY `fk_ta_subject` (`subject_id`),
  KEY `fk_ta_batch` (`batch_id`),
  KEY `fk_ta_assigner` (`assigned_by`),
  CONSTRAINT `fk_ta_assigner` FOREIGN KEY (`assigned_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_ta_batch` FOREIGN KEY (`batch_id`) REFERENCES `batches` (`id`),
  CONSTRAINT `fk_ta_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`),
  CONSTRAINT `fk_ta_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `fk_ta_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_assignments`
--

LOCK TABLES `teacher_assignments` WRITE;
/*!40000 ALTER TABLE `teacher_assignments` DISABLE KEYS */;
INSERT INTO `teacher_assignments` VALUES (9,10,4,15,56,5,'2026-07-30 01:40:27'),(10,3,4,13,55,5,'2026-07-30 01:40:27'),(11,12,4,14,35,5,'2026-07-30 01:40:27'),(12,13,4,17,49,5,'2026-07-30 01:40:27'),(13,11,4,20,57,5,'2026-07-30 01:40:27'),(14,10,4,20,58,5,'2026-07-30 01:40:27'),(15,11,4,20,59,5,'2026-07-30 01:40:27'),(16,9,4,20,60,5,'2026-07-30 01:40:27');
/*!40000 ALTER TABLE `teacher_assignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `role` enum('ADMIN','CLASS_COORDINATOR','TEACHER') NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$10$WG7Qg5dZvx1F.xJv4qV/gu3XNcCa9qekDk1pjCSxBeSwkZnSqxDaW','System Administrator','ADMIN',1,NULL,NULL),(2,'Ankush','$2a$10$2A/cMB3yLA12LDe2Eplw1OW5hXor.Uc3eJKBaYf7fR/LuJK.oJIW6','Ankush Hutke','TEACHER',1,NULL,NULL),(3,'Renuka','$2a$10$UA17Tr9urPBU/UGs6a7CMOfZOSz1Iyozsdr4GUDNAM94i9sXe6Waq','Renuka Nagpure','TEACHER',1,NULL,NULL),(4,'Savita','$2a$10$kEcDS0BDiFz.kgucB.JeVuhUWnZ9F99eEWftiwVrSooswpnj4qNvi','Savita Sawant','CLASS_COORDINATOR',1,NULL,NULL),(5,'Sandeep','$2a$10$.k8/fS/RhqqspUYbEGxFFOdl8J0/dOlVMlS0y5KxXH7ht.7awL8FC','Sandeep Zade','CLASS_COORDINATOR',1,NULL,NULL),(6,'Dr. Sunil','$2a$10$0lKhD0QeN7XT3E9oU8yekuzV1AGcEyHvmQZWTfifjXYZ.JZSu/o0q','Dr. Sunil B. Wankhade','TEACHER',1,NULL,NULL),(7,'Prof Sanjana','$2a$10$Q3PjoCVuH45Whi4SJqGi9OjF6xjJJo6.oJ/yuAG.icVy5bZsfK2dO','Sanjana T. Satpute','TEACHER',1,NULL,NULL),(8,'Prof Mrunalinee','$2a$10$n7kIEDJ5cat.a/MRBMnj7uMcLPAwweZUHKLY.sJVYOrWDTRfjPlma','Mrunalinee V. Soliv','TEACHER',1,NULL,NULL),(9,'Prof Deepika','$2a$10$ROW.MT5u0D0mzsKHucmncOlRV9gxRCnhCmu/QXcYawEHHK7PlEefC','Deepika S. Sonawane','TEACHER',1,NULL,NULL),(10,'Prof Divya','$2a$10$MNTU4sIgidDinbZ.obkFUOIQs5Tsq7Qedy2WlhuVRvaLHJ.ZPiHR6','Divya V. Gajangi','TEACHER',1,NULL,NULL),(11,'Prof Rahul','$2a$10$4ZCCPTgCkbMtz1Se.SsA0uHDhNZTgMUvZ3S40P6GtdyPRit7LVijO','Rahul L. Jiwane','TEACHER',1,NULL,NULL),(12,'Dr. Swati','$2a$10$Rxel1Km9wbztoMX8eiriXu8fYXI1Gu6ojIOuzozeAQxGLZqjS/dWi','Dr. Swati V. Narvane','TEACHER',1,NULL,NULL),(13,'Prof Nilesh','$2a$10$1fEznm9i0WsL.Rvo7B4OqO50UvlsW8MQ3umAfA0xEtkVvIjWlbYhK','Nilesh Shimpi','TEACHER',1,NULL,NULL),(14,'Prof Mrunal','$2a$10$e1JU2t6trRuWTpzrE.TqbughlZisb2HJNVrGD7P.yX9NlxQsCrNIG','Mrunal Marathe','TEACHER',1,NULL,NULL),(15,'Prof Kalpana','$2a$10$kaXpLW0ZSua1DqlVYXFgXuulJ18BWL75KakWTX7I8jzzZ3AXLggza','Kalpana Mane','TEACHER',1,NULL,NULL),(16,'Prof Trishala','$2a$10$BDzkFthrHJVb89GxwVshru3BB/PZoqmtYetvuzR3rewz2VWB1G4vq','Trishala Dixit','TEACHER',1,NULL,NULL),(17,'Faculty','$2a$10$Xq3cwO5VTpESzg4pu.Wgyuh048kcC6AYJSBuuSy.uYId0nZ45mZ8W','Faculty 2','TEACHER',1,NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-30  7:17:08
