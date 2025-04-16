<script setup>
import { ref } from 'vue';
import LoginForm from './LoginForm.vue';
import RegisterForm from './RegisterForm.vue';

const isLogin = ref(true); // 控制显示登录还是注册表单

// 切换登录/注册表单
const toggleForm = () => {
  isLogin.value = !isLogin.value;
};
</script>

<template>
  <view class="login-page">
    <view class="login-box">
      <view class="header">
        <text class="title">{{ isLogin ? '登录' : '注册' }}</text>
      </view>
      
      <!-- 使用登录组件 -->
      <LoginForm v-if="isLogin" :switchToRegister="toggleForm" />
      
      <!-- 使用注册组件 -->
      <RegisterForm v-else :switchToLogin="toggleForm" />
    </view>
  </view>
</template>

<style lang="scss" scoped>
.login-page {
  width: 100%;
  height: 100vh;
  background: #5dd5c8 url(https://spring-oss.oss-cn-beijing.aliyuncs.com/images/img/bgimg.png) center bottom no-repeat;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  width: 90%;
  max-width: 600rpx;
  background-color: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 13rpx 0 rgba(0, 0, 0, 0.1);
  padding: 40rpx;
}

.header {
  text-align: left;
  margin-bottom: 40rpx;
  
  .title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
}

/* 小屏幕适配 */
@media screen and (max-width: 375px) {
  .login-box {
    padding: 30rpx;
  }
  
  .header .title {
    font-size: 32rpx;
  }
}
</style>