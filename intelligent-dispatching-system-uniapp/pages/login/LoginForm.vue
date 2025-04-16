<script setup>
import { reactive, ref } from 'vue';
import * as userApi from '../../api/userAPI';
const props = defineProps({
  switchToRegister: Function
});

const loginForm = reactive({
  phone: '', // 将account改为phone
  password: ''
});

const rememberMe = ref(true);
const loading = ref(false);

// 处理记住我选项变化
const handleRememberChange = (e) => {
  rememberMe.value = e.detail.value.length > 0;
};

// 登录方法
const handleLogin = async () => {
  if (!loginForm.phone || !loginForm.password) {
    uni.showToast({
      title: '手机号和密码不能为空',
      icon: 'none'
    });
    return;
  }
  
  // 验证手机号格式
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!phoneReg.test(loginForm.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    });
    return;
  }
  
  loading.value = true;
  
  try {
    // 调用封装的登录接口，将phone作为account传递
    const res = await userApi.login({
      account: loginForm.phone,
      password: loginForm.password
    });
    
    if (res.status === 200) {
      const { token, user } = res.data;
      
      // 存储token到本地存储
      uni.setStorageSync('token', token);
      // 存储user到本地存储
      uni.setStorageSync('userInfo', JSON.stringify(user));
      
      uni.showToast({
        title: '登录成功',
        icon: 'success'
      });
      
      // 根据角色跳转到不同页面
     if (user.role === 'technician') {
        uni.reLaunch({
          url: '/pages/employee/home'
        });
      } else {
        uni.reLaunch({
          url: '/pages/user/home'
        });
      }
    } else {
      uni.showToast({
        title: res.msg || '登录失败',
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
      <uni-icons type="phone" size="20" color="#007aff"></uni-icons>
      <input 
        class="input" 
        v-model="loginForm.phone" 
        type="number"
        maxlength="11"
        placeholder="请输入手机号" 
        placeholder-style="color: #999;"
      />
    </view>
    
    <view class="form-item">
      <uni-icons type="locked" size="20" color="#007aff"></uni-icons>
      <input 
        class="input" 
        v-model="loginForm.password" 
        password
        placeholder="请输入密码" 
        placeholder-style="color: #999;"
      />
    </view>
    
    <view class="form-tip">
      <text class="tip-text">* 手机号即为您的登录账号</text>
    </view>
    
    <view class="remember-box">
      <checkbox-group @change="handleRememberChange">
        <checkbox :checked="rememberMe" value="1" color="#007aff" style="transform:scale(0.8);" />
      </checkbox-group>
      <text class="remember-text">记住我</text>
    </view>
    
    <button 
      class="submit-btn" 
      type="primary" 
      :loading="loading" 
      @click="handleLogin"
    >登录</button>
    
    <view class="switch-form">
      <text @click="switchToRegister">没有账号？去注册</text>
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
  
  .remember-box {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    
    .remember-text {
      font-size: 24rpx;
      color: #666;
      margin-left: 10rpx;
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