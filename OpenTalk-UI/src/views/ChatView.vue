<template>
  <section class="grid-2">
    <!-- 左侧：搜索 + 好友列表 -->
    <aside class="stack">
      <div class="container-card card-pad stack">
        <div class="stack">
          <label class="label">搜索用户（按用户名）</label>
          <div class="flex gap-8">
            <input class="input" v-model.trim="key" @keyup.enter="doSearch" placeholder="输入用户名，例如 bob" />
            <button class="button secondary" @click="doSearch">搜索</button>
          </div>
        </div>
        <div class="list">
          <div v-if="!results.length" class="note">无搜索结果</div>
          <div v-for="u in results" :key="u" class="row">
            <AvatarMono :seed="profileOf(u).displayName || u" :color="profileOf(u).color" />
            <div style="display:flex; flex-direction:column">
              <strong>{{ profileOf(u).displayName || u }}</strong>
              <span class="note">@{{ u }}</span>
            </div>
            <div class="flex-1"></div>
            <button class="button secondary" @click="openChat(u)">聊天</button>
            <button class="button primary" @click="addFriend(u)">加好友</button>
          </div>
        </div>
      </div>

      <div class="container-card card-pad stack">
        <div class="flex items-center justify-between">
          <span class="badge">我的好友</span>
          <router-link to="/friends" class="note">去管理</router-link>
        </div>
        <div class="list">
          <div v-if="!friends.length" class="note">暂无好友，先去搜索添加吧</div>
          <div v-for="u in friends" :key="u" class="row" :class="{active: u===peer}" @click="openChat(u)">
            <AvatarMono :seed="profileOf(u).displayName || u" :color="profileOf(u).color" />
            <div style="display:flex; flex-direction:column">
              <strong>{{ profileOf(u).displayName || u }}</strong>
              <span class="note">@{{ u }}</span>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧：聊天 -->
    <section class="chat-shell">
      <div class="chat-head">
        <AvatarMono v-if="peer" :seed="peerProfile.displayName || peer" :color="peerProfile.color" />
        <div style="display:flex; flex-direction:column">
          <strong>{{ peer ? (peerProfile.displayName || peer) : '选择一个好友开始聊天' }}</strong>
          <span class="note" v-if="peer">@{{ peer }}</span>
        </div>
        <div class="flex-1"></div>
        <button class="button secondary" :disabled="!peer" @click="clearHistory" title="清除与该好友的本地聊天记录">清除记录</button>
      </div>

      <div id="chat-body" class="chat-body">
        <template v-if="peer">
          <div v-for="m in msgs" :key="m.ts + ':' + m.from" class="msg" :class="{me: m.from===current}">
            <div class="bubble">{{ m.text }}</div>
            <span class="time">{{ text.time(m.ts) }}</span>
          </div>
        </template>
        <div v-else class="note">在左侧选择好友或通过搜索开始对话</div>
      </div>

      <form class="composer" @submit.prevent="send">
        <input class="input" v-model="input" :disabled="!peer" placeholder="输入消息，Enter 发送。支持多标签页实时同步。" autocomplete="off" />
        <button class="button primary" :disabled="!peer">发送</button>
      </form>
    </section>
  </section>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue';
import { API, text } from '../utils/store.js';
import { Channel } from '../utils/bus.js';
import AvatarMono from '../components/AvatarMono.vue';

const current = computed(() => API.getCurrent()?.username || '');

const key = ref('');
const results = ref([]);
const friends = ref([]);
const peer = ref('');
const peerProfile = ref({ displayName: '', color: '#d2d2d7' });
const input = ref('');
const msgs = ref([]);

let chatChannel = null;

function profileOf(u) { return API.getProfile(u); }
function refreshFriends() { friends.value = API.listFriends(current.value); }
function doSearch() {
  const k = key.value.trim();
  results.value = k ? API.listUsers().filter(u => u.includes(k)) : [];
}
function openChat(u) {
  if (!u || !API.exists(u)) return;
  peer.value = u;
  peerProfile.value = API.getProfile(u);
  msgs.value = API.getMessages(current.value, peer.value);
  nextTick(() => { const el = document.querySelector('#chat-body'); el && (el.scrollTop = el.scrollHeight); });

  chatChannel?.close();
  const pairKey = API.pairKey(current.value, u);
  chatChannel = new Channel(`ot_chat_${pairKey}`);
  chatChannel.on((m) => {
    if (m?.type === 'chat' && m.to === current.value) {
      API.pushMessage(m.from, m.to, { from: m.from, to: m.to, text: m.text, ts: m.ts });
      msgs.value.push({ from: m.from, to: m.to, text: m.text, ts: m.ts });
      nextTick(() => { const el = document.querySelector('#chat-body'); el && (el.scrollTop = el.scrollHeight); });
    }
  });
}
function send() {
  const textVal = (input.value || '').trim();
  if (!textVal || !peer.value) return;
  const m = { type: 'chat', from: current.value, to: peer.value, text: textVal.slice(0, 2000), ts: Date.now() };
  API.pushMessage(m.from, m.to, { from: m.from, to: m.to, text: m.text, ts: m.ts });
  msgs.value.push({ from: m.from, to: m.to, text: m.text, ts: m.ts });
  chatChannel?.post(m);
  input.value = '';
  nextTick(() => { const el = document.querySelector('#chat-body'); el && (el.scrollTop = el.scrollHeight); });
}
function clearHistory() {
  if (!peer.value) return;
  localStorage.setItem(`ot_messages:${API.pairKey(current.value, peer.value)}`, JSON.stringify([]));
  msgs.value = [];
}

onMounted(refreshFriends);
</script>