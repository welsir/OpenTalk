import { defineStore } from 'pinia'
import type { Room } from '@/types'

export const useRoomStore = defineStore('room', {
    state: () => ({
        rooms: [
            {
                id: 1,
                name: '技术讨论',
                creator: 'Alice Wang',
                participants: 3,
                active: true
            },
            {
                id: 2,
                name: '项目会议',
                creator: 'Bob Chen',
                participants: 5,
                active: false
            }
        ] as Room[],
        selectedRoom: null as Room | null,
        isVideoOn: true,
        isMicOn: true
    }),

    actions: {
        createRoom(name: string, creator: string) {
            const newRoom: Room = {
                id: this.rooms.length + 1,
                name,
                creator,
                participants: 1,
                active: true
            }
            this.rooms.push(newRoom)
            return newRoom
        },

        joinRoom(room: Room) {
            this.selectedRoom = room
        },

        leaveRoom() {
            this.selectedRoom = null
        },

        toggleVideo() {
            this.isVideoOn = !this.isVideoOn
        },

        toggleMic() {
            this.isMicOn = !this.isMicOn
        }
    }
})