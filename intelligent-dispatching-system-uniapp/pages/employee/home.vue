<template>
  <view class="home-container">
    <!-- 固定的顶部区域 -->
    <view class="fixed-header">
      <!-- 顶部安全区域 -->
      <view class="safe-area-top"></view>
      <!-- 标题区域 -->
      <view class="header">
        <text class="title">工单管理</text>
      </view>
      
      <!-- 切换标签 -->
      <view class="tab-container">
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'assigned' }"
          @click="switchTab('assigned')"
        >
          <text class="tab-text">未完成工单</text>
        </view>
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'completed' }"
          @click="switchTab('completed')"
        >
          <text class="tab-text">已完成工单</text>
        </view>
      </view>
    </view>
    
    <!-- 可滚动的内容区域 -->
    <scroll-view class="scrollable-content" scroll-y refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
      <!-- 工单列表 -->
      <view class="order-list">
        <!-- 无数据提示 -->
        <view class="empty-tip" v-if="filteredOrderList.length === 0">
          <image class="empty-image" src="/static/images/empty.png"></image>
          <text class="empty-text">{{ activeTab === 'pending' ? '暂无未完成工单' : '暂无已完成工单' }}</text>
        </view>
        
        <!-- 使用工单卡片组件 -->
        <OrderCard 
          v-for="(item, index) in filteredOrderList" 
          :key="index"
          :order="item"
          @click="viewOrderDetail"
        />
      </view>
    </scroll-view>
    
    <!-- 底部安全区域 -->
    <view class="safe-area-bottom">
      <!-- 底部导航栏 -->
      <TabBar role="technician" active="index" />
    </view>
  </view>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { getEmployeeOrders } from '@/api/orderAPI.js';
  import { onShow, onUnload } from '@dcloudio/uni-app';
  import TabBar from '@/components/tab-bar/index.vue';
  import OrderCard from '@/components/order-card/index.vue';

  // 工单列表
  const orderList = ref([]);
  const loading = ref(false);
  const refreshing = ref(false);
  const activeTab = ref('assigned'); // 默认显示未完成工单
  
  // 根据当前选中的标签过滤工单列表
  const filteredOrderList = computed(() => {
    if (activeTab.value === 'assigned') {
      // 未完成工单：状态为 pending, assigned, in_progress
      return orderList.value.filter(order => 
        ['pending', 'assigned', 'in_progress'].includes(order.status)
      );
    } else {
      // 已完成工单：状态为 completed, closed
      return orderList.value.filter(order => 
        ['completed', 'closed'].includes(order.status)
      );
    }
  });
  
  // 切换标签
  const switchTab = (tab) => {
    activeTab.value = tab;
  };
  
  // 下拉刷新
  const onRefresh = () => {
    refreshing.value = true;
    fetchEmployeeOrders().then(() => {
      refreshing.value = false;
    });
  };
  
  // 获取员工工单列表
  const fetchEmployeeOrders = async () => {
    loading.value = true;
    const userInfoStr = uni.getStorageSync('userInfo');
    
    if (!userInfoStr) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/index'
        });
      }, 1500);
      loading.value = false;
      return;
    }
    const employeeInfoStr = uni.getStorageSync('employeeInfo');
    const employeeInfo = JSON.parse(employeeInfoStr);
    if (!employeeInfo.employeeId) {
      uni.showToast({
        title: '员工ID不存在',
        icon: 'none'
      });
      loading.value = false;
      return;
    }
    
    try {
      const res = await getEmployeeOrders(employeeInfo.employeeId);
      if (res.status === 200 && res.data) {
        orderList.value = res.data.map(order => {
          // 处理工单数据，确保字段名称一致
          return {
            ...order,
            order_id: order.orderId || order.order_id,
            order_type: order.orderType || order.order_type,
            created_at: order.createdAt || order.created_at,
            assigned_employee: order.assignedEmployee || order.assigned_employee,
            resolved_at: order.resolvedAt || order.resolved_at,
            location_latitude: order.locationLatitude || order.location_latitude,
            location_longitude: order.locationLongitude || order.location_longitude
          };
        });
      } else {
        // 如果返回成功但没有数据，设置为空数组
        orderList.value = [];
        console.log('获取工单列表成功，但没有数据');
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
      url: `/pages/employee/order-detail?id=${orderId}`
    });
  };
  
  // 监听工单更新事件
  const handleOrderUpdated = () => {
    fetchEmployeeOrders();
  };
  
  onMounted(() => {
    fetchEmployeeOrders();
    // 监听工单更新事件
    uni.$on('order-updated', handleOrderUpdated);
  });
  
  onShow(() => {
    fetchEmployeeOrders();
  });
  
  // 页面卸载时移除事件监听
  onUnload(() => {
    uni.$off('order-updated', handleOrderUpdated);
  });
</script>

<style lang="scss" scoped>
.home-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: #f5f5f5;
  z-index: 100;
  padding: 0 30rpx;
  height: 240rpx; // 顶部高度（含safe-area-top、header和tab）
}

.safe-area-top {
  height: 100rpx; /* 顶部安全区域 */
}

.header {
  margin-bottom: 20rpx;
  padding: 20rpx 0;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

/* 标签切换样式 */
.tab-container {
  display: flex;
  margin-bottom: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  position: relative;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
}

.tab-item.active .tab-text {
  color: #007aff;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background-color: #007aff;
  border-radius: 2rpx;
}

.scrollable-content {
  flex: 1;
  margin-top: 240rpx; /* 与fixed-header高度一致 */
  padding: 0 30rpx;
  padding-bottom: 120rpx; /* 留出底部安全区空间，避免被底部导航遮挡 */
  box-sizing: border-box;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-top: 50rpx;
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

.safe-area-bottom {
  height: 100rpx;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
}
</style>