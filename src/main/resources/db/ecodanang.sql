-- MySQL dump 10.13  Distrib 8.0.42, for macos15 (x86_64)
--
-- Host: localhost    Database: ecodana
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `AccountDeletionLogs`
--

DROP TABLE IF EXISTS `AccountDeletionLogs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `AccountDeletionLogs` (
  `LogId` char(36) NOT NULL,
  `UserId` char(36) NOT NULL,
  `DeletionReason` varchar(255) NOT NULL,
  `AdditionalComments` text,
  `Timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`LogId`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `accountdeletionlogs_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BatteryLogs`
--

DROP TABLE IF EXISTS `BatteryLogs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BatteryLogs` (
  `LogId` char(36) NOT NULL,
  `VehicleId` char(36) NOT NULL,
  `BookingId` char(36) DEFAULT NULL,
  `BatteryLevel` decimal(5,2) NOT NULL,
  `CheckTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`LogId`),
  KEY `VehicleId` (`VehicleId`),
  KEY `BookingId` (`BookingId`),
  CONSTRAINT `batterylogs_ibfk_1` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`) ON DELETE CASCADE,
  CONSTRAINT `batterylogs_ibfk_2` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Booking`
--

DROP TABLE IF EXISTS `Booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Booking` (
  `BookingId` char(36) NOT NULL DEFAULT (uuid()),
  `UserId` char(36) NOT NULL,
  `VehicleId` char(36) NOT NULL,
  `HandledBy` char(36) DEFAULT NULL,
  `PickupDateTime` datetime NOT NULL,
  `ReturnDateTime` datetime NOT NULL,
  `TotalAmount` decimal(10,2) NOT NULL,
  `Status` varchar(50) NOT NULL DEFAULT 'Pending',
  `DiscountId` char(36) DEFAULT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CancelReason` varchar(500) DEFAULT NULL,
  `BookingCode` varchar(20) NOT NULL,
  `ExpectedPaymentMethod` varchar(50) DEFAULT NULL,
  `RentalType` varchar(10) NOT NULL DEFAULT 'daily',
  `CustomerName` varchar(100) DEFAULT NULL,
  `CustomerPhone` varchar(15) DEFAULT NULL,
  `CustomerAddress` varchar(255) DEFAULT NULL,
  `CustomerEmail` varchar(100) DEFAULT NULL,
  -- ĐÃ LOẠI BỎ: DriverLicenseImageUrl varchar(500) DEFAULT NULL,
  `TermsAgreed` tinyint(1) NOT NULL DEFAULT '0',
  `TermsAgreedAt` datetime DEFAULT NULL,
  `TermsVersion` varchar(10) DEFAULT 'v1.0',
  PRIMARY KEY (`BookingId`),
  UNIQUE KEY `BookingCode` (`BookingCode`),
  KEY `UserId` (`UserId`),
  KEY `VehicleId` (`VehicleId`),
  KEY `HandledBy` (`HandledBy`),
  KEY `DiscountId` (`DiscountId`),
  KEY `idx_booking_code` (`BookingCode`),
  KEY `idx_booking_dates` (`PickupDateTime`, `ReturnDateTime`), -- THÊM INDEX MỚI
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`),
  CONSTRAINT `booking_ibfk_3` FOREIGN KEY (`HandledBy`) REFERENCES `Users` (`UserId`),
  CONSTRAINT `booking_ibfk_4` FOREIGN KEY (`DiscountId`) REFERENCES `Discount` (`DiscountId`) ON DELETE SET NULL,
  CONSTRAINT `CHK_Booking_Amount` CHECK ((`TotalAmount` >= 0)),
  CONSTRAINT `CHK_Booking_RentalType` CHECK ((`RentalType` in (_utf8mb4'hourly',_utf8mb4'daily',_utf8mb4'monthly')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
-- Table structure for table `BookingApproval`
--

DROP TABLE IF EXISTS `BookingApproval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BookingApproval` (
  `ApprovalId` char(36) NOT NULL,
  `BookingId` char(36) NOT NULL,
  `StaffId` char(36) NOT NULL,
  `ApprovalStatus` varchar(20) NOT NULL,
  `ApprovalDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Note` varchar(500) DEFAULT NULL,
  `RejectionReason` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`ApprovalId`),
  KEY `BookingId` (`BookingId`),
  KEY `StaffId` (`StaffId`),
  CONSTRAINT `bookingapproval_ibfk_1` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`) ON DELETE CASCADE,
  CONSTRAINT `bookingapproval_ibfk_2` FOREIGN KEY (`StaffId`) REFERENCES `Users` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BookingInsurance`
--

DROP TABLE IF EXISTS `BookingInsurance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BookingInsurance` (
  `BookingInsuranceId` char(36) NOT NULL DEFAULT (uuid()),
  `BookingId` char(36) NOT NULL,
  `InsuranceId` char(36) NOT NULL,
  `PremiumAmount` decimal(10,2) NOT NULL,
  `RentalDays` decimal(10,2) NOT NULL,
  `VehicleSeats` int NOT NULL,
  `EstimatedVehicleValue` decimal(15,2) DEFAULT NULL,
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`BookingInsuranceId`),
  KEY `BookingId` (`BookingId`),
  KEY `InsuranceId` (`InsuranceId`),
  CONSTRAINT `bookinginsurance_ibfk_1` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`) ON DELETE CASCADE,
  CONSTRAINT `bookinginsurance_ibfk_2` FOREIGN KEY (`InsuranceId`) REFERENCES `Insurance` (`InsuranceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BookingSurcharges`
--

DROP TABLE IF EXISTS `BookingSurcharges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BookingSurcharges` (
  `SurchargeId` char(36) NOT NULL DEFAULT (uuid()),
  `BookingId` char(36) NOT NULL,
  `SurchargeType` varchar(50) NOT NULL,
  `Amount` decimal(10,2) NOT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `SurchargeCategory` varchar(50) DEFAULT NULL,
  `IsSystemGenerated` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`SurchargeId`),
  KEY `BookingId` (`BookingId`),
  CONSTRAINT `bookingsurcharges_ibfk_1` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`) ON DELETE CASCADE,
  CONSTRAINT `CHK_BookingSurcharges_Amount` CHECK ((`Amount` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CarBrand`
--

DROP TABLE IF EXISTS `CarBrand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `CarBrand` (
  `BrandId` char(36) NOT NULL,
  `BrandName` varchar(100) NOT NULL,
  PRIMARY KEY (`BrandId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CarFeature`
--

DROP TABLE IF EXISTS `CarFeature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `CarFeature` (
  `FeatureId` char(36) NOT NULL,
  `FeatureName` varchar(100) NOT NULL,
  PRIMARY KEY (`FeatureId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CitizenIdCards`
--

DROP TABLE IF EXISTS `CitizenIdCards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `CitizenIdCards` (
  `Id` char(36) NOT NULL,
  `UserId` char(36) NOT NULL,
  `CitizenIdNumber` varchar(12) NOT NULL,
  `FullName` varchar(100) NOT NULL,
  `DOB` date NOT NULL,
  `CitizenIdImageUrl` varchar(500) NOT NULL,
  `CitizenIdBackImageUrl` varchar(500) NOT NULL,
  `CitizenIdIssuedDate` date NOT NULL,
  `CitizenIdIssuedPlace` varchar(100) NOT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `UserId` (`UserId`),
  CONSTRAINT `citizenidcards_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Contract`
--

DROP TABLE IF EXISTS `Contract`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Contract` (
  `ContractId` char(36) NOT NULL,
  `ContractCode` varchar(30) NOT NULL,
  `UserId` char(36) NOT NULL,
  `BookingId` char(36) NOT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `SignedDate` datetime DEFAULT NULL,
  `CompletedDate` datetime DEFAULT NULL,
  `Status` varchar(20) NOT NULL DEFAULT 'Created',
  `TermsAccepted` tinyint(1) NOT NULL DEFAULT '0',
  `SignatureData` text,
  `SignatureMethod` varchar(20) DEFAULT NULL,
  `ContractPdfUrl` varchar(500) DEFAULT NULL,
  `ContractFileType` varchar(20) DEFAULT NULL,
  `Notes` varchar(500) DEFAULT NULL,
  `CancellationReason` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`ContractId`),
  UNIQUE KEY `ContractCode` (`ContractCode`),
  KEY `UserId` (`UserId`),
  KEY `BookingId` (`BookingId`),
  KEY `idx_contract_status` (`Status`), -- THÊM INDEX MỚI
  CONSTRAINT `contract_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `contract_ibfk_2` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
-- Table structure for table `ContractDocuments`
--

DROP TABLE IF EXISTS `ContractDocuments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ContractDocuments` (
  `DocumentId` char(36) NOT NULL,
  `ContractId` char(36) NOT NULL,
  `DriverLicenseImageUrl` varchar(500) DEFAULT NULL,
  `DriverLicenseNumber` varchar(50) DEFAULT NULL,
  `CitizenIdFrontImageUrl` varchar(500) DEFAULT NULL,
  `CitizenIdBackImageUrl` varchar(500) DEFAULT NULL,
  `CitizenIdNumber` varchar(50) DEFAULT NULL,
  `CitizenIdIssuedDate` date DEFAULT NULL,
  `CitizenIdIssuedPlace` varchar(100) DEFAULT NULL,
  `DriverLicenseImageHash` varchar(128) DEFAULT NULL,
  `CitizenIdFrontImageHash` varchar(128) DEFAULT NULL,
  `CitizenIdBackImageHash` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`DocumentId`),
  KEY `ContractId` (`ContractId`),
  CONSTRAINT `contractdocuments_ibfk_1` FOREIGN KEY (`ContractId`) REFERENCES `Contract` (`ContractId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Discount`
--

DROP TABLE IF EXISTS `Discount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Discount` (
  `DiscountId` char(36) NOT NULL,
  `DiscountName` varchar(100) NOT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `DiscountType` varchar(20) NOT NULL,
  `DiscountValue` decimal(10,2) NOT NULL,
  `StartDate` date NOT NULL,
  `EndDate` date NOT NULL,
  `IsActive` tinyint(1) NOT NULL,
  `CreatedDate` datetime NOT NULL,
  `VoucherCode` varchar(20) DEFAULT NULL,
  `MinOrderAmount` decimal(10,2) NOT NULL,
  `MaxDiscountAmount` decimal(10,2) DEFAULT NULL,
  `UsageLimit` int DEFAULT NULL,
  `UsedCount` int NOT NULL,
  `DiscountCategory` varchar(20) NOT NULL DEFAULT 'General',
  PRIMARY KEY (`DiscountId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DriverLicenses`
--

DROP TABLE IF EXISTS `DriverLicenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `DriverLicenses` (
  `LicenseId` char(36) NOT NULL,
  `UserId` char(36) NOT NULL,
  `LicenseNumber` varchar(50) DEFAULT NULL,
  `FullName` varchar(100) DEFAULT NULL,
  `DOB` date DEFAULT NULL,
  `LicenseImage` text,
  `CreatedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`LicenseId`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `driverlicenses_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EmailOTPVerification`
--

DROP TABLE IF EXISTS `EmailOTPVerification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `EmailOTPVerification` (
  `Id` char(36) NOT NULL,
  `OTP` varchar(255) NOT NULL,
  `ExpiryTime` datetime NOT NULL,
  `IsUsed` tinyint(1) NOT NULL,
  `UserId` char(36) NOT NULL,
  `CreatedAt` datetime NOT NULL,
  `ResendCount` int NOT NULL,
  `LastResendTime` datetime DEFAULT NULL,
  `ResendBlockUntil` datetime DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `emailotpverification_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FuelType`
--

DROP TABLE IF EXISTS `FuelType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FuelType` (
  `FuelTypeId` char(36) NOT NULL,
  `FuelName` varchar(100) NOT NULL,
  PRIMARY KEY (`FuelTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Insurance`
--

DROP TABLE IF EXISTS `Insurance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Insurance` (
  `InsuranceId` char(36) NOT NULL DEFAULT (uuid()),
  `InsuranceName` varchar(100) NOT NULL,
  `InsuranceType` varchar(50) NOT NULL,
  `BaseRatePerDay` decimal(10,2) NOT NULL,
  `PercentageRate` decimal(5,2) DEFAULT NULL,
  `CoverageAmount` decimal(15,2) NOT NULL,
  `ApplicableVehicleSeats` varchar(50) DEFAULT NULL,
  `Description` varchar(500) DEFAULT NULL,
  `IsActive` tinyint(1) NOT NULL DEFAULT '1',
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`InsuranceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Notification`
--

DROP TABLE IF EXISTS `Notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Notification` (
  `NotificationId` char(36) NOT NULL,
  `UserId` char(36) NOT NULL,
  `Message` text NOT NULL,
  `CreatedDate` datetime NOT NULL,
  `IsRead` tinyint(1) NOT NULL,
  PRIMARY KEY (`NotificationId`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PasswordResetTokens`
--

DROP TABLE IF EXISTS `PasswordResetTokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `PasswordResetTokens` (
  `Id` char(36) NOT NULL,
  `Token` varchar(255) NOT NULL,
  `ExpiryTime` datetime NOT NULL,
  `IsUsed` tinyint(1) NOT NULL DEFAULT '0',
  `UserId` char(36) NOT NULL,
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`Id`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `passwordresettokens_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Payment`
--

DROP TABLE IF EXISTS `Payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Payment` (
  `PaymentId` char(36) NOT NULL,
  `BookingId` char(36) NOT NULL,
  `ContractId` char(36) DEFAULT NULL,
  `Amount` decimal(10,2) NOT NULL,
  `PaymentMethod` varchar(50) NOT NULL,
  `PaymentStatus` varchar(20) NOT NULL DEFAULT 'Pending',
  `PaymentType` varchar(20) NOT NULL DEFAULT 'Deposit',
  `TransactionId` varchar(100) DEFAULT NULL,
  `PaymentDate` datetime DEFAULT NULL,
  `UserId` char(36) DEFAULT NULL,
  `Notes` varchar(500) DEFAULT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`PaymentId`),
  KEY `BookingId` (`BookingId`),
  KEY `ContractId` (`ContractId`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `payment_ibfk_1` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`) ON DELETE CASCADE,
  CONSTRAINT `payment_ibfk_2` FOREIGN KEY (`ContractId`) REFERENCES `Contract` (`ContractId`),
  CONSTRAINT `payment_ibfk_3` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Roles`
--

DROP TABLE IF EXISTS `Roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Roles` (
  `RoleId` char(36) NOT NULL,
  `RoleName` varchar(50) DEFAULT NULL,
  `NormalizedName` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`RoleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Terms`
--

DROP TABLE IF EXISTS `Terms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Terms` (
  `TermsId` char(36) NOT NULL DEFAULT (uuid()),
  `Version` varchar(10) NOT NULL,
  `Title` varchar(200) NOT NULL,
  `ShortContent` text,
  `FullContent` text NOT NULL,
  `EffectiveDate` date NOT NULL,
  `IsActive` tinyint(1) NOT NULL DEFAULT '1',
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`TermsId`),
  UNIQUE KEY `Version` (`Version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TransmissionType`
--

DROP TABLE IF EXISTS `TransmissionType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `TransmissionType` (
  `TransmissionTypeId` char(36) NOT NULL,
  `TransmissionName` varchar(100) NOT NULL,
  PRIMARY KEY (`TransmissionTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserFavoriteVehicles`
--

DROP TABLE IF EXISTS `UserFavoriteVehicles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserFavoriteVehicles` (
  `UserId` char(36) NOT NULL,
  `VehicleId` char(36) NOT NULL,
  PRIMARY KEY (`UserId`,`VehicleId`),
  KEY `VehicleId` (`VehicleId`),
  CONSTRAINT `userfavoritevehicles_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `userfavoritevehicles_ibfk_2` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserFeedback`
--

DROP TABLE IF EXISTS `UserFeedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserFeedback` (
  `FeedbackId` char(36) NOT NULL,
  `UserId` char(36) NOT NULL,
  `VehicleId` char(36) DEFAULT NULL,
  `BookingId` char(36) DEFAULT NULL,
  `Rating` int NOT NULL,
  `Content` varchar(4000) DEFAULT NULL,
  `Reviewed` date NOT NULL,
  `CreatedDate` datetime NOT NULL,
  `StaffReply` text,
  `ReplyDate` datetime DEFAULT NULL,
  PRIMARY KEY (`FeedbackId`),
  KEY `UserId` (`UserId`),
  KEY `VehicleId` (`VehicleId`),
  KEY `BookingId` (`BookingId`),
  CONSTRAINT `userfeedback_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `userfeedback_ibfk_2` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`) ON DELETE SET NULL,
  CONSTRAINT `userfeedback_ibfk_3` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`),
  CONSTRAINT `CHK_Rating_Range` CHECK ((`Rating` BETWEEN 1 AND 5)) -- THÊM RÀNG BUỘC MỚI
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `UserLogins`
--

DROP TABLE IF EXISTS `UserLogins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserLogins` (
  `LoginProvider` varchar(128) NOT NULL,
  `ProviderKey` varchar(128) NOT NULL,
  `ProviderDisplayName` text,
  `UserId` char(36) NOT NULL,
  PRIMARY KEY (`LoginProvider`,`ProviderKey`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `userlogins_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Users`
--

DROP TABLE IF EXISTS `Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Users` (
  `UserId` char(36) NOT NULL,
  `Username` varchar(100) NOT NULL,
  `UserDOB` date DEFAULT NULL,
  `PhoneNumber` varchar(11) DEFAULT NULL,
  `AvatarUrl` varchar(255) DEFAULT NULL,
  `Gender` varchar(10) DEFAULT NULL,
  `FirstName` varchar(256) DEFAULT NULL,
  `LastName` varchar(256) DEFAULT NULL,
  `Status` varchar(20) NOT NULL,
  `RoleId` char(36) NOT NULL,
  `CreatedDate` datetime DEFAULT NULL,
  `NormalizedUserName` varchar(256) DEFAULT NULL,
  `Email` varchar(100) NOT NULL,
  `NormalizedEmail` varchar(256) DEFAULT NULL,
  `EmailVerifed` tinyint(1) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `SecurityStamp` text,
  `ConcurrencyStamp` text,
  `TwoFactorEnabled` tinyint(1) NOT NULL,
  `LockoutEnd` datetime DEFAULT NULL,
  `LockoutEnabled` tinyint(1) NOT NULL,
  `AccessFailedCount` int NOT NULL,
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `Username` (`Username`),
  UNIQUE KEY `Email` (`Email`),
  KEY `RoleId` (`RoleId`),
  KEY `idx_user_email` (`Email`),
  KEY `idx_user_status` (`Status`), -- THÊM INDEX MỚI
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`RoleId`) REFERENCES `Roles` (`RoleId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `UserVoucherUsage`
--

DROP TABLE IF EXISTS `UserVoucherUsage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserVoucherUsage` (
  `UserId` char(36) NOT NULL,
  `DiscountId` char(36) NOT NULL,
  `UsedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserId`,`DiscountId`),
  KEY `DiscountId` (`DiscountId`),
  CONSTRAINT `uservoucherusage_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `uservoucherusage_ibfk_2` FOREIGN KEY (`DiscountId`) REFERENCES `Discount` (`DiscountId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Vehicle`
--

DROP TABLE IF EXISTS `Vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Vehicle` (
  `VehicleId` char(36) NOT NULL DEFAULT (uuid()),
  `BrandId` char(36) NOT NULL,
  `VehicleModel` varchar(50) NOT NULL,
  `YearManufactured` int DEFAULT NULL,
  `TransmissionTypeId` char(36) DEFAULT NULL,
  `FuelTypeId` char(36) NOT NULL,
  `LicensePlate` varchar(20) NOT NULL,
  `Seats` int NOT NULL,
  `Odometer` int NOT NULL,
  `PricePerHour` decimal(10,2) NOT NULL,
  `PricePerDay` decimal(10,2) NOT NULL,
  `PricePerMonth` decimal(10,2) NOT NULL,
  `Status` varchar(20) NOT NULL,
  `Description` varchar(500) DEFAULT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CategoryId` char(36) DEFAULT NULL,
  `LastUpdatedBy` char(36) DEFAULT NULL,
  `VehicleType` varchar(20) NOT NULL,
  `RequiresLicense` tinyint(1) NOT NULL DEFAULT '1',
  `BatteryCapacity` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`VehicleId`),
  KEY `BrandId` (`BrandId`),
  KEY `TransmissionTypeId` (`TransmissionTypeId`),
  KEY `FuelTypeId` (`FuelTypeId`),
  KEY `LastUpdatedBy` (`LastUpdatedBy`),
  KEY `CategoryId` (`CategoryId`),
  KEY `idx_vehicle_type` (`VehicleType`),
  KEY `idx_vehicle_license` (`RequiresLicense`),
  KEY `idx_license_plate` (`LicensePlate`),
  KEY `idx_vehicle_status` (`Status`), -- THÊM INDEX MỚI
  CONSTRAINT `vehicle_ibfk_1` FOREIGN KEY (`BrandId`) REFERENCES `CarBrand` (`BrandId`),
  CONSTRAINT `vehicle_ibfk_2` FOREIGN KEY (`TransmissionTypeId`) REFERENCES `TransmissionType` (`TransmissionTypeId`),
  CONSTRAINT `vehicle_ibfk_3` FOREIGN KEY (`FuelTypeId`) REFERENCES `FuelType` (`FuelTypeId`),
  CONSTRAINT `vehicle_ibfk_4` FOREIGN KEY (`LastUpdatedBy`) REFERENCES `Users` (`UserId`) ON DELETE SET NULL,
  CONSTRAINT `vehicle_ibfk_5` FOREIGN KEY (`CategoryId`) REFERENCES `VehicleCategories` (`CategoryId`),
  CONSTRAINT `CHK_VehicleType` CHECK ((`VehicleType` in (_utf8mb4'ElectricCar',_utf8mb4'Motorcycle',_utf8mb4'ElectricMotorcycle')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
-- Table structure for table `VehicleCategories`
--

DROP TABLE IF EXISTS `VehicleCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `VehicleCategories` (
  `CategoryId` char(36) NOT NULL,
  `CategoryName` varchar(100) NOT NULL,
  `VehicleType` varchar(20) NOT NULL,
  PRIMARY KEY (`CategoryId`),
  CONSTRAINT `CHK_VehicleCategoryType` CHECK ((`VehicleType` in (_utf8mb4'ElectricCar',_utf8mb4'Motorcycle',_utf8mb4'ElectricMotorcycle')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `VehicleConditionLogs`
--

DROP TABLE IF EXISTS `VehicleConditionLogs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `VehicleConditionLogs` (
  `LogId` char(36) NOT NULL,
  `BookingId` char(36) NOT NULL,
  `VehicleId` char(36) NOT NULL,
  `StaffId` char(36) DEFAULT NULL,
  `CheckType` varchar(20) NOT NULL,
  `CheckTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Odometer` int DEFAULT NULL,
  `FuelLevel` varchar(20) DEFAULT NULL,
  `ConditionStatus` varchar(100) DEFAULT NULL,
  `ConditionDescription` varchar(1000) DEFAULT NULL,
  `DamageImages` text,
  `Note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`LogId`),
  KEY `BookingId` (`BookingId`),
  KEY `VehicleId` (`VehicleId`),
  KEY `StaffId` (`StaffId`),
  CONSTRAINT `vehicleconditionlogs_ibfk_1` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`) ON DELETE CASCADE,
  CONSTRAINT `vehicleconditionlogs_ibfk_2` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`) ON DELETE CASCADE,
  CONSTRAINT `vehicleconditionlogs_ibfk_3` FOREIGN KEY (`StaffId`) REFERENCES `Users` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `VehicleFeaturesMapping`
--

DROP TABLE IF EXISTS `VehicleFeaturesMapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `VehicleFeaturesMapping` (
  `VehicleId` char(36) NOT NULL,
  `FeatureId` char(36) NOT NULL,
  PRIMARY KEY (`VehicleId`,`FeatureId`),
  KEY `FeatureId` (`FeatureId`),
  CONSTRAINT `vehiclefeaturesmapping_ibfk_1` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`) ON DELETE CASCADE,
  CONSTRAINT `vehiclefeaturesmapping_ibfk_2` FOREIGN KEY (`FeatureId`) REFERENCES `CarFeature` (`FeatureId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `VehicleImages`
--

DROP TABLE IF EXISTS `VehicleImages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `VehicleImages` (
  `ImageId` char(36) NOT NULL,
  `VehicleId` char(36) NOT NULL,
  `ImageUrl` varchar(255) NOT NULL,
  `IsMain` tinyint(1) NOT NULL,
  PRIMARY KEY (`ImageId`),
  KEY `VehicleId` (`VehicleId`),
  CONSTRAINT `vehicleimages_ibfk_1` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-14  0:36:00

CREATE INDEX idx_vehicle_rental_prices ON Vehicle(PricePerDay, PricePerHour, PricePerMonth);
CREATE INDEX idx_booking_dates_status ON Booking(PickupDateTime, ReturnDateTime, Status);