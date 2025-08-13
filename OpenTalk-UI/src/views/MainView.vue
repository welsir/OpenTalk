<template>
  <div class="main-container">
    <!-- 左侧导航栏 -->
    <div class="nav-sidebar">
      <div class="nav-header">
        <div class="user-avatar" @click="showProfile = true">
          {{ userStore.currentUser?.avatar || '👤' }}
        </div>
      </div>
      
      <div class="nav-items">
        <div 
          class="nav-item" 
          :class="{ active: activeNav === 'chats' }"
          @click="switchNav('chats')"
        >
          <el-icon><ChatLineRound /></el-icon>
        </div>
        <div 
           class="nav-item" 
           :class="{ active: activeNav === 'live' }"
           @click="switchNav('live')"
         >
           <el-icon><VideoCamera /></el-icon>
         </div>
      </div>
      
      <div class="nav-footer">
        <div class="nav-item" @click="showSettings = true">
          <el-icon><Setting /></el-icon>
        </div>
      </div>
    </div>
    
    <!-- 中间会话列表 -->
    <div class="chat-list" :style="{ width: chatListWidth + 'px' }">
      <div class="chat-list-header">
        <h3>{{ activeNav === 'chats' ? '聊天' : '直播' }}</h3>
        <el-button 
          size="small" 
          type="primary" 
          @click="activeNav === 'chats' ? showAddFriend = true : showCreateRoom = true"
        >
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      
      <div class="chat-list-content">
        <!-- 聊天列表 -->
        <div v-if="activeNav === 'chats'" class="chats-section">
          <!-- 群聊 -->
          <div v-if="chatStore.groups.length > 0" class="chat-group">
            <div class="group-title">群聊</div>
            <div 
              v-for="group in chatStore.groups" 
              :key="group.id"
              class="chat-item"
              :class="{ active: isActiveGroup(group) }"
              @click="selectGroup(group)"
            >
              <div class="chat-avatar">{{ group.avatar || '👥' }}</div>
              <div class="chat-info">
                <div class="chat-name">{{ group.name }}</div>
                <div class="chat-message">{{ group.lastMessage || '暂无消息' }}</div>
              </div>
            </div>
          </div>

          <!-- 私聊 -->
          <div v-if="userStore.friends.length > 0" class="chat-group">
            <div class="group-title">私聊</div>
            <div 
              v-for="friend in userStore.friends" 
              :key="friend.id"
              class="chat-item"
              :class="{ active: isActiveFriend(friend) }"
              @click="selectFriend(friend)"
            >
              <div class="chat-avatar">{{ friend.avatar || '👤' }}</div>
              <div class="chat-info">
                <div class="chat-name">{{ friend.name }}</div>
                <div class="chat-message">点击开始聊天</div>
              </div>
            </div>
          </div>
          
          <div v-if="chatStore.groups.length === 0 && userStore.friends.length === 0" class="empty-list">
            <p>暂无聊天</p>
          </div>
        </div>
        
        <!-- 直播间列表 -->
        <div v-else-if="activeNav === 'live'" class="live-rooms-section">
          <div 
            v-for="liveRoom in liveRooms" 
            :key="liveRoom.id"
            class="live-room-item"
            @click="joinLiveRoom(liveRoom)"
          >
            <div class="live-room-avatar">
               <img 
                 :src="liveRoom.streamerAvatar" 
                 :alt="liveRoom.streamerName" 
                 @error="handleImageError"
               />
               <div class="live-status" :class="{ online: liveRoom.isLive }"></div>
             </div>
            <div class="live-room-info">
              <div class="streamer-name">{{ liveRoom.streamerName }}</div>
              <div class="live-title">{{ liveRoom.title }}</div>
              <div class="live-stats">
                <span class="viewer-count">{{ formatViewerCount(liveRoom.viewerCount) }}</span>
                <span class="live-category">{{ liveRoom.category }}</span>
              </div>
            </div>
            <div class="live-thumbnail">
               <img 
                 :src="liveRoom.thumbnail" 
                 :alt="liveRoom.title" 
                 @error="handleImageError"
               />
               <div v-if="liveRoom.isLive" class="live-badge">直播中</div>
             </div>
          </div>
          
          <div v-if="liveRooms.length === 0" class="empty-list">
            <p>暂无关注的主播</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 可拖拽分割线 -->
    <div 
      class="resize-handle"
      @mousedown="startResize"
    ></div>
    
    <!-- 右侧聊天内容区 -->
    <div class="content-area">
      <!-- 房间页面 -->
      <RoomPage v-if="currentView === 'room'" />
      <!-- 私聊页面 -->
      <PrivateChatPage v-else-if="currentView === 'private'" />
      <!-- 群聊页面 -->
      <ChatArea v-else-if="currentView === 'group'" />
      <!-- 视频房间 -->
      <VideoRoom v-else-if="roomStore.selectedRoom" />
      <!-- 默认状态 -->
      <div v-else class="empty-state">
        <p>请选择一个聊天或房间</p>
      </div>
    </div>
    
    <!-- 弹窗组件 -->
    <ProfileModal v-model:visible="showProfile" />
    <AddFriendModal v-model:visible="showAddFriend" />
    <CreateRoomModal v-model:visible="showCreateRoom" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ChatLineRound, VideoCamera, Setting, Plus } from '@element-plus/icons-vue'
