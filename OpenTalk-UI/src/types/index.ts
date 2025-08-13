// 用户类型
export interface User {
    id: number
    username: string
    password?: string
    name: string
    avatar: string
    status: 'online' | 'offline' | 'away' | 'busy'
    bio: string
    email?: string
    phone?: string
    lastSeen?: string
    isTyping?: boolean
}

// 消息类型
export interface Message {
    id: number
    senderId: number
    senderName: string
    senderAvatar: string
    content: string
    type: 'text' | 'image' | 'file' | 'video' | 'audio' | 'system'
    timestamp: number
    chatId: number
    chatType: 'private' | 'group'
    isRead?: boolean
    replyTo?: number
    reactions?: MessageReaction[]
}

// 消息反应
export interface MessageReaction {
    emoji: string
    userId: number
    userName: string
}

// 聊天会话类型
export interface Chat {
    id: number
    type: 'private' | 'group'
    name: string
    avatar: string
    participants: User[]
    lastMessage?: Message
    unreadCount: number
    isOnline?: boolean
    isTyping?: boolean
    createdAt: number
    updatedAt: number
}

// 群组类型
export interface Group extends Chat {
    description: string
    creator: number
    admins: number[]
    settings: GroupSettings
}

// 群组设置
export interface GroupSettings {
    allowInvite: boolean
    allowMemberAdd: boolean
    muteAll: boolean
    showMemberList: boolean
}

// 房间类型（音视频通话）
export interface Room {
    id: number
    name: string
    type: 'video' | 'audio'
    creator: number
    participants: RoomParticipant[]
    maxParticipants: number
    isLocked: boolean
    password?: string
    createdAt: number
    status: 'waiting' | 'active' | 'ended'
}

// 房间参与者
export interface RoomParticipant {
    user: User
    isVideoOn: boolean
    isMicOn: boolean
    isScreenSharing: boolean
    joinedAt: number
    role: 'host' | 'participant'
}

// 好友类型
export interface Friend extends User {
    friendshipId: number
    addedAt: number
    lastMessage?: Message
    unreadCount: number
    chatId?: number
}

// 好友请求
export interface FriendRequest {
    id: number
    fromUser: User
    toUser: User
    message: string
    status: 'pending' | 'accepted' | 'rejected'
    createdAt: number
}

// 通知类型
export interface Notification {
    id: number
    type: 'friend_request' | 'message' | 'group_invite' | 'system'
    title: string
    content: string
    data?: any
    isRead: boolean
    createdAt: number
}

// API响应类型
export interface ApiResponse<T = any> {
    success: boolean
    data?: T
    message?: string
    code?: number
}

// 文件上传类型
export interface FileUpload {
    id: number
    name: string
    size: number
    type: string
    url: string
    uploadedAt: number
}