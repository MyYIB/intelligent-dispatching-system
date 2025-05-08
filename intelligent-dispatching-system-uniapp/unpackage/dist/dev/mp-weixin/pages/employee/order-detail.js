"use strict";
const common_vendor = require("../../common/vendor.js");
const api_orderAPI = require("../../api/orderAPI.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_uni_popup2 = common_vendor.resolveComponent("uni-popup");
  (_easycom_uni_icons2 + _easycom_uni_popup2)();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
const _easycom_uni_popup = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-popup/uni-popup.js";
if (!Math) {
  (_easycom_uni_icons + _easycom_uni_popup)();
}
const _sfc_main = {
  __name: "order-detail",
  setup(__props) {
    const orderDetail = common_vendor.ref({});
    const loading = common_vendor.ref(false);
    const orderId = common_vendor.ref("");
    const userInfo = common_vendor.ref({});
    const inventoryPopup = common_vendor.ref(null);
    const inventoryList = common_vendor.ref([]);
    const inventoryUseList = common_vendor.ref([]);
    const markers = common_vendor.computed(() => {
      if (!orderDetail.value.locationLatitude || !orderDetail.value.locationLongitude) {
        return [];
      }
      return [{
        id: 1,
        latitude: orderDetail.value.locationLatitude,
        longitude: orderDetail.value.locationLongitude,
        iconPath: "/static/images/marker.png",
        width: 30,
        height: 30,
        anchor: {
          x: 0.5,
          y: 1
        }
      }];
    });
    const fetchOrderDetail = async () => {
      loading.value = true;
      try {
        const res = await api_orderAPI.getOrderDetail(orderId.value);
        if (res.status === 200) {
          orderDetail.value = res.data || {};
          if (orderDetail.value.status === "in_progress" || orderDetail.value.status === "completed") {
            const inventoryRes = await api_orderAPI.getOrderInventory(orderId.value);
            if (inventoryRes.status === 200) {
              inventoryUseList.value = inventoryRes.data || [];
            }
          }
          if (orderDetail.value.status !== "pending") {
            const orderRes = await api_orderAPI.getOrderUserInfo(orderId.value);
            if (orderRes.status === 200) {
              userInfo.value = orderRes.data || {};
            }
          }
        } else {
          common_vendor.index.showToast({
            title: res.message || "获取工单详情失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:223", "获取工单详情失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const getInventoryList = async () => {
      try {
        const res = await api_orderAPI.fetchInventoryList();
        if (res.status === 200) {
          inventoryList.value = res.data.map((item) => ({
            ...item,
            useQuantity: 0
          }));
        } else {
          common_vendor.index.showToast({
            title: "获取备料列表失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:251", "获取备料列表失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      }
    };
    const handleOrderAction = () => {
      if (orderDetail.value.status === "assigned") {
        openInventoryPopup();
      } else if (orderDetail.value.status === "in_progress") {
        completeOrder();
      }
    };
    const openInventoryPopup = async () => {
      await getInventoryList();
      inventoryPopup.value.open();
    };
    const closeInventoryPopup = () => {
      inventoryPopup.value.close();
    };
    const increaseQuantity = (index) => {
      const item = inventoryList.value[index];
      if (item.useQuantity < item.quantity) {
        item.useQuantity++;
      }
    };
    const decreaseQuantity = (index) => {
      const item = inventoryList.value[index];
      if (item.useQuantity > 0) {
        item.useQuantity--;
      }
    };
    const validateQuantity = (index) => {
      const item = inventoryList.value[index];
      let value = parseInt(item.useQuantity);
      if (isNaN(value) || value < 0) {
        item.useQuantity = 0;
      } else if (value > item.quantity) {
        item.useQuantity = item.quantity;
      }
    };
    const confirmUseInventory = async () => {
      const selectedItems = inventoryList.value.filter((item) => item.useQuantity > 0);
      if (selectedItems.length === 0) {
        common_vendor.index.showToast({
          title: "请选择至少一种备料",
          icon: "none"
        });
        return;
      }
      loading.value = true;
      try {
        for (const item of selectedItems) {
          const inventoryUse = {
            inventoryId: item.itemId,
            orderId: parseInt(orderId.value),
            num: parseInt(item.useQuantity)
          };
          const res = await api_orderAPI.submitInventoryUse(inventoryUse);
          if (!res || res.status !== 200) {
            throw new Error((res == null ? void 0 : res.message) || "添加备料使用记录失败");
          }
        }
        const updateRes = await api_orderAPI.updateOrderStatus(parseInt(orderId.value), "in_progress");
        if (updateRes.status === 200) {
          common_vendor.index.showToast({
            title: "开始处理工单",
            icon: "success"
          });
          closeInventoryPopup();
          fetchOrderDetail();
          common_vendor.index.$emit("order-updated");
        } else {
          throw new Error(updateRes.message || "更新工单状态失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:362", "处理工单失败", error);
        common_vendor.index.showToast({
          title: error.message || "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const completeOrder = async () => {
      common_vendor.index.showModal({
        title: "确认完成",
        content: "确定已解决问题并完成该工单吗？",
        success: async (res) => {
          if (res.confirm) {
            loading.value = true;
            try {
              const updateRes = await api_orderAPI.updateOrderStatus(parseInt(orderId.value), "completed");
              if (updateRes.status === 200) {
                common_vendor.index.showToast({
                  title: "工单已完成",
                  icon: "success"
                });
                fetchOrderDetail();
                common_vendor.index.$emit("order-updated");
              } else {
                throw new Error(updateRes.message || "完成工单失败");
              }
            } catch (error) {
              common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:399", "完成工单失败", error);
              common_vendor.index.showToast({
                title: error.message || "网络异常，请稍后重试",
                icon: "none"
              });
            } finally {
              loading.value = false;
            }
          }
        }
      });
    };
    const getActionButtonText = () => {
      const statusMap = {
        "assigned": "开始处理",
        "in_progress": "完成工单"
      };
      return statusMap[orderDetail.value.status] || "";
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
        "nrepair": "设备报修",
        "complaint": "服务投诉"
      };
      return typeMap[type] || "未知类型";
    };
    const getRepairTypeName = (type) => {
      const typeMap = {
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
      return typeMap[type] || "未知类型";
    };
    const getStatusText = (status) => {
      const statusMap = {
        "pending": "待处理",
        "assigned": "已分配",
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
    common_vendor.onLoad((options) => {
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
        a: orderDetail.value.locationLatitude,
        b: orderDetail.value.locationLongitude,
        c: markers.value,
        d: common_vendor.t(getStatusText(orderDetail.value.status)),
        e: common_vendor.n(getStatusClass(orderDetail.value.status)),
        f: common_vendor.t(orderDetail.value.orderId || "暂无"),
        g: common_vendor.t(getOrderTypeName(orderDetail.value.orderType)),
        h: common_vendor.t(getRepairTypeName(orderDetail.value.repairType)),
        i: common_vendor.t(orderDetail.value.location || "暂无"),
        j: common_vendor.t(formatTime(orderDetail.value.createdAt)),
        k: orderDetail.value.deadline
      }, orderDetail.value.deadline ? {
        l: common_vendor.t(formatTime(orderDetail.value.deadline))
      } : {}, {
        m: common_vendor.t(orderDetail.value.description || "暂无描述"),
        n: common_vendor.t(userInfo.value.username || "暂无"),
        o: common_vendor.t(userInfo.value.phone || "暂无"),
        p: orderDetail.value.status === "in_progress" || orderDetail.value.status === "completed"
      }, orderDetail.value.status === "in_progress" || orderDetail.value.status === "completed" ? common_vendor.e({
        q: inventoryUseList.value.length === 0
      }, inventoryUseList.value.length === 0 ? {} : {
        r: common_vendor.f(inventoryUseList.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.item_name),
            b: common_vendor.t(item.num),
            c: index
          };
        })
      }) : {}, {
        s: common_vendor.t(getActionButtonText()),
        t: orderDetail.value.status === "assigned" ? 1 : "",
        v: orderDetail.value.status === "in_progress" ? 1 : "",
        w: common_vendor.o(handleOrderAction),
        x: loading.value,
        y: common_vendor.o(closeInventoryPopup),
        z: common_vendor.p({
          type: "close",
          size: "20",
          color: "#999"
        }),
        A: inventoryList.value.length === 0
      }, inventoryList.value.length === 0 ? {} : {
        B: common_vendor.f(inventoryList.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.itemName),
            b: common_vendor.t(item.quantity),
            c: common_vendor.o(($event) => decreaseQuantity(index), item.itemId),
            d: common_vendor.o([($event) => item.useQuantity = $event.detail.value, item.itemId, ($event) => validateQuantity(index), item.itemId], item.itemId),
            e: item.useQuantity,
            f: common_vendor.o(($event) => increaseQuantity(index), item.itemId),
            g: item.itemId
          };
        })
      }, {
        C: common_vendor.o(closeInventoryPopup),
        D: common_vendor.o(confirmUseInventory),
        E: common_vendor.sr(inventoryPopup, "7dfc586b-0", {
          "k": "inventoryPopup"
        }),
        F: common_vendor.p({
          type: "center"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-7dfc586b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/employee/order-detail.js.map
