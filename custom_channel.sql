-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 31, 2021 at 08:43 PM
-- Server version: 10.4.19-MariaDB
-- PHP Version: 8.0.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `custom_channel`
--

-- --------------------------------------------------------

--
-- Table structure for table `channel`
--

CREATE TABLE `channel` (
  `guild` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `channel` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `leader` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `config`
--

CREATE TABLE `config` (
  `guild` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `category` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `textChannel` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `voiceChannel` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `prefix` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '>',
  `link` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `language` int(2) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `join_timer`
--

CREATE TABLE `join_timer` (
  `guildId` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `userId` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `time` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE `messages` (
  `guild` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `channel` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `message` varchar(18) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `channel`
--
ALTER TABLE `channel`
  ADD PRIMARY KEY (`channel`);

--
-- Indexes for table `config`
--
ALTER TABLE `config`
  ADD PRIMARY KEY (`guild`),
  ADD UNIQUE KEY `config_guild_uindex` (`guild`),
  ADD UNIQUE KEY `config_category_uindex` (`category`),
  ADD UNIQUE KEY `config_textChannel_uindex` (`textChannel`),
  ADD UNIQUE KEY `config_voiceChannel_uindex` (`voiceChannel`);

--
-- Indexes for table `join_timer`
--
ALTER TABLE `join_timer`
  ADD UNIQUE KEY `user_time` (`userId`,`time`);

--
-- Indexes for table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`guild`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
