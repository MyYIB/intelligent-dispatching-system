<script setup>
import { onMounted, ref } from "vue";
import request from "@/utils/request";
import { ElMessage, ElNotification } from "element-plus";
import { Edit, Delete, Plus, Search } from "@element-plus/icons-vue";

// 响应式数据
const equipmentName = ref(""); // 搜索的设备名称
const employeeName = ref(""); // 搜索的员工姓名
const maintenanceDialogVisible = ref(false); // 设备维护对话框
const currentMaintenance = ref({}); // 当前编辑的设备维护
const dialogTitle = ref("添加设备维护"); // 对话框标题
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tableData = ref([]);
const headerBg = "headerBg";
const multipleSelection = ref([]);

// 当前选中的状态标签
const activeStatus = ref('all');

// 状态映射
const statusMap = {
  'pending': '待处理',
  'in_progress': '处理中',
  'completed': '已完成'
};

// 状态标签样式
const statusTagType = {
  'pending': 'warning',
  'in_progress': 'primary',
  'completed': 'success'
};

// 加载数据
const load = () => {
  request
    .get("order/maintenance/page", {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        equipmentName: equipmentName.value,
        employeeName: employeeName.value,
        status: activeStatus.value === 'all' ? null : activeStatus.value
      },
    })
    .then((res) => {
      tableData.value = res.data.data;
      total.value = res.data.total;
    });
};

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
  equipmentName.value = "";
  employeeName.value = "";
  activeStatus.value = "all";
  load();
};

// 打开添加设备维护对话框
const handleAdd = () => {
  dialogTitle.value = "添加设备维护";
  currentMaintenance.value = {
    equipmentName: "",
    maintenanceDate: "",
    details: "",
    employeeId: null,
    status: "pending"
  };
  maintenanceDialogVisible.value = true;
};

// 打开编辑设备维护对话框
const handleEdit = (row) => {
  dialogTitle.value = "编辑设备维护";
  currentMaintenance.value = { ...row };
  maintenanceDialogVisible.value = true;
};

// 删除设备维护
const handleDelete = (id) => {
  ElMessage.confirm("确定要删除该设备维护记录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    request.delete(`order/maintenance/delete/${id}`).then((res) => {
      if (res.data.status === 200) {
        ElNotification({
          title: "成功",
          message: "删除成功",
          type: "success",
        });
        load(); // 重新加载数据
      } else {
        ElNotification({
          title: "失败",
          message: res.data.msg,
          type: "error",
        });
      }
    });
  });
};

