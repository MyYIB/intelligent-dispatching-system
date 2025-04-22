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
        //     { path: 'term', name: '学期信息管理', component: () => import('../views/baseIfo/Term.vue'), meta: { requireAuth: true } },
        //     { path: 'paperInfo', name: '试卷信息', component: () => import('../views/paperManage/PaperInfo.vue'), meta: { requireAuth: true } },
        //     { path: 'paperAna', name: '试卷分析', component: () => import('../views/paperManage/PaperAna.vue'), meta: { requireAuth: true } },
        //     { path: 'scoreAdd', name: '成绩录入', component: () => import('../views/scoreManage/ScoreAdd.vue'), meta: { requireAuth: true } },
        //     { path: 'scoreQuery', name: '成绩查询', component: () => import('../views/scoreManage/ScoreQuery.vue'), meta: { requireAuth: true } },
        //     { path: 'course', name: '排课', component: () => import('../views/teach/Teach.vue'), meta: { requireAuth: true } },
        //     { path: 'selectCourse', name: '选课', component: () => import('../views/selectCourse/Student.vue'), meta: { requireAuth: true } },
        //     { path: 'searchCourse', name: '个人选课', component: () => import('../views/selectCourse/StudentTwo.vue'), meta: { requireAuth: true } },
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
