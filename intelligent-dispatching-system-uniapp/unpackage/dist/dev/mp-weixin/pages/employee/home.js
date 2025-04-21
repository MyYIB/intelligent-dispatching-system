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
    const refreshing = common_vendor.ref(false);
    const activeTab = common_vendor.ref("pending");
    const filteredOrderList = common_vendor.computed(() => {
      if (activeTab.value === "pending") {
        return orderList.value.filter(
          (order) => ["pending", "assigned", "in_progress"].includes(order.status)
        );
      } else {
        return orderList.value.filter(
          (order) => ["completed", "closed"].includes(order.status)
        );
      }
    });
    const switchTab = (tab) => {
      activeTab.value = tab;
    };
    const onRefresh = () => {
      refreshing.value = true;
      fetchEmployeeOrders().then(() => {
        refreshing.value = false;
      });
    };
    const fetchEmployeeOrders = async () => {
      loading.value = true;
      const userInfoStr = common_vendor.index.getStorageSync("userInfo");
      if (!userInfoStr) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        setTimeout(() => {
          common_vendor.index.navigateTo({
            url: "/pages/login/index"
          });
        }, 1500);
        loading.value = false;
        return;
      }
      const userInfo = JSON.parse(userInfoStr);
      common_vendor.index.__f__("log", "at pages/employee/home.vue:120", userInfo);
      if (!userInfo.employeeId) {
        common_vendor.index.showToast({
          title: "员工ID不存在",
          icon: "none"
        });
        loading.value = false;
        return;
      }
      try {
        const res = await api_orderAPI.getEmployeeOrders(userInfo.employeeId);
        if (res.status === 200 && res.data) {
          orderList.value = res.data.map((order) => {
            return {
              ...order,
              order_id: order.orderId || order.order_id,
              order_type: order.orderType || order.order_type,
              created_at: order.createdAt || order.created_at,
              assigned_employee: order.assignedEmployee || order.assigned_employee,
              resolved_at: order.resolvedAt || order.resolved_at,
              location_latitude: order.locationLatitude || order.location_latitude,
              location_longitude: order.locationLongitude || order.location_longitude
            };
          });
        } else {
          orderList.value = [];
          common_vendor.index.__f__("log", "at pages/employee/home.vue:149", "获取工单列表成功，但没有数据");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/home.vue:152", "获取工单列表失败", error);
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
        url: `/pages/employee/order-detail?id=${orderId}`
      });
    };
    const handleOrderUpdated = () => {
      fetchEmployeeOrders();
    };
    common_vendor.onMounted(() => {
      fetchEmployeeOrders();
      common_vendor.index.$on("order-updated", handleOrderUpdated);
    });
    common_vendor.onShow(() => {
      fetchEmployeeOrders();
    });
    common_vendor.onUnload(() => {
      common_vendor.index.$off("order-updated", handleOrderUpdated);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: activeTab.value === "pending" ? 1 : "",
        b: common_vendor.o(($event) => switchTab("pending")),
        c: activeTab.value === "completed" ? 1 : "",
        d: common_vendor.o(($event) => switchTab("completed")),
        e: filteredOrderList.value.length === 0
      }, filteredOrderList.value.length === 0 ? {
        f: common_assets._imports_0$2,
        g: common_vendor.t(activeTab.value === "pending" ? "暂无未完成工单" : "暂无已完成工单")
      } : {}, {
        h: common_vendor.f(filteredOrderList.value, (item, index, i0) => {
          return {
            a: index,
            b: common_vendor.o(viewOrderDetail, index),
            c: "08871800-0-" + i0,
            d: common_vendor.p({
              order: item
            })
          };
        }),
        i: common_vendor.o(onRefresh),
        j: refreshing.value,
        k: common_vendor.p({
          role: "employee",
          active: "index"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-08871800"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/employee/home.js.map
