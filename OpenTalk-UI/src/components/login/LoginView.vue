<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
      
      <!-- 金色粒子效果 -->
         <div class="particles">
           <div class="particle" v-for="n in 20" :key="n" :style="getParticleStyle(n)"></div>
         </div>
      
      <!-- 鼠标跟随光效 -->
      <div class="mouse-glow" ref="mouseGlow" :style="mouseGlowStyle"></div>
      
      <!-- 悬浮几何图形 -->
      <div class="floating-shapes">
        <div class="shape shape-diamond"></div>
        <div class="shape shape-triangle"></div>
        <div class="shape shape-hexagon"></div>
      </div>
      
      <!-- 时间显示区域 -->
        <div class="time-display-area">
          <!-- 背景有机链无穷大符号 -->
          <div class="organic-infinity-bg">
            <div class="organic-chain">
              <!-- 左侧循环链 -->
               <div class="chain-loop left-loop">
                 <div class="chain-node" v-for="n in 8" :key="'left-' + n" :style="getChainNodeStyle('left', n)"></div>
                 <div class="chain-connector" v-for="n in 8" :key="'left-conn-' + n" :style="getConnectorStyle('left', n)"></div>
               </div>
               <!-- 右侧循环链 -->
               <div class="chain-loop right-loop">
                 <div class="chain-node" v-for="n in 8" :key="'right-' + n" :style="getChainNodeStyle('right', n)"></div>
                 <div class="chain-connector" v-for="n in 8" :key="'right-conn-' + n" :style="getConnectorStyle('right', n)"></div>
               </div>
              <!-- 中心连接点 -->
              <div class="center-junction"></div>
              <!-- 流动能量 -->
               <div class="energy-flow">
                 <div class="energy-particle" v-for="n in 6" :key="n" :style="getEnergyStyle(n)"></div>
               </div>
            </div>
          </div>
          <!-- 前景时间显示 -->
          <div class="time-display-fg">
            <div class="current-time">{{ currentTime }}</div>
            <div class="date-info">{{ currentDate }}</div>
          </div>
        </div>
      
      <!-- 装饰性文字 -->
       <div class="decorative-text">
         <span class="luxury-text">TIMEMACHINELAB</span>
         <span class="experience-text">TMLive</span>
         <!-- 装饰元素 -->
         <div class="text-decorations">
           <div class="deco-line deco-line-1"></div>
           <div class="deco-line deco-line-2"></div>
           <div class="deco-dot deco-dot-1"></div>
           <div class="deco-dot deco-dot-2"></div>
           <div class="deco-dot deco-dot-3"></div>
           <div class="deco-glow"></div>
         </div>
       </div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card glass-effect">
      <!-- Logo和标题 -->
      <div class="login-header">
        <div class="logo-container">
          <div class="logo">OT</div>
        </div>
        <h1 class="title">{{ isRegister ? '加入 OpenTalk' : '欢迎回来' }}</h1>
        <p class="subtitle">{{ isRegister ? '创建账号，开始你的沟通之旅' : '登录到你的账号继续使用' }}</p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleSubmit" class="login-form">
        <!-- 用户名 -->
        <div class="form-group">
          <label class="form-label">用户名</label>
          <div class="input-container">
            <el-icon class="input-icon"><User /></el-icon>
            <input
              v-model="formData.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名"
              :class="{ 'error': errors.username }"
              @blur="validateField('username')"
              @input="clearError('username')"
            />
          </div>
          <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
        </div>

        <!-- 姓名（注册时显示） -->
        <div v-if="isRegister" class="form-group">
          <label class="form-label">姓名</label>
          <div class="input-container">
            <el-icon class="input-icon"><UserFilled /></el-icon>
            <input
              v-model="formData.name"
              type="text"
              class="form-input"
              placeholder="请输入真实姓名"
              :class="{ 'error': errors.name }"
              @blur="validateField('name')"
              @input="clearError('name')"
            />
          </div>
          <div v-if="errors.name" class="error-message">{{ errors.name }}</div>
        </div>

        <!-- 邮箱（注册时显示） -->
        <div v-if="isRegister" class="form-group">
          <label class="form-label">邮箱</label>
          <div class="input-container">
            <el-icon class="input-icon"><Message /></el-icon>
            <input
              v-model="formData.email"
              type="email"
              class="form-input"
              placeholder="请输入邮箱地址"
              :class="{ 'error': errors.email }"
              @blur="validateField('email')"
              @input="clearError('email')"
            />
          </div>
          <div v-if="errors.email" class="error-message">{{ errors.email }}</div>
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label class="form-label">密码</label>
          <div class="input-container">
            <el-icon class="input-icon"><Lock /></el-icon>
            <input
              v-model="formData.password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="请输入密码"
              :class="{ 'error': errors.password }"
              @blur="validateField('password')"
              @input="clearError('password')"
            />
            <button
              type="button"
              class="password-toggle"
              @click="showPassword = !showPassword"
            >
              <el-icon><View v-if="!showPassword" /><Hide v-else /></el-icon>
            </button>
          </div>
          <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
        </div>

        <!-- 确认密码（注册时显示） -->
        <div v-if="isRegister" class="form-group">
          <label class="form-label">确认密码</label>
          <div class="input-container">
            <el-icon class="input-icon"><Lock /></el-icon>
            <input
              v-model="formData.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="请再次输入密码"
              :class="{ 'error': errors.confirmPassword }"
              @blur="validateField('confirmPassword')"
              @input="clearError('confirmPassword')"
            />
            <button
              type="button"
              class="password-toggle"
              @click="showConfirmPassword = !showConfirmPassword"
            >
              <el-icon><View v-if="!showConfirmPassword" /><Hide v-else /></el-icon>
            </button>
          </div>
          <div v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</div>
        </div>

        <!-- 记住我（登录时显示） -->
        <div v-if="!isRegister" class="form-group">
          <label class="checkbox-container">
            <input v-model="rememberMe" type="checkbox" class="checkbox" />
            <span class="checkbox-label">记住我</span>
          </label>
        </div>

        <!-- 提交按钮 -->
        <button
          type="submit"
          class="submit-btn btn-primary btn-lg"
          :disabled="loading"
          :class="{ 'loading': loading }"
        >
          <el-icon v-if="loading" class="loading-icon"><Loading /></el-icon>
          {{ isRegister ? '创建账号' : '登录' }}
        </button>
      </form>

      <!-- 切换模式 -->
      <div class="form-footer">
        <p class="switch-text">
          {{ isRegister ? '已有账号？' : '还没有账号？' }}
          <button
            type="button"
            class="switch-btn"
            @click="toggleMode"
          >
            {{ isRegister ? '立即登录' : '立即注册' }}
          </button>
        </p>
      </div>

      <!-- 社交登录（可选） -->
      <div class="social-login">
        <div class="divider">
          <span>或</span>
        </div>
        <div class="social-buttons">
          <button type="button" class="social-btn">
            <el-icon><Platform /></el-icon>
            <span>微信登录</span>
          </button>
          <button type="button" class="social-btn">
            <el-icon><Iphone /></el-icon>
            <span>Apple ID</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  UserFilled,
  Lock,
  Message,
  View,
  Hide,
  Loading,
  Platform,
  Iphone
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const isRegister = ref(false)
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const rememberMe = ref(false)

