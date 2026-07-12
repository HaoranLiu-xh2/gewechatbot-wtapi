<template>
  <div class="register-container">
    <!-- 动态粒子背景画布 -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

    <!-- 浮动光效层 -->
    <div class="glow-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
    </div>

    <!-- 扫描线效果 -->
    <div class="scan-line"></div>

    <!-- 网格背景 -->
    <div class="grid-bg"></div>

    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-hex">
            <svg viewBox="0 0 120 120" class="hex-svg">
              <polygon
                points="60,5 110,30 110,80 60,105 10,80 10,30"
                class="hex-border"
              />
              <polygon
                points="60,18 95,36 95,74 60,92 25,74 25,36"
                class="hex-inner"
              />
            </svg>
            <span class="logo-text">W</span>
          </div>
        </div>
        <h1 class="brand-title">
          <span class="title-wt">WT</span><span class="title-api">API</span>
        </h1>
        <p class="brand-subtitle">微信AI智能 · 连接无限可能</p>
        <div class="brand-divider">
          <span class="divider-line"></span>
          <span class="divider-dot"></span>
          <span class="divider-line"></span>
        </div>
        <p class="brand-desc">
          基于微信生态的AI智能管理平台<br />
          整合消息、群聊、朋友圈，打造智能社交新体验
        </p>
        <div class="tech-tags">
          <span class="tag">微信生态</span>
          <span class="tag">智能消息</span>
          <span class="tag">群聊管理</span>
          <span class="tag">朋友圈分析</span>
        </div>
      </div>
    </div>

    <!-- 右侧注册卡片 -->
    <div class="register-section">
      <div class="register-card">
        <div class="card-glow"></div>
        <div class="card-border"></div>
        <div class="card-content">
          <div class="card-header">
            <div class="header-icon">
              <el-icon size="32"><User /></el-icon>
            </div>
            <h2 class="card-title">智能Agent 注册</h2>
            <p class="card-subtitle">开启 WTAPI 智能Agent 之旅</p>
          </div>

          <el-form
            :model="registerForm"
            :rules="rules"
            ref="registerFormRef"
            class="register-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                class="sci-input"
              >
                <template #prefix>
                  <el-icon class="prefix-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                class="sci-input"
              >
                <template #prefix>
                  <el-icon class="prefix-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                show-password
                class="sci-input"
              >
                <template #prefix>
                  <el-icon class="prefix-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="请输入昵称（选填）"
                size="large"
                class="sci-input"
              >
                <template #prefix>
                  <el-icon class="prefix-icon"><UserFilled /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="register-btn-submit"
                :loading="loading"
                @click="handleRegister"
              >
                <span class="btn-text">立即注册</span>
                <el-icon class="btn-icon"><ArrowRightBold /></el-icon>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="card-footer">
            <div class="footer-line">
              <span class="line-glow"></span>
            </div>
            <div class="footer-actions">
              <span class="footer-text">已有账号？</span>
              <el-button type="primary" link class="login-btn-link" @click="goLogin">
                去登录
                <el-icon><Right /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="copyright">
      <span class="copy-text"> WTAPI Technology Co., Ltd. All Rights Reserved.</span>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)
const particleCanvas = ref(null)

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

// 确认密码校验
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3-50 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6-20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 注册
const handleRegister = () => {
  registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await register(registerForm)
      ElMessage.success(res.msg || '注册成功')
      router.push('/login')
    } finally {
      loading.value = false
    }
  })
}

// 跳转到登录页
const goLogin = () => {
  router.push('/login')
}

// Canvas 粒子动画
let animationId = null
let ctx = null
let particles = []

onMounted(() => {
  initParticles()
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
})

function initParticles() {
  const canvas = particleCanvas.value
  if (!canvas) return

  ctx = canvas.getContext('2d')
  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)

  const particleCount = Math.floor((canvas.width * canvas.height) / 12000)
  particles = []

  for (let i = 0; i < particleCount; i++) {
    particles.push(createParticle(canvas.width, canvas.height))
  }

  animate()
}

function createParticle(w, h) {
  return {
    x: Math.random() * w,
    y: Math.random() * h,
    vx: (Math.random() - 0.5) * 0.5,
    vy: (Math.random() - 0.5) * 0.5,
    radius: Math.random() * 2 + 1,
    opacity: Math.random() * 0.5 + 0.2,
    color: getRandomColor()
  }
}

function getRandomColor() {
  const colors = ['#00d4ff', '#0099ff', '#00ffcc', '#4facfe', '#00f2fe', '#a855f7']
  return colors[Math.floor(Math.random() * colors.length)]
}

function resizeCanvas() {
  const canvas = particleCanvas.value
  if (!canvas) return
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
}

