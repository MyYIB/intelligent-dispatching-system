"use strict";
const common_vendor = require("../../common/vendor.js");
const api_userAPI = require("../../api/userAPI.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = {
  __name: "LoginForm",
  props: {
    switchToRegister: Function
  },
  setup(__props) {
    const loginForm = common_vendor.reactive({
      phone: "",
      // 将account改为phone
      password: ""
    });
    const rememberMe = common_vendor.ref(true);
    const loading = common_vendor.ref(false);
    const handleRememberChange = (e) => {
      rememberMe.value = e.detail.value.length > 0;
    };
    const handleLogin = async () => {
      if (!loginForm.phone || !loginForm.password) {
        common_vendor.index.showToast({
          title: "手机号和密码不能为空",
          icon: "none"
        });
        return;
      }
      const phoneReg = /^1[3-9]\d{9}$/;
      if (!phoneReg.test(loginForm.phone)) {
        common_vendor.index.showToast({
          title: "请输入正确的手机号",
          icon: "none"
        });
        return;
      }
      loading.value = true;
      try {
        const res = await api_userAPI.login({
          account: loginForm.phone,
          password: loginForm.password
        });
        if (res.status === 200) {
          common_vendor.index.__f__("log", "at pages/login/LoginForm.vue:51", res.data);
          const { token, user } = res.data;
          common_vendor.index.setStorageSync("token", token);
          common_vendor.index.setStorageSync("userInfo", JSON.stringify(user));
          common_vendor.index.showToast({
            title: "登录成功",
            icon: "success"
          });
          if (user.role === "technician") {
            common_vendor.index.reLaunch({
              url: "/pages/employee/home"
            });
          } else {
            common_vendor.index.reLaunch({
              url: "/pages/user/home"
            });
          }
        } else {
          common_vendor.index.showToast({
            title: res.msg || "登录失败",
            icon: "none"
          });
        }
      } catch (err) {
        common_vendor.index.showToast({
          title: "网络错误，请稍后重试",
          icon: "none"
        });
        common_vendor.index.__f__("error", "at pages/login/LoginForm.vue:85", err);
      } finally {
        loading.value = false;
      }
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.p({
          type: "phone",
          size: "20",
          color: "#007aff"
        }),
        b: loginForm.phone,
        c: common_vendor.o(($event) => loginForm.phone = $event.detail.value),
        d: common_vendor.p({
          type: "locked",
          size: "20",
          color: "#007aff"
        }),
        e: loginForm.password,
        f: common_vendor.o(($event) => loginForm.password = $event.detail.value),
        g: rememberMe.value,
        h: common_vendor.o(handleRememberChange),
        i: loading.value,
        j: common_vendor.o(handleLogin),
        k: common_vendor.o((...args) => __props.switchToRegister && __props.switchToRegister(...args))
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-4554dff8"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/LoginForm.js.map
