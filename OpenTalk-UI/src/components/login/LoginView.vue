<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <h1>OpenTalk</h1>
        <p>连接世界，分享精彩</p>
      </div>

      <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input
              v-model="formData.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
          />
        </el-form-item>

        <el-form-item v-if="isRegister" prop="name">
          <el-input
              v-model="formData.name"
              placeholder="姓名"
              prefix-icon="UserFilled"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="formData.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              @click="handleSubmit"
              :loading="loading"
              class="submit-btn"
          >
            {{ isRegister ? '注册' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="switch-mode">
        <el-link type="primary" @click="isRegister = !isRegister">
          {{ isRegister ? '已有账号？立即登录' : '没有账号？立即注册' }}
        </el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isRegister = ref(false)
const loading = ref(false)
const formRef = ref()

const formData = reactive({
  username: '',
  password: '',
  name: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  await formRef.value.validate((valid: boolean) => {
    if (valid) {
      loading.value = true

      setTimeout(() => {
        if (isRegister.value) {
          const success = userStore.register(
              formData.username,
              formData.password,
              formData.name
          )
          if (success) {
            ElMessage.success('注册成功')
            router.push('/main')
          }
        } else {
          const success = userStore.login(formData.username, formData.password)
          if (success) {
            ElMessage.success('登录成功')
            router.push('/main')
          } else {
            ElMessage.error('用户名或密码错误')
          }
        }
        loading.value = false
      }, 500)
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: 20px;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.submit-btn {
  width: 100%;
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
}
</style>