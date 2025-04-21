<template>
  <view class="complaint-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <text class="nav-title">投诉服务</text>
    </view>
    
    <!-- 表单内容 -->
    <view class="form-content">
      <!-- 投诉内容 -->
      <view class="form-item">
        <text class="form-label">投诉内容</text>
        <view class="textarea-wrapper">
          <textarea 
            v-model="description" 
            placeholder="请详细描述您的投诉内容..." 
            maxlength="500"
            class="description-input"
          ></textarea>
          <text class="word-count">{{ description.length }}/500</text>
        </view>
      </view>
      
      <!-- 联系方式确认 -->
      <view class="form-item">
        <text class="form-label">联系方式确认</text>
        <view class="contact-info">
          <text>当前联系电话：{{ userPhone || '未绑定' }}</text>
          <button class="small-btn" @click="updatePhone" v-if="!userPhone">绑定手机</button>
        </view>
      </view>
    </view>
    
    <!-- 提交按钮 -->
    <view class="submit-btn-wrapper">
      <button class="submit-btn" type="primary" @click="submitComplaint">提交投诉</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 表单数据
const description = ref('');
const userPhone = ref('');

// 获取用户信息
const getUserInfo = () => {
  const userInfoStr = uni.getStorageSync('userInfo');
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr);
      userPhone.value = userInfo.phone || '';
    } catch (e) {
      console.error('解析用户信息失败', e);
    }
  }
};

// 更新手机号
const updatePhone = () => {
  uni.navigateTo({
    url: '/pages/user/profile'
  });
};

// 提交投诉
const submitComplaint = () => {
  // 表单验证
  if (!description.value.trim()) {
    return uni.showToast({
      title: '请填写投诉内容',
      icon: 'none'
    });
  }
  
  // 构建提交数据
  const orderData = {
    order_type: 'complaint',
    description: description.value
  };
  
  // 显示加载提示
  uni.showLoading({
    title: '提交中...'
  });
  
  // 调用API提交投诉单
  // 这里替换为您的实际API调用
  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({
      title: '投诉提交成功',
      icon: 'success'
    });
    
    // 延迟返回上一页
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  }, 1000);
  
  // 实际API调用示例
  /*
  submitOrder(orderData).then(res => {
    uni.hideLoading();
    if (res.code === 200) {
      uni.showToast({
        title: '投诉提交成功',
        icon: 'success'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    } else {
      uni.showToast({
        title: res.message || '提交失败',
        icon: 'none'
      });
    }
  }).catch(err => {
    uni.hideLoading();
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    });
  });
  */
};

// 返回上一页
const goBack = () => {
  uni.navigateBack();
};

// 页面加载时初始化
onMounted(() => {
  getUserInfo();
});
</script>

<style lang="scss" scoped>
.complaint-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.nav-bar {
  display: flex;
  align-items: center;
  height: 90rpx;
  background-color: #fff;
  padding: 0 30rpx;
  position: relative;
}

.nav-back {
  padding: 10rpx;
}

.nav-title {
  position: absolute;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 32rpx;
  font-weight: 500;
}

.form-content {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 12rpx;
  padding: 20rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 15rpx;
  display: block;
}

.textarea-wrapper {
  position: relative;
  border: 1px solid #eee;
  border-radius: 8rpx;
  background-color: #f9f9f9;
}

.description-input {
  width: 100%;
  height: 400rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.word-count {
  position: absolute;
  right: 20rpx;
  bottom: 20rpx;
  font-size: 24rpx;
  color: #999;
}

.contact-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background-color: #f9f9f9;
  border-radius: 8rpx;
  border: 1px solid #eee;
}

.small-btn {
  font-size: 24rpx;
  padding: 6rpx 20rpx;
  line-height: 1.5;
  background-color: #007aff;
  color: #fff;
  border-radius: 30rpx;
  margin: 0;
}

.submit-btn-wrapper {
  padding: 30rpx 20rpx;
}

.submit-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  font-size: 32rpx;
  background-color: #007aff;
}
</style>