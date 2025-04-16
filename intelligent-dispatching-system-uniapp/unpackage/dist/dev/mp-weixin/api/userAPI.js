"use strict";
const api_request = require("./request.js");
function login(data) {
  return api_request.request("/login/login", "POST", data);
}
function register(data) {
  return api_request.request("/login/register", "POST", data);
}
exports.login = login;
exports.register = register;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/userAPI.js.map