import ChatArea from '@/components/main/ChatArea.vue'
import VideoRoom from '@/components/main/VideoRoom.vue'
import RoomPage from '@/components/main/RoomPage.vue'
import PrivateChatPage from '@/components/main/PrivateChatPage.vue'
import ProfileModal from '@/components/modals/ProfileModal.vue'
import AddFriendModal from '@/components/modals/AddFriendModal.vue'
import CreateRoomModal from '@/components/modals/CreateRoomModal.vue'
import { useRoomStore } from '@/stores/room'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import type { User, Group, Room } from '@/types'

const roomStore = useRoomStore()
const chatStore = useChatStore()
const userStore = useUserStore()

// 响应式变量
const activeNav = ref('chats')
const showProfile = ref(false)
const showAddFriend = ref(false)
const showCreateRoom = ref(false)
const showSettings = ref(false)
const chatListWidth = ref(280)
const isResizing = ref(false)

// 直播间数据
const liveRooms = ref([
  {
    id: '1',
    streamerName: 'LPL开赛啦',
    streamerAvatar: 'https://via.placeholder.com/40x40/ff6b35/ffffff?text=LPL',
    title: '2024春季赛总决赛',
    category: '英雄联盟',
    viewerCount: 304172,
    isLive: true,
    thumbnail: 'https://via.placeholder.com/60x40/333333/ffffff?text=LOL'
  },
  {
    id: '2',
    streamerName: '游戏大神',
    streamerAvatar: 'https://via.placeholder.com/40x40/007aff/ffffff?text=游戏',
    title: '王者荣耀巅峰对决',
    category: '王者荣耀',
    viewerCount: 89234,
    isLive: true,
    thumbnail: 'https://via.placeholder.com/60x40/333333/ffffff?text=王者'
  },
  {
    id: '3',
    streamerName: '技术分享',
    streamerAvatar: 'https://via.placeholder.com/40x40/67c23a/ffffff?text=技术',
    title: 'Vue3 + TypeScript 实战',
    category: '编程',
    viewerCount: 12456,
    isLive: false,
    thumbnail: 'https://via.placeholder.com/60x40/333333/ffffff?text=Vue3'
  }
])

// 格式化观看人数
const formatViewerCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

// 加入直播间
const joinLiveRoom = (liveRoom: any) => {
  console.log('加入直播间:', liveRoom)
  // 这里可以跳转到直播间页面或打开直播窗口
}

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  if (img) {
    // 设置默认占位符图片
    img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjNDQ0NDQ0Ii8+CjxwYXRoIGQ9Ik0yMCAzMEMxNS4wMjk0IDMwIDExIDI1Ljk3MDYgMTEgMjFDMTEgMTYuMDI5NCAxNS4wMjk0IDEyIDIwIDEyQzI0Ljk3MDYgMTIgMjkgMTYuMDI5NCAyOSAyMUMyOSAyNS45NzA2IDI0Ljk3MDYgMzAgMjAgMzBaIiBmaWxsPSIjNjY2NjY2Ii8+Cjwvc3ZnPgo='
  }
}

// 初始化聊天数据
onMounted(() => {
  chatStore.initializeChats()
  roomStore.loadRooms()
})

// 导航切换
const switchNav = (nav: string) => {
  activeNav.value = nav
  // 清除当前选中状态
  if (nav === 'chats') {
    roomStore.currentRoom = null
  } else if (nav === 'live') {
    chatStore.selectedChatId = null
    chatStore.selectedChatType = 'private'
  }
}

// 选择好友
const selectFriend = (friend: User) => {
  const chatId = `private_${Math.min(userStore.currentUser!.id, friend.id)}_${Math.max(userStore.currentUser!.id, friend.id)}`
  chatStore.selectChat(chatId, 'private')
}

// 判断好友是否激活
const isActiveFriend = (friend: User) => {
  if (chatStore.selectedChatType !== 'private') return false
  const chatId = `private_${Math.min(userStore.currentUser!.id, friend.id)}_${Math.max(userStore.currentUser!.id, friend.id)}`
  return chatStore.selectedChatId === chatId
}

// 选择群组
const selectGroup = (group: Group) => {
  chatStore.selectChat(group.id, 'group')
}

// 判断群组是否激活
const isActiveGroup = (group: Group) => {
  return chatStore.selectedChatType === 'group' && chatStore.selectedChatId === group.id
}

