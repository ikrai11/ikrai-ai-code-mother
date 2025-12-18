<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
  </div>

  <a-table :columns="columns" :data-source="data" :pagination="pagination" @change="doTableChange">
    <template #headerCell="{ column }">
      <template v-if="column.key === 'name'">
        <span>
          <smile-outlined />
          Name
        </span>
      </template>
    </template>

    <template #bodyCell="{ column, text, record }">
      <template v-if="['userName', 'userProfile', 'userRole'].includes(column.dataIndex)">
        <div>
          <a-input
            v-if="editableData[record.id]"
            v-model:value="editableData[record.id][column.dataIndex]"
            style="margin: -5px 0"
          />
          <template v-else-if="column.dataIndex === 'userRole'">
            <div v-if="text === 'admin'">
              <a-tag color="green">管理员</a-tag>
            </div>
            <div v-else>
              <a-tag color="blue">普通用户</a-tag>
            </div>
          </template>
          <template v-else>
            {{ text }}
          </template>
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'userAvatar'">
        <a-image :src="record.userAvatar" :width="120" />
      </template>
      <template v-else-if="column.dataIndex === 'createTime'">
        {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-else-if="column.key === 'action'">
        <div class="editable-row-operations">
          <span v-if="editableData[record.id]">
            <a-button type="primary" @click="save(record.id)" style="margin-right: 8px">保存</a-button>
            <a-popconfirm title="确定要取消吗？" @confirm="cancel(record.id)">
              <a-button>取消</a-button>
            </a-popconfirm>
          </span>
          <span v-else>
            <a-button @click="edit(record.id)" style="margin-right: 8px">编辑</a-button>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </span>
        </div>
      </template>
    </template>
  </a-table>
</template>
<script lang="ts" setup>
import { SmileOutlined, DownOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage, updateUser } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { cloneDeep } from 'lodash-es'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 正在编辑的数据
const editableData = reactive<Record<string, API.UserVO>>({})

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number, pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 获取数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 编辑用户
const edit = (id: string) => {
  const user = data.value.find(item => item.id === id)
  if (user) {
    editableData[id] = cloneDeep(user)
  }
}

// 保存用户
const save = async (id: string) => {
  try {
    const userToUpdate = editableData[id]
    if (!userToUpdate) return

    // 准备更新请求数据
    const updateRequest: API.UserUpdateRequest = {
      id: userToUpdate.id,
      userName: userToUpdate.userName,
      userProfile: userToUpdate.userProfile,
      userRole: userToUpdate.userRole,
    }

    // 调用更新API
    const response = await updateUser(updateRequest)
    if (response.data.code === 0) {
      // 更新成功，同步到数据源
      const userIndex = data.value.findIndex(item => item.id === id)
      if (userIndex !== -1) {
        Object.assign(data.value[userIndex], userToUpdate)
      }
      message.success('更新成功')
      delete editableData[id]
    } else {
      message.error(response.data.message || '更新失败')
    }
  } catch (error) {
    message.error('更新失败')
  }
}

// 取消编辑
const cancel = (id: string) => {
  delete editableData[id]
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>
<style scoped>
#userManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}
</style>