// 批量删除
const handleBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请至少选择一条记录");
    return;
  }
  
  ElMessage.confirm(`确定要删除选中的 ${multipleSelection.value.length} 条记录吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    const ids = multipleSelection.value.map(item => item.maintenanceId);
    request.post("order/maintenance/batchDelete", ids).then((res) => {
      if (res.data.status === 200) {
        ElNotification({
          title: "成功",
          message: "批量删除成功",
          type: "success",
        });
        load(); // 重新加载数据
      } else {
        ElNotification({
          title: "失败",
          message: res.data.msg,
          type: "error",
        });
      }
    });
  });
};

// 提交设备维护表单
const submitForm = () => {
  // 表单验证
  if (!currentMaintenance.value.equipmentName) {
    ElMessage.warning("请输入设备名称");
    return;
  }
  
  if (!currentMaintenance.value.maintenanceDate) {
    ElMessage.warning("请选择维护日期");
    return;
  }
  
  if (!currentMaintenance.value.details) {
    ElMessage.warning("请输入维护详情");
    return;
  }
  
  if (!currentMaintenance.value.employeeId) {
    ElMessage.warning("请选择负责员工");
    return;
  }
  
  // 判断是新增还是编辑
  const url = currentMaintenance.value.maintenanceId 
    ? "order/maintenance/update" 
    : "order/maintenance/create";
  
  request.post(url, currentMaintenance.value).then((res) => {
    if (res.data.status === 200) {
      ElNotification({
        title: "成功",
        message: currentMaintenance.value.maintenanceId ? "更新成功" : "添加成功",
        type: "success",
      });
      maintenanceDialogVisible.value = false;
      load(); // 重新加载数据
    } else {
      ElNotification({
        title: "失败",
        message: res.data.msg,
        type: "error",
      });
    }
  });
};

// 更新设备维护状态
const updateStatus = (id, status) => {
  request.post("order/maintenance/updateStatus", {
    maintenanceId: id,
    status: status
  }).then((res) => {
    if (res.data.status === 200) {
      ElNotification({
        title: "成功",
        message: "状态更新成功",
        type: "success",
      });
      load(); // 重新加载数据
    } else {
      ElNotification({
        title: "失败",
        message: res.data.msg,
        type: "error",
      });
    }
  });
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 切换状态标签
const changeStatusTab = (status) => {
  activeStatus.value = status;
  load();
};

// 员工列表
const employeeList = ref([]);

// 获取员工列表
const fetchEmployeeList = () => {
  request.get("employee/list").then((res) => {
    if (res.data.status === 200) {
      employeeList.value = res.data.data;
      console.log(employeeList.value)
    }
  });
};

onMounted(() => {
  load();
  fetchEmployeeList();
});
</script>

<template>
  <div>
    <div style="margin: 10px 0">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增
      </el-button>
      <el-button type="danger" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon>批量删除
      </el-button>
    </div>
    
    <!-- 搜索栏 -->
    <div style="margin: 10px 0">
      <el-input v-model="equipmentName" style="width: 200px" placeholder="请输入设备名称" clearable>
        <template #suffix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input v-model="employeeName" style="width: 200px" placeholder="请输入员工姓名" class="ml-5" clearable>
        <template #suffix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
      <el-button type="warning" class="ml-5" @click="reset">重置</el-button>
    </div>

    <!-- 状态标签 -->
    <div style="margin: 15px 0">
      <el-radio-group v-model="activeStatus" @change="changeStatusTab">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="pending">待处理</el-radio-button>
        <el-radio-button label="in_progress">处理中</el-radio-button>
        <el-radio-button label="completed">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe :header-cell-class-name="headerBg" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="maintenanceId" label="维护ID" width="80" />
      <el-table-column prop="equipmentName" label="设备名称" width="150" />
      <el-table-column prop="employeeName" label="负责员工" width="120" />
      <el-table-column prop="status" label="维护状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.status]">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="maintenanceDate" label="计划维护日期" width="150">
        <template #default="{ row }">
          {{ formatDate(row.maintenanceDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="details" label="维护详情">
        <template #default="{ row }">
          <el-tooltip :content="row.details" placement="top" :show-after="500">
            <div class="details-text">{{ row.details }}</div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center">
        <template #default="{ row }">
          <el-button type="primary" @click="handleEdit(row)" size="small">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button type="danger" @click="handleDelete(row.maintenanceId)" size="small">
            <el-icon><Delete /></el-icon>删除
          </el-button>
          <el-dropdown v-if="row.status !== 'completed'" trigger="click">
            <el-button type="success" size="small">
              状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="row.status === 'pending'" @click="updateStatus(row.maintenanceId, 'in_progress')">开始处理</el-dropdown-item>
                <el-dropdown-item v-if="row.status === 'in_progress'" @click="updateStatus(row.maintenanceId, 'completed')">完成维护</el-dropdown-item>
                <el-dropdown-item v-if="row.status === 'completed'" @click="updateStatus(row.maintenanceId, 'pending')">重新处理</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="padding: 10px 0"
    />

    <!-- 设备维护对话框 -->
    <el-dialog
        v-model="maintenanceDialogVisible"
        :title="dialogTitle"
        width="50%"
        destroy-on-close
    >
      <el-form :model="currentMaintenance" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="currentMaintenance.equipmentName" placeholder="请输入设备名称" />
        </el-form-item>
        
        <el-form-item label="维护日期">
          <el-date-picker
            v-model="currentMaintenance.maintenanceDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="负责员工">
          <el-select v-model="currentMaintenance.employeeId" placeholder="请选择负责员工" style="width: 100%">
            <el-option
              v-for="item in employeeList"
              :key="item.employeeId"
              :label="item.name"
              :value="item.employeeId"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="维护状态">
          <el-select v-model="currentMaintenance.status" placeholder="请选择维护状态" style="width: 100%">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="维护详情">
          <el-input
            v-model="currentMaintenance.details"
            type="textarea"
            :rows="4"
            placeholder="请输入维护详情"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="maintenanceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.ml-5 {
  margin-left: 5px;
}

.headerBg {
  background-color: #f8f8f9 !important;
}

.details-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

/* 状态标签样式 */
.el-radio-group {
  margin-bottom: 15px;
}
</style>