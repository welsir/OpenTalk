// 极简 WebRTC 房间管理器：同源多标签页演示
import { reactive } from 'vue'
import { uuid } from './store.js'
import { Channel } from './bus.js'

function createRoomManager(currentUser) {
    const state = reactive({
        joined: false,
        roomId: '',
        selfId: uuid(),
        members: [],
        peers: new Map(),
        dataChannels: new Map(),
        localStream: null,
        micOn: true,
        camOn: true,
        chatlog: []
    })

    let chan = null
    function broadcast(msg){ chan?.post({ ...msg, roomId: state.roomId }) }
    function addMember(id, user){ if(!state.members.find(m=>m.id===id)) state.members.push({id,user}) }
    function removeMember(id){ state.members = state.members.filter(m=>m.id!==id) }

    async function ensureMedia(){
        if (state.localStream) return state.localStream
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true, video: true })
        state.localStream = stream
        return stream
    }
    function closePeer(id){
        state.dataChannels.get(id)?.close()
        state.dataChannels.delete(id)
        const pc = state.peers.get(id)
        if (pc) { try { pc.close() } catch {} }
        state.peers.delete(id)
    }
    function cleanup(){
        state.members = []
        ;[...state.peers.keys()].forEach(closePeer)
        state.localStream?.getTracks().forEach(t=>t.stop())
        state.localStream = null
        chan?.close(); chan = null
        state.chatlog = []
    }
    function sendText(text){
        const msg = { type:'room-text', from: currentUser, text: String(text||'').slice(0,1000), ts: Date.now() }
        for (const dc of state.dataChannels.values()){
            try { if (dc.readyState==='open') dc.send(JSON.stringify(msg)) } catch {}
        }
        state.chatlog.push({ from: msg.from, text: msg.text, ts: msg.ts })
    }
    function setupDataChannel(dc){
        dc.onmessage = (e)=>{
            try {
                const data = JSON.parse(e.data)
                if (data?.type==='room-text') state.chatlog.push({ from: data.from, text: data.text, ts: data.ts })
            } catch {}
        }
    }
    async function makePeer(remote){
        const pc = new RTCPeerConnection({ iceServers:[{ urls:'stun:stun.l.google.com:19302' }] })
        state.peers.set(remote.id, pc)
        const stream = await ensureMedia()
        for (const track of stream.getTracks()) pc.addTrack(track, stream)
        const dc = pc.createDataChannel('chat')
        state.dataChannels.set(remote.id, dc)
        setupDataChannel(dc)
        pc.ondatachannel = (e)=>{ state.dataChannels.set(remote.id, e.channel); setupDataChannel(e.channel) }
        pc.onicecandidate = (e)=>{ if(e.candidate) broadcast({ type:'ice', from: state.selfId, to: remote.id, candidate: e.candidate }) }
        return pc
    }
    async function join(roomId){
        if (state.joined) return true
        state.roomId = roomId
        chan = new Channel(`ot_room_${roomId}`)
        state.joined = true

        chan.on(async (msg)=>{
            if (!msg || msg.roomId !== state.roomId) return
            if (msg.from === state.selfId) return
            switch (msg.type){
                case 'new-peer': {
                    addMember(msg.from, msg.user)
                    const pc = await makePeer({ id: msg.from, user: msg.user })
                    const offer = await pc.createOffer()
                    await pc.setLocalDescription(offer)
                    broadcast({ type:'offer', from: state.selfId, to: msg.from, sdp: offer, user: currentUser })
                    break
                }
                case 'offer': {
                    if (msg.to !== state.selfId) break
                    addMember(msg.from, msg.user)
                    let pc = state.peers.get(msg.from)
                    if (!pc){
                        pc = new RTCPeerConnection({ iceServers:[{ urls:'stun:stun.l.google.com:19302' }] })
                        state.peers.set(msg.from, pc)
                        pc.ondatachannel = (e)=>{ state.dataChannels.set(msg.from, e.channel); setupDataChannel(e.channel) }
                        pc.onicecandidate = (e)=>{ if(e.candidate) broadcast({ type:'ice', from: state.selfId, to: msg.from, candidate: e.candidate }) }
                        const stream = await ensureMedia()
                        for (const track of stream.getTracks()) pc.addTrack(track, stream)
                    }
                    await pc.setRemoteDescription(new RTCSessionDescription(msg.sdp))
                    const answer = await pc.createAnswer()
                    await pc.setLocalDescription(answer)
                    broadcast({ type:'answer', from: state.selfId, to: msg.from, sdp: answer, user: currentUser })
                    break
                }
                case 'answer': {
                    if (msg.to !== state.selfId) break
                    const pc = state.peers.get(msg.from)
                    if (pc) await pc.setRemoteDescription(new RTCSessionDescription(msg.sdp))
                    break
                }
                case 'ice': {
                    if (msg.to !== state.selfId) break
                    const pc = state.peers.get(msg.from)
                    if (pc && msg.candidate) try { await pc.addIceCandidate(new RTCIceCandidate(msg.candidate)) } catch {}
                    break
                }
                case 'leave': {
                    removeMember(msg.from)
                    closePeer(msg.from)
                    break
                }
            }
        })
        broadcast({ type:'new-peer', from: state.selfId, user: currentUser })
        return true
    }
    function leave(){
        if (!state.joined) return
        broadcast({ type:'leave', from: state.selfId })
        cleanup()
        state.joined = false
        state.roomId = ''
    }
    function toggleMic(){ state.micOn = !state.micOn; state.localStream?.getAudioTracks().forEach(t=>t.enabled = state.micOn) }
    function toggleCam(){ state.camOn = !state.camOn; state.localStream?.getVideoTracks().forEach(t=>t.enabled = state.camOn) }

    return { state, join, leave, ensureMedia, toggleMic, toggleCam, sendText }
}

export { createRoomManager }