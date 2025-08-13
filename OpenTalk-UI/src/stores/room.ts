import { defineStore } from 'pinia'
import type { Room, RoomParticipant } from '@/types'
import { useUserStore } from './user'

export const useRoomStore = defineStore('room', {
    state: () => ({
        rooms: [] as Room[],
        currentRoom: null as Room | null,
        selectedRoom: null as Room | null,
        localStream: null as MediaStream | null,
        remoteStreams: {} as Record<number, MediaStream>,
        isVideoEnabled: false,
        isMicEnabled: false,
        isScreenSharing: false,
        connectionStatus: 'disconnected' as 'connecting' | 'connected' | 'disconnected' | 'failed',
        participants: [] as RoomParticipant[],
        isInCall: false,
        audioEnabled: true,
        videoEnabled: true,
        screenShareSupported: true,
        callParticipants: [] as any[],
        roomSettings: {
            maxParticipants: 8,
            allowScreenShare: true,
            recordSession: false,
            muteOnJoin: true
        }
    }),

    getters: {
        isInRoom: (state) => state.currentRoom !== null,
        
        activeParticipants: (state) => {
            return state.participants.filter(p => p.status === 'connected')
        },
        
        participantCount: (state) => {
            return state.participants.length
        },
        
        canJoinRoom: (state) => {
            return state.participantCount < state.roomSettings.maxParticipants
        },
        
        currentUserParticipant: (state) => {
            const userStore = useUserStore()
            return state.participants.find(p => p.userId === userStore.currentUser?.id)
        }
    },

    actions: {
        async initializeMedia() {
            try {
                const stream = await navigator.mediaDevices.getUserMedia({
                    video: true,
                    audio: true
                })
                this.localStream = stream
                this.isVideoEnabled = true
                this.isMicEnabled = !this.roomSettings.muteOnJoin
                
                // 如果设置为加入时静音，则静音麦克风
                if (this.roomSettings.muteOnJoin) {
                    this.toggleMic()
                }
                
                return stream
            } catch (error) {
                console.error('Failed to initialize media:', error)
                throw error
            }
        },
        
        async getScreenShare() {
            try {
                const stream = await navigator.mediaDevices.getDisplayMedia({
                    video: true,
                    audio: true
                })
                return stream
            } catch (error) {
                console.error('Failed to get screen share:', error)
                throw error
            }
        },

        createRoom(name: string, description: string, isPrivate: boolean = false) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return null
            
            const room: Room = {
                id: `room_${Date.now()}`,
                name,
                description,
                hostId: userStore.currentUser.id,
                participants: [],
                maxParticipants: this.roomSettings.maxParticipants,
                isPrivate,
                settings: {
                    allowScreenShare: this.roomSettings.allowScreenShare,
                    recordSession: this.roomSettings.recordSession,
                    muteOnJoin: this.roomSettings.muteOnJoin,
                    requireApproval: isPrivate
                },
                createdAt: Date.now(),
                isActive: true
            }
            
            this.rooms.push(room)
            return room
        },

        async joinRoom(roomId: string) {
            const userStore = useUserStore()
            if (!userStore.currentUser) return false
            
            const room = this.rooms.find(r => r.id === roomId)
            if (!room || !this.canJoinRoom) return false
            
            try {
                this.connectionStatus = 'connecting'
                
                // 初始化媒体设备
                await this.initializeMedia()
                
                // 创建参与者信息
                const participant: RoomParticipant = {
                    userId: userStore.currentUser.id,
                    name: userStore.currentUser.name,
                    avatar: userStore.currentUser.avatar,
                    joinedAt: Date.now(),
                    isVideoEnabled: this.isVideoEnabled,
                    isMicEnabled: this.isMicEnabled,
                    isScreenSharing: false,
                    status: 'connected',
                    role: room.hostId === userStore.currentUser.id ? 'host' : 'participant'
                }
                
                // 添加到房间参与者列表
                room.participants.push(participant)
                this.participants.push(participant)
                
                this.currentRoom = room
                this.connectionStatus = 'connected'
                
                return true
            } catch (error) {
                console.error('Failed to join room:', error)
                this.connectionStatus = 'failed'
                return false
            }
        },

        leaveRoom(roomId?: string) {
            const userStore = useUserStore()
            if (!userStore.currentUser || !this.currentRoom) return
            
            // 停止本地媒体流
            if (this.localStream) {
                this.localStream.getTracks().forEach(track => track.stop())
                this.localStream = null
            }
            
            // 停止屏幕共享
            if (this.isScreenSharing) {
                this.stopScreenShare()
            }
            
            // 从房间中移除参与者
            const room = this.currentRoom
            const participantIndex = room.participants.findIndex(
                p => p.userId === userStore.currentUser!.id
            )
            
            if (participantIndex > -1) {
                room.participants.splice(participantIndex, 1)
            }
            
            // 从本地参与者列表中移除
            const localParticipantIndex = this.participants.findIndex(
                p => p.userId === userStore.currentUser!.id
            )
            
            if (localParticipantIndex > -1) {
                this.participants.splice(localParticipantIndex, 1)
            }
            
            // 如果是房主离开且还有其他参与者，转让房主
            if (room.hostId === userStore.currentUser.id && room.participants.length > 0) {
                room.hostId = room.participants[0].userId
                room.participants[0].role = 'host'
            }
            
            // 如果房间没有参与者了，标记为非活跃
            if (room.participants.length === 0) {
                room.isActive = false
            }
            
            this.currentRoom = null
            this.isVideoEnabled = false
            this.isMicEnabled = false
            this.isScreenSharing = false
            this.connectionStatus = 'disconnected'
            this.remoteStreams = {}
        },

        toggleVideo() {
            if (!this.localStream) return
            
            this.isVideoEnabled = !this.isVideoEnabled
            
            const videoTrack = this.localStream.getVideoTracks()[0]
            if (videoTrack) {
                videoTrack.enabled = this.isVideoEnabled
            }
            
            // 更新参与者状态
            const participant = this.currentUserParticipant
            if (participant) {
                participant.isVideoEnabled = this.isVideoEnabled
            }
        },

        toggleMic() {
            if (!this.localStream) return
            
            this.isMicEnabled = !this.isMicEnabled
            
            const audioTrack = this.localStream.getAudioTracks()[0]
            if (audioTrack) {
                audioTrack.enabled = this.isMicEnabled
            }
            
            // 更新参与者状态
            const participant = this.currentUserParticipant
            if (participant) {
                participant.isMicEnabled = this.isMicEnabled
            }
        },
        
        async startScreenShare() {
            if (!this.roomSettings.allowScreenShare) return false
            
            try {
                const screenStream = await this.getScreenShare()
                
                // 替换视频轨道
                if (this.localStream) {
                    const videoTrack = this.localStream.getVideoTracks()[0]
                    const screenTrack = screenStream.getVideoTracks()[0]
                    
                    if (videoTrack && screenTrack) {
                        this.localStream.removeTrack(videoTrack)
                        this.localStream.addTrack(screenTrack)
                        videoTrack.stop()
                    }
                }
                
                this.isScreenSharing = true
                
                // 监听屏幕共享结束
                screenStream.getVideoTracks()[0].onended = () => {
                    this.stopScreenShare()
                }
                
                // 更新参与者状态
                const participant = this.currentUserParticipant
                if (participant) {
                    participant.isScreenSharing = true
                }
                
                return true
            } catch (error) {
                console.error('Failed to start screen share:', error)
                return false
            }
        },
        
        async stopScreenShare() {
            if (!this.isScreenSharing) return
            
            try {
                // 重新获取摄像头
                const cameraStream = await navigator.mediaDevices.getUserMedia({
                    video: true,
                    audio: false
                })
                
                if (this.localStream) {
                    const currentVideoTrack = this.localStream.getVideoTracks()[0]
                    const newVideoTrack = cameraStream.getVideoTracks()[0]
                    
                    if (currentVideoTrack && newVideoTrack) {
                        this.localStream.removeTrack(currentVideoTrack)
                        this.localStream.addTrack(newVideoTrack)
                        currentVideoTrack.stop()
                        
                        // 设置视频轨道状态
                        newVideoTrack.enabled = this.isVideoEnabled
                    }
                }
                
                this.isScreenSharing = false
                
                // 更新参与者状态
                const participant = this.currentUserParticipant
                if (participant) {
                    participant.isScreenSharing = false
                }
            } catch (error) {
                console.error('Failed to stop screen share:', error)
            }
        },
        
        addRemoteStream(userId: number, stream: MediaStream) {
            this.remoteStreams[userId] = stream
        },
        
        removeRemoteStream(userId: number) {
            if (this.remoteStreams[userId]) {
                this.remoteStreams[userId].getTracks().forEach(track => track.stop())
                delete this.remoteStreams[userId]
            }
        },
        
        updateParticipantStatus(userId: number, updates: Partial<RoomParticipant>) {
            const participant = this.participants.find(p => p.userId === userId)
            if (participant) {
                Object.assign(participant, updates)
            }
            
            // 同时更新房间中的参与者信息
            if (this.currentRoom) {
                const roomParticipant = this.currentRoom.participants.find(p => p.userId === userId)
                if (roomParticipant) {
                    Object.assign(roomParticipant, updates)
                }
            }
        },
        
        kickParticipant(userId: number) {
            const userStore = useUserStore()
            if (!userStore.currentUser || !this.currentRoom) return false
            
            // 只有房主可以踢人
            if (this.currentRoom.hostId !== userStore.currentUser.id) return false
            
            const participantIndex = this.participants.findIndex(p => p.userId === userId)
            if (participantIndex > -1) {
                this.participants.splice(participantIndex, 1)
            }
            
            const roomParticipantIndex = this.currentRoom.participants.findIndex(p => p.userId === userId)
            if (roomParticipantIndex > -1) {
                this.currentRoom.participants.splice(roomParticipantIndex, 1)
            }
            
            // 移除远程流
            this.removeRemoteStream(userId)
            
            return true
        },
        
        muteParticipant(userId: number, mute: boolean) {
            const userStore = useUserStore()
            if (!userStore.currentUser || !this.currentRoom) return false
            
            // 只有房主可以静音他人
            if (this.currentRoom.hostId !== userStore.currentUser.id) return false
            
            this.updateParticipantStatus(userId, { isMicEnabled: !mute })
            return true
        },
        
        updateRoomSettings(roomIdOrSettings: string | Partial<typeof this.roomSettings>, settings?: any) {
            if (typeof roomIdOrSettings === 'string') {
                // 新的签名：updateRoomSettings(roomId, settings)
                const room = this.rooms.find(r => r.id === roomIdOrSettings)
                if (room && settings) {
                    Object.assign(room.settings, settings)
                }
            } else {
                // 原有签名：updateRoomSettings(settings)
                Object.assign(this.roomSettings, roomIdOrSettings)
                if (this.currentRoom) {
                    Object.assign(this.currentRoom.settings, roomIdOrSettings)
                }
            }
        },
        
        getRoomById(roomId: string) {
            return this.rooms.find(r => r.id === roomId)
        },
        
        getActiveRooms() {
            return this.rooms.filter(r => r.isActive)
        },
        
        getPublicRooms() {
            return this.rooms.filter(r => r.isActive && !r.isPrivate)
        },

        // 选择房间（用于显示房间页面）
        selectRoom(room: Room) {
            this.currentRoom = room
        },

        // 加载房间列表（模拟数据）
        loadRooms() {
            this.rooms = [
                {
                    id: '1',
                    name: '技术讨论',
                    description: '技术相关话题讨论',
                    type: 'text',
                    isPrivate: false,
                    createdBy: 1,
                    createdAt: new Date(),
                    participants: [],
                    maxParticipants: 50,
                    settings: {
                        allowScreenShare: true,
                        recordSession: false,
                        muteOnJoin: false
                    },
                    status: 'active',
                    hostId: 1,
                    isActive: true
                },
                {
                    id: '2',
                    name: '项目会议',
                    description: '项目进度讨论',
                    type: 'video',
                    isPrivate: true,
                    createdBy: 2,
                    createdAt: new Date(),
                    participants: [],
                    maxParticipants: 10,
                    settings: {
                        allowScreenShare: true,
                        recordSession: true,
                        muteOnJoin: true
                    },
                    status: 'active',
                    hostId: 2,
                    isActive: true
                }
            ]
        },

        // 加入通话
        async joinCall(roomId: string) {
            try {
                await this.initializeMedia()
                this.isInCall = true
                // 这里应该实现实际的WebRTC连接逻辑
                return true
            } catch (error) {
                console.error('Failed to join call:', error)
                throw error
            }
        },

        // 离开通话
        async leaveCall() {
            this.isInCall = false
            if (this.localStream) {
                this.localStream.getTracks().forEach(track => track.stop())
                this.localStream = null
            }
        },

        // 切换屏幕共享
        async toggleScreenShare() {
            if (this.isScreenSharing) {
                await this.stopScreenShare()
            } else {
                await this.startScreenShare()
            }
        }
    }
})