// 基础URL
const BASE_URL = 'http://localhost:8081';

// 封装请求方法
export const request = (url, method, data) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        console.log(res);
        if (res.statusCode === 401) {
          // 未授权，清除token并跳转到登录页
          uni.removeStorageSync('token');
          uni.removeStorageSync('userInfo');
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          });
          
          // 使用navigateTo替代reLaunch，避免超时问题
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages/login/Login',
              fail: (err) => {
                console.error('跳转登录页失败:', err);
                // 如果跳转失败，尝试使用switchTab
                uni.switchTab({
                  url: '/pages/index'
                });
              }
            });
          }, 1500);
          reject(new Error('未授权'));
        } else {
          resolve(res.data);
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};