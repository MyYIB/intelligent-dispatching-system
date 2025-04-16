<script setup>
import { ref, onMounted } from 'vue';
import SignOverlay from '../overlay/sign.vue';
import OrderOverlay from '../overlay/order.vue';

const props = defineProps({
  role: {
    type: String,
    default: 'customer'
  },
  active: {
    type: String,
    default: 'user'
  }
});

// 用户导航项
const customerTabs = [
  { id: 'index', text: '首页', icon: 'home', url: '/pages/user/home' },
  { id: 'add', text: '', icon: '', isCenter: true },
  { id: 'user', text: '我的', icon: 'person', url: '/pages/user/info' }
];

// 员工导航项
const employeeTabs = [
  { id: 'index', text: '工作台', icon: 'home', url: '/pages/employee/home' },
  { id: 'add', text: '', icon: 'add', isCenter: true },
  { id: 'user', text: '我的', icon: 'person', url: '/pages/user/info' }
];

// 根据角色选择导航项
const tabs = ref([]);
// 控制悬浮框显示
const showOverlay = ref(false);

onMounted(() => {
  if (props.role === 'technician') {
    tabs.value = employeeTabs;
  } else {
    tabs.value = customerTabs;
  }
});

// 切换页面前检查登录状态
const switchTab = (url, isCenter = false) => {
  if (isCenter) {
    // 检查登录状态
    const token = uni.getStorageSync('token');
    const userInfo = uni.getStorageSync('userInfo');
    
    if (!token || !userInfo) {
      // 未登录，显示提示
      uni.showToast({
        title: '请先登录',
        icon: 'none',
        duration: 1500
      });
      
      // 延迟跳转到登录页面
      setTimeout(() => {
        uni.redirectTo({
          url: '/pages/login/Login'
        });
      }, 1500);
      return;
    }
    
    // 已登录，显示悬浮框
    showOverlay.value = true;
    return;
  }
  
  // 检查登录状态
  const token = uni.getStorageSync('token');
  const userInfo = uni.getStorageSync('userInfo');
  
  if (!token || !userInfo) {
    // 未登录，显示提示
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 1500
    });
    
    // 延迟跳转到登录页面
    setTimeout(() => {
      uni.redirectTo({
        url: '/pages/login/Login'
      });
    }, 1500);
    return;
  }
  
  // 已登录，正常跳转
  uni.redirectTo({
    url: url
  });
};

// 关闭悬浮框
const closeOverlay = () => {
  showOverlay.value = false;
};

// 处理签到/报修成功
const handleSuccess = () => {
  // 可以在这里添加额外的处理逻辑
};
</script>

<template>
  <view class="custom-tab-bar">
    <view 
      v-for="(item, index) in tabs" 
      :key="item.id"
      :class="['tab-item', { 'active': active === item.id, 'center-item': item.isCenter }]"
      @click="item.isCenter ? switchTab('', true) : switchTab(item.url)"
    >
      <view :class="['tab-icon', { 'center-icon': item.isCenter }]">
        <!-- 使用图片替代uni-icons -->
        <image 
          v-if="item.isCenter" 
          src="/static/images/add-circle.png" 
          class="add-image"
          mode="aspectFit"
        ></image>
        <uni-icons 
          v-else
          :type="item.icon" 
          size="24" 
          :color="active === item.id ? '#007aff' : '#999'"
        ></uni-icons>
      </view>
      <text 
        v-if="!item.isCenter"
        class="tab-text" 
        :class="{ 'active-text': active === item.id }"
      >{{ item.text }}</text>
    </view>
  </view>
  
  <!-- 根据角色显示不同的悬浮框组件 -->
  <SignOverlay 
    v-if="showOverlay && props.role === 'technician'" 
    @close="closeOverlay"
    @success="handleSuccess"
  />
  
  <OrderOverlay 
    v-if="showOverlay && props.role !== 'technician'" 
    @close="closeOverlay"
    @success="handleSuccess"
  />
</template>

<style lang="scss" scoped>
.custom-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background-color: #fff;
  display: flex;
  border-top: 1px solid #eee;
  z-index: 999;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.center-item {
  margin-top: -30rpx;
  z-index: 1000;
}

.tab-icon {
  display: flex;
  justify-content: center;
  align-items: center;
}

.center-icon {
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  background-color: #007aff;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.2);
  transform: translateY(-15rpx);
}

.add-image {
  width: 50rpx;
  height: 50rpx;
}

.tab-text {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
}

.active-text {
  color: #007aff;
}
</style>