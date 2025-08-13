<template>
  <div class="chat-area">
    <template v-if="chatStore.selectedChat && chatStore.selectedChat.isGroup">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-user-info">
          <span class="chat-avatar icon-3d">{{ chatStore.selectedChat.avatar }}</span>
          <div class="chat-user-details">
            <div class="chat-user-name">{{ chatStore.selectedChat.name }}</div>
            <div class="chat-user-status">
              {{ chatStore.selectedChat.status === 'online' ? '在线' : '离线' }}
            </div>
          </div>
        </div>

        <div class="chat-actions">
          <el-button :icon="Phone" circle class="glass-container" />
          <el-button :icon="VideoCamera" circle class="glass-container" />
          <el-dropdown @command="handleGroupMenuCommand">
            <el-button>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="groupInfo">群聊信息</el-dropdown-item>
                <el-dropdown-item command="editGroupName">修改群名称</el-dropdown-item>
                <el-dropdown-item command="groupAnnouncement">群公告</el-dropdown-item>
                <el-dropdown-item command="groupMembers">群成员管理</el-dropdown-item>
                <el-dropdown-item divided command="muteGroup">消息免打扰</el-dropdown-item>
                <el-dropdown-item command="leaveGroup" class="danger">退出群聊</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{ 'message-own': isOwnMessage(message) }"
        >
          <div class="message-bubble">
            <div class="message-content">{{ message.content }}</div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <!-- 工具栏 -->
        <div class="input-toolbar">
          <el-button
              :icon="Picture"
              circle
              size="small"
              @click="selectImage"
              title="发送图片"
              class="acrylic"
          />
          <el-button
              :icon="Paperclip"
              circle
              size="small"
              @click="selectFile"
              title="发送文件"
              class="acrylic"
          />
          <el-button
              :icon="Microphone"
              circle
              size="small"
              @click="toggleVoiceRecord"
              :type="isRecording ? 'danger' : 'default'"
              :title="isRecording ? '停止录音' : '语音消息'"
              class="acrylic"
          />
          <el-popover
              placement="top"
              width="300"
              trigger="click"
          >
            <template #reference>
              <el-button
                  circle
                  size="small"
                  title="表情"
                  class="acrylic"
              >
                😊
              </el-button>
            </template>
            <div class="emoji-picker">
              <span
                  v-for="emoji in commonEmojis"
                  :key="emoji"
                  class="emoji-item"
                  @click="insertEmoji(emoji)"
              >
                {{ emoji }}
              </span>
            </div>
          </el-popover>
        </div>
        
        <!-- 输入框 -->
        <div class="input-container">
          <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入消息..."
              @keydown="handleKeyDown"
              class="input-field"
          />
          <el-button
              type="primary"
              :icon="Promotion"
              @click="sendMessage"
              :disabled="!inputMessage.trim() && !selectedFiles.length"
              class="send-button shimmer-effect"
          >
            发送
          </el-button>
        </div>
        
        <!-- 文件预览 -->
        <div v-if="selectedFiles.length" class="file-preview">
          <div
              v-for="(file, index) in selectedFiles"
              :key="index"
              class="file-item"
          >
            <el-image
                v-if="file.type.startsWith('image/')"
                :src="file.preview"
                class="image-preview"
                fit="cover"
            />
            <div v-else class="file-info">
              <el-icon><Document /></el-icon>
              <span>{{ file.name }}</span>
            </div>
            <el-button
                :icon="Close"
                circle
                size="small"
                @click="removeFile(index)"
                class="remove-file pulse-effect"
            />
          </div>
        </div>
      </div>
    </template>

    <!-- 空状态 -->
    <div v-else class="empty-chat">
      <el-empty description="选择一个好友开始聊天">
        <template #image>
          <el-icon :size="100" color="#c0c4cc">
            <ChatLineRound />
          </el-icon>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { 
  Phone, 
  VideoCamera, 
  Promotion, 
  ChatLineRound, 
  Picture,
  More, 
  Paperclip, 
  Microphone, 
  Document, 
  Close 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { useRoomStore } from '@/stores/room'
import type { Message } from '@/types'

const chatStore = useChatStore()
const userStore = useUserStore()
const roomStore = useRoomStore()

const inputMessage = ref('')
const messagesContainer = ref<HTMLElement>()
const selectedFiles = ref<File[]>([])
const isRecording = ref(false)
const mediaRecorder = ref<MediaRecorder | null>(null)

// 常用表情
const commonEmojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣',
  '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰',
  '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜',
  '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏',
  '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
  '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠',
  '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱', '😨',
  '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙',
  '👏', '🙌', '👐', '🤲', '🤝', '🙏', '❤️', '🧡',
  '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔'
]

