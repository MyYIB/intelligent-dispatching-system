import { request } from './request.js';

// 获取用户工单列表
export function getUserOrders() {
  return request('/order/user/list', 'GET');
}

// 获取工单详情
export function getOrderDetail(orderId) {
  return request(`/order/detail/${orderId}`, 'GET');
}

// 创建工单
export function createOrder(data) {
  return request('/order/create', 'POST', data);
}

// 取消工单
export function cancelOrder(orderId) {
  return request(`/order/cancel/${orderId}`, 'POST');
}

// 评价工单
export function rateOrder(orderId, data) {
  return request(`/order/rate/${orderId}`, 'POST', data);
}