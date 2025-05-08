<script setup>
import {ref, onMounted, reactive, nextTick} from "vue";
import request from "@/utils/request";
import { ElNotification, ElMessage } from "element-plus";
import { CirclePlus, Edit, Remove, Search, Plus, Minus, Document } from "@element-plus/icons-vue";
import { debounce } from 'lodash-es';

// 响应式数据
const inventoryDialogForm = ref(false); // 新增物料的表单
const inventoryDialogTitle = ref("");
const usageDialogVisible = ref(false); // 使用情况弹窗
const currentInventoryId = ref(null); // 当前查看的物料ID
const currentInventoryName = ref(""); // 当前查看的物料名称
const usageRecords = ref([]); // 使用记录
const usageLoading = ref(false); // 使用记录加载状态
const multipleSelection = ref([]);
const itemName = ref(""); // 搜索的物料名称
const pageNum = ref(1);
const pageSize = ref(4);
const total = ref(0);
const tableData = ref([]);
const ifAdd = ref(true);
const headerBg = "headerBg";
const inventoryForm = reactive({
  itemId: "",
  itemName: "",
  quantity: 0,
  lastUpdated: ""
});
const inventoryFormRef = ref(null);

// 表单的验证规则
const rules = {
  itemName: [{ required: true, message: "请输入物料名称", trigger: "blur" }],
  quantity: [
    { required: true, message: "请输入库存数量", trigger: "blur" },
    { type: 'number', min: 0, message: '库存数量必须大于等于0', trigger: 'blur', transform: (value) => Number(value) }
  ]
};

// 加载数据
const load = debounce(() => {
  request
      .get("inventory/list", {
        params: {
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          itemName: itemName.value
        },
      })
      .then((res) => {
        console.log(res);
        if (res.data.status === 200) {
          // 检查返回的数据格式，确保tableData是数组
          if (res.data.data && res.data.data.records) {
            // 如果返回的是分页对象，取其中的records数组
            tableData.value = res.data.data.records;
            total.value = res.data.data.total || 0;
          } else if (Array.isArray(res.data.data)) {
            // 如果直接返回数组
            tableData.value = res.data.data;
            total.value = res.data.total || res.data.data.length;
          } else {
            // 其他情况，确保tableData至少是空数组
            console.warn("返回的数据格式不符合预期:", res.data);
            tableData.value = [];
            total.value = 0;
          }
        } else {
          ElMessage.error(res.data.msg || "获取物料列表失败");
          tableData.value = [];
          total.value = 0;
        }
      })
      .catch(err => {
        console.error("获取物料列表失败:", err);
        ElMessage.error("网络异常，请稍后重试");
        tableData.value = [];
        total.value = 0;
      });
}, 300);

// 添加/编辑物料
const saveInventory = async () => {
  try {
    await inventoryFormRef.value.validate(); // 直接 await validate()，它会返回一个 Promise
    const url = ifAdd.value ? "inventory/add" : "inventory/update";

    const res = await request.post(url, inventoryForm);
    if (res.data.status === 200) {
      ElNotification({ title: "成功", message: res.data.msg, type: "success" });
      inventoryDialogForm.value = false;
      load();
    } else {
      ElNotification({ title: "失败", message: res.data.msg, type: "error" });
    }
  } catch (error) {
    ElMessage.error("请按要求输入");
  }
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
  itemName.value = "";
  load();
};

// 添加新物料
const addNewInventory = () => {
  ifAdd.value = true;
  inventoryDialogForm.value = true;
  inventoryDialogTitle.value = "新增物料";
  // 清空表单数据
  Object.keys(inventoryForm).forEach(key => {
    inventoryForm[key] = key === "quantity" ? 0 : ""; // 清空所有字段，quantity默认为0
  });
  // 重置表单校验状态（避免上次校验状态保留）
  nextTick(() => {
    inventoryFormRef.value?.resetFields();
  });
};

