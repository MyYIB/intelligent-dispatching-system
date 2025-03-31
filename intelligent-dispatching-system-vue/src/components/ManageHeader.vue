<script setup>
import request from "@/utils/request";
import { ref, onMounted,computed,defineProps } from 'vue';
import { useUserStore } from '@/store'; // 引入 Pinia Store
import { ElNotification } from "element-plus";
const store = useUserStore();
const props = defineProps({
  collapseBtnClass: String,
  collapse: Function,
})
// 响应式变量
const dialogForm = ref(false);
const changeForm = ref({
  account: "",
  password: "",
});
const confirmedPassword = ref("");
const username = ref("");

// 计算当前路径名称
const currentPathName = computed(() => store.currentPathName);
// 获取用户名
const getUserName = async () => {
  username.value = store.user.username;


};
// 退出登录
const logout = () => {
  store.logout();
  window.location.href = "/login";
};

// 修改密码
const changePassword = () => {
  dialogForm.value = true;
};

// 提交密码修改
const postPassword = async () => {
  if (changeForm.value.password !== confirmedPassword.value) {
    ElNotification({ title: "失败", message: "请输入相同的密码", type: "error" });
    return;
  }

  changeForm.value.account = store.user.account;

  const res = await request.post("login/changePassword", changeForm.value);
  if (res.code === 200) {
    ElNotification({ title: "成功", message: "修改密码成功", type: "success" });
    dialogForm.value = false;
  } else {
    ElNotification({ title: "失败", message: "修改密码失败", type: "error" });
  }
};




// 页面加载时获取用户名
onMounted(getUserName);
</script>

<template>
  <div style="line-height: 60px; display: flex">
    <div style="flex: 1;">
      <span
          :class="props.collapseBtnClass"
          style="cursor: pointer; font-size: 18px"
          @click="props.collapse"
      ></span>

      <el-breadcrumb separator="/" style="display: inline-block; margin-left: 10px">
        <el-breadcrumb-item :to="'/'">首页</el-breadcrumb-item>
        <el-breadcrumb-item> {{ currentPathName }} </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧用户菜单 -->
    <el-dropdown trigger="click" style="width: 50px; height: 50px; border-radius: 100px; text-align: center; cursor: pointer; background: #e1e1e1; display: flex; align-items: center; justify-content: center; position: relative;  margin-top: 5px">
      <span style="">
        {{ username }}
      </span>
      <i class="el-icon-arrow-down" style="margin-left: 5px"></i>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="changePassword">修改个人密码</el-dropdown-item>
          <el-dropdown-item @click="logout">退出</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <el-dialog title="修改密码" v-model="dialogForm" width="30%">
      <el-form :model="changeForm">
        <el-form-item label="新密码" label-width="90px">
          <el-input v-model="changeForm.password" type="password" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" label-width="90px">
          <el-input v-model="confirmedPassword" type="password" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogForm = false">取 消</el-button>
        <el-button type="primary" @click="postPassword">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>

</style>