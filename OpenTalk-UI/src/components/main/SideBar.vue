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

      <div class="user-info glass-container">
        <span class="avatar icon-3d">{{ userStore.currentUser?.avatar }}</span>
        <div class="user-details">
          <div class="user-name">{{ userStore.currentUser?.name }}</div>
          <div class="user-status">
            <el-tag size="small" type="success">在线</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 标签页切换 - 修改这部分 -->
    <div class="tab-container acrylic">
      <div class="tab-buttons">
        <button
            :class="['tab-button', { active: activeTab === 'chats' }]"
            @click="switchToChats"
        >
          <el-icon class="icon-3d"><ChatLineRound /></el-icon>
          <span>聊天</span>
        </button>
        <button
            :class="['tab-button', { active: activeTab === 'rooms' }]"
            @click="switchToRooms"
        >
          <el-icon class="icon-3d"><VideoCamera /></el-icon>
          <span>房间</span>
        </button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="sidebar-content">
      <!-- 聊天内容 -->
      <div v-show="activeTab === 'chats'" class="tab-content">
        <div class="chat-actions">
          <el-button
              type="primary"
              :icon="Plus"
              @click="showAddFriend = true"
              class="action-btn shimmer-effect"
              round
          >
            添加好友
          </el-button>
          <el-button
              type="success"
              :icon="Plus"
              @click="showCreateGroup = true"
              class="action-btn shimmer-effect"
              round
          >
            创建群聊
          </el-button>
        </div>

        <!-- 群聊列表 -->
        <div v-if="chatStore.groups.length > 0" class="chat-section">
          <div class="section-title">群聊</div>
          <div class="group-list">
            <div
                v-for="group in chatStore.groups"
                :key="group.id"
                class="chat-item group-item"
                :class="{ active: isActiveGroup(group) }"
                @click="selectGroup(group)"
            >
              <span class="chat-avatar icon-3d">{{ group.avatar }}</span>
              <div class="chat-info">
                <div class="chat-name">{{ group.name }}</div>
                <div class="chat-message">{{ group.participants.length }}人</div>
              </div>
              <div class="chat-actions-menu">
                 <el-dropdown @command="(command) => handleGroupAction(command, group)" trigger="click">
                   <el-button :icon="More" circle size="small" class="menu-btn" />
                   <template #dropdown>
                     <el-dropdown-menu>
                       <el-dropdown-item command="leave" class="danger">
                       <el-icon class="icon-3d"><More /></el-icon>
                       退出群聊
                     </el-dropdown-item>
                     </el-dropdown-menu>
                   </template>
                 </el-dropdown>
               </div>
            </div>
          </div>
        </div>

        <!-- 私聊列表 -->
        <div class="chat-section">
          <div class="section-title">私聊</div>
          <div v-if="userStore.friends.length === 0" class="empty-state">
            <el-empty description="暂无好友，快去添加吧！" />
          </div>
          <div v-else class="friend-list">
            <div
                v-for="friend in userStore.friends"
                :key="friend.id"
                class="chat-item friend-item"
                :class="{ active: isActiveFriend(friend) }"
                @click="selectFriend(friend)"
            >
              <span class="chat-avatar icon-3d">{{ friend.avatar }}</span>
              <div class="chat-info">
                <div class="chat-name">{{ friend.name }}</div>
                <div class="chat-message">点击开始聊天</div>
              </div>
              <el-badge
                  v-if="friend.status === 'online'"
                  is-dot
                  class="status-badge"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 房间内容 -->
      <div v-show="activeTab === 'rooms'" class="tab-content">
        <div class="room-header-actions">
          <el-button
              type="primary"
              :icon="Plus"
              @click="createRoom"
              class="add-btn"
          >
            创建房间
          </el-button>
        </div>

        <RoomList
            :rooms="roomStore.rooms"
            :loading="roomStore.isLoading"
            @select-room="selectRoom"
            @join-room="joinRoom"
            @leave-room="leaveRoom"
            @edit-room="editRoom"
            @delete-room="deleteRoom"
            @copy-room-link="copyRoomLink"
            @create-room="createRoom"
        />
      </div>
    </div>

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
    
    <!-- 创建群聊弹窗 -->
    <CreateGroupModal v-model:visible="showCreateGroup" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Plus, SwitchButton, ChatLineRound, VideoCamera, More } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { useRoomStore } from '@/stores/room'
import ProfileModal from '@/components/Modals/ProfileModal.vue'
import AddFriendModal from '@/components/Modals/AddFriendModal.vue'
import CreateGroupModal from '@/components/Modals/CreateGroupModal.vue'
import RoomList from '@/components/room/RoomList.vue'
import type { User, Room, Group } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()
const roomStore = useRoomStore()

const activeTab = ref('chats')
const showProfile = ref(false)
const showAddFriend = ref(false)
const showCreateGroup = ref(false)

