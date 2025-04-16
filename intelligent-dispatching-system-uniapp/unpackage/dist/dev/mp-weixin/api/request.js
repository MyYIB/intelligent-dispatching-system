"use strict";
const common_vendor = require("../common/vendor.js");
const BASE_URL = "http://localhost:8081";
const request = (url, method, data) => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token");
    common_vendor.index.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: {
        "Content-Type": "application/json",
        "Authorization": token ? `Bearer ${token}` : ""
      },
      success: (res) => {
        common_vendor.index.__f__("log", "at api/request.js:18", res);
        if (res.statusCode === 401) {
          common_vendor.index.removeStorageSync("token");
          common_vendor.index.removeStorageSync("userInfo");
          common_vendor.index.showToast({
            title: "登录已过期，请重新登录",
            icon: "none"
          });
          setTimeout(() => {
            common_vendor.index.navigateTo({
              url: "/pages/login/Login",
              fail: (err) => {
                common_vendor.index.__f__("error", "at api/request.js:33", "跳转登录页失败:", err);
                common_vendor.index.switchTab({
                  url: "/pages/index"
                });
              }
            });
          }, 1500);
          reject(new Error("未授权"));
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
exports.request = request;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/request.js.map
