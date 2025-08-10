
<template>
  <section class="grid-3">
    <section class="container-card card-pad">
      <h3 class="section-title">个人资料</h3>
      <form class="stack" @submit.prevent="save">
        <div class="stack">
          <label class="label">用户名（只读）</label>
          <input class="input" v-model="form.username" readonly />
        </div>
        <div class="stack">
          <label class="label">昵称</label>
          <input class="input" v-model="form.nickname" placeholder="展示名称" />
        </div>
        <div class="stack">
          <label class="label">个性签名</label>
          <input class="input" v-model="form.bio" placeholder="一句话介绍自己" />
        </div>
        <div class="stack">
          <label class="label">头像颜色</label>
          <input class="input" type="color" v-model="form.color" />
        </div>
        <div class="flex gap-8">
          <button class="button primary">保存</button>
          <button class="button secondary" type="button" @click="reset">重置为默认</button>
        </div>
      </form>
    </section>

    <section class="container-card card-pad">
      <h3 class="section-title">账号与安全</h3>
      <div class="stack">
        <p class="note">密码不可找回。请妥善保管。</p>
        <div class="flex gap-8">
          <button class="button secondary" @click="exportData">导出本地数据</button>
          <button class="button secondary" @click="importData">导入本地数据</button>
        </div>
        <input ref="file" type="file" accept="application/json" hidden />
      </div>
    </section>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { API } from '../utils/store.js';

const current = API.getCurrent()?.username || '';
const file = ref(null);
const form = ref({ username: current, nickname: '', bio: '', color: '#0a84ff' });

function load() {
  const p = API.getProfile(current);
  form.value.nickname = p.displayName || current;
  form.value.bio = p.bio || '';
  form.value.color = p.color || '#0a84ff';
}
function save() {
  API.setProfile(current, { displayName: form.value.nickname.trim() || current, bio: form.value.bio, color: form.value.color });
  alert('已保存');
}
function reset() {
  form.value.nickname = current;
  form.value.bio = '';
  form.value.color = '#0a84ff';
  save();
}
function exportData() {
  const blob = new Blob([JSON.stringify(localStorage, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a'); a.href = url; a.download = `opentalk-export-${Date.now()}.json`; a.click();
  URL.revokeObjectURL(url);
}
function importData() {
  file.value.onchange = async (e) => {
    const f = e.target.files[0];
    if (!f) return;
    try {
      const json = JSON.parse(await f.text());
      if (json && typeof json === 'object') {
        Object.keys(json).forEach(k => localStorage.setItem(k, json[k]));
        alert('导入成功，刷新页面后生效');
      }
    } catch { alert('导入失败'); }
  };
  file.value.click();
}
onMounted(load);
</script>