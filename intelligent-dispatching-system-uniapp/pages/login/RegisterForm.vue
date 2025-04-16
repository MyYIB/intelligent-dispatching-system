<script setup>
import { reactive, ref } from 'vue';
import * as userApi from '../../api/userAPI';

const props = defineProps({
  switchToLogin: Function
});

const registerForm = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  email: '',
  address: ''
});

const loading = ref(false);

// 注册方法
const handleRegister = async () => {
  // 表单验证 
  if (!registerForm.username || !registerForm.phone || 
      !registerForm.password || !registerForm.confirmPassword ||
      !registerForm.email || !registerForm.address) {
    uni.showToast({
      title: '请填写完整注册信息',
      icon: 'none'
    });
    return;
  }
  
  if (registerForm.password !== registerForm.confirmPassword) {
    uni.showToast({
      title: '两次输入的密码不一致',
      icon: 'none'
    });
    return;
  }
  
  // 手机号验证
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!phoneReg.test(registerForm.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    });
    return;
  }
  
  // 邮箱验证
  const emailReg = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
  if (!emailReg.test(registerForm.email)) {
    uni.showToast({
      title: '请输入正确的邮箱格式',
      icon: 'none'
    });
    return;
  }
  
  loading.value = true;
  
  try {
    // 调用封装的注册接口，使用phone作为account
    const res = await userApi.register({
      username: registerForm.username,
      account: registerForm.phone, // 使用手机号作为账号
      phone: registerForm.phone,
      password: registerForm.password,
      email: registerForm.email,
      address: registerForm.address,
      role: 'customer' // 默认注册为客户角色
    });
    
    if (res.status === 200) {
      uni.showToast({
        title: '注册成功，请登录',
        icon: 'success'
      });
      // 清空表单
      Object.keys(registerForm).forEach(key => {
        registerForm[key] = '';
      });
      // 注册成功后切换到登录页
      props.switchToLogin();
    } else {
      uni.showToast({
        title: res.msg || '注册失败',
        icon: 'none'
      });
    }
  } catch (err) {
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    });
    console.error(err);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <view class="form">
    <view class="form-item">
      <uni-icons type="person" size="20" color="#007aff"></uni-icons>
      <input 
        class="input" 
        v-model="registerForm.username" 
        placeholder="请输入用户名" 
        placeholder-style="color: #999;"
      />
    </view>
    
    <!-- 删除账号输入栏 -->
    
    <view class="form-item">
      <uni-icons type="phone" size="20" color="#007aff"></uni-icons>
      <input 
        class="input" 
        v-model="registerForm.phone" 
        placeholder="请输入手机号" 
        placeholder-style="color: #999;"
        type="number"
        maxlength="11"
      />
    </view>
    
    <view class="form-item">
      <uni-icons type="locked" size="20" color="#007aff"></uni-icons>
      <input 
        class="input" 
        v-model="registerForm.password" 
        password
        placeholder="请输入密码" 
        placeholder-style="color: #999;"
      />
    </view>
    
    <view class="form-item">
      <uni-icons type="locked" size="20" color="#007aff"></uni-icons>
      <input 
        class="input" 
        v-model="registerForm.confirmPassword" 
        password
        placeholder="请确认密码" 
        placeholder-style="color: #999;"
      />
    </view>
    
    <view class="form-item">
      <uni-icons type="email" size="20" color="#007aff"></uni-icons>
      <input 
        class="input" 
        v-model="registerForm.email" 
        placeholder="请输入邮箱" 
        placeholder-style="color: #999;"
      />
    </view>
    
    <view class="form-item">
      <uni-icons type="location" size="20" color="#007aff"></uni-icons>
      <input 
        class="input" 
        v-model="registerForm.address" 
        placeholder="请输入地址" 
        placeholder-style="color: #999;"
      />
    </view>
    
    <view class="form-tip">
      <text class="tip-text">* 手机号将作为您的登录账号</text>
    </view>
    
    <button 
      class="submit-btn" 
      type="primary" 
      :loading="loading" 
      @click="handleRegister"
    >注册</button>
    
    <view class="switch-form">
      <text @click="switchToLogin">已有账号？去登录</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.form {
  .form-item {
    display: flex;
    align-items: center;
    border-bottom: 1px solid #eee;
    padding: 20rpx 0;
    margin-bottom: 20rpx;
    
    .input {
      flex: 1;
      height: 80rpx;
      padding-left: 20rpx;
      font-size: 28rpx;
    }
  }
  
  .form-tip {
    margin-bottom: 20rpx;
    
    .tip-text {
      font-size: 24rpx;
    }
  }
  
  .submit-btn {
    width: 100%;
    height: 90rpx;
    line-height: 90rpx;
    font-size: 32rpx;
    margin-top: 20rpx;
    background-color: #007aff;
  }
  
  .switch-form {
    text-align: center;
    margin-top: 30rpx;
    font-size: 26rpx;
    color: #007aff;
  }
}
</style>