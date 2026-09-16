import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'
import { Layout } from '@/utils/routerHelper'
import { NO_RESET_WHITE_LIST } from '@/constants'

export const constantRouterMap: AppRouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    name: 'Root',
    meta: {
      hidden: true
    }
  },
  {
    path: '/redirect',
    component: Layout,
    name: 'RedirectWrap',
    children: [
      {
        path: '/redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/Redirect/Redirect.vue'),
        meta: {}
      }
    ],
    meta: {
      hidden: true,
      noTagsView: true
    }
  },
  {
    path: '/login',
    component: () => import('@/views/Login/Login.vue'),
    name: 'Login',
    meta: {
      hidden: true,
      title: 'router.login',
      noTagsView: true
    }
  },
  {
    path: '/personal',
    component: Layout,
    redirect: '/personal/personal-center',
    name: 'Personal',
    meta: {
      title: 'router.personal',
      hidden: true,
      canTo: true
    },
    children: [
      {
        path: 'personal-center',
        component: () => import('@/views/Personal/PersonalCenter/PersonalCenter.vue'),
        name: 'PersonalCenter',
        meta: {
          title: 'router.personalCenter',
          hidden: true,
          canTo: true
        }
      }
    ]
  },
  {
    path: '/404',
    component: () => import('@/views/Error/404.vue'),
    name: 'NoFind',
    meta: {
      hidden: true,
      title: '404',
      noTagsView: true
    }
  }
]

export const asyncRouterMap: AppRouteRecordRaw[] = [
  {
    path: '/home',
    component: Layout,
    redirect: '/home/index',
    name: 'Home',
    meta: {
      title: '首页',
      icon: 'vi-ant-design:home-filled',
      affix: true
    },
    children: [
      {
        path: 'index',
        component: () => import('@/views/Home/Home.vue'),
        name: 'HomeIndex',
        meta: {
          title: '首页',
          noCache: true,
          affix: true
        }
      }
    ]
  },
  {
    path: '/pay',
    component: Layout,
    redirect: '/pay/order',
    name: 'PayManage',
    meta: {
      title: '支付管理',
      icon: 'vi-ep:money'
    },
    children: [
      {
        path: 'order',
        component: () => import('@/views/pay/order/index.vue'),
        name: 'PayOrderList',
        meta: {
          title: '支付订单',
          noCache: true
        }
      },
      {
        path: 'refund',
        component: () => import('@/views/pay/refund/index.vue'),
        name: 'PayRefundList',
        meta: {
          title: '退款订单',
          noCache: true
        }
      },
      {
        // 详情页：从列表行进入，不进菜单
        path: 'refund/:refundNo',
        component: () => import('@/views/pay/refund/detail.vue'),
        name: 'PayRefundDetail',
        meta: {
          title: '退款订单详情',
          hidden: true
        }
      },
      {
        // 详情页：从列表行进入，不进菜单
        path: 'order/:paymentNo',
        component: () => import('@/views/pay/order/detail.vue'),
        name: 'PayOrderDetail',
        meta: {
          title: '支付订单详情',
          hidden: true
        }
      }
    ]
  },
  {
    path: '/account',
    component: Layout,
    redirect: '/account/user',
    name: 'AccountManage',
    meta: {
      title: '资金管理',
      icon: 'vi-ep:wallet'
    },
    children: [
      {
        path: 'user',
        component: () => import('@/views/pay/account/user.vue'),
        name: 'UserAccountList',
        meta: {
          title: '用户账户',
          noCache: true
        }
      },
      {
        path: 'merchant',
        component: () => import('@/views/pay/account/merchant.vue'),
        name: 'MerchantAccountList',
        meta: {
          title: '商户账户',
          noCache: true
        }
      },
      {
        path: 'flow',
        component: () => import('@/views/pay/account/flow.vue'),
        name: 'AccountFlowList',
        meta: {
          title: '资金流水',
          noCache: true
        }
      },
      {
        path: 'report',
        component: () => import('@/views/pay/account/report.vue'),
        name: 'DailySummaryReport',
        meta: {
          title: '日汇总报表',
          noCache: true
        }
      }
    ]
  },
  {
    path: '/merchant',
    component: Layout,
    redirect: '/merchant/list',
    name: 'MerchantManage',
    meta: {
      title: '商户管理',
      icon: 'vi-ep:office-building'
    },
    children: [
      {
        path: 'list',
        component: () => import('@/views/pay/merchant/list.vue'),
        name: 'MerchantList',
        meta: {
          title: '商户列表',
          noCache: true
        }
      },
      {
        path: 'create',
        component: () => import('@/views/pay/merchant/create.vue'),
        name: 'MerchantCreate',
        meta: {
          title: '新建商户',
          noCache: true
        }
      },
      {
        path: 'audit',
        component: () => import('@/views/pay/merchant/audit.vue'),
        name: 'MerchantAudit',
        meta: {
          title: '商户审核',
          noCache: true
        }
      },
      {
        // 详情页：从列表行进入，不进菜单
        path: 'detail/:merchantNo',
        component: () => import('@/views/pay/merchant/detail.vue'),
        name: 'MerchantDetail',
        meta: {
          title: '商户详情',
          hidden: true
        }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/api-log',
    name: 'SystemManage',
    meta: {
      title: '系统管理',
      icon: 'vi-ep:setting'
    },
    children: [
      {
        path: 'api-log',
        component: () => import('@/views/pay/system/api-log.vue'),
        name: 'ApiLogList',
        meta: {
          title: '接口日志',
          noCache: true
        }
      },
      {
        path: 'notify',
        component: () => import('@/views/pay/system/notify.vue'),
        name: 'NotifyList',
        meta: {
          title: '回调通知管理',
          noCache: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  strict: true,
  routes: constantRouterMap as RouteRecordRaw[],
  scrollBehavior: () => ({ left: 0, top: 0 })
})

export const resetRouter = (): void => {
  router.getRoutes().forEach((route) => {
    const { name } = route
    if (name && !NO_RESET_WHITE_LIST.includes(name as string)) {
      router.hasRoute(name) && router.removeRoute(name)
    }
  })
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
