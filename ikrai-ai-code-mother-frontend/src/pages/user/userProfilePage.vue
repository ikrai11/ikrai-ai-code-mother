<template>
  <div id="userProfilePage">
    <h2 class="title">个人中心</h2>
    <a-divider />

    <a-form
      :model="formData"
      layout="vertical"
      @finish="handleSubmit"
      style="max-width: 600px;"
    >
      <a-form-item label="账号" :colon="false">
        <a-input v-model:value="formData.userAccount" disabled placeholder="账号" />
      </a-form-item>

      <a-form-item label="用户名" :colon="false" :rules="[{ required: true, message: '请输入用户名' }]">
        <a-input v-model:value="formData.userName" placeholder="输入用户名" />
      </a-form-item>

      <a-form-item label="头像" :colon="false">
        <a-input v-model:value="formData.userAvatar" placeholder="输入头像URL" />
      </a-form-item>

      <a-form-item label="简介" :colon="false">
        <a-textarea v-model:value="formData.userProfile" placeholder="输入个人简介" :rows="4" />
      </a-form-item>

      <a-form-item label="角色" :colon="false">
        <a-select v-model:value="formData.userRole" disabled style="width: 100%;">
          <a-select-option value="user">普通用户</a-select-option>
          <a-select-option value="admin">管理员</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit">保存修改</a-button>
        <a-button style="margin-left: 8px;" @click="handleReset">重置</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { updateUser } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser

// 表单数据
const formData = reactive<API.UserUpdateRequest>({
  id: loginUser.id,
  userName: loginUser.userName,
  userAvatar: loginUser.userAvatar,
  userProfile: loginUser.userProfile,
  userRole: loginUser.userRole
})

// 原始数据，用于重置
const originalData = ref<API.UserUpdateRequest>({...formData})

// 页面加载时初始化表单数据
onMounted(() => {
  formData.id = loginUser.id
  formData.userName = loginUser.userName
  formData.userAvatar = loginUser.userAvatar
  formData.userProfile = loginUser.userProfile
  formData.userRole = loginUser.userRole
  originalData.value = {...formData}
})

// 提交表单
const handleSubmit = async () => {
  try {
    const res = await updateUser(formData)
    if (res.data.code === 0) {
      message.success('个人信息更新成功')
      // 更新全局登录用户信息
      loginUserStore.setLoginUser({
        ...loginUserStore.loginUser,
        userName: formData.userName,
        userAvatar: formData.userAvatar,
        userProfile: formData.userProfile
      })
      // 更新原始数据
      originalData.value = {...formData}
    } else {
      message.error('更新失败：' + (res.data.message || '未知错误'))
    }
  } catch (error) {
    message.error('更新失败：' + error)
  }
}

// 重置表单
const handleReset = () => {
  Object.assign(formData, originalData.value)
}
</script>

<style scoped>
#userProfilePage {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.title {
  margin-bottom: 0;
  color: #1890ff;
}
</style>
