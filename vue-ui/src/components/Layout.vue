<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="260px" class="sidebar">
      <div class="sidebar-bg"></div>
      <div class="sidebar-content">
        <div class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 120 120" class="logo-svg">
              <polygon
                points="60,5 110,30 110,80 60,105 10,80 10,30"
                class="logo-hex-border"
              />
            </svg>
            <span class="logo-w">W</span>
          </div>
          <div class="logo-text">
            <span class="text-wt">WT</span><span class="text-api">API</span>
          </div>
        </div>

        <div class="menu-divider">
          <span class="divider-glow"></span>
        </div>

        <el-menu
          :default-active="activeMenu"
          router
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.65)"
          active-text-color="#00d4ff"
          class="sci-menu"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/chat">
            <el-icon><ChatLineRound /></el-icon>
            <span>聚合聊天</span>
          </el-menu-item>
          <el-menu-item index="/wx">
            <el-icon><ChatDotRound /></el-icon>
            <span>微信管理</span>
          </el-menu-item>
          <el-menu-item index="/mass-message">
            <el-icon><Promotion /></el-icon>
            <span>消息群发任务</span>
          </el-menu-item>
          <el-menu-item index="/group-message">
            <el-icon><ChatLineSquare /></el-icon>
            <span>群消息任务</span>
          </el-menu-item>
          <el-menu-item index="/moments">
            <el-icon><Camera /></el-icon>
            <span>朋友圈任务</span>
          </el-menu-item>
          <el-menu-item index="/ai-reply">
            <el-icon><Cpu /></el-icon>
            <span>AI聊消息回复</span>
          </el-menu-item>
          <el-menu-item index="/new-customer">
            <el-icon><UserFilled /></el-icon>
            <span>新客户应答</span>
          </el-menu-item>
          <el-menu-item index="/customer-analysis">
            <el-icon><TrendCharts /></el-icon>
            <span>客户分析跟踪</span>
          </el-menu-item>
          <el-menu-item index="/user">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <div class="footer-glow-line"></div>
          <p class="footer-version">WTAPI v1.0.0</p>
        </div>
      </div>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <div class="breadcrumb">
            <el-icon class="breadcrumb-icon"><Monitor /></el-icon>
            <span class="breadcrumb-text">{{ pageTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="token-btn" @click="openTokenDialog" title="设置微信 API Token">
            <el-icon><Key /></el-icon>
            <span>Token</span>
          </div>
          <div class="user-info">
            <div class="user-avatar">
              <el-icon size="18"><UserFilled /></el-icon>
            </div>
            <span class="username">{{ userStore.username }}</span>
          </div>
          <div class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出</span>
          </div>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view :key="route.path" />
        </div>
      </el-main>
    </el-container>
  </el-container>

  <!-- Token 设置弹窗 -->
  <el-dialog
    v-model="tokenDialogVisible"
    title="设置微信 API Token"
    width="460px"
    :close-on-click-modal="false"
    class="sci-dialog"
    destroy-on-close
  >
    <div class="token-tip">
      <el-icon><InfoFilled /></el-icon>
      <span>Token 仅用于调用 wx.chuapi.com 接口，与系统登录无关。</span>
    </div>
    <el-input
      v-model="tokenForm.token"
      type="textarea"
      :rows="4"
      placeholder="请输入 X-finder-TOKEN"
      clearable
    />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="tokenDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tokenLoading" @click="saveToken">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser, updateToken } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 页面标题
const pageTitle = computed(() => {
  const titles = {
    '/home': '系统首页',
    '/user': '用户管理',
    '/wx': '微信管理',
    '/chat': '聚合聊天',
    '/mass-message': '消息群发任务',
    '/group-message': '群消息任务',
    '/moments': '朋友圈任务',
    '/ai-reply': 'AI聊消息回复',
    '/new-customer': '新客户应答',
    '/customer-analysis': '客户分析跟踪'
  }
  return titles[route.path] || 'WTAPI 智能Agent'
})

// Token 弹窗
const tokenDialogVisible = ref(false)
const tokenForm = ref({ token: '' })
const tokenLoading = ref(false)

// 打开 Token 设置弹窗
const openTokenDialog = () => {
  tokenForm.value.token = userStore.wxApiToken || ''
  tokenDialogVisible.value = true
}

// 保存 Token
const saveToken = async () => {
  if (!tokenForm.value.token.trim()) {
    ElMessage.warning('请输入微信 API Token')
    return
  }
  tokenLoading.value = true
  try {
    await updateToken({ token: tokenForm.value.token.trim() })
    userStore.setWxApiToken(tokenForm.value.token.trim())
    ElMessage.success('Token 更新成功')
    tokenDialogVisible.value = false
  } catch (error) {
    console.error(error)
  } finally {
    tokenLoading.value = false
  }
}

// 加载当前用户信息（同步微信 API Token）
const loadCurrentUser = async () => {
  try {
    const res = await getCurrentUser()
    if (res.data && res.data.token) {
      userStore.setWxApiToken(res.data.token)
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    userStore.clearUserInfo()
    router.push('/login')
    ElMessage.success('已安全退出系统')
  })
}

onMounted(() => {
  loadCurrentUser()
})
</script>

<style lang="scss" scoped>
.layout-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #050b14 0%, #0a1628 50%, #0d1b3e 100%);
}

