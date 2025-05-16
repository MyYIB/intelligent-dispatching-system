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
      
      <!-- 回访信息 - 仅在已完成状态显示 -->
      <view class="info-section" v-if="orderDetail.status === 'completed'">
        <view class="info-header">
          <text class="info-title">回访信息</text>
        </view>
        
        <view v-if="!feedbackInfo" class="empty-data">
          <text>暂未进行回访</text>
        </view>
        
        <view v-else>
          <view class="info-item">
            <text class="item-label">回访状态</text>
            <text class="item-value" :class="getFeedbackStateClass(feedbackInfo.feedbackState)">
              {{ getFeedbackStateText(feedbackInfo.feedbackState) }}
            </text>
          </view>
          
          <view class="info-item" v-if="feedbackInfo.feedbackState === 'completed'">
            <text class="item-label">满意度评分</text>
            <view class="satisfaction-score">
              <uni-rate :value="feedbackInfo.satisfactionScore || 0" :size="18" :readonly="true" />
              <text class="score-text">{{ feedbackInfo.satisfactionScore || 0 }}分</text>
            </view>
          </view>
          <view class="info-item" v-if="feedbackInfo.needTime">
            <text class="item-label">回访时间</text>
            <text class="item-value">{{ formatTime(feedbackInfo.needTime) }}</text>
          </view>
          <view class="info-item" v-if="feedbackInfo.feedbackTime">
            <text class="item-label">完成回访时间</text>
            <text class="item-value">{{ formatTime(feedbackInfo.feedbackTime) }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部操作区 -->
    <view class="action-bar">
      <button 
        class="cancel-btn" 
        @click="cancelOrder"
        v-if="orderDetail.status === 'pending' || orderDetail.status ==='assigned' || orderDetail.status ==='in_progress'"
      >
        取消工单
      </button>
      
      <button 
        class="rate-btn" 
        @click="openRatePopup"
        v-if="orderDetail.status === 'completed' && feedbackInfo && feedbackInfo.feedbackState === 'unrated'"
      >
        评价服务
      </button>
    </view>
    
    <!-- 评分弹窗 -->
    <uni-popup ref="ratePopup" type="center">
      <view class="rate-popup">
        <view class="popup-header">
          <text class="popup-title">服务评价</text>
          <uni-icons type="close" size="20" color="#999" @click="closeRatePopup"></uni-icons>
        </view>
        
        <view class="popup-content">
          <view class="rate-form">
            <view class="form-item">
              <text class="form-label">满意度评分</text>
              <uni-rate v-model="rateForm.satisfaction_score" :size="24" />
            </view>
          </view>
        </view>
        
        <view class="popup-footer">
          <button class="cancel-btn-small" @click="closeRatePopup">取消</button>
          <button class="confirm-btn" @click="submitRating">提交评价</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getOrderDetail, cancelOrder as apiCancelOrder, getOrderEmployeeDetail, getFeedbackByOrderId, submitUserRating } from '@/api/orderAPI.js';
import { onLoad } from '@dcloudio/uni-app';  // 正确导入 onLoad

// 工单详情数据
const orderDetail = ref({});
const employeeDetail = ref({});
const loading = ref(false);
const feedbackInfo = ref(null);
const ratePopup = ref(null);
const rateForm = ref({
  satisfaction_score: 5,
});

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
      if(orderDetail.value.status !== 'pending'){
        const orderRes = await getOrderEmployeeDetail(orderDetail.value.orderId); 
        if(orderRes.status === 200){
          employeeDetail.value = orderRes.data || {};
          console.log(employeeDetail.value);
        }
      }
      
      // 如果工单状态是已完成，获取回访信息
      if(orderDetail.value.status === 'completed'){
        await fetchFeedbackInfo();
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

// 获取回访信息
const fetchFeedbackInfo = async () => {
  try {
    const res = await getFeedbackByOrderId(orderId.value);
    if (res.status === 200) {
      feedbackInfo.value = res.data;
    } else {
      feedbackInfo.value = null;
    }
  } catch (error) {
    console.error('获取回访信息失败', error);
    feedbackInfo.value = null;
  }
};

// 打开评分弹窗
const openRatePopup = () => {
  // 重置表单
  rateForm.value = {
    satisfaction_score: 5,
  };
  
  ratePopup.value.open();
};

// 关闭评分弹窗
const closeRatePopup = () => {
  ratePopup.value.close();
};

// 提交评分
const submitRating = async () => {
  if (rateForm.value.satisfaction_score < 1) {
    uni.showToast({
      title: '请至少选择1星评分',
      icon: 'none'
    });
    return;
  }
  
  loading.value = true;
  
  try {
    const userInfoStr = uni.getStorageSync('userInfo');
    if (!userInfoStr) {
      throw new Error('请先登录');
    }
    
    const userInfo = JSON.parse(userInfoStr);
    
    const ratingData = {
      feedback_id: feedbackInfo.value.feedbackId,
      order_id: parseInt(orderId.value),
      user_id: userInfo.userId,
      satisfaction_score: rateForm.value.satisfaction_score,
      rating_time: new Date().toISOString()
    };
    
    const res = await submitUserRating(ratingData);
    
    if (res.status === 200) {
      uni.showToast({
        title: '评价已提交',
        icon: 'success'
      });
      
      // 关闭弹窗
      closeRatePopup();
      
      // 刷新回访信息
      await fetchFeedbackInfo();
    } else {
      throw new Error(res.msg || '提交评价失败');
    }
  } catch (error) {
    console.error('提交评价失败', error);
    uni.showToast({
      title: error.message || '网络异常，请稍后重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 获取回访状态文本
const getFeedbackStateText = (state) => {
  const stateMap = {
    'uncompleted': '待回访',
    'completed': '已回访',
    'unrated': '待评价'
  };
  return stateMap[state] || '未知状态';
};

// 获取回访状态样式类
const getFeedbackStateClass = (state) => {
  const classMap = {
    'uncompleted': 'state-pending',
    'completed': 'state-completed',
    'unrated': 'state-unrated'
  };
  return classMap[state] || '';
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
/* 回访状态样式 */
.state-pending {
  color: #fa8c16;
}

.state-in-progress {
  color: #1890ff;
}

.state-completed {
  color: #52c41a;
}

.state-unrated {
  color: #722ed1;
}

/* 满意度评分样式 */
.satisfaction-score {
  display: flex;
  align-items: center;
}

.score-text {
  margin-left: 10rpx;
  font-size: 26rpx;
  color: #666;
}

/* 空数据提示 */
.empty-data {
  padding: 30rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}

/* 评价按钮样式 */
.rate-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #722ed1;
  color: #fff;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 500;
  transition: all 0.3s;
  
  &:active {
    background-color: #5b21b6;
  }
}

/* 评分弹窗样式 */
.rate-popup {
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
  padding: 30rpx;
}

.rate-form {
  padding: 20rpx 0;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.rate-textarea {
  width: 100%;
  height: 200rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.textarea-counter {
  font-size: 24rpx;
  color: #999;
  text-align: right;
  display: block;
  margin-top: 10rpx;
}

.popup-footer {
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  border-top: 1rpx solid #eee;
}

.cancel-btn-small {
  width: 45%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #f5f5f5;
  color: #666;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.confirm-btn {
  width: 45%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #722ed1;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
}
</style>
