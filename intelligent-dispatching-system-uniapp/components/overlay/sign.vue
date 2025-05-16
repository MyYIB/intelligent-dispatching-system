<script setup>
import { ref, onMounted, computed } from 'vue';
import { updateEmployeeLocation } from '@/api/employeeAPI.js'; 

// 定义事件
const emit = defineEmits(['close', 'success']);

// 员工信息
const employeeInfo = ref(null);
// 签到状态
const loading = ref(false);
const location = ref('正在获取位置...');
const currentTime = ref('');
const latitude = ref(null);
const longitude = ref(null);

// 根据员工状态计算按钮文本和操作类型
const isSignIn = computed(() => employeeInfo.value?.status === 'off' || !employeeInfo.value?.status);
const buttonText = computed(() => isSignIn.value ? '上班签到' : '下班签退');
const targetStatus = computed(() => isSignIn.value ? 'available' : 'off');

// 获取当前时间
const updateCurrentTime = () => {
  const now = new Date();
  const hours = now.getHours().toString().padStart(2, '0');
  const minutes = now.getMinutes().toString().padStart(2, '0');
  const seconds = now.getSeconds().toString().padStart(2, '0');
  currentTime.value = `${hours}:${minutes}:${seconds}`;
};

// 获取员工信息
const getEmployeeInfo = () => {
  const employeeInfoStr = uni.getStorageSync('employeeInfo');
  if (employeeInfoStr) {
    employeeInfo.value = JSON.parse(employeeInfoStr);
  }
};
// 获取位置信息
const getLocation = () => {
  // 先检查位置权限
  uni.getSetting({
    success: (res) => {
      if (!res.authSetting['scope.userLocation']) {
        // 如果没有位置权限，先请求权限
        uni.authorize({
          scope: 'scope.userLocation',
          success: () => {
            // 获得授权后获取位置
            getLocationInfo();
          },
          fail: () => {
            // 用户拒绝授权，提示用户去设置页面开启
            uni.showModal({
              title: '提示',
              content: '需要获取您的地理位置，请在设置中打开位置权限',
              confirmText: '去设置',
              cancelText: '取消',
              success: (result) => {
                if (result.confirm) {
                  uni.openSetting();
                } else {
                  location.value = '未授权获取位置';
                }
              }
            });
          }
        });
      } else {
        // 已有权限，直接获取位置
        getLocationInfo();
      }
    },
    fail: (err) => {
      console.error('获取设置失败', err);
      location.value = '获取设置失败';
      uni.showToast({
        title: '获取设置失败',
        icon: 'none'
      });
    }
  });
};

// 获取位置信息的具体实现
const getLocationInfo = () => {
  uni.showLoading({
    title: '获取位置中...'
  });
  
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      uni.hideLoading();
      latitude.value = res.latitude;
      longitude.value = res.longitude;
      
      // 使用经纬度获取地址信息
      uni.request({
        url: `https://apis.map.qq.com/ws/geocoder/v1/?location=${res.latitude},${res.longitude}&key=727BZ-3JF6L-HSHPO-MY4HW-SNDIK-DZBQ7`,
        success: (addressRes) => {
          console.log('地址解析结果:', addressRes);
          if (addressRes.data && addressRes.data.status === 0 && addressRes.data.result) {
            // 获取详细地址信息
            const result = addressRes.data.result;
            // 使用更详细的地址信息组合
            const addressComponent = result.address_component;
            const formatted_addresses = result.formatted_addresses;
            
            // 优先使用推荐地址，如果没有则使用详细地址组合
            if (formatted_addresses && formatted_addresses.recommend) {
              location.value = formatted_addresses.recommend;
            } else {
              // 组合详细地址
              let detailAddress = '';
              if (addressComponent) {
                detailAddress = [
                  addressComponent.province,
                  addressComponent.city,
                  addressComponent.district,
                  addressComponent.street,
                  addressComponent.street_number
                ].filter(Boolean).join('');
              }
              location.value = detailAddress || result.address || `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
            }
          } else {
            location.value = `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
          }
        },
        fail: (err) => {
          console.error('地址解析失败:', err);
          location.value = `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
        }
      });
    },
    fail: (err) => {
      uni.hideLoading();
      console.error('获取位置失败', err);
      location.value = '无法获取位置信息';
      
      // 显示错误提示
      uni.showToast({
        title: '获取位置失败，请检查GPS是否开启',
        icon: 'none'
      });
    }
  });
};

// 处理签到/签退
const handleSign = async () => {
  if (!latitude.value || !longitude.value) {
    uni.showToast({
      title: '位置信息获取失败，请重试',
      icon: 'none'
    });
    // 再次尝试获取位置
    getLocation();
    return;
  }
  
  if (!employeeInfo.value || !employeeInfo.value.employeeId) {
    console.error('员工信息不完整:', employeeInfo.value);
    uni.showToast({
      title: '员工信息不完整，请重新登录',
      icon: 'none'
    });
    return;
  }
  
  loading.value = true;
  
  try {
    
    // 构建请求数据，确保所有字段都是基本类型
    const updateData = {
      employeeId: employeeInfo.value.employeeId,
      location: location.value,
      locationLatitude: Number(latitude.value),
      locationLongitude: Number(longitude.value),
      status: targetStatus.value,
      currentWorkload: 0
    };
    
    console.log('发送签到/签退请求数据:', updateData);
    
    const res = await updateEmployeeLocation(updateData);
    console.log('签到/签退响应:', res);
    
    if (res.status === 200) {
      // 更新本地存储的员工信息
      const employeeInfoStr = uni.getStorageSync('employeeInfo');
      if (employeeInfoStr) {
        const employeeInfo = JSON.parse(employeeInfoStr);
        employeeInfo.status = targetStatus.value;
        employeeInfo.location = location.value;
        employeeInfo.locationLatitude = latitude.value;
        employeeInfo.locationLongitude = longitude.value;
        uni.setStorageSync('employeeInfo', JSON.stringify(employeeInfo));
      }
      
      uni.showToast({
        title: isSignIn.value ? '签到成功' : '签退成功',
        icon: 'success'
      });
      
      // 通知父组件签到/签退成功
      emit('success', {
        ...updateData,
        isSignIn: isSignIn.value
      });
      
      // 关闭弹窗
      setTimeout(() => {
        emit('close');
      }, 1500);
    } else {
      uni.showToast({
        title: res.message || (isSignIn.value ? '签到失败' : '签退失败'),
        icon: 'none'
      });
    }
  } catch (error) {
    console.error(isSignIn.value ? '签到失败' : '签退失败', error);
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 关闭弹窗
const closeOverlay = () => {
  emit('close');
};

onMounted(() => {
  // 获取本地存储的员工信息
  getEmployeeInfo();
  // 初始化时间
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
        <text class="sign-title">{{ isSignIn ? '员工签到' : '员工签退' }}</text>
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
          :class="isSignIn ? 'sign-in-btn' : 'sign-out-btn'"
          :loading="loading"
          @click="handleSign"
        >{{ buttonText }}</button>
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
}

.sign-in-btn {
  background-color: #007aff;
}

.sign-out-btn {
  background-color: #ff9500;
}
</style>