// 动效相关
const mouseGlow = ref(null)
const mouseGlowStyle = ref({ left: '0px', top: '0px', opacity: '0' })
const currentTime = ref('')
const currentDate = ref('')

// 粒子样式生成
const getParticleStyle = (index: number) => {
  const delay = Math.random() * 10
  const duration = 15 + Math.random() * 10
  const size = 2 + Math.random() * 4
  const left = Math.random() * 100
  const opacity = 0.3 + Math.random() * 0.4
  
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size}px`,
    opacity: opacity
  }
}

// 有机链节点样式生成
const getChainNodeStyle = (side: string, index: number) => {
  const angle = (index - 1) * 45 // 每个节点间隔45度
  const radius = 60
  const centerX = side === 'left' ? 80 : 220
  const centerY = 80
  
  const x = centerX + radius * Math.cos(angle * Math.PI / 180)
  const y = centerY + radius * Math.sin(angle * Math.PI / 180)
  
  return {
    left: `${x}px`,
    top: `${y}px`,
    animationDelay: `${index * 0.2}s`
  }
}

// 连接器样式生成
const getConnectorStyle = (side: string, index: number) => {
  const angle = (index - 1) * 45
  const nextAngle = index * 45
  const radius = 60
  const centerX = side === 'left' ? 80 : 220
  const centerY = 80
  
  const x1 = centerX + radius * Math.cos(angle * Math.PI / 180)
  const y1 = centerY + radius * Math.sin(angle * Math.PI / 180)
  const x2 = centerX + radius * Math.cos(nextAngle * Math.PI / 180)
  const y2 = centerY + radius * Math.sin(nextAngle * Math.PI / 180)
  
  const length = Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2)
  const rotation = Math.atan2(y2 - y1, x2 - x1) * 180 / Math.PI
  
  return {
    left: `${x1}px`,
    top: `${y1}px`,
    width: `${length}px`,
    transform: `rotate(${rotation}deg)`,
    animationDelay: `${index * 0.2 + 0.1}s`
  }
}

// 能量粒子样式生成
const getEnergyStyle = (index: number) => {
  const delay = (index - 1) * 1.2
  const duration = 7.2
  
  return {
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}



// 鼠标移动事件
const handleMouseMove = (e: MouseEvent) => {
  mouseGlowStyle.value = {
    left: `${e.clientX - 100}px`,
    top: `${e.clientY - 100}px`,
    opacity: '0.6'
  }
}

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { 
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

// 生命周期
let timeInterval: NodeJS.Timeout

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  document.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
  document.removeEventListener('mousemove', handleMouseMove)
})

// 表单数据
const formData = reactive({
  username: '',
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 错误信息
const errors = reactive({
  username: '',
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 验证规则
const validateField = (field: string) => {
  switch (field) {
    case 'username':
      if (!formData.username) {
        errors.username = '请输入用户名'
      } else if (formData.username.length < 3) {
        errors.username = '用户名至少3个字符'
      } else if (formData.username.length > 20) {
        errors.username = '用户名不能超过20个字符'
      } else {
        errors.username = ''
      }
      break
    
    case 'name':
      if (isRegister.value && !formData.name) {
        errors.name = '请输入姓名'
      } else {
        errors.name = ''
      }
      break
    
    case 'email':
      if (isRegister.value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!formData.email) {
          errors.email = '请输入邮箱地址'
        } else if (!emailRegex.test(formData.email)) {
          errors.email = '请输入有效的邮箱地址'
        } else {
          errors.email = ''
        }
      }
      break
    
    case 'password':
      if (!formData.password) {
        errors.password = '请输入密码'
      } else if (formData.password.length < 6) {
        errors.password = '密码至少6个字符'
      } else {
        errors.password = ''
      }
      break
    
    case 'confirmPassword':
      if (isRegister.value) {
        if (!formData.confirmPassword) {
          errors.confirmPassword = '请确认密码'
        } else if (formData.password !== formData.confirmPassword) {
          errors.confirmPassword = '两次输入的密码不一致'
        } else {
          errors.confirmPassword = ''
        }
      }
      break
  }
}

// 清除错误信息
const clearError = (field: string) => {
  errors[field as keyof typeof errors] = ''
}

// 验证所有字段
const validateForm = () => {
  validateField('username')
  validateField('password')
  
  if (isRegister.value) {
    validateField('name')
    validateField('email')
    validateField('confirmPassword')
  }
  
  return Object.values(errors).every(error => !error)
}

// 切换登录/注册模式
const toggleMode = () => {
  isRegister.value = !isRegister.value
  // 清空表单和错误信息
  Object.keys(formData).forEach(key => {
    formData[key as keyof typeof formData] = ''
  })
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = ''
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }
  
  loading.value = true
  
  try {
    if (isRegister.value) {
      // 注册逻辑
      const success = userStore.register(
        formData.username,
        formData.password,
        formData.name,
        formData.email
      )
      
      if (success) {
        ElMessage.success('注册成功！')
        // 注册成功后自动登录
        const loginSuccess = userStore.login(formData.username, formData.password)
        if (loginSuccess) {
          await router.push('/main')
        }
      } else {
        ElMessage.error('注册失败，用户名可能已存在')
      }
    } else {
      // 登录逻辑
      const success = userStore.login(formData.username, formData.password)
      
      if (success) {
        ElMessage.success('登录成功！')
        await router.push('/main')
      } else {
        ElMessage.error('登录失败，请检查用户名和密码')
      }
    }
  } catch (error) {
    console.error('Login error:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 25%, #2d2d30 50%, #1a1a1a 75%, #0a0a0a 100%);
  padding: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 20%, rgba(255, 215, 0, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 140, 0, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 60%, rgba(255, 215, 0, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.2) 0%, rgba(255, 140, 0, 0.1) 50%, transparent 70%);
  animation: float 6s ease-in-out infinite;
  border: 1px solid rgba(255, 215, 0, 0.1);
}

.bg-circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.bg-circle-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.bg-circle-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(26, 26, 26, 0.95) 0%, rgba(45, 45, 48, 0.9) 100%);
  border: 1px solid rgba(255, 215, 0, 0.2);
  backdrop-filter: blur(20px);
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.6),
    0 0 0 1px rgba(255, 215, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  animation: slideInUp 0.6s ease;
  position: relative;
  z-index: 1;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 头部 */
.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.logo-container {
  margin-bottom: var(--spacing-lg);
}

.logo {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #ffd700 0%, #ff8c00 100%);
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #000000;
  font-size: 24px;
  font-weight: 800;
  box-shadow: 
    0 10px 30px rgba(255, 215, 0, 0.4),
    0 4px 15px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 215, 0, 0.3);
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.subtitle {
  color: #cccccc;
  font-size: 16px;
}

/* 表单 */
.login-form {
  margin-bottom: var(--spacing-lg);
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  margin-bottom: 8px;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: #ffd700;
  z-index: 1;
}

.form-input {
  width: 100%;
  height: 52px;
  padding: 0 16px 0 48px;
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 12px;
  font-size: 16px;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d30 100%);
  color: #ffffff;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.form-input:focus {
  outline: none;
  border-color: #ffd700;
  box-shadow: 
    0 0 0 4px rgba(255, 215, 0, 0.25),
    0 4px 12px rgba(255, 215, 0, 0.2);
  background: linear-gradient(135deg, #2d2d30 0%, #1a1a1a 100%);
}

.form-input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.25);
}

.form-input::placeholder {
  color: #888888;
}

.password-toggle {
  position: absolute;
  right: 16px;
  background: none;
  border: none;
  color: #ffd700;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.password-toggle:hover {
  color: #ffed4e;
  background: rgba(255, 215, 0, 0.1);
}

.error-message {
  color: #fca5a5;
  font-size: 12px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 复选框 */
.checkbox-container {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.checkbox {
  width: 18px;
  height: 18px;
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 4px;
  cursor: pointer;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d30 100%);
  transition: all 0.3s ease;
}

.checkbox:checked {
  background: linear-gradient(135deg, #ffd700 0%, #ff8c00 100%);
  border-color: #ffd700;
}

.checkbox-label {
  font-size: 14px;
  color: #cccccc;
  cursor: pointer;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 52px;
  margin-bottom: 24px;
  position: relative;
  background: linear-gradient(135deg, #ffd700 0%, #ff8c00 100%);
  color: #000000;
  font-weight: 600;
  font-size: 16px;
  border: 1px solid rgba(255, 215, 0, 0.5);
  border-radius: 12px;
  box-shadow: 
    0 10px 30px rgba(255, 215, 0, 0.4),
    0 4px 15px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  cursor: pointer;
}

.submit-btn:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
  box-shadow: 
    0 15px 35px rgba(255, 215, 0, 0.5),
    0 6px 20px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.submit-btn.loading {
  pointer-events: none;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-bottom: 24px;
}

.switch-text {
  color: #cccccc;
  font-size: 14px;
}

.switch-btn {
  background: none;
  border: none;
  color: #ffd700;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: underline;
  transition: all 0.3s ease;
}

.switch-btn:hover {
  color: #ffed4e;
  text-shadow: 0 0 8px rgba(255, 215, 0, 0.5);
}

/* 社交登录 */
.social-login {
  margin-top: 24px;
}

.divider {
  position: relative;
  text-align: center;
  margin-bottom: 24px;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(255, 215, 0, 0.2);
}

.divider span {
  background: linear-gradient(135deg, rgba(26, 26, 26, 0.95) 0%, rgba(45, 45, 48, 0.9) 100%);
  padding: 0 16px;
  color: #888888;
  font-size: 14px;
}

.social-buttons {
  display: flex;
  gap: 12px;
}

.social-btn {
  flex: 1;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(135deg, #2d2d30 0%, #1a1a1a 100%);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 12px;
  color: #ffffff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.social-btn:hover {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d30 100%);
  border-color: #ffd700;
  color: #ffd700;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.2);
}

/* 动效元素样式 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.particle {
  position: absolute;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.8) 0%, rgba(255, 140, 0, 0.4) 50%, transparent 70%);
  border-radius: 50%;
  animation: particleFloat linear infinite;
}

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

.mouse-glow {
  position: fixed;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.3) 0%, rgba(255, 140, 0, 0.1) 50%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  transition: opacity 0.3s ease;
  z-index: 0;
}

.floating-shapes {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  animation: shapeFloat 8s ease-in-out infinite;
}

.shape-diamond {
  top: 15%;
  left: 15%;
  width: 20px;
  height: 20px;
  background: linear-gradient(45deg, rgba(255, 215, 0, 0.6), rgba(255, 140, 0, 0.4));
  transform: rotate(45deg);
  animation-delay: 0s;
}

.shape-triangle {
  top: 70%;
  right: 20%;
  width: 0;
  height: 0;
  border-left: 12px solid transparent;
  border-right: 12px solid transparent;
  border-bottom: 20px solid rgba(255, 215, 0, 0.5);
  animation-delay: 2s;
}

.shape-hexagon {
  bottom: 25%;
  left: 25%;
  width: 16px;
  height: 16px;
  background: rgba(255, 215, 0, 0.4);
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  animation-delay: 4s;
}

@keyframes shapeFloat {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
  }
}

.time-display-area {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 320px;
  height: 180px;
  z-index: 1;
}

.organic-infinity-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transform: rotate(-15deg);
  opacity: 0.6;
}

.organic-chain {
  position: relative;
  width: 100%;
  height: 100%;
}

.chain-loop {
  position: absolute;
  width: 120px;
  height: 120px;
}

.left-loop {
  top: 30px;
  left: 20px;
}

.right-loop {
  top: 30px;
  right: 20px;
}

.chain-node {
  position: absolute;
  width: 12px;
  height: 12px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.9) 0%, rgba(255, 140, 0, 0.6) 70%, transparent 100%);
  border-radius: 50%;
  border: 2px solid rgba(255, 215, 0, 0.4);
  box-shadow: 
    0 0 8px rgba(255, 215, 0, 0.6),
    inset 0 0 4px rgba(255, 255, 255, 0.3);
  animation: nodeGlow 3s ease-in-out infinite;
}

.chain-connector {
  position: absolute;
  height: 3px;
  background: linear-gradient(90deg, 
    rgba(255, 215, 0, 0.8) 0%, 
    rgba(255, 140, 0, 0.6) 50%, 
    rgba(255, 215, 0, 0.8) 100%);
  border-radius: 2px;
  box-shadow: 0 0 4px rgba(255, 215, 0, 0.4);
  animation: connectorPulse 2s ease-in-out infinite;
  transform-origin: left center;
}

.center-junction {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  background: radial-gradient(circle, rgba(255, 215, 0, 1) 0%, rgba(255, 140, 0, 0.8) 70%);
  border-radius: 50%;
  border: 3px solid rgba(255, 215, 0, 0.6);
  box-shadow: 
    0 0 12px rgba(255, 215, 0, 0.8),
    inset 0 0 6px rgba(255, 255, 255, 0.4);
  animation: junctionPulse 2.5s ease-in-out infinite;
}

.energy-flow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.energy-particle {
  position: absolute;
  width: 6px;
  height: 6px;
  background: rgba(255, 215, 0, 1);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.8);
  animation: energyFlow linear infinite;
}

.time-display-fg {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: rgba(255, 215, 0, 0.95);
  font-family: 'Courier New', monospace;
  z-index: 2;
  padding: 15px 20px;
}

.current-time {
  font-size: 28px;
  font-weight: bold;
  text-shadow: 0 0 15px rgba(255, 215, 0, 0.8);
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.date-info {
  font-size: 11px;
  color: rgba(255, 215, 0, 0.7);
  text-transform: uppercase;
  letter-spacing: 2px;
}



@keyframes nodeGlow {
  0%, 100% {
    box-shadow: 
      0 0 8px rgba(255, 215, 0, 0.6),
      inset 0 0 4px rgba(255, 255, 255, 0.3);
    transform: scale(1);
  }
  50% {
    box-shadow: 
      0 0 15px rgba(255, 215, 0, 0.9),
      inset 0 0 8px rgba(255, 255, 255, 0.5);
    transform: scale(1.1);
  }
}

@keyframes connectorPulse {
  0%, 100% {
    opacity: 0.6;
    box-shadow: 0 0 4px rgba(255, 215, 0, 0.4);
  }
  50% {
    opacity: 1;
    box-shadow: 0 0 8px rgba(255, 215, 0, 0.8);
  }
}

@keyframes junctionPulse {
  0%, 100% {
    box-shadow: 
      0 0 12px rgba(255, 215, 0, 0.8),
      inset 0 0 6px rgba(255, 255, 255, 0.4);
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    box-shadow: 
      0 0 20px rgba(255, 215, 0, 1),
      inset 0 0 10px rgba(255, 255, 255, 0.6);
    transform: translate(-50%, -50%) scale(1.2);
  }
}

@keyframes energyFlow {
  0% {
    left: 80px;
    top: 80px;
    opacity: 1;
  }
  12.5% {
    left: 120px;
    top: 50px;
    opacity: 1;
  }
  25% {
    left: 140px;
    top: 80px;
    opacity: 1;
  }
  37.5% {
    left: 120px;
    top: 110px;
    opacity: 1;
  }
  50% {
    left: 160px;
    top: 80px;
    opacity: 1;
  }
  62.5% {
    left: 200px;
    top: 50px;
    opacity: 1;
  }
  75% {
    left: 220px;
    top: 80px;
    opacity: 1;
  }
  87.5% {
    left: 200px;
    top: 110px;
    opacity: 1;
  }
  100% {
    left: 80px;
    top: 80px;
    opacity: 1;
  }
}

.decorative-text {
  position: absolute;
  bottom: 40px;
  left: 40px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
  z-index: 1;
}

.luxury-text {
  font-size: 28px;
  font-weight: 900;
  color: rgba(255, 215, 0, 0.1);
  text-transform: uppercase;
  letter-spacing: 3px;
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

.experience-text {
  font-size: 16px;
  color: rgba(255, 215, 0, 0.3);
  text-transform: uppercase;
  letter-spacing: 6px;
  margin-left: 15px;
}

.text-decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
}

.deco-line {
  position: absolute;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 215, 0, 0.2) 50%, transparent 100%);
  border-radius: 1px;
}

.deco-line-1 {
  top: 20%;
  left: -50px;
  width: 80px;
  height: 1px;
  animation: lineGlow 4s ease-in-out infinite;
}

.deco-line-2 {
  bottom: 30%;
  right: -40px;
  width: 60px;
  height: 1px;
  animation: lineGlow 4s ease-in-out infinite 2s;
}

.deco-dot {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(255, 215, 0, 0.4);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.3);
}

.deco-dot-1 {
  top: 15%;
  left: -30px;
  animation: dotPulse 3s ease-in-out infinite;
}

.deco-dot-2 {
  top: 60%;
  right: -25px;
  animation: dotPulse 3s ease-in-out infinite 1s;
}

.deco-dot-3 {
  bottom: 20%;
  left: -20px;
  animation: dotPulse 3s ease-in-out infinite 2s;
}

.deco-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.05) 0%, transparent 70%);
  border-radius: 50%;
  animation: glowPulse 6s ease-in-out infinite;
}

@keyframes lineGlow {
  0%, 100% {
    opacity: 0.2;
    transform: scaleX(0.5);
  }
  50% {
    opacity: 0.6;
    transform: scaleX(1);
  }
}

@keyframes dotPulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.5);
  }
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.3;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-page {
    padding: 16px;
  }
  
  .login-card {
    padding: 24px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .social-buttons {
    flex-direction: column;
  }
  
  .time-display-area {
    top: 15px;
    right: 15px;
    width: 280px;
    height: 140px;
  }
  
  .organic-infinity-bg {
    transform: rotate(-10deg);
  }
  
  .chain-loop {
    width: 90px;
    height: 90px;
  }
  
  .left-loop {
    top: 25px;
    left: 15px;
  }
  
  .right-loop {
    top: 25px;
    right: 15px;
  }
  
  .chain-node {
    width: 10px;
    height: 10px;
  }
  
  .chain-connector {
    height: 2px;
  }
  
  .center-junction {
    width: 12px;
    height: 12px;
  }
  
  .energy-particle {
    width: 4px;
    height: 4px;
  }
  
  .current-time {
    font-size: 22px;
  }
  
  .date-info {
    font-size: 10px;
  }
  
  .decorative-text {
    bottom: 20px;
    left: 20px;
  }
  
  .luxury-text {
    font-size: 20px;
    letter-spacing: 2px;
  }
  
  .experience-text {
    font-size: 14px;
    letter-spacing: 4px;
  }
  
  .deco-line-1 {
    width: 40px;
    left: -25px;
  }
  
  .deco-line-2 {
    width: 30px;
    right: -20px;
  }
  
  .deco-dot {
    width: 3px;
    height: 3px;
  }
  
  .deco-dot-1 {
    left: -15px;
  }
  
  .deco-dot-2 {
    right: -12px;
  }
  
  .deco-dot-3 {
    left: -10px;
  }
  
  .deco-glow {
    width: 120px;
    height: 120px;
  }
}
</style>