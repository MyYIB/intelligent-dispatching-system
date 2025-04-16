"use strict";
const common_vendor = require("../common/vendor.js");
const common_assets = require("../common/assets.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_uni_icons + TabBar)();
}
const TabBar = () => "../components/tab-bar/index.js";
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const userInfo = common_vendor.ref(null);
    const isLogin = common_vendor.ref(false);
    const userRole = common_vendor.ref("customer");
    const menuList = [
      { id: 1, icon: "information-circle", text: "关于", url: "/pages/base/about", useImage: true },
      { id: 2, icon: "close-circle", text: "退出登录", isLogout: true, useImage: true }
    ];
    const checkLoginStatus = () => {
      const token = common_vendor.index.getStorageSync("token");
      const userInfoStr = common_vendor.index.getStorageSync("userInfo");
      if (token && userInfoStr) {
        try {
          userInfo.value = JSON.parse(userInfoStr);
          userRole.value = userInfo.value.role || "customer";
          isLogin.value = true;
        } catch (e) {
          isLogin.value = false;
        }
      } else {
        isLogin.value = false;
      }
    };
    const goToLogin = () => {
      if (!isLogin.value) {
        common_vendor.index.navigateTo({
          url: "/pages/login/Login"
        });
      } else {
        const userInfoPage = userRole.value === "technician" ? "/pages/employee/info" : "/pages/user/info";
        common_vendor.index.redirectTo({
          url: userInfoPage
        });
      }
    };
    const handleMenuClick = (item) => {
      if (item.isLogout) {
        common_vendor.index.showModal({
          title: "提示",
          content: "确定要退出登录吗？",
          success: function(res) {
            if (res.confirm) {
              common_vendor.index.removeStorageSync("token");
              common_vendor.index.removeStorageSync("userInfo");
              isLogin.value = false;
              userInfo.value = null;
              userRole.value = "customer";
              common_vendor.index.showToast({
                title: "已退出登录",
                icon: "success"
              });
            }
          }
        });
      } else {
        common_vendor.index.navigateTo({
          url: item.url
        });
      }
    };
    common_vendor.onMounted(() => {
      checkLoginStatus();
    });
    common_vendor.onShow(() => {
      checkLoginStatus();
    });
    return (_ctx, _cache) => {
      var _a, _b;
      return common_vendor.e({
        a: !isLogin.value
      }, !isLogin.value ? {
        b: common_assets._imports_0
      } : {
        c: ((_a = userInfo.value) == null ? void 0 : _a.avatar) || "/static/images/default-avatar.png"
      }, {
        d: common_vendor.o(goToLogin),
        e: common_vendor.t(isLogin.value ? (_b = userInfo.value) == null ? void 0 : _b.username : "未登录"),
        f: common_vendor.o(goToLogin),
        g: common_vendor.f(menuList, (item, k0, i0) => {
          return common_vendor.e({
            a: item.useImage && item.isLogout
          }, item.useImage && item.isLogout ? {
            b: common_assets._imports_0$1
          } : item.useImage ? {
            d: common_assets._imports_1
          } : {
            e: "02281a80-0-" + i0,
            f: common_vendor.p({
              type: item.icon,
              size: "20",
              color: "#666"
            })
          }, {
            c: item.useImage,
            g: common_vendor.t(item.text),
            h: "02281a80-1-" + i0,
            i: item.id,
            j: common_vendor.o(($event) => handleMenuClick(item), item.id),
            k: !item.isLogout || isLogin.value
          });
        }),
        h: common_vendor.p({
          type: "right",
          size: "16",
          color: "#ccc"
        }),
        i: common_vendor.p({
          role: userRole.value,
          active: "user"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-02281a80"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../.sourcemap/mp-weixin/pages/index.js.map
