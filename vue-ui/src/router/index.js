import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: '/mass-message',
        name: 'MassMessage',
        component: () => import('@/components/PlaceholderView.vue'),
        meta: {
          title: '消息群发任务',
          icon: 'Promotion',
          description: '批量向多个微信好友或标签客户发送消息的任务管理，支持定时发送、消息模板与变量替换等能力。',
          gradient: 'linear-gradient(135deg, #00a8ff, #0066ff)'
        }
      },
      {
        path: '/group-message',
        name: 'GroupMessage',
        component: () => import('@/components/PlaceholderView.vue'),
        meta: {
          title: '群消息任务',
          icon: 'ChatLineSquare',
          description: '针对微信群聊的消息群发与任务调度，可配置目标群聊、发送内容与执行时间等。',
          gradient: 'linear-gradient(135deg, #a855f7, #ec4899)'
        }
      },
      {
        path: '/moments',
        name: 'Moments',
        component: () => import('@/components/PlaceholderView.vue'),
        meta: {
          title: '朋友圈任务',
          icon: 'Camera',
          description: '自动化或半自动化发布微信朋友圈内容，支持图文、视频及定时发布策略。',
          gradient: 'linear-gradient(135deg, #00ffcc, #00d4ff)'
        }
      },
      {
        path: '/ai-reply',
        name: 'AiReply',
        component: () => import('@/components/PlaceholderView.vue'),
        meta: {
          title: 'AI聊消息回复',
          icon: 'Cpu',
          description: '基于 AI 大模型自动回复微信消息，可配置话术、知识库与自动应答策略。',
          gradient: 'linear-gradient(135deg, #f59e0b, #ef4444)'
        }
      },
      {
        path: '/new-customer',
        name: 'NewCustomer',
        component: () => import('@/components/PlaceholderView.vue'),
        meta: {
          title: '新客户应答',
          icon: 'UserFilled',
          description: '新添加微信好友时的自动欢迎语、标签备注与后续跟进流程配置。',
          gradient: 'linear-gradient(135deg, #10b981, #06b6d4)'
        }
      },
      {
        path: '/customer-analysis',
        name: 'CustomerAnalysis',
        component: () => import('@/components/PlaceholderView.vue'),
        meta: {
          title: '客户分析跟踪',
          icon: 'TrendCharts',
          description: '对客户聊天记录、行为数据进行统计分析与跟进提醒，辅助转化运营。',
          gradient: 'linear-gradient(135deg, #8b5cf6, #d946ef)'
        }
      },
      {
        path: '/user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
      },
      {
        path: '/wx',
        name: 'Wx',
        component: () => import('@/views/wx/index.vue'),
        meta: { title: '微信管理', icon: 'ChatDotRound' }
      },
      {
        path: '/chat',
        name: 'Chat',
        component: () => import('@/views/chat/index.vue'),
        meta: { title: '聚合聊天', icon: 'ChatLineRound' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由前置守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }
  // 未登录跳转到登录页
  if (!userStore.token) {
    next('/login')
    return
  }
  next()
})

export default router
