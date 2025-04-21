import { request } from './request.js';

// 获取用户工单列表
export function getUserOrders(userId) {
  return request(`/order/user/list?userId=${userId}`, 'GET');
}
// 获取员工工单列表
export function getEmployeeOrders(employeeId) {
  return request(`/order/employee/list?employeeId=${employeeId}`, 'GET');
}
// 获取工单详情
export function getOrderDetail(orderId) {
  return request(`/order/detail?orderId=${orderId}`, 'GET');
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