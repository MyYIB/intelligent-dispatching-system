"use strict";
const common_vendor = require("../../common/vendor.js");
const api_orderAPI = require("../../api/orderAPI.js");
if (!Array) {
  const _easycom_uni_rate2 = common_vendor.resolveComponent("uni-rate");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_uni_popup2 = common_vendor.resolveComponent("uni-popup");
  (_easycom_uni_rate2 + _easycom_uni_icons2 + _easycom_uni_popup2)();
}
const _easycom_uni_rate = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-rate/uni-rate.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
const _easycom_uni_popup = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-popup/uni-popup.js";
if (!Math) {
  (_easycom_uni_rate + _easycom_uni_icons + _easycom_uni_popup)();
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
    const feedbackPopup = common_vendor.ref(null);
    const feedbackInfo = common_vendor.ref(null);
    const feedbackForm = common_vendor.ref({
      feedback_content: ""
    });
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
          if (orderDetail.value.status === "completed") {
            await fetchFeedbackInfo();
          }
        } else {
          common_vendor.index.showToast({
            title: res.message || "获取工单详情失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:358", "获取工单详情失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const fetchFeedbackInfo = async () => {
      try {
        const res = await api_orderAPI.getFeedbackByOrderId(orderId.value);
        if (res.status === 200) {
          feedbackInfo.value = res.data;
        } else {
          feedbackInfo.value = null;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:378", "获取回访信息失败", error);
        feedbackInfo.value = null;
      }
    };
    const getInventoryList = async () => {
      try {
        const res = await api_orderAPI.fetchInventoryList();
        if (res.status === 200) {
          const inventoryData = res.data.records || res.data;
          if (Array.isArray(inventoryData)) {
            inventoryList.value = inventoryData.map((item) => ({
              ...item,
              useQuantity: 0,
              // 为物料添加分类属性，方便后续分类展示
              category: getCategoryByName(item.itemName)
            }));
          } else {
            common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:401", "物料数据格式不正确:", res.data);
            common_vendor.index.showToast({
              title: "物料数据格式不正确",
              icon: "none"
            });
            inventoryList.value = [];
          }
        } else {
          common_vendor.index.showToast({
            title: "获取备料列表失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:415", "获取备料列表失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      }
    };
    const getCategoryByName = (name) => {
      if (!name)
        return "其他";
      const categoryRules = [
        { keywords: ["光纤", "跳纤", "尾纤", "保护套管"], category: "光纤类" },
        { keywords: ["网线", "电缆", "同轴"], category: "线缆类" },
        { keywords: ["路由器", "交换机", "光猫"], category: "网络设备" },
        { keywords: ["接头", "插座", "插头"], category: "接口配件" },
        { keywords: ["工具", "钳子", "螺丝刀"], category: "工具类" }
      ];
      for (const rule of categoryRules) {
        if (rule.keywords.some((keyword) => name.includes(keyword))) {
          return rule.category;
        }
      }
      return "其他";
    };
    const handleOrderAction = () => {
      if (orderDetail.value.status === "assigned") {
        openInventoryPopup();
      } else if (orderDetail.value.status === "in_progress") {
        completeOrder();
      } else if (orderDetail.value.status === "completed") {
        openFeedbackPopup();
      }
    };
    const openFeedbackPopup = () => {
      feedbackForm.value = {
        feedback_content: ""
      };
      if (feedbackInfo.value) {
        feedbackForm.value.feedback_content = feedbackInfo.value.feedback_content || "";
      }
      feedbackPopup.value.open();
    };
    const submitFeedback = async () => {
      loading.value = true;
      try {
        const employeeInfoStr = common_vendor.index.getStorageSync("employeeInfo");
        if (!employeeInfoStr) {
          throw new Error("请先登录");
        }
        const employeeInfo = JSON.parse(employeeInfoStr);
        const feedbackData = {
          order_id: parseInt(orderId.value),
          employee_id: employeeInfo.employeeId,
          feedback_state: "unrated",
          feedback_content: feedbackForm.value.feedback_content
        };
        if (feedbackInfo.value && feedbackInfo.value.feedback_id) {
          feedbackData.feedback_id = feedbackInfo.value.feedback_id;
        }
        const res = await api_orderAPI.submitFeedbackRecord(feedbackData);
        if (res.status === 200) {
          common_vendor.index.showToast({
            title: "回访已完成",
            icon: "success"
          });
          closeFeedbackPopup();
          await fetchFeedbackInfo();
          common_vendor.index.$emit("feedback-updated");
        } else {
          throw new Error(res.message || "提交回访失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:523", "提交回访失败", error);
        common_vendor.index.showToast({
          title: error.message || "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const closeFeedbackPopup = () => {
      feedbackPopup.value.close();
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
      const selectedItems2 = inventoryList.value.filter((item) => item.useQuantity > 0);
      if (selectedItems2.length === 0) {
        common_vendor.index.showToast({
          title: "请选择至少一种备料",
          icon: "none"
        });
        return;
      }
      loading.value = true;
      try {
        for (const item of selectedItems2) {
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
        common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:628", "处理工单失败", error);
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
              common_vendor.index.__f__("error", "at pages/employee/order-detail.vue:665", "完成工单失败", error);
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
        "in_progress": "完成工单",
        "completed": feedbackInfo.value && feedbackInfo.value.feedback_state === "completed" ? "已完成回访" : "进行回访"
      };
      return statusMap[orderDetail.value.status] || "";
    };
    const showActionButton = common_vendor.computed(() => {
      if (orderDetail.value.status === "assigned" || orderDetail.value.status === "in_progress") {
        return true;
      }
      if (orderDetail.value.status === "completed") {
        return !feedbackInfo.value || feedbackInfo.value.feedbackState === "uncompleted";
      }
      return false;
    });
    const getFeedbackStateText = (state) => {
      const stateMap = {
        "uncompleted": "待回访",
        "completed": "已回访",
        "unrated": "待评价"
      };
      return stateMap[state] || "未知状态";
    };
    const getFeedbackStateClass = (state) => {
      const classMap = {
        "uncompleted": "state-pending",
        "unrated": "state-in-progress",
        "completed": "state-completed"
      };
      return classMap[state] || "";
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
    const searchKeyword = common_vendor.ref("");
    const currentCategory = common_vendor.ref("全部");
    const inventoryCategories = common_vendor.computed(() => {
      const categories = ["全部"];
      const categorySet = /* @__PURE__ */ new Set();
      inventoryList.value.forEach((item) => {
        if (item.category && !categorySet.has(item.category)) {
          categorySet.add(item.category);
          categories.push(item.category);
        }
      });
      return categories;
    });
    const filteredInventoryList = common_vendor.computed(() => {
      let result = [...inventoryList.value];
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        result = result.filter(
          (item) => item.itemName.toLowerCase().includes(keyword)
        );
      }
      if (currentCategory.value !== "全部") {
        result = result.filter((item) => item.category === currentCategory.value);
      }
      return result;
    });
    const selectedItems = common_vendor.computed(() => {
      return inventoryList.value.filter((item) => item.useQuantity > 0);
    });
    const getOriginalIndex = (item) => {
      return inventoryList.value.findIndex((i) => i.itemId === item.itemId);
    };
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
        s: orderDetail.value.status === "completed"
      }, orderDetail.value.status === "completed" ? common_vendor.e({
        t: !feedbackInfo.value
      }, !feedbackInfo.value ? {} : common_vendor.e({
        v: common_vendor.t(getFeedbackStateText(feedbackInfo.value.feedbackState)),
        w: common_vendor.n(getFeedbackStateClass(feedbackInfo.value.feedbackState)),
        x: feedbackInfo.value.feedbackState === "completed"
      }, feedbackInfo.value.feedbackState === "completed" ? {
        y: common_vendor.p({
          value: feedbackInfo.value.satisfactionScore || 0,
          size: 18,
          readonly: true
        }),
        z: common_vendor.t(feedbackInfo.value.satisfactionScore || 0)
      } : {}, {
        A: feedbackInfo.value.needTime
      }, feedbackInfo.value.needTime ? {
        B: common_vendor.t(formatTime(feedbackInfo.value.needTime))
      } : {}, {
        C: feedbackInfo.value.feedbackTime
      }, feedbackInfo.value.feedbackTime ? {
        D: common_vendor.t(formatTime(feedbackInfo.value.feedbackTime))
      } : {}, {
        E: feedbackInfo.value.feedbackContent
      }, feedbackInfo.value.feedbackContent ? {
        F: common_vendor.t(feedbackInfo.value.feedbackContent)
      } : {})) : {}, {
        G: showActionButton.value
      }, showActionButton.value ? {
        H: common_vendor.t(getActionButtonText()),
        I: orderDetail.value.status === "assigned" ? 1 : "",
        J: orderDetail.value.status === "in_progress" ? 1 : "",
        K: orderDetail.value.status === "completed" && (!feedbackInfo.value || feedbackInfo.value.feedback_state !== "completed") ? 1 : "",
        L: common_vendor.o(handleOrderAction),
        M: loading.value
      } : {}, {
        N: common_vendor.o(closeInventoryPopup),
        O: common_vendor.p({
          type: "close",
          size: "20",
          color: "#999"
        }),
        P: common_vendor.p({
          type: "search",
          size: "18",
          color: "#999"
        }),
        Q: searchKeyword.value,
        R: common_vendor.o(($event) => searchKeyword.value = $event.detail.value),
        S: searchKeyword.value
      }, searchKeyword.value ? {
        T: common_vendor.o(($event) => searchKeyword.value = ""),
        U: common_vendor.p({
          type: "clear",
          size: "18",
          color: "#999"
        })
      } : {}, {
        V: inventoryCategories.value.length > 1
      }, inventoryCategories.value.length > 1 ? {
        W: common_vendor.f(inventoryCategories.value, (category, idx, i0) => {
          return {
            a: common_vendor.t(category),
            b: idx,
            c: currentCategory.value === category ? 1 : "",
            d: common_vendor.o(($event) => currentCategory.value = category, idx)
          };
        })
      } : {}, {
        X: filteredInventoryList.value.length === 0
      }, filteredInventoryList.value.length === 0 ? {
        Y: common_vendor.t(searchKeyword.value ? "未找到相关物料" : "暂无可用备料")
      } : common_vendor.e({
        Z: selectedItems.value.length > 0
      }, selectedItems.value.length > 0 ? {
        aa: common_vendor.t(selectedItems.value.length),
        ab: common_vendor.f(selectedItems.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.itemName),
            b: common_vendor.t(item.useQuantity),
            c: item.itemId
          };
        })
      } : {}, {
        ac: common_vendor.f(filteredInventoryList.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.itemName),
            b: common_vendor.t(item.quantity),
            c: common_vendor.o(($event) => decreaseQuantity(getOriginalIndex(item)), item.itemId),
            d: common_vendor.o([($event) => item.useQuantity = $event.detail.value, item.itemId, ($event) => validateQuantity(getOriginalIndex(item)), item.itemId], item.itemId),
            e: item.useQuantity,
            f: common_vendor.o(($event) => increaseQuantity(getOriginalIndex(item)), item.itemId),
            g: item.itemId,
            h: item.useQuantity > 0 ? 1 : ""
          };
        })
      }), {
        ad: selectedItems.value.length > 0
      }, selectedItems.value.length > 0 ? {
        ae: common_vendor.t(selectedItems.value.length)
      } : {}, {
        af: common_vendor.o(closeInventoryPopup),
        ag: common_vendor.o(confirmUseInventory),
        ah: common_vendor.sr(inventoryPopup, "7dfc586b-1", {
          "k": "inventoryPopup"
        }),
        ai: common_vendor.p({
          type: "center"
        }),
        aj: common_vendor.o(closeFeedbackPopup),
        ak: common_vendor.p({
          type: "close",
          size: "20",
          color: "#999"
        }),
        al: feedbackForm.value.feedback_content,
        am: common_vendor.o(($event) => feedbackForm.value.feedback_content = $event.detail.value),
        an: common_vendor.t(feedbackForm.value.feedback_content.length),
        ao: common_vendor.o(closeFeedbackPopup),
        ap: common_vendor.o(submitFeedback),
        aq: common_vendor.sr(feedbackPopup, "7dfc586b-5", {
          "k": "feedbackPopup"
        }),
        ar: common_vendor.p({
          type: "center"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-7dfc586b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/employee/order-detail.js.map
