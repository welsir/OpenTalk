<template>
  <el-dialog
      v-model="dialogVisible"
      title="个人资料"
      width="500px"
      :before-close="handleClose"
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

<style scoped>
.profile-content {
  padding: 20px 0;
}

.profile-view {
  text-align: center;
}

.profile-avatar {
  margin-bottom: 30px;
}

.avatar-display {
  font-size: 80px;
}

.profile-info {
  text-align: left;
}

.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #e4e7ed;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  width: 100px;
  color: #909399;
  font-size: 14px;
}

.info-item span {
  flex: 1;
  color: #303133;
}
</style>