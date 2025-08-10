<template>
  <el-dialog
      v-model="dialogVisible"
      title="添加好友"
      width="500px"
  >
    <div class="search-box">
      <el-input
          v-model="searchQuery"
          placeholder="搜索用户..."
          prefix-icon="Search"
          clearable
      />
    </div>

    <div class="user-list">
      <div
          v-for="user in filteredUsers"
          :key="user.id"
          class="user-item"
      >
        <div class="user-info">
          <span class="user-avatar">{{ user.avatar }}</span>
          <div class="user-details">
            <div class="user-name">{{ user.name }}</div>
            <div class="user-bio">{{ user.bio }}</div>
          </div>
        </div>

        <el-button
            type="primary"
            size="small"
            @click="addFriend(user)"
        >
          添加
        </el-button>
      </div>

      <el-empty
          v-if="filteredUsers.length === 0"
          description="没有找到用户"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { User } from '@/types'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const userStore = useUserStore()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const searchQuery = ref('')

const filteredUsers = computed(() => {
  return userStore.availableUsers.filter(user =>
      user.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      user.username.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const addFriend = (user: User) => {
  const success = userStore.addFriend(user.id)
  if (success) {
    ElMessage.success(`已添加 ${user.name} 为好友`)
    dialogVisible.value = false
  }
}
</script>

<style scoped>
.search-box {
  margin-bottom: 20px;
}

.user-list {
  max-height: 400px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-item:hover {
  background: #f5f7fa;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  font-size: 32px;
  margin-right: 12px;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.user-bio {
  font-size: 12px;
  color: #909399;
}
</style>