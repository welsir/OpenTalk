import { defineStore } from 'pinia'
import type { User, Friend, FriendRequest, Notification } from '@/types'

// 模拟用户数据
const mockUsers: User[] = [
    {
        id: 1,
        username: 'alice',
        password: '123456',
        name: 'Alice Wang',
        avatar: '👩',
        status: 'online',
        bio: '热爱生活，享受编程',
        email: 'alice@example.com',
        phone: '13800138001',
        lastSeen: new Date().toISOString()
    },
    {
        id: 2,
        username: 'bob',
        password: '123456',
        name: 'Bob Chen',
        avatar: '👨',
        status: 'online',
        bio: '全栈开发者',
        email: 'bob@example.com',
        phone: '13800138002',
        lastSeen: new Date().toISOString()
    },
    {
        id: 3,
        username: 'charlie',
        password: '123456',
        name: 'Charlie Li',
        avatar: '🧑',
        status: 'offline',
        bio: '设计师',
        email: 'charlie@example.com',
        phone: '13800138003',
        lastSeen: new Date(Date.now() - 3600000).toISOString() // 1小时前
    },
    {
        id: 4,
        username: 'diana',
        password: '123456',
        name: 'Diana Zhang',
        avatar: '👩‍💼',
        status: 'away',
        bio: '产品经理',
        email: 'diana@example.com',
        phone: '13800138004',
        lastSeen: new Date(Date.now() - 1800000).toISOString() // 30分钟前
    },
    {
        id: 5,
        username: 'evan',
        password: '123456',
        name: 'Evan Liu',
        avatar: '👨‍💻',
        status: 'busy',
        bio: '前端工程师',
        email: 'evan@example.com',
        phone: '13800138005',
        lastSeen: new Date().toISOString()
    }
]