function animate() {
  if (!ctx || !particleCanvas.value) return
  const canvas = particleCanvas.value
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  for (let i = 0; i < particles.length; i++) {
    const p = particles[i]
    p.x += p.vx
    p.y += p.vy

    if (p.x < 0 || p.x > canvas.width) p.vx *= -1
    if (p.y < 0 || p.y > canvas.height) p.vy *= -1

    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    ctx.fillStyle = p.color
    ctx.globalAlpha = p.opacity
    ctx.fill()

    for (let j = i + 1; j < particles.length; j++) {
      const p2 = particles[j]
      const dx = p.x - p2.x
      const dy = p.y - p2.y
      const dist = Math.sqrt(dx * dx + dy * dy)

      if (dist < 150) {
        ctx.beginPath()
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(p2.x, p2.y)
        ctx.strokeStyle = p.color
        ctx.globalAlpha = (1 - dist / 150) * 0.15
        ctx.lineWidth = 1
        ctx.stroke()
      }
    }
  }

  ctx.globalAlpha = 1
  animationId = requestAnimationFrame(animate)
}
</script>

<style lang="scss" scoped>
:global(html),
:global(body),
:global(#app) {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  background: #050b14;
}

.register-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #050b14 0%, #0a1628 40%, #0d1b3e 70%, #0a0e27 100%);
}

/* 粒子画布 */
.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

/* 网格背景 */
.grid-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { background-position: 0 0; }
  100% { background-position: 60px 60px; }
}

/* 浮动光球 */
.glow-orbs {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.3) 0%, transparent 70%);
  top: -10%;
  left: -5%;
  animation: orbFloat1 15s ease-in-out infinite;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(168, 85, 247, 0.25) 0%, transparent 70%);
  bottom: -10%;
  right: 30%;
  animation: orbFloat2 18s ease-in-out infinite;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(0, 255, 204, 0.2) 0%, transparent 70%);
  top: 40%;
  left: 30%;
  animation: orbFloat3 12s ease-in-out infinite;
}

.orb-4 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(0, 153, 255, 0.2) 0%, transparent 70%);
  top: 10%;
  right: 10%;
  animation: orbFloat4 20s ease-in-out infinite;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(50px, 30px) scale(1.1); }
  66% { transform: translate(-30px, 60px) scale(0.9); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-40px, -50px) scale(1.15); }
  66% { transform: translate(60px, -20px) scale(0.85); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, -40px) scale(1.2); }
}

@keyframes orbFloat4 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-60px, 40px) scale(0.9); }
  66% { transform: translate(30px, -30px) scale(1.1); }
}

/* 扫描线 */
.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.6), transparent);
  z-index: 2;
  animation: scanMove 6s linear infinite;
  opacity: 0.6;
}

@keyframes scanMove {
  0% { top: -3px; }
  100% { top: 100%; }
}

/* 左侧品牌区 */
.brand-section {
  flex: 0 0 auto;
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 3;
}

.brand-content {
  max-width: 100%;
}

.brand-logo {
  margin-bottom: 30px;
}

.logo-hex {
  position: relative;
  width: 100px;
  height: 100px;
}

.hex-svg {
  width: 100%;
  height: 100%;
}

.hex-border {
  fill: none;
  stroke: rgba(0, 212, 255, 0.6);
  stroke-width: 2;
  animation: hexPulse 3s ease-in-out infinite;
}

.hex-inner {
  fill: rgba(0, 212, 255, 0.1);
  stroke: rgba(0, 212, 255, 0.4);
  stroke-width: 1;
}

.logo-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 42px;
  font-weight: 900;
  color: #00d4ff;
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.6);
}

@keyframes hexPulse {
  0%, 100% { stroke-opacity: 0.6; }
  50% { stroke-opacity: 1; }
}

.brand-title {
  font-size: 64px;
  font-weight: 900;
  letter-spacing: 4px;
  margin-bottom: 16px;
  line-height: 1.1;
}

.title-wt {
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 50%, #00f2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 30px rgba(0, 212, 255, 0.4));
}

.title-api {
  background: linear-gradient(135deg, #a855f7 0%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 30px rgba(168, 85, 247, 0.4));
}

.brand-subtitle {
  font-size: 22px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 6px;
  margin-bottom: 30px;
  font-weight: 300;
}

.brand-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.5), transparent);
}

.divider-dot {
  width: 8px;
  height: 8px;
  background: #00d4ff;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.8);
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.6; }
}

