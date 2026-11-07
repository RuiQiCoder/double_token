<script setup>
import { computed, ref } from 'vue'
import { authenticate, request, refresh, setToken, hasToken } from './api'

const mock = ref(false)
const registering = ref(false)
const username = ref('')
const password = ref('')
const visible = ref(false)
const busy = ref(false)
const notice = ref('')
const noticeType = ref('')
const user = ref('')
const signedIn = ref(hasToken())
const logs = ref([])
const demoAccounts = new Map([['demo_user', 'Demo@123']])
const sessionLabel = computed(() => signedIn.value ? '会话已建立' : '等待身份验证')
function tell(message, type = 'error') { notice.value = message; noticeType.value = type }
function record(action, result, ok = true) {
  logs.value.unshift({ id: Date.now() + Math.random(), action, result, ok, time: new Date().toLocaleTimeString('zh-CN', { hour12: false }) })
  logs.value = logs.value.slice(0, 6)
}
function switchMode() {
  setToken(''); signedIn.value = false; user.value = ''; notice.value = ''; logs.value = []
}
async function submit() {
  if (busy.value) return
  notice.value = ''
  if (!/^[a-zA-Z0-9_]{4,20}$/.test(username.value)) return tell('用户名需为 4–20 位字母、数字或下划线')
  if (!/^(?=.*[0-9])(?=.*[A-Za-z])(?=.*[\x21-\x2F\x3A-\x40\x5B-\x60\x7B-\x7E])[\x21-\x7E]{6,26}$/.test(password.value)) return tell('密码需为 6–26 位，包含字母、数字和特殊字符')
  busy.value = true
  try {
    if (mock.value) {
      if (registering.value) {
        if (demoAccounts.has(username.value)) throw new Error('该用户已注册')
        demoAccounts.set(username.value, password.value)
      } else if (demoAccounts.get(username.value) !== password.value) throw new Error('用户名或密码错误')
    } else await authenticate(username.value, password.value, registering.value)
    if (registering.value) {
      registering.value = false
      tell('注册成功，请使用此账号登录', 'success')
      record('注册账号', '账号创建成功')
    } else {
      signedIn.value = true; user.value = username.value; password.value = ''
      tell('登录成功，欢迎回来', 'success'); record('身份验证', '登录成功，访问令牌已就绪')
    }
  } catch (error) { tell(error.message); record(registering.value ? '注册账号' : '身份验证', error.message, false) }
  finally { busy.value = false }
}
async function test(action) {
  if (busy.value) return
  busy.value = true; notice.value = ''
  try {
    let result
    if (mock.value) {
      if (action !== 'public' && !signedIn.value) throw new Error('未登录或登录已过期，请先登录')
      result = action === 'public' ? 't1' : action === 'protected' ? `t2，我是：${user.value}` : '模拟访问令牌已刷新'
    } else {
      const response = action === 'refresh' ? await refresh() : await request(action === 'public' ? '/test/t1' : '/test/t2')
      result = action === 'refresh' ? '访问令牌已刷新' : typeof response === 'string' ? response : JSON.stringify(response)
      if (hasToken()) signedIn.value = true
    }
    record(action === 'public' ? '公开接口 /test/t1' : action === 'protected' ? '受保护接口 /test/t2' : '刷新令牌', result)
    tell(result, 'success')
  } catch (error) {
    if ([401, 40102].includes(error.code)) { signedIn.value = false; user.value = '' }
    record('接口验证', error.message, false); tell(error.message)
  } finally { busy.value = false }
}
function logout() {
  setToken(''); signedIn.value = false; user.value = ''; password.value = ''
  tell('已清除访问令牌及其本地存储。后端未提供注销接口，刷新 Cookie 仍由浏览器保留。', 'success')
}
function fillDemo() { username.value = 'demo_user'; password.value = 'Demo@123'; notice.value = '' }
</script>

