import { defineStore } from 'pinia'
import { store } from '../index'
import { UserType } from '@/api/login/types'
import { ElMessageBox } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { loginOutApi } from '@/api/login'
import { useTagsViewStore } from './tagsView'
import router from '@/router'

interface UserState {
  userId: number
  userInfo?: UserType
  tokenKey: string
  token: string
  refreshToken: string
  roleRouters?: string[] | AppCustomRouteRecordRaw[]
  rememberMe: boolean
  loginInfo?: string
}

export const useUserStore = defineStore('user', {
  state: (): UserState => {
    return {
      userId: 0,
      userInfo: undefined,
      tokenKey: 'Authorization',
      token: '',
      refreshToken: '',
      roleRouters: undefined,
      // 记住我
      rememberMe: true,
      loginInfo: undefined
    }
  },
  getters: {
    getTokenKey(): string {
      return this.tokenKey
    },
    getToken(): string {
      return this.token
    },
    getRefreshToken(): string {
      return this.refreshToken
    },
    getUserId(): number {
      return this.userId
    },
    getUserInfo(): UserType | undefined {
      return this.userInfo
    },
    getRoleRouters(): string[] | AppCustomRouteRecordRaw[] | undefined {
      return this.roleRouters
    },
    getRememberMe(): boolean {
      return this.rememberMe
    },
    getLoginInfo(): string | undefined {
      return this.loginInfo
    }
  },
  actions: {
    setTokenKey(tokenKey: string) {
      this.tokenKey = tokenKey
    },
    setUserId(userId: number) {
      this.userId = userId
    },
    setToken(token: string) {
      this.token = token
    },
    setRefreshToken(refreshToken: string) {
      this.refreshToken = refreshToken
    },
    setUserInfo(userInfo?: UserType) {
      this.userInfo = userInfo
    },
    setRoleRouters(roleRouters: string[] | AppCustomRouteRecordRaw[]) {
      this.roleRouters = roleRouters
    },
    logoutConfirm() {
      const { t } = useI18n()
      ElMessageBox.confirm(t('common.loginOutMessage'), t('common.reminder'), {
        confirmButtonText: t('common.ok'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
        .then(async () => {
          const res = await loginOutApi(this.userId).catch(() => {})
          if (res) {
            this.reset()
          }
        })
        .catch(() => {})
    },
    reset() {
      const tagsViewStore = useTagsViewStore()
      tagsViewStore.delAllViews()
      this.setUserId(0)
      this.setToken('')
      this.setRefreshToken('')
      this.setUserInfo(undefined)
      this.setRoleRouters([])
      router.replace('/login')
    },
    logout() {
      this.reset()
    },
    setRememberMe(rememberMe: boolean) {
      this.rememberMe = rememberMe
    },
    setLoginInfo(loginInfo: string | undefined) {
      this.loginInfo = loginInfo
    }
  },
  persist: true
})

export const useUserStoreWithOut = () => {
  return useUserStore(store)
}
