"use strict";
const api_request = require("./request.js");
function getUserOrders() {
  return api_request.request("/order/user/list", "GET");
}
exports.getUserOrders = getUserOrders;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/orderAPI.js.map
