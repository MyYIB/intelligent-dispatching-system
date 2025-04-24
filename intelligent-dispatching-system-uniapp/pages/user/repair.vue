<template>
  <view class="repair-container">
    
    <!-- 表单内容 -->
    <view class="form-content">
      <!-- 报修类型选择 -->
      <view class="form-item">
        <text class="form-label">报修类型</text>
        <view class="repair-type-selector">
          <picker @change="handleTypeChange" :value="typeIndex" :range="repairTypeNames">
            <view class="picker-view">
              <text>{{ selectedTypeName || '请选择报修类型' }}</text>
              <uni-icons type="right" size="16" color="#ccc"></uni-icons>
            </view>
          </picker>
        </view>
      </view>
      
      <!-- 问题描述 -->
      <view class="form-item">
        <text class="form-label">问题描述</text>
        <view class="textarea-wrapper">
          <textarea 
            v-model="description" 
            placeholder="请详细描述您遇到的问题..." 
            maxlength="200"
            class="description-input"
          ></textarea>
          <text class="word-count">{{ description.length }}/200</text>
        </view>
      </view>
      
      <!-- 位置选择 -->
      <view class="form-item">
        <text class="form-label">报修位置</text>
        <view class="location-selector" @click="chooseLocation">
          <text v-if="location">{{ location }}</text>
          <text v-else class="placeholder">点击选择位置</text>
          <uni-icons type="location" size="20" color="#007aff"></uni-icons>
        </view>
      </view>
      
      <!-- 地图显示 -->
      <view class="map-container" v-if="latitude && longitude">
        <map 
          class="map" 
          :latitude="latitude" 
          :longitude="longitude" 
          :markers="markers"
          scale="16"
        ></map>
      </view>
      <!-- 截止日期 -->
      <view class="form-item">
        <text class="form-label">期望完成日期</text>
        <view class="date-selector">
          <picker 
            mode="date" 
            :value="deadline" 
            :start="minDate" 
            @change="handleDateChange"
          >
            <view class="picker-view">
              <text>{{ deadline || '请选择日期' }}</text>
              <uni-icons type="calendar" size="16" color="#ccc"></uni-icons>
            </view>
          </picker>
        </view>
      </view>
    </view>
    
    <!-- 提交按钮 -->
    <view class="submit-btn-wrapper">
      <button class="submit-btn" type="primary" @click="submitRepair">提交报修</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getRepairT } from '@/api/userAPI.js'
import { createOrder } from '../../api/orderAPI';
// 报修类型列表
const repairTypes = ref([]);

// 报修类型名称列表（用于选择器）
const repairTypeNames = computed(() => {
  return repairTypes.value.map(item => item.name);
});

// 表单数据
const typeIndex = ref(0);
const selectedTypeId = ref(null);
const selectedTypeName = ref('');
const description = ref('');
const location = ref('');
const latitude = ref('');
const longitude = ref('');
const deadline = ref('');
// 地图标记点
const markers = computed(() => {
  if (!latitude.value || !longitude.value) return [];
  return [{
    id: 1,
    latitude: latitude.value,
    longitude: longitude.value,
    title: location.value,
    width: 30,
    height: 30,
    anchor: {
      x: 0.5,
      y: 0.5
    }
  }];
});



// 最小日期（今天）
const minDate = computed(() => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
});

// 处理报修类型变化
const handleTypeChange = (e) => {
  const index = e.detail.value;
  typeIndex.value = index;
  selectedTypeId.value = repairTypes.value[index].id;
  selectedTypeName.value = repairTypes.value[index].name;
};

// 处理日期变化
const handleDateChange = (e) => {
  deadline.value = e.detail.value;
};

// 选择位置
const chooseLocation = () => {
  // 先检查位置权限
  uni.getSetting({
    success: (res) => {
      if (!res.authSetting['scope.userLocation']) {
        // 如果没有位置权限，先请求权限
        uni.authorize({
          scope: 'scope.userLocation',
          success: () => {
            // 获得授权后打开位置选择器
            openLocationChooser();
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
                }
              }
            });
          }
        });
      } else {
        // 已有权限，直接打开位置选择器
        openLocationChooser();
      }
    },
    fail: (err) => {
      console.error('获取设置失败', err);
      uni.showToast({
        title: '获取设置失败',
        icon: 'none'
      });
    }
  });
};

