<script setup>
import { ref } from 'vue';
import ManageAside from "@/components/ManageAside.vue";
import ManageHeader from "@/components/ManageHeader.vue";
const collapseBtnClass = ref('el-icon-s-fold');
const isCollapse = ref(false);
const sideWidth = ref(200);
const logoTextShow = ref(true);
const collapse = () => {
  isCollapse.value = !isCollapse.value;
  if (isCollapse.value) {
    sideWidth.value = 64;
    collapseBtnClass.value = 'el-icon-s-unfold';
    logoTextShow.value = false;
  } else {
    sideWidth.value = 200;
    collapseBtnClass.value = 'el-icon-s-fold';
    logoTextShow.value = true;
  }
};
</script>

<template>
  <el-container style="min-height: 100vh">
    <el-aside :width="sideWidth + 'px'" style="box-shadow: 2px 0 6px rgb(0 21 41 );">
      <ManageAside :isCollapse="isCollapse" :logoTextShow="logoTextShow"/>
    </el-aside>

    <el-container>
      <el-header style="border-bottom: 1px solid #ccc;">
        <manage-header :collapseBtnClass="collapseBtnClass" :collapse="collapse"/>
      </el-header>

      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>

</style>