用户表 (users)
user_id：用户ID（主键，自增）
username：用户名
phone：手机号（唯一）
email：电子邮件
address：地址
role：用户角色（customer=普通用户，admin=管理员，technician=技术人员）
created_at：创建时间
员工表 (employees)
employee_id：员工ID（主键，自增）
name：员工姓名
phone：手机号（唯一）
email：电子邮件
skill_set：技能集（文本存储多个技能）
skill_level ENUM('beginner', 'intermediate', 'expert') [default: 'beginner']:员工
location：当前位置
workload：工作负载（用于派单时计算）
status：状态（available=可用，busy=忙碌，off=离线）
备料库表 (inventory)
item_id：备件ID（主键，自增）
item_name：备件名称
quantity：库存数量
last_updated：最后更新时间
工单表 (work_orders)
order_id：工单ID（主键，自增）
user_id：用户ID（外键，关联users）
order_type：工单类型（repair=报修，complaint=投诉）
description：工单描述
status：工单状态（pending=待处理，assigned=已分配，in_progress=进行中，completed=已完成，closed=已关闭）
created_at：创建时间
assigned_employee：指派员工ID（外键，关联employees）
resolved_at：解决时间
派单记录表 (dispatch_records)
dispatch_id：派单记录ID（主键，自增）
order_id：工单ID（外键，关联work_orders）
employee_id：员工ID（外键，关联employees）
dispatch_time：派单时间
completion_time：完成时间
feedback_score：用户反馈评分（1-5分）
设备维护表 (equipment_maintenance)
maintenance_id：维护记录ID（主键，自增）
equipment_name：设备名称
maintenance_date：维护日期
details：维护详情
数据分析记录 (reports)
report_id：报表ID（主键，自增）
report_type：报表类型（performance=性能分析，inventory=库存分析，complaints=投诉分析）
generated_at：生成时间
report_data：报表数据（存储分析结果）