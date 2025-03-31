<script setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/store';
const userStore = useUserStore();
const userName = ref('');
const role = computed(() => {
  switch (userStore.user.role) {
    case "admin":
      return '管理员';
    case "employee":
      return '员工';
    case "costumer":
      return '用户';
    default:
      return '';
  }
});
const getNameFromUser = () => {
  userName.value = userStore.user.username || '';
};
onMounted(() => {
  getNameFromUser();
});
</script>

<template>
  <div>
    <h1>欢迎{{ role }}{{ userName }}进入电信运营商智能化派工系统</h1>
  </div>
</template>

<style scoped>

</style>