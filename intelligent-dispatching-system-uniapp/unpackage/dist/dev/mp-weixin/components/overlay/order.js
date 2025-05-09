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
  __name: "order",
  emits: ["close", "success"],
  setup(__props, { emit: __emit }) {
    const emit = __emit;
    const repairTypes = [
      { id: 1, icon: "settings", name: "报修", page: "/pages/user/repair" },
      { id: 2, icon: "staff", name: "投诉", page: "/pages/user/complaint" }
    ];
    const selectedType = common_vendor.ref(null);
    const selectType = (type) => {
      selectedType.value = type;
    };
    const closeOverlay = () => {
      emit("close");
    };
    const goToPage = () => {
      if (!selectedType.value) {
        common_vendor.index.showToast({
          title: "请选择类型",
          icon: "none"
        });
        return;
      }
      emit("close");
      common_vendor.index.navigateTo({
        url: selectedType.value.page
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(closeOverlay),
        b: common_vendor.p({
          type: "close",
          size: "20",
          color: "#999"
        }),
        c: common_vendor.f(repairTypes, (type, k0, i0) => {
          return {
            a: "c2141c22-1-" + i0,
            b: common_vendor.p({
              type: type.icon,
              size: "30",
              color: "#007aff"
            }),
            c: common_vendor.t(type.name),
            d: type.id,
            e: selectedType.value && selectedType.value.id === type.id ? 1 : "",
            f: common_vendor.o(($event) => selectType(type), type.id)
          };
        }),
        d: common_vendor.o(goToPage)
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c2141c22"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/components/overlay/order.js.map
