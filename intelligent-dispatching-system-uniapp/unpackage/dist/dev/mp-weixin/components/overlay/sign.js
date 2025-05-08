"use strict";
const common_vendor = require("../../common/vendor.js");
const api_employeeAPI = require("../../api/employeeAPI.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = {
  __name: "sign",
  emits: ["close", "success"],
  setup(__props, { emit: __emit }) {
    const emit = __emit;
    const employeeInfo = common_vendor.ref(null);
    const loading = common_vendor.ref(false);
    const location = common_vendor.ref("正在获取位置...");
    const currentTime = common_vendor.ref("");
    const latitude = common_vendor.ref(null);
    const longitude = common_vendor.ref(null);
    const isSignIn = common_vendor.computed(() => {
      var _a, _b;
      return ((_a = employeeInfo.value) == null ? void 0 : _a.status) === "off" || !((_b = employeeInfo.value) == null ? void 0 : _b.status);
    });
    const buttonText = common_vendor.computed(() => isSignIn.value ? "上班签到" : "下班签退");
    const targetStatus = common_vendor.computed(() => isSignIn.value ? "available" : "off");
    const updateCurrentTime = () => {
      const now = /* @__PURE__ */ new Date();
      const hours = now.getHours().toString().padStart(2, "0");
      const minutes = now.getMinutes().toString().padStart(2, "0");
      const seconds = now.getSeconds().toString().padStart(2, "0");
      currentTime.value = `${hours}:${minutes}:${seconds}`;
    };
    const getEmployeeInfo = () => {
      const employeeInfoStr = common_vendor.index.getStorageSync("employeeInfo");
      if (employeeInfoStr) {
        employeeInfo.value = JSON.parse(employeeInfoStr);
      }
    };
    const getLocation = () => {
      common_vendor.index.getSetting({
        success: (res) => {
          if (!res.authSetting["scope.userLocation"]) {
            common_vendor.index.authorize({
              scope: "scope.userLocation",
              success: () => {
                getLocationInfo();
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
                    } else {
                      location.value = "未授权获取位置";
                    }
                  }
                });
              }
            });
          } else {
            getLocationInfo();
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at components/overlay/sign.vue:74", "获取设置失败", err);
          location.value = "获取设置失败";
          common_vendor.index.showToast({
            title: "获取设置失败",
            icon: "none"
          });
        }
      });
    };
    const getLocationInfo = () => {
      common_vendor.index.showLoading({
        title: "获取位置中..."
      });
      common_vendor.index.getLocation({
        type: "gcj02",
        success: (res) => {
          common_vendor.index.hideLoading();
          latitude.value = res.latitude;
          longitude.value = res.longitude;
          common_vendor.index.request({
            url: `https://apis.map.qq.com/ws/geocoder/v1/?location=${res.latitude},${res.longitude}&key=727BZ-3JF6L-HSHPO-MY4HW-SNDIK-DZBQ7`,
            success: (addressRes) => {
              common_vendor.index.__f__("log", "at components/overlay/sign.vue:101", "地址解析结果:", addressRes);
              if (addressRes.data && addressRes.data.status === 0 && addressRes.data.result) {
                const result = addressRes.data.result;
                const addressComponent = result.address_component;
                const formatted_addresses = result.formatted_addresses;
                if (formatted_addresses && formatted_addresses.recommend) {
                  location.value = formatted_addresses.recommend;
                } else {
                  let detailAddress = "";
                  if (addressComponent) {
                    detailAddress = [
                      addressComponent.province,
                      addressComponent.city,
                      addressComponent.district,
                      addressComponent.street,
                      addressComponent.street_number
                    ].filter(Boolean).join("");
                  }
                  location.value = detailAddress || result.address || `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
                }
              } else {
                location.value = `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
              }
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at components/overlay/sign.vue:131", "地址解析失败:", err);
              location.value = `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
            }
          });
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at components/overlay/sign.vue:138", "获取位置失败", err);
          location.value = "无法获取位置信息";
          common_vendor.index.showToast({
            title: "获取位置失败，请检查GPS是否开启",
            icon: "none"
          });
        }
      });
    };
    const handleSign = async () => {
      if (!latitude.value || !longitude.value) {
        common_vendor.index.showToast({
          title: "位置信息获取失败，请重试",
          icon: "none"
        });
        getLocation();
        return;
      }
      if (!employeeInfo.value || !employeeInfo.value.employeeId) {
        common_vendor.index.__f__("error", "at components/overlay/sign.vue:163", "员工信息不完整:", employeeInfo.value);
        common_vendor.index.showToast({
          title: "员工信息不完整，请重新登录",
          icon: "none"
        });
        return;
      }
      loading.value = true;
      try {
        const updateData = {
          employeeId: employeeInfo.value.employeeId,
          location: location.value,
          locationLatitude: Number(latitude.value),
          locationLongitude: Number(longitude.value),
          status: targetStatus.value,
          currentWorkload: 0
        };
        common_vendor.index.__f__("log", "at components/overlay/sign.vue:185", "发送签到/签退请求数据:", updateData);
        const res = await api_employeeAPI.updateEmployeeLocation(updateData);
        common_vendor.index.__f__("log", "at components/overlay/sign.vue:188", "签到/签退响应:", res);
        if (res.status === 200) {
          const employeeInfoStr = common_vendor.index.getStorageSync("employeeInfo");
          if (employeeInfoStr) {
            const employeeInfo2 = JSON.parse(employeeInfoStr);
            employeeInfo2.status = targetStatus.value;
            employeeInfo2.location = location.value;
            employeeInfo2.locationLatitude = latitude.value;
            employeeInfo2.locationLongitude = longitude.value;
            common_vendor.index.setStorageSync("employeeInfo", JSON.stringify(employeeInfo2));
          }
          common_vendor.index.showToast({
            title: isSignIn.value ? "签到成功" : "签退成功",
            icon: "success"
          });
          emit("success", {
            ...updateData,
            isSignIn: isSignIn.value
          });
          setTimeout(() => {
            emit("close");
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: res.message || (isSignIn.value ? "签到失败" : "签退失败"),
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at components/overlay/sign.vue:224", isSignIn.value ? "签到失败" : "签退失败", error);
        common_vendor.index.showToast({
          title: "网络异常，请稍后重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const closeOverlay = () => {
      emit("close");
    };
    common_vendor.onMounted(() => {
      getEmployeeInfo();
      updateCurrentTime();
      setInterval(updateCurrentTime, 1e3);
      getLocation();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(isSignIn.value ? "员工签到" : "员工签退"),
        b: common_vendor.o(closeOverlay),
        c: common_vendor.p({
          type: "close",
          size: "20",
          color: "#999"
        }),
        d: common_vendor.t(currentTime.value),
        e: common_vendor.t(location.value),
        f: common_vendor.t(buttonText.value),
        g: common_vendor.n(isSignIn.value ? "sign-in-btn" : "sign-out-btn"),
        h: loading.value,
        i: common_vendor.o(handleSign)
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-18eaaea1"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/components/overlay/sign.js.map
