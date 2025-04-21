"use strict";
const common_vendor = require("../../common/vendor.js");
const api_orderAPI = require("../../api/orderAPI.js");
const _sfc_main = {
  __name: "order-detail",
  setup(__props) {
    const orderDetail = common_vendor.ref({});
    const employeeName = common_vendor.ref("");
    const loading = common_vendor.ref(false);
    const markers = common_vendor.computed(() => {
      if (!orderDetail.value.location_latitude || !orderDetail.value.location_longitude) {
        return [];
      }
      return [{
        id: 1,
        latitude: orderDetail.value.location_latitude,
        longitude: orderDetail.value.location_longitude,
        iconPath: "/static/images/marker.png",
        width: 30,
        height: 30
      }];
    });
    const orderId = common_vendor.ref("");
    const fetchOrderDetail = async () => {
      loading.value = true;
      try {
        const res = await api_orderAPI.getOrderDetail(orderId.value);
        if (res.status === 200) {
          orderDetail.value = res.data || {};
        } else {
          common_vendor.index.showToast({
            title: res.message || "获取工单详情失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/order-detail.vue:134", "获取工单详情失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const cancelOrder = async () => {
      common_vendor.index.showModal({
        title: "确认取消",
        content: "确定要取消该工单吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await api_orderAPI.cancelOrder(orderId.value);
              if (result.status === 200) {
                common_vendor.index.showToast({
                  title: "工单已取消",
                  icon: "success"
                });
                fetchOrderDetail();
                common_vendor.index.$emit("order-updated");
              } else {
                common_vendor.index.showToast({
                  title: result.message || "取消失败",
                  icon: "none"
                });
              }
            } catch (error) {
              common_vendor.index.__f__("error", "at pages/user/order-detail.vue:169", "取消工单失败", error);
              common_vendor.index.showToast({
                title: "网络异常，请稍后重试",
                icon: "none"
              });
            }
          }
        }
      });
    };
    const formatTime = (timestamp) => {
      if (!timestamp)
        return "暂无";
      const date = typeof timestamp === "string" ? new Date(timestamp.replace(/-/g, "/").replace("T", " ")) : new Date(timestamp);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hour = date.getHours().toString().padStart(2, "0");
      const minute = date.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hour}:${minute}`;
    };
    const getOrderTypeName = (type) => {
      const typeMap = {
        "repair": "设备报修",
        "complaint": "服务投诉"
      };
      return typeMap[type] || "未知类型";
    };
    const getStatusText = (status) => {
      const statusMap = {
        "pending": "待处理",
        "assigned": "已分配工人",
        "in_progress": "处理中",
        "completed": "已完成",
        "closed": "已关闭"
      };
      return statusMap[status] || "未知状态";
    };
    const getStatusClass = (status) => {
      const classMap = {
        "pending": "status-pending",
        "assigned": "status-assigned",
        "in_progress": "status-in-progress",
        "completed": "status-completed",
        "closed": "status-closed"
      };
      return classMap[status] || "";
    };
    const getPriorityText = (priority) => {
      const priorityMap = {
        1: "低",
        2: "中",
        3: "高",
        4: "紧急",
        5: "特急"
      };
      return priorityMap[priority] || `${priority}级`;
    };
    common_vendor.onLoad((options) => {
      common_vendor.index.__f__("log", "at pages/user/order-detail.vue:244", "页面参数:", options);
      if (options && options.id) {
        orderId.value = options.id;
        fetchOrderDetail();
      } else {
        common_vendor.index.showToast({
          title: "工单ID不存在",
          icon: "none"
        });
        setTimeout(() => {
          common_vendor.index.navigateBack();
        }, 1500);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: orderDetail.value.location_latitude || 39.909,
        b: orderDetail.value.location_longitude || 116.397,
        c: markers.value,
        d: common_vendor.t(getStatusText(orderDetail.value.status)),
        e: common_vendor.n(getStatusClass(orderDetail.value.status)),
        f: common_vendor.t(orderDetail.value.order_id || "暂无"),
        g: common_vendor.t(getOrderTypeName(orderDetail.value.order_type)),
        h: common_vendor.t(orderDetail.value.location || "暂无"),
        i: common_vendor.t(formatTime(orderDetail.value.created_at)),
        j: orderDetail.value.priority
      }, orderDetail.value.priority ? {
        k: common_vendor.t(getPriorityText(orderDetail.value.priority))
      } : {}, {
        l: orderDetail.value.deadline
      }, orderDetail.value.deadline ? {
        m: common_vendor.t(formatTime(orderDetail.value.deadline))
      } : {}, {
        n: common_vendor.t(orderDetail.value.description || "暂无描述"),
        o: orderDetail.value.status !== "pending"
      }, orderDetail.value.status !== "pending" ? common_vendor.e({
        p: orderDetail.value.assigned_employee
      }, orderDetail.value.assigned_employee ? {
        q: common_vendor.t(employeeName.value || "工号: " + orderDetail.value.assigned_employee)
      } : {}, {
        r: orderDetail.value.resolved_at
      }, orderDetail.value.resolved_at ? {
        s: common_vendor.t(formatTime(orderDetail.value.resolved_at))
      } : {}) : {}, {
        t: orderDetail.value.status === "pending"
      }, orderDetail.value.status === "pending" ? {
        v: common_vendor.o(cancelOrder)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-29badeee"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/order-detail.js.map
