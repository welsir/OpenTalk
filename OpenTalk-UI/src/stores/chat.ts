import { defineStore } from 'pinia'
import type { Message, Chat, Group, MessageReaction } from '@/types'
import { useUserStore } from './user'

export const useChatStore = defineStore('chat', {
    state: () => ({
        chats: [] as Chat[],
        groups: [] as Group[],
        messages: {} as Record<string, Message[]>, // chatId -> messages
        selectedChatId: null as string | null,
        selectedChatType: 'private' as 'private' | 'group',
        typingUsers: {} as Record<string, number[]>, // chatId -> userIds
        onlineUsers: [] as number[]
    }),

    getters: {
        currentMessages: (state) => {
            return state.selectedChatId ? state.messages[state.selectedChatId] || [] : []
        },
        
        currentChat: (state) => {
            if (!state.selectedChatId) return null
            
            if (state.selectedChatType === 'private') {
                const chat = state.chats.find(chat => chat.id === state.selectedChatId)
                if (chat && chat.friendInfo) {
                    // 返回好友信息用于显示
                    return {
                        id: chat.id,
                        name: chat.friendInfo.name,
                        avatar: chat.friendInfo.avatar,
                        status: chat.friendInfo.status || 'offline',
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
        
        selectedChat: (state) => {
            // 兼容性 getter，与 currentChat 相同
            if (!state.selectedChatId) return null
            
            if (state.selectedChatType === 'private') {
                const chat = state.chats.find(chat => chat.id === state.selectedChatId)
                if (chat && chat.friendInfo) {
                    return {
                        id: chat.id,
                        name: chat.friendInfo.name,
                        avatar: chat.friendInfo.avatar,
                        status: chat.friendInfo.status || 'offline',
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
        initializeChats() {
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            // 初始化私聊
            this.chats = userStore.friends.map(friend => ({
                id: `private_${Math.min(userStore.currentUser!.id, friend.id)}_${Math.max(userStore.currentUser!.id, friend.id)}`,
                participants: [userStore.currentUser!.id, friend.id],
                lastMessage: null,
                unreadCount: friend.unreadCount || 0,
                createdAt: friend.addedAt,
                updatedAt: Date.now(),
                // 添加好友信息用于显示
                friendInfo: friend
            }))
            
            // 初始化一些示例群组
            this.groups = [
                {
                    id: 'group_1',
                    name: '工作群',
                    description: '日常工作交流',
                    avatar: '💼',
                    participants: [userStore.currentUser.id, 1, 2, 3],
                    admins: [userStore.currentUser.id],
                    owner: userStore.currentUser.id,
                    settings: {
                        allowInvite: true,
                        muteAll: false,
                        showMemberCount: true
                    },
                    unreadCount: 0,
                    createdAt: Date.now() - 86400000 * 7, // 7天前
                    updatedAt: Date.now()
                },
                {
                    id: 'group_2',
                    name: '朋友圈',
                    description: '朋友们的日常分享',
                    avatar: '👥',
                    participants: [userStore.currentUser.id, 2, 4, 5],
                    admins: [2],
                    owner: 2,
                    settings: {
                        allowInvite: false,
                        muteAll: false,
                        showMemberCount: true
                    },
                    unreadCount: 2,
                    createdAt: Date.now() - 86400000 * 3, // 3天前
                    updatedAt: Date.now()
                }
            ]
            
            // 初始化一些示例消息
            this.initializeSampleMessages()
        },
        
        initializeSampleMessages() {
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            // 为第一个私聊添加示例消息
            if (this.chats.length > 0) {
                const firstChatId = this.chats[0].id
                this.messages[firstChatId] = [
                    {
                        id: 'msg_1',
                        senderId: this.chats[0].participants.find(id => id !== userStore.currentUser!.id)!,
                        content: '你好！最近怎么样？',
                        timestamp: Date.now() - 3600000, // 1小时前
                        type: 'text',
                        chatId: firstChatId,
                        status: 'read'
                    },
                    {
                        id: 'msg_2',
                        senderId: userStore.currentUser.id,
                        content: '还不错，你呢？工作忙吗？',
                        timestamp: Date.now() - 3500000,
                        type: 'text',
                        chatId: firstChatId,
                        status: 'read'
                    }
                ]
            }
            
            // 为群聊添加示例消息
            if (this.groups.length > 0) {
                const firstGroupId = this.groups[0].id
                this.messages[firstGroupId] = [
                    {
                        id: 'msg_group_1',
                        senderId: 1,
                        content: '大家好，今天的会议改到下午3点',
                        timestamp: Date.now() - 7200000, // 2小时前
                        type: 'text',
                        chatId: firstGroupId,
                        status: 'read'
                    },
                    {
                        id: 'msg_group_2',
                        senderId: 2,
                        content: '收到，我会准时参加的',
                        timestamp: Date.now() - 7000000,
                        type: 'text',
                        chatId: firstGroupId,
                        status: 'read'
                    }
                ]
            }
        },

        selectChat(chatId: string, type: 'private' | 'group' = 'private') {
            this.selectedChatId = chatId
            this.selectedChatType = type
            
            if (!this.messages[chatId]) {
                this.messages[chatId] = []
            }
            
            // 标记消息为已读
            this.markChatAsRead(chatId, type)
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
            const messages = this.messages[chatId] || []
            messages.forEach(msg => {
                if (msg.status === 'delivered') {
                    msg.status = 'read'
                }
            })
        },

        sendMessage(content: string, type: 'text' | 'image' | 'file' | 'audio' = 'text', fileData?: any) {
            const userStore = useUserStore()
            if (!this.selectedChatId || !userStore.currentUser) return

            const message: Message = {
                id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                senderId: userStore.currentUser.id,
                content,
                timestamp: Date.now(),
                type,
                chatId: this.selectedChatId,
                status: 'sent',
                fileData
            }

            if (!this.messages[this.selectedChatId]) {
                this.messages[this.selectedChatId] = []
            }
            
            this.messages[this.selectedChatId].push(message)
            
            // 更新聊天的最后消息时间
            this.updateChatLastMessage(this.selectedChatId, message)
            
            // 模拟消息状态更新
            setTimeout(() => {
                message.status = 'delivered'
            }, 1000)
            
            setTimeout(() => {
                message.status = 'read'
            }, 3000)
        },
        
        updateChatLastMessage(chatId: string, message: Message) {
            const chat = this.chats.find(c => c.id === chatId)
            const group = this.groups.find(g => g.id === chatId)
            
            if (chat) {
                chat.lastMessage = message
                chat.updatedAt = message.timestamp
            } else if (group) {
                group.lastMessage = message
                group.updatedAt = message.timestamp
            }
        },
        
        deleteMessage(messageId: string) {
            if (!this.selectedChatId) return
            
            const messages = this.messages[this.selectedChatId]
            const index = messages.findIndex(msg => msg.id === messageId)
            
            if (index > -1) {
                messages.splice(index, 1)
            }
        },
        
        addReaction(messageId: string, emoji: string) {
            const userStore = useUserStore()
            if (!this.selectedChatId || !userStore.currentUser) return
            
            const messages = this.messages[this.selectedChatId]
            const message = messages.find(msg => msg.id === messageId)
            
            if (message) {
                if (!message.reactions) {
                    message.reactions = []
                }
                
                const existingReaction = message.reactions.find(
                    r => r.emoji === emoji && r.userId === userStore.currentUser!.id
                )
                
                if (existingReaction) {
                    // 移除反应
                    message.reactions = message.reactions.filter(r => r !== existingReaction)
                } else {
                    // 添加反应
                    message.reactions.push({
                        emoji,
                        userId: userStore.currentUser.id,
                        timestamp: Date.now()
                    })
                }
            }
        },
        
        createGroup(name: string, description: string, participantIds: number[]) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return null
            
            const group: Group = {
                id: `group_${Date.now()}`,
                name,
                description,
                avatar: '👥',
                participants: [userStore.currentUser.id, ...participantIds],
                admins: [userStore.currentUser.id],
                owner: userStore.currentUser.id,
                settings: {
                    allowInvite: true,
                    muteAll: false,
                    showMemberCount: true
                },
                unreadCount: 0,
                createdAt: Date.now(),
                updatedAt: Date.now()
            }
            
            this.groups.push(group)
            this.messages[group.id] = []
            
            return group
        },
        
        joinGroup(groupId: string) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return false
            
            const group = this.groups.find(g => g.id === groupId)
            if (group && !group.participants.includes(userStore.currentUser.id)) {
                group.participants.push(userStore.currentUser.id)
                return true
            }
            
            return false
        },
        
        leaveGroup(groupId: string) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return false
            
            const group = this.groups.find(g => g.id === groupId)
            if (group) {
                const index = group.participants.indexOf(userStore.currentUser.id)
                if (index > -1) {
                    group.participants.splice(index, 1)
                    
                    // 如果是群主离开，转让群主
                    if (group.owner === userStore.currentUser.id && group.participants.length > 0) {
                        group.owner = group.participants[0]
                        if (!group.admins.includes(group.owner)) {
                            group.admins.push(group.owner)
                        }
                    }
                    
                    return true
                }
            }
            
            return false
        },
        
        setTyping(chatId: string, isTyping: boolean) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return
            
            if (!this.typingUsers[chatId]) {
                this.typingUsers[chatId] = []
            }
            
            const typingList = this.typingUsers[chatId]
            const userIndex = typingList.indexOf(userStore.currentUser.id)
            
            if (isTyping && userIndex === -1) {
                typingList.push(userStore.currentUser.id)
            } else if (!isTyping && userIndex > -1) {
                typingList.splice(userIndex, 1)
            }
        },
        
        clearChat(chatId: string) {
            if (this.messages[chatId]) {
                this.messages[chatId] = []
            }
        },
        
        searchMessages(query: string, chatId?: string) {
            const searchIn = chatId ? [chatId] : Object.keys(this.messages)
            const results: Message[] = []
            
            searchIn.forEach(id => {
                const messages = this.messages[id] || []
                const matches = messages.filter(msg => 
                    msg.content.toLowerCase().includes(query.toLowerCase())
                )
                results.push(...matches)
            })
            
            return results.sort((a, b) => b.timestamp - a.timestamp)
        }
    }
})