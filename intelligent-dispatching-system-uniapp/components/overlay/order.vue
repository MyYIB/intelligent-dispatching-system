<script setup>
import { ref } from 'vue';

// 定义事件
const emit = defineEmits(['close', 'success']);

// 报修类型
const repairTypes = [
  { id: 1, icon: 'settings', name: '报修', page: '/pages/user/repair' },
  { id: 2, icon: 'staff', name: '投诉', page: '/pages/user/complaint' }
];

// 选中的报修类型
const selectedType = ref(null);

// 选择报修类型
const selectType = (type) => {
  selectedType.value = type;
};

// 关闭弹窗
const closeOverlay = () => {
  emit('close');
};

// 去对应页面
const goToPage = () => {
  if (!selectedType.value) {
    uni.showToast({
      title: '请选择类型',
      icon: 'none'
    });
    return;
  }
  
  // 关闭弹窗
  emit('close');
  
  // 跳转到对应页面
  uni.navigateTo({
    url: selectedType.value.page
  });
};
</script>

<template>
  <view class="order-overlay">
    <view class="order-container">
      <view class="order-header">
        <text class="order-title">快速服务</text>
        <uni-icons type="close" size="20" color="#999" @click="closeOverlay"></uni-icons>
      </view>
      
      <view class="order-content">
        <text class="order-tip">请选择服务类型：</text>
        
        <view class="repair-types">
          <view 
            v-for="type in repairTypes" 
            :key="type.id"
            class="repair-type-item"
            :class="{ 'selected': selectedType && selectedType.id === type.id }"
            @click="selectType(type)"
          >
            <uni-icons :type="type.icon" size="30" color="#007aff"></uni-icons>
            <text>{{ type.name }}</text>
          </view>
        </view>
        
        <button 
          class="order-btn" 
          type="primary" 
          @click="goToPage"
        >确定</button>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.order-overlay {
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

.order-container {
  width: 80%;
  max-width: 600rpx;
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1px solid #eee;
}

.order-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.order-content {
  padding: 30rpx;
}

.order-tip {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
  display: block;
}

.repair-types {
  display: flex;
  justify-content: space-around;
  margin: 30rpx 0;
}

.repair-type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  border-radius: 8rpx;
  transition: all 0.3s;
}

.repair-type-item text {
  font-size: 24rpx;
  color: #333;
  margin-top: 10rpx;
}

.selected {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.order-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  font-size: 32rpx;
  margin-top: 20rpx;
  background-color: #007aff;
}
</style>