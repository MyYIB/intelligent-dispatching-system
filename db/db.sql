CREATE TABLE `user` (  
  `user_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `username` VARCHAR(50) NOT NULL,  
  `phone` VARCHAR(20) UNIQUE NOT NULL,  
  `password` VARCHAR(50) NOT NULL DEFAULT '123456',  
  `email` VARCHAR(100),  
  `address` VARCHAR(255),  
  `role` ENUM('customer', 'admin', 'technician') NOT NULL DEFAULT 'customer',  
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
);  

CREATE TABLE `employee` (  
  `employee_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `user_id` INT NOT NULL,  
  `skill_set` TEXT,  
  `location` VARCHAR(100),  
  `location_latitude` FLOAT,  
  `location_longitude` FLOAT,  
  `workload` INT DEFAULT 0,  
  `skill_level` ENUM('beginner', 'intermediate', 'expert') DEFAULT 'beginner',  
  `current_workload` INT DEFAULT 0,  
  `max_workload` INT DEFAULT 10,  
  `status` ENUM('available', 'busy', 'off') DEFAULT 'available',  
  FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE  
);  

CREATE TABLE `inventory` (  
  `item_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `item_name` VARCHAR(100) NOT NULL,  
  `quantity` INT NOT NULL,  
  `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
);  

CREATE TABLE `work_orders` (  
  `order_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `user_id` INT,  
  `order_type` ENUM('nrepair', 'complaint') NOT NULL,  
  `description` TEXT,  
  `status` ENUM('pending', 'assigned', 'in_progress', 'completed', 'closed') DEFAULT 'pending',  
  `location` VARCHAR(100),  
  `location_latitude` FLOAT,  
  `location_longitude` FLOAT,  
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
  `assigned_employee` INT,  
  `resolved_at` TIMESTAMP,  
  `priority` ENUM('low', 'med', 'high') DEFAULT 'med',  
  `deadline` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
  FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,  
  FOREIGN KEY (`assigned_employee`) REFERENCES `employee` (`employee_id`) ON DELETE SET NULL  
);  

CREATE TABLE `inventory_use` (  
  `inventory_use_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `inventory_id` INT,  
  `order_id` INT,  
  `num` INT CHECK (`num` >= 0),  
  FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`item_id`) ON DELETE CASCADE,  
  FOREIGN KEY (`order_id`) REFERENCES `work_orders` (`order_id`) ON DELETE CASCADE  
);  

CREATE TABLE `dispatch_records` (  
  `dispatch_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `order_id` INT,  
  `employee_id` INT,  
  `dispatch_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
  `completion_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
  FOREIGN KEY (`order_id`) REFERENCES `work_orders` (`order_id`) ON DELETE CASCADE,  
  FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE CASCADE  
);  

CREATE TABLE `feedback_records` (  
  `feedback_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `order_id` INT,  
  `employee_id` INT,  
  `feedback_state` BOOL NOT NULL DEFAULT false,  
  `feedback_text` TEXT,  
  `satisfaction_score` INT,  
  `feedback_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
  FOREIGN KEY (`order_id`) REFERENCES `work_orders` (`order_id`) ON DELETE CASCADE,  
  FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE CASCADE  
);  

CREATE TABLE `equipment_maintenance` (  
  `maintenance_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `equipment_name` VARCHAR(100) NOT NULL,  
  `maintenance_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
  `details` TEXT  
);  

CREATE TABLE `reports` (  
  `report_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `report_type` ENUM('performance', 'inventory', 'complaints') NOT NULL,  
  `generated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
  `report_data` TEXT  
);  

CREATE TABLE `task_assignments` (  
  `assignment_id` INT PRIMARY KEY AUTO_INCREMENT,  
  `order_id` INT,  
  `employee_id` INT,  
  `matching_score` INT,  
  `assigned_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  
  FOREIGN KEY (`order_id`) REFERENCES `work_orders` (`order_id`) ON DELETE CASCADE,  
  FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE CASCADE  
);  