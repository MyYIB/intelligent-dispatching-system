<template>
  <view class="detail-container">
    <!-- 地图区域 -->
    <view class="map-container">
      <map 
        class="order-map" 
        :latitude="orderDetail.locationLatitude" 
        :longitude="orderDetail.locationLongitude" 
        :markers="markers"
        scale="16"
        show-location
      ></map>
    </view>
    
    <!-- 工单信息卡片 -->
    <view class="order-card">
      <!-- 工单状态 -->
      <view class="status-bar" :class="getStatusClass(orderDetail.status)">
        <text class="status-text">{{ getStatusText(orderDetail.status) }}</text>
      </view>
      
      <!-- 工单基本信息 -->
      <view class="info-section">
        <view class="info-header">
          <text class="info-title">基本信息</text>
        </view>
        
        <view class="info-item">
          <text class="item-label">工单编号</text>
          <text class="item-value">{{ orderDetail.orderId || '暂无' }}</text>
        </view>
        
        <view class="info-item">
          <text class="item-label">工单类型</text>
          <text class="item-value">{{ getOrderTypeName(orderDetail.orderType) }}</text>
        </view>
        <view class="info-item">
          <text class="item-label">报修类型</text>
          <text class="item-value">{{ getRepairTypeName(orderDetail.repairType) }}</text>
        </view>
        <view class="info-item">
          <text class="item-label">报修位置</text>
          <text class="item-value">{{ orderDetail.location || '暂无' }}</text>
        </view>
        
        <view class="info-item">
          <text class="item-label">创建时间</text>
          <text class="item-value">{{ formatTime(orderDetail.createdAt) }}</text>
        </view>
        
        
        <view class="info-item" v-if="orderDetail.deadline">
          <text class="item-label">截止时间</text>
          <text class="item-value">{{ formatTime(orderDetail.deadline) }}</text>
        </view>
      </view>
      
      <!-- 工单详情 -->
      <view class="info-section">
        <view class="info-header">
          <text class="info-title">问题描述</text>
        </view>
        <view class="description-box">
          <text class="description-text">{{ orderDetail.description || '暂无描述' }}</text>
        </view>
      </view>
      
      <!-- 处理信息 -->
      <view class="info-section" v-if="orderDetail.status !== 'pending'">
        <view class="info-header">
          <text class="info-title">处理信息</text>
        </view>
        
        <view class="info-item" v-if="employeeDetail.name">
          <text class="item-label">员工姓名</text>
          <text class="item-value">{{ employeeDetail.name || '暂无' }}</text>
        </view>

        <view class="info-item" v-if="employeeDetail.phone">
          <text class="item-label">员工电话</text>
          <text class="item-value">{{ employeeDetail.phone }}</text>
        </view>

        <view class="info-item" v-if="employeeDetail.resolved_at">
          <text class="item-label">解决时间</text>
          <text class="item-value">{{ formatTime(orderDetail.resolved_at) || '未完成' }}</text>
        </view>
      </view>
    </view>
    
    <!-- 底部操作区 -->
    <view class="action-bar" v-if="orderDetail.status === 'pending' || orderDetail.status ==='assigned' || orderDetail.status ==='in_progress' ">
      <button class="cancel-btn" @click="cancelOrder">取消工单</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getOrderDetail, cancelOrder as apiCancelOrder, getOrderEmployeeDetail } from '@/api/orderAPI.js';
import { onLoad } from '@dcloudio/uni-app';  // 正确导入 onLoad

// 工单详情数据
const orderDetail = ref({});
const employeeDetail = ref({});
const loading = ref(false);
const markers = computed(() => {
  if (!orderDetail.value.location_latitude || !orderDetail.value.location_longitude) {
    return [];
  }
  return [{
    id: 1,
    latitude: orderDetail.value.locationLatitude,
    longitude: orderDetail.value.locationLongitude,
    iconPath: '/static/images/marker.png',
    width: 30,
    height: 30,
    anchor: {
      x: 0.5,
      y: 1.0
    }
  }];
});

// 获取页面参数
const orderId = ref('');

