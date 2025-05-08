"use strict";
const common_vendor = require("../../common/vendor.js");
const api_userAPI = require("../../api/userAPI.js");
const api_orderAPI = require("../../api/orderAPI.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = {
  __name: "repair",
  setup(__props) {
    const repairTypes = common_vendor.ref([]);
    const repairTypeNames = common_vendor.computed(() => {
      return repairTypes.value.map((item) => item.name);
    });
    const typeIndex = common_vendor.ref(0);
    const selectedTypeId = common_vendor.ref(null);
    const selectedTypeName = common_vendor.ref("");
    const description = common_vendor.ref("");
    const location = common_vendor.ref("");
    const latitude = common_vendor.ref("");
    const longitude = common_vendor.ref("");
    const deadline = common_vendor.ref("");
    const markers = common_vendor.computed(() => {
      if (!latitude.value || !longitude.value)
        return [];
      return [{
        id: 1,
        latitude: latitude.value,
        longitude: longitude.value,
        title: location.value,
        width: 30,
        height: 30,
        anchor: {
          x: 0.5,
          y: 0.5
        }
      }];
    });
    const minDate = common_vendor.computed(() => {
      const today = /* @__PURE__ */ new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, "0");
      const day = String(today.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    });
    const handleTypeChange = (e) => {
      const index = e.detail.value;
      typeIndex.value = index;
      selectedTypeId.value = repairTypes.value[index].id;
      selectedTypeName.value = repairTypes.value[index].name;
    };
    const handleDateChange = (e) => {
      deadline.value = e.detail.value;
    };
    const chooseLocation = () => {
      common_vendor.index.getSetting({
        success: (res) => {
          if (!res.authSetting["scope.userLocation"]) {
            common_vendor.index.authorize({
              scope: "scope.userLocation",
              success: () => {
                openLocationChooser();
              },
              fail: () => {
                common_vendor.index.showModal({
                  title: "提示",
                  content: "需要获取您的地理位置，请在设置中打开位置权限",
                  confirmText: "去设置",
                  cancelText: "取消",
                  success: (result) => {
                    if (result.confirm) {
                      common_vendor.index.openSetting();
                    }
                  }
                });
              }
            });
          } else {
            openLocationChooser();
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/user/repair.vue:175", "获取设置失败", err);
          common_vendor.index.showToast({
            title: "获取设置失败",
            icon: "none"
          });
        }
      });
    };
    const openLocationChooser = () => {
      common_vendor.index.chooseLocation({
        success: (res) => {
          location.value = res.name || res.address;
          latitude.value = res.latitude;
          longitude.value = res.longitude;
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/user/repair.vue:193", "选择位置失败", err);
          if (err.errMsg.indexOf("cancel") === -1) {
            common_vendor.index.showToast({
              title: "选择位置失败，请检查网络或权限设置",
              icon: "none"
            });
          }
        }
      });
    };
    const submitRepair = () => {
      if (!selectedTypeId.value) {
        return common_vendor.index.showToast({
          title: "请选择报修类型",
          icon: "none"
        });
      }
      if (!description.value.trim()) {
        return common_vendor.index.showToast({
          title: "请填写问题描述",
          icon: "none"
        });
      }
      if (!location.value) {
        return common_vendor.index.showToast({
          title: "请选择报修位置",
          icon: "none"
        });
      }
      const userInfoStr = common_vendor.index.getStorageSync("userInfo");
      if (!userInfoStr) {
        common_vendor.index.showToast({ title: "用户未登录", icon: "none" });
        return;
      }
      const userInfo = JSON.parse(userInfoStr);
      common_vendor.index.__f__("log", "at pages/user/repair.vue:234", userInfoStr);
      const orderData = {
        userId: userInfo.userId,
        orderType: "nrepair",
        repairType: selectedTypeId.value,
        description: description.value,
        location: location.value,
        locationLatitude: latitude.value,
        locationLongitude: longitude.value,
        deadline: deadline.value ? deadline.value + "T00:00:00" : null
      };
      common_vendor.index.__f__("log", "at pages/user/repair.vue:246", orderData);
      common_vendor.index.showLoading({
        title: "提交中..."
      });
      api_orderAPI.createOrder(orderData).then((res) => {
        common_vendor.index.__f__("log", "at pages/user/repair.vue:267", res);
        common_vendor.index.hideLoading();
        if (res.status === 200) {
          common_vendor.index.showToast({
            title: "报修提交成功",
            icon: "success"
          });
          common_vendor.index.$emit("order-updated");
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: res.message || "提交失败",
            icon: "none"
          });
        }
      }).catch((err) => {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      });
    };
    const getRepairType = async () => {
      try {
        const res = await api_userAPI.getRepairT();
        if (res.status === 200) {
          repairTypes.value = res.data.map((type) => ({
            id: type.reparTypeId,
            name: type.reparTypeName
          }));
          if (repairTypes.value.length > 0) {
            selectedTypeId.value = repairTypes.value[0].id;
            selectedTypeName.value = repairTypes.value[0].name;
          }
        } else {
          common_vendor.index.showToast({
            title: res.message || "获取报修类型失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/repair.vue:315", "获取报修类型失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      }
    };
    common_vendor.onMounted(() => {
      getRepairType();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(selectedTypeName.value || "请选择报修类型"),
        b: common_vendor.p({
          type: "right",
          size: "16",
          color: "#ccc"
        }),
        c: common_vendor.o(handleTypeChange),
        d: typeIndex.value,
        e: repairTypeNames.value,
        f: description.value,
        g: common_vendor.o(($event) => description.value = $event.detail.value),
        h: common_vendor.t(description.value.length),
        i: location.value
      }, location.value ? {
        j: common_vendor.t(location.value)
      } : {}, {
        k: common_vendor.p({
          type: "location",
          size: "20",
          color: "#007aff"
        }),
        l: common_vendor.o(chooseLocation),
        m: latitude.value && longitude.value
      }, latitude.value && longitude.value ? {
        n: latitude.value,
        o: longitude.value,
        p: markers.value
      } : {}, {
        q: common_vendor.t(deadline.value || "请选择日期"),
        r: common_vendor.p({
          type: "calendar",
          size: "16",
          color: "#ccc"
        }),
        s: deadline.value,
        t: minDate.value,
        v: common_vendor.o(handleDateChange),
        w: common_vendor.o(submitRepair)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f7a17f08"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/repair.js.map
