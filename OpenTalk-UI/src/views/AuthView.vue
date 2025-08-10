<template>
  <div class="auth-page">
    <!-- 左侧品牌面板（桌面端可见） -->
    <aside class="brand-panel">
      <div class="brand-content">
        <div class="logo">
          <span class="mark" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="white" stroke-width="2">
              <path d="M12 2v20M2 12h20" />
            </svg>
          </span>
          <span>ACME Enterprise</span>
        </div>

        <h1>Secure access to your enterprise workspace</h1>
        <p>Single sign-on, compliance ready, and built for large organizations.</p>

        <ul class="bullets" aria-label="Key highlights">
          <li>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 6L9 17l-5-5" />
            </svg>
            SSO with Google & Microsoft Azure AD
          </li>
          <li>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 6L9 17l-5-5" />
            </svg>
            SOC 2 / ISO 27001 security practices
          </li>
          <li>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 6L9 17l-5-5" />
            </svg>
            Global data residency & audit logs
          </li>
        </ul>

        <footer>
          Need help? Contact support@acme.inc
        </footer>
      </div>
    </aside>

    <!-- 右侧表单面板 -->
    <main class="form-panel">
      <div class="card" role="region" aria-label="Sign in form">
        <header>
          <h2>Sign in</h2>
          <p class="subtitle">Use your work email to continue</p>
        </header>

        <form @submit.prevent="onSubmit" novalidate>
          <div class="field">
            <label for="email">Work email</label>
            <input
                id="email"
                type="email"
                v-model.trim="email"
                @blur="touched.email = true"
                :class="{ invalid: errorEmail }"
                placeholder="name@company.com"
                autocomplete="email"
                inputmode="email"
                required
                aria-describedby="emailHelp"
            />
            <p id="emailHelp" v-if="errorEmail" class="error" role="alert">{{ errorEmail }}</p>
          </div>

          <div class="field">
            <label for="password">Password</label>
            <div class="password">
              <input
                  :type="showPw ? 'text' : 'password'"
                  id="password"
                  v-model="password"
                  @blur="touched.password = true"
                  :class="{ invalid: errorPassword }"
                  autocomplete="current-password"
                  required
              />
              <button
                  type="button"
                  class="ghost"
                  @click="showPw = !showPw"
                  :aria-pressed="showPw ? 'true' : 'false'"
                  aria-label="Toggle password visibility"
              >
                {{ showPw ? 'Hide' : 'Show' }}
              </button>
            </div>

            <div class="row">
              <label class="checkbox">
                <input type="checkbox" v-model="remember" />
                Remember me
              </label>
              <a href="#" class="link" @click.prevent="onForgot">Forgot password?</a>
            </div>
          </div>

          <button class="btn primary" type="submit" :disabled="loading" :aria-busy="loading ? 'true' : 'false'">
            <span v-if="loading">Signing in…</span>
            <span v-else>Sign in</span>
          </button>
        </form>

        <div class="divider" aria-hidden="true"><span>or</span></div>

        <div class="sso-row">
          <button class="btn sso" @click="sso('google')" aria-label="Continue with Google">
            <svg class="sso-icon" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <circle cx="12" cy="12" r="10" fill="#ffffff10" />
              <text x="12" y="16" text-anchor="middle" font-size="12" fill="#fff">G</text>
            </svg>
            Continue with Google
          </button>
          <button class="btn sso" @click="sso('microsoft')" aria-label="Continue with Microsoft">
            <svg class="sso-icon" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <rect x="4" y="4" width="7" height="7" fill="#f25022" />
              <rect x="13" y="4" width="7" height="7" fill="#7fba00" />
              <rect x="4" y="13" width="7" height="7" fill="#00a4ef" />
              <rect x="13" y="13" width="7" height="7" fill="#ffb900" />
            </svg>
            Continue with Microsoft
          </button>
        </div>

        <p class="terms">
          By continuing you agree to our
          <a href="#">Terms</a> and <a href="#">Privacy Policy</a>.
        </p>
      </div>

      <footer class="foot" aria-label="Footer">
        <span>© {{ year }} Acme Inc.</span>
        <nav class="links" aria-label="Footer links">
          <a href="#">Status</a>
          <a href="#">Security</a>
          <a href="#">Help</a>
        </nav>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const email = ref('')
const password = ref('')
const remember = ref(true)
const showPw = ref(false)
const loading = ref(false)
const touched = ref({ email: false, password: false })
const year = new Date().getFullYear()

const errorEmail = computed(() => {
  if (!touched.value.email) return ''
  if (!email.value) return 'Email is required'
  const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)
  return ok ? '' : 'Enter a valid email'
})

const errorPassword = computed(() => {
  if (!touched.value.password) return ''
  return password.value.length >= 8 ? '' : 'Min 8 characters'
})

