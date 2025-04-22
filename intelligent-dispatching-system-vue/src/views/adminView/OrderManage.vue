<script setup>
import { onMounted, ref} from "vue";
import request from "@/utils/request";
import {ElMessage, ElNotification} from "element-plus";
import { Edit, User, View} from "@element-plus/icons-vue";
import {debounce} from 'lodash-es';

// 响应式数据
const customerName = ref(""); // 搜索的用户名
const employeeName = ref(""); // 搜索的员工姓名
const orderDetailDialogVisible = ref(false); // 订单详情对话框
const currentOrder = ref(null); // 当前查看的订单
const pageNum = ref(1);
const pageSize = ref(4);
const total = ref(0);
const tableData = ref([]);
const headerBg = "headerBg";
const multipleSelection = ref([]);

// 当前选中的状态标签
const activeStatus = ref('all');

// 状态映射
const statusMap = {
  'pending': '待处理',
  'assigned': '已分配工人',
  'in_progress': '处理中',
  'completed': '已完成',
  'closed': '已关闭'
};

// 状态标签样式
const statusTagType = {
  'pending': 'warning',
  'assigned': 'primary',
  'in_progress': 'primary',
  'completed': 'success',
  'closed': 'info'
};

// 加载数据
const load = debounce(() => {
  request
    .get("order/repair/page", {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        customerName: customerName.value,
        employeeName: employeeName.value,
        status: activeStatus.value === 'all' ? null : activeStatus.value
      },
    })
    .then((res) => {
      tableData.value = res.data.data;
      total.value = res.data.total;
    });
}, 300);

// 处理分页
const handleSelectionChange = (rows) => {
  multipleSelection.value = rows;
};

const handleCurrentChange = (num) => {
  pageNum.value = num;
  load();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  pageNum.value = 1; // 切换每页条数时，应该回到第一页
  load();
};

// 重置表单
const reset = () => {
  customerName.value = "";
  employeeName.value = "";
  activeStatus.value = "all";
  load();
};

// 查看订单详情
const viewOrderDetail = (row) => {
  currentOrder.value = row;
  orderDetailDialogVisible.value = true;
};

// 修改订单状态
const changeOrderStatus = (orderId, newStatus) => {
  request.post("order/updateStatus", {
    orderId: orderId,
    status: newStatus
  }).then((res) => {
    if (res.data.status === 200) {
      ElNotification({ title: "成功", message: "工单状态更新成功", type: "success" });
      load();
    } else {
      ElNotification({ title: "失败", message: res.data.msg, type: "error" });
    }
  });
};

// 添加技术员选择对话框相关的响应式数据
const assignDialogVisible = ref(false);
const availableTechnicians = ref([]);
const selectedTechnician = ref(null);
const currentOrderId = ref(null);
const technicianLoading = ref(false);

// 分配技术员对话框
const assignTechnician = (orderId) => {
  currentOrderId.value = orderId;
  availableTechnicians.value = [];
  selectedTechnician.value = null;
  assignDialogVisible.value = true;
  loadAvailableTechnicians();
};

// 加载可用技术员
const loadAvailableTechnicians = () => {
  technicianLoading.value = true;
  request.get("employee/available")
    .then(res => {
      if (res.data.status === 200 && res.data.data) {
        
        availableTechnicians.value = res.data.data;
        console.log(availableTechnicians.value);
      } else {
        availableTechnicians.value = [];
        ElMessage.error(res.data.msg || "暂无可用技术员");
      }
    })
    .catch(err => {
      console.error("获取技术员列表出错:", err);
      ElMessage.error("获取技术员列表出错");
    })
    .finally(() => {
      technicianLoading.value = false;
    });
};

// 确认分配技术员
const confirmAssign = () => {
  if (!selectedTechnician.value) {
    ElMessage.warning("请选择一名技术员");
    return;
  }
  
  request.post("order/assign", {
    orderId: currentOrderId.value,
    employeeId: selectedTechnician.value
  }).then(res => {
    if (res.data.status === 200) {
      ElNotification({
        title: "成功",
        message: "技术员分配成功",
        type: "success"
      });
      assignDialogVisible.value = false;
      selectedTechnician.value = null;
      load(); // 重新加载工单列表
    } else {
      ElNotification({
        title: "失败",
        message: res.data.msg || "分配失败",
        type: "error"
      });
    }
  }).catch(err => {
    console.error("分配技术员出错:", err);
    ElMessage.error("分配技术员出错");
  });
};

