"use strict";
const api_request = require("./request.js");
function getUserOrders(userId) {
  return api_request.request(`/order/user/list?userId=${userId}`, "GET");
}
function getEmployeeOrders(employeeId) {
  return api_request.request(`/order/employee/list?employeeId=${employeeId}`, "GET");
}
function getOrderDetail(orderId) {
  return api_request.request(`/order/detail?orderId=${orderId}`, "GET");
}
function getOrderEmployeeDetail(orderId) {
  return api_request.request(`/order/getDispatchEmployee?orderId=${orderId}`, "GET");
}
function createOrder(data) {
  return api_request.request("/order/create", "POST", data);
}
function cancelOrder(orderId) {
  return api_request.request(`/order/cancel?orderId=${orderId}`, "POST");
}
function getOrderInventory(orderId) {
  return api_request.request(`/inventory/getUse?orderId=${orderId}`, "GET");
}
function fetchInventoryList() {
  return api_request.request("/inventory/list", "GET");
}
function updateOrderStatus(orderId, status) {
  return api_request.request(`/order/updateStatus`, "POST", { orderId, status });
}
function submitInventoryUse(data) {
  return api_request.request(`/inventory/use`, "POST", data);
}
function getOrderUserInfo(orderId) {
  return api_request.request(`/order/getUser?orderId=${orderId}`, "GET");
}
function getFeedbackByOrderId(orderId) {
  return api_request.request(`/order/getFeedback?orderId=${orderId}`, "GET");
}
function submitFeedbackRecord(data) {
  return api_request.request(`/order/submitFeedback`, "POST", data);
}
function submitUserRating(data) {
  return api_request.request(`/order/submitFeedbackScore`, "POST", data);
}
function getMaintenanceList(employeeId) {
  return api_request.request(`/order/maintenance/employee?employeeId=${employeeId}`, "GET");
}
function updateMaintenanceStatus(data) {
  return api_request.request(`/order/maintenance/updateStatus`, "POST", data);
}
function createMaintenanceOrder(data) {
  return api_request.request(`/order/maintenance/create`, "POST", data);
}
exports.cancelOrder = cancelOrder;
exports.createMaintenanceOrder = createMaintenanceOrder;
exports.createOrder = createOrder;
exports.fetchInventoryList = fetchInventoryList;
exports.getEmployeeOrders = getEmployeeOrders;
exports.getFeedbackByOrderId = getFeedbackByOrderId;
exports.getMaintenanceList = getMaintenanceList;
exports.getOrderDetail = getOrderDetail;
exports.getOrderEmployeeDetail = getOrderEmployeeDetail;
exports.getOrderInventory = getOrderInventory;
exports.getOrderUserInfo = getOrderUserInfo;
exports.getUserOrders = getUserOrders;
exports.submitFeedbackRecord = submitFeedbackRecord;
exports.submitInventoryUse = submitInventoryUse;
exports.submitUserRating = submitUserRating;
exports.updateMaintenanceStatus = updateMaintenanceStatus;
exports.updateOrderStatus = updateOrderStatus;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/orderAPI.js.map
