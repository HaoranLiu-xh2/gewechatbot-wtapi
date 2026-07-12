<template>
  <div class="home-container">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="card-bg-glow"></div>
      <div class="welcome-content">
        <div class="avatar-ring">
          <div class="avatar-inner">
            <el-icon size="40"><Avatar /></el-icon>
          </div>
        </div>
        <div class="welcome-text">
          <h1 class="welcome-title">
            欢迎回来，<span class="highlight">{{ userStore.username }}</span>
          </h1>
          <p class="welcome-subtitle">WTAPI 智能Agent · 连接微信生态无限可能</p>
        </div>
        <div class="welcome-stats">
          <div class="stat-item">
            <div class="stat-value">24/7</div>
            <div class="stat-label">全天候服务</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value">99.9%</div>
            <div class="stat-label">系统稳定性</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value"><el-icon><Check /></el-icon></div>
            <div class="stat-label">安全认证</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能卡片 -->
    <div class="feature-grid">
      <div class="feature-card" v-for="(feature, index) in features" :key="index">
        <div class="feature-glow"></div>
        <div class="feature-border"></div>
        <div class="feature-content">
          <div class="feature-icon" :style="{ background: feature.gradient }">
            <el-icon size="24"><component :is="feature.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.desc }}</p>
        </div>
      </div>
    </div>

    <!-- 系统状态 -->
    <div class="status-section">
      <div class="status-card">
        <div class="status-header">
          <el-icon class="status-icon"><DataLine /></el-icon>
          <span class="status-title">系统运行状态</span>
        </div>
        <div class="status-items">
          <div class="status-item">
            <div class="status-dot active"></div>
            <span class="status-name">API 网关</span>
            <span class="status-value">正常运行</span>
          </div>
          <div class="status-item">
            <div class="status-dot active"></div>
            <span class="status-name">数据库</span>
            <span class="status-value">正常运行</span>
          </div>
          <div class="status-item">
            <div class="status-dot active"></div>
            <span class="status-name">缓存服务</span>
            <span class="status-value">正常运行</span>
          </div>
          <div class="status-item">
            <div class="status-dot active"></div>
            <span class="status-name">消息队列</span>
            <span class="status-value">正常运行</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/store'
import { ref } from 'vue'

const userStore = useUserStore()

const features = ref([
  {
    icon: 'UserFilled',
    title: '用户管理',
    desc: '统一管理用户账号、权限与安全策略',
    gradient: 'linear-gradient(135deg, #00a8ff, #0066ff)'
  },
  {
    icon: 'DataAnalysis',
    title: '数据分析',
    desc: '实时数据可视化与智能分析报表',
    gradient: 'linear-gradient(135deg, #a855f7, #ec4899)'
  },
  {
    icon: 'Monitor',
    title: '系统监控',
    desc: '全方位系统性能监控与告警机制',
    gradient: 'linear-gradient(135deg, #00ffcc, #00d4ff)'
  },
  {
    icon: 'Lock',
    title: '安全中心',
    desc: '多层安全防护与访问控制策略',
    gradient: 'linear-gradient(135deg, #f59e0b, #ef4444)'
  }
])
</script>

<style lang="scss" scoped>
.home-container {
  padding-bottom: 20px;
}

/* 欢迎卡片 */
.welcome-card {
  position: relative;
  border-radius: 20px;
  background: rgba(10, 22, 45, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.15);
  overflow: hidden;
  margin-bottom: 24px;
}

.card-bg-glow {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.08) 0%, transparent 60%);
  pointer-events: none;
}

.welcome-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px 36px;
  z-index: 1;
}

.avatar-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00a8ff, #a855f7);
  padding: 3px;
  animation: avatarPulse 3s ease-in-out infinite;
  flex-shrink: 0;
}

@keyframes avatarPulse {
  0%, 100% { box-shadow: 0 0 20px rgba(0, 212, 255, 0.2); }
  50% { box-shadow: 0 0 40px rgba(0, 212, 255, 0.4); }
}

.avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #0a1628, #0d1b3e);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00d4ff;
}

.welcome-text {
  flex: 1;
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.highlight {
  background: linear-gradient(135deg, #00d4ff, #a855f7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.45);
  letter-spacing: 1px;
}

.welcome-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #00d4ff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: linear-gradient(180deg, transparent, rgba(0, 212, 255, 0.3), transparent);
}

/* 功能卡片 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.feature-card {
  position: relative;
  border-radius: 16px;
  background: rgba(10, 22, 45, 0.4);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.feature-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0, 212, 255, 0.25);
  box-shadow: 0 10px 40px rgba(0, 212, 255, 0.1);
}

.feature-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 0%, rgba(0, 212, 255, 0.05) 0%, transparent 60%);
  pointer-events: none;
}

.feature-content {
  position: relative;
  padding: 24px;
  z-index: 1;
}

.feature-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 16px;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  line-height: 1.5;
}

/* 系统状态 */
.status-section {
  margin-bottom: 20px;
}

.status-card {
  border-radius: 16px;
  background: rgba(10, 22, 45, 0.4);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.1);
  padding: 24px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.status-icon {
  color: #00d4ff;
  font-size: 20px;
}

.status-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.status-items {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(0, 212, 255, 0.05);
  border: 1px solid rgba(0, 212, 255, 0.1);
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #67c23a;
  box-shadow: 0 0 10px rgba(103, 194, 58, 0.5);
  animation: dotBlink 2s ease-in-out infinite;
}

@keyframes dotBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-name {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.status-value {
  margin-left: auto;
  font-size: 13px;
  color: #67c23a;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 1200px) {
  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .status-items {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
  }
  .feature-grid {
    grid-template-columns: 1fr;
  }
  .status-items {
    grid-template-columns: 1fr;
  }
}
</style>
