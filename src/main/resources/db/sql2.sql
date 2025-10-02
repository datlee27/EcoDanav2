-- =============================================
-- DATABASE: ecodanav2
-- HỆ QUẢN TRỊ: MySQL
-- PHIÊN BẢN TỐI ƯU HÓA
-- =============================================

USE ecodanav2;

-- Vô hiệu hóa các kiểm tra để đảm bảo việc xóa và tạo bảng diễn ra suôn sẻ
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

-- =============================================
-- SECTION 1: CÁC BẢNG TRA CỨU (LOOKUP TABLES)
-- =============================================

DROP TABLE IF EXISTS `Roles`;
CREATE TABLE `Roles` (
  `RoleId` char(36) NOT NULL,
  `RoleName` varchar(50) DEFAULT NULL,
  `NormalizedName` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`RoleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `VehicleCategories`;
CREATE TABLE `VehicleCategories` (
  `CategoryId` int NOT NULL AUTO_INCREMENT,
  `CategoryName` varchar(100) NOT NULL,
  PRIMARY KEY (`CategoryId`),
  UNIQUE KEY `CategoryName` (`CategoryName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `TransmissionTypes`;
CREATE TABLE `TransmissionTypes` (
  `TransmissionTypeId` int NOT NULL AUTO_INCREMENT,
  `TransmissionTypeName` varchar(100) NOT NULL,
  PRIMARY KEY (`TransmissionTypeId`),
  UNIQUE KEY `TransmissionTypeName` (`TransmissionTypeName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================
-- SECTION 2: CÁC BẢNG CỐT LÕI (CORE TABLES)
-- =============================================

DROP TABLE IF EXISTS `Users`;
CREATE TABLE `Users` (
  `UserId` char(36) NOT NULL,
  `Username` varchar(100) NOT NULL,
  `FirstName` varchar(256) DEFAULT NULL,
  `LastName` varchar(256) DEFAULT NULL,
  `UserDOB` date DEFAULT NULL,
  `PhoneNumber` varchar(15) DEFAULT NULL,
  `AvatarUrl` varchar(255) DEFAULT NULL,
  `Gender` ENUM('Male', 'Female', 'Other') DEFAULT NULL,
  `Status` ENUM('Active', 'Inactive', 'Banned') NOT NULL DEFAULT 'Active',
  `RoleId` char(36) NOT NULL,
  `Email` varchar(100) NOT NULL,
  `EmailVerifed` tinyint(1) NOT NULL DEFAULT '0',
  `PasswordHash` varchar(255) NOT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `NormalizedUserName` varchar(256) DEFAULT NULL,
  `NormalizedEmail` varchar(256) DEFAULT NULL,
  `SecurityStamp` text,
  `ConcurrencyStamp` text,
  `TwoFactorEnabled` tinyint(1) NOT NULL DEFAULT '0',
  `LockoutEnd` datetime DEFAULT NULL,
  `LockoutEnabled` tinyint(1) NOT NULL DEFAULT '0',
  `AccessFailedCount` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `Username` (`Username`),
  UNIQUE KEY `Email` (`Email`),
  KEY `RoleId` (`RoleId`),
  KEY `idx_user_email` (`Email`),
  KEY `idx_user_status` (`Status`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`RoleId`) REFERENCES `Roles` (`RoleId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `UserDocuments`;
CREATE TABLE `UserDocuments` (
  `DocumentId` char(36) NOT NULL,
  `UserId` char(36) NOT NULL,
  `DocumentType` ENUM('CitizenId', 'DriverLicense', 'Passport') NOT NULL,
  `DocumentNumber` varchar(50) NOT NULL,
  `FullName` varchar(100) DEFAULT NULL,
  `DOB` date DEFAULT NULL,
  `IssuedDate` date DEFAULT NULL,
  `IssuedPlace` varchar(100) DEFAULT NULL,
  `FrontImageUrl` varchar(500) DEFAULT NULL,
  `BackImageUrl` varchar(500) DEFAULT NULL,
  `IsVerified` tinyint(1) NOT NULL DEFAULT '0',
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`DocumentId`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `userdocuments_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Vehicle`;
CREATE TABLE `Vehicle` (
  `VehicleId` char(36) NOT NULL,
  `VehicleModel` varchar(50) NOT NULL COMMENT 'Ví dụ: VF8, Feliz S',
  `YearManufactured` int DEFAULT NULL,
  `LicensePlate` varchar(20) NOT NULL,
  `Seats` int NOT NULL,
  `Odometer` int NOT NULL,
  `RentalPrices` JSON DEFAULT NULL COMMENT 'Lưu giá dạng JSON: {"hourly": 50000, "daily": 500000, "monthly": 10000000}',
  `Status` ENUM('Available', 'Rented', 'Maintenance', 'Unavailable') NOT NULL DEFAULT 'Available',
  `Description` varchar(500) DEFAULT NULL,
  `VehicleType` ENUM('ElectricCar', 'ElectricMotorcycle') NOT NULL,
  `RequiresLicense` tinyint(1) NOT NULL DEFAULT '1',
  `BatteryCapacity` decimal(10,2) DEFAULT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LastUpdatedBy` char(36) DEFAULT NULL,
  `CategoryId` int DEFAULT NULL,
  `TransmissionTypeId` int DEFAULT NULL,
  `MainImageUrl` varchar(255) DEFAULT NULL,
  `ImageUrls` JSON DEFAULT NULL COMMENT 'Lưu dạng mảng JSON: ["url1", "url2"]',
  `Features` JSON DEFAULT NULL COMMENT 'Lưu dạng mảng JSON: ["GPS", "Camera 360"]',
  PRIMARY KEY (`VehicleId`),
  KEY `LastUpdatedBy` (`LastUpdatedBy`),
  KEY `CategoryId` (`CategoryId`),
  KEY `TransmissionTypeId` (`TransmissionTypeId`),
  KEY `idx_vehicle_type` (`VehicleType`),
  KEY `idx_license_plate` (`LicensePlate`),
  KEY `idx_vehicle_status` (`Status`),
  CONSTRAINT `vehicle_ibfk_1` FOREIGN KEY (`LastUpdatedBy`) REFERENCES `Users` (`UserId`) ON DELETE SET NULL,
  CONSTRAINT `vehicle_ibfk_2` FOREIGN KEY (`CategoryId`) REFERENCES `VehicleCategories` (`CategoryId`) ON DELETE SET NULL,
  CONSTRAINT `vehicle_ibfk_3` FOREIGN KEY (`TransmissionTypeId`) REFERENCES `TransmissionTypes` (`TransmissionTypeId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Discount`;
CREATE TABLE `Discount` (
  `DiscountId` char(36) NOT NULL,
  `DiscountName` varchar(100) NOT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `DiscountType` ENUM('Percentage', 'FixedAmount') NOT NULL,
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
  PRIMARY KEY (`DiscountId`),
  UNIQUE KEY `VoucherCode` (`VoucherCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Booking`;
CREATE TABLE `Booking` (
  `BookingId` char(36) NOT NULL,
  `UserId` char(36) NOT NULL,
  `VehicleId` char(36) NOT NULL,
  `HandledBy` char(36) DEFAULT NULL,
  `PickupDateTime` datetime NOT NULL,
  `ReturnDateTime` datetime NOT NULL,
  `TotalAmount` decimal(10,2) NOT NULL,
  `Status` ENUM('Pending', 'Approved', 'Rejected', 'Ongoing', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Pending',
  `DiscountId` char(36) DEFAULT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CancelReason` varchar(500) DEFAULT NULL,
  `BookingCode` varchar(20) NOT NULL,
  `ExpectedPaymentMethod` varchar(50) DEFAULT NULL,
  `RentalType` ENUM('hourly', 'daily', 'monthly') NOT NULL DEFAULT 'daily',
  `TermsAgreed` tinyint(1) NOT NULL DEFAULT '0',
  `TermsAgreedAt` datetime DEFAULT NULL,
  `TermsVersion` varchar(10) DEFAULT 'v1.0',
  PRIMARY KEY (`BookingId`),
  UNIQUE KEY `BookingCode` (`BookingCode`),
  KEY `UserId` (`UserId`),
  KEY `VehicleId` (`VehicleId`),
  KEY `HandledBy` (`HandledBy`),
  KEY `DiscountId` (`DiscountId`),
  KEY `idx_booking_dates_status` (`PickupDateTime`, `ReturnDateTime`, `Status`),
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`),
  CONSTRAINT `booking_ibfk_3` FOREIGN KEY (`HandledBy`) REFERENCES `Users` (`UserId`),
  CONSTRAINT `booking_ibfk_4` FOREIGN KEY (`DiscountId`) REFERENCES `Discount` (`DiscountId`) ON DELETE SET NULL,
  CONSTRAINT `CHK_Booking_Amount` CHECK ((`TotalAmount` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Contract`;
CREATE TABLE `Contract` (
  `ContractId` char(36) NOT NULL,
  `ContractCode` varchar(30) NOT NULL,
  `UserId` char(36) NOT NULL,
  `BookingId` char(36) NOT NULL,
  `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `SignedDate` datetime DEFAULT NULL,
  `CompletedDate` datetime DEFAULT NULL,
  `Status` ENUM('Draft', 'Signed', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Draft',
  `TermsAccepted` tinyint(1) NOT NULL DEFAULT '0',
  `SignatureData` text,
  `SignatureMethod` varchar(20) DEFAULT NULL,
  `ContractPdfUrl` varchar(500) DEFAULT NULL,
  `Notes` varchar(500) DEFAULT NULL,
  `CancellationReason` varchar(500) DEFAULT NULL,
  `CitizenIdSnapshotId` char(36) DEFAULT NULL,
  `DriverLicenseSnapshotId` char(36) DEFAULT NULL,
  PRIMARY KEY (`ContractId`),
  UNIQUE KEY `ContractCode` (`ContractCode`),
  KEY `UserId` (`UserId`),
  KEY `BookingId` (`BookingId`),
  KEY `idx_contract_status` (`Status`),
  CONSTRAINT `contract_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `contract_ibfk_2` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`),
  CONSTRAINT `contract_ibfk_3` FOREIGN KEY (`CitizenIdSnapshotId`) REFERENCES `UserDocuments` (`DocumentId`) ON DELETE SET NULL,
  CONSTRAINT `contract_ibfk_4` FOREIGN KEY (`DriverLicenseSnapshotId`) REFERENCES `UserDocuments` (`DocumentId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Payment`;
CREATE TABLE `Payment` (
  `PaymentId` char(36) NOT NULL,
  `BookingId` char(36) NOT NULL,
  `ContractId` char(36) DEFAULT NULL,
  `Amount` decimal(10,2) NOT NULL,
  `PaymentMethod` varchar(50) NOT NULL,
  `PaymentStatus` ENUM('Pending', 'Completed', 'Failed', 'Refunded') NOT NULL DEFAULT 'Pending',
  `PaymentType` ENUM('Deposit', 'FinalPayment', 'Surcharge', 'Refund') NOT NULL DEFAULT 'Deposit',
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

DROP TABLE IF EXISTS `BookingApproval`;
CREATE TABLE `BookingApproval` (
  `ApprovalId` char(36) NOT NULL,
  `BookingId` char(36) NOT NULL,
  `StaffId` char(36) NOT NULL,
  `ApprovalStatus` ENUM('Approved', 'Rejected') NOT NULL,
  `ApprovalDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Note` varchar(500) DEFAULT NULL,
  `RejectionReason` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`ApprovalId`),
  KEY `BookingId` (`BookingId`),
  KEY `StaffId` (`StaffId`),
  CONSTRAINT `bookingapproval_ibfk_1` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`) ON DELETE CASCADE,
  CONSTRAINT `bookingapproval_ibfk_2` FOREIGN KEY (`StaffId`) REFERENCES `Users` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `BookingSurcharges`;
CREATE TABLE `BookingSurcharges` (
  `SurchargeId` char(36) NOT NULL,
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

-- =============================================
-- SECTION 3: CÁC BẢNG PHỤ TRỢ (SUPPORTING TABLES)
-- =============================================

DROP TABLE IF EXISTS `UserFeedback`;
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
  CONSTRAINT `CHK_Rating_Range` CHECK ((`Rating` BETWEEN 1 AND 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `VehicleConditionLogs`;
CREATE TABLE `VehicleConditionLogs` (
  `LogId` char(36) NOT NULL,
  `BookingId` char(36) NOT NULL,
  `VehicleId` char(36) NOT NULL,
  `StaffId` char(36) DEFAULT NULL,
  `CheckType` ENUM('Pickup', 'Return') NOT NULL,
  `CheckTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Odometer` int DEFAULT NULL,
  `FuelLevel` varchar(20) DEFAULT NULL,
  `ConditionStatus` varchar(100) DEFAULT NULL,
  `ConditionDescription` varchar(1000) DEFAULT NULL,
  `DamageImages` JSON,
  `Note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`LogId`),
  KEY `BookingId` (`BookingId`),
  KEY `VehicleId` (`VehicleId`),
  KEY `StaffId` (`StaffId`),
  CONSTRAINT `vehicleconditionlogs_ibfk_1` FOREIGN KEY (`BookingId`) REFERENCES `Booking` (`BookingId`) ON DELETE CASCADE,
  CONSTRAINT `vehicleconditionlogs_ibfk_2` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`) ON DELETE CASCADE,
  CONSTRAINT `vehicleconditionlogs_ibfk_3` FOREIGN KEY (`StaffId`) REFERENCES `Users` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `BatteryLogs`;
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

DROP TABLE IF EXISTS `EmailOTPVerification`;
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

DROP TABLE IF EXISTS `PasswordResetTokens`;
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

DROP TABLE IF EXISTS `UserFavoriteVehicles`;
CREATE TABLE `UserFavoriteVehicles` (
  `UserId` char(36) NOT NULL,
  `VehicleId` char(36) NOT NULL,
  PRIMARY KEY (`UserId`,`VehicleId`),
  KEY `VehicleId` (`VehicleId`),
  CONSTRAINT `userfavoritevehicles_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `userfavoritevehicles_ibfk_2` FOREIGN KEY (`VehicleId`) REFERENCES `Vehicle` (`VehicleId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `UserLogins`;
CREATE TABLE `UserLogins` (
  `LoginProvider` varchar(128) NOT NULL,
  `ProviderKey` varchar(128) NOT NULL,
  `ProviderDisplayName` text,
  `UserId` char(36) NOT NULL,
  PRIMARY KEY (`LoginProvider`,`ProviderKey`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `userlogins_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `UserVoucherUsage`;
CREATE TABLE `UserVoucherUsage` (
  `UserId` char(36) NOT NULL,
  `DiscountId` char(36) NOT NULL,
  `UsedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserId`,`DiscountId`),
  KEY `DiscountId` (`DiscountId`),
  CONSTRAINT `uservoucherusage_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE,
  CONSTRAINT `uservoucherusage_ibfk_2` FOREIGN KEY (`DiscountId`) REFERENCES `Discount` (`DiscountId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Notification`;
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

DROP TABLE IF EXISTS `AccountDeletionLogs`;
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

DROP TABLE IF EXISTS `Terms`;
CREATE TABLE `Terms` (
  `TermsId` char(36) NOT NULL,
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

-- Khôi phục lại các thiết lập ban đầu
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;