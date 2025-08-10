import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'login',
            component: () => import('@/components/Login/LoginView.vue')
        },
        {
            path: '/main',
            name: 'main',
            component: () => import('@/views/MainView.vue'),
            meta: { requiresAuth: true }
        }
    ]
})

router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        next('/')
    } else if (to.path === '/' && userStore.isLoggedIn) {
        next('/main')
    } else {
        next()
    }
})

export default router