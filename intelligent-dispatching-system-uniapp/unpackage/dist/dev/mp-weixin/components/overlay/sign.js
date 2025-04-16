"use strict";
const common_vendor = require("../../common/vendor.js");
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
    const loading = common_vendor.ref(false);
    const location = common_vendor.ref("正在获取位置...");
    const currentTime = common_vendor.ref("");
    const updateCurrentTime = () => {
      const now = /* @__PURE__ */ new Date();
      const hours = now.getHours().toString().padStart(2, "0");
      const minutes = now.getMinutes().toString().padStart(2, "0");
      const seconds = now.getSeconds().toString().padStart(2, "0");
      currentTime.value = `${hours}:${minutes}:${seconds}`;
    };
    const getLocation = () => {
      common_vendor.index.getLocation({
        type: "gcj02",
        success: (res) => {
          common_vendor.index.request({
            url: `https://apis.map.qq.com/ws/geocoder/v1/?location=${res.latitude},${res.longitude}&key=YOUR_KEY`,
            success: (addressRes) => {
              if (addressRes.data && addressRes.data.result) {
                location.value = addressRes.data.result.address;
              } else {
                location.value = `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
              }
            },
            fail: () => {
              location.value = `位置: ${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`;
            }
          });
        },
        fail: () => {
          location.value = "无法获取位置信息";
        }
      });
    };
    const handleSign = () => {
      loading.value = true;
      setTimeout(() => {
        loading.value = false;
        common_vendor.index.showToast({
          title: "签到成功",
          icon: "success"
        });
        emit("success");
        setTimeout(() => {
          emit("close");
        }, 1500);
      }, 1e3);
    };
    const closeOverlay = () => {
      emit("close");
    };
    common_vendor.onMounted(() => {
      updateCurrentTime();
      setInterval(updateCurrentTime, 1e3);
      getLocation();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(closeOverlay),
        b: common_vendor.p({
          type: "close",
          size: "20",
          color: "#999"
        }),
        c: common_vendor.t(currentTime.value),
        d: common_vendor.t(location.value),
        e: loading.value,
        f: common_vendor.o(handleSign)
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-18eaaea1"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/components/overlay/sign.js.map
