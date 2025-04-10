<script setup>
import {ref, onMounted, reactive, nextTick} from "vue";
import request from "@/utils/request";
import { ElNotification, ElMessage } from "element-plus";
import { CirclePlus, Edit, Remove,Phone,Search,Location,Upload,Download } from "@element-plus/icons-vue";
import { debounce } from 'lodash-es';
// 响应式数据
const userDialogForm = ref(false); // 新增用户的表单
const userDialogTitle = ref("");
const multipleSelection = ref([]);
const userName = ref(""); // 搜索的姓名
const userPhone = ref(""); // 搜索的电话
const userAddress = ref(""); // 搜索的地址
const pageNum = ref(1);
const pageSize = ref(4);
const total = ref(0);
const tableData = ref([]);
const ifAdd = ref(true);
const headerBg = "headerBg";
const UserForm = reactive({
  username: "",
  phone: "",
  email: "",
  address: "",
});
const UserFormRef = ref(null);
// 表单的验证规则
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入用户手机", trigger: "blur" }],
  email: [{ required: true, message: "请输入用户邮箱", trigger: "blur" }],
  address: [{ required: true, message: "请输入用户地址", trigger: "blur" }],
};
const baseURL =  "http://localhost:8081";

// 加载数据
const load = debounce(() => {
  request
      .get("user/page", {
        params: {
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          username: userName.value,
          phone: userPhone.value,
          address: userAddress.value,
        },
      })
      .then((res) => {
        console.log(res);
        tableData.value = res.data.data;
        total.value = res.data.total;
      });
},300);

// 添加/编辑
const newUser = async () => {
  try {
    await UserFormRef.value.validate(); // 直接 await validate()，它会返回一个 Promise
    const url = ifAdd.value ? "user/addNewUser" : "user/updateUser";

    const res = await request.post(url, UserForm);
    if (res.data.status === 200) {
      ElNotification({ title: "成功", message: res.data.msg, type: "success" });
      userDialogForm.value = false;
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
}
// 重置表单
const reset = () => {
  userName.value = "";
  userPhone.value = "";
  userAddress.value = "";
  load();
};

// 添加新用户
const addNewUser = () => {
  ifAdd.value = true;
  userDialogForm.value = true;
  userDialogTitle.value = "新增用户";
  // 清空表单数据
  Object.keys(UserForm).forEach(key => {
    UserForm[key] = ""; // 清空所有字段
  });
  // 重置表单校验状态（避免上次校验状态保留）
  nextTick(() => {
    UserFormRef.value?.resetFields();
  });
};

// 编辑
const handleEdit = (row) => {
  ifAdd.value = false;
  Object.assign(UserForm, row);
  userDialogForm.value = true;
  userDialogTitle.value = "编辑用户";
  // 在编辑时，确保表单不会立即显示错误
  nextTick(() => {
    UserFormRef.value?.clearValidate();  // 清除上次编辑的验证错误
  });
};

// 删除用户
const del = (id) => {
  request.delete(`user/delete?id=${id}`).then((res) => {
    if (res.data.status === 200) {
      ElNotification({ title: "删除成功", message: "删除用户成功", type: "success" });
      load();
    } else {
      ElMessage.error("删除失败");
    }
  });
};

// 批量删除
const delBatch = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请先选择要删除的用户！");
    return;
  }
  let ids = multipleSelection.value.map((v) => v.id);
  request.delete("user/delBatch", {ids}).then((res) => {
    if (res.data.status === 200) {
      ElMessage.success("批量删除成功");
      load();
    } else {
      ElMessage.error("批量删除失败");
    }
  });
};

//导入文件
const handleExcelImportSuccess = () =>{
  ElMessage.success("导入文件成功")
  load();
}
//导出文件
const exportExcel = () =>{
  window.open(`${baseURL}/user/export`);
}
onMounted(load);
</script>

<template>
  <div>
    <!-- 搜索栏 -->
    <div style="margin: 10px 0">
      <el-input v-model="userName" style="width: 200px" placeholder="请输入用户名" clearable>
        <template #suffix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input v-model="userPhone" style="width: 200px" placeholder="请输入电话" class="ml-5" clearable>
        <template #suffix><el-icon><Phone /></el-icon></template>
      </el-input>
      <el-input v-model="userAddress" style="width: 200px" placeholder="请输入地址" class="ml-5" clearable>
        <template #suffix><el-icon><Location /></el-icon></template>
      </el-input>
      <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
      <el-button type="warning" class="ml-5" @click="reset">重置</el-button>
    </div>

    <!-- 按钮操作 -->
    <div style="margin: 10px 0">
      <el-button type="primary" @click="addNewUser">
        新增 <el-icon><CirclePlus /></el-icon>
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
      <el-upload
          :action='`${baseURL}/user/import`'
          :show-file-list="false"
          accept="xlsx"
          :on-success="handleExcelImportSuccess"
          style="display: inline-block"
      >
        <el-button type="primary" class="ml-5">导入 <el-icon><Download /></el-icon></el-button>
      </el-upload>
      <el-button type="primary" @click="exportExcel" class="ml-5">导出 <el-icon><Upload /></el-icon></el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe :header-cell-class-name="headerBg" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="address" label="地址" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button type="success" @click="handleEdit(row)">
            编辑 <el-icon><Edit /></el-icon>
          </el-button>
          <el-popconfirm
              confirm-button-text="确定"
              cancel-button-text="我再想想"
              title="您确定删除吗？"
              @confirm="del(row.userId)"
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
    <!-- 新增/编辑用户弹窗 -->
    <el-dialog :title="userDialogTitle" v-model="userDialogForm" width="30%">
      <el-form :model="UserForm" ref="UserFormRef" :rules="rules">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="UserForm.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="用户电话" prop="phone">
          <el-input v-model="UserForm.phone" autocomplete="off" />
        </el-form-item>
        <el-form-item label="用户邮箱" prop="email">
          <el-input v-model="UserForm.email" autocomplete="off" />
        </el-form-item>
        <el-form-item label="用户地址" prop="address">
          <el-input v-model="UserForm.address" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogForm = false">取 消</el-button>
        <el-button type="primary" @click="newUser()">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
