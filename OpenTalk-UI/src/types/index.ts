// 用户类型
export interface User {
    id: string
    username: string
    password?: string
    nickname: string
    avatar?: string
    status: 'online' | 'offline' | 'away' | 'busy'
    email?: string
    phone?: string
    lastSeen?: string
    isTyping?: boolean
}

// 消息类型
export interface Message {
    id: string
    senderId: string
    senderName: string
    senderAvatar: string
    content: string
    type: 'text' | 'image' | 'file' | 'video' | 'audio' | 'system'
    timestamp: number
    chatId: string
    chatType: 'private' | 'group'
    isRead?: boolean
    replyTo?: string
    reactions?: MessageReaction[]
}

// 消息反应
export interface MessageReaction {
    emoji: string
    userId: string
    userName: string
}

// 聊天会话类型
export interface Chat {
    id: string
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
    creator: string
    admins: string[]
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
    id: string
    name: string
    type: 'video' | 'audio'
    creator: string
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
    friendshipId: string
    addedAt: number
    lastMessage?: Message
    unreadCount: number
    chatId?: string
}

// 好友请求
export interface FriendRequest {
    id: string
    fromUser: User
    toUser: User
    message: string
    status: 'pending' | 'accepted' | 'rejected'
    createdAt: number
}

// 通知类型
export interface Notification {
    id: string
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
    id: string
    name: string
    size: number
    type: string
    url: string
    uploadedAt: number
}