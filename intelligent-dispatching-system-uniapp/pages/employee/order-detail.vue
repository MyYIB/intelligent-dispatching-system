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
      
      <!-- 用户信息 -->
      <view class="info-section">
        <view class="info-header">
          <text class="info-title">用户信息</text>
        </view>
        
        <view class="info-item">
          <text class="item-label">用户姓名</text>
          <text class="item-value">{{ userInfo.username || '暂无' }}</text>
        </view>
        
        <view class="info-item">
          <text class="item-label">联系电话</text>
          <text class="item-value">{{ userInfo.phone || '暂无' }}</text>
        </view>
      </view>
      
      <!-- 物料使用情况 - 仅在处理中或已完成状态显示 -->
      <view class="info-section" v-if="orderDetail.status === 'in_progress' || orderDetail.status === 'completed'">
        <view class="info-header">
          <text class="info-title">物料使用情况</text>
        </view>
        
        <view v-if="inventoryUseList.length === 0" class="empty-data">
          <text>暂无物料使用记录</text>
        </view>
        
        <view v-else class="inventory-list">
          <view v-for="(item, index) in inventoryUseList" :key="index" class="inventory-item">
            <view class="inventory-name">{{ item.item_name }}</view>
            <view class="inventory-quantity">使用数量: {{ item.num }}</view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部操作区 -->
    <view class="action-bar">
      <button 
        class="primary-btn" 
        :class="{'process-btn': orderDetail.status === 'assigned', 'complete-btn': orderDetail.status === 'in_progress'}"
        @click="handleOrderAction"
        :loading="loading"
      >
        {{ getActionButtonText() }}
      </button>
    </view>
    
    <!-- 备料使用弹窗 -->
    <uni-popup ref="inventoryPopup" type="center">
      <view class="inventory-popup">
        <view class="popup-header">
          <text class="popup-title">选择使用备料</text>
          <uni-icons type="close" size="20" color="#999" @click="closeInventoryPopup"></uni-icons>
        </view>
        
        <view class="popup-content">
          <view v-if="inventoryList.length === 0" class="empty-data">
            <text>暂无可用备料</text>
          </view>
          
          <scroll-view v-else scroll-y class="inventory-scroll">
            <view v-for="(item, index) in inventoryList" :key="item.itemId" class="inventory-select-item">
              <view class="inventory-info">
                <text class="inventory-name">{{ item.itemName }}</text>
                <text class="inventory-stock">库存: {{ item.quantity }}</text>
              </view>
              
              <view class="quantity-control">
                <button class="quantity-btn" @click="decreaseQuantity(index)">-</button>
                <input 
                  type="number" 
                  class="quantity-input" 
                  v-model="item.useQuantity" 
                  @input="validateQuantity(index)"
                />
                <button class="quantity-btn" @click="increaseQuantity(index)">+</button>
              </view>
            </view>
          </scroll-view>
        </view>
        
        <view class="popup-footer">
          <button class="cancel-btn" @click="closeInventoryPopup">取消</button>
          <button class="confirm-btn" @click="confirmUseInventory">确认使用</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getOrderDetail, updateOrderStatus,getOrderInventory ,fetchInventoryList, submitInventoryUse,getOrderUserInfo} from '@/api/orderAPI.js';
import { onLoad } from '@dcloudio/uni-app';

// 工单详情数据
const orderDetail = ref({});
const loading = ref(false);
const orderId = ref('');
const userInfo = ref({});
// 备料相关
const inventoryPopup = ref(null);
const inventoryList = ref([]);
const inventoryUseList = ref([]);