// 编辑物料
const handleEdit = (row) => {
  ifAdd.value = false;
  Object.assign(inventoryForm, row);
  inventoryDialogForm.value = true;
  inventoryDialogTitle.value = "编辑物料";
  // 在编辑时，确保表单不会立即显示错误
  nextTick(() => {
    inventoryFormRef.value?.clearValidate();  // 清除上次编辑的验证错误
  });
};

// 删除物料
const del = (itemId) => {
  request.delete(`inventory/delete?itemId=${itemId}`).then((res) => {
    if (res.data.status === 200) {
      ElNotification({ title: "删除成功", message: "删除物料成功", type: "success" });
      load();
    } else {
      ElMessage.error(res.data.msg || "删除失败");
    }
  });
};

// 批量删除
const delBatch = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请先选择要删除的物料！");
    return;
  }
  let ids = multipleSelection.value.map((v) => v.itemId);
  request.delete("inventory/delBatch", {data: ids}).then((res) => {
    if (res.data.status === 200) {
      ElMessage.success("批量删除成功");
      load();
    } else {
      ElMessage.error(res.data.msg || "批量删除失败");
    }
  });
};

// 增加库存
const increaseQuantity = (row) => {
  const newQuantity = row.quantity + 1;
  updateQuantity(row.itemId, newQuantity);
};

// 减少库存
const decreaseQuantity = (row) => {
  if (row.quantity <= 0) {
    ElMessage.warning("库存已为0，无法减少");
    return;
  }
  const newQuantity = row.quantity - 1;
  updateQuantity(row.itemId, newQuantity);
};

