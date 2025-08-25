import { defineStore } from 'pinia'
import type { Message, Chat, Group, MessageReaction } from '@/types'
import { useUserStore } from './user'
import { chatService } from '@/services/chat.service.enhanced'

export const useChatStore = defineStore('chat', {
    state: () => ({
        chats: JSON.parse(localStorage.getItem('chat_chats') || '[]') as Chat[],
        groups: JSON.parse(localStorage.getItem('chat_groups') || '[]') as Group[],
        messages: JSON.parse(localStorage.getItem('chat_messages') || '{}') as Record<string, Message[]>, // chatId -> messages
        selectedChatId: localStorage.getItem('chat_selectedChatId') as string | null,
        selectedChatType: (localStorage.getItem('chat_selectedChatType') || 'private') as 'private' | 'group',
        typingUsers: {} as Record<string, number[]>, // chatId -> userIds
        onlineUsers: [] as string[]
    }),

    getters: {
        currentMessages: (state) => {
            return state.selectedChatId ? state.messages[state.selectedChatId] || [] : []
        },
        
        currentChat: (state) => {
            if (!state.selectedChatId) return null
            
            if (state.selectedChatType === 'private') {
                const chat = state.chats.find(chat => chat.id === state.selectedChatId)
                if (chat) {
                    return {
                        id: chat.id,
                        name: chat.name,
                        avatar: chat.avatar,
                        status: 'online',
                        isGroup: false
                    }
                }
                return null
            } else {
                const group = state.groups.find(group => group.id === state.selectedChatId)
                if (group) {
                    return {
                        id: group.id,
                        name: group.name,
                        avatar: group.avatar,
                        status: 'online',
                        isGroup: true
                    }
                }
                return null
            }
        },
        
        sortedChats: (state) => {
            const allChats = [
                ...state.chats.map(chat => ({ ...chat, type: 'private' as const })),
                ...state.groups.map(group => ({ ...group, type: 'group' as const }))
            ]
            
            return allChats.sort((a, b) => {
                const aLastMessage = state.messages[a.id]?.slice(-1)[0]
                const bLastMessage = state.messages[b.id]?.slice(-1)[0]
                
                if (!aLastMessage && !bLastMessage) return 0
                if (!aLastMessage) return 1
                if (!bLastMessage) return -1
                
                return bLastMessage.timestamp - aLastMessage.timestamp
            })
        },
        
        unreadChatsCount: (state) => {
            return [...state.chats, ...state.groups].reduce((count, chat) => {
                return count + (chat.unreadCount || 0)
            }, 0)
        }
    },

    actions: {
        // 保存数据到localStorage
        saveToStorage() {
            try {
                localStorage.setItem('chat_chats', JSON.stringify(this.chats))
                localStorage.setItem('chat_groups', JSON.stringify(this.groups))
                localStorage.setItem('chat_messages', JSON.stringify(this.messages))
                localStorage.setItem('chat_selectedChatId', this.selectedChatId || '')
                localStorage.setItem('chat_selectedChatType', this.selectedChatType)
            } catch (error) {
                console.warn('保存聊天数据到本地存储失败:', error)
            }
        },
        
        // 清除本地存储
        clearStorage() {
            try {
                localStorage.removeItem('chat_chats')
                localStorage.removeItem('chat_groups')
                localStorage.removeItem('chat_messages')
                localStorage.removeItem('chat_selectedChatId')
                localStorage.removeItem('chat_selectedChatType')
            } catch (error) {
                console.warn('清除聊天数据本地存储失败:', error)
            }
        },
        
        async initializeChats() {
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            // 如果本地已有数据且不为空，直接使用
            if (this.chats.length > 0 || this.groups.length > 0) {
                return
            }
            
            try {
                // 使用增强的聊天服务获取聊天列表
                const [chats, groups] = await Promise.all([
                    chatService.getChats(userStore.currentUser.id),
                    chatService.getGroups(userStore.currentUser.id)
                ])
                
                this.chats = chats
                this.groups = groups
                
                // 初始化示例消息
                this.initializeSampleMessages()
                
                // 保存到本地存储
                this.saveToStorage()
            } catch (error) {
                console.error('初始化聊天失败:', error)
                // 如果服务失败，使用本地数据
                this.initializeLocalChats()
            }
        },
        
        initializeLocalChats() {
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            // 基于好友列表创建私聊
            this.chats = userStore.friends.map(friend => ({
                id: `private_${userStore.currentUser!.id}_${friend.id}`,
                type: 'private' as const,
                name: friend.nickname,
                avatar: friend.avatar || '/default-avatar.png',
                participants: [
                    userStore.currentUser!,
                    {
                        id: friend.id,
                        username: friend.nickname,
                        nickname: friend.nickname,
                        avatar: friend.avatar,
                        status: friend.status
                    }
                ],
                lastMessage: undefined,
                unreadCount: 0,
                isOnline: friend.status === 'online',
                createdAt: Date.now(),
                updatedAt: Date.now()
            }))
            
            // 创建示例群组
            this.groups = [
                {
                    id: 'group_1',
                    type: 'group' as const,
                    name: '开发团队',
                    avatar: '/group-avatar.png',
                    description: '项目开发讨论群',
                    creator: userStore.currentUser.id,
                    admins: [userStore.currentUser.id],
                    participants: [
                        userStore.currentUser,
                        ...userStore.friends.slice(0, 3).map(friend => ({
                            id: friend.id,
                            username: friend.nickname,
                            nickname: friend.nickname,
                            avatar: friend.avatar,
                            status: friend.status
                        }))
                    ],
                    settings: {
                        allowInvite: true,
                        allowMemberAdd: true,
                        muteAll: false,
                        showMemberList: true
                    },
                    lastMessage: undefined,
                    unreadCount: 0,
                    createdAt: Date.now(),
                    updatedAt: Date.now()
                }
            ]
            
            this.initializeSampleMessages()
            
            // 保存到本地存储
            this.saveToStorage()
        },
        
        initializeSampleMessages() {
            const userStore = useUserStore()
            const currentUserId = userStore.currentUser?.id
            
            // 为每个私聊添加示例消息
            this.chats.forEach((chat, index) => {
                const otherUser = chat.participants.find(p => p.id !== currentUserId)
                if (!otherUser) return
                
                this.messages[chat.id] = [
                    {
                        id: `msg_${chat.id}_1`,
                        senderId: otherUser.id,
                        senderName: otherUser.nickname,
                        senderAvatar: otherUser.avatar || '/default-avatar.png',
                        content: '你好！最近怎么样？',
                        type: 'text',
                        timestamp: Date.now() - 7200000 - (index * 300000),
                        chatId: chat.id,
                        chatType: 'private',
                        isRead: true
                    },
                    {
                        id: `msg_${chat.id}_2`,
                        senderId: currentUserId!,
                        senderName: userStore.currentUser!.nickname,
                        senderAvatar: userStore.currentUser!.avatar || '/default-avatar.png',
                        content: '还不错，你呢？',
                        type: 'text',
                        timestamp: Date.now() - 7000000 - (index * 300000),
                        chatId: chat.id,
                        chatType: 'private',
                        isRead: true
                    },
                    {
                        id: `msg_${chat.id}_3`,
                        senderId: otherUser.id,
                        senderName: otherUser.nickname,
                        senderAvatar: otherUser.avatar || '/default-avatar.png',
                        content: '挺好的，有空一起出来聊聊吧！',
                        type: 'text',
                        timestamp: Date.now() - 6800000 - (index * 300000),
                        chatId: chat.id,
                        chatType: 'private',
                        isRead: true
                    }
                ]
            })
            
            // 为每个群聊添加示例消息
            this.groups.forEach((group, index) => {
                const participants = group.participants.filter(p => p.id !== currentUserId)
                
                this.messages[group.id] = [
                    {
                        id: `msg_${group.id}_1`,
                        senderId: participants[0]?.id || 'user1',
                        senderName: participants[0]?.nickname || '用户1',
                        senderAvatar: participants[0]?.avatar || '/default-avatar.png',
                        content: '大家好！欢迎加入群聊',
                        type: 'text',
                        timestamp: Date.now() - 3600000 - (index * 200000),
                        chatId: group.id,
                        chatType: 'group',
                        isRead: true
                    },
                    {
                        id: `msg_${group.id}_2`,
                        senderId: participants[1]?.id || 'user2',
                        senderName: participants[1]?.nickname || '用户2',
                        senderAvatar: participants[1]?.avatar || '/default-avatar.png',
                        content: '谢谢！很高兴认识大家',
                        type: 'text',
                        timestamp: Date.now() - 3400000 - (index * 200000),
                        chatId: group.id,
                        chatType: 'group',
                        isRead: true
                    },
                    {
                        id: `msg_${group.id}_3`,
                        senderId: currentUserId!,
                        senderName: userStore.currentUser!.nickname,
                        senderAvatar: userStore.currentUser!.avatar || '/default-avatar.png',
                        content: '大家好，请多多指教！',
                        type: 'text',
                        timestamp: Date.now() - 3200000 - (index * 200000),
                        chatId: group.id,
                        chatType: 'group',
                        isRead: true
                    }
                ]
            })
        },

        selectChat(chatId: string, type: 'private' | 'group' = 'private') {
            this.selectedChatId = chatId
            this.selectedChatType = type
            
            // 标记为已读
            this.markChatAsRead(chatId, type)
            
            // 加载消息
            this.loadMessages(chatId)
            
            // 保存选择状态到本地存储
            this.saveToStorage()
        },
        
        async loadMessages(chatId: string) {
            try {
                const messages = await chatService.getMessages(chatId)
                this.messages[chatId] = messages
            } catch (error) {
                console.error('加载消息失败:', error)
                // 如果没有消息，保持现有的示例消息
            }
        },
        
        markChatAsRead(chatId: string, type: 'private' | 'group') {
            if (type === 'private') {
                const chat = this.chats.find(c => c.id === chatId)
                if (chat) {
                    chat.unreadCount = 0
                }
            } else {
                const group = this.groups.find(g => g.id === chatId)
                if (group) {
                    group.unreadCount = 0
                }
            }
            
            // 标记消息为已读
            if (this.messages[chatId]) {
                this.messages[chatId].forEach(message => {
                    message.isRead = true
                })
            }
        },

        async sendMessage(content: string, type: 'text' | 'image' | 'file' | 'audio' = 'text', fileData?: any) {
            if (!this.selectedChatId) return
            
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            try {
                const message = await chatService.sendMessage({
                    chatId: this.selectedChatId,
                    content,
                    type,
                    fileData
                })
                
                // 添加到本地消息列表
                if (!this.messages[this.selectedChatId]) {
                    this.messages[this.selectedChatId] = []
                }
                this.messages[this.selectedChatId].push(message)
                
                // 更新聊天的最后消息
                this.updateChatLastMessage(this.selectedChatId, message)
                
                // 保存到本地存储
                this.saveToStorage()
            } catch (error) {
                console.error('发送消息失败:', error)
                // 创建本地消息作为fallback
                const localMessage: Message = {
                    id: `msg_${Date.now()}`,
                    senderId: userStore.currentUser.id,
                    senderName: userStore.currentUser.nickname,
                    senderAvatar: userStore.currentUser.avatar || '/default-avatar.png',
                    content,
                    type,
                    timestamp: Date.now(),
                    chatId: this.selectedChatId,
                    chatType: this.selectedChatType,
                    isRead: false
                }
                
                if (!this.messages[this.selectedChatId]) {
                    this.messages[this.selectedChatId] = []
                }
                this.messages[this.selectedChatId].push(localMessage)
                this.updateChatLastMessage(this.selectedChatId, localMessage)
                
                // 保存到本地存储
                this.saveToStorage()
            }
        },
        
        updateChatLastMessage(chatId: string, message: Message) {
            const chat = this.chats.find(c => c.id === chatId)
            const group = this.groups.find(g => g.id === chatId)
            
            if (chat) {
                chat.lastMessage = message
                chat.updatedAt = Date.now()
            } else if (group) {
                group.lastMessage = message
                group.updatedAt = Date.now()
            }
        },
        
        async deleteMessage(messageId: string) {
            try {
                await chatService.deleteMessage(messageId)
                
                // 从本地消息列表中删除
                Object.keys(this.messages).forEach(chatId => {
                    this.messages[chatId] = this.messages[chatId].filter(msg => msg.id !== messageId)
                })
            } catch (error) {
                console.error('删除消息失败:', error)
            }
        },
        
        async addReaction(messageId: string, emoji: string) {
            try {
                const reaction = await chatService.addReaction(messageId, emoji)
                
                // 更新本地消息的反应
                Object.keys(this.messages).forEach(chatId => {
                    const message = this.messages[chatId].find(msg => msg.id === messageId)
                    if (message) {
                        if (!message.reactions) {
                            message.reactions = []
                        }
                        
                        // 检查是否已经有相同的反应
                        const existingReaction = message.reactions.find(r => r.emoji === emoji && r.userId === reaction.userId)
                        if (!existingReaction) {
                            message.reactions.push(reaction)
                        }
                    }
                })
            } catch (error) {
                console.error('添加反应失败:', error)
            }
        },
        
        async createGroup(name: string, description: string, participantIds: string[]) {
            try {
                const group = await chatService.createGroup({
                    name,
                    description,
                    participantIds
                })
                
                this.groups.push(group)
                
                // 保存到本地存储
                this.saveToStorage()
                
                return group
            } catch (error) {
                console.error('创建群组失败:', error)
                throw error
            }
        },
        
        async joinGroup(groupId: string) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            try {
                await chatService.joinGroup(groupId, userStore.currentUser.id)
                
                // 重新加载群组列表
                const groups = await chatService.getGroups(userStore.currentUser.id)
                this.groups = groups
                
                // 保存到本地存储
                this.saveToStorage()
            } catch (error) {
                console.error('加入群组失败:', error)
            }
        },
        
        async leaveGroup(groupId: string) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            try {
                await chatService.leaveGroup(groupId, userStore.currentUser.id)
                
                // 从本地群组列表中移除
                this.groups = this.groups.filter(g => g.id !== groupId)
                
                // 清理消息
                delete this.messages[groupId]
                
                // 如果当前选中的是这个群组，清除选择
                if (this.selectedChatId === groupId) {
                    this.selectedChatId = null
                }
                
                // 保存到本地存储
                this.saveToStorage()
            } catch (error) {
                console.error('离开群组失败:', error)
            }
        },
        
        async setTyping(chatId: string, isTyping: boolean) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            try {
                await chatService.setTyping(chatId, userStore.currentUser.id, isTyping)
            } catch (error) {
                console.error('设置打字状态失败:', error)
            }
        },
        
        async clearChat(chatId: string) {
            try {
                await chatService.clearChat(chatId)
                this.messages[chatId] = []
                
                // 保存到本地存储
                this.saveToStorage()
            } catch (error) {
                console.error('清空聊天失败:', error)
            }
        },
        
        async searchMessages(query: string, chatId?: string) {
            try {
                const messages = await chatService.searchMessages(query, chatId)
                return messages
            } catch (error) {
                console.error('搜索消息失败:', error)
                return []
            }
        }
    }
})