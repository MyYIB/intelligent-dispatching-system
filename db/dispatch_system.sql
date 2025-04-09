/*
 Navicat Premium Data Transfer

 Source Server         : xc
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : dispatch_system

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 07/04/2025 20:23:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dispatch_records
-- ----------------------------
DROP TABLE IF EXISTS `dispatch_records`;
CREATE TABLE `dispatch_records`  (
  `dispatch_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NULL DEFAULT NULL,
  `employee_id` int NULL DEFAULT NULL,
  `dispatch_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `completion_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dispatch_id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `employee_id`(`employee_id` ASC) USING BTREE,
  CONSTRAINT `dispatch_records_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `work_orders` (`order_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `dispatch_records_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dispatch_records
-- ----------------------------

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee`  (
  `employee_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location_latitude` float NULL DEFAULT NULL,
  `location_longitude` float NULL DEFAULT NULL,
  `workload` int NULL DEFAULT 0,
  `skill_level` int NULL DEFAULT NULL,
  `level_point` float NULL DEFAULT NULL,
  `current_workload` int NULL DEFAULT 0,
  `max_workload` int NULL DEFAULT 10,
  `status` enum('available','busy','off') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'available',
  PRIMARY KEY (`employee_id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `employee_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employee
-- ----------------------------
INSERT INTO `employee` VALUES (1, 2, 'lm', '123', '123', '123', 123, 123, 0, 1, 1, 0, 10, 'available');

-- ----------------------------
-- Table structure for employee_skills
-- ----------------------------
DROP TABLE IF EXISTS `employee_skills`;
CREATE TABLE `employee_skills`  (
  `employee_id` int NOT NULL,
  `skill_id` int NOT NULL,
  PRIMARY KEY (`employee_id`, `skill_id`) USING BTREE,
  INDEX `skill_id`(`skill_id` ASC) USING BTREE,
  CONSTRAINT `employee_skills_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `employee_skills_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`skill_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employee_skills
-- ----------------------------

-- ----------------------------
-- Table structure for equipment_maintenance
-- ----------------------------
DROP TABLE IF EXISTS `equipment_maintenance`;
CREATE TABLE `equipment_maintenance`  (
  `maintenance_id` int NOT NULL AUTO_INCREMENT,
  `equipment_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `maintenance_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`maintenance_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of equipment_maintenance
-- ----------------------------

-- ----------------------------
-- Table structure for feedback_records
-- ----------------------------
DROP TABLE IF EXISTS `feedback_records`;
CREATE TABLE `feedback_records`  (
  `feedback_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NULL DEFAULT NULL,
  `employee_id` int NULL DEFAULT NULL,
  `feedback_state` tinyint(1) NOT NULL DEFAULT 0,
  `feedback_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `satisfaction_score` int NULL DEFAULT NULL,
  `feedback_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`feedback_id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `employee_id`(`employee_id` ASC) USING BTREE,
  CONSTRAINT `feedback_records_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `work_orders` (`order_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `feedback_records_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of feedback_records
-- ----------------------------

-- ----------------------------
-- Table structure for inventory
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory`  (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `quantity` int NOT NULL,
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory
-- ----------------------------

-- ----------------------------
-- Table structure for inventory_use
-- ----------------------------
DROP TABLE IF EXISTS `inventory_use`;
CREATE TABLE `inventory_use`  (
  `inventory_use_id` int NOT NULL AUTO_INCREMENT,
  `inventory_id` int NULL DEFAULT NULL,
  `order_id` int NULL DEFAULT NULL,
  `num` int NULL DEFAULT NULL,
  PRIMARY KEY (`inventory_use_id`) USING BTREE,
  INDEX `inventory_id`(`inventory_id` ASC) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  CONSTRAINT `inventory_use_ibfk_1` FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`item_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `inventory_use_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `work_orders` (`order_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `inventory_use_chk_1` CHECK (`num` >= 0)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_use
-- ----------------------------

-- ----------------------------
-- Table structure for repar_type
-- ----------------------------
DROP TABLE IF EXISTS `repar_type`;
CREATE TABLE `repar_type`  (
  `repar_type_id` int NOT NULL AUTO_INCREMENT,
  `repar_type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`repar_type_id`) USING BTREE,
  UNIQUE INDEX `repar_type_name`(`repar_type_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repar_type
-- ----------------------------
INSERT INTO `repar_type` VALUES (10, '5G 信号差');
INSERT INTO `repar_type` VALUES (8, 'Wi-Fi 信号弱');
INSERT INTO `repar_type` VALUES (1, '宽带故障');
INSERT INTO `repar_type` VALUES (3, '断网问题');
INSERT INTO `repar_type` VALUES (11, '机房维护');
INSERT INTO `repar_type` VALUES (12, '电缆故障');
INSERT INTO `repar_type` VALUES (7, '电视信号问题');
INSERT INTO `repar_type` VALUES (2, '网络卡顿');
INSERT INTO `repar_type` VALUES (6, '网络延迟高 ');
INSERT INTO `repar_type` VALUES (14, '设备升级');
INSERT INTO `repar_type` VALUES (5, '设备损坏');
INSERT INTO `repar_type` VALUES (9, '语音通话问题');
INSERT INTO `repar_type` VALUES (4, '路由器问题');
INSERT INTO `repar_type` VALUES (13, '远程协助请求 ');

-- ----------------------------
-- Table structure for reports
-- ----------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports`  (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `report_type` enum('performance','inventory','complaints') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `generated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `report_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`report_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reports
-- ----------------------------

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills`  (
  `skill_id` int NOT NULL AUTO_INCREMENT,
  `skill_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`skill_id`) USING BTREE,
  UNIQUE INDEX `skill_name`(`skill_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skills
-- ----------------------------
INSERT INTO `skills` VALUES (9, 'IP 配置 ');
INSERT INTO `skills` VALUES (7, '交换机调试');
INSERT INTO `skills` VALUES (8, '光纤熔接');
INSERT INTO `skills` VALUES (1, '宽带安装');
INSERT INTO `skills` VALUES (6, '服务器维护');
INSERT INTO `skills` VALUES (10, '电视机顶盒维修');
INSERT INTO `skills` VALUES (4, '线路维修');
INSERT INTO `skills` VALUES (3, '网络调试 ');
INSERT INTO `skills` VALUES (5, '设备更换');
INSERT INTO `skills` VALUES (2, '路由器配置');
INSERT INTO `skills` VALUES (11, '远程技术支持');

-- ----------------------------
-- Table structure for task_assignments
-- ----------------------------
DROP TABLE IF EXISTS `task_assignments`;
CREATE TABLE `task_assignments`  (
  `assignment_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NULL DEFAULT NULL,
  `employee_id` int NULL DEFAULT NULL,
  `matching_score` int NULL DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`assignment_id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `employee_id`(`employee_id` ASC) USING BTREE,
  CONSTRAINT `task_assignments_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `work_orders` (`order_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `task_assignments_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_assignments
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '123456',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` enum('customer','admin','technician') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'customer',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `account` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'root', '01', '123456', '56561', '南昌航空大学', 'admin', '2025-03-18 15:52:34', '01');
INSERT INTO `user` VALUES (2, 'lm', '101', '123456', '123', '南昌航空大学', 'customer', '2025-03-21 17:55:14', '101');

-- ----------------------------
-- Table structure for work_orders
-- ----------------------------
DROP TABLE IF EXISTS `work_orders`;
CREATE TABLE `work_orders`  (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `order_type` enum('nrepair','complaint') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` enum('pending','assigned','in_progress','completed','closed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location_latitude` float NULL DEFAULT NULL,
  `location_longitude` float NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `assigned_employee` int NULL DEFAULT NULL,
  `resolved_at` timestamp NULL DEFAULT NULL,
  `priority` enum('low','med','high') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'med',
  `deadline` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `repair_type` int NULL DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `work_orders_ibfk_2`(`assigned_employee` ASC) USING BTREE,
  INDEX `work_orders_ibfk_3`(`repair_type` ASC) USING BTREE,
  INDEX `work_orders_ibfk_1`(`user_id` ASC) USING BTREE,
  CONSTRAINT `work_orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `work_orders_ibfk_2` FOREIGN KEY (`assigned_employee`) REFERENCES `employee` (`employee_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_orders
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