// 切换到聊天标签页
const switchToChats = () => {
  console.log('Switching to chats tab, current activeTab:', activeTab.value)
  activeTab.value = 'chats'
  // 清除房间选中状态
  if (roomStore.currentRoom) {
    console.log('Clearing room selection:', roomStore.currentRoom.name)
    roomStore.currentRoom = null
  }
  console.log('After switch - activeTab:', activeTab.value, 'currentRoom:', roomStore.currentRoom)
}

// 切换到房间标签页
const switchToRooms = () => {
  console.log('Switching to rooms tab, current activeTab:', activeTab.value)
  activeTab.value = 'rooms'
  // 清除聊天选中状态
  if (chatStore.selectedChatId) {
    console.log('Clearing chat selection:', chatStore.selectedChatId)
    chatStore.selectedChatId = null
    chatStore.selectedChatType = 'private'
  }
  console.log('After switch - activeTab:', activeTab.value, 'selectedChatId:', chatStore.selectedChatId)
}

const selectFriend = (friend: User) => {
  // 生成私聊ID
  const chatId = `private_${Math.min(userStore.currentUser!.id, friend.id)}_${Math.max(userStore.currentUser!.id, friend.id)}`
  chatStore.selectChat(chatId, 'private')
}

const isActiveFriend = (friend: User) => {
  if (chatStore.selectedChatType !== 'private') return false
  const chatId = `private_${Math.min(userStore.currentUser!.id, friend.id)}_${Math.max(userStore.currentUser!.id, friend.id)}`
  return chatStore.selectedChatId === chatId
}

const selectGroup = (group: Group) => {
  chatStore.selectChat(group.id, 'group')
}

const isActiveGroup = (group: Group) => {
  return chatStore.selectedChatType === 'group' && chatStore.selectedChatId === group.id
}

const handleGroupAction = async (command: string, group: Group) => {
  if (command === 'leave') {
    try {
      await ElMessageBox.confirm(`确定要退出群聊"${group.name}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      const success = chatStore.leaveGroup(group.id)
      if (success) {
        ElMessage.success('已退出群聊')
        // 如果当前选中的是这个群聊，清除选中状态
        if (chatStore.selectedChatId === group.id) {
          chatStore.selectedChatId = null
          chatStore.selectedChatType = 'private'
        }
      } else {
        ElMessage.error('退出群聊失败')
      }
    } catch {
      // 用户取消
    }
  }
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

const selectRoom = (room: Room) => {
  roomStore.selectRoom(room)
  router.push('/video-room')
}

const joinRoom = (room: Room) => {
  roomStore.joinRoom(room.id)
  ElMessage.success(`已加入房间：${room.name}`)
  router.push('/video-room')
}

const leaveRoom = (room: Room) => {
  ElMessageBox.confirm(`确定要离开房间"${room.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    roomStore.leaveRoom(room.id)
    ElMessage.success('已离开房间')
  }).catch(() => {})
}

const editRoom = (room: Room) => {
  ElMessageBox.prompt('请输入新的房间名称', '编辑房间', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: room.name,
    inputPattern: /\S+/,
    inputErrorMessage: '房间名称不能为空'
  }).then(({ value }) => {
    // TODO: 实现编辑房间逻辑
    ElMessage.success('房间信息已更新')
  }).catch(() => {})
}

