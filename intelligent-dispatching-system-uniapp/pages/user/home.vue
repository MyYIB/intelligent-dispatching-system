<template>
    <view class="home-container">
      <!-- 标题区域 -->
      <view class="header">
        <text class="title">我的报修</text>
      </view>
      
      <!-- 工单列表 -->
      <view class="order-list">
        <!-- 无数据提示 -->
        <view class="empty-tip" v-if="orderList.length === 0">
          <image class="empty-image" src="/static/images/empty.png"></image>
          <text class="empty-text">暂无报修记录</text>
        </view>
        
        <!-- 工单卡片 -->
        <view 
          class="order-card" 
          v-for="(item, index) in orderList" 
          :key="index"
          @click="viewOrderDetail(item.id)"
        >
          <view class="order-header">
            <text class="order-type">{{ item.type }}</text>
            <text class="order-status" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</text>
          </view>
          
          <view class="order-content">
            <text class="order-desc">{{ item.description }}</text>
          </view>
          
          <view class="order-footer">
            <text class="order-time">{{ formatTime(item.createTime) }}</text>
          </view>
        </view>
      </view>
      
      <!-- 底部安全区域 -->
      <view class="safe-area-bottom">
            <!-- 底部导航栏 -->
            <TabBar role="user" active="index" />
      </view>
    </view>
  </template>
  
  <script setup>
  import { ref, onMounted} from 'vue';
  import { getUserOrders } from '@/api/orderAPI.js';
  import { onShow } from '@dcloudio/uni-app';
  import TabBar from '@/components/tab-bar/index.vue';

  // 工单列表
  const orderList = ref([]);
  const loading = ref(false);
  
  // 获取用户工单列表
  const fetchUserOrders = async () => {
    loading.value = true;
    try {
      const res = await getUserOrders();
      if (res.code === 200) {
        orderList.value = res.data || [];
      } else {
        uni.showToast({
          title: res.message || '获取工单失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('获取工单列表失败', error);
      uni.showToast({
        title: '网络异常，请稍后重试',
        icon: 'none'
      });
    } finally {
      loading.value = false;
    }
  };
  
  // 查看工单详情
  const viewOrderDetail = (orderId) => {
    uni.navigateTo({
      url: `/pages/user/order-detail?id=${orderId}`
    });
  };
  
  // 格式化时间
  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hour = date.getHours().toString().padStart(2, '0');
    const minute = date.getMinutes().toString().padStart(2, '0');
    
    return `${year}-${month}-${day} ${hour}:${minute}`;
  };
  
  // 获取状态文本
  const getStatusText = (status) => {
    const statusMap = {
      'pending': '待处理',
      'processing': '处理中',
      'completed': '已完成',
      'cancelled': '已取消'
    };
    return statusMap[status] || '未知状态';
  };
  
  // 获取状态样式类
  const getStatusClass = (status) => {
    const classMap = {
      'pending': 'status-pending',
      'processing': 'status-processing',
      'completed': 'status-completed',
      'cancelled': 'status-cancelled'
    };
    return classMap[status] || '';
  };
  
  onMounted(() => {
    fetchUserOrders();
  });
  
  onShow(() => {
    fetchUserOrders();
  });
  </script>
  
  <style lang="scss" scoped>
  .home-container {
    padding: 30rpx;
    background-color: #f5f5f5;
    min-height: 100vh;
  }
  
  .header {
    margin-bottom: 30rpx;
    padding: 20rpx 0;
  }
  
  .title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
  
  .order-list {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }
  
  .empty-tip {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
  }
  
  .empty-image {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 20rpx;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
  
  .order-card {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 30rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  }
  
  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  
  .order-type {
    font-size: 32rpx;
    font-weight: 500;
    color: #333;
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
  
  .status-processing {
    background-color: #E6F7FF;
    color: #1890FF;
  }
  
  .status-completed {
    background-color: #F6FFED;
    color: #52C41A;
  }
  
  .status-cancelled {
    background-color: #F5F5F5;
    color: #999;
  }
  
  .order-content {
    margin-bottom: 20rpx;
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
  
  .safe-area-bottom {
    height: 100rpx;
  }
  </style>