<script setup>
import { ref, onMounted} from 'vue';
import { onShow } from '@dcloudio/uni-app';
import TabBar from '@/components/tab-bar/index.vue';

// 用户信息
const userInfo = ref(null);
const isLogin = ref(false);
const userRole = ref('customer');

// 菜单项列表，根据登录状态和角色动态显示
const menuList = ref([]);

// 检查登录状态
const checkLoginStatus = () => {
  const token = uni.getStorageSync('token');
  const userInfoStr = uni.getStorageSync('userInfo');
  
  if (token && userInfoStr) {
    try {
      userInfo.value = JSON.parse(userInfoStr);
      userRole.value = userInfo.value.role || 'customer';
      isLogin.value = true;
      
      // 更新菜单项，根据角色设置菜单
      updateMenuItems();
      
    } catch (e) {
      isLogin.value = false;
      updateMenuItems();
    }
  } else {
    isLogin.value = false;
    updateMenuItems();
  }
};

// 根据登录状态和角色更新菜单项
const updateMenuItems = () => {
    if (userRole.value === 'technician') {
      // 技术员菜单项
      menuList.value = [
        { id: 1, text: '个人资料', icon: 'person', url: '/pages/employee/profile' },
        { id: 2, text: '技能管理', icon: 'star', url: '/pages/employee/skills' },
        { id: 3, text: '关于', useImage: true, url: '/pages/base/about' },
        { id: 4, text: '退出登录', isLogout: true, useImage: true }
      ];
    } else {
      // 普通用户菜单项
      menuList.value = [
        { id: 1, text: '个人资料', icon: 'person', url: '/pages/user/profile' },
        { id: 2, text: '地址管理', icon: 'location', url: '/pages/user/address' },
        { id: 3, text: '关于', useImage: true, url: '/pages/base/about' },
        { id: 4, text: '退出登录', isLogout: true, useImage: true }
      ];
    }
  
};

// 跳转到登录页面或用户信息页面
const goToLogin = () => {
  if (!isLogin.value) {
    uni.navigateTo({
      url: '/pages/login/Login'
    });
  } else {
    // 已登录用户点击头像跳转到用户信息页面
    const userInfoPage = userRole.value === 'technician' ? '/pages/employee/info' : '/pages/user/info';
    uni.redirectTo({
      url: userInfoPage
    });
  }
};

// 跳转到菜单项对应的页面
const handleMenuClick = (item) => {
  if (item.isLogout) {
    // 处理退出登录
    uni.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: function (res) {
        if (res.confirm) {
          // 清除登录信息
          uni.removeStorageSync('token');
          uni.removeStorageSync('userInfo');
          
          // 更新状态
          isLogin.value = false;
          userInfo.value = null;
          userRole.value = 'customer';
          
          // 更新菜单项
          updateMenuItems();
          
          // 显示提示
          uni.showToast({
            title: '已退出登录',
            icon: 'success'
          });
        }
      }
    });
  } else {
    uni.navigateTo({
      url: item.url
    });
  }
};

onMounted(() => {
  checkLoginStatus();
});

onShow(() => {
  checkLoginStatus();
});
</script>

<template>
  <view class="container">
    <!-- 顶部安全区域，避免被状态栏遮挡 -->
    <view class="safe-area-top"></view>
    
    <!-- 用户信息区域 -->
    <view class="user-info-section">
      <view class="avatar-container" @click="goToLogin">
        <image class="avatar" src="/static/images/default-avatar.png" v-if="!isLogin"></image>
        <image class="avatar" :src="userInfo?.avatar || '/static/images/default-avatar.png'" v-else></image>
      </view>
      <view class="user-name" @click="goToLogin">{{ isLogin ? userInfo?.username : '未登录' }}</view>
    </view>
    
    <!-- 菜单列表 -->
    <view class="menu-list">
    <view 
      class="menu-item" 
      v-for="item in menuList" 
      :key="item.id" 
      @click="handleMenuClick(item)"
    >
      <view class="menu-item-left">
        <view class="icon-wrapper">
          <image 
            v-if="item.useImage && item.isLogout" 
            src="/static/images/logout.png" 
            class="menu-icon"
          ></image>
          <image 
            v-else-if="item.useImage && item.text === '关于'" 
            src="/static/images/about.png" 
            class="menu-icon"
          ></image>
          <uni-icons v-else :type="item.icon" size="20" color="#666"></uni-icons>
        </view>
        <text class="menu-text">{{ item.text }}</text>
      </view>
      <uni-icons type="right" size="16" color="#ccc"></uni-icons>
    </view>
   </view>
    
    <!-- 底部安全区域，确保内容不被底部导航栏遮挡 -->
    <view class="safe-area-bottom"></view>
    
    <!-- 底部导航栏 -->
    <TabBar :role="userRole" active="user" />
  </view>
</template>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  padding-bottom: 120rpx; /* 为底部导航栏预留空间 */
  position: fixed; /* 固定容器位置，防止滚动 */
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
}

.safe-area-top {
  height: 170rpx; /* 顶部安全区域高度，避免被状态栏遮挡 */
}

.user-info-section {
  background-color: #fff;
  padding: 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar-container {
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  overflow: hidden;
  background-color: #e0e0e0;
  margin-bottom: 20rpx;
}

.avatar {
  width: 100%;
  height: 100%;
}

.user-name {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
}

.menu-list {
  background-color: #fff;
  border-radius: 12rpx;
  margin: 0 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item-left {
  display: flex;
  align-items: center;
}

.icon-wrapper {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.menu-icon {
  width: 36rpx;
  height: 36rpx;
}

.menu-text {
  margin-left: 20rpx;
  font-size: 28rpx;
  color: #333;
}

.safe-area-bottom {
  height: 50rpx; /* 额外的安全区域，防止内容被底部导航栏遮挡 */
}
</style>