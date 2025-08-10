<template>
  <section class="stack">
    <div class="container-card card-pad">
      <div class="flex items-center gap-12">
        <div style="min-width: 220px" class="flex-1">
          <div class="stack">
            <label class="label">加入房间（房间 ID）</label>
            <input class="input" v-model.trim="rid" placeholder="如：rt-6F3A1B" />
          </div>
        </div>
        <button class="button primary" :disabled="manager.state.joined" @click="join">加入</button>
        <div class="flex-1"></div>
        <button class="button secondary" @click="createRandom">创建随机房间</button>
        <span class="note">提示：可开两个标签页，加入同一房间体验视频与文字。</span>
      </div>
    </div>

    <div v-if="manager.state.joined" class="grid-3">
      <section class="stage">
        <div class="videos">
          <VideoTile v-for="v in videos" :key="v.id" :label="v.label" :stream="v.stream" :muted="v.id===manager.state.selfId" />
        </div>
        <div class="controls">
          <button class="button secondary" @click="manager.toggleMic()">麦克风 <span class="badge" style="margin-left:6px">{{ micState }}</span></button>
          <button class="button secondary" @click="manager.toggleCam()">摄像头 <span class="badge" style="margin-left:6px">{{ camState }}</span></button>
          <button class="button secondary" @click="copyRoom">复制房间 ID</button>
          <div class="flex-1"></div>
          <button class="button danger" @click="leave">离开房间</button>
        </div>
      </section>

      <aside class="room-side">
        <div class="head flex items-center justify-between">
          <div><strong>房间</strong> <span class="badge" style="margin-left:6px">{{ manager.state.roomId }}</span></div>
          <span class="badge">成员 {{ manager.state.members.length }}</span>
        </div>
        <div class="members">
          <span v-for="m in manager.state.members" :key="m.id" class="badge">@{{ m.user }}</span>
        </div>
        <div class="chatlog">
          <div v-for="m in manager.state.chatlog" :key="m.ts + ':' + m.from" class="msg" :class="{me: m.from===current}">
            <div class="bubble">{{ m.text }}</div>
            <span class="time">{{ text.time(m.ts) }}</span>
          </div>
        </div>
        <form class="composer" @submit.prevent="sendText">
          <input id="room-input" class="input" placeholder="发送房间文字消息（全员可见）" autocomplete="off" />
          <button class="button primary">发送</button>
        </form>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { createRoomManager } from '../utils/webrtc.js';
import { API, text, uuid } from '../utils/store.js';
import VideoTile from '../components/VideoTile.vue';

const current = API.getCurrent()?.username || '';
const rid = ref('');
const manager = createRoomManager(current);
const videos = ref([]); // [{id, label, stream}]

const micState = computed(() => manager.state.micOn ? '开' : '关');
const camState = computed(() => manager.state.camOn ? '开' : '关');

function createRandom() { rid.value = `rt-${uuid().slice(0, 6).toUpperCase()}`; }
async function join() {
  if (!rid.value.trim()) return;
  await manager.join(rid.value.trim());
  const stream = await manager.ensureMedia();
  upsertVideo(manager.state.selfId, current, stream);
}
function leave() {
  videos.value = [];
  manager.leave();
}
function upsertVideo(id, label, stream) {
  const i = videos.value.findIndex(v => v.id === id);
  if (i >= 0) videos.value[i] = { id, label, stream };
  else videos.value.push({ id, label, stream });
}
function copyRoom() {
  if (!manager.state.roomId) return;
  navigator.clipboard?.writeText(manager.state.roomId);
  alert('已复制房间 ID');
}
function sendText() {
  const el = document.getElementById('room-input');
  const t = (el?.value || '').trim();
  if (!t) return;
  manager.sendText(t);
  el.value = '';
}

// 简单轮询：为每个 RTCPeerConnection 聚合远端流
let poll = null;
onMounted(() => {
  poll = setInterval(() => {
    if (!manager.state.joined) return;
    if (manager.state.localStream) upsertVideo(manager.state.selfId, current, manager.state.localStream);
    manager.state.peers.forEach((pc, pid) => {
      const ms = new MediaStream();
      pc.getReceivers().forEach(r => { if (r.track && r.track.readyState !== 'ended') ms.addTrack(r.track); });
      if (ms.getTracks().length) {
        const member = manager.state.members.find(m => m.id === pid);
        upsertVideo(pid, member?.user || '成员', ms);
      }
    });
  }, 1000);
});
onUnmounted(() => { if (poll) clearInterval(poll); });
</script>