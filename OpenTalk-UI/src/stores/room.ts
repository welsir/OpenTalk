import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { roomService, type Room, type RoomInfo, type CreateRoomRequest, type JoinRoomRequest, type LeaveRoomRequest } from '@/services/room.service.enhanced'
import type { RoomParticipant } from '@/types'

export const useRoomStore = defineStore('room', () => {
  // State
  const rooms = ref<Room[]>([])
  const currentRoom = ref<RoomInfo | null>(null)
  const localStream = ref<MediaStream | null>(null)
  const remoteStreams = ref<Record<string, MediaStream>>({})
  const isConnected = ref(false)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const roomSettings = ref({
    videoEnabled: true,
    audioEnabled: true,
    screenSharing: false,
    maxParticipants: 8,
    allowScreenShare: true,
    recordSession: false,
    muteOnJoin: true
  })

  // Media state
  const isVideoEnabled = ref(false)
  const isMicEnabled = ref(false)
  const isScreenSharing = ref(false)
  const connectionStatus = ref<'connecting' | 'connected' | 'disconnected' | 'failed'>('disconnected')
  const isInCall = ref(false)

  // Getters
  const activeRooms = computed(() => 
    rooms.value.filter((room: Room) => room.status === 'active')
  )

  const publicRooms = computed(() => 
    rooms.value.filter((room: Room) => room.isPublic && room.status === 'active')
  )

  const currentRoomParticipants = computed(() => 
    currentRoom.value?.members || []
  )

  const isInRoom = computed(() => !!currentRoom.value)

  const participantCount = computed(() => 
    currentRoomParticipants.value.length
  )

  const canJoinRoom = computed(() => 
    participantCount.value < roomSettings.value.maxParticipants
  )

  const activeParticipants = computed(() => 
    currentRoomParticipants.value.filter((p: RoomParticipant) => p.status === 'online')
  )

  // Media Actions
  const initializeMedia = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: roomSettings.value.videoEnabled,
        audio: roomSettings.value.audioEnabled
      })
      
      localStream.value = stream
      isVideoEnabled.value = true
      isMicEnabled.value = !roomSettings.value.muteOnJoin
      
      // 如果设置为加入时静音，则静音麦克风
      if (roomSettings.value.muteOnJoin) {
        toggleMic()
      }
      
      return stream
    } catch (err) {
      error.value = '无法访问媒体设备'
      throw err
    }
  }

  const toggleVideo = () => {
    if (!localStream.value) return
    
    const videoTrack = localStream.value.getVideoTracks()[0]
    if (videoTrack) {
      videoTrack.enabled = !videoTrack.enabled
      isVideoEnabled.value = videoTrack.enabled
      roomSettings.value.videoEnabled = videoTrack.enabled
    }
  }

  const toggleMic = () => {
    if (!localStream.value) return
    
    const audioTrack = localStream.value.getAudioTracks()[0]
    if (audioTrack) {
      audioTrack.enabled = !audioTrack.enabled
      isMicEnabled.value = audioTrack.enabled
      roomSettings.value.audioEnabled = audioTrack.enabled
    }
  }

  const startScreenShare = async () => {
    try {
      const screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: true
      })
      
      // Replace video track with screen share
      if (localStream.value) {
        const videoTrack = localStream.value.getVideoTracks()[0]
        if (videoTrack) {
          localStream.value.removeTrack(videoTrack)
        }
        
        const screenTrack = screenStream.getVideoTracks()[0]
        localStream.value.addTrack(screenTrack)
        
        isScreenSharing.value = true
        roomSettings.value.screenSharing = true
        
        // Handle screen share end
        screenTrack.onended = () => {
          stopScreenShare()
        }
      }
    } catch (err) {
      error.value = '开始屏幕共享失败'
      throw err
    }
  }

  const stopScreenShare = async () => {
    if (!localStream.value || !isScreenSharing.value) return
    
    try {
      // Get new camera stream
      const cameraStream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: false // Keep existing audio
      })
      
      // Replace screen track with camera track
      const screenTrack = localStream.value.getVideoTracks()[0]
      if (screenTrack) {
        localStream.value.removeTrack(screenTrack)
        screenTrack.stop()
      }
      
      const cameraTrack = cameraStream.getVideoTracks()[0]
      localStream.value.addTrack(cameraTrack)
      
      isScreenSharing.value = false
      roomSettings.value.screenSharing = false
    } catch (err) {
      error.value = '停止屏幕共享失败'
      throw err
    }
  }

  const toggleScreenShare = async () => {
    if (isScreenSharing.value) {
      await stopScreenShare()
    } else {
      await startScreenShare()
    }
  }

  const addRemoteStream = (userId: string, stream: MediaStream) => {
    remoteStreams.value[userId] = stream
  }

  const removeRemoteStream = (userId: string) => {
    const stream = remoteStreams.value[userId]
    if (stream) {
      stream.getTracks().forEach(track => track.stop())
      delete remoteStreams.value[userId]
    }
  }

  // Room Management Actions (delegated to service)
  const createRoom = async (roomData: { roomName: string; ownerUid: string; ownerName: string; description?: string; isPublic: boolean; maxMembers: number }) => {
    isLoading.value = true
    error.value = null
    
    try {
      const createRequest: CreateRoomRequest = roomData
      const newRoom = await roomService.createRoom(createRequest)
      
      rooms.value.push(newRoom)
      // 创建房间后获取详细信息
      await getRoomInfo(newRoom.id)
      
      return newRoom
    } catch (err) {
      error.value = err instanceof Error ? err.message : '创建房间失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const joinRoom = async (roomId: string, userId: string, userName: string) => {
    isLoading.value = true
    error.value = null
    connectionStatus.value = 'connecting'
    
    try {
      const joinRequest: JoinRoomRequest = { roomId, userId, userName }
      await roomService.joinRoom(joinRequest)
      
      // 加入成功后获取房间详细信息
      await getRoomInfo(roomId)
      isConnected.value = true
      connectionStatus.value = 'connected'
      
      // Initialize media when joining room
      await initializeMedia()
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加入房间失败'
      connectionStatus.value = 'failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const leaveRoom = async (userId: string) => {
    if (!currentRoom.value) return
    
    isLoading.value = true
    
    try {
      const leaveRequest: LeaveRoomRequest = { roomId: currentRoom.value.id, userId }
      await roomService.leaveRoom(leaveRequest)
      
      // Clean up media streams
       if (localStream.value) {
         localStream.value.getTracks().forEach((track: MediaStreamTrack) => track.stop())
       }
       
       // Clean up remote streams
       Object.values(remoteStreams.value).forEach((stream: MediaStream) => {
         stream.getTracks().forEach((track: MediaStreamTrack) => track.stop())
       })
      
      // Reset state
      localStream.value = null
      remoteStreams.value = {}
      currentRoom.value = null
      isConnected.value = false
      connectionStatus.value = 'disconnected'
      isInCall.value = false
      isVideoEnabled.value = false
      isMicEnabled.value = false
      isScreenSharing.value = false
    } catch (err) {
      error.value = err instanceof Error ? err.message : '离开房间失败'
    } finally {
      isLoading.value = false
    }
  }

  const getRoomInfo = async (roomId: string) => {
    try {
      const roomInfo = await roomService.getRoomInfo(roomId)
      currentRoom.value = roomInfo
      return roomInfo
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取房间信息失败'
      throw err
    }
  }

  const loadPublicRooms = async () => {
    isLoading.value = true
    error.value = null
    
    try {
      const publicRoomList = await roomService.getPublicRooms()
      rooms.value = publicRoomList
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载公共房间失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const loadOwnedRooms = async (userId: string) => {
    isLoading.value = true
    error.value = null
    
    try {
      const ownedRoomList = await roomService.getOwnedRooms(userId)
      return ownedRoomList
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载拥有的房间失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const loadJoinedRooms = async (userId: string) => {
    isLoading.value = true
    error.value = null
    
    try {
      const joinedRoomList = await roomService.getJoinedRooms(userId)
      return joinedRoomList
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载已加入的房间失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  // 加载房间列表（默认加载公共房间）
  const loadRooms = async () => {
    return await loadPublicRooms()
  }

  // Utility Actions
  const selectRoom = (room: Room) => {
    // 只设置基本房间信息，详细信息需要通过getRoomInfo获取
    currentRoom.value = {
      ...room,
      members: []
    }
  }

  const updateRoomSettings = (settings: Partial<typeof roomSettings.value>) => {
    Object.assign(roomSettings.value, settings)
  }

  // WebRTC related functions (simplified)
  const joinCall = async (roomId: string, userId: string, userName: string) => {
    await joinRoom(roomId, userId, userName)
    isInCall.value = true
    // Additional WebRTC setup would go here
  }

  const leaveCall = async (userId: string) => {
    await leaveRoom(userId)
    isInCall.value = false
    // Additional WebRTC cleanup would go here
  }

  return {
    // State
    rooms,
    currentRoom,
    localStream,
    remoteStreams,
    isConnected,
    isLoading,
    error,
    roomSettings,
    isVideoEnabled,
    isMicEnabled,
    isScreenSharing,
    connectionStatus,
    isInCall,
    
    // Getters
    activeRooms,
    publicRooms,
    currentRoomParticipants,
    isInRoom,
    participantCount,
    canJoinRoom,
    activeParticipants,
    
    // Media Actions
    initializeMedia,
    toggleVideo,
    toggleMic,
    startScreenShare,
    stopScreenShare,
    toggleScreenShare,
    addRemoteStream,
    removeRemoteStream,
    
    // Room Management Actions
    createRoom,
    joinRoom,
    leaveRoom,
    getRoomInfo,
    loadRooms,
    loadPublicRooms,
    loadOwnedRooms,
    loadJoinedRooms,
    selectRoom,
    updateRoomSettings,
    
    // WebRTC Actions
    joinCall,
    leaveCall
  }
})