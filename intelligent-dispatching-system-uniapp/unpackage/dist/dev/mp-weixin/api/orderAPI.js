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
function createOrder(data) {
  return api_request.request("/order/create", "POST", data);
}
function cancelOrder(orderId) {
  return api_request.request(`/order/cancel/${orderId}`, "POST");
}
exports.cancelOrder = cancelOrder;
exports.createOrder = createOrder;
exports.getEmployeeOrders = getEmployeeOrders;
exports.getOrderDetail = getOrderDetail;
exports.getUserOrders = getUserOrders;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/orderAPI.js.map
