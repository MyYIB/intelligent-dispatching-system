"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Math) {
  (LoginForm + RegisterForm)();
}
const LoginForm = () => "./LoginForm.js";
const RegisterForm = () => "./RegisterForm.js";
const _sfc_main = {
  __name: "Login",
  setup(__props) {
    const isLogin = common_vendor.ref(true);
    const toggleForm = () => {
      isLogin.value = !isLogin.value;
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(isLogin.value ? "登录" : "注册"),
        b: isLogin.value
      }, isLogin.value ? {
        c: common_vendor.p({
          switchToRegister: toggleForm
        })
      } : {
        d: common_vendor.p({
          switchToLogin: toggleForm
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-0a03bc64"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/Login.js.map
