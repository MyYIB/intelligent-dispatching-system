import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import './assets/global.css';
import request from '@/utils/request';
import { createPinia } from 'pinia';
import router from './router';
const app = createApp(App);
const pinia = createPinia();
app.use(ElementPlus, { size: 'small' });
app.use(pinia)
app.use(router);
// 全局挂载 request 方法
app.config.globalProperties.$request = request;

import {useUserStore} from "@/store";

//钩子函数，访问路由前调用
router.beforeEach((to, from, next) => {
    const store = useUserStore()
    console.log(store)
    //路由需要认证
        if (to.meta.requireAuth) {
            //判断store里是否有token
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
    }
)
app.mount('#app')
