"use strict";
const api_request = require("./request.js");
function updateEmployeeLocation(data) {
  return api_request.request("/employee/updateLocation", "POST", data);
}
exports.updateEmployeeLocation = updateEmployeeLocation;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/employeeAPI.js.map
