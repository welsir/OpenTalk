import { defineStore } from 'pinia'
import type { Message, User } from '@/types'

export const useChatStore = defineStore('chat', {
    state: () => ({
        messages: {} as Record<number, Message[]>,
        selectedChat: null as User | null
    }),

    actions: {
        selectChat(user: User) {
            this.selectedChat = user
            if (!this.messages[user.id]) {
                this.messages[user.id] = []
            }
        },

        sendMessage(userId: number, senderName: string, content: string) {
            const message: Message = {
                id: Date.now(),
                senderId: userId,
                senderName,
                content,
                time: new Date().toLocaleTimeString('zh-CN', {
                    hour: '2-digit',
                    minute: '2-digit'
                })
            }

            if (!this.messages[userId]) {
                this.messages[userId] = []
            }
            this.messages[userId].push(message)
        },

        clearChat() {
            this.selectedChat = null
        }
    }
})