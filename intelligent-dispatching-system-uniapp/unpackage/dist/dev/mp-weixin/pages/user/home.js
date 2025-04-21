"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_orderAPI = require("../../api/orderAPI.js");
if (!Math) {
  (OrderCard + TabBar)();
}
const TabBar = () => "../../components/tab-bar/index.js";
const OrderCard = () => "../../components/order-card/index.js";
const _sfc_main = {
  __name: "home",
  setup(__props) {
    const orderList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const fetchUserOrders = async () => {
      loading.value = true;
      const userInfoStr = common_vendor.index.getStorageSync("userInfo");
      const userInfo = JSON.parse(userInfoStr);
      try {
        const res = await api_orderAPI.getUserOrders(userInfo.userId);
        if (res.status === 200) {
          common_vendor.index.__f__("log", "at pages/user/home.vue:60", res.data);
          orderList.value = res.data || [];
        } else {
          common_vendor.index.showToast({
            title: res.message || "获取工单失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/home.vue:69", "获取工单列表失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const viewOrderDetail = (orderId) => {
      common_vendor.index.navigateTo({
        url: `/pages/user/order-detail?id=${orderId}`
      });
    };
    common_vendor.onMounted(() => {
      fetchUserOrders();
      common_vendor.index.$on("order-updated", fetchUserOrders);
    });
    common_vendor.onShow(() => {
      fetchUserOrders();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: orderList.value.length === 0
      }, orderList.value.length === 0 ? {
        b: common_assets._imports_0$2
      } : {}, {
        c: common_vendor.f(orderList.value, (item, index, i0) => {
          return {
            a: index,
            b: common_vendor.o(viewOrderDetail, index),
            c: "f834fd70-0-" + i0,
            d: common_vendor.p({
              order: item
            })
          };
        }),
        d: common_vendor.p({
          role: "user",
          active: "index"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f834fd70"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/home.js.map