// 地图标记
const markers = computed(() => {
  if (!orderDetail.value.locationLatitude || !orderDetail.value.locationLongitude) {
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

// 获取工单详情
const fetchOrderDetail = async () => {
  loading.value = true;
  try {
    const res = await getOrderDetail(orderId.value);
    if (res.status === 200) {
      orderDetail.value = res.data || {};
      
      // 如果工单状态是处理中或已完成，获取物料使用情况
      if (orderDetail.value.status === 'in_progress' || orderDetail.value.status === 'completed') {
        const inventoryRes = await getOrderInventory(orderId.value);
        if (inventoryRes.status === 200) {
          inventoryUseList.value = inventoryRes.data || [];
        }
      }
      // 获取用户信息
      if (orderDetail.value.status !== 'pending') {
        const orderRes = await getOrderUserInfo(orderId.value);
        if (orderRes.status === 200) {
          userInfo.value = orderRes.data || {};
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

// 获取备料列表
const getInventoryList = async () => {
  try {
    const res = await fetchInventoryList();
    
    if (res.status === 200 ) {
      // 添加使用数量字段
      inventoryList.value = res.data.map(item => ({
        ...item,
        useQuantity: 0
      }));
    } else {
      uni.showToast({
        title: '获取备料列表失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('获取备料列表失败', error);
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    });
  }
};



// 处理工单操作
const handleOrderAction = () => {
  if (orderDetail.value.status === 'assigned') {
    // 已分配状态，打开备料选择弹窗
    openInventoryPopup();
  } else if (orderDetail.value.status === 'in_progress') {
    // 处理中状态，完成工单
    completeOrder();
  }
};

// 打开备料选择弹窗
const openInventoryPopup = async () => {
  await getInventoryList();
  inventoryPopup.value.open();
};

// 关闭备料选择弹窗
const closeInventoryPopup = () => {
  inventoryPopup.value.close();
};

// 增加备料使用数量
const increaseQuantity = (index) => {
  const item = inventoryList.value[index];
  if (item.useQuantity < item.quantity) {
    item.useQuantity++;
  }
};

// 减少备料使用数量
const decreaseQuantity = (index) => {
  const item = inventoryList.value[index];
  if (item.useQuantity > 0) {
    item.useQuantity--;
  }
};

// 验证输入的数量
const validateQuantity = (index) => {
  const item = inventoryList.value[index];
  let value = parseInt(item.useQuantity);
  
  if (isNaN(value) || value < 0) {
    item.useQuantity = 0;
  } else if (value > item.quantity) {
    item.useQuantity = item.quantity;
  }
};

// 确认使用备料
const confirmUseInventory = async () => {
  // 筛选出使用数量大于0的备料
  const selectedItems = inventoryList.value.filter(item => item.useQuantity > 0);
  
  if (selectedItems.length === 0) {
    uni.showToast({
      title: '请选择至少一种备料',
      icon: 'none'
    });
    return;
  }
  
  loading.value = true;
  
  try {
    // 添加备料使用记录
    for (const item of selectedItems) {
        const inventoryUse = {
            inventoryId: item.itemId,
            orderId: parseInt(orderId.value),
            num: parseInt(item.useQuantity)
        }
        const res = await submitInventoryUse(inventoryUse);
      
        if (!res || res.status !== 200) {
          throw new Error(res?.message || '添加备料使用记录失败');
        }
    }
    
    // 更新工单状态为处理中
    const updateRes = await updateOrderStatus(parseInt(orderId.value), 'in_progress');
    
    if (updateRes.status === 200) {
      uni.showToast({
        title: '开始处理工单',
        icon: 'success'
      });
      
      // 关闭弹窗
      closeInventoryPopup();
      
      // 刷新工单详情
      fetchOrderDetail();
      
      // 通知列表页刷新
      uni.$emit('order-updated');
    } else {
      throw new Error(updateRes.message || '更新工单状态失败');
    }
  } catch (error) {
    console.error('处理工单失败', error);
    uni.showToast({
      title: error.message || '网络异常，请稍后重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 完成工单
const completeOrder = async () => {
  uni.showModal({
    title: '确认完成',
    content: '确定已解决问题并完成该工单吗？',
    success: async (res) => {
      if (res.confirm) {
        loading.value = true;
        
        try {
          const updateRes = await updateOrderStatus(parseInt(orderId.value), 'completed');
          
          if (updateRes.status === 200) {
            uni.showToast({
              title: '工单已完成',
              icon: 'success'
            });
            
            // 刷新工单详情
            fetchOrderDetail();
            
            // 通知列表页刷新
            uni.$emit('order-updated');
          } else {
            throw new Error(updateRes.message || '完成工单失败');
          }
        } catch (error) {
          console.error('完成工单失败', error);
          uni.showToast({
            title: error.message || '网络异常，请稍后重试',
            icon: 'none'
          });
        } finally {
          loading.value = false;
        }
      }
    }
  });
};


// 获取操作按钮文本
const getActionButtonText = () => {
  const statusMap = {
    'assigned': '开始处理',
    'in_progress': '完成工单'
  };
  return statusMap[orderDetail.value.status] || '';
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

// 获取报修类型名称
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
    'assigned': '已分配',
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

// 页面加载
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
</script>

<style lang="scss" scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 120rpx;
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
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.primary-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 500;
  transition: all 0.3s;
}

.process-btn {
  background-color: #1890FF;
  color: #fff;
  
  &:active {
    background-color: #096dd9;
  }
}

.complete-btn {
  background-color: #52C41A;
  color: #fff;
  
  &:active {
    background-color: #389e0d;
  }
}

.empty-data {
  padding: 40rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}

.inventory-list {
  margin-top: 20rpx;
}

.inventory-item {
  padding: 20rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.inventory-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.inventory-quantity {
  font-size: 26rpx;
  color: #666;
}

/* 备料选择弹窗样式 */
.inventory-popup {
  width: 650rpx;
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.popup-header {
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #eee;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.popup-content {
  max-height: 700rpx;
  padding: 0 30rpx;
}

.inventory-scroll {
  max-height: 600rpx;
}

.inventory-select-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #eee;
}

.inventory-info {
  flex: 1;
}

.inventory-stock {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.quantity-btn {
  width: 60rpx;
  height: 60rpx;
  line-height: 56rpx;
  text-align: center;
  border: 1rpx solid #ddd;
  background-color: #f5f5f5;
  color: #333;
  font-size: 28rpx;
  padding: 0;
}

.quantity-input {
  width: 80rpx;
  height: 60rpx;
  text-align: center;
  border-top: 1rpx solid #ddd;
  border-bottom: 1rpx solid #ddd;
  margin: 0 2rpx;
  font-size: 28rpx;
}

.popup-footer {
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  border-top: 1rpx solid #eee;
}

.cancel-btn, .confirm-btn {
  width: 280rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background-color: #1890FF;
  color: #fff;
}
</style>