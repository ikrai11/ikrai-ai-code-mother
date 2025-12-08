<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">真嗣应用生成</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
const loginUserStore = useLoginUserStore()
import { LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'
import checkAccess from '@/access/checkAccess'
import ACCESS_ENUM from '@/access/accessEnum'

const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    label: '主页',
    title: '主页',
    meta: {
      access: ACCESS_ENUM.NOT_LOGIN
    }
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
    meta: {
      access: ACCESS_ENUM.ADMIN
    }
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/ikrai-ai/ikrai-ai-code-mother', target: '_blank' }, '编程导航'),
    title: '编程导航',
    meta: {
      access: ACCESS_ENUM.NOT_LOGIN
    }
  },
]

// 将菜单转换为路由项
const menuToRouteItem = (menu: any) => {
  return {
    ...menu,
    // 如果没有meta属性，添加默认值
    meta: menu.meta || {}
  };
};

// 过滤菜单项并使其响应式
const menuItems = computed<MenuProps['items']>(() => {
  return originItems.filter((menu) => {
    // 将菜单转换为路由项
    const item = menuToRouteItem(menu);
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单，有权限则返回 true，则保留该菜单
    return checkAccess(loginUserStore.loginUser, item.meta?.access as string);
  });
})

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error(res.data.message || '退出登录失败')
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 48px;
  width: 48px;
}

.site-title {
  margin: 0;
  font-size: 18px;
  color: #1890ff;
}

.ant-menu-horizontal {
  border-bottom: none !important;
}

.user-login-status {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
