<script setup>
import request from "@/utils/request";
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/store';
import { ElNotification } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const store = useUserStore();

const rememberMe = ref(true);
const loginForm = ref({
  account: '',
  password: ''
});
const formRef = ref(null);
const rules = {
  account: [{ required: true, message: '请输入帐号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const login = () => {
  request.post('login/login', loginForm.value)
      .then(res => {
        if (res.data.code === 200) {
          const { token, user } = res.data.data;
          //存储token
          store.setToken(token);
          //存储user
          store.setUser(user);
          if(user.role === "admin"){
            const path  = route.query.redirect || '/';
            router.replace(path);
          }

        } else {
          ElNotification({ title: '失败', message: res.data.msg, type: 'error' });
          console.log(res);
        }
      });
};
</script>

<template>
  <div class="login-form">
    <el-header class="header" height="45px">
      <h2 class="title">登录</h2>
    </el-header>
    <div class="form">
      <el-form label-position="left" :model="loginForm"  status-icon :rules="rules" ref="formRef">
        <el-form-item  prop="account">
          <el-input v-model="loginForm.account" autocomplete="on" placeholder="请输入帐号"
                    :prefix-icon="User"></el-input>
        </el-form-item>
        <el-form-item prop="password" style="margin-bottom: 15px;">
          <el-input type="password" show-password v-model="loginForm.password" autocomplete="off" placeholder="请输入密码"
                    :prefix-icon="Lock"></el-input>
        </el-form-item>
        <el-form-item style="margin-bottom: 5px;">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item style="margin-bottom: 10px;">
          <el-button type="primary" class="submit" @click="login">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-form .header {
  position: relative;
  text-align: left;
  border: none;
  padding: 0;
}

.login-form .header .title {
  margin: 0;
}


.login-form .form {
  text-align: left;
}

.login-form .form .retrieve-password {
  text-decoration: none;
  float: right;
  color: #005980;
}

.login-form .form .submit {
  width: 100%;
}
/* 输入框 */
.el-input {
  width: 100%;           /* 占满父容器宽度 */
  height: 3rem;          /* 1rem ≈ 16px (默认) */
  font-size: 1rem;       /* 字体跟随根元素缩放 */
}

/* 按钮 */
.el-button.submit {
  height: 3rem;          /* 增加按钮高度 */
  font-size: 1rem;       /* 增大字体 */
}

/* 标题 */
.header .title {
  font-size: 1.5rem;       /* 增大标题字体 */
  margin-bottom: 1.25rem;   /* 增加标题与表单的间距 */
}
/* 表单项间距 */
.el-form-item {
  margin-bottom: 1.25rem !important; /* 20px → 1.25rem */
}
/* 超小屏幕（如iPhone SE） */
@media (max-width: 360px) {
  .header .title {
    font-size: 1.2rem;  /* 进一步缩小标题 */
  }
  .el-input {
    height: 2.5rem;     /* 缩小输入框高度 */
  }
}
</style>