// 加入房间
const joinRoom = (room: Room) => {
  roomStore.selectRoom(room)
}

// 拖拽调整宽度
const startResize = (e: MouseEvent) => {
  isResizing.value = true
  const startX = e.clientX
  const startWidth = chatListWidth.value
  
  const handleMouseMove = (e: MouseEvent) => {
    if (!isResizing.value) return
    
    const deltaX = e.clientX - startX
    const newWidth = startWidth + deltaX
    
    // 限制最小和最大宽度
    if (newWidth >= 200 && newWidth <= 500) {
      chatListWidth.value = newWidth
    }
  }
  
  const handleMouseUp = () => {
    isResizing.value = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
    document.body.style.cursor = 'default'
    document.body.style.userSelect = 'auto'
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

// 根据当前状态决定显示哪个页面
const currentView = computed(() => {
  // 如果选择了房间，显示房间页面
  if (roomStore.currentRoom) {
    return 'room'
  }
  // 如果选择了聊天
  if (chatStore.currentChat) {
    // 私聊页面
    if (!chatStore.currentChat.isGroup) {
      return 'private'
    }
    // 群聊页面
    else {
      return 'group'
    }
  }
  // 其他情况返回null，显示默认状态
  return null
})
</script>

<style scoped>
.main-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d30 100%);
}

/* 左侧导航栏 */
.nav-sidebar {
  width: 60px;
  background: #1e1e1e;
  border-right: 1px solid #333333;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.3);
}

.nav-header {
  margin-bottom: 20px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

.user-avatar:hover {
  background: linear-gradient(135deg, #ff8c5a 0%, #ffa940 100%);
  transform: scale(1.08);
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.4);
}

.nav-items {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888888;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.nav-item:hover {
  background: #333333;
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.nav-item.active {
  background: linear-gradient(135deg, #007aff 0%, #0056cc 100%);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.4);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #ff6b35;
  border-radius: 2px;
}

.nav-footer {
  margin-top: auto;
}

/* 中间会话列表 */
.chat-list {
  min-width: 200px;
  max-width: 500px;
  background: #2a2a2a;
  border-right: 1px solid #404040;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.2);
}

/* 可拖拽分割线 */
.resize-handle {
  width: 4px;
  background: transparent;
  cursor: col-resize;
  position: relative;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.resize-handle:hover {
  background: linear-gradient(180deg, #ff6b35 0%, #007aff 100%);
  box-shadow: 0 0 8px rgba(255, 107, 53, 0.5);
}

.resize-handle::before {
  content: '';
  position: absolute;
  top: 0;
  left: -2px;
  right: -2px;
  bottom: 0;
  background: transparent;
}

.chat-list-header {
  padding: 16px 20px;
  border-bottom: 1px solid #404040;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #333333;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.chat-list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.chat-list-content {
  flex: 1;
  overflow-y: auto;
}

.chat-group {
  padding: 12px 0;
}

.group-title {
  padding: 8px 20px;
  font-size: 12px;
  color: #999999;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: #2a2a2a;
  border-bottom: 1px solid #404040;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #333333;
  position: relative;
}

.chat-item:hover {
  background: #363636;
  transform: translateX(2px);
}

.chat-item.active {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.1);
}

.chat-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #ff6b35;
}

.chat-item.active .chat-name,
.chat-item.active .chat-message {
  color: white;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #444444 0%, #555555 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 18px;
  flex-shrink: 0;
  border: 1px solid #555555;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-name {
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-message {
  font-size: 12px;
  color: #cccccc;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-list {
  padding: 40px 20px;
  text-align: center;
  color: #86868b;
  font-size: 14px;
}

/* 直播间列表样式 */
.live-rooms-section {
  padding: 12px 0;
}

.live-room-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #333333;
  position: relative;
}

.live-room-item:hover {
  background: #363636;
  transform: translateX(2px);
}

.live-room-avatar {
  position: relative;
  margin-right: 12px;
  flex-shrink: 0;
}

.live-room-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #444444;
}

.live-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #2a2a2a;
  background: #909399;
}

.live-status.online {
  background: #67c23a;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.5);
}

.live-room-info {
  flex: 1;
  min-width: 0;
  margin-right: 12px;
}

.streamer-name {
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.live-title {
  font-size: 12px;
  color: #cccccc;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.live-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

.viewer-count {
  color: #ffd700;
  font-weight: 500;
}

.live-category {
  color: #888888;
  background: #333333;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
}

.live-thumbnail {
  position: relative;
  width: 60px;
  height: 40px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.live-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.live-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: white;
  font-size: 8px;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #1f1f1f;
  border-left: 1px solid #404040;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888888;
  font-size: 16px;
  background: radial-gradient(circle at center, #2a2a2a 0%, #1f1f1f 70%);
}
</style>