// 打开位置选择器
const openLocationChooser = () => {
  uni.chooseLocation({
    success: (res) => {
      location.value = res.name || res.address;
      latitude.value = res.latitude;
      longitude.value = res.longitude;
    },
    fail: (err) => {
      console.error('选择位置失败', err);
      // 判断是否是因为用户取消了操作
      if (err.errMsg.indexOf('cancel') === -1) {
        uni.showToast({
          title: '选择位置失败，请检查网络或权限设置',
          icon: 'none'
        });
      }
    }
  });
};

// 提交报修
const submitRepair = () => {
  // 表单验证
  if (!selectedTypeId.value) {
    return uni.showToast({
      title: '请选择报修类型',
      icon: 'none'
    });
  }
  
  if (!description.value.trim()) {
    return uni.showToast({
      title: '请填写问题描述',
      icon: 'none'
    });
  }
  
  if (!location.value) {
    return uni.showToast({
      title: '请选择报修位置',
      icon: 'none'
    });
  }
  const userInfoStr = uni.getStorageSync('userInfo');
  if (!userInfoStr) {
  uni.showToast({ title: '用户未登录', icon: 'none' });
  return;
  }
  const userInfo = JSON.parse(userInfoStr);
  console.log(userInfoStr)
  // 构建提交数据
  const orderData = {
    userId: userInfo.userId,
    orderType: 'nrepair',
    repairType: selectedTypeId.value,
    description: description.value,
    location: location.value,
    locationLatitude: latitude.value,
    locationLongitude: longitude.value,
    deadline: deadline.value ? deadline.value + 'T00:00:00' : null
  };
  console.log(orderData)
  // 显示加载提示
  uni.showLoading({
    title: '提交中...'
  });
  
  // 调用API提交报修单
  // 调试
  // setTimeout(() => {
  //   uni.hideLoading();
  //   uni.showToast({
  //     title: '报修提交成功',
  //     icon: 'success'
  //   });
    
  //   // 延迟返回上一页
  //   setTimeout(() => {
  //     uni.navigateBack();
  //   }, 1500);
  // }, 1000);
  createOrder(orderData).then(res => {
    uni.hideLoading();
    if (res.status === 200) {
      uni.showToast({
        title: '报修提交成功',
        icon: 'success'
      });
      uni.$emit('order-updated');
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
  
};


const getRepairType = async() =>{
    try{
        const res = await getRepairT();
        if (res.status === 200) {
            repairTypes.value = res.data.map(type=>({
              id: type.reparTypeId,
              name: type.reparTypeName
            }));            
            // 如果有数据，默认选中第一项
            if(repairTypes.value.length > 0) {
                selectedTypeId.value = repairTypes.value[0].id;
                selectedTypeName.value = repairTypes.value[0].name;
            }
        } else {
            uni.showToast({
                title: res.message || '获取报修类型失败',
                icon: 'none'
            });
        }
    } catch (error) {
        console.error('获取报修类型失败', error);
        uni.showToast({
            title: '网络异常，请稍后重试',
            icon: 'none'
        });
       
    }
}
// 页面加载时初始化
onMounted(() => {
    getRepairType(); 
});
</script>

<style lang="scss" scoped>
.repair-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
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

.repair-type-selector, .location-selector, .date-selector {
  border: 1px solid #eee;
  border-radius: 8rpx;
  padding: 20rpx;
  background-color: #f9f9f9;
}

.picker-view {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.textarea-wrapper {
  position: relative;
  border: 1px solid #eee;
  border-radius: 8rpx;
  background-color: #f9f9f9;
}

.description-input {
  width: 100%;
  height: 200rpx;
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

.location-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.placeholder {
  color: #999;
}

.map-container {
  margin: 20rpx 0 30rpx;
  height: 300rpx;
  border-radius: 8rpx;
  overflow: hidden;
}

.map {
  width: 100%;
  height: 100%;
}

.priority-selector {
  display: flex;
  gap: 20rpx;
}

.priority-item {
  flex: 1;
  text-align: center;
  padding: 15rpx 0;
  border: 1px solid #eee;
  border-radius: 8rpx;
  background-color: #f9f9f9;
}

.priority-item.active {
  background-color: #e6f7ff;
  border-color: #91d5ff;
  color: #007aff;
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