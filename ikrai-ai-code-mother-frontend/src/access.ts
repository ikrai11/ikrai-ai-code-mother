import { useLoginUserStore } from '@/stores/loginUser.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

// 是否为首次获取登录用户
let firstFetchLoginUser = true

/**
 * 全局权限校验
 */
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  // 确保页面刷新，首次加载时，能够等后端返回用户信息后再校验权限
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  const toUrl = to.fullPath
  if (toUrl.startsWith('/admin')) {
    if (toUrl === '/admin/appManage') {
      // 应用管理：只需登录
      if (!loginUser || !loginUser.id) {
        message.error('请先登录')
        next(`/user/login?redirect=${to.fullPath}`)
        return
      }
    } else {
      // 其他 admin 路由：需要 admin 角色
      if (!loginUser || loginUser.userRole !== 'admin') {
        message.error('没有权限')
        next(`/user/login?redirect=${to.fullPath}`)
        return
      }
    }
  }
  next()
})