.brand-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.8;
  margin-bottom: 32px;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: rgba(0, 212, 255, 0.9);
  border: 1px solid rgba(0, 212, 255, 0.25);
  background: rgba(0, 212, 255, 0.05);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.tag:hover {
  background: rgba(0, 212, 255, 0.15);
  border-color: rgba(0, 212, 255, 0.5);
  box-shadow: 0 0 15px rgba(0, 212, 255, 0.2);
}

/* 右侧注册区 */
.register-section {
  flex: 0 0 auto;
  width: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  position: relative;
  z-index: 3;
  overflow-y: auto;
}

.register-card {
  width: 100%;
  max-width: 420px;
  position: relative;
  border-radius: 20px;
  background: rgba(10, 22, 45, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.15);
  overflow: hidden;
  margin: 20px 0;
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle at 50% 50%,
    rgba(0, 212, 255, 0.08) 0%,
    transparent 50%
  );
  animation: cardGlowRotate 10s linear infinite;
  pointer-events: none;
}

@keyframes cardGlowRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 20px;
  padding: 1.5px;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.4), rgba(168, 85, 247, 0.2), rgba(0, 212, 255, 0.4));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
  animation: borderGlow 4s ease-in-out infinite;
}

@keyframes borderGlow {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.card-content {
  position: relative;
  padding: 36px 32px;
  z-index: 1;
}

.card-header {
  text-align: center;
  margin-bottom: 24px;
}

.header-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.15), rgba(168, 85, 247, 0.15));
  border: 1px solid rgba(0, 212, 255, 0.2);
  color: #00d4ff;
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.card-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6px;
  letter-spacing: 2px;
}

.card-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  letter-spacing: 1px;
}

/* 输入框 */
:deep(.sci-input .el-input__wrapper) {
  background: rgba(0, 0, 0, 0.25) !important;
  border: 1px solid rgba(0, 212, 255, 0.2) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.1) inset !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
}

:deep(.sci-input .el-input__wrapper:hover) {
  border-color: rgba(0, 212, 255, 0.4) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.2) inset, 0 0 15px rgba(0, 212, 255, 0.1) !important;
}

:deep(.sci-input .el-input__wrapper.is-focus) {
  border-color: rgba(0, 212, 255, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.3) inset, 0 0 20px rgba(0, 212, 255, 0.15) !important;
}

:deep(.sci-input .el-input__inner),
:deep(.el-input__inner) {
  color: #fff !important;
  font-size: 14px !important;
  background: transparent !important;
}

:deep(.sci-input .el-input__inner::placeholder),
:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.sci-input .el-input__prefix-inner),
:deep(.el-input__prefix-inner) {
  color: rgba(0, 212, 255, 0.6) !important;
  font-size: 18px;
  margin-right: 8px;
}

/* 覆盖 Element Plus 默认白色背景 */
:deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.25) !important;
  border: 1px solid rgba(0, 212, 255, 0.2) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.1) inset !important;
  border-radius: 12px !important;
}

/* 覆盖浏览器自动填充导致的白色背景 */
:deep(.el-input__inner:-webkit-autofill),
:deep(.el-input__inner:-webkit-autofill:hover),
:deep(.el-input__inner:-webkit-autofill:focus),
:deep(.el-input__inner:-webkit-autofill:active) {
  -webkit-box-shadow: 0 0 0 1000px rgba(0, 0, 0, 0.25) inset !important;
  -webkit-text-fill-color: #fff !important;
  transition: background-color 5000s ease-in-out 0s;
}

:deep(.el-form-item__error) {
  color: #ff6b6b;
}

/* 注册按钮 */
.register-btn-submit {
  width: 100%;
  height: 46px;
  border-radius: 12px;
  background: linear-gradient(135deg, #00a8ff 0%, #0066ff 50%, #00d4ff 100%) !important;
  border: none !important;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.register-btn-submit::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.register-btn-submit:hover::before {
  left: 100%;
}

.register-btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 212, 255, 0.35);
}

.btn-text {
  position: relative;
  z-index: 1;
}

.btn-icon {
  margin-left: 8px;
  position: relative;
  z-index: 1;
}

/* 底部 */
.card-footer {
  margin-top: 16px;
}

.footer-line {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.line-glow {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.5), transparent);
  border-radius: 1px;
}

.footer-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.footer-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
}

.login-btn-link {
  color: #00d4ff !important;
  font-size: 14px !important;
  font-weight: 500;
}

.login-btn-link:hover {
  color: #00f2fe !important;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}

/* 版权 */
.copyright {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 3;
}

.copy-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.2);
  letter-spacing: 1px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .brand-section {
    display: none;
  }
  .register-section {
    width: 100%;
    max-width: 420px;
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .register-card {
    max-width: 100%;
  }
  .card-content {
    padding: 24px 20px;
  }
}
</style>
