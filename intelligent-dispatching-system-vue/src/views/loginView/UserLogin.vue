<script setup>

</script>

<template>
  <div class="login-page">
    <div class="login-box">
        <router-view/>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  width: 100%;
  height: 100vh;
  background: #5dd5c8 url(https://spring-oss.oss-cn-beijing.aliyuncs.com/images/img/bgimg.png) center bottom no-repeat;
}

.login-page .login-box {
  margin: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  width: 90%;
  max-width: 500px;
  height: auto;
  padding: 40px;
  background: #fff;
  box-shadow: 0 6px 13px 0 rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
/* 小屏幕适配（如手机） */
@media (max-width: 480px) {
  .login-page .login-box {
    padding: 20px;     /* 减小内边距 */
  }
}
</style>