// 更新库存数量
const updateQuantity = (itemId, quantity) => {
  request.post("inventory/update", {
    itemId: itemId,
    quantity: quantity
  }).then((res) => {
    if (res.data.status === 200) {
      ElMessage.success("更新库存成功");
      load();
    } else {
      ElMessage.error(res.data.msg || "更新库存失败");
    }
  });
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

// 查看物料使用情况
const viewUsage = (row) => {
  currentInventoryId.value = row.itemId;
  currentInventoryName.value = row.itemName;
  usageDialogVisible.value = true;
  loadUsageRecords();
};

// 加载使用记录
const loadUsageRecords = () => {
  if (!currentInventoryId.value) return;
  
  usageLoading.value = true;
  usageRecords.value = []; // 先清空数据，避免旧数据影响
  
  request.get(`inventory/usage/${currentInventoryId.value}`)
    .then(res => {
      console.log("获取使用记录的原始数据:", res.data);
      if (res.data.status === 200) {
        // 确保数据是数组
        usageRecords.value = Array.isArray(res.data.data) ? res.data.data : [];
        
        // 打印处理后的数据，用于调试
        console.log("处理后的使用记录数据:", usageRecords.value);
      } else {
        ElMessage.error(res.data.msg || "获取使用记录失败");
        usageRecords.value = [];
      }
    })
    .catch(err => {
      console.error("获取使用记录失败", err);
      ElMessage.error("网络异常，请稍后重试");
      usageRecords.value = [];
    })
    .finally(() => {
      usageLoading.value = false;
    });
};

// 获取工单状态中文名称
const getOrderStatusText = (status) => {
  const statusMap = {
    'pending': '待处理',
    'assigned': '已分配',
    'in_progress': '处理中',
    'completed': '已完成',
    'cancelled': '已取消'
  };
  return statusMap[status] || status;
};
// 获取工单类型中文名称
const getOrderTypeText = (type) => {
  const typeMap = {
    'nrepair': '报修',
    'complaint': '投诉'
  };
  return typeMap[type] || type;
};
onMounted(load);
</script>

<template>
  <div>
    <!-- 搜索栏 -->
    <div style="margin: 10px 0">
      <el-input v-model="itemName" style="width: 200px" placeholder="请输入物料名称" clearable>
        <template #suffix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
      <el-button type="warning" class="ml-5" @click="reset">重置</el-button>
    </div>

    <!-- 按钮操作 -->
    <div style="margin: 10px 0">
      <el-button type="primary" @click="addNewInventory">
        新增物料 <el-icon><CirclePlus /></el-icon>
      </el-button>
      <el-popconfirm
          class="ml-5"
          confirm-button-text="确定"
          cancel-button-text="我再想想"
          icon="el-icon-info"
          icon-color="red"
          title="您确定批量删除这些数据吗？"
          @confirm="delBatch"
      >
        <template #reference>
          <el-button type="danger">批量删除 <el-icon><Remove /></el-icon></el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe :header-cell-class-name="headerBg" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="itemId" label="ID" width="80" />
      <el-table-column prop="itemName" label="物料名称" width="180" />
      <el-table-column prop="quantity" label="库存数量" width="120" />
      <el-table-column label="最后更新时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.lastUpdated) }}
        </template>
      </el-table-column>
      <el-table-column label="库存操作" width="150" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" circle @click="increaseQuantity(row)">
            <el-icon><Plus /></el-icon>
          </el-button>
          <el-button type="warning" size="small" circle @click="decreaseQuantity(row)">
            <el-icon><Minus /></el-icon>
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center">
        <template #default="{ row }">
          <el-button type="success" @click="handleEdit(row)">
            编辑 <el-icon><Edit /></el-icon>
          </el-button>
          <el-button type="info" @click="viewUsage(row)">
            使用记录 <el-icon><Document /></el-icon>
          </el-button>
          <el-popconfirm
              confirm-button-text="确定"
              cancel-button-text="我再想想"
              title="您确定删除吗？"
              @confirm="del(row.itemId)"
          >
            <template #reference>
              <el-button type="danger">删除 <el-icon><Remove /></el-icon></el-button>
            </template>
          </el-popconfirm>
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
    
    <!-- 新增/编辑物料弹窗 -->
    <el-dialog :title="inventoryDialogTitle" v-model="inventoryDialogForm" width="30%">
      <el-form :model="inventoryForm" ref="inventoryFormRef" :rules="rules">
        <el-form-item label="物料名称" prop="itemName">
          <el-input v-model="inventoryForm.itemName" autocomplete="off" />
        </el-form-item>
        <el-form-item label="库存数量" prop="quantity">
          <el-input-number v-model="inventoryForm.quantity" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inventoryDialogForm = false">取 消</el-button>
        <el-button type="primary" @click="saveInventory()">确 定</el-button>
      </template>
    </el-dialog>
    
    <!-- 物料使用记录弹窗 -->
    <el-dialog 
      :title="`物料使用记录 - ${currentInventoryName}`" 
      v-model="usageDialogVisible" 
      width="80%"
      destroy-on-close
    >
      <el-table 
        :data="usageRecords" 
        border 
        stripe 
        v-loading="usageLoading"
        :header-cell-class-name="headerBg"
        row-key="key"
      >
        <el-table-column prop="inventory_use_id" label="使用记录ID" width="100" />
        <el-table-column prop="order_id" label="工单ID" width="80" />
        <el-table-column prop="order_type" label="工单类型" width="100">
          <template #default="scope">
            {{ scope.row && scope.row.order_type ? getOrderTypeText(scope.row.order_type) : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="order_description" label="工单描述" width="200" show-overflow-tooltip />
        <el-table-column prop="num" label="使用数量" width="80" />
        <el-table-column prop="order_status" label="工单状态" width="100">
          <template #default="scope">
            {{ scope.row && scope.row.order_status ? getOrderStatusText(scope.row.order_status) : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="150" show-overflow-tooltip />
        <el-table-column prop="use_time" label="使用时间" width="160">
          <template #default="scope">
            {{ scope.row && scope.row.use_time ? formatDate(scope.row.use_time) : '' }}
          </template>
        </el-table-column>
        <el-table-column label="技术员信息" width="200">
          <template #default="scope">
            <div v-if="scope.row && scope.row.employee_name">
              <div>{{ scope.row.employee_name }}</div>
              <div v-if="scope.row.employee_phone">
                <el-tag size="small" type="info">{{ scope.row.employee_phone }}</el-tag>
              </div>
            </div>
            <div v-else>
              <el-tag size="small" type="warning">未分配技术员</el-tag>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="usageDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.ml-5 {
  margin-left: 5px;
}
.headerBg {
  background-color: #eef1f6;
}
</style>