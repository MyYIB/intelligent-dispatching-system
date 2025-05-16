"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_uni_icons + SignOverlay + OrderOverlay)();
}
const SignOverlay = () => "../overlay/sign.js";
const OrderOverlay = () => "../overlay/order.js";
const _sfc_main = {
  __name: "index",
  props: {
    role: {
      type: String,
      default: "customer"
    },
    active: {
      type: String,
      default: "user"
    }
  },
  setup(__props) {
    const props = __props;
    const customerTabs = [
      { id: "index", text: "首页", icon: "home", url: "/pages/user/home" },
      { id: "add", text: "", icon: "", isCenter: true },
      { id: "user", text: "我的", icon: "person", url: "/pages/user/info" }
    ];
    const employeeTabs = [
      { id: "index", text: "工作台", icon: "home", url: "/pages/employee/home" },
      { id: "add", text: "", icon: "add", isCenter: true },
      { id: "user", text: "我的", icon: "person", url: "/pages/employee/info" }
    ];
    const tabs = common_vendor.ref([]);
    const showOverlay = common_vendor.ref(false);
    common_vendor.onMounted(() => {
      if (props.role === "technician") {
        tabs.value = employeeTabs;
      } else {
        tabs.value = customerTabs;
      }
    });
    const switchTab = (url, isCenter = false) => {
      if (isCenter) {
        const token2 = common_vendor.index.getStorageSync("token");
        const userInfo2 = common_vendor.index.getStorageSync("userInfo");
        if (!token2 || !userInfo2) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none",
            duration: 1500
          });
          setTimeout(() => {
            common_vendor.index.redirectTo({
              url: "/pages/login/Login"
            });
          }, 1500);
          return;
        }
        showOverlay.value = true;
        return;
      }
      const token = common_vendor.index.getStorageSync("token");
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      if (!token || !userInfo) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none",
          duration: 1500
        });
        setTimeout(() => {
          common_vendor.index.redirectTo({
            url: "/pages/login/Login"
          });
        }, 1500);
        return;
      }
      common_vendor.index.redirectTo({
        url
      });
    };
    const closeOverlay = () => {
      showOverlay.value = false;
    };
    const handleSuccess = (data) => {
      if (data && (data.status === "available" || data.status === "off")) {
        const pages = getCurrentPages();
        const currentPage = pages[pages.length - 1];
        if (currentPage && currentPage.onLoad) {
          currentPage.onLoad(currentPage.options || {});
        }
      }
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(tabs.value, (item, index, i0) => {
          return common_vendor.e({
            a: item.isCenter
          }, item.isCenter ? {
            b: common_assets._imports_0$3
          } : {
            c: "059cbefb-0-" + i0,
            d: common_vendor.p({
              type: item.icon,
              size: "24",
              color: __props.active === item.id ? "#007aff" : "#999"
            })
          }, {
            e: common_vendor.n({
              "center-icon": item.isCenter
            }),
            f: !item.isCenter
          }, !item.isCenter ? {
            g: common_vendor.t(item.text),
            h: __props.active === item.id ? 1 : ""
          } : {}, {
            i: item.id,
            j: common_vendor.n({
              "active": __props.active === item.id,
              "center-item": item.isCenter
            }),
            k: common_vendor.o(($event) => item.isCenter ? switchTab("", true) : switchTab(item.url), item.id)
          });
        }),
        b: showOverlay.value && props.role === "technician"
      }, showOverlay.value && props.role === "technician" ? {
        c: common_vendor.o(closeOverlay),
        d: common_vendor.o(handleSuccess)
      } : {}, {
        e: showOverlay.value && props.role !== "technician"
      }, showOverlay.value && props.role !== "technician" ? {
        f: common_vendor.o(closeOverlay),
        g: common_vendor.o(handleSuccess)
      } : {});
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-059cbefb"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/components/tab-bar/index.js.map
