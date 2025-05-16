"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_orderAPI = require("../../api/orderAPI.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_uni_popup2 = common_vendor.resolveComponent("uni-popup");
  (_easycom_uni_icons2 + _easycom_uni_popup2)();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
const _easycom_uni_popup = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-popup/uni-popup.js";
if (!Math) {
  (OrderCard + _easycom_uni_icons + TabBar + _easycom_uni_popup)();
}
const TabBar = () => "../../components/tab-bar/index.js";
const OrderCard = () => "../../components/order-card/index.js";
const _sfc_main = {
  __name: "home",
  setup(__props) {
    const orderList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const refreshing = common_vendor.ref(false);
    const activeTab = common_vendor.ref("assigned");
    const maintenanceList = common_vendor.ref([]);
    const addMaintenancePopup = common_vendor.ref(null);
    const newMaintenance = common_vendor.ref({
      equipmentName: "",
      maintenanceDate: "",
      details: "",
      employeeId: null,
      status: "pending"
    });
    const filteredOrderList = common_vendor.computed(() => {
      if (activeTab.value === "assigned") {
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
      if (tab === "maintenance") {
        fetchMaintenanceList();
      }
    };
    const onRefresh = () => {
      refreshing.value = true;
      if (activeTab.value === "maintenance") {
        fetchMaintenanceList().then(() => {
          refreshing.value = false;
        });
      } else {
        fetchEmployeeOrders().then(() => {
          refreshing.value = false;
        });
      }
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
      const employeeInfoStr = common_vendor.index.getStorageSync("employeeInfo");
      const employeeInfo = JSON.parse(employeeInfoStr);
      if (!employeeInfo.employeeId) {
        common_vendor.index.showToast({
          title: "员工ID不存在",
          icon: "none"
        });
        loading.value = false;
        return;
      }
      try {
        const res = await api_orderAPI.getEmployeeOrders(employeeInfo.employeeId);
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
          common_vendor.index.__f__("log", "at pages/employee/home.vue:275", "获取工单列表成功，但没有数据");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/home.vue:278", "获取工单列表失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const fetchMaintenanceList = async () => {
      loading.value = true;
      const employeeInfoStr = common_vendor.index.getStorageSync("employeeInfo");
      if (!employeeInfoStr) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        loading.value = false;
        return;
      }
      const employeeInfo = JSON.parse(employeeInfoStr);
      if (!employeeInfo.employeeId) {
        common_vendor.index.showToast({
          title: "员工ID不存在",
          icon: "none"
        });
        loading.value = false;
        return;
      }
      try {
        const res = await api_orderAPI.getMaintenanceList(employeeInfo.employeeId);
        if (res.status === 200 && res.data) {
          maintenanceList.value = res.data;
        } else {
          maintenanceList.value = [];
          common_vendor.index.__f__("log", "at pages/employee/home.vue:319", "获取设备维护列表成功，但没有数据");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/home.vue:322", "获取设备维护列表失败", error);
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
    const startMaintenance = async (maintenanceId) => {
      common_vendor.index.showModal({
        title: "确认开始",
        content: "确定要开始此设备维护任务吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              const updateRes = await api_orderAPI.updateMaintenanceStatus({ maintenanceId, status: "in_progress" });
              if (updateRes.status === 200) {
                common_vendor.index.showToast({
                  title: "已开始维护",
                  icon: "success"
                });
                fetchMaintenanceList();
              } else {
                throw new Error(updateRes.msg || "开始维护失败");
              }
            } catch (error) {
              common_vendor.index.__f__("error", "at pages/employee/home.vue:361", "开始维护失败", error);
              common_vendor.index.showToast({
                title: error.message || "网络异常，请稍后重试",
                icon: "none"
              });
            }
          }
        }
      });
    };
    const completeMaintenance = async (maintenanceId) => {
      common_vendor.index.showModal({
        title: "确认完成",
        content: "确定要完成此设备维护任务吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              const updateRes = await api_orderAPI.updateMaintenanceStatus({ maintenanceId, status: "completed" });
              if (updateRes.status === 200) {
                common_vendor.index.showToast({
                  title: "维护已完成",
                  icon: "success"
                });
                fetchMaintenanceList();
              } else {
                throw new Error(updateRes.msg || "完成维护失败");
              }
            } catch (error) {
              common_vendor.index.__f__("error", "at pages/employee/home.vue:392", "完成维护失败", error);
              common_vendor.index.showToast({
                title: error.message || "网络异常，请稍后重试",
                icon: "none"
              });
            }
          }
        }
      });
    };
    const showAddMaintenancePopup = () => {
      newMaintenance.value = {
        equipmentName: "",
        maintenanceDate: "",
        details: "",
        employeeId: null,
        status: "pending"
      };
      const employeeInfoStr = common_vendor.index.getStorageSync("employeeInfo");
      if (employeeInfoStr) {
        const employeeInfo = JSON.parse(employeeInfoStr);
        newMaintenance.value.employeeId = employeeInfo.employeeId;
      }
      addMaintenancePopup.value.open();
    };
    const closeAddMaintenancePopup = () => {
      addMaintenancePopup.value.close();
    };
    const onDateChange = (e) => {
      newMaintenance.value.maintenanceDate = e.detail.value;
    };
    const submitMaintenance = async () => {
      if (!newMaintenance.value.equipmentName) {
        common_vendor.index.showToast({
          title: "请输入设备名称",
          icon: "none"
        });
        return;
      }
      if (!newMaintenance.value.maintenanceDate) {
        common_vendor.index.showToast({
          title: "请选择维护时间",
          icon: "none"
        });
        return;
      }
      if (!newMaintenance.value.details) {
        common_vendor.index.showToast({
          title: "请输入维护内容",
          icon: "none"
        });
        return;
      }
      try {
        const maintenanceData = {
          equipmentName: newMaintenance.value.equipmentName,
          maintenanceDate: newMaintenance.value.maintenanceDate ? newMaintenance.value.maintenanceDate + "T00:00:00" : null,
          details: newMaintenance.value.details,
          employeeId: newMaintenance.value.employeeId,
          status: newMaintenance.value.status
        };
        const res = await api_orderAPI.createMaintenanceOrder(maintenanceData);
        if (res.status === 200) {
          common_vendor.index.showToast({
            title: "添加成功",
            icon: "success"
          });
          closeAddMaintenancePopup();
          fetchMaintenanceList();
        } else {
          throw new Error(res.msg || "添加失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/home.vue:483", "添加设备维护失败", error);
        common_vendor.index.showToast({
          title: error.message || "网络异常，请稍后重试",
          icon: "none"
        });
      }
    };
    const getMaintenanceStatusText = (status) => {
      const statusMap = {
        "pending": "待处理",
        "in_progress": "处理中",
        "completed": "已完成"
      };
      return statusMap[status] || "未知状态";
    };
    const getMaintenanceStatusClass = (status) => {
      const classMap = {
        "pending": "status-pending",
        "in_progress": "status-in-progress",
        "completed": "status-completed"
      };
      return classMap[status] || "";
    };
    const formatDate = (dateStr) => {
      if (!dateStr)
        return "";
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
    };
    const handleOrderUpdated = () => {
      fetchEmployeeOrders();
    };
    const handleMaintenanceUpdated = () => {
      fetchMaintenanceList();
    };
    common_vendor.onMounted(() => {
      fetchEmployeeOrders();
      common_vendor.index.$on("order-updated", handleOrderUpdated);
      common_vendor.index.$on("maintenance-updated", handleMaintenanceUpdated);
    });
    common_vendor.onShow(() => {
      if (activeTab.value === "maintenance") {
        fetchMaintenanceList();
      } else {
        fetchEmployeeOrders();
      }
    });
    common_vendor.onUnload(() => {
      common_vendor.index.$off("order-updated", handleOrderUpdated);
      common_vendor.index.$off("maintenance-updated", handleMaintenanceUpdated);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: activeTab.value === "assigned" ? 1 : "",
        b: common_vendor.o(($event) => switchTab("assigned")),
        c: activeTab.value === "completed" ? 1 : "",
        d: common_vendor.o(($event) => switchTab("completed")),
        e: activeTab.value === "maintenance" ? 1 : "",
        f: common_vendor.o(($event) => switchTab("maintenance")),
        g: activeTab.value === "assigned" || activeTab.value === "completed"
      }, activeTab.value === "assigned" || activeTab.value === "completed" ? common_vendor.e({
        h: filteredOrderList.value.length === 0
      }, filteredOrderList.value.length === 0 ? {
        i: common_assets._imports_0$2,
        j: common_vendor.t(activeTab.value === "assigned" ? "暂无未完成工单" : "暂无已完成工单")
      } : {}, {
        k: common_vendor.f(filteredOrderList.value, (item, index, i0) => {
          return {
            a: index,
            b: common_vendor.o(viewOrderDetail, index),
            c: "08871800-0-" + i0,
            d: common_vendor.p({
              order: item
            })
          };
        })
      }) : {}, {
        l: activeTab.value === "maintenance"
      }, activeTab.value === "maintenance" ? common_vendor.e({
        m: maintenanceList.value.length === 0
      }, maintenanceList.value.length === 0 ? {
        n: common_assets._imports_0$2
      } : {}, {
        o: common_vendor.f(maintenanceList.value, (item, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.equipmentName),
            b: common_vendor.t(getMaintenanceStatusText(item.status)),
            c: common_vendor.n(getMaintenanceStatusClass(item.status)),
            d: common_vendor.t(item.maintenanceId),
            e: common_vendor.t(formatDate(item.maintenanceDate)),
            f: common_vendor.t(item.details),
            g: item.status === "pending"
          }, item.status === "pending" ? {
            h: common_vendor.o(($event) => startMaintenance(item.maintenanceId), index)
          } : {}, {
            i: item.status === "in_progress"
          }, item.status === "in_progress" ? {
            j: common_vendor.o(($event) => completeMaintenance(item.maintenanceId), index)
          } : {}, {
            k: index
          });
        }),
        p: common_vendor.p({
          type: "plusempty",
          size: "24",
          color: "#007aff"
        }),
        q: common_vendor.o(showAddMaintenancePopup)
      }) : {}, {
        r: common_vendor.o(onRefresh),
        s: refreshing.value,
        t: common_vendor.p({
          role: "technician",
          active: "index"
        }),
        v: newMaintenance.value.equipmentName,
        w: common_vendor.o(($event) => newMaintenance.value.equipmentName = $event.detail.value),
        x: common_vendor.t(newMaintenance.value.maintenanceDate || "请选择日期"),
        y: newMaintenance.value.maintenanceDate,
        z: common_vendor.o(onDateChange),
        A: newMaintenance.value.details,
        B: common_vendor.o(($event) => newMaintenance.value.details = $event.detail.value),
        C: common_vendor.t(newMaintenance.value.details.length),
        D: common_vendor.o(closeAddMaintenancePopup),
        E: common_vendor.o(submitMaintenance),
        F: common_vendor.sr(addMaintenancePopup, "08871800-3", {
          "k": "addMaintenancePopup"
        }),
        G: common_vendor.p({
          type: "center"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-08871800"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/employee/home.js.map
