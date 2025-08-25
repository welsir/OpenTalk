<template>
  <div class="chat-area">
    <template v-if="currentChat && currentChat.isGroup">
      <!-- 聊天头部 -->
      <ChatHeader
          :chat="currentChat"
          @call="startAudioCall"
          @videoCall="startVideoCall"
          @menuAction="handleGroupMenuCommand"
      />

      <!-- 消息区域 -->
      <MessageList
          :messages="messages"
          :current-user-id="userStore.currentUser?.id"
          :typing-users="typingUsers"
          :show-sender-name="currentChat.isGroup"
          @reaction-click="handleReactionClick"
          @message-click="handleMessageClick"
          ref="messageListRef"
      />

      <!-- 输入区域 -->
      <MessageInput
          @sendMessage="handleSendMessage"
          @sendVoice="handleSendVoice"
          @sendFile="handleSendFile"
          @typing-start="handleTypingStart"
          @typing-stop="handleTypingStop"
      />
    </template>

    <!-- 空状态 -->
    <div v-else class="empty-chat">
      <div class="empty-content">
        <el-icon :size="80" color="#c0c4cc">
          <ChatLineRound />
        </el-icon>
        <h3 class="empty-title">选择一个聊天开始对话</h3>
        <p class="empty-description">从左侧选择好友或群组，开始愉快的聊天吧！</p>
        <div class="empty-tips">
          <div class="tip-item">
            <el-icon color="#409eff"><ChatLineRound /></el-icon>
            <span>💬 发送文字、图片、文件</span>
          </div>
          <div class="tip-item">
            <el-icon color="#67c23a"><ChatLineRound /></el-icon>
            <span>🎤 语音消息和通话</span>
          </div>
          <div class="tip-item">
            <el-icon color="#e6a23c"><ChatLineRound /></el-icon>
            <span>👥 创建群聊，多人协作</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ChatLineRound } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { useInteractionStore } from '@/stores/interaction'
import ChatHeader from '@/components/chat/ChatHeader.vue'
import MessageList from '@/components/chat/MessageList.vue'
import MessageInput from '@/components/chat/MessageInput.vue'
import { ElMessage } from 'element-plus'
import type { Message } from '@/types'

const chatStore = useChatStore()
const userStore = useUserStore()
const interactionStore = useInteractionStore()
const messageListRef = ref()

const currentChat = computed(() => chatStore.currentChat)

const messages = computed(() => {
  return chatStore.currentMessages
})

const typingUsers = computed(() => {
  if (!chatStore.selectedChatId) return []
  const userIds = chatStore.typingUsers[chatStore.selectedChatId] || []
  return userIds.map(userId => ({
    userId,
    userName: userStore.getUserById(userId)?.nickname || '未知用户'
  }))
})

const handleSendMessage = async (content: string, files?: any[]) => {
  if (!content.trim() && !files?.length) return
  
  try {
    // 发送文本消息
    if (content.trim()) {
      await chatStore.sendMessage(content, 'text')
    }
    
    // 发送文件消息
    if (files?.length) {
      for (const file of files) {
        const fileType = file.type.startsWith('image/') ? 'image' : 'file'
        await chatStore.sendMessage(file.name, fileType, file)
      }
    }
    
    // 发送后滚动到底部
    setTimeout(() => {
      messageListRef.value?.scrollToBottom()
    }, 100)
  } catch (error) {
    console.error('发送消息失败:', error)
  }
}

const handleSendVoice = async (audioBlob: Blob) => {
  try {
    await chatStore.sendMessage('语音消息', 'audio', audioBlob)
    setTimeout(() => {
      messageListRef.value?.scrollToBottom()
    }, 100)
  } catch (error) {
    console.error('发送语音消息失败:', error)
  }
}

const handleSendFile = async (files: any[]) => {
  try {
    for (const file of files) {
      const fileType = file.type.startsWith('image/') ? 'image' : 'file'
      await chatStore.sendMessage(file.name, fileType, file)
    }
    setTimeout(() => {
      messageListRef.value?.scrollToBottom()
    }, 100)
  } catch (error) {
    console.error('发送文件失败:', error)
  }
}

const startAudioCall = () => {
  // TODO: 开始语音通话
  console.log('开始语音通话')
}

const startVideoCall = () => {
  // TODO: 开始视频通话
  console.log('开始视频通话')
}

const handleGroupMenuCommand = (command: string) => {
  console.log('群聊菜单命令:', command)
  // TODO: 处理群聊菜单命令
}

const handleTypingStart = () => {
  if (chatStore.selectedChatId && userStore.currentUser) {
    interactionStore.setTypingStatus(chatStore.selectedChatId, userStore.currentUser.id, true)
  }
}

const handleTypingStop = () => {
  if (chatStore.selectedChatId && userStore.currentUser) {
    interactionStore.setTypingStatus(chatStore.selectedChatId, userStore.currentUser.id, false)
  }
}

const handleReactionClick = (messageId: string, emoji: string) => {
  if (!userStore.currentUser) return
  
  try {
    interactionStore.toggleReaction(messageId, userStore.currentUser.id, emoji)
    ElMessage.success('反应已添加')
  } catch (error) {
    ElMessage.error('添加反应失败')
    console.error('添加反应失败:', error)
  }
}

const handleMessageClick = (message: Message) => {
  console.log('消息被点击:', message)
  // TODO: 处理消息点击，比如显示消息详情、复制消息等
}
</script>

<style scoped>
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-gradient-light);
  position: relative;
  font-family: var(--font-primary);
  transition: all var(--duration-300) var(--ease-out);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-6);
  background: var(--glass-bg);
  border-bottom: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: all var(--duration-200) var(--ease-out);
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
  font-weight: var(--font-semibold);
  color: var(--color-neutral-900);
  margin-bottom: var(--space-1);
  font-size: var(--text-xl);
  font-family: var(--font-display);
  letter-spacing: -0.025em;
}

.chat-user-status {
  font-size: var(--text-sm);
  color: var(--color-neutral-500);
  font-weight: var(--font-medium);
  letter-spacing: 0.01em;
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

.empty-content {
  text-align: center;
  max-width: 400px;
  padding: 2rem;
}

.empty-title {
  margin: var(--space-6) 0 var(--space-2);
  color: var(--color-neutral-800);
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  font-family: var(--font-display);
  letter-spacing: -0.025em;
}

.empty-description {
  margin: 0 0 var(--space-8);
  color: var(--color-neutral-600);
  font-size: var(--text-base);
  line-height: var(--leading-relaxed);
  font-weight: var(--font-normal);
}

.empty-tips {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  text-align: left;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, #2a2a2a 0%, #333333 100%);
  border-radius: 8px;
  border: 1px solid #444444;
  transition: all 0.2s ease;
}

.tip-item:hover {
  background: linear-gradient(135deg, #333333 0%, #444444 100%);
  border-color: #555555;
  transform: translateY(-1px);
}

.tip-item span {
  color: #e5e5e7;
  font-size: 0.875rem;
}
</style>