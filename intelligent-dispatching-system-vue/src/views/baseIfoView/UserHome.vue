<script setup>
import { ref, computed, onMounted, reactive, onUnmounted } from 'vue';
import { useUserStore } from '@/store';
import request from '@/utils/request';
import * as echarts from 'echarts';
// 导入需要的图标组件
import { Document, Timer, Select, Star, User, Box, UserFilled } from '@element-plus/icons-vue';

const userStore = useUserStore();
const userName = ref('');
const role = computed(() => {
  switch (userStore.user.role) {
    case "admin":
      return '管理员';
    case "employee":
      return '员工';
    case "costumer":
      return '用户';
    default:
      return '';
  }
});

// 统计数据
const stats = reactive({
  totalOrders: 0,
  pendingOrders: 0,
  completedOrders: 0,
  totalEmployees: 0,
  totalUsers: 0,
  totalInventory: 0,
  lowStockItems: 0,
  averageSatisfaction: 0,
  pendingMaintenance: 0
});

// 图表实例
const orderTypeChartRef = ref(null);
const orderStatusChartRef = ref(null);
const satisfactionChartRef = ref(null);
const workloadChartRef = ref(null);

// 图表实例对象
let orderTypeChart = null;
let orderStatusChart = null;
let satisfactionChart = null;
let workloadChart = null;

// 加载状态
const loading = ref(true);

// 获取用户名
const getNameFromUser = () => {
  userName.value = userStore.user.username || '';
};

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await request.get('/dashboard/stats');
    if (res.data.status === 200) {
      Object.assign(stats, res.data.data);
    }
  } catch (error) {
    console.error('获取统计数据失败', error);
  } finally {
    loading.value = false;
  }
};

// 获取工单类型分布数据
const fetchOrderTypeData = async () => {
  try {
    const res = await request.get('/dashboard/orderTypeDistribution');
    if (res.data.status === 200) {
      initOrderTypeChart(res.data.data);
    }
  } catch (error) {
    console.error('获取工单类型分布数据失败', error);
  }
};

// 获取工单状态分布数据
const fetchOrderStatusData = async () => {
  try {
    const res = await request.get('/dashboard/orderStatusDistribution');
    if (res.data.status === 200) {
      initOrderStatusChart(res.data.data);
    }
  } catch (error) {
    console.error('获取工单状态分布数据失败', error);
  }
};

// 获取满意度分布数据
const fetchSatisfactionData = async () => {
  try {
    const res = await request.get('/dashboard/satisfactionDistribution');
    if (res.data.status === 200) {
      initSatisfactionChart(res.data.data);
    }
  } catch (error) {
    console.error('获取满意度分布数据失败', error);
  }
};

// 获取员工工作负载数据
const fetchWorkloadData = async () => {
  try {
    const res = await request.get('/dashboard/employeeWorkload');
    if (res.data.status === 200) {
      initWorkloadChart(res.data.data);
    }
  } catch (error) {
    console.error('获取员工工作负载数据失败', error);
  }
};