async function onSubmit() {
  touched.value.email = true
  touched.value.password = true
  if (errorEmail.value || errorPassword.value) return
  loading.value = true
  try {
    // TODO: 替换为你的实际鉴权逻辑
    await new Promise((r) => setTimeout(r, 800))
    router.push({ name: 'chat' })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function onForgot() {
  // TODO: 跳转到忘记密码流程
  alert('Redirect to password reset flow')
}

function sso(provider) {
  // TODO: 接入你们的 SSO 流程
  alert('SSO: ' + provider)
}
</script>

<style scoped>
/* 页面网格与背景 */
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr;
  background: linear-gradient(180deg, #0b1220, #0a0f1a);
  color: #e5e7eb;
}
@media (min-width: 960px) {
  .auth-page {
    grid-template-columns: 1.1fr 1fr;
  }
}

/* 左侧品牌面板（桌面） */
.brand-panel {
  display: none;
}
@media (min-width: 960px) {
  .brand-panel {
    display: block;
    position: relative;
    padding: 48px;
    background:
        radial-gradient(1200px 600px at -10% -20%, rgba(59,130,246,.35), transparent 60%),
        radial-gradient(1000px 500px at 110% 120%, rgba(16,185,129,.25), transparent 60%),
        linear-gradient(180deg, #0b1220, #0f172a);
  }
  .brand-content {
    position: sticky;
    top: 48px;
    max-width: 560px;
  }
  .logo {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    font-weight: 800;
    font-size: 18px;
    color: #fff;
  }
  .logo .mark {
    width: 36px;
    height: 36px;
    display: grid;
    place-items: center;
    background: linear-gradient(135deg, #2563eb, #9333ea);
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(37, 99, 235, .35);
  }
  .brand-panel h1 {
    margin: 20px 0 12px;
    font-size: 40px;
    line-height: 1.1;
    letter-spacing: -0.02em;
  }
  .brand-panel p {
    color: #b6c2d9;
    margin-bottom: 16px;
  }
  .bullets {
    margin: 24px 0;
    padding: 0;
    list-style: none;
  }
  .bullets li {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    margin: 10px 0;
    color: #cbd5e1;
  }
  .bullets svg {
    flex: 0 0 auto;
    color: #34d399;
    margin-top: 2px;
  }
  .brand-panel footer {
    margin-top: 32px;
    color: #9aa7bd;
    font-size: 13px;
  }
}

/* 右侧表单区 */
.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 32px 20px;
}
@media (min-width: 480px) {
  .form-panel { padding: 48px; }
}

.card {
  background: rgba(17, 24, 39, .6);
  border: 1px solid rgba(148, 163, 184, .18);
  backdrop-filter: saturate(120%) blur(8px);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, .35);
  max-width: 480px;
  width: 100%;
  margin: 0 auto;
}
.card h2 {
  font-size: 28px;
  margin: 4px 0 8px;
  font-weight: 800;
}
.subtitle {
  color: #9aa7bd;
  margin-bottom: 18px;
}

/* 表单元素 */
.field { margin: 14px 0 18px; }
label {
  display: block;
  font-size: 13px;
  color: #9aa7bd;
  margin-bottom: 8px;
}

input[type="email"], input[type="password"], input[type="text"] {
  width: 100%;
  appearance: none;
  border: 1px solid rgba(148, 163, 184, .25);
  background: #0b1220;
  color: #e5e7eb;
  border-radius: 12px;
  padding: 12px 14px;
  outline: none;
  transition: border-color .2s, box-shadow .2s;
}
input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, .25);
}

.password {
  display: flex;
  align-items: center;
  gap: 8px;
}
.password input { flex: 1; }
.password .ghost {
  border: 1px solid rgba(148, 163, 184, .25);
  background: #0b1220;
  color: #cbd5e1;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.password .ghost:hover {
  border-color: #94a3b8;
  color: #fff;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #cbd5e1;
  font-size: 13px;
}
.link {
  color: #93c5fd;
  font-size: 13px;
  text-decoration: none;
}
.link:hover { text-decoration: underline; }

/* 按钮 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-radius: 12px;
  padding: 12px 14px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: transform .03s ease, box-shadow .2s ease, filter .2s;
}
.btn:active { transform: translateY(1px); }
.btn.primary {
  width: 100%;
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  color: white;
  box-shadow: 0 10px 30px rgba(37, 99, 235, .35);
}
.btn.primary:hover { filter: brightness(1.07); }
.btn.sso {
  width: 100%;
  background: #0b1220;
  color: #e5e7eb;
  border: 1px solid rgba(148, 163, 184, .25);
}
.btn.sso:hover {
  border-color: #94a3b8;
  color: #fff;
}
.sso-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
  margin-top: 10px;
}
@media (min-width: 420px) {
  .sso-row { grid-template-columns: 1fr 1fr; }
}

.sso-icon { opacity: .9; }

.error {
  color: #fca5a5;
  font-size: 12px;
  margin-top: 6px;
}
.invalid {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, .25) !important;
}

.divider {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 16px 0;
  color: #94a3b8;
  font-size: 12px;
}
.divider::before, .divider::after {
  content: '';
  height: 1px;
  flex: 1;
  background: rgba(148, 163, 184, .25);
}

.terms {
  color: #94a3b8;
  font-size: 12px;
  margin-top: 14px;
}

.foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  color: #9aa7bd;
  font-size: 12px;
}
.foot .links {
  display: flex;
  gap: 12px;
}
.foot a {
  color: #9ec1ff;
  text-decoration: none;
}
.foot a:hover { text-decoration: underline; }

/* 降低动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .btn { transition: none; }
  input:focus { box-shadow: none; }
}
</style>