const messages = computed(() => {
  if (!chatStore.selectedChatId) return []
  return chatStore.currentMessages
})

const currentChat = computed(() => {
  return chatStore.currentChat
})

const isOwnMessage = (message: Message) => {
  return message.senderId === userStore.currentUser?.id
}

const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const sendMessage = () => {
  if ((!inputMessage.value.trim() && !selectedFiles.value.length) || !chatStore.selectedChatId) return

  // 发送文本消息
  if (inputMessage.value.trim()) {
    chatStore.sendMessage(inputMessage.value.trim())
    inputMessage.value = ''
  }

  // 发送文件
  if (selectedFiles.value.length) {
    selectedFiles.value.forEach(file => {
      const fileType = file.type.startsWith('image/') ? 'image' : 'file'
      chatStore.sendMessage(`[${fileType.toUpperCase()}] ${file.name}`, fileType as any, file)
    })
    selectedFiles.value = []
  }

  scrollToBottom()
}

const selectImage = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.multiple = true
  input.onchange = (e) => {
    const files = (e.target as HTMLInputElement).files
    if (files) {
      handleFileSelect(Array.from(files))
    }
  }
  input.click()
}

const selectFile = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.multiple = true
  input.onchange = (e) => {
    const files = (e.target as HTMLInputElement).files
    if (files) {
      handleFileSelect(Array.from(files))
    }
  }
  input.click()
}

const handleFileSelect = (files: File[]) => {
  files.forEach(file => {
    if (file.size > 10 * 1024 * 1024) { // 10MB limit
      ElMessage.warning(`文件 ${file.name} 超过10MB限制`)
      return
    }
    
    const fileWithPreview = file as File & { preview?: string }
    
    if (file.type.startsWith('image/')) {
      const reader = new FileReader()
      reader.onload = (e) => {
        fileWithPreview.preview = e.target?.result as string
      }
      reader.readAsDataURL(file)
    }
    
    selectedFiles.value.push(fileWithPreview)
  })
}

const removeFile = (index: number) => {
  selectedFiles.value.splice(index, 1)
}

const insertEmoji = (emoji: string) => {
  inputMessage.value += emoji
}

const toggleVoiceRecord = async () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder.value = new MediaRecorder(stream)
    
    const chunks: Blob[] = []
    
    mediaRecorder.value.ondataavailable = (e) => {
      chunks.push(e.data)
    }
    
    mediaRecorder.value.onstop = () => {
      const blob = new Blob(chunks, { type: 'audio/wav' })
      const file = new File([blob], `voice_${Date.now()}.wav`, { type: 'audio/wav' })
      chatStore.sendMessage('[语音消息]', 'audio', file)
      
      // 停止所有音频轨道
      stream.getTracks().forEach(track => track.stop())
    }
    
    mediaRecorder.value.start()
    isRecording.value = true
    ElMessage.info('开始录音...')
  } catch (error) {
    console.error('录音失败:', error)
    ElMessage.error('无法访问麦克风')
  }
}

const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop()
    isRecording.value = false
    ElMessage.success('录音完成')
  }
}

const startVideoCall = () => {
  if (!currentChat.value) return
  
  // 创建视频通话房间
  const room = roomStore.createRoom(
    `与 ${currentChat.value.name} 的视频通话`,
    '视频通话',
    true
  )
  
  if (room) {
    roomStore.joinRoom(room.id)
    ElMessage.success('视频通话已发起')
  }
}