// 初始化工单类型分布图表
const initOrderTypeChart = (data) => {
  if (!orderTypeChartRef.value) return;
  
  // 使用 MutationObserver 监听 DOM 变化
  const observer = new MutationObserver((mutations, obs) => {
    const chartContainer = orderTypeChartRef.value;
    if (chartContainer && chartContainer.offsetHeight > 0 && chartContainer.offsetWidth > 0) {
      try {
        // 销毁之前的实例
        if (orderTypeChart) {
          orderTypeChart.dispose();
        }
        // 处理数据，将英文类型转换为中文显示
        const processedData = data.map(item => {
          const newItem = {...item};
          if (newItem.name === 'nrepair') {
            newItem.name = '报修';
          } else if (newItem.name === 'complaint') {
            newItem.name = '投诉';
          }
          return newItem;
        });
        // 创建新实例
        orderTypeChart = echarts.init(chartContainer);
        const option = {
          title: {
            text: '工单类型分布',
            left: 'center',
            
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            data: processedData.map(item => item.name)
          },
          series: [
            {
              name: '工单类型',
              type: 'pie',
              radius: ['50%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '18',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: processedData
            }
          ]
        };
        
        orderTypeChart.setOption(option);
        console.log('工单类型图表初始化成功', chartContainer.offsetWidth, chartContainer.offsetHeight);
        
        // 停止观察
        obs.disconnect();
      } catch (error) {
        console.error('工单类型图表初始化失败', error);
      }
    }
  });
  
  // 开始观察
  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
  
  // 设置超时，防止无限等待
  setTimeout(() => {
    observer.disconnect();
  }, 10000);
};

// 类似修改其他图表初始化函数
const initOrderStatusChart = (data) => {
  if (!orderStatusChartRef.value) return;
  
  const observer = new MutationObserver((mutations, obs) => {
    const chartContainer = orderStatusChartRef.value;
    if (chartContainer && chartContainer.offsetHeight > 0 && chartContainer.offsetWidth > 0) {
      try {
        if (orderStatusChart) {
          orderStatusChart.dispose();
        }
        orderStatusChart = echarts.init(chartContainer);
        const option = {
          title: {
            text: '工单状态分布',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            data: data.map(item => item.name)
          },
          series: [
            {
              name: '工单状态',
              type: 'pie',
              radius: '50%',
              data: data,
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }
          ]
        };
        
        orderStatusChart.setOption(option);
        console.log('工单状态图表初始化成功', chartContainer.offsetWidth, chartContainer.offsetHeight);
        obs.disconnect();
      } catch (error) {
        console.error('工单状态图表初始化失败', error);
      }
    }
  });
  
  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
  
  setTimeout(() => {
    observer.disconnect();
  }, 10000);
};

const initSatisfactionChart = (data) => {
  if (!satisfactionChartRef.value) return;
  
  setTimeout(() => {
    try {
      if (satisfactionChart) {
        satisfactionChart.dispose();
      }
      satisfactionChart = echarts.init(satisfactionChartRef.value);
      const option = {
        title: {
          text: '用户满意度分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.score + '分')
        },
        yAxis: {
          type: 'value',
          name: '数量'
        },
        series: [
          {
            name: '满意度评分',
            type: 'bar',
            data: data.map(item => item.count),
            itemStyle: {
              color: function(params) {
                // 根据分数设置不同颜色
                const score = parseInt(params.name);
                if (score <= 2) return '#F56C6C';
                if (score <= 3) return '#E6A23C';
                return '#67C23A';
              }
            }
          }
        ]
      };
      
      satisfactionChart.setOption(option);
    } catch (error) {
      console.error('满意度图表初始化失败', error);
    }
  }, 1000);
};

const initWorkloadChart = (data) => {
  if (!workloadChartRef.value) return;
  
  setTimeout(() => {
    try {
      if (workloadChart) {
        workloadChart.dispose();
      }
      workloadChart = echarts.init(workloadChartRef.value);
      const option = {
        title: {
          text: '员工工作负载',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          name: '工作负载'
        },
        yAxis: {
          type: 'category',
          data: data.map(item => item.name),
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        series: [
          {
            name: '当前工作负载',
            type: 'bar',
            data: data.map(item => item.current),
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '最大工作负载',
            type: 'bar',
            data: data.map(item => item.max),
            itemStyle: {
              color: '#909399'
            }
          }
        ]
      };
      
      workloadChart.setOption(option);
    } catch (error) {
      console.error('工作负载图表初始化失败', error);
    }
  }, 1000);
};

// 窗口大小变化时重新调整图表大小
const handleResize = () => {
  // 检查图表实例是否存在且未被销毁
  if (orderTypeChart && !orderTypeChart.isDisposed()) {
    orderTypeChart.resize();
  }
  if (orderStatusChart && !orderStatusChart.isDisposed()) {
    orderStatusChart.resize();
  }
  if (satisfactionChart && !satisfactionChart.isDisposed()) {
    satisfactionChart.resize();
  }
  if (workloadChart && !workloadChart.isDisposed()) {
    workloadChart.resize();
  }
};

onMounted(() => {
  getNameFromUser();
  fetchStats();
  
  // 延迟加载图表数据
  setTimeout(() => {
    fetchOrderTypeData();
    fetchOrderStatusData();
    fetchSatisfactionData();
    fetchWorkloadData();
  }, 1000);
  
  window.addEventListener('resize', handleResize);
});

// 组件卸载时移除事件监听
onUnmounted( () => {
  window.removeEventListener('resize', handleResize);

  // 销毁图表实例
  orderTypeChart && orderTypeChart.dispose();
  orderStatusChart && orderStatusChart.dispose();
  satisfactionChart && satisfactionChart.dispose();
  workloadChart && workloadChart.dispose();
});
</script>

<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h1>欢迎{{ role }}{{ userName }}进入电信运营商智能化派工系统</h1>
      <p class="dashboard-subtitle">数据可视化仪表盘</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else>
      <!-- 统计卡片 -->
      <div class="stat-cards">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-container">
                  <div class="stat-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                </div>
                <div class="stat-info">
                  <div class="stat-title">总工单数</div>
                  <div class="stat-value">{{ stats.totalOrders }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-container">
                  <div class="stat-icon pending">
                    <el-icon><Timer /></el-icon>
                  </div>
                </div>
                <div class="stat-info">
                  <div class="stat-title">待处理工单</div>
                  <div class="stat-value">{{ stats.pendingOrders }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-container">
                  <div class="stat-icon completed">
                    <el-icon><Select /></el-icon>
                  </div>
                </div>
                <div class="stat-info">
                  <div class="stat-title">已完成工单</div>
                  <div class="stat-value">{{ stats.completedOrders }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-container">
                  <div class="stat-icon satisfaction">
                    <el-icon><Star /></el-icon>
                  </div>
                </div>
                <div class="stat-info">
                  <div class="stat-title">平均满意度</div>
                  <div class="stat-value">{{ stats.averageSatisfaction.toFixed(1) }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-container">
                  <div class="stat-icon users">
                    <el-icon><User /></el-icon>
                  </div>
                </div>
                <div class="stat-info">
                  <div class="stat-title">用户总数</div>
                  <div class="stat-value">{{ stats.totalUsers }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-container">
                  <div class="stat-icon employees">
                    <el-icon><UserFilled /></el-icon>
                  </div>
                </div>
                <div class="stat-info">
                  <div class="stat-title">员工总数</div>
                  <div class="stat-value">{{ stats.totalEmployees }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-container">
                  <div class="stat-icon inventory">
                    <el-icon><Box /></el-icon>
                  </div>
                </div>
                <div class="stat-info">
                  <div class="stat-title">库存总量</div>
                  <div class="stat-value">{{ stats.totalInventory }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-container">
                  <div class="stat-icon maintenance">
                    <el-icon><Box /></el-icon> <!-- 替换 Tools 图标 -->
                  </div>
                </div>
                <div class="stat-info">
                  <div class="stat-title">待处理维护</div>
                  <div class="stat-value">{{ stats.pendingMaintenance }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 图表区域 - 修改图表容器结构 -->
      <div class="chart-container">
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-card shadow="hover" class="chart-card">
              <div style="width: 100%; height: 400px;">
                <div ref="orderTypeChartRef" style="width: 100%; height: 100%;"></div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :md="12">
            <el-card shadow="hover" class="chart-card">
              <div style="width: 100%; height: 400px;">
                <div ref="orderStatusChartRef" style="width: 100%; height: 100%;"></div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :xs="24" :md="12">
            <el-card shadow="hover" class="chart-card">
              <div style="width: 100%; height: 400px;">
                <div ref="satisfactionChartRef" style="width: 100%; height: 100%;"></div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :md="12">
            <el-card shadow="hover" class="chart-card">
              <div style="width: 100%; height: 400px;">
                <div ref="workloadChartRef" style="width: 100%; height: 100%;"></div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 低库存提醒 -->
      <div class="low-stock-section" v-if="stats.lowStockItems > 0">
        <el-alert
          title="库存预警"
          type="warning"
          :description="`当前有 ${stats.lowStockItems} 种备件库存不足，请及时补充。`"
          show-icon
          :closable="false"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.welcome-section {
  text-align: center;
  margin-bottom: 30px;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.welcome-section h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.dashboard-subtitle {
  color: #909399;
  font-size: 16px;
  margin-top: 10px;
}

.loading-container {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 30px;
}

.stat-card {
  height: 120px;
  transition: all 0.3s;
  margin-bottom: 10px;
  border-radius: 8px;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  height: 100%;
  align-items: center;
  padding: 0 20px;
}

.stat-icon-container {
  margin-right: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #409EFF;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.stat-icon :deep(i) {
  font-size: 24px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.pending {
  background-color: #E6A23C;
}

.completed {
  background-color: #67C23A;
}

.satisfaction {
  background-color: #F56C6C;
}

.users {
  background-color: #9254DE;
}

.employees {
  background-color: #36CFC9;
}

.inventory {
  background-color: #FF9C6E;
}

.maintenance {
  background-color: #73D13D;
}

.chart-container {
  margin-bottom: 30px;
}

.chart-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  /* 移除固定高度，让内部元素决定高度 */
}

.chart {
  width: 100%;
  height: 100%;
}

.low-stock-section {
  margin-top: 20px;
}

/* 添加响应式调整 */
@media screen and (max-width: 1200px) {
  .chart-card {
    height: 350px;
  }
}

@media screen and (max-width: 768px) {
  .stat-card {
    margin-bottom: 15px;
  }
  
  .chart-card {
    height: 300px;
  }
  
  .welcome-section h1 {
    font-size: 20px;
  }
}
</style>