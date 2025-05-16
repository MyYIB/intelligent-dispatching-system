<script setup>
import {nextTick, onMounted, reactive, ref} from "vue";
import request from "@/utils/request";
import {ElMessage, ElNotification} from "element-plus";
import {CirclePlus, Edit, List, Location, Phone, Remove, Search} from "@element-plus/icons-vue";
import {debounce} from 'lodash-es';
// 响应式数据
const employeeName = ref(""); // 搜索的姓名
const employeePhone = ref(""); // 搜索的电话
const userDialogForm = ref(false); // 新增用户的表单
const userDialogTitle = ref("");
const multipleSelection = ref([]);
const pageNum = ref(1);
const pageSize = ref(4);
const total = ref(0);
const tableData = ref([]);
const ifAdd = ref(true);
const headerBg = "headerBg";
const UserForm = reactive({
  name: "",
  phone: "",
  email: "",
  address: "",
  skillLevel: "",
  skills: [],
  status: "off",
});
const UserFormRef = ref(null);
const rules = {
  name: [{ required: true, message: "请输入员工名称", trigger: "blur" }],
  phone: [{ required: true, message: "请输入员工手机", trigger: "blur" }],
  email: [{ required: true, message: "请输入员工邮箱", trigger: "blur" }],
  address: [{ required: true, message: "请输入员工地址", trigger: "blur" }],
};
// 所有技能数据（初始为空）
const skillsList = ref([]);
//等级选项
const levelOptions = ref([1,2,3,4,5,6,7,8,9,10]);
// 员工状态选项
const statusOptions = ref([
  { value: 'available', label: '可分配工作' },
  { value: 'busy', label: '忙碌' },
  { value: 'off', label: '下班' }
]);
// 保存员工技能
const employeeSkills = ref({});
// 地图对话框控制变量和当前选中的位置
const mapDialogVisible = ref(false);
const currentLocation = ref(null);
// 加载数据
const load = debounce(() => {
  request
      .get("employee/page", {
        params: {
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          username: employeeName.value,
          phone: employeePhone.value,
        },
      })
      .then((res) => {
        tableData.value = res.data.data.map(employee => ({
          ...employee,
          skills: Array.isArray(employee.skills) ? employee.skills : []
        }));
        total.value = res.data.total;
      });
},300);

// 添加/编辑
const newUser = async () => {
  try {
    await UserFormRef.value.validate(); // 直接 await validate()，它会返回一个 Promise
    const url = ifAdd.value ? "employee/addNewEmployee" : "employee/updateEmployee";
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
  console.log(UserForm)
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
  employeeName.value = "";
  employeePhone.value = "";
  load();
};
// 添加新员工
const addNewUser = () => {
  ifAdd.value = true;
  userDialogForm.value = true;
  userDialogTitle.value = "新增员工";
  // 清空表单数据
  Object.assign(UserForm, {
    username: "",
    phone: "",
    email: "",
    address: "",
    skillLevel: "",
    skills: [], 
    status: "available",
  });
  // 重置表单校验状态（避免上次校验状态保留）
  nextTick(() => {
    UserFormRef.value?.resetFields();
  });
};

// 编辑
const handleEdit = (row) => {
  ifAdd.value = false;
  
  // 先获取员工的技能列表
  request.get(`employee/getEmployeeSkills?employeeId=${row.employeeId}`, {})
    .then((res) => {
      if (res.data.status === 200) {
        // 将技能ID转换为穿梭框需要的格式
        const skillIds = res.data.data.map(skill => skill.skillId);
        
        // 确保技能列表已加载
        if (skillsList.value.length === 0) {
          loadSkills().then(() => {
            // 设置表单数据
            const formData = {
              ...row,
              skills: skillIds || [] // 使用获取到的技能ID列表
            };
            Object.assign(UserForm, formData);
          });
        } else {
          // 设置表单数据
          const formData = {
            ...row,
            skills: skillIds || [] // 使用获取到的技能ID列表
          };
          Object.assign(UserForm, formData);
        }
        
        userDialogForm.value = true;
        userDialogTitle.value = "编辑员工";
        
        // 在编辑时，确保表单不会立即显示错误
        nextTick(() => {
          UserFormRef.value?.clearValidate();  // 清除上次编辑的验证错误
        });
      } else {
        ElMessage.error(res.data.msg || "获取员工技能失败");
      }
    })
    .catch(error => {
      console.error("获取员工技能出错:", error);
      ElMessage.error("获取员工技能出错");
    });
};

// 删除用户
const del = (id) => {
  request.delete(`employee/delete?id=${id}`).then((res) => {
    if (res.data.status === 200) {
      ElNotification({ title: "删除成功", message: "删除员工成功", type: "success" });
      load();
    } else {
      ElMessage.error("删除失败");
    }
  });
};

// 批量删除
const delBatch = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请先选择要删除的员工！");
    return;
  }
  let ids = multipleSelection.value.map((v) => v.id);
  request.delete("employee/delBatch", {ids}).then((res) => {
    if (res.data.status === 200) {
      ElMessage.success("批量删除成功");
      load();
    } else {
      ElMessage.error("批量删除失败");
    }
  });
};


//展示员工技能
const loadEmployeeSkills =(id) => {
  if(!employeeSkills.value[id]){
    request.get(`employee/getEmployeeSkills?employeeId=${id}`,{})
        .then((res) => {
          if (res.data.status === 200) {
            const skills = res.data.data.map(skill => skill.skillName);
            employeeSkills.value[id] = skills;
          }else{
            ElMessage.error(res.data.msg)
          }
        })
  }else {
    return null
  }
}
//展示员工等级经验
const showLevel = (row) =>{
  return row.levelPoint%100
}

// 展示位置
// 添加地图相关变量
const map = ref(null);
const marker = ref(null);
// 对话框关闭处理函数
const handleDialogClose = () => {
  if (map.value) {
    map.value.destroy();
    map.value = null;
  }
  marker.value = null;
};
// 展示位置方法
const showLocation = async (row) => {
  if (row.locationLatitude && row.locationLongitude) {

    console.log('位置数据:', row.locationLatitude, row.locationLongitude);
    currentLocation.value = {
      latitude: row.locationLatitude,
      longitude: row.locationLongitude,
      name: row.name
    };
    mapDialogVisible.value = true;

    // 等待对话框渲染完成
    await nextTick();
    await new Promise(resolve => setTimeout(resolve, 100)); // 添加短暂延迟

    try {
      // 每次都重新创建地图实例
      if (map.value) {
        map.value.destroy();
        map.value = null;
      }

      const container = document.getElementById('container');
      if (!container) {
        throw new Error('地图容器不存在');
      }

      // 确保经纬度是数值类型
      const lng = parseFloat(currentLocation.value.longitude);
      const lat = parseFloat(currentLocation.value.latitude);

      if (isNaN(lng) || isNaN(lat)) {
        throw new Error('经纬度格式不正确');
      }

      console.log('创建地图:', lng, lat);

      map.value = new window.AMap.Map(container, {
        zoom: 13,
        center: [lng, lat],
        resizeEnable: true
      });

      marker.value = new window.AMap.Marker({
        position: [lng, lat],
        title: currentLocation.value.name,
      });
      map.value.add(marker.value);

      // 添加信息窗体
      const infoWindow = new window.AMap.InfoWindow({
        content: `<div style="padding: 8px;">${currentLocation.value.name}</div>`,
        offset: new window.AMap.Pixel(0, -30)
      });

      // 默认打开信息窗体
      infoWindow.open(map.value, [lng, lat]);

    } catch (error) {
      console.error('地图初始化失败:', error);
      ElMessage.error(`地图加载失败: ${error.message}`);
    }
  } else {
    ElMessage.warning('暂无位置信息');
  }
};

//计算工作负载比例
const showWorkloadPercent = (row) => {
  return (row.currentWorkload / row.maxWorkload)*100
}
//工作负载状态
const showWorkloadStatus = (row) => {
  let percent = showWorkloadPercent(row)
  if (percent <= 30)
    return "success"
  if (percent > 30 && percent <= 60)
    return "warning"
  if (percent > 60)
    return "exception"
}
// // 获取技能列表（后端接口）
const loadSkills = () => {
  return new Promise((resolve) => {
    request
      .get("employee/getSkillList", {})
      .then((res) => {
        if (res.data.status === 200) {
          // 将后端获取的技能数据填充到 skillsList
          skillsList.value = res.data.data.map(skill => ({
            key: skill.skillId,
            label: skill.skillName,
          }));
          resolve();
        } else {
          ElMessage.error(res.data.msg);
          resolve();
        }
      })
      .catch(error => {
        console.error("加载技能列表出错:", error);
        ElMessage.error("加载技能列表出错");
        resolve();
      });
  });
};
// 获取状态标签
const getStatusLabel = (status) => {
  const statusObj = statusOptions.value.find(item => item.value === status);
  return statusObj ? statusObj.label : '未知状态';
};

