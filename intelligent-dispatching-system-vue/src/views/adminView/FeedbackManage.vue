<script setup>
import { onMounted, ref } from "vue";
import request from "@/utils/request";
import { ElNotification } from "element-plus";
import { Edit, User, View } from "@element-plus/icons-vue";
import { debounce } from 'lodash-es';

// 响应式数据
const customerName = ref(""); // 搜索的用户名
const employeeName = ref(""); // 搜索的员工姓名
const feedbackDetailDialogVisible = ref(false); // 回访详情对话框
const currentFeedback = ref(null); // 当前查看的回访
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
  'uncompleted': '待回访',
  'completed': '已回访',
  'unrated': '待评价'
};

// 状态标签样式
const statusTagType = {
  'uncompleted': 'warning',
  'unrated': 'primary',
  'completed': 'success',
};

// 加载数据
const load = debounce(() => {
  request
    .get("order/feedback/page", {
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

// 查看回访详情
const viewFeedbackDetail = (row) => {
  currentFeedback.value = row;
  feedbackDetailDialogVisible.value = true;
};

// 修改回访状态
const changeFeedbackStatus = (feedbackId, newStatus) => {
  request.post("order/feedback/updateStatus", {
    feedbackId: feedbackId,
    status: newStatus
  }).then((res) => {
    if (res.data.status === 200) {
      ElNotification({ title: "成功", message: "回访状态更新成功", type: "success" });
      load();
    } else {
      ElNotification({ title: "失败", message: res.data.msg, type: "error" });
    }
  });
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



// 提醒员工回访
const remindEmployee = (feedbackId) => {
  request.post("order/feedback/remind", {
    feedbackId: feedbackId
  }).then((res) => {
    if (res.data.status === 200) {
      ElNotification({ title: "成功", message: "已发送提醒", type: "success" });
    } else {
      ElNotification({ title: "失败", message: res.data.msg, type: "error" });
    }
  });
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
        <el-radio-button label="uncompleted">待回访</el-radio-button>
        <el-radio-button label="completed">已回访</el-radio-button>
        <el-radio-button label="unrated">待评价</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe :header-cell-class-name="headerBg" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="feedbackId" label="回访ID" width="80" />
      <el-table-column prop="orderId" label="工单ID" width="80" />
      <el-table-column prop="customerName" label="用户名" width="80" />
      <el-table-column prop="employeeName" label="技术员" width="80" />
      <el-table-column prop="feedbackState" label="回访状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.feedbackState]">{{ statusMap[row.feedbackState] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="satisfactionScore" label="满意度评分" width="150">
        <template #default="{ row }">
          <div v-if="row.satisfactionScore">
            <el-rate v-model="row.satisfactionScore" disabled show-score text-color="#ff9900" />
          </div>
          <span v-else>未评分</span>
        </template>
      </el-table-column>
      <el-table-column prop="needTime" label="待回访时间" width="130">
        <template #default="{ row }">
          {{ formatTime(row.needTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="feedbackTime" label="回访时间" width="130">
        <template #default="{ row }">
          {{ formatTime(row.feedbackTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <div class="operation-buttons">
            <el-button type="primary" @click="viewFeedbackDetail(row)" size="small" plain>
              <el-icon><View /></el-icon>查看
            </el-button>
            
            <el-dropdown v-if="row.feedbackState === 'uncompleted'" trigger="click">
              <el-button type="success" size="small" plain>
                <el-icon><Edit /></el-icon>操作
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="row.feedbackState === 'uncompleted'" @click="changeFeedbackStatus(row.feedbackId, 'unrated')">完成回访</el-dropdown-item>
                  <el-dropdown-item v-if="row.feedbackState === 'uncompleted'" @click="remindEmployee(row.feedbackId)">提醒员工</el-dropdown-item>
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

    <!-- 回访详情对话框 -->
    <el-dialog
        v-model="feedbackDetailDialogVisible"
        title="回访详情"
        width="50%"
        destroy-on-close
    >
      <div v-if="currentFeedback" class="feedback-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="回访ID">{{ currentFeedback.feedbackId }}</el-descriptions-item>
          <el-descriptions-item label="工单ID">{{ currentFeedback.orderId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentFeedback.customerName }}</el-descriptions-item>
          <el-descriptions-item label="技术员">{{ currentFeedback.employeeName }}</el-descriptions-item>
          <el-descriptions-item label="回访状态">
            <el-tag :type="statusTagType[currentFeedback.feedbackState]">{{ statusMap[currentFeedback.feedbackState] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="满意度评分">
            <div v-if="currentFeedback.satisfactionScore">
              <el-rate v-model="currentFeedback.satisfactionScore" disabled show-score text-color="#ff9900" />
            </div>
            <span v-else>未评分</span>
          </el-descriptions-item>
          <el-descriptions-item label="待回访时间">{{ formatTime(currentFeedback.needTime) }}</el-descriptions-item>
          <el-descriptions-item label="回访时间">{{ formatTime(currentFeedback.feedbackTime) }}</el-descriptions-item>
          <el-descriptions-item label="回访内容" :span="2">{{ currentFeedback.feedbackText || '暂无回访内容' }}</el-descriptions-item>
          <el-descriptions-item label="工单描述" :span="2">{{ currentFeedback.orderDescription || '暂无工单描述' }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 工单信息 -->
        <div v-if="currentFeedback.orderInfo" class="order-info">
          <h3>工单信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="工单类型">{{ currentFeedback.orderInfo.orderType }}</el-descriptions-item>
            <el-descriptions-item label="工单状态">{{ statusMap[currentFeedback.orderInfo.status] }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(currentFeedback.orderInfo.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ formatTime(currentFeedback.orderInfo.resolvedAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
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

.feedback-detail {
  padding: 10px;
  
  .order-info {
    margin-top: 20px;
    
    h3 {
      margin-bottom: 10px;
      font-size: 16px;
      font-weight: bold;
    }
  }
}

// 状态标签样式
.el-radio-group {
  margin-bottom: 15px;
}

// 满意度评分样式
.el-rate {
  display: inline-flex;
  align-items: center;
}
</style>