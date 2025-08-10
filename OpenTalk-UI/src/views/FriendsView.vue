<template>
  <section class="grid-2">
    <aside class="container-card card-pad stack">
      <span class="badge">操作</span>
      <div class="stack">
        <label class="label">添加好友（用户名）</label>
        <div class="flex gap-8">
          <input class="input" v-model.trim="input" placeholder="输入对方用户名" />
          <button class="button primary" @click="add">添加</button>
        </div>
      </div>
      <p class="note">双方互为好友后，即可在「聊天」中开始对话。</p>
    </aside>

    <section class="container-card card-pad">
      <h3 class="section-title">我的好友</h3>
      <div class="list">
        <div v-if="!friends.length" class="note">暂无好友</div>
        <div v-for="u in friends" :key="u" class="row">
          <AvatarMono :seed="profileOf(u).displayName || u" :color="profileOf(u).color" />
          <div style="display:flex; flex-direction:column">
            <strong>{{ profileOf(u).displayName || u }}</strong>
            <span class="note">@{{ u }}</span>
          </div>
          <div class="flex-1"></div>
          <span class="badge">好友</span>
          <router-link to="/chat"><button class="button secondary">聊天</button></router-link>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { ref } from 'vue';
import { API } from '../utils/store.js';
import AvatarMono from '../components/AvatarMono.vue';

const current = API.getCurrent()?.username || '';
const input = ref('');
const friends = ref(API.listFriends(current));

function profileOf(u) { return API.getProfile(u); }

function add() {
  const v = input.value.trim();
  if (!v) return;
  try {
    API.addFriend(current, v);
    friends.value = API.listFriends(current);
    input.value = '';
    alert('已添加好友');
  } catch (e) { alert(e.message); }
}
</script>