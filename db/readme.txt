用户表 (users)
user_id：int 用户ID（主键，自增）
username：varchar(50) 用户名
phone：varchar(20) 手机号（唯一）
password: varchar(50) 密码
email：varchar(50) 电子邮件
address：varchar(255) 地址
role：enum 用户角色（customer=普通用户，admin=管理员，technician=技术人员）
created_at：timestamp 创建时间
account：varchar(20) 账号
员工表 (employees)
employee_id：员工ID（主键，自增）
user_id:int 用户ID（外键，关联users）
name：varchar(50) 员工姓名
phone：varchar(20) 手机号（唯一）
email：varchar(50) 电子邮件
skill_level:int 员工等级
level_point:float 等级经验
location：varchar(100)当前位置
location_latitude：float 位置维度
location_longitude：float 位置经度
current_workload：int 当前工作负载（用于派单时计算）
max_workload：int 最大工作负载
status：enum 状态（available=可用，busy=忙碌，off=离线）
技能表(skills)
skill_id
skill_name
员工技能关联表(employee_skills)
employee_id
skill_id
备料库表 (inventory)
item_id：int 备件ID（主键，自增）
item_name：varchar(100) 备件名称
quantity：int 库存数量
last_updated：timestamp 最后更新时间
备料使用表(inventory_use)
inventory_use_id：int 备料使用id
inventory_id：int 备料id
order_id：int 工单id
num: int 使用数量
created_at:timestamp 使用时间
工单表 (work_orders)
order_id：int 工单ID（主键，自增）
user_id：int 用户ID（外键，关联users）
order_type：enum 工单类型（repair=报修，complaint=投诉）
description：text 工单描述
status：enum 工单状态（pending=待处理，assigned=已分配，in_progress=进行中，completed=已完成，closed=已关闭）
location：varchar(100) 保修位置
location_latitude：float 保修位置维度
location_longitude：float 保修位置经度
created_at：timestamp 创建时间
assigned_employee：int 指派员工ID（外键，关联employees）
resolved_at：timestamp 解决时间
priority：int 优先级（数值越大表示优先级越高）
deadline：timestamp 截止时间
派单记录表 (dispatch_records)
dispatch_id：int 派单记录ID（主键，自增）
order_id：int 工单ID（外键，关联work_orders）
employee_id：int 员工ID（外键，关联employees）
dispatch_time：timestamp 派单时间
completion_time：timestamp 完成时间
智能派单员工匹配分数表
assignment_id:int 匹配ID
order_id:int 工单ID（外键，关联work_orders）
assigned_employee：int 员工ID（外键，关联employees）
matching_score:double 匹配分数
assigned_at:timestamp 生成时间
回访记录表 （feedback_records）
feedback_id：int 回访记录ID
order_id：int 工单ID（外键，关联work_orders）
employee_id：int 员工ID（外键，关联employees）
feedback_state：enum（completed已完成回访，uncompleted为未完成回访，unrated用户未评分）
satisfaction_score：int 用户评分
feedback_time：timestrap 完成回访时间
need_time：timestamp 待回访时间
设备维护表 (equipment_maintenance)
maintenance_id：int 维护记录ID（主键，自增）
equipment_name：varchar(100) 设备名称
maintenance_date：timestamp 维护日期
details：text 维护详情
employee_id:int 员工ID（外键，关联employees）
数据分析记录 (reports)
report_id：报表ID（主键，自增）
report_type：报表类型（performance=性能分析，inventory=库存分析，complaints=投诉分析）
generated_at：生成时间
report_data：报表数据（存储分析结果）

