<template>
  <header class="navbar">
    <div class="brand">
      <div class="logo" aria-hidden="true">◯</div>
      <div class="name">OpenTalk</div>

      <div class="nav-actions">
        <router-link v-slot="{ navigate, isExactActive }" to="/chat" custom>
          <button class="tab" :class="{active: isExactActive}" @click="navigate">聊天</button>
        </router-link>
        <router-link v-slot="{ navigate, isExactActive }" to="/friends" custom>
          <button class="tab" :class="{active: isExactActive}" @click="navigate">好友</button>
        </router-link>
        <router-link v-slot="{ navigate, isExactActive }" to="/rooms" custom>
          <button class="tab" :class="{active: isExactActive}" @click="navigate">房间</button>
        </router-link>
        <router-link v-slot="{ navigate, isExactActive }" to="/profile" custom>
          <button class="tab" :class="{active: isExactActive}" @click="navigate">我的</button>
        </router-link>
      </div>

      <div class="nav-actions" style="margin-left: 12px">
        <div v-if="currentUser" class="flex items-center gap-8">
          <AvatarMono :seed="profile.displayName || currentUser" :color="profile.color" />
          <span class="badge">{{ profile.displayName || currentUser }}</span>
          <button class="tab" @click="logout">退出</button>
        </div>
        <div v-else>
          <router-link to="/auth"><button class="tab">登录</button></router-link>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { API } from '../utils/store.js';
import AvatarMono from './AvatarMono.vue';

const currentUser = computed(() => API.getCurrent()?.username || '');
const profile = computed(() => currentUser.value ? API.getProfile(currentUser.value) : { displayName: '游客', color: '#d2d2d7' });
function logout() {
  API.logout();
  location.hash = '#/auth';
}
</script>