/* 侧边栏 */
.sidebar {
  position: relative;
  overflow: hidden;
  border-right: 1px solid rgba(0, 212, 255, 0.1);
}

.sidebar-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #0a1628 0%, #0d1b3e 50%, #0a0e27 100%);
  z-index: 0;
}

.sidebar-bg::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.02) 1px, transparent 1px);
  background-size: 40px 40px;
}

.sidebar-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* Logo */
.logo {
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
}

.logo-icon {
  position: relative;
  width: 40px;
  height: 40px;
}

.logo-svg {
  width: 100%;
  height: 100%;
}

.logo-hex-border {
  fill: none;
  stroke: rgba(0, 212, 255, 0.5);
  stroke-width: 2;
}

.logo-w {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
  font-weight: 900;
  color: #00d4ff;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 2px;
}

.text-wt {
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.text-api {
  background: linear-gradient(135deg, #a855f7 0%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 菜单分割线 */
.menu-divider {
  padding: 0 20px;
  margin-bottom: 8px;
}

.divider-glow {
  display: block;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.3), transparent);
}

/* 菜单 */
:deep(.sci-menu) {
  border-right: none !important;
  flex: 1;
  background: transparent !important;
}

:deep(.sci-menu .el-menu-item) {
  height: 52px;
  line-height: 52px;
  margin: 4px 12px;
  border-radius: 10px;
  font-size: 14px;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

:deep(.sci-menu .el-menu-item:hover) {
  background: rgba(0, 212, 255, 0.08) !important;
  color: rgba(255, 255, 255, 0.9) !important;
}

:deep(.sci-menu .el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.15), rgba(0, 212, 255, 0.05)) !important;
  border-left: 3px solid #00d4ff;
  color: #00d4ff !important;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.1);
}

:deep(.sci-menu .el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 16px 20px;
}

.footer-glow-line {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.2), transparent);
  margin-bottom: 12px;
}

.footer-version {
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.25);
  letter-spacing: 1px;
}

/* 顶部导航 */
.header {
  background: rgba(10, 22, 45, 0.7);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
}

.breadcrumb-icon {
  color: #00d4ff;
  font-size: 18px;
}

.breadcrumb-text {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.token-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.2);
  color: rgba(0, 212, 255, 0.9);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.token-btn:hover {
  background: rgba(0, 212, 255, 0.15);
  border-color: rgba(0, 212, 255, 0.4);
  box-shadow: 0 0 15px rgba(0, 212, 255, 0.15);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px;
  border-radius: 24px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.15);
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00a8ff, #a855f7);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.username {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.2);
  color: rgba(245, 108, 108, 0.8);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(245, 108, 108, 0.2);
  border-color: rgba(245, 108, 108, 0.4);
  box-shadow: 0 0 15px rgba(245, 108, 108, 0.15);
}

/* 主内容区 */
.main-content {
  background: transparent;
  padding: 20px;
  overflow-y: auto;
}

.content-wrapper {
  min-height: 100%;
}

.token-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.15);
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  line-height: 1.5;
}

.token-tip .el-icon {
  color: #00d4ff;
  font-size: 14px;
  margin-top: 1px;
  flex-shrink: 0;
}
</style>
