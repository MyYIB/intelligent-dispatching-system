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
    const employeeDetail = common_vendor.ref({});
    const loading = common_vendor.ref(false);
    const feedbackInfo = common_vendor.ref(null);
    const ratePopup = common_vendor.ref(null);
    const rateForm = common_vendor.ref({
      satisfaction_score: 5
    });
    const markers = common_vendor.computed(() => {
      if (!orderDetail.value.location_latitude || !orderDetail.value.location_longitude) {
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
    const orderId = common_vendor.ref("");
    const fetchOrderDetail = async () => {
      loading.value = true;
      try {
        const res = await api_orderAPI.getOrderDetail(orderId.value);
        if (res.status === 200) {
          orderDetail.value = res.data || {};
          if (orderDetail.value.status !== "pending") {
            const orderRes = await api_orderAPI.getOrderEmployeeDetail(orderDetail.value.orderId);
            if (orderRes.status === 200) {
              employeeDetail.value = orderRes.data || {};
              common_vendor.index.__f__("log", "at pages/user/order-detail.vue:221", employeeDetail.value);
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
        common_vendor.index.__f__("error", "at pages/user/order-detail.vue:237", "获取工单详情失败", error);
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
        common_vendor.index.__f__("error", "at pages/user/order-detail.vue:257", "获取回访信息失败", error);
        feedbackInfo.value = null;
      }
    };
    const openRatePopup = () => {
      rateForm.value = {
        satisfaction_score: 5
      };
      ratePopup.value.open();
    };
    const closeRatePopup = () => {
      ratePopup.value.close();
    };
    const submitRating = async () => {
      if (rateForm.value.satisfaction_score < 1) {
        common_vendor.index.showToast({
          title: "请至少选择1星评分",
          icon: "none"
        });
        return;
      }
      loading.value = true;
      try {
        const userInfoStr = common_vendor.index.getStorageSync("userInfo");
        if (!userInfoStr) {
          throw new Error("请先登录");
        }
        const userInfo = JSON.parse(userInfoStr);
        const ratingData = {
          feedback_id: feedbackInfo.value.feedbackId,
          order_id: parseInt(orderId.value),
          user_id: userInfo.userId,
          satisfaction_score: rateForm.value.satisfaction_score,
          rating_time: (/* @__PURE__ */ new Date()).toISOString()
        };
        const res = await api_orderAPI.submitUserRating(ratingData);
        if (res.status === 200) {
          common_vendor.index.showToast({
            title: "评价已提交",
            icon: "success"
          });
          closeRatePopup();
          await fetchFeedbackInfo();
        } else {
          throw new Error(res.msg || "提交评价失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/order-detail.vue:322", "提交评价失败", error);
        common_vendor.index.showToast({
          title: error.message || "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
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
        "completed": "state-completed",
        "unrated": "state-unrated"
      };
      return classMap[state] || "";
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
              common_vendor.index.__f__("error", "at pages/user/order-detail.vue:377", "取消工单失败", error);
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
        n: orderDetail.value.status !== "pending"
      }, orderDetail.value.status !== "pending" ? common_vendor.e({
        o: employeeDetail.value.name
      }, employeeDetail.value.name ? {
        p: common_vendor.t(employeeDetail.value.name || "暂无")
      } : {}, {
        q: employeeDetail.value.phone
      }, employeeDetail.value.phone ? {
        r: common_vendor.t(employeeDetail.value.phone)
      } : {}, {
        s: employeeDetail.value.resolved_at
      }, employeeDetail.value.resolved_at ? {
        t: common_vendor.t(formatTime(orderDetail.value.resolved_at) || "未完成")
      } : {}) : {}, {
        v: orderDetail.value.status === "completed"
      }, orderDetail.value.status === "completed" ? common_vendor.e({
        w: !feedbackInfo.value
      }, !feedbackInfo.value ? {} : common_vendor.e({
        x: common_vendor.t(getFeedbackStateText(feedbackInfo.value.feedbackState)),
        y: common_vendor.n(getFeedbackStateClass(feedbackInfo.value.feedbackState)),
        z: feedbackInfo.value.feedbackState === "completed"
      }, feedbackInfo.value.feedbackState === "completed" ? {
        A: common_vendor.p({
          value: feedbackInfo.value.satisfactionScore || 0,
          size: 18,
          readonly: true
        }),
        B: common_vendor.t(feedbackInfo.value.satisfactionScore || 0)
      } : {}, {
        C: feedbackInfo.value.needTime
      }, feedbackInfo.value.needTime ? {
        D: common_vendor.t(formatTime(feedbackInfo.value.needTime))
      } : {}, {
        E: feedbackInfo.value.feedbackTime
      }, feedbackInfo.value.feedbackTime ? {
        F: common_vendor.t(formatTime(feedbackInfo.value.feedbackTime))
      } : {})) : {}, {
        G: orderDetail.value.status === "pending" || orderDetail.value.status === "assigned" || orderDetail.value.status === "in_progress"
      }, orderDetail.value.status === "pending" || orderDetail.value.status === "assigned" || orderDetail.value.status === "in_progress" ? {
        H: common_vendor.o(cancelOrder)
      } : {}, {
        I: orderDetail.value.status === "completed" && feedbackInfo.value && feedbackInfo.value.feedbackState === "unrated"
      }, orderDetail.value.status === "completed" && feedbackInfo.value && feedbackInfo.value.feedbackState === "unrated" ? {
        J: common_vendor.o(openRatePopup)
      } : {}, {
        K: common_vendor.o(closeRatePopup),
        L: common_vendor.p({
          type: "close",
          size: "20",
          color: "#999"
        }),
        M: common_vendor.o(($event) => rateForm.value.satisfaction_score = $event),
        N: common_vendor.p({
          size: 24,
          modelValue: rateForm.value.satisfaction_score
        }),
        O: common_vendor.o(closeRatePopup),
        P: common_vendor.o(submitRating),
        Q: common_vendor.sr(ratePopup, "29badeee-1", {
          "k": "ratePopup"
        }),
        R: common_vendor.p({
          type: "center"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-29badeee"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/order-detail.js.map
