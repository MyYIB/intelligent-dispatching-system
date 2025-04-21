"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = {
  __name: "complaint",
  setup(__props) {
    const description = common_vendor.ref("");
    const userPhone = common_vendor.ref("");
    const getUserInfo = () => {
      const userInfoStr = common_vendor.index.getStorageSync("userInfo");
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          userPhone.value = userInfo.phone || "";
        } catch (e) {
          common_vendor.index.__f__("error", "at pages/user/complaint.vue:59", "解析用户信息失败", e);
        }
      }
    };
    const updatePhone = () => {
      common_vendor.index.navigateTo({
        url: "/pages/user/profile"
      });
    };
    const submitComplaint = () => {
      if (!description.value.trim()) {
        return common_vendor.index.showToast({
          title: "请填写投诉内容",
          icon: "none"
        });
      }
      ({
        order_type: "complaint",
        description: description.value
      });
      common_vendor.index.showLoading({
        title: "提交中..."
      });
      setTimeout(() => {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "投诉提交成功",
          icon: "success"
        });
        setTimeout(() => {
          common_vendor.index.navigateBack();
        }, 1500);
      }, 1e3);
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    common_vendor.onMounted(() => {
      getUserInfo();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.p({
          type: "left",
          size: "20",
          color: "#333"
        }),
        b: common_vendor.o(goBack),
        c: description.value,
        d: common_vendor.o(($event) => description.value = $event.detail.value),
        e: common_vendor.t(description.value.length),
        f: common_vendor.t(userPhone.value || "未绑定"),
        g: !userPhone.value
      }, !userPhone.value ? {
        h: common_vendor.o(updatePhone)
      } : {}, {
        i: common_vendor.o(submitComplaint)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-5ac1d7f0"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/complaint.js.map
