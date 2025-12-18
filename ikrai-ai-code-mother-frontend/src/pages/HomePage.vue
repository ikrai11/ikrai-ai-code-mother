<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import dayjs from 'dayjs'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词
const optimizePrompt = () => {
  if (!userPrompt.value.trim()) {
    message.warning('请先输入提示词')
    return
  }
  // 这里可以调用优化接口，暂时简单处理
  message.info('优化功能开发中...')
}

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面
      await router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'priority',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看应用详情
const viewApp = (appId: number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 查看应用作品
const viewWork = (appId: number | undefined) => {
  if (appId) {
    router.push(`/app/work/${appId}`)
  }
}

// 格式化时间
const formatTime = (time: string | undefined) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD')
}

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
  <div id="homePage">
    <!-- 网站标题和描述 -->
    <div class="hero-section">
      <h1 class="hero-title">一句话 <span class="highlight">🤖</span> 呈所想</h1>
      <p class="hero-description">与 AI 对话轻松创建应用和网站</p>
    </div>

    <!-- 用户提示词输入框 -->
    <div class="input-section">
      <a-textarea
        v-model:value="userPrompt"
        placeholder="使用 NoCode 创建一个高效的小工具，帮我计算......"
        :rows="4"
        :maxlength="1000"
        show-count
        class="prompt-input"
      />
      <div class="input-actions">
        <a-button type="text" size="small" @click="optimizePrompt">
          <template #icon>
            <span>✨</span>
          </template>
          优化
        </a-button>
        <a-button type="primary" size="large" @click="createApp" :loading="creating">
          <template #icon>
            <span>↑</span>
          </template>
        </a-button>
      </div>
    </div>

    <!-- 快捷按钮 -->
    <div class="quick-actions">
      <a-button type="default" @click="setPrompt('波普风电商页面')">波普风电商页面</a-button>
      <a-button type="default" @click="setPrompt('企业网站')">企业网站</a-button>
      <a-button type="default" @click="setPrompt('电商运营后台')">电商运营后台</a-button>
      <a-button type="default" @click="setPrompt('暗黑话题社区')">暗黑话题社区</a-button>
    </div>

    <!-- 我的作品 -->
    <div class="section">
      <h2 class="section-title">我的作品</h2>
      <div class="app-grid">
        <AppCard
          v-for="app in myApps"
          :key="app.id"
          :app="app"
          @view-chat="viewApp(app.id)"
          @view-work="viewWork(app.id)"
        />
      </div>
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="myAppsPage.current"
          v-model:page-size="myAppsPage.pageSize"
          :total="myAppsPage.total"
          :show-size-changer="false"
          :show-total="(total: number) => `共 ${total} 个应用`"
          @change="loadMyApps"
        />
      </div>
    </div>

    <!-- 精选案例 -->
    <div class="section">
      <h2 class="section-title">精选案例</h2>
      <div class="app-grid">
        <AppCard
          v-for="app in featuredApps"
          :key="app.id"
          :app="app"
          :featured="true"
          @view-chat="viewApp(app.id)"
          @view-work="viewWork(app.id)"
        />
      </div>
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="featuredAppsPage.current"
          v-model:page-size="featuredAppsPage.pageSize"
          :total="featuredAppsPage.total"
          :show-size-changer="false"
          :show-total="(total: number) => `共 ${total} 个案例`"
          @change="loadFeaturedApps"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  padding: 60px 0 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  margin-bottom: 40px;
  color: white;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px;
  line-height: 1.2;
}

.highlight {
  color: #ffd700;
}

.hero-description {
  font-size: 18px;
  margin: 0;
  opacity: 0.9;
}

/* 输入区域 */
.input-section {
  position: relative;
  margin-bottom: 24px;
}

.prompt-input {
  border-radius: 12px;
  border: 2px solid #e8e8e8;
  font-size: 16px;
  padding: 16px 60px 16px 16px;
  transition: all 0.3s;
}

.prompt-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.input-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 快捷按钮 */
.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 60px;
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: 20px;
  padding: 4px 16px;
  height: auto;
}

/* 区域标题 */
.section {
  margin-bottom: 60px;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 32px;
  color: #1a1a1a;
}

/* 应用卡片网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-description {
    font-size: 16px;
  }

  .app-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    justify-content: center;
  }
}
</style>
