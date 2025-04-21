<template>
  <view class="order-card" @click="handleClick">
    <view class="order-header">
      <view class="order-type-box">
        <text class="order-type-title">报修类型</text>
        <text class="order-type-value">{{ order.repairTypeName || getRepairTypeName(order.repairType) }}</text>
      </view>
      <text class="order-status" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</text>
    </view>
    
    <view class="order-content">
      <text class="order-desc">{{ order.description }}</text>
    </view>
    
    <view class="order-footer">
      <text class="order-time">{{ formatTime(order.createdAt) }}</text>
    </view>
  </view>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  order: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['click']);

const handleClick = () => {
  
  emit('click', props.order.orderId);
  
};

// 报修类型映射（如有需要可根据实际类型调整）
const repairTypeMap = {
  1: '宽带故障',
  2: '网络卡顿',
  3: '断网问题',
  4: '路由器问题',
  5: '设备损坏',
  6: '网络延迟高',
  7: '电视信号问题',
  8: 'Wi-Fi信号弱',
  10: '5G信号差',
  11: '机房维护',
  12: '电缆故障',
  13: '远程协助请求',
  14: '设备升级'
};
const getRepairTypeName = (type) => {
  
  return repairTypeMap[type] || '未知类型';
};



const getStatusText = (status) => {
  
  const statusMap = {
    'pending': '待处理',
    'assigned': '已分配工人',
    'in_progress': '处理中',
    'completed': '已完成',
    'closed': '已关闭'
  };
  return statusMap[status] || '未知状态';
};

const getStatusClass = (status) => {
  const classMap = {
    'pending': 'status-pending',
    'assigned': 'status-assigned',
    'in_progress': 'status-in-progress',
    'completed': 'status-completed',
    'closed': 'status-closed'
  };
  return classMap[status] || '';
};
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  // 兼容 ISO 字符串（如2025-04-21T19:52:03）
  const date = typeof timestamp === 'string'
    ? new Date(timestamp.replace(/-/g, '/').replace('T', ' '))
    : new Date(timestamp);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hour = date.getHours().toString().padStart(2, '0');
  const minute = date.getMinutes().toString().padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}`;
};
</script>

<style lang="scss" scoped>
.order-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 36rpx 32rpx 28rpx 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  margin-bottom: 16rpx;
  border: 2rpx solid #007aff;
  transition: box-shadow 0.2s;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18rpx;
}

.order-type-box {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.order-type-title {
  font-size: 22rpx;
  color: #007aff;
  font-weight: bold;
  margin-bottom: 4rpx;
}

.order-type-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.order-status {
  font-size: 26rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.status-pending {
  background-color: #FFF7E6;
  color: #FA8C16;
}

.status-assigned {
  background-color: #E6F7FF;
  color: #1890FF;
}

.status-in-progress {
  background-color: #F0F5FF;
  color: #2F54EB;
}

.status-completed {
  background-color: #F6FFED;
  color: #52C41A;
}

.status-closed {
  background-color: #F5F5F5;
  color: #8C8C8C;
}

.order-content {
  margin-bottom: 18rpx;
}

.order-desc {
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
}

.order-time {
  font-size: 24rpx;
  color: #999;
}
</style>