export const useUserStore = defineStore('user', {
    state: () => ({
        currentUser: null as User | null,
        allUsers: mockUsers,
        friends: [] as Friend[],
        friendRequests: [] as FriendRequest[],
        notifications: [] as Notification[],
        onlineUsers: [] as number[]
    }),

    getters: {
        isLoggedIn: (state) => !!state.currentUser,
        
        availableUsers: (state) => {
            if (!state.currentUser) return []
            return state.allUsers.filter(
                user => user.id !== state.currentUser?.id &&
                    !state.friends.find(f => f.id === user.id)
            )
        },
        
        onlineFriends: (state) => {
            return state.friends.filter(friend => 
                state.onlineUsers.includes(friend.id)
            )
        },
        
        pendingFriendRequests: (state) => {
            return state.friendRequests.filter(req => 
                req.status === 'pending' && req.toUser.id === state.currentUser?.id
            )
        },
        
        unreadNotifications: (state) => {
            return state.notifications.filter(notif => !notif.isRead)
        }
    },

    actions: {
        login(username: string, password: string) {
            const user = this.allUsers.find(
                u => u.username === username && u.password === password
            )
            if (user) {
                this.currentUser = { ...user }
                delete this.currentUser.password // 不在前端保存密码
                this.updateOnlineStatus(user.id, true)
                this.loadUserData()
                return true
            }
            return false
        },

        register(username: string, password: string, name: string, email?: string) {
            const existingUser = this.allUsers.find(u => u.username === username)
            if (existingUser) {
                return false
            }
            const newUser: User = {
                id: this.allUsers.length + 1,
                username,
                password,
                name,
                avatar: this.generateAvatar(name),
                status: 'online',
                bio: '新用户',
                email: email || '',
                lastSeen: new Date().toISOString()
            }
            this.allUsers.push(newUser)
            this.currentUser = { ...newUser }
            delete this.currentUser.password
            this.updateOnlineStatus(newUser.id, true)
            return true
        },

        logout() {
            if (this.currentUser) {
                this.updateOnlineStatus(this.currentUser.id, false)
            }
            this.currentUser = null
            this.friends = []
            this.friendRequests = []
            this.notifications = []
        },

        updateProfile(data: Partial<User>) {
            if (this.currentUser) {
                Object.assign(this.currentUser, data)
                // 同步更新到allUsers中
                const userIndex = this.allUsers.findIndex(u => u.id === this.currentUser?.id)
                if (userIndex !== -1) {
                    Object.assign(this.allUsers[userIndex], data)
                }
            }
        },

        updateOnlineStatus(userId: number, isOnline: boolean) {
            if (isOnline) {
                if (!this.onlineUsers.includes(userId)) {
                    this.onlineUsers.push(userId)
                }
            } else {
                const index = this.onlineUsers.indexOf(userId)
                if (index > -1) {
                    this.onlineUsers.splice(index, 1)
                }
            }
            
            // 更新用户状态
            const user = this.allUsers.find(u => u.id === userId)
            if (user) {
                user.status = isOnline ? 'online' : 'offline'
                user.lastSeen = new Date().toISOString()
            }
        },

        sendFriendRequest(toUserId: number, message: string = '') {
            if (!this.currentUser) return false
            
            const toUser = this.allUsers.find(u => u.id === toUserId)
            if (!toUser) return false
            
            // 检查是否已经是好友
            if (this.friends.find(f => f.id === toUserId)) return false
            
            // 检查是否已经发送过请求
            const existingRequest = this.friendRequests.find(
                req => req.fromUser.id === this.currentUser!.id && 
                       req.toUser.id === toUserId && 
                       req.status === 'pending'
            )
            if (existingRequest) return false
            
            const request: FriendRequest = {
                id: Date.now(),
                fromUser: this.currentUser,
                toUser,
                message,
                status: 'pending',
                createdAt: Date.now()
            }
            
            this.friendRequests.push(request)
            
            // 创建通知
            this.addNotification({
                id: Date.now() + 1,
                type: 'friend_request',
                title: '新的好友请求',
                content: `${this.currentUser.name} 想要添加你为好友`,
                data: { requestId: request.id },
                isRead: false,
                createdAt: Date.now()
            })
            
            return true
        },

        acceptFriendRequest(requestId: number) {
            const request = this.friendRequests.find(req => req.id === requestId)
            if (!request || request.status !== 'pending') return false
            
            request.status = 'accepted'
            
            // 添加到好友列表
            const friend: Friend = {
                ...request.fromUser,
                friendshipId: Date.now(),
                addedAt: Date.now(),
                unreadCount: 0
            }
            
            this.friends.push(friend)
            
            // 创建通知
            this.addNotification({
                id: Date.now(),
                type: 'friend_request',
                title: '好友请求已接受',
                content: `你和 ${request.fromUser.name} 现在是好友了`,
                isRead: false,
                createdAt: Date.now()
            })
            
            return true
        },

        rejectFriendRequest(requestId: number) {
            const request = this.friendRequests.find(req => req.id === requestId)
            if (!request || request.status !== 'pending') return false
            
            request.status = 'rejected'
            return true
        },

        removeFriend(friendId: number) {
            const index = this.friends.findIndex(f => f.id === friendId)
            if (index > -1) {
                this.friends.splice(index, 1)
                return true
            }
            return false
        },

        addNotification(notification: Notification) {
            this.notifications.unshift(notification)
            // 限制通知数量
            if (this.notifications.length > 100) {
                this.notifications = this.notifications.slice(0, 100)
            }
        },

        markNotificationAsRead(notificationId: number) {
            const notification = this.notifications.find(n => n.id === notificationId)
            if (notification) {
                notification.isRead = true
            }
        },

        markAllNotificationsAsRead() {
            this.notifications.forEach(n => n.isRead = true)
        },

        generateAvatar(name: string): string {
            const avatars = ['👤', '👨', '👩', '🧑', '👨‍💻', '👩‍💻', '👨‍💼', '👩‍💼', '🧑‍💼']
            const index = name.length % avatars.length
            return avatars[index]
        },

        loadUserData() {
            // 模拟加载用户数据
            if (this.currentUser) {
                // 初始化一些好友数据
                const initialFriends = this.allUsers
                    .filter(u => u.id !== this.currentUser.id)
                    .slice(0, 2)
                    .map(user => ({
                        ...user,
                        friendshipId: Date.now() + user.id,
                        addedAt: Date.now() - Math.random() * 86400000 * 30, // 30天内随机时间
                        unreadCount: Math.floor(Math.random() * 3)
                    }))
                
                this.friends = initialFriends
                
                // 设置一些用户在线
                this.onlineUsers = [1, 2, 5] // Alice, Bob, Evan 在线
            }
        },

        searchUsers(query: string) {
            if (!query.trim()) return []
            
            return this.allUsers.filter(user => 
                user.id !== this.currentUser?.id &&
                (user.name.toLowerCase().includes(query.toLowerCase()) ||
                 user.username.toLowerCase().includes(query.toLowerCase()) ||
                 user.bio.toLowerCase().includes(query.toLowerCase()))
            )
        }
    }
})