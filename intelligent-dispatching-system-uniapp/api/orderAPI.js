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

//获取工单派单员工详情
export function getOrderEmployeeDetail(orderId) {
  return request(`/order/getDispatchEmployee?orderId=${orderId}`, 'GET');
}
// 创建工单
export function createOrder(data) {
  return request('/order/create', 'POST', data);
}

// 取消工单
export function cancelOrder(orderId) {
  return request(`/order/cancel?orderId=${orderId}`, 'POST');
}

// 评价工单
export function rateOrder(orderId, data) {
  return request(`/order/rate/${orderId}`, 'POST', data);
}

// 获取工单使用物料
export function getOrderInventory(orderId) {
  return request(`/inventory/getUse?orderId=${orderId}`, 'GET');
}

// 获取物料列表
export function fetchInventoryList() {
  return request('/inventory/list', 'GET');
}

// 更新工单状态
export function updateOrderStatus(orderId, status) {
  return request(`/order/updateStatus`, 'POST', {orderId: orderId,status: status});
}

// 提交物料使用
export function submitInventoryUse(data) {
  return request(`/inventory/use`, 'POST', data);
}

// 获取工单用户信息
export function getOrderUserInfo(orderId) {
  return request(`/order/getUser?orderId=${orderId}`, 'GET');
}