<template>
  <div class="shell">
    <header class="topbar"><a class="brand" href="/" aria-label="Login Lab 首页"><span class="brand-icon">L<span>↗</span></span>Login Lab<span class="brand-tag">身份认证实验室</span></a><span class="version">SPRING BOOT × VUE <i></i> v1.0</span></header>
    <main>
      <div class="page-heading"><div><div class="eyebrow">YOUR ACCESS STARTS HERE</div><h1>连接，从安全登录开始。</h1><p>一个轻量、直观的身份认证工作台。</p></div><label class="mode-switch"><input type="checkbox" v-model="mock" :disabled="busy" @change="switchMode"><span class="toggle"></span>本地模拟<span class="mode-badge">{{ mock ? 'MOCK' : 'LIVE API' }}</span></label></div>
      <div class="workspace">
        <section class="story-panel"><div class="story-top"><span class="pill"><i></i> SECURE BY DESIGN</span><span class="index">01 / AUTH</span></div><div class="orbit-art" aria-hidden="true"><div class="orbit orbit-one"></div><div class="orbit orbit-two"></div><div class="orbit orbit-three"></div><span class="orbit-dot dot-one"></span><span class="orbit-dot dot-two"></span><div class="lock-tile"><div class="lock-loop"></div><div class="lock-body">•</div></div><span class="art-tag tag-top">JWT AUTHENTICATION</span><span class="art-tag tag-bottom">✓ 身份已加密保护</span></div><div class="story-copy"><h2>简单的体验，<br>可靠的每一次连接。</h2><p>从账号验证到访问授权，<br>让身份认证的每一步都清晰可见。</p></div><div class="feature-row"><span>◈ JWT 认证</span><span>↻ 自动续期</span><span>⊞ 权限控制</span></div></section>
        <section class="auth-panel"><div class="section-kicker">ACCOUNT ACCESS <span>{{ mock ? '模拟环境' : '后端联调' }}</span></div>
          <template v-if="!signedIn"><h2>{{ registering ? '创建新账号' : '欢迎回来' }}<span class="wave">{{ registering ? ' ✦' : ' ↗' }}</span></h2><p class="subtitle">{{ registering ? '填写以下信息，开始你的第一次连接。' : '输入你的账号信息，开始本次安全会话。' }}</p><div class="tabs"><button :class="{ active: !registering }" :disabled="busy" @click="registering = false; notice = ''">账号登录</button><button :class="{ active: registering }" :disabled="busy" @click="registering = true; notice = ''">注册账号</button></div>
          <form @submit.prevent="submit"><label for="username">用户名</label><div class="input-wrap"><span>♙</span><input id="username" v-model="username" autocomplete="username" placeholder="请输入用户名" maxlength="20" :disabled="busy" required></div><label for="password">密码</label><div class="input-wrap"><span>⌑</span><input id="password" v-model="password" :type="visible ? 'text' : 'password'" :autocomplete="registering ? 'new-password' : 'current-password'" placeholder="请输入密码" maxlength="26" :disabled="busy" required><button class="show-password" type="button" @click="visible = !visible" :aria-label="visible ? '隐藏密码' : '显示密码'">{{ visible ? '隐藏' : '显示' }}</button></div><p class="field-help">6–26 位，包含字母、数字及特殊字符</p><button class="primary" :disabled="busy">{{ busy ? '正在处理…' : registering ? '创建账号' : '安全登录' }}<span>→</span></button></form><button v-if="mock" class="demo-fill" @click="fillDemo" :disabled="busy">填入演示账号 demo_user / Demo@123</button><div class="security-note"><span>♧</span> {{ mock ? '模拟账号仅保存在当前页面内存中' : '通过真实后端验证，密码不会保存在浏览器中' }}</div></template>
          <template v-else><div class="success-icon">✓</div><h2>连接成功</h2><p class="subtitle">{{ user || '当前用户' }}，你的会话已准备就绪。</p><div class="session-card"><span>当前认证状态</span><strong><i></i>已登录</strong><p>{{ mock ? '正在使用本地模拟会话' : '访问令牌已缓存至本地存储（accessToken）' }}</p></div><button class="primary" :disabled="busy" @click="test('protected')">验证访问权限 <span>→</span></button><button class="logout" @click="logout" :disabled="busy">清除本页会话</button></template>
          <div v-if="notice" class="notice" :class="noticeType" role="status" aria-live="polite">{{ notice }}</div>
        </section>
      </div>
      <section class="console"><div class="console-heading"><div><span class="console-icon">⌘</span><h2>接口验证台</h2><span class="muted">验证连接，观察认证流程</span></div><span class="session-status"><i :class="{ online: signedIn }"></i>{{ sessionLabel }}</span></div><div class="endpoint-grid"><button :disabled="busy" @click="test('public')"><span class="endpoint-icon">↗</span><span><strong>公开接口</strong><small>GET /test/t1</small></span><span class="endpoint-label">无需登录 →</span></button><button :disabled="busy" @click="test('protected')"><span class="endpoint-icon">♧</span><span><strong>受保护接口</strong><small>GET /test/t2</small></span><span class="endpoint-label">权限验证 →</span></button><button :disabled="busy" @click="test('refresh')"><span class="endpoint-icon">↻</span><span><strong>刷新令牌</strong><small>POST /user/login/refresh</small></span><span class="endpoint-label">会话续期 →</span></button></div><div class="activity"><div class="activity-title"><span>请求记录 <b>{{ logs.length.toString().padStart(2, '0') }}</b></span><button v-if="logs.length" @click="logs = []">清空</button></div><p v-if="!logs.length" class="empty-log"><span>⌁</span> 点击上方接口，查看请求结果</p><ol v-else><li v-for="log in logs" :key="log.id"><time>{{ log.time }}</time><span :class="log.ok ? 'ok' : 'fail'">{{ log.ok ? '成功' : '失败' }}</span><strong>{{ log.action }}</strong><span class="result">{{ log.result }}</span></li></ol></div></section>
      <footer><span><i></i> {{ mock ? '本地模拟模式 · 不发送网络请求' : '真实接口模式 · 后端端口 8092（默认）' }}</span><span>Built with Vue 3 <span class="footer-separator">/</span> Designed for secure connections</span></footer>
    </main>
  </div>
</template>
