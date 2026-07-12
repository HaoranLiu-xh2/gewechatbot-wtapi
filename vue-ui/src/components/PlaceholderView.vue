<template>
  <div class="placeholder-container">
    <div class="placeholder-card">
      <div class="card-glow"></div>
      <div class="placeholder-header">
        <div class="placeholder-icon" :style="{ background: gradient }">
          <el-icon size="32">
            <component :is="icon" />
          </el-icon>
        </div>
        <div class="placeholder-title-wrap">
          <h1 class="placeholder-title">{{ title }}</h1>
          <div class="dev-badge">
            <el-icon><Tools /></el-icon>
            <span>待开发功能</span>
          </div>
        </div>
      </div>

      <div class="placeholder-divider"></div>

      <div class="placeholder-body">
        <div class="section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>功能说明</span>
          </div>
          <p class="section-desc">{{ description }}</p>
        </div>

        <div class="section">
          <div class="section-title">
            <el-icon><Clock /></el-icon>
            <span>开发计划</span>
          </div>
          <p class="section-desc">
            该模块正在规划开发中，后续将提供完整的数据管理、任务调度与操作界面，敬请期待。
          </p>
        </div>
      </div>

      <div class="placeholder-footer">
        <div class="footer-line"></div>
        <span class="footer-text">WTAPI 智能Agent · 持续迭代中</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const title = computed(() => route.meta.title || '功能页面')
const description = computed(() => route.meta.description || '')
const icon = computed(() => route.meta.icon || 'Monitor')
const gradient = computed(() => route.meta.gradient || 'linear-gradient(135deg, #00a8ff, #0066ff)')
</script>

<style lang="scss" scoped>
.placeholder-container {
  min-height: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 60px;
}

.placeholder-card {
  position: relative;
  width: 100%;
  max-width: 760px;
  border-radius: 20px;
  background: rgba(10, 22, 45, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.15);
  overflow: hidden;
  padding: 40px;
}

.card-glow {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.08) 0%, transparent 60%);
  pointer-events: none;
}

.placeholder-header {
  position: relative;
  display: flex;
  align-items: center;
  gap: 20px;
  z-index: 1;
}

.placeholder-icon {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 8px 30px rgba(0, 212, 255, 0.15);
}

.placeholder-title-wrap {
  flex: 1;
}

.placeholder-title {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.dev-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  background: rgba(245, 108, 108, 0.12);
  border: 1px solid rgba(245, 108, 108, 0.25);
  color: rgba(245, 108, 108, 0.9);
  font-size: 13px;
  font-weight: 500;
}

.dev-badge .el-icon {
  font-size: 14px;
}

.placeholder-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.2), transparent);
  margin: 32px 0;
}

.placeholder-body {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 28px;
  z-index: 1;
}

.section {
  padding: 22px 24px;
  border-radius: 14px;
  background: rgba(0, 212, 255, 0.04);
  border: 1px solid rgba(0, 212, 255, 0.1);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #00d4ff;
  margin-bottom: 12px;
}

.section-title .el-icon {
  font-size: 16px;
}

.section-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.8;
  margin: 0;
}

.placeholder-footer {
  position: relative;
  margin-top: 32px;
  text-align: center;
  z-index: 1;
}

.footer-line {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.15), transparent);
  margin-bottom: 16px;
}

.footer-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
  letter-spacing: 1px;
}
</style>
