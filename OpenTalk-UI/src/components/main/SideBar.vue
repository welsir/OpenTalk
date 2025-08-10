<template>
  <div class="sidebar">
    <!-- 顶部用户信息 -->
    <div class="sidebar-header">
      <div class="app-title">
        <h2>OpenTalk</h2>
        <el-button
            :icon="Setting"
            circle
            @click="showProfile = true"
        />
      </div>

      <div class="user-info">
        <span class="avatar">{{ userStore.currentUser?.avatar }}</span>
        <div class="user-details">
          <div class="user-name">{{ userStore.currentUser?.name }}</div>
          <div class="user-status">
            <el-tag size="small" type="success">在线</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" class="sidebar-tabs">
      <el-tab-pane label="聊天" name="chats">
        <div class="tab-content">
          <el-button
              type="primary"
              :icon="Plus"
              @click="showAddFriend = true"
              class="add-btn"
          >
            添加好友
          </el-button>

          <div v-if="userStore.friends.length === 0" class="empty-state">
            <el-empty description="暂无好友，快去添加吧！" />
          </div>

          <div v-else class="friend-list">
            <div
                v-for="friend in userStore.friends"
                :key="friend.id"
                class="friend-item"
                :class="{ active: chatStore.selectedChat?.id === friend.id }"
                @click="selectFriend(friend)"
            >
              <span class="friend-avatar">{{ friend.avatar }}</span>
              <div class="friend-info">
                <div class="friend-name">{{ friend.name }}</div>
                <div class="friend-message">点击开始聊天</div>
              </div>
              <el-badge
                  v-if="friend.status === 'online'"
                  is-dot
                  class="status-badge"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="房间" name="rooms">
        <div class="tab-content">
          <el-button
              type="primary"
              :icon="Plus"
              @click="createRoom"
              class="add-btn"
          >
            创建房间
          </el-button>

          <div class="room-list">
            <div
                v-for="room in roomStore.rooms"
                :key="room.id"
                class="room-item"
                @click="joinRoom(room)"
            >
              <div class="room-header">
                <span class="room-name">{{ room.name }}</span>
                <el-tag
                    v-if="room.active"
                    size="small"
                    type="success"
                >
                  进行中
                </el-tag>
              </div>
              <div class="room-info">
                <span>创建者：{{ room.creator }}</span>
                <span>{{ room.participants }} 位参与者</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 底部操作 -->
    <div class="sidebar-footer">
      <el-button
          type="danger"
          :icon="SwitchButton"
          @click="handleLogout"
          text
      >
        退出登录
      </el-button>
    </div>

    <!-- 个人资料弹窗 -->
    <ProfileModal v-model:visible="showProfile" />

    <!-- 添加好友弹窗 -->
    <AddFriendModal v-model:visible="showAddFriend" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Plus, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { useRoomStore } from '@/stores/room'
import ProfileModal from '@/components/Modals/ProfileModal.vue'
import AddFriendModal from '@/components/Modals/AddFriendModal.vue'
import type { User, Room } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()
const roomStore = useRoomStore()

const activeTab = ref('chats')
const showProfile = ref(false)
const showAddFriend = ref(false)

const selectFriend = (friend: User) => {
  chatStore.selectChat(friend)
}

const createRoom = () => {
  ElMessageBox.prompt('请输入房间名称', '创建房间', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '房间名称不能为空'
  }).then(({ value }) => {
    const room = roomStore.createRoom(value, userStore.currentUser!.name)
    ElMessage.success('房间创建成功')
  }).catch(() => {})
}

const joinRoom = (room: Room) => {
  roomStore.joinRoom(room)
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}
</script>

<style scoped>
.sidebar {
  width: 320px;
  height: 100vh;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.app-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.app-title h2 {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 12px;
}

.avatar {
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

.sidebar-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.sidebar-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
}

.tab-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 15px;
}

.add-btn {
  width: 100%;
  margin-bottom: 15px;
}

.friend-list,
.room-list {
  flex: 1;
  overflow-y: auto;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
  margin-bottom: 8px;
}

.friend-item:hover {
  background: #f5f7fa;
}

.friend-item.active {
  background: #ecf5ff;
}

.friend-avatar {
  font-size: 28px;
  margin-right: 12px;
}

.friend-info {
  flex: 1;
}

.friend-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.friend-message {
  font-size: 12px;
  color: #909399;
}

.status-badge {
  margin-left: auto;
}

.room-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 10px;
  transition: transform 0.3s;
}

.room-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.room-name {
  font-weight: 500;
  color: #303133;
}

.room-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.sidebar-footer {
  padding: 15px;
  border-top: 1px solid #e4e7ed;
}

.sidebar-footer .el-button {
  width: 100%;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>