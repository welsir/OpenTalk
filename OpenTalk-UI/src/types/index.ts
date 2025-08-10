// 用户类型
export interface User {
    id: number
    username: string
    password: string
    name: string
    avatar: string
    status: 'online' | 'offline'
    bio: string
}

// 消息类型
export interface Message {
    id: number
    senderId: number
    senderName: string
    content: string
    time: string
}

// 房间类型
export interface Room {
    id: number
    name: string
    creator: string
    participants: number
    active: boolean
}

// 好友类型
export interface Friend extends User {
    lastMessage?: string
    unreadCount?: number
}