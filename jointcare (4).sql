-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 30, 2026 at 08:29 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `jointcare`
--

-- --------------------------------------------------------

--
-- Table structure for table `comorbidities`
--

CREATE TABLE `comorbidities` (
  `id` int(10) UNSIGNED NOT NULL,
  `patient_id` varchar(50) NOT NULL,
  `doctor_id` varchar(50) NOT NULL,
  `title` varchar(255) NOT NULL,
  `text` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `comorbidities`
--

INSERT INTO `comorbidities` (`id`, `patient_id`, `doctor_id`, `title`, `text`, `created_at`) VALUES
(1, 'P123', 'D001', 'Comorbidity', 'Diabetes Mellitus Type 2', '2025-12-06 19:35:55'),
(2, 'P123', 'D001', 'Comorbidity', 'Diabetes Mellitus Type 2', '2025-12-06 19:36:10'),
(3, 'P0003', 'doc_1333', 'Comorbidity', 'joint knee pain', '2025-12-06 19:38:12'),
(4, 'P0005', 'doc_1111', 'Comorbidity', 'diabetis', '2025-12-25 09:17:06'),
(5, 'P0003', 'doc_1333', 'Comorbidity', 'bp', '2026-01-06 04:22:11'),
(6, 'P0003', 'doc_1333', 'Comorbidity', 'bp', '2026-01-06 07:31:20'),
(7, 'Pat_2222', 'doc_7777', 'Comorbidity', 'Diabetis', '2026-01-07 07:37:22'),
(8, 'Pat_2222', 'doc_7777', 'Comorbidity', 'blood pressure', '2026-01-07 07:37:35'),
(9, 'Pat_2222', 'doc_7777', 'Comorbidity', 'bp', '2026-01-18 19:18:41'),
(10, 'Pat_2222', 'doc_7777', 'Comorbidity', 'leg pain', '2026-01-19 15:10:33'),
(11, 'Pat_2222', 'doc_7777', 'Comorbidity', 'Diabetis', '2026-01-20 12:51:19'),
(12, 'Pat_2222', 'doc_7777', 'Comorbidity', 'blood pressure', '2026-01-26 14:01:15'),
(13, 'pat_1910', 'doc_1910', 'Comorbidity', 'Blood pressure', '2026-01-28 03:12:17'),
(14, 'pat_1910', 'doc_1910', 'Comorbidity', 'Diabetes', '2026-01-28 17:09:53'),
(15, 'pat_1910', 'doc_1910', 'Comorbidity', 'bp', '2026-01-28 17:57:00'),
(16, 'pat_1910', 'doc_1910', 'Comorbidity', 'ulcer', '2026-01-30 06:42:45'),
(17, 'pat_1910', 'doc_1910', 'Comorbidity', 'diabetes', '2026-01-30 06:47:16');

-- --------------------------------------------------------

--
-- Table structure for table `complaints`
--

CREATE TABLE `complaints` (
  `id` int(11) UNSIGNED NOT NULL,
  `patient_id` varchar(20) NOT NULL,
  `doctor_id` varchar(20) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `complaints`
--

INSERT INTO `complaints` (`id`, `patient_id`, `doctor_id`, `title`, `description`, `created_at`) VALUES
(1, 'pat_1001', 'doc_1333', 'Knee pain when walking', 'Patient complains of right knee pain since 3 days, worse when climbing stairs.', '2025-12-06 17:51:27'),
(2, 'p_0001', 'doc_1333', 'Knee pain', '', '2025-12-06 18:09:05'),
(3, 'P0003', 'doc_1333', 'hypertension', '', '2025-12-06 18:09:45'),
(4, 'P0003', 'doc_1333', 'leg pain', '', '2025-12-06 18:50:06'),
(5, 'P0003', 'doc_1333', 'joint pain', '', '2025-12-06 18:50:16'),
(6, 'P0005', 'doc_1111', 'leg pain', '', '2025-12-25 09:16:46'),
(7, 'P0003', 'doc_1333', 'leg pain', '', '2026-01-06 07:31:10'),
(8, 'Pat_2222', 'doc_7777', 'leg pain', '', '2026-01-07 07:36:34'),
(9, 'Pat_2222', 'doc_7777', 'blood pressure', '', '2026-01-07 07:36:43'),
(10, 'Pat_2222', 'doc_7777', 'not taking tablets regularly', '', '2026-01-19 15:09:31'),
(11, 'Pat_2222', 'doc_7777', 'joint pain', '', '2026-01-20 14:43:44'),
(12, 'Pat_2222', 'doc_7777', 'joint pain', '', '2026-01-26 14:00:58'),
(13, 'Pat_2222', 'doc_7777', 'leg pain', '', '2026-01-27 06:57:48'),
(14, 'pat_1910', 'doc_1910', 'leg pain', '', '2026-01-28 03:10:11'),
(15, 'pat_1910', 'doc_1910', 'joint pain', '', '2026-01-28 17:08:50'),
(16, 'pat_1910', 'doc_1910', 'knee pain', '', '2026-01-30 06:42:30'),
(17, 'pat_1910', 'doc_1910', 'head ache', '', '2026-01-30 06:47:08');

-- --------------------------------------------------------

--
-- Table structure for table `daily_pain`
--

CREATE TABLE `daily_pain` (
  `id` int(11) DEFAULT NULL,
  `user_id` varchar(200) DEFAULT NULL,
  `pain_value` int(11) DEFAULT NULL,
  `record_date` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `daily_pain`
--

INSERT INTO `daily_pain` (`id`, `user_id`, `pain_value`, `record_date`) VALUES
(NULL, 'P0005', 7, '2025-12-25 23:00:52'),
(NULL, 'P0005', 4, '2025-12-25 23:00:59'),
(NULL, 'P0005', 8, '2025-12-25 23:01:50'),
(NULL, 'P0005', 1, '2025-12-25 23:01:57'),
(NULL, 'P0005', 6, '2025-12-25 23:02:31'),
(NULL, 'P0005', 4, '2025-12-26 15:51:28'),
(NULL, 'P0008', 2, '2026-01-04 14:43:08'),
(NULL, 'P0008', 6, '2026-01-04 14:43:13'),
(NULL, 'P0008', 8, '2026-01-04 14:43:16'),
(NULL, 'P0003', 4, '2026-01-06 21:03:55'),
(NULL, 'P0003', 6, '2026-01-06 21:03:59'),
(NULL, 'P0003', 8, '2026-01-06 21:04:22'),
(NULL, 'P0003', 3, '2026-01-06 21:04:28'),
(NULL, 'P0003', 3, '2026-01-06 21:05:24'),
(NULL, 'P0003', 1, '2026-01-06 21:06:46'),
(NULL, 'P0003', 3, '2026-01-06 21:24:54'),
(NULL, 'pat_2222', 0, '2026-01-15 08:27:51'),
(NULL, 'pat_2222', 4, '2026-01-15 08:27:57'),
(NULL, 'pat_2222', 7, '2026-01-15 08:28:01'),
(NULL, 'pat_2222', 5, '2026-01-15 08:28:04'),
(NULL, 'pat_2222', 7, '2026-01-15 08:28:08'),
(NULL, 'pat_2222', 4, '2026-01-15 08:28:46'),
(NULL, 'pat_1910', 3, '2026-01-28 23:35:49'),
(NULL, 'pat_1910', 5, '2026-01-28 23:35:51'),
(NULL, 'pat_1910', 7, '2026-01-28 23:35:53'),
(NULL, 'pat_1910', 9, '2026-01-28 23:35:56'),
(NULL, 'pat_1910', 4, '2026-01-28 23:35:58');

-- --------------------------------------------------------

--
-- Table structure for table `doctors`
--

CREATE TABLE `doctors` (
  `id` int(11) NOT NULL,
  `doctor_id` varchar(20) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `specialization` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctors`
--

INSERT INTO `doctors` (`id`, `doctor_id`, `full_name`, `email`, `phone`, `specialization`, `password`, `created_at`) VALUES
(1, 'doc_1001', 'John Doe', 'john@example.com', '9876543210', 'Orthopedic Surgeon', 'secret123', '2025-12-05 08:24:42'),
(2, 'doc_2001', 'John Test', 'test2001@example.com', '9876543210', 'Orthopedic', 'secret123', '2025-12-05 08:26:23'),
(3, 'doc_1556', 'chakri', 'chakri@gmail.com', '3425768990', 'ortho', '123456', '2025-12-05 08:51:09'),
(4, 'doc_1776', 'chandra', 'chadra@gmail.com', '2315467389', 'orthopedic', '123456', '2025-12-05 16:41:27'),
(5, 'doc_1024', 'suneetha', 'suneetha@gmail.com', '3427165890', 'orthopedic', '123456', '2025-12-05 16:51:03'),
(6, 'doc_1999', 'madhu', 'madhu@gmail.com', '2314562389', 'orthopedic', '123456', '2025-12-06 10:05:58'),
(7, 'doc_1333', 'amar', 'amar@gmail.com', '3245637898', 'orthopedic', '123456', '2025-12-06 15:04:25'),
(8, 'doc_1899', 'GNANA DEEPIKA', 'gnanadeepikausdf@gmail.com', '4523897654', 'orthopedic', '123456', '2025-12-16 05:22:51'),
(9, 'doc_1111', 'ramya', 'ramya@gmail.com', '3420876343', 'orthopedic', '123456', '2025-12-16 07:51:39'),
(10, 'doc_1022', 'sandhyas', 'andhya123@gmail.com', '3420987654', 'orthopedics', '123456', '2025-12-17 06:56:47'),
(11, 'doc_1334', 'poojitha', 'poojitha123@gmail.com', '1324567890', 'orthopedic', '123456', '2025-12-17 18:04:29'),
(12, 'doc_4444', 'sreerams', 'rreamu@gmail.com', '3425785690', 'orthopedic', '123456', '2025-12-17 18:05:20'),
(13, 'doc_5555', 'UPPU GNANA DEEPIKA', 'gnanadeepikau@gmail.com', '6919652520', 'Orthopedic', '123456', '2026-01-04 02:47:23'),
(14, 'doc_9999', 'radhika', 'radhikad@gmail.com', '2879643542', 'Orthopedic', '123456', '2026-01-04 09:09:10'),
(17, 'doc_0000', 'rama', 'ramu@gmail.com', '5248963215', 'Orthopedic', 'Deepu@123', '2026-01-07 06:42:24'),
(18, 'doc_1444', 'varshu', 'varshup@gmail.com', '5463127898', 'Orthopedic', 'Varshu@123', '2026-01-07 06:43:32'),
(19, 'doc_6666', 'Deepika', 'deepikau@gmail.com', '5421369745', 'Orthopedic', 'Deepika@123', '2026-01-07 07:19:46'),
(20, 'doc_8888', 'raghu', 'raghunath@gmail.ckm', '3498845213', 'Ortho', 'raghu@1234', '2026-01-07 07:22:54'),
(21, 'doc_7777', 'Priya', 'priyas@gmail.com', '4578123694', 'Orthopedic', 'Ab@1234', '2026-01-07 07:29:39'),
(22, 'doc_1004', '1111', 'ra@gnail.co', '1111111111', 'Oncologist', 'Qwerty@12', '2026-01-27 08:37:27'),
(23, 'doc_1910', 'gnanag', 'gnana23@gmail.com', '6532478945', 'Ortho', '$2y$10$WkjwJdyixgbytRSIZRUA7eAuzXxVJnZRcaQXIufqJbguLn0VHTD66', '2026-01-27 11:24:17');

-- --------------------------------------------------------

--
-- Table structure for table `investigations`
--

CREATE TABLE `investigations` (
  `id` int(10) UNSIGNED NOT NULL,
  `patient_id` varchar(50) NOT NULL,
  `hb` varchar(50) DEFAULT NULL,
  `total_leukocyte` varchar(50) DEFAULT NULL,
  `differential_count` varchar(100) DEFAULT NULL,
  `platelet_count` varchar(50) DEFAULT NULL,
  `esr` varchar(50) DEFAULT NULL,
  `crp` varchar(50) DEFAULT NULL,
  `lft_total_bilirubin` varchar(50) DEFAULT NULL,
  `lft_direct_bilirubin` varchar(50) DEFAULT NULL,
  `ast` varchar(50) DEFAULT NULL,
  `alt` varchar(50) DEFAULT NULL,
  `albumin` varchar(50) DEFAULT NULL,
  `total_protein` varchar(50) DEFAULT NULL,
  `ggt` varchar(50) DEFAULT NULL,
  `urea` varchar(50) DEFAULT NULL,
  `creatinine` varchar(50) DEFAULT NULL,
  `uric_acid` varchar(50) DEFAULT NULL,
  `urine_routine` varchar(255) DEFAULT NULL,
  `urine_pcr` varchar(50) DEFAULT NULL,
  `ra_factor` varchar(50) DEFAULT NULL,
  `anti_ccp` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `investigations`
--

INSERT INTO `investigations` (`id`, `patient_id`, `hb`, `total_leukocyte`, `differential_count`, `platelet_count`, `esr`, `crp`, `lft_total_bilirubin`, `lft_direct_bilirubin`, `ast`, `alt`, `albumin`, `total_protein`, `ggt`, `urea`, `creatinine`, `uric_acid`, `urine_routine`, `urine_pcr`, `ra_factor`, `anti_ccp`, `created_at`) VALUES
(1, 'P001', '13.5', '7800', 'N60 L30 M8 E2', '2.5 lakh', '20', '3.2', '0.8', '0.2', '32', '28', '4.2', '7.0', '30', '28', '0.9', '4.5', 'No protein, no sugar', '0.1', 'Negative', 'Negative', '2025-12-07 04:17:12'),
(2, 'P0003', '3', '23', '677', '66', 'gh', '56', '32', 'hgdn', 'fbvjfg', 'bfgtr', 'tttrh', 'sdemfn', 'we12f', 'tbggf', '6u6b nn', 'ngnh', 'ghg', 'fert', 'r3rgrfb', 'fgy56', '2025-12-07 04:19:48'),
(3, 'P0005', '1', 'eu', 'bzhs', 'nnzi', 'sye', 'jaja', 'jansn', 'nN', 'jan', 'bB', 'jK', '32', 'sn', 'nzn', 'nsnz', 'Uh', 'jznz', 'zn', 'n', 'jz', '2025-12-25 09:18:19'),
(4, 'Pat_2222', '2', 'bab', 'hab', 'bab', 'hav', 'bab', 'gca', 'iihs', 'hab', 'jana', 'jan', 'ush', 'jana', 'ushab', 'sjbsb', 'sjbs', 'hsbsb', 'yuv', 'yfcy', 'vuuv', '2026-01-08 05:00:49'),
(5, 'Pat_2222', 'yvy', 'jv', 'uv', 'bk', 'ubs', 'ik', 'kgywb', 'jsnon', 'hvbk', 'vuh', 'gijg', 'bihoen', 'bios', 'biug', 'bibsk', 'jbb', 'ivskb', 'ihbuv', 'fyubj', 'jbjy', '2026-01-26 14:07:51'),
(6, 'pat_1910', 'heh', '23', '65', '89', 'fag', 'hazb', 'hansn', 'hahbs', 'gsgsb', 'nsjj', 'hshdb', 'hdhdn', 'hebbd', 'hshxbxb', 'hdhdb', 'jsjdb', 'hdjdn', 'hsbxb', '32', 'hsbsb', '2026-01-28 03:18:38'),
(7, 'pat_1910', '36', '156', '993', '772', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '2026-01-30 06:44:47');

-- --------------------------------------------------------

--
-- Table structure for table `medications`
--

CREATE TABLE `medications` (
  `id` int(10) UNSIGNED NOT NULL,
  `patient_id` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  `dose` varchar(100) DEFAULT '-',
  `period` varchar(50) DEFAULT '-',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `medications`
--

INSERT INTO `medications` (`id`, `patient_id`, `name`, `dose`, `period`, `created_at`) VALUES
(1, 'pat_1910', 'Dolo', '500mg', 'daily', '2026-01-28 07:23:10'),
(2, 'pat_1910', 'Paracetamol', '100 mg', 'daily', '2026-01-28 07:35:00'),
(3, 'pat_1910', 'DMArd', '300 mg', 'daily', '2026-01-28 07:35:40'),
(4, 'pat_1910', 'Dolo', '200 mg', '-', '2026-01-28 08:07:42'),
(5, 'pat_1910', 'TNF2', '100 mg', 'daily', '2026-01-28 17:12:36'),
(6, 'pat_1910', 'Dolo2', '400 mg', 'daily', '2026-01-28 17:59:57'),
(7, 'pat_1910', 'Amoxylin', '600mg', '3', '2026-01-30 06:44:16'),
(8, 'pat_2222', 'Doli', '200 mg', 'daily', '2026-01-30 07:23:33'),
(9, 'pat_2222', 'Paracetamol', '200 mg', 'daily', '2026-01-30 07:23:46');

-- --------------------------------------------------------

--
-- Table structure for table `patients`
--

CREATE TABLE `patients` (
  `id` int(11) NOT NULL,
  `patient_id` varchar(20) NOT NULL,
  `doctor_id` varchar(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `age` int(11) NOT NULL,
  `sex` varchar(10) NOT NULL,
  `occupation` varchar(100) NOT NULL,
  `address` varchar(255) NOT NULL,
  `mobile` varchar(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `patients`
--

INSERT INTO `patients` (`id`, `patient_id`, `doctor_id`, `name`, `email`, `age`, `sex`, `occupation`, `address`, `mobile`, `password`, `created_at`) VALUES
(1, 'P0001', NULL, 'Test Patient', 'testpatient@example.com', 32, 'Female', 'Engineer', 'Some address, City', '9876543210', 'secret123', '2025-12-05 17:08:56'),
(2, 'P0002', 'doc_1334', 'raghu', 'ragu@gmail.com', 32, 'male', 'software', 'nellore', '1290763425', '234567', '2025-12-06 15:59:06'),
(3, 'P0003', 'doc_1333', 'ramumaya', 'ramulur@gmail.com', 65, 'male', 'teacher', 'nellore', '5423986230', '345678', '2025-12-06 17:23:43'),
(4, 'P0004', 'doc_1899', 'radha', 'radhas@gmail.com', 21, 'female', 'software', 'nellore', '2486450987', '234567', '2025-12-16 05:24:42'),
(5, 'P0005', 'doc_1111', 'ramulu', 'ramulu@gmail.com2', 32, 'male', 'engineer', 'kadapa', '4509823467', '234567', '2025-12-16 07:57:54'),
(6, 'P0006', 'doc_1022', 'raghav', 'raghav@gmail.com', 23, 'male', 'software', 'nellore', '1298765434', '234567', '2025-12-17 06:57:54'),
(7, 'P0007', NULL, 'ramu', 'ramus@gmail.com', 25, 'male', 'teacher', 'nellore', '5841237584', '234567', '2026-01-04 02:49:24'),
(8, 'P0008', 'doc_9999', 'gopi', 'gopi@gmail.com', 25, 'male', 'software', 'nellore', '5678123498', '234789', '2026-01-04 09:10:44'),
(9, 'Pat_2222', 'doc_7777', 'ramya', 'ramya@gmail.com', 21, 'female', 'software', 'nellore', '4512378965', 'Ramya@12', '2026-01-07 07:31:38'),
(10, 'pat_1910', 'doc_2001', 'deepika', 'deeps12@gmail.com', 21, 'female', 'teacher', 'nellore', '7842153426', 'Deepu@12', '2026-01-27 11:48:52');

-- --------------------------------------------------------

--
-- Table structure for table `referrals`
--

CREATE TABLE `referrals` (
  `patient_id` varchar(50) NOT NULL,
  `message` text NOT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `referrals`
--

INSERT INTO `referrals` (`patient_id`, `message`, `created_at`) VALUES
('pat_1910', 'Dr radha', '2026-01-28 12:13:42'),
('pat_1910', 'Dr asha', '2026-01-28 23:31:17'),
('pat_1910', 'Dr john', '2026-01-30 12:18:11'),
('Pat_2222', 'Dr asha', '2026-01-30 12:54:16');

-- --------------------------------------------------------

--
-- Table structure for table `save_disease_scores_graph`
--

CREATE TABLE `save_disease_scores_graph` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `patient_id` varchar(200) DEFAULT NULL,
  `pga` float DEFAULT NULL,
  `crp` float DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `tjc` int(11) DEFAULT 0,
  `sjc` int(11) DEFAULT 0,
  `ea` float DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `save_disease_scores_graph`
--

INSERT INTO `save_disease_scores_graph` (`id`, `user_id`, `patient_id`, `pga`, `crp`, `created_at`, `tjc`, `sjc`, `ea`) VALUES
(1, NULL, 'P0005', 3, 52, '2025-12-25 22:49:11', 0, 0, 0),
(2, NULL, 'P0005', 8, 52, '2025-12-25 22:53:26', 0, 0, 0),
(3, NULL, 'P0005', 12, 82, '2025-12-25 22:55:41', 0, 0, 0),
(4, NULL, 'P0005', 5, 65, '2025-12-25 22:57:40', 0, 0, 0),
(5, NULL, 'P0005', 6, 91, '2025-12-25 22:59:03', 0, 0, 0),
(6, NULL, 'P0005', 5, 58, '2025-12-25 22:59:27', 0, 0, 0),
(7, NULL, 'P0005', 7, 65, '2025-12-25 22:59:54', 0, 0, 0),
(8, NULL, 'P0005', 4, 63, '2025-12-26 15:48:30', 0, 0, 0),
(9, NULL, 'P0005', 6, 52, '2025-12-26 15:49:51', 0, 0, 0),
(10, NULL, 'P0003', 5, 25, '2026-01-04 14:50:25', 0, 0, 0),
(11, NULL, 'P0003', 3, 63, '2026-01-04 14:52:08', 0, 0, 0),
(12, NULL, 'P0003', 4, 656, '2026-01-06 20:38:43', 0, 0, 0),
(13, NULL, 'P0003', 0, 568, '2026-01-06 22:58:43', 0, 0, 0),
(14, NULL, 'Pat_2222', 4, 63, '2026-01-07 13:08:43', 0, 0, 0),
(15, NULL, 'Pat_2222', 5, 52, '2026-01-07 13:10:37', 0, 0, 0),
(16, NULL, 'Pat_2222', 4, 85, '2026-01-07 13:11:40', 0, 0, 0),
(17, NULL, 'Pat_2222', 7, 42, '2026-01-07 13:13:46', 0, 0, 0),
(18, NULL, 'Pat_2222', 2, 25, '2026-01-08 10:25:17', 0, 0, 0),
(19, NULL, 'Pat_2222', 4, 52, '2026-01-08 12:40:37', 0, 0, 0),
(20, NULL, 'Pat_2222', 3, 25, '2026-01-08 12:42:34', 0, 0, 0),
(21, NULL, 'Pat_2222', 4, 24, '2026-01-08 12:53:48', 0, 0, 0),
(22, NULL, 'Pat_2222', 4, 52, '2026-01-08 13:06:45', 0, 0, 0),
(23, NULL, 'Pat_2222', 4, 82, '2026-01-08 13:11:38', 0, 0, 0),
(24, NULL, 'Pat_2222', 3, 45, '2026-01-08 13:12:50', 0, 0, 0),
(25, NULL, 'Pat_2222', 3, 45, '2026-01-08 13:13:40', 0, 0, 0),
(26, NULL, 'Pat_2222', 4, 65, '2026-01-08 13:17:15', 0, 0, 0),
(27, NULL, 'Pat_2222', 4, 85, '2026-01-08 13:23:39', 0, 0, 0),
(28, NULL, 'Pat_2222', 8, 45, '2026-01-08 13:24:16', 0, 0, 0),
(29, NULL, 'Pat_2222', 6, 13, '2026-01-08 13:27:10', 0, 0, 0),
(30, NULL, 'Pat_2222', 4, 52, '2026-01-18 22:36:23', 0, 0, 0),
(31, NULL, 'Pat_2222', 3.4, 53, '2026-01-18 22:46:07', 0, 0, 6.9),
(32, NULL, 'Pat_2222', 4.6, 49, '2026-01-18 23:20:18', 0, 0, 4),
(33, NULL, 'Pat_2222', 3, 20, '2026-01-18 23:27:42', 0, 0, 6),
(34, NULL, 'Pat_2222', 2, 12, '2026-01-18 23:29:49', 0, 0, 5),
(35, NULL, 'Pat_2222', 4.9, 52, '2026-01-18 23:32:27', 0, 0, 3.1),
(36, NULL, 'Pat_2222', 7.7, 52, '2026-01-19 00:00:38', 0, 0, 6),
(37, NULL, 'Pat_2222', 5.4, 52, '2026-01-19 00:07:56', 0, 0, 2.4),
(38, NULL, 'Pat_2222', 4.4, 52, '2026-01-19 20:43:31', 0, 0, 4.8),
(39, NULL, 'Pat_2222', 5.2, 52, '2026-01-20 06:47:26', 0, 0, 3.9),
(40, NULL, 'Pat_2222', 4.1, 621, '2026-01-20 18:22:17', 0, 0, 3.1),
(41, NULL, 'Pat_2222', 2.1, 12, '2026-01-20 18:26:35', 0, 0, 1.9),
(42, NULL, 'Pat_2222', 3.3, 52, '2026-01-20 20:24:17', 0, 0, 2.9),
(43, NULL, 'Pat_2222', 3.6, 32, '2026-01-26 19:29:56', 0, 0, 3.5),
(44, NULL, 'pat_1910', 3.6, 52, '2026-01-28 08:44:00', 0, 0, 3),
(45, NULL, 'pat_1910', 2.5, 42, '2026-01-28 08:44:28', 0, 0, 2),
(46, NULL, 'pat_1910', 5.1, 32, '2026-01-28 08:45:22', 0, 0, 2.9),
(47, NULL, 'pat_1910', 3.7, 31, '2026-01-28 08:45:49', 0, 0, 4.4),
(48, NULL, 'pat_1910', 4.6, 21, '2026-01-28 08:46:27', 0, 0, 5.1),
(49, NULL, 'pat_1910', 2.7, 42, '2026-01-28 11:52:31', 0, 0, 2.9),
(50, NULL, 'pat_1910', 2.8, 23, '2026-01-28 22:42:17', 0, 0, 3.9),
(51, NULL, 'pat_1910', 3.2, 21, '2026-01-28 23:28:41', 0, 0, 4),
(52, NULL, 'pat_1910', 2.7, 56, '2026-01-30 12:13:24', 0, 0, 5.7),
(53, NULL, 'pat_1910', 5.9, 32, '2026-01-30 12:17:33', 0, 0, 2.4);

-- --------------------------------------------------------

--
-- Table structure for table `treatments`
--

CREATE TABLE `treatments` (
  `id` int(10) UNSIGNED NOT NULL,
  `patient_id` varchar(50) NOT NULL,
  `medicine_name` varchar(255) NOT NULL,
  `dose` varchar(100) DEFAULT NULL,
  `route` varchar(50) DEFAULT NULL,
  `frequency_number` varchar(20) DEFAULT NULL,
  `frequency_text` varchar(100) DEFAULT NULL,
  `duration_weeks` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `treatments`
--

INSERT INTO `treatments` (`id`, `patient_id`, `medicine_name`, `dose`, `route`, `frequency_number`, `frequency_text`, `duration_weeks`, `created_at`) VALUES
(1, 'pat_1910', 'Dolo', '200 mg', 'Tablet', '2', 'Daily', '5', '2026-01-28 17:18:41'),
(2, 'pat_1910', 'Dolo2', '200 mg', 'Tablet', '2', 'daily', '3', '2026-01-28 18:38:17'),
(3, 'pat_1910', 'Cold', 'dolo', 'Tablet', '2', 'daily', '2', '2026-01-30 06:45:30'),
(4, 'pat_1910', 'Paracetamol', '200mg', 'Injection', '2', 'once', '32', '2026-01-30 06:46:38'),
(5, 'Pat_2222', 'Dolo', '100 mg', 'Tablet', '2', 'daily', '3', '2026-01-30 07:24:07');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `comorbidities`
--
ALTER TABLE `comorbidities`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `complaints`
--
ALTER TABLE `complaints`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_patient` (`patient_id`),
  ADD KEY `idx_doctor` (`doctor_id`);

--
-- Indexes for table `doctors`
--
ALTER TABLE `doctors`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `doctor_id` (`doctor_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `investigations`
--
ALTER TABLE `investigations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_patient` (`patient_id`);

--
-- Indexes for table `medications`
--
ALTER TABLE `medications`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `patients`
--
ALTER TABLE `patients`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `patient_id` (`patient_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `mobile` (`mobile`);

--
-- Indexes for table `save_disease_scores_graph`
--
ALTER TABLE `save_disease_scores_graph`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `treatments`
--
ALTER TABLE `treatments`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `comorbidities`
--
ALTER TABLE `comorbidities`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `complaints`
--
ALTER TABLE `complaints`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `doctors`
--
ALTER TABLE `doctors`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `investigations`
--
ALTER TABLE `investigations`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `medications`
--
ALTER TABLE `medications`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `patients`
--
ALTER TABLE `patients`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `save_disease_scores_graph`
--
ALTER TABLE `save_disease_scores_graph`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=54;

--
-- AUTO_INCREMENT for table `treatments`
--
ALTER TABLE `treatments`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