const startAudioCall = () => {
  if (!currentChat.value) return
  
  // 创建语音通话房间
  const room = roomStore.createRoom(
    `与 ${currentChat.value.name} 的语音通话`,
    '语音通话',
    true
  )
  
  if (room) {
    roomStore.joinRoom(room.id)
    ElMessage.success('语音通话已发起')
  }
}

// 处理群聊菜单命令
const handleGroupMenuCommand = (command: string) => {
  switch (command) {
    case 'groupInfo':
      ElMessage.info('群聊信息功能开发中...')
      break
    case 'editGroupName':
      ElMessage.info('修改群名称功能开发中...')
      break
    case 'groupAnnouncement':
      ElMessage.info('群公告功能开发中...')
      break
    case 'groupMembers':
      ElMessage.info('群成员管理功能开发中...')
      break
    case 'muteGroup':
      ElMessage.info('消息免打扰功能开发中...')
      break
    case 'leaveGroup':
      ElMessage.warning('退出群聊功能开发中...')
      break
    default:
      break
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

watch(() => chatStore.selectedChatId, () => {
  scrollToBottom()
})

watch(() => messages.value.length, () => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d30 100%);
  position: relative;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #2a2a2a 0%, #333333 100%);
  border-bottom: 1px solid #444444;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}



.chat-user-info {
  display: flex;
  align-items: center;
}

.chat-avatar {
  font-size: 42px;
  margin-right: 20px;
  filter: 
    drop-shadow(0 6px 12px rgba(0, 0, 0, 0.15))
    drop-shadow(0 3px 6px rgba(0, 0, 0, 0.1))
    drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
}

.chat-header:hover .chat-avatar {
  transform: scale(1.05) rotate(2deg);
  filter: 
    drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2))
    drop-shadow(0 4px 8px rgba(0, 0, 0, 0.15))
    drop-shadow(0 2px 4px rgba(0, 0, 0, 0.25));
}

.chat-user-details {
  display: flex;
  flex-direction: column;
}

.chat-user-name {
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 4px;
  font-size: 18px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.chat-user-status {
  font-size: 13px;
  color: #cccccc;
  font-weight: 500;
}

.chat-actions {
  display: flex;
  gap: 12px;
}

.chat-actions .el-button {
  border-radius: 8px;
  height: 36px;
  padding: 0 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #333333 0%, #444444 100%);
  border: 1px solid #555555;
  color: #e5e5e7;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: visible;
}

.chat-actions .el-button::after {
  content: '';
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.3) 0%, rgba(255, 215, 0, 0.1) 50%, transparent 70%);
  border-radius: 50%;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  z-index: -2;
}

.chat-actions .el-button:hover {
  background: linear-gradient(135deg, #444444 0%, #555555 100%);
  color: #ffd700;
  border-color: #ffd700;
  transform: translateY(-8px) scale(1.05);
  box-shadow: 
    0 8px 25px rgba(255, 215, 0, 0.4),
    0 4px 15px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.chat-actions .el-button:hover::after {
  opacity: 1;
  transform: scale(1.2);
}

.chat-actions .el-button:hover .el-icon {
   transform: translateY(-2px) scale(1.1);
   filter: drop-shadow(0 0 8px rgba(255, 215, 0, 0.8));
   transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
 }

 .chat-actions .el-dropdown {
   margin: 0 !important;
   padding: 0 !important;
   display: inline-block;
 }

 .chat-actions .el-dropdown .el-button {
   margin: 0 !important;
   padding: 0 16px !important;
 }

 .chat-actions > * {
   margin: 0 !important;
 }

 .chat-actions > *:not(:last-child) {
   margin-right: 0 !important;
 }




.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d30 100%);
}

.message-item {
  display: flex;
  margin-bottom: 20px;
}

.message-item.message-own {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 8px;
  word-wrap: break-word;
  transition: all 0.15s ease;
}

