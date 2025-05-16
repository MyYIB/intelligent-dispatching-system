<template>
  <view class="home-container">
    <!-- 固定的顶部区域 -->
    <view class="fixed-header">
      <!-- 顶部安全区域 -->
      <view class="safe-area-top"></view>
      <!-- 标题区域 -->
      <view class="header">
        <text class="title">工作管理</text>
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
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'maintenance' }"
          @click="switchTab('maintenance')"
        >
          <text class="tab-text">设备维护</text>
        </view>
      </view>
    </view>
    
    <!-- 可滚动的内容区域 -->
    <scroll-view class="scrollable-content" scroll-y refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
      <!-- 工单列表 -->
      <view class="order-list" v-if="activeTab === 'assigned' || activeTab === 'completed'">
        <!-- 无数据提示 -->
        <view class="empty-tip" v-if="filteredOrderList.length === 0">
          <image class="empty-image" src="/static/images/empty.png"></image>
          <text class="empty-text">{{ activeTab === 'assigned' ? '暂无未完成工单' : '暂无已完成工单' }}</text>
        </view>
        
        <!-- 使用工单卡片组件 -->
        <OrderCard 
          v-for="(item, index) in filteredOrderList" 
          :key="index"
          :order="item"
          @click="viewOrderDetail"
        />
      </view>
      
      <!-- 设备维护列表 -->
      <view class="maintenance-list" v-if="activeTab === 'maintenance'">
        <!-- 无数据提示 -->
        <view class="empty-tip" v-if="maintenanceList.length === 0">
          <image class="empty-image" src="/static/images/empty.png"></image>
          <text class="empty-text">暂无设备维护任务</text>
        </view>
        
        <!-- 设备维护卡片 -->
        <view 
          class="maintenance-card" 
          v-for="(item, index) in maintenanceList" 
          :key="index"
        >
          <view class="card-header">
            <text class="equipment-name">{{ item.equipmentName }}</text>
            <view class="status-tag" :class="getMaintenanceStatusClass(item.status)">
              {{ getMaintenanceStatusText(item.status) }}
            </view>
          </view>
          
          <view class="card-content">
            <view class="info-row">
              <text class="info-label">维护ID:</text>
              <text class="info-value">{{ item.maintenanceId }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">计划时间:</text>
              <text class="info-value">{{ formatDate(item.maintenanceDate) }}</text>
            </view>
            <view class="info-row details-row">
              <text class="info-label">维护内容:</text>
              <text class="info-value details-text">{{ item.details }}</text>
            </view>
          </view>
          
          <view class="card-footer">
            <button 
              class="action-btn start-btn" 
              v-if="item.status === 'pending'"
              @click.stop="startMaintenance(item.maintenanceId)"
            >开始维护</button>
            <button 
              class="action-btn complete-btn" 
              v-if="item.status === 'in_progress'"
              @click.stop="completeMaintenance(item.maintenanceId)"
            >完成维护</button>
            
          </view>
        </view>
        
        <!-- 添加设备维护按钮 -->
        <view class="add-maintenance" @click="showAddMaintenancePopup">
          <uni-icons type="plusempty" size="24" color="#007aff"></uni-icons>
          <text>添加设备维护记录</text>
        </view>
      </view>
    </scroll-view>
    
    <!-- 底部安全区域 -->
    <view class="safe-area-bottom">
      <!-- 底部导航栏 -->
      <TabBar role="technician" active="index" />
    </view>
    
    <!-- 添加设备维护弹窗 -->
    <uni-popup ref="addMaintenancePopup" type="center">
      <view class="popup-content">
        <view class="popup-title">添加设备维护记录</view>
        
        <view class="form-item">
          <text class="form-label">设备名称</text>
          <input class="form-input" v-model="newMaintenance.equipmentName" placeholder="请输入设备名称" />
        </view>
        
        <view class="form-item">
          <text class="form-label">计划维护时间</text>
          <picker 
            mode="date" 
            :value="newMaintenance.maintenanceDate" 
            @change="onDateChange"
            class="date-picker"
          >
            <view class="picker-value">
              {{ newMaintenance.maintenanceDate || '请选择日期' }}
            </view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="form-label">维护内容</text>
          <textarea 
            class="form-textarea" 
            v-model="newMaintenance.details" 
            placeholder="请输入维护内容详情"
            maxlength="200"
          ></textarea>
          <text class="textarea-counter">{{ newMaintenance.details.length }}/200</text>
        </view>
        
        <view class="popup-buttons">
          <button class="cancel-btn" @click="closeAddMaintenancePopup">取消</button>
          <button class="confirm-btn" @click="submitMaintenance">确认</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { getEmployeeOrders,getMaintenanceList,updateMaintenanceStatus,createMaintenanceOrder } from '@/api/orderAPI.js';
  import { onShow, onUnload } from '@dcloudio/uni-app';
  import TabBar from '@/components/tab-bar/index.vue';
  import OrderCard from '@/components/order-card/index.vue';

  // 工单列表
  const orderList = ref([]);
  const loading = ref(false);
  const refreshing = ref(false);
  const activeTab = ref('assigned'); // 默认显示未完成工单
  
  // 设备维护列表
  const maintenanceList = ref([]);
  const addMaintenancePopup = ref(null);
  const newMaintenance = ref({
    equipmentName: '',
    maintenanceDate: '',
    details: '',
    employeeId: null,
    status: 'pending'
  });
  
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
    if (tab === 'maintenance') {
      fetchMaintenanceList();
    }
  };
  
  // 下拉刷新
  const onRefresh = () => {
    refreshing.value = true;
    if (activeTab.value === 'maintenance') {
      fetchMaintenanceList().then(() => {
        refreshing.value = false;
      });
    } else {
      fetchEmployeeOrders().then(() => {
        refreshing.value = false;
      });
    }
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
  
  // 获取设备维护列表
  const fetchMaintenanceList = async () => {
    loading.value = true;
    const employeeInfoStr = uni.getStorageSync('employeeInfo');
    
    if (!employeeInfoStr) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      });
      loading.value = false;
      return;
    }
    
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
      const res = await getMaintenanceList(employeeInfo.employeeId);
      
      if (res.status === 200 && res.data) {
        maintenanceList.value = res.data;
      } else {
        maintenanceList.value = [];
        console.log('获取设备维护列表成功，但没有数据');
      }
    } catch (error) {
      console.error('获取设备维护列表失败', error);
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
  

  
  // 开始设备维护
  const startMaintenance = async (maintenanceId) => {
    uni.showModal({
      title: '确认开始',
      content: '确定要开始此设备维护任务吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const updateRes = await updateMaintenanceStatus({maintenanceId:maintenanceId, status:'in_progress'});
            
            if (updateRes.status === 200) {
              uni.showToast({
                title: '已开始维护',
                icon: 'success'
              });
              fetchMaintenanceList();
            } else {
              throw new Error(updateRes.msg || '开始维护失败');
            }
          } catch (error) {
            console.error('开始维护失败', error);
            uni.showToast({
              title: error.message || '网络异常，请稍后重试',
              icon: 'none'
            });
          }
        }
      }
    });
  };
  
  // 完成设备维护
  const completeMaintenance = async (maintenanceId) => {
    uni.showModal({
      title: '确认完成',
      content: '确定要完成此设备维护任务吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const updateRes = await updateMaintenanceStatus({maintenanceId:maintenanceId, status:'completed'});
            
            if (updateRes.status === 200) {
              uni.showToast({
                title: '维护已完成',
                icon: 'success'
              });
              fetchMaintenanceList();
            } else {
              throw new Error(updateRes.msg || '完成维护失败');
            }
          } catch (error) {
            console.error('完成维护失败', error);
            uni.showToast({
              title: error.message || '网络异常，请稍后重试',
              icon: 'none'
            });
          }
        }
      }
    });
  };
  
  // 显示添加设备维护弹窗
  const showAddMaintenancePopup = () => {
    // 重置表单
    newMaintenance.value = {
      equipmentName: '',
      maintenanceDate: '',
      details: '',
      employeeId: null,
      status: 'pending'
    };
    
    // 获取员工ID
    const employeeInfoStr = uni.getStorageSync('employeeInfo');
    if (employeeInfoStr) {
      const employeeInfo = JSON.parse(employeeInfoStr);
      newMaintenance.value.employeeId = employeeInfo.employeeId;
    }
    
    addMaintenancePopup.value.open();
  };
  
  // 关闭添加设备维护弹窗
  const closeAddMaintenancePopup = () => {
    addMaintenancePopup.value.close();
  };
  
  // 日期选择器变化
  const onDateChange = (e) => {
    newMaintenance.value.maintenanceDate = e.detail.value;
  };
  
  // 提交设备维护
  const submitMaintenance = async () => {
    // 表单验证
    if (!newMaintenance.value.equipmentName) {
      uni.showToast({
        title: '请输入设备名称',
        icon: 'none'
      });
      return;
    }
    
    if (!newMaintenance.value.maintenanceDate) {
      uni.showToast({
        title: '请选择维护时间',
        icon: 'none'
      });
      return;
    }
    
    if (!newMaintenance.value.details) {
      uni.showToast({
        title: '请输入维护内容',
        icon: 'none'
      });
      return;
    }
    
    try {
      // 提交设备维护
      const maintenanceData = {
        equipmentName: newMaintenance.value.equipmentName,
        maintenanceDate: newMaintenance.value.maintenanceDate ? newMaintenance.value.maintenanceDate + 'T00:00:00' : null,
        details: newMaintenance.value.details,
        employeeId: newMaintenance.value.employeeId,
        status: newMaintenance.value.status
      };
      const res = await createMaintenanceOrder(maintenanceData);
      
      if (res.status === 200) {
        uni.showToast({
          title: '添加成功',
          icon: 'success'
        });
        closeAddMaintenancePopup();
        fetchMaintenanceList();
      } else {
        throw new Error(res.msg || '添加失败');
      }
    } catch (error) {
      console.error('添加设备维护失败', error);
      uni.showToast({
        title: error.message || '网络异常，请稍后重试',
        icon: 'none'
      });
    }
  };
  
  // 获取维护状态文本
  const getMaintenanceStatusText = (status) => {
    const statusMap = {
      'pending': '待处理',
      'in_progress': '处理中',
      'completed': '已完成'
    };
    return statusMap[status] || '未知状态';
  };
  
  // 获取维护状态样式类
  const getMaintenanceStatusClass = (status) => {
    const classMap = {
      'pending': 'status-pending',
      'in_progress': 'status-in-progress',
      'completed': 'status-completed'
    };
    return classMap[status] || '';
  };
  
  // 格式化日期
  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  };
  
  // 监听工单更新事件
  const handleOrderUpdated = () => {
    fetchEmployeeOrders();
  };
  
  // 监听设备维护更新事件
  const handleMaintenanceUpdated = () => {
    fetchMaintenanceList();
  };
  
  onMounted(() => {
    fetchEmployeeOrders();
    // 监听工单更新事件
    uni.$on('order-updated', handleOrderUpdated);
    uni.$on('maintenance-updated', handleMaintenanceUpdated);
  });
  
  onShow(() => {
    if (activeTab.value === 'maintenance') {
      fetchMaintenanceList();
    } else {
      fetchEmployeeOrders();
    }
  });
  
  // 页面卸载时移除事件监听
  onUnload(() => {
    uni.$off('order-updated', handleOrderUpdated);
    uni.$off('maintenance-updated', handleMaintenanceUpdated);
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

.order-list, .maintenance-list {
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

/* 设备维护卡片样式 */
.maintenance-card {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.equipment-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.status-tag {
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.status-pending {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-in-progress {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-completed {
  background-color: #f6ffed;
  color: #52c41a;
}

.card-content {
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  margin-bottom: 10rpx;
}

.info-label {
  width: 160rpx;
  color: #666;
  font-size: 28rpx;
}

.info-value {
  flex: 1;
  color: #333;
  font-size: 28rpx;
}

.details-row {
  align-items: flex-start;
}

.details-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  padding: 10rpx 30rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  margin-left: 20rpx;
}

.start-btn {
  background-color: #1890ff;
  color: #fff;
}

.complete-btn {
  background-color: #52c41a;
  color: #fff;
}

.view-btn {
  background-color: #f5f5f5;
  color: #666;
}

/* 添加设备维护按钮 */
.add-maintenance {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-top: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  color: #007aff;
  font-size: 28rpx;
}

.add-maintenance uni-icons {
  margin-right: 10rpx;
}

/* 弹窗样式 */
.popup-content {
  width: 650rpx;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30rpx;
}

.form-item {
  margin-bottom: 20rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
}

.form-input, .date-picker {
  width: 100%;
  height: 80rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.picker-value {
  height: 80rpx;
  line-height: 80rpx;
  color: #333;
}

.form-textarea {
  width: 100%;
  height: 200rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.textarea-counter {
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.popup-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 30rpx;
}

.cancel-btn, .confirm-btn {
  width: 45%;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background-color: #007aff;
  color: #fff;
}
</style>