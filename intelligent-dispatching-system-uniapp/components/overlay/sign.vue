<script setup>
import { ref, onMounted } from 'vue';

// 定义事件
const emit = defineEmits(['close', 'success']);

// 签到状态
const loading = ref(false);
const location = ref('正在获取位置...');
const currentTime = ref('');

// 获取当前时间
const updateCurrentTime = () => {
  const now = new Date();
  const hours = now.getHours().toString().padStart(2, '0');
  const minutes = now.getMinutes().toString().padStart(2, '0');
  const seconds = now.getSeconds().toString().padStart(2, '0');
  currentTime.value = `${hours}:${minutes}:${seconds}`;
};

// 获取位置信息
const getLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      // 使用经纬度获取地址信息
      uni.request({
        url: `https://apis.map.qq.com/ws/geocoder/v1/?location=${res.latitude},${res.longitude}&key=YOUR_KEY`,
        success: (addressRes) => {
          if (addressRes.data && addressRes.data.result) {
            location.value = addressRes.data.result.address;
          } else {
            location.value = `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
          }
        },
        fail: () => {
          location.value = `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
        }
      });
    },
    fail: () => {
      location.value = '无法获取位置信息';
    }
  });
};

// 处理签到
const handleSign = () => {
  loading.value = true;
  
  // 模拟签到请求
  setTimeout(() => {
    loading.value = false;
    uni.showToast({
      title: '签到成功',
      icon: 'success'
    });
    
    // 通知父组件签到成功
    emit('success');
    
    // 关闭弹窗
    setTimeout(() => {
      emit('close');
    }, 1500);
  }, 1000);
};

// 关闭弹窗
const closeOverlay = () => {
  emit('close');
};

onMounted(() => {
  updateCurrentTime();
  // 每秒更新时间
  setInterval(updateCurrentTime, 1000);
  // 获取位置
  getLocation();
});
</script>

<template>
  <view class="sign-overlay">
    <view class="sign-container">
      <view class="sign-header">
        <text class="sign-title">员工签到</text>
        <uni-icons type="close" size="20" color="#999" @click="closeOverlay"></uni-icons>
      </view>
      
      <view class="sign-content">
        <view class="time-display">
          <text class="time-label">当前时间</text>
          <text class="time-value">{{ currentTime }}</text>
        </view>
        
        <view class="location-display">
          <text class="location-label">当前位置</text>
          <text class="location-value">{{ location }}</text>
        </view>
        
        <button 
          class="sign-btn" 
          type="primary" 
          :loading="loading"
          @click="handleSign"
        >立即签到</button>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.sign-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.sign-container {
  width: 80%;
  max-width: 600rpx;
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.sign-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1px solid #eee;
}

.sign-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.sign-content {
  padding: 30rpx;
}

.time-display, .location-display {
  margin-bottom: 30rpx;
}

.time-label, .location-label {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 10rpx;
  display: block;
}

.time-value {
  font-size: 48rpx;
  color: #333;
  font-weight: bold;
  display: block;
  text-align: center;
  margin: 20rpx 0;
}

.location-value {
  font-size: 28rpx;
  color: #333;
  display: block;
  background-color: #f5f5f5;
  padding: 20rpx;
  border-radius: 8rpx;
  line-height: 1.5;
}

.sign-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  font-size: 32rpx;
  margin-top: 30rpx;
  background-color: #007aff;
}
</style>