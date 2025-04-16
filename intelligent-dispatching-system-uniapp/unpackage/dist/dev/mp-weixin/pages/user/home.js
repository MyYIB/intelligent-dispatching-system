"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_orderAPI = require("../../api/orderAPI.js");
if (!Math) {
  TabBar();
}
const TabBar = () => "../../components/tab-bar/index.js";
const _sfc_main = {
  __name: "home",
  setup(__props) {
    const orderList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const fetchUserOrders = async () => {
      loading.value = true;
      try {
        const res = await api_orderAPI.getUserOrders();
        if (res.code === 200) {
          orderList.value = res.data || [];
        } else {
          common_vendor.index.showToast({
            title: res.message || "获取工单失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/home.vue:70", "获取工单列表失败", error);
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
    const formatTime = (timestamp) => {
      if (!timestamp)
        return "";
      const date = new Date(timestamp);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hour = date.getHours().toString().padStart(2, "0");
      const minute = date.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hour}:${minute}`;
    };
    const getStatusText = (status) => {
      const statusMap = {
        "pending": "待处理",
        "processing": "处理中",
        "completed": "已完成",
        "cancelled": "已取消"
      };
      return statusMap[status] || "未知状态";
    };
    const getStatusClass = (status) => {
      const classMap = {
        "pending": "status-pending",
        "processing": "status-processing",
        "completed": "status-completed",
        "cancelled": "status-cancelled"
      };
      return classMap[status] || "";
    };
    common_vendor.onMounted(() => {
      fetchUserOrders();
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
            a: common_vendor.t(item.type),
            b: common_vendor.t(getStatusText(item.status)),
            c: common_vendor.n(getStatusClass(item.status)),
            d: common_vendor.t(item.description),
            e: common_vendor.t(formatTime(item.createTime)),
            f: index,
            g: common_vendor.o(($event) => viewOrderDetail(item.id), index)
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