const deleteRoom = (room: Room) => {
  ElMessageBox.confirm(`确定要删除房间"${room.name}"吗？此操作不可撤销。`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(() => {
    // TODO: 实现删除房间逻辑
    ElMessage.success('房间已删除')
  }).catch(() => {})
}

const copyRoomLink = (room: Room) => {
  const link = `${window.location.origin}/room/${room.id}`
  navigator.clipboard.writeText(link).then(() => {
    ElMessage.success('房间链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
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
  width: 280px;
  height: 100vh;
  background: var(--glass-bg);
  border-right: 1px solid var(--glass-border);
  display: flex;
  flex-direction: column;
  position: relative;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: var(--shadow-lg);
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 20%, rgba(120, 119, 198, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 118, 117, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(0, 122, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.sidebar > * {
  position: relative;
  z-index: 1;
}

.sidebar-header {
  padding: 20px;
  background: var(--surface-secondary);
  border-bottom: 1px solid var(--glass-border);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.sidebar-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 24px;
  right: 24px;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
}

.app-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.app-title h2 {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-neutral-900);
  letter-spacing: -0.05em;
  margin: 0 0 var(--space-4) 0;
  font-family: var(--font-display);
}

.user-info {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #ffffff;
  border: 1px solid #e5e5ea;
  border-radius: 8px;
  transition: border-color 0.15s ease;
}

.user-info:hover {
  border-color: #d2d2d7;
}

.avatar {
  font-size: 40px;
  margin-right: 16px;
  filter: 
    drop-shadow(0 4px 8px rgba(0, 0, 0, 0.15))
    drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.user-info:hover .avatar {
  transform: scale(1.1) rotate(5deg);
  filter: 
    drop-shadow(0 6px 12px rgba(0, 0, 0, 0.2))
    drop-shadow(0 3px 6px rgba(0, 0, 0, 0.15));
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: var(--font-semibold);
  color: var(--color-neutral-900);
  margin-bottom: var(--space-1);
  font-size: var(--text-base);
  font-family: var(--font-display);
  letter-spacing: -0.01em;
}

/* 简洁标签页样式 */
.tab-container {
  border-bottom: 1px solid var(--glass-border);
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.tab-buttons {
  display: flex;
  margin: 0;
  padding: 0;
}

.tab-button {
  flex: 1;
  padding: var(--space-3) var(--space-4);
  border: none;
  background: transparent;
  color: var(--color-neutral-500);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  cursor: pointer;
  transition: all var(--duration-150) var(--ease-out);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border-bottom: 2px solid transparent;
  font-family: var(--font-primary);
  letter-spacing: 0.01em;
}

.tab-button:hover:not(.active) {
  color: var(--color-neutral-800);
  background: var(--color-neutral-50);
}

.tab-button.active {
  color: var(--color-primary-600);
  border-bottom-color: var(--color-primary-600);
  background: transparent;
  font-weight: var(--font-semibold);
}

.tab-button .el-icon {
  font-size: 18px;
}

/* 内容区域 */
.sidebar-content {
  flex: 1;
  overflow: hidden;
  position: relative;
  background: transparent;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.tab-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow-y: auto;
}

.chat-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.action-btn {
  flex: 1;
  height: 48px;
  font-weight: 400;
  background: rgba(0, 122, 255, 0.9) !important;
  border: 0.5px solid rgba(0, 122, 255, 1) !important;
  color: white !important;
  box-shadow: 0 1px 3px rgba(0, 122, 255, 0.3) !important;
  transition: all 0.2s ease !important;
  position: relative;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
  transition: left 0.6s ease;
}

.action-btn:hover {
  background: rgba(0, 122, 255, 1) !important;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.4) !important;
  transform: translateY(-0.5px);
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn:active {
  transform: translateY(-1px) scale(0.98);
}

.chat-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--color-neutral-500);
  margin-bottom: var(--space-3);
  padding: 0 var(--space-1);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-family: var(--font-primary);
}

.friend-list,
.group-list,
.room-list {
  flex: 1;
  overflow-y: auto;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 18px 20px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.chat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(0, 122, 255, 0.1) 50%, transparent 100%);
  transition: left 0.5s ease;
}

.chat-item:hover {
  transform: translateX(6px) translateY(-2px);
  background: 
    linear-gradient(135deg, rgba(0, 122, 255, 0.12) 0%, rgba(88, 86, 214, 0.08) 100%);
  border-color: rgba(0, 122, 255, 0.3);
  box-shadow: 
    0 8px 24px rgba(0, 122, 255, 0.2),
    0 4px 8px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.chat-item:hover::before {
  left: 100%;
}

.chat-item.active {
  background: 
    linear-gradient(135deg, rgba(0, 122, 255, 0.2) 0%, rgba(88, 86, 214, 0.15) 100%);
  border-color: rgba(0, 122, 255, 0.4);
  box-shadow: 
    0 8px 32px rgba(0, 122, 255, 0.25),
    0 4px 12px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    inset 0 -1px 0 rgba(0, 0, 0, 0.05);
  transform: translateX(8px);
}

.chat-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  top: 20%;
  bottom: 20%;
  width: 4px;
  background: linear-gradient(180deg, #007aff 0%, #5856d6 100%);
  border-radius: 0 2px 2px 0;
  box-shadow: 0 0 8px rgba(0, 122, 255, 0.5);
}

.chat-avatar {
  font-size: 36px;
  margin-right: 16px;
  filter: 
    drop-shadow(0 4px 8px rgba(0, 0, 0, 0.15))
    drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.chat-item:hover .chat-avatar {
  transform: scale(1.1);
  filter: 
    drop-shadow(0 6px 12px rgba(0, 0, 0, 0.2))
    drop-shadow(0 3px 6px rgba(0, 0, 0, 0.15));
}

.chat-item.active .chat-avatar {
  transform: scale(1.05);
  filter: 
    drop-shadow(0 4px 12px rgba(0, 122, 255, 0.3))
    drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));
}

.chat-info {
  flex: 1;
  z-index: 1;
}

.chat-name {
  font-weight: var(--font-semibold);
  color: var(--color-neutral-900);
  margin-bottom: var(--space-1);
  font-size: var(--text-base);
  font-family: var(--font-primary);
  letter-spacing: -0.01em;
}

.chat-message {
  font-size: var(--text-sm);
  color: var(--color-neutral-500);
  font-weight: var(--font-normal);
  line-height: var(--leading-snug);
}

.status-badge {
  margin-left: auto;
  z-index: 1;
}

.chat-actions-menu {
  z-index: 1;
}

.menu-btn {
  opacity: 0;
  transition: opacity 0.3s;
}

.chat-item:hover .menu-btn {
  opacity: 1;
}

.room-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.room-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border-color: rgba(0, 122, 255, 0.2);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.room-name {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 15px;
}

.room-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #8e8e93;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
}

.sidebar-footer .el-button {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-footer .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3);
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}
</style>