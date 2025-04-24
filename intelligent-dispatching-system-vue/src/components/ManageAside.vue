<template>
  <el-menu
      :default-openeds="['1', '3']"
      style="min-height: 100%; overflow-x: hidden"
      background-color="rgb(48, 65, 86)"
      text-color="#fff"
      active-text-color="#ffd04b"
      :collapse-transition="false"
      :collapse="props.isCollapse"
      router
  >
    <div style="height: 60px; line-height: 60px; text-align: center">
      <img src="../assets/logo.png" alt="" style="width: 20px; position: relative; top: 5px; right: 5px">
      <b style="color: white" v-show="props.logoTextShow">后台管理系统</b>
    </div>

    <el-menu-item v-if="isAdmin || isEmployee || isCustomer" index="/home">
      <span>主页</span>
    </el-menu-item>


    <el-sub-menu index="1">
      <template #title>
        <span>用户管理</span>
      </template>
      <el-menu-item v-if="isAdmin" index="/userManage">
        用户信息管理
      </el-menu-item>
      <el-menu-item v-if="isAdmin" index="/orderManage">
        报修处理
      </el-menu-item>
      <el-menu-item v-if="isAdmin" index="1-2">
        投诉处理
      </el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="2">
      <template #title>
        <span>员工管理</span>
      </template>
      <el-menu-item v-if="isAdmin" index="/employeeManage">
        员工信息管理
      </el-menu-item>

      <el-menu-item v-if="isAdmin" index="1-2">
        回访处理
      </el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="3">
      <template #title>
        <span>设备管理</span>
      </template>
      <el-menu-item v-if="isAdmin" index="1-2">
        物料管理
      </el-menu-item>
      <el-menu-item v-if="isAdmin" index="1-2">
        设备维护
      </el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>
<script setup>
import { ref, onMounted,defineProps } from 'vue';
import { useUserStore } from '@/store';
import {ElNotification} from "element-plus"; // 引入 Pinia Store
// 获取 Pinia Store
const store = useUserStore();
const props = defineProps({
  isCollapse: Boolean,
  logoTextShow: Boolean
});
// 定义角色权限
const isAdmin = ref(false);
const isEmployee = ref(false);
const isCustomer = ref(false);
// 获取权限信息
const getAuth = () => {
  const auth = store.user.role;
  switch (auth) {
    case "admin":
      isAdmin.value = true;
      break;
    case "employee":
      isEmployee.value = true;
      break;
    case "costumer":
      isCustomer.value = true;
      break;
    default:
      ElNotification({ title: '失败', message: "登录角色错误", type: 'error' });
  }
};

// 组件挂载时获取权限
onMounted(() => {
  getAuth();
});
</script>
<style scoped>

</style>