// 取消分配
const cancelAssign = () => {
  assignDialogVisible.value = false;
  selectedTechnician.value = null;
};

//获取报修类型
const getRepairType = (type) => {
  console.log(type);
   const repairTypeMap = {
     1: '宽带故障',
     2: '网络卡顿',
     3: '断网问题',
     4: '路由器问题',
     5: '设备损坏',
     6: '网络延迟高',
     7: '电视信号问题',
     8: 'Wi-Fi信号弱',
     10: '5G信号差',
     11: '机房维护',
     12: '电缆故障',
     13: '远程协助请求',
     14: '设备升级'}
  return repairTypeMap[type] || '未知';
};

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString();
};

// 切换状态标签
const changeStatusTab = (status) => {
  activeStatus.value = status;
  load();
};

onMounted(load);
</script>

<template>
  <div>
    <!-- 搜索栏 -->
    <div style="margin: 10px 0">
      <el-input v-model="customerName" style="width: 200px" placeholder="请输入用户名" clearable>
        <template #suffix><el-icon><User /></el-icon></template>
      </el-input>
      <el-input v-model="employeeName" style="width: 200px" placeholder="请输入员工姓名" class="ml-5" clearable>
        <template #suffix><el-icon><User /></el-icon></template>
      </el-input>
      <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
      <el-button type="warning" class="ml-5" @click="reset">重置</el-button>
    </div>

    <!-- 状态标签 -->
    <div style="margin: 15px 0">
      <el-radio-group v-model="activeStatus" @change="changeStatusTab">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="pending">待处理</el-radio-button>
        <el-radio-button label="assigned">已分配工人</el-radio-button>
        <el-radio-button label="in_progress">处理中</el-radio-button>
        <el-radio-button label="completed">已完成</el-radio-button>
        <el-radio-button label="closed">已关闭</el-radio-button>
      </el-radio-group>
    </div>


    <!-- 表格 -->
    <el-table :data="tableData" border stripe :header-cell-class-name="headerBg" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="orderId" label="工单ID" width="80" />
      <el-table-column prop="customerName" label="用户名" width="80" />
      <el-table-column prop="repairTypeName" label="报修类型" width="90" >
        <template #default="{ row }">
          {{ getRepairType(row.repairType) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="问题描述" show-overflow-tooltip />
      <el-table-column prop="employeeName" label="技术员" width="80">
        <template #default="{ row }">
          <span v-if="row.employeeName">{{ row.employeeName }}</span>
          <span v-else class="no-technician">未分配</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.status]">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="130">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="完成时间" width="130">
        <template #default="{ row }">
          {{ formatTime(row.resolveAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <div class="operation-buttons">
            <el-button type="primary" @click="viewOrderDetail(row)" size="small" plain>
              <el-icon><View /></el-icon>查看
            </el-button>
            
            <el-dropdown v-if="row.status !== 'completed' && row.status !== 'closed'" trigger="click">
              <el-button type="success" size="small" plain>
                <el-icon><Edit /></el-icon>更改状态
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="row.status === 'pending'" @click="assignTechnician(row.orderId)">分配技术员</el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 'assigned'" @click="changeOrderStatus(row.orderId, 'in_progress')">开始处理</el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 'in_progress'" @click="changeOrderStatus(row.orderId, 'completed')">完成订单</el-dropdown-item>
                  <el-dropdown-item v-if="row.status !== 'closed'" @click="changeOrderStatus(row.orderId, 'closed')">关闭订单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[4, 5, 10, 20]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="padding: 10px 0"
    />

    <!-- 订单详情对话框 -->
    <el-dialog
        v-model="orderDetailDialogVisible"
        title="工单详情"
        width="50%"
        destroy-on-close
    >
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单ID">{{ currentOrder.orderId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentOrder.customerName }}</el-descriptions-item>
          <el-descriptions-item label="报修类型">{{ getRepairType(currentOrder.repairType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType[currentOrder.status]">{{ statusMap[currentOrder.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="技术员">{{ currentOrder.employeeName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.customerPhone }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatTime(currentOrder.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间" :span="2">{{ formatTime(currentOrder.resolveAt) }}</el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">{{ currentOrder.description }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
        </el-descriptions>
        <!-- 订单处理记录 -->
        <div v-if="currentOrder.logs && currentOrder.logs.length > 0" class="order-logs">
          <h3>处理记录</h3>
          <el-timeline>
            <el-timeline-item
                v-for="(log, index) in currentOrder.logs"
                :key="index"
                :timestamp="formatTime(log.createdAt)"
                :type="log.type"
            >
              {{ log.content }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>

    <!-- 技术员选择对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="选择技术员"
      width="50%"
      destroy-on-close
    >
      <div v-loading="technicianLoading">
        <el-alert
          v-if="availableTechnicians.length === 0 && !technicianLoading"
          title="当前没有可用的技术员"
          type="warning"
          :closable="false"
          show-icon
        />
        
        <el-radio-group v-model="selectedTechnician" class="technician-list">
          <el-card 
            v-for="tech in availableTechnicians" 
            :key="tech.employeeId"
            class="technician-card"
            :class="{ 'selected': selectedTechnician === tech.employeeId }"
            @click="selectedTechnician = tech.employeeId"
          >
            <div class="technician-info">
              <div class="technician-avatar">
                <el-avatar :size="50" icon="User" />
              </div>
              <div class="technician-details">
                <h3>{{ tech.name }}</h3>
                <div class="tech-detail-item">
                  <span>技能等级:</span>
                  <div class="skill-level">
                    <!-- 修改等级显示方式，使用更醒目的徽章样式 -->
                    <div class="level-badge">Lv.{{ tech.skillLevel }}</div>
                    
                  </div>
                </div>
                <div class="tech-detail-item">
                  <span>当前工作量:</span>
                  <div class="workload-progress">
                    <el-progress 
                      :percentage="Math.round((tech.currentWorkload / (tech.maxWorkload || 1)) * 100)" 
                      :status="tech.currentWorkload >= (tech.maxWorkload * 0.8) ? 'warning' : ''"
                    />
                    
                  </div>
                </div>
                <div class="tech-detail-item">
                  <span>联系电话:</span>
                  <span class="phone-number">{{ tech.phone || '暂无' }}</span>
                </div>
                <div class="tech-detail-item">
                  <span>当前位置:</span>
                  <span class="phone-number">{{ tech.location || '暂无' }}</span>
                </div>
              </div>
            </div>
            <el-radio :label="tech.employeeId" class="technician-radio">选择</el-radio>
          </el-card>
        </el-radio-group>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelAssign">取消</el-button>
          <el-button type="primary" @click="confirmAssign" :disabled="!selectedTechnician">
            确认分配
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.ml-5 {
  margin-left: 5px;
}

// 添加操作按钮样式
.operation-buttons {
  display: flex;
  justify-content: space-around;
  gap: 8px;
  
  .el-button {
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      margin-right: 4px;
    }
  }
}

.order-detail {
  padding: 10px;
  
  .order-images {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
      font-size: 16px;
      font-weight: bold;
    }
    
    .order-image {
      width: 100px;
      height: 100px;
      margin-right: 10px;
      margin-bottom: 10px;
      border-radius: 4px;
      cursor: pointer;
    }
  }
  
  .order-logs {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.no-technician {
  color: #909399;
  font-style: italic;
}

// 状态标签样式
.el-radio-group {
  margin-bottom: 15px;
}

// 技术员选择样式
.technician-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 10px;
  width: 100%;
}

.technician-card {
  width: calc(50% - 10px);
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  padding-bottom: 40px; /* 增加底部内边距，为单选按钮留出空间 */
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.selected {
    border: 2px solid #409EFF;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }
}

.technician-info {
  display: flex;
  align-items: flex-start;
}

.technician-avatar {
  margin-right: 15px;
}

.technician-details {
  flex: 1;
  
  h3 {
    margin-top: 0;
    margin-bottom: 10px;
    font-size: 16px;
  }
}

.tech-detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  
  > span:first-child {
    width: 80px;
    color: #606266;
    font-weight: 600 !important; // 增加权重和粗细
    display: inline-block !important; // 确保显示为块级元素
    font-size: 13px !important; // 增加字体大小
    margin-right: 8px !important; // 增加右边距
  }
  .phone-number {
    color: #333;
    font-size: 13px;
    max-width: calc(100% - 100px); /* 确保电话号码不会超出容器 */
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .skill-level {
    flex: 1;
    display: flex;
    align-items: center;

    .level-badge {
      min-width: 40px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      background-color: #409EFF;
      color: white;
      border-radius: 12px;
      font-size: 12px;
      font-weight: bold;
      padding: 0 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }
  
  .workload-progress {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .workload-text {
      font-size: 12px;
      color: #909399;
      margin-top: 2px;
    }
  }
}

.technician-radio {
  position: absolute;
  right: 15px;
  bottom: 8px;
}

</style>