// 获取状态类型（用于标签颜色）
const getStatusType = (status) => {
  switch(status) {
    case 'available':
      return 'success';
    case 'busy':
      return 'warning';
    case 'off':
      return 'info';
    default:
      return '';
  }
};

onMounted(() => {
  load();
  loadSkills();
});
</script>

<template>
  <div>
    <!-- 搜索栏 -->
    <div style="margin: 10px 0">
      <el-input v-model="employeeName" style="width: 200px" placeholder="请输入员工名" clearable>
        <template #suffix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input v-model="employeePhone" style="width: 200px" placeholder="请输入电话" class="ml-5" clearable>
        <template #suffix><el-icon><Phone /></el-icon></template>
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
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe :header-cell-class-name="headerBg" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="employeeId" label="ID" width="50" />
      <el-table-column prop="name" label="员工姓名" width="50" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="address" label="家庭地址" width="150"/>
      <el-table-column prop="skillLevel" label="等级" width="180">
        <template #default="{ row }">
          <div style="display: flex; align-items: center;">
            <span class="level-badge">Lv.{{ row.skillLevel }}</span>  <!-- 等级文本 -->
            <el-progress
                :stroke-width="25"
                :text-inside="true"
                :percentage="showLevel(row)"
                style="flex: 1;"
            >
              <span style="color: #333333">{{ row.levelPoint }}/100</span>
            </el-progress>
          </div>
        </template>

      </el-table-column>
      <el-table-column label="技能集" >
        <template #default="{ row }">
          <el-tooltip :content="employeeSkills[row.employeeId]?.join('，') || '暂无技能'" placement="top" effect="light">
            <el-icon @mouseenter="loadEmployeeSkills(row.employeeId)"
                     style="cursor: pointer"><List /></el-icon>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="当前地址" >
        <template #default="{ row }">
          <el-tooltip :content="row.location" placement="top" effect="light">
            <el-icon @click="showLocation(row)" style="cursor: pointer"><Location /></el-icon>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column  label="工作负载" >
        <template #default="{ row }">
          <el-progress
              :stroke-width="20"
              :text-inside="true"
              :percentage=showWorkloadPercent(row)
              :status=showWorkloadStatus(row)
          >
            <span style="color: #333333">{{row.currentWorkload}}/{{row.maxWorkload}}</span>
          </el-progress>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag
            :type="getStatusType(row.status)"
            effect="light"
          >
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button type="success" @click="handleEdit(row)">
            编辑 <el-icon><Edit /></el-icon>
          </el-button>
          <el-popconfirm
              confirm-button-text="确定"
              cancel-button-text="我再想想"
              title="您确定删除吗？"
              @confirm="del(row.employeeId)"
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
    <el-dialog :title="userDialogTitle" v-model="userDialogForm" width="35%">
      <el-form :model="UserForm" ref="UserFormRef" :rules="rules">
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="UserForm.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="员工电话" prop="phone">
          <el-input v-model="UserForm.phone" autocomplete="off" />
        </el-form-item>
        <el-form-item label="员工邮箱" prop="email">
          <el-input v-model="UserForm.email" autocomplete="off" />
        </el-form-item>
        <el-form-item label="员工家庭地址" prop="address">
          <el-input v-model="UserForm.address" autocomplete="off" />
        </el-form-item>
        <el-form-item label="等级" prop="skillLevel">
          <el-select v-model="UserForm.skillLevel" placeholder="Select" style="width: 240px">
            <el-option
                v-for="item in levelOptions"
                :key="item"
                :label="item"
                :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="员工状态" prop="status">
      <el-select v-model="UserForm.status" placeholder="请选择员工状态" style="width: 240px">
        <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
    </el-form-item>
        <el-form-item label="技能集" prop="skills">
          <el-transfer
              v-model="UserForm.skills"
              :data="skillsList"
              :titles="['可选技能', '已选技能']"
              filterable
              filter-placeholder="搜索技能"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogForm = false">取 消</el-button>
        <el-button type="primary" @click="newUser()">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 地图对话框 -->
    <el-dialog
        v-model="mapDialogVisible"
        title="员工位置"
        width="50%"
        destroy-on-close
        @closed="handleDialogClose"
        :append-to-body="true"
    >
      <div id="container" style="height: 400px; width: 100%; position: relative;"></div>
    </el-dialog>
  </div>
</template>

<style scoped>

.level-badge {
      min-width: 30px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      background-color: #409EFF;
      color: white;
      border-radius: 12px;
      font-size: 12px;
      font-weight: bold;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      margin-right: 5px;
    }
</style>