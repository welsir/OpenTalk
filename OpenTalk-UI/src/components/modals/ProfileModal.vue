<template>
  <el-dialog
      v-model="dialogVisible"
      title="个人资料"
      width="600px"
      :before-close="handleClose"
      center
      :modal="true"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      :append-to-body="true"
      :z-index="3000"
      class="profile-dialog"
  >
    <div class="profile-content">
      <div v-if="!isEditing" class="profile-view">
        <div class="profile-avatar">
          <span class="avatar-display">{{ profileData.avatar }}</span>
        </div>

        <div class="profile-info">
          <div class="info-item">
            <label>用户名</label>
            <span>@{{ profileData.username }}</span>
          </div>

          <div class="info-item">
            <label>姓名</label>
            <span>{{ profileData.name }}</span>
          </div>

          <div class="info-item">
            <label>个人简介</label>
            <span>{{ profileData.bio }}</span>
          </div>
        </div>
      </div>

      <el-form
          v-else
          :model="profileData"
          label-width="80px"
      >
        <el-form-item label="姓名">
          <el-input v-model="profileData.name" />
        </el-form-item>

        <el-form-item label="个人简介">
          <el-input
              v-model="profileData.bio"
              type="textarea"
              :rows="3"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <span v-if="!isEditing">
        <el-button type="primary" @click="isEditing = true">
          编辑资料
        </el-button>
      </span>
      <span v-else>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const userStore = useUserStore()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const isEditing = ref(false)
const profileData = ref({ ...userStore.currentUser! })

watch(() => props.visible, (newVal) => {
  if (newVal) {
    profileData.value = { ...userStore.currentUser! }
    isEditing.value = false
  }
})

const handleClose = () => {
  dialogVisible.value = false
}

const cancelEdit = () => {
  profileData.value = { ...userStore.currentUser! }
  isEditing.value = false
}

const saveProfile = () => {
  userStore.updateProfile(profileData.value)
  isEditing.value = false
  ElMessage.success('资料更新成功')
}
</script>

<style>
/* 弹窗全局样式 - 不使用scoped以确保能应用到body下的弹窗 */
.profile-dialog .el-dialog {
  margin: 0 !important;
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  max-height: 90vh !important;
  overflow: visible !important;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border-radius: 16px !important;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.1),
    0 8px 32px rgba(0, 0, 0, 0.08) !important;
  border: 0.5px solid rgba(255, 255, 255, 0.8) !important;
  z-index: 3000 !important;
}

.profile-dialog .el-dialog__header {
  padding: 24px 32px 16px !important;
  background: transparent !important;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.06) !important;
}

.profile-dialog .el-dialog__title {
  font-size: 20px !important;
  font-weight: 600 !important;
  color: rgba(60, 60, 67, 0.9) !important;
}

.profile-dialog .el-dialog__body {
  padding: 0 !important;
  max-height: 70vh !important;
  overflow-y: auto !important;
}

.profile-dialog .el-dialog__footer {
  padding: 16px 32px 24px !important;
  background: transparent !important;
  border-top: 0.5px solid rgba(0, 0, 0, 0.06) !important;
}

.profile-dialog .el-overlay {
  z-index: 2999 !important;
}
</style>

<style scoped>

.profile-content {
  padding: 32px;
  background: transparent;
}

.profile-view {
  text-align: center;
}

.profile-avatar {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.avatar-display {
  font-size: 64px;
  width: 96px;
  height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  border: 0.5px solid rgba(0, 0, 0, 0.04);
  border-radius: 50%;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: all 0.2s ease;
}

.avatar-display:hover {
  transform: translateY(-1px);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 1);
}

.profile-info {
  text-align: left;
  background: rgba(248, 248, 248, 0.6);
  border-radius: 10px;
  padding: 16px 20px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.info-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-item:hover {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  margin: 0 -12px;
  padding: 12px 12px;
}

.info-item label {
  width: 100px;
  color: rgba(60, 60, 67, 0.6);
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
}

.info-item span {
  flex: 1;
  color: rgba(60, 60, 67, 0.9);
  font-size: 15px;
  font-weight: 400;
}

/* 编辑表单样式 */
.el-form {
  background: rgba(248, 248, 248, 0.6);
  border-radius: 10px;
  padding: 16px 20px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.el-form-item {
  margin-bottom: 16px;
}

.el-form-item:last-child {
  margin-bottom: 0;
}

.el-form-item__label {
  color: rgba(60, 60, 67, 0.8) !important;
  font-weight: 500 !important;
}

.el-input__wrapper {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 0.5px solid rgba(0, 0, 0, 0.06) !important;
  border-radius: 8px !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: all 0.2s ease !important;
}

.el-input__wrapper:hover {
  background: rgba(255, 255, 255, 0.9) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06) !important;
}

.el-input__wrapper.is-focus {
  border-color: rgba(0, 122, 255, 0.3) !important;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.06),
    0 0 0 2px rgba(0, 122, 255, 0.1) !important;
}

.el-textarea__inner {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  color: rgba(60, 60, 67, 0.9) !important;
}

/* 按钮样式 */
.el-button {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 0.5px solid rgba(0, 0, 0, 0.06) !important;
  color: rgba(60, 60, 67, 0.8) !important;
  font-weight: 400 !important;
  border-radius: 8px !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: all 0.2s ease !important;
}

.el-button:hover {
  background: rgba(255, 255, 255, 0.9) !important;
  color: rgba(60, 60, 67, 1) !important;
  transform: translateY(-0.5px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
}

.el-button--primary {
  background: rgba(0, 122, 255, 0.9) !important;
  border-color: rgba(0, 122, 255, 1) !important;
  color: white !important;
}

.el-button--primary:hover {
  background: rgba(0, 122, 255, 1) !important;
  box-shadow: 0 2px 12px rgba(0, 122, 255, 0.3) !important;
}
</style>