.message-own .message-bubble {
  background: linear-gradient(135deg, #007aff 0%, #0056cc 100%);
  color: #ffffff;
  margin-left: auto;
  border: 1px solid #007aff;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
  font-weight: 500;
}

.message-other .message-bubble {
  background: linear-gradient(135deg, #2a2a2a 0%, #363636 100%);
  color: #e5e5e7;
  border: 1px solid #444444;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}



.message-content {
  margin-bottom: 6px;
  word-wrap: break-word;
  font-size: 15px;
  line-height: 1.4;
  font-weight: 400;
}

.message-time {
  font-size: 12px;
  opacity: 0.6;
  font-weight: 500;
}

.message-own .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.chat-input {
  padding: 16px 24px;
  background: linear-gradient(135deg, #2a2a2a 0%, #333333 100%);
  border-top: 1px solid #444444;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.3);
}



.input-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.input-toolbar .el-button {
  border-radius: 14px;
  height: 42px;
  padding: 0 16px;
  font-weight: 600;
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.7) 100%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(15px);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
}

.input-toolbar .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.4) 50%, transparent 100%);
  transition: left 0.6s ease;
}

.input-toolbar .el-button:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 
    0 6px 20px rgba(0, 0, 0, 0.12),
    0 3px 6px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(255, 255, 255, 0.9) 100%);
}

.input-toolbar .el-button:hover::before {
  left: 100%;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: linear-gradient(135deg, #333333 0%, #444444 100%);
  border: 1px solid #555555;
  border-radius: 8px;
  padding: 8px 12px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.input-container:focus-within {
  border-color: #ffd700;
  box-shadow: 0 2px 12px rgba(255, 215, 0, 0.3);
}

.input-field {
  flex: 1;
}

.input-field .el-input {
  border-radius: 16px;
}

.input-field .el-input__wrapper {
  border-radius: 6px;
  background: linear-gradient(135deg, #2a2a2a 0%, #333333 100%);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.3);
  border: 1px solid #555555;
  transition: all 0.2s ease;
  min-height: 40px;
}

.input-field .el-input__inner {
  color: #ffffff;
  background: transparent;
}

.input-field .el-input__inner::placeholder {
  color: #888888;
}

.input-field .el-input__wrapper:hover {
  box-shadow: 
    0 6px 20px rgba(0, 0, 0, 0.1),
    0 3px 6px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  border-color: rgba(0, 122, 255, 0.4);
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(255, 255, 255, 0.9) 100%);
}

.input-field .el-input__wrapper.is-focus {
  box-shadow: 
    0 8px 24px rgba(0, 122, 255, 0.2),
    0 4px 8px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border-color: #007aff;
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(248, 249, 250, 0.95) 100%);
}

.send-button {
  align-self: flex-end;
}

.send-button .el-button {
  border-radius: 6px;
  height: 40px;
  padding: 0 20px;
  font-weight: 500;
  background: linear-gradient(135deg, #007aff 0%, #0056cc 100%);
  border: 1px solid #007aff;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
  transition: all 0.2s ease;
}

.send-button .el-button:hover {
  background: linear-gradient(135deg, #0056cc 0%, #003d99 100%);
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.4);
  transform: translateY(-1px);
}

.send-button .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
  transition: left 0.6s ease;
}

.send-button .el-button:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 
    0 12px 32px rgba(0, 122, 255, 0.4),
    0 6px 12px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  background: 
    linear-gradient(135deg, rgba(0, 122, 255, 1) 0%, rgba(88, 86, 214, 1) 60%, rgba(175, 82, 222, 0.95) 100%);
}

.send-button .el-button:hover::before {
  left: 100%;
}

.send-button .el-button:active {
  transform: translateY(-1px) scale(0.98);
}

.emoji-picker {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  text-align: center;
  transition: background-color 0.2s;
}

.emoji-item:hover {
  background-color: #f0f0f0;
}

.file-preview {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.file-item {
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.file-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.image-preview {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #8e8e93;
  font-weight: 500;
}

.remove-file {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ff3b30;
  border-color: #ff3b30;
  color: white;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.remove-file:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 59, 48, 0.4);
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
}
</style>