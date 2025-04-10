import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import './assets/global.css';
import request from '@/utils/request';
import { createPinia } from 'pinia';
import router from './router';
import AMapLoader from '@amap/amap-jsapi-loader';
import {useUserStore} from "@/store"; 
// 高德地图安全配置
window._AMapSecurityConfig = {
  securityJsCode: '222a71664e09faba3c60b0180b4d752c',
}

// 先加载高德地图，然后初始化应用
AMapLoader.load({
  key: 'e429535a4e35e969ff396bed5116d30e',
  version: '2.0',
}).then(() => {
  const app = createApp(App);
  const pinia = createPinia();
  
  app.use(ElementPlus, { size: 'small' });
  app.use(pinia);
  app.use(router);
  app.config.globalProperties.$request = request;

  // 路由守卫
  router.beforeEach((to, from, next) => {
    const store = useUserStore()
    if (to.meta.requireAuth) {
      if (store.token) {
        next()
      } else {
        next({
          path: 'login',
          query: { redirect: to.fullPath }
        })
      }
    } else {
      next()
    }
  });

  app.mount('#app');
}).catch(e => {
  console.error('高德地图加载失败：', e);
});
