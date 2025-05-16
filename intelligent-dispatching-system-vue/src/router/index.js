import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/store'
const routes = [
    {
        path: '/',
        component: () => import('@/views/baseIfoView/ManageView.vue'),
        redirect: '/home',
         children: [
            { path: 'home', name: '首页', component: () => import('@/views/baseIfoView/UserHome.vue'), meta: { requireAuth: true } },
            { path: 'userManage', name: '用户管理', component: () => import('../views/adminView/UserManage.vue'), meta: { requireAuth: true } },
            { path: 'employeeManage', name: '员工信息管理', component: () => import('../views/adminView/EmployeeManage.vue'), meta: { requireAuth: true } },
            { path: 'orderManage', name: '报修处理', component: () => import('../views/adminView/OrderManage.vue'), meta: { requireAuth: true } },
            { path: 'inventoryManage', name: '物料管理', component: () => import('../views/adminView/InventoryManage.vue'), meta: { requireAuth: true } },
            { path: 'feedbackManage', name: '回访管理', component: () => import('../views/adminView/FeedbackManage.vue'), meta: { requireAuth: true } },
            { path: 'maintenanceManage', name: '维护管理', component: () => import('../views/adminView/MaintenanceManage.vue'), meta: { requireAuth: true } },
        
         ],
        meta: { requireAuth: true }
    },
    {
        path: '/login',
        component: () => import('@/views/loginView/UserLogin.vue'),
        children: [
            {
                path: '',
                name: '登录',
                component: () => import('@/views/loginView/LoginForm.vue'),
            },
        ]
    },
];
const router = createRouter({
    history: createWebHistory(),
    routes
});
// 全局前置守卫
// 路由守卫
router.beforeEach((to, from, next) => {
    const store = useUserStore(); // 获取 Pinia store 实例
    localStorage.setItem("currentPathName", to.name)  // 设置当前的路由名称，为了在Header组件中去使用
    store.setPathName(to.name)  // 触发store的数据更新
    next()  // 放行路由
})

export default router;
