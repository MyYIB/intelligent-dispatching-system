<template>
  <view class="home-container">
    <!-- 固定的顶部区域 -->
    <view class="fixed-header">
      <!-- 顶部安全区域 -->
      <view class="safe-area-top"></view>
      <!-- 标题区域 -->
      <view class="header">
        <text class="title">我的报修</text>
      </view>
    </view>
    
    <!-- 可滚动的内容区域 -->
    <scroll-view class="scrollable-content" scroll-y>
      <!-- 工单列表 -->
      <view class="order-list">
        <!-- 无数据提示 -->
        <view class="empty-tip" v-if="orderList.length === 0">
          <image class="empty-image" src="/static/images/empty.png"></image>
          <text class="empty-text">暂无报修记录</text>
        </view>
        
        <!-- 使用工单卡片组件 -->
        <OrderCard 
          v-for="(item, index) in orderList" 
          :key="index"
          :order="item"
          @click="viewOrderDetail"
        />
      </view>
    </scroll-view>
    
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
  import OrderCard from '@/components/order-card/index.vue';

  // 工单列表
  const orderList = ref([]);
  const loading = ref(false);
  
  // 获取用户工单列表
  const fetchUserOrders = async () => {
    loading.value = true;
    const userInfoStr = uni.getStorageSync('userInfo');
    const userInfo = JSON.parse(userInfoStr);
    try {
      const res = await getUserOrders(userInfo.userId);
      if (res.status === 200) {
        console.log(res.data)
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
  
  onMounted(() => {
    fetchUserOrders();
    uni.$on('order-updated', fetchUserOrders); 
  });
  
  onShow(() => {
    fetchUserOrders();
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
  height: 180rpx; // 顶部高度（含safe-area-top和header）
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

.scrollable-content {
  flex: 1;
  margin-top: 180rpx; /* 与fixed-header高度一致 */
  padding: 0 30rpx;
  padding-bottom: 120rpx; /* 留出底部安全区空间，避免被底部导航遮挡 */
  box-sizing: border-box;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  /* 移除padding-bottom，避免重复 */
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