// 获取工单详情
const fetchOrderDetail = async () => {
  loading.value = true;
  try {
    const res = await getOrderDetail(orderId.value);
    if (res.status === 200) {
      
      orderDetail.value = res.data || {};
      // 获取员工姓名
      if(orderDetail.status !== 'pending'){
        const orderRes = await getOrderEmployeeDetail(orderDetail.value.orderId); 
        if(orderRes.status === 200){
          employeeDetail.value = orderRes.data || {};
          console.log(employeeDetail.value);
        }
      }
      
    } else {
      uni.showToast({
        title: res.message || '获取工单详情失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('获取工单详情失败', error);
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 取消工单
const cancelOrder = async () => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该工单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await apiCancelOrder(orderId.value);
          if (result.status === 200) {
            uni.showToast({
              title: '工单已取消',
              icon: 'success'
            });
            // 刷新工单详情
            fetchOrderDetail();
            // 通知列表页刷新
            uni.$emit('order-updated');
          } else {
            uni.showToast({
              title: result.message || '取消失败',
              icon: 'none'
            });
          }
        } catch (error) {
          console.error('取消工单失败', error);
          uni.showToast({
            title: '网络异常，请稍后重试',
            icon: 'none'
          });
        }
      }
    }
  });
};



// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '暂无';
  // 兼容 ISO 字符串
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

// 获取工单类型名称
const getOrderTypeName = (type) => {
  const typeMap = {
    'nrepair': '设备报修',
    'complaint': '服务投诉'
  };
  return typeMap[type] || '未知类型';
};
// 获取工单类型名称
const getRepairTypeName = (type) => {
  const typeMap = {
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
  return typeMap[type] || '未知类型';
};
// 获取状态文本
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

// 获取状态样式类
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


// 修改：使用 onLoad 钩子正确获取参数
onLoad((options) => {
  if (options && options.id) {
    orderId.value = options.id;
    fetchOrderDetail();
  } else {
    uni.showToast({
      title: '工单ID不存在',
      icon: 'none'
    });
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  }
});

// 移除有问题的 onMounted 和之前错误的 onLoad 定义
</script>

<style lang="scss" scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 30rpx;
}

.map-container {
  width: 100%;
  height: 420rpx;
  margin-top: 0;
  overflow: hidden;
  border-bottom-left-radius: 24rpx;
  border-bottom-right-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.order-map {
  width: 100%;
  height: 100%;
}

.order-card {
  margin: 20rpx 30rpx 30rpx;
  background-color: #fff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  overflow: hidden;
  z-index: 10;
  position: relative;
  border: 1rpx solid rgba(0, 0, 0, 0.03);
}

.status-bar {
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.03);
}

.status-text {
  font-size: 28rpx;
  font-weight: 600;
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
}

.status-pending {
  background-color: #FFF7E6;
  .status-text { color: #FA8C16; background-color: rgba(250, 140, 22, 0.1); }
}

.status-assigned {
  background-color: #E6F7FF;
  .status-text { color: #1890FF; background-color: rgba(24, 144, 255, 0.1); }
}

.status-in-progress {
  background-color: #F0F5FF;
  .status-text { color: #2F54EB; background-color: rgba(47, 84, 235, 0.1); }
}

.status-completed {
  background-color: #F6FFED;
  .status-text { color: #52C41A; background-color: rgba(82, 196, 26, 0.1); }
}

.status-closed {
  background-color: #F5F5F5;
  .status-text { color: #8C8C8C; background-color: rgba(140, 140, 140, 0.1); }
}

.info-section {
  padding: 30rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.03);
}

.info-header {
  margin-bottom: 24rpx;
}

.info-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  position: relative;
  padding-left: 20rpx;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 28rpx;
    background-color: #1890FF;
    border-radius: 3rpx;
  }
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.03);
  
  &:last-child {
    border-bottom: none;
  }
}

.item-label {
  font-size: 28rpx;
  color: #666;
}

.item-value {
  font-size: 28rpx;
  color: #333;
  max-width: 70%;
  text-align: right;
  font-weight: 500;
}

.description-box {
  padding: 24rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  margin-top: 10rpx;
}

.description-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.action-bar {
  padding: 30rpx;
  margin-top: 20rpx;
}

.cancel-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #fff;
  color: #ff4d4f;
  border: 2rpx solid #ff4d4f;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 500;
  transition: all 0.3s;
  
  &:active {
    background-color: rgba(255, 77, 79, 0.05);
  }
}
</style>
