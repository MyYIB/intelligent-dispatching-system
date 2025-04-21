"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "index",
  props: {
    order: {
      type: Object,
      required: true
    }
  },
  emits: ["click"],
  setup(__props, { emit: __emit }) {
    const props = __props;
    const emit = __emit;
    const handleClick = () => {
      emit("click", props.order.orderId);
    };
    const repairTypeMap = {
      1: "宽带故障",
      2: "网络卡顿",
      3: "断网问题",
      4: "路由器问题",
      5: "设备损坏",
      6: "网络延迟高",
      7: "电视信号问题",
      8: "Wi-Fi信号弱",
      10: "5G信号差",
      11: "机房维护",
      12: "电缆故障",
      13: "远程协助请求",
      14: "设备升级"
    };
    const getRepairTypeName = (type) => {
      return repairTypeMap[type] || "未知类型";
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
    const formatTime = (timestamp) => {
      if (!timestamp)
        return "";
      const date = typeof timestamp === "string" ? new Date(timestamp.replace(/-/g, "/").replace("T", " ")) : new Date(timestamp);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hour = date.getHours().toString().padStart(2, "0");
      const minute = date.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hour}:${minute}`;
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(__props.order.repairTypeName || getRepairTypeName(__props.order.repairType)),
        b: common_vendor.t(getStatusText(__props.order.status)),
        c: common_vendor.n(getStatusClass(__props.order.status)),
        d: common_vendor.t(__props.order.description),
        e: common_vendor.t(formatTime(__props.order.createdAt)),
        f: common_vendor.o(handleClick)
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-dc142323"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/components/order-card/index.js.map
