import { defineStore } from 'pinia'
import type { User } from '@/types'

// 模拟用户数据
const mockUsers: User[] = [
    {
        id: 1,
        username: 'alice',
        password: '123456',
        name: 'Alice Wang',
        avatar: '👩',
        status: 'online',
        bio: '热爱生活，享受编程'
    },
    {
        id: 2,
        username: 'bob',
        password: '123456',
        name: 'Bob Chen',
        avatar: '👨',
        status: 'online',
        bio: '全栈开发者'
    },
    {
        id: 3,
        username: 'charlie',
        password: '123456',
        name: 'Charlie Li',
        avatar: '🧑',
        status: 'offline',
        bio: '设计师'
    }
]

export const useUserStore = defineStore('user', {
    state: () => ({
        currentUser: null as User | null,
        allUsers: mockUsers,
        friends: [] as User[]
    }),

    getters: {
        isLoggedIn: (state) => !!state.currentUser,
        availableUsers: (state) => {
            if (!state.currentUser) return []
            return state.allUsers.filter(
                user => user.id !== state.currentUser?.id &&
                    !state.friends.find(f => f.id === user.id)
            )
        }
    },

    actions: {
        login(username: string, password: string) {
            const user = this.allUsers.find(
                u => u.username === username && u.password === password
            )
            if (user) {
                this.currentUser = user
                return true
            }
            return false
        },

        register(username: string, password: string, name: string) {
            const newUser: User = {
                id: this.allUsers.length + 1,
                username,
                password,
                name,
                avatar: '🤖',
                status: 'online',
                bio: '新用户'
            }
            this.allUsers.push(newUser)
            this.currentUser = newUser
            return true
        },

        logout() {
            this.currentUser = null
            this.friends = []
        },

        updateProfile(data: Partial<User>) {
            if (this.currentUser) {
                this.currentUser = { ...this.currentUser, ...data }
            }
        },

        addFriend(userId: number) {
            const user = this.allUsers.find(u => u.id === userId)
            if (user && !this.friends.find(f => f.id === userId)) {
                this.friends.push(user)
                return true
            }
            return false
        }
    }
})