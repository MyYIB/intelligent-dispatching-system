"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_uni_icons + TabBar)();
}
const TabBar = () => "../../components/tab-bar/index.js";
const _sfc_main = {
  __name: "info",
  setup(__props) {
    const userInfo = common_vendor.ref(null);
    const isLogin = common_vendor.ref(false);
    const menuList = [
      { id: 1, text: "个人资料", icon: "person", url: "/pages/employee/profile" },
      { id: 2, text: "技能管理", icon: "star", url: "/pages/employee/skills" },
      { id: 3, text: "关于", useImage: true, url: "/pages/base/about" },
      { id: 4, text: "退出登录", isLogout: true, useImage: true }
    ];
    const checkLoginStatus = () => {
      const token = common_vendor.index.getStorageSync("token");
      const userInfoStr = common_vendor.index.getStorageSync("userInfo");
      if (token && userInfoStr) {
        try {
          userInfo.value = JSON.parse(userInfoStr);
          isLogin.value = true;
          if (userInfo.value.role !== "technician") {
            common_vendor.index.redirectTo({
              url: "/pages/user/info"
            });
          }
        } catch (e) {
          isLogin.value = false;
          goToLogin();
        }
      } else {
        isLogin.value = false;
        goToLogin();
      }
    };
    const goToLogin = () => {
      common_vendor.index.redirectTo({
        url: "/pages/login/Login"
      });
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
              common_vendor.index.showToast({
                title: "已退出登录",
                icon: "success"
              });
              setTimeout(() => {
                common_vendor.index.reLaunch({
                  url: "/pages/index"
                });
              }, 1500);
            }
          }
        });
      } else {
        common_vendor.index.redirectTo({
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
      var _a, _b, _c;
      return {
        a: ((_a = userInfo.value) == null ? void 0 : _a.avatar) || "/static/images/default-avatar.png",
        b: common_vendor.t(((_b = userInfo.value) == null ? void 0 : _b.username) || "技术员"),
        c: common_vendor.t(((_c = userInfo.value) == null ? void 0 : _c.department) || "维修部门"),
        d: common_vendor.f(menuList, (item, k0, i0) => {
          return common_vendor.e({
            a: item.useImage && item.isLogout
          }, item.useImage && item.isLogout ? {
            b: common_assets._imports_0$1
          } : item.useImage ? {
            d: common_assets._imports_1
          } : {
            e: "bc9fbc61-0-" + i0,
            f: common_vendor.p({
              type: item.icon,
              size: "20",
              color: "#666"
            })
          }, {
            c: item.useImage,
            g: common_vendor.t(item.text),
            h: "bc9fbc61-1-" + i0,
            i: item.id,
            j: common_vendor.o(($event) => handleMenuClick(item), item.id)
          });
        }),
        e: common_vendor.p({
          type: "right",
          size: "16",
          color: "#ccc"
        }),
        f: common_vendor.p({
          role: "technician",
          active: "user"
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-bc9fbc61"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/employee/info.js.map
