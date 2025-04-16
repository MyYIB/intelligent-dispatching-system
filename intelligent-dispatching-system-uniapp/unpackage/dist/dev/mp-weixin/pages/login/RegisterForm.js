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
  __name: "RegisterForm",
  props: {
    switchToLogin: Function
  },
  setup(__props) {
    const props = __props;
    const registerForm = common_vendor.reactive({
      username: "",
      phone: "",
      password: "",
      confirmPassword: "",
      email: "",
      address: ""
    });
    const loading = common_vendor.ref(false);
    const handleRegister = async () => {
      if (!registerForm.username || !registerForm.phone || !registerForm.password || !registerForm.confirmPassword || !registerForm.email || !registerForm.address) {
        common_vendor.index.showToast({
          title: "请填写完整注册信息",
          icon: "none"
        });
        return;
      }
      if (registerForm.password !== registerForm.confirmPassword) {
        common_vendor.index.showToast({
          title: "两次输入的密码不一致",
          icon: "none"
        });
        return;
      }
      const phoneReg = /^1[3-9]\d{9}$/;
      if (!phoneReg.test(registerForm.phone)) {
        common_vendor.index.showToast({
          title: "请输入正确的手机号",
          icon: "none"
        });
        return;
      }
      const emailReg = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
      if (!emailReg.test(registerForm.email)) {
        common_vendor.index.showToast({
          title: "请输入正确的邮箱格式",
          icon: "none"
        });
        return;
      }
      loading.value = true;
      try {
        const res = await api_userAPI.register({
          username: registerForm.username,
          account: registerForm.phone,
          // 使用手机号作为账号
          phone: registerForm.phone,
          password: registerForm.password,
          email: registerForm.email,
          address: registerForm.address,
          role: "customer"
          // 默认注册为客户角色
        });
        if (res.status === 200) {
          common_vendor.index.showToast({
            title: "注册成功，请登录",
            icon: "success"
          });
          Object.keys(registerForm).forEach((key) => {
            registerForm[key] = "";
          });
          props.switchToLogin();
        } else {
          common_vendor.index.showToast({
            title: res.msg || "注册失败",
            icon: "none"
          });
        }
      } catch (err) {
        common_vendor.index.showToast({
          title: "网络错误，请稍后重试",
          icon: "none"
        });
        common_vendor.index.__f__("error", "at pages/login/RegisterForm.vue:97", err);
      } finally {
        loading.value = false;
      }
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.p({
          type: "person",
          size: "20",
          color: "#007aff"
        }),
        b: registerForm.username,
        c: common_vendor.o(($event) => registerForm.username = $event.detail.value),
        d: common_vendor.p({
          type: "phone",
          size: "20",
          color: "#007aff"
        }),
        e: registerForm.phone,
        f: common_vendor.o(($event) => registerForm.phone = $event.detail.value),
        g: common_vendor.p({
          type: "locked",
          size: "20",
          color: "#007aff"
        }),
        h: registerForm.password,
        i: common_vendor.o(($event) => registerForm.password = $event.detail.value),
        j: common_vendor.p({
          type: "locked",
          size: "20",
          color: "#007aff"
        }),
        k: registerForm.confirmPassword,
        l: common_vendor.o(($event) => registerForm.confirmPassword = $event.detail.value),
        m: common_vendor.p({
          type: "email",
          size: "20",
          color: "#007aff"
        }),
        n: registerForm.email,
        o: common_vendor.o(($event) => registerForm.email = $event.detail.value),
        p: common_vendor.p({
          type: "location",
          size: "20",
          color: "#007aff"
        }),
        q: registerForm.address,
        r: common_vendor.o(($event) => registerForm.address = $event.detail.value),
        s: loading.value,
        t: common_vendor.o(handleRegister),
        v: common_vendor.o((...args) => __props.switchToLogin && __props.switchToLogin(...args))
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-bb04b352"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/RegisterForm.js.map
