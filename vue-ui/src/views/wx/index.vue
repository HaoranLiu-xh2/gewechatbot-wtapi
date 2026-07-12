<template>
  <div class="wx-container">
    <!-- 顶部操作栏 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><ChatDotRound /></el-icon>
        <div class="title-text">
          <h2>微信账号管理</h2>
          <p>管理已登录的微信实体，支持扫码登录新账号</p>
        </div>
      </div>
      <div class="header-actions">
        <div class="token-status" :class="{ 'has-token': userStore.wxApiToken }">
          <el-icon><Key /></el-icon>
          <span>{{ userStore.wxApiToken ? 'Token 已设置' : 'Token 未设置' }}</span>
        </div>
        <el-button type="primary" class="login-btn" @click="openLoginDialog">
          <el-icon><Plus /></el-icon>
          <span>登录微信</span>
        </el-button>
      </div>
    </div>

    <!-- 账号列表 -->
    <div class="account-section">
      <div class="section-header">
        <el-icon><List /></el-icon>
        <span>已登录账号</span>
      </div>
      <el-table
        :data="accountList"
        v-loading="loading"
        class="sci-table"
        row-class-name="sci-table-row"
      >
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.headImgUrl" class="wx-avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickName" label="昵称" min-width="120">
          <template #default="{ row }">
            <span class="nickname">{{ row.nickName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="wxid" label="wxid" min-width="160">
          <template #default="{ row }">
            <span class="text-mono">{{ row.wxid || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="alias" label="别名" min-width="120">
          <template #default="{ row }">
            <span class="text-mono">{{ row.alias || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="regionName" label="地区" min-width="100">
          <template #default="{ row }">
            <span class="region-tag">{{ row.regionName || row.regionId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="loginType" label="登录方式" width="100">
          <template #default="{ row }">
            <span class="type-tag">{{ row.loginType || 'mac' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <div class="status-tag" :class="row.status === 1 ? 'online' : 'offline'">
              <span class="status-dot"></span>
              <span>{{ row.status === 1 ? '在线' : '离线' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="登录时间" min-width="160">
          <template #default="{ row }">
            <span class="text-dim">{{ row.createTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              class="action-link-warning"
              @click="handleLogout(row)"
            >
              <el-icon><SwitchButton /></el-icon>
              <span>下线</span>
            </el-button>
            <template v-else>
              <el-button
                type="warning"
                link
                class="action-link-warning"
                @click="handleRecover(row)"
              >
                <el-icon><RefreshLeft /></el-icon>
                <span>恢复</span>
              </el-button>
              <el-button
                type="danger"
                link
                class="action-link-danger icon-only"
                @click="handleDelete(row)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon size="48"><ChatDotRound /></el-icon>
            <p>暂无已登录的微信账号</p>
          </div>
        </template>
      </el-table>
    </div>

    <!-- 登录参数弹窗 -->
    <el-dialog
      v-model="loginDialogVisible"
      title="登录微信"
      width="420px"
      :close-on-click-modal="false"
      class="sci-dialog"
      destroy-on-close
    >
      <div class="login-form">
        <div class="form-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>请选择登录地区，系统将生成微信扫码二维码。</span>
        </div>
        <el-form :model="loginForm" label-position="top" class="sci-form">
          <!-- <el-form-item label="AppId">
            <el-input
              v-model="loginForm.appId"
              placeholder="请输入 AppId（首次登录可留空）"
              clearable
            />
          </el-form-item>
          <el-form-item label="Aid">
            <el-input
              v-model="loginForm.aid"
              placeholder="请输入 Aid"
              clearable
            />
          </el-form-item> -->
          <el-form-item label="登录地区">
            <el-select
              v-model="loginForm.regionId"
              placeholder="请选择地区"
              class="region-select"
              filterable
            >
              <el-option
                v-for="item in regionList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="登录方式">
            <span class="login-type-tag">
              <el-icon><Monitor /></el-icon>
              <span>macOS 桌面端</span>
            </span>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="loginDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="qrLoading" @click="getQrCode">获取二维码</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 二维码弹窗 -->
    <el-dialog
      v-model="qrDialogVisible"
      title="微信扫码登录"
      width="400px"
      :close-on-click-modal="false"
      class="sci-dialog qr-dialog"
      :show-close="!checking"
      @closed="stopPolling"
    >
      <div class="qr-content">
        <div v-if="checking" class="qr-status">
          <div class="scanning-line"></div>
          <p class="status-text">{{ pollingStatusText }}</p>
        </div>

        <!-- 未扫码时显示二维码 -->
        <div v-if="!scannedAvatar" class="qr-box">
          <img v-if="qrImgUrl" :src="qrImgUrl" alt="微信登录二维码" class="qr-image" />
          <div v-else class="qr-placeholder">
            <el-icon size="40"><Picture /></el-icon>
            <p>二维码加载中...</p>
          </div>
        </div>

        <!-- 扫码后显示大头像 -->
        <div v-else class="scan-avatar-large">
          <div class="avatar-ring">
            <el-avatar :size="160" :src="scannedAvatar" class="avatar-img-large">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
          </div>
          <p v-if="scannedNickname" class="scan-nickname-large">{{ scannedNickname }}</p>
          <p class="scan-hint">正在确认登录...</p>
        </div>

        <p v-if="!scannedAvatar" class="qr-tip">请使用微信扫一扫，扫描上方二维码登录</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="qrDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLoginQrCode, checkLogin, listAccounts, deleteAccount, logoutAccount } from '@/api/wx'

const userStore = useUserStore()

// 账号列表
const accountList = ref([])
const loading = ref(false)

// 登录弹窗
const loginDialogVisible = ref(false)
const loginForm = ref({
  appId: '',
  aid: '',
  regionId: '110000',
  type: 'mac'
})
const qrLoading = ref(false)

// 二维码弹窗
const qrDialogVisible = ref(false)
const qrImgUrl = ref('')
const checking = ref(false)
const pollingStatusText = ref('等待扫码...')
const scannedAvatar = ref('')
const scannedNickname = ref('')

// 轮询相关
let pollTimer = null
let pollCount = 0
const MAX_POLL_COUNT = 90

// 地区列表
const regionList = [
  { value: '110000', label: '北京市' },
  { value: '120000', label: '天津市' },
  { value: '130000', label: '河北省' },
  { value: '140000', label: '山西省' },
  { value: '210000', label: '辽宁省' },
  { value: '220000', label: '吉林省' },
  { value: '230000', label: '黑龙江省' },
  { value: '310000', label: '上海市' },
  { value: '320000', label: '江苏省' },
  { value: '330000', label: '浙江省' },
  { value: '340000', label: '安徽省' },
  { value: '350000', label: '福建省' },
  { value: '360000', label: '江西省' },
  { value: '370000', label: '山东省' },
  { value: '410000', label: '河南省' },
  { value: '420000', label: '湖北省' },
  { value: '430000', label: '湖南省' },
  { value: '440000', label: '广东省' },
  { value: '450000', label: '广西省' },
  { value: '460000', label: '海南省' },
  { value: '500000', label: '重庆市' },
  { value: '510000', label: '四川省' },
  { value: '520000', label: '贵州省' },
  { value: '530000', label: '云南省' },
  { value: '540000', label: '西藏' },
  { value: '610000', label: '陕西省' },
  { value: '620000', label: '甘肃省' },
  { value: '630000', label: '青海省' },
  { value: '640000', label: '宁夏' }
]

// 打开登录弹窗
const openLoginDialog = () => {
  if (!userStore.wxApiToken) {
    ElMessage.warning('请先点击右上角「Token」按钮设置微信 API Token')
    return
  }
  loginForm.value.appId = ''
  loginForm.value.aid = ''
  loginForm.value.regionId = '110000'
  loginDialogVisible.value = true
}

// 统一处理二维码响应并开启轮询
const handleQrResponse = (res, regionId, type, regionName) => {
  // 后端返回结构：{ code, msg, data: { ret, msg, data: { qrData, qrUrl, qrImgBase64, uuid, appId } } }
  const wxRes = res.data || {}
  const data = wxRes.data || {}
  const qrData = (data.qrData || '').trim().replace(/^`|`$/g, '')
  const qrUrl = (data.qrUrl || '').trim().replace(/^`|`$/g, '')
  const uuid = data.uuid || ''
  const appId = data.appId || ''

  if (!uuid) {
    ElMessage.error('获取二维码失败：未返回 uuid')
    return false
  }

  // 优先使用后端返回的 base64 二维码，其次 qrUrl，最后根据 qrData 生成
  if (data.qrImgBase64 && data.qrImgBase64.startsWith('data:image')) {
    qrImgUrl.value = data.qrImgBase64
  } else if (qrUrl) {
    qrImgUrl.value = qrUrl
  } else if (qrData) {
    qrImgUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(qrData)}`
  }

  qrDialogVisible.value = true
  startPolling(appId, uuid, regionId, type, regionName)
  return true
}

// 获取二维码
const getQrCode = async () => {
  qrLoading.value = true
  try {
    const res = await getLoginQrCode({
      appId: loginForm.value.appId || '',
      aid: loginForm.value.aid || '',
      proxyIp: '',
      regionId: loginForm.value.regionId,
      type: loginForm.value.type
    })
    const region = regionList.find((item) => item.value === loginForm.value.regionId)
    if (handleQrResponse(res, loginForm.value.regionId, loginForm.value.type, region ? region.label : '')) {
      loginDialogVisible.value = false
    }
  } catch (error) {
    console.error(error)
  } finally {
    qrLoading.value = false
  }
}

// 恢复登录：使用历史参数重新取码
const handleRecover = async (row) => {
  try {
    const res = await getLoginQrCode({
      appId: row.appId || '',
      aid: '',
      proxyIp: '',
      regionId: row.regionId || '110000',
      type: row.loginType || 'mac'
    })
    handleQrResponse(res, row.regionId, row.loginType || 'mac', row.regionName || '')
  } catch (error) {
    console.error('恢复登录失败', error)
  }
}

// 开始轮询
const startPolling = (appId, uuid, regionId, type, regionName) => {
  stopPolling()
  checking.value = true
  pollingStatusText.value = '等待扫码...'
  scannedAvatar.value = ''
  scannedNickname.value = ''
  pollCount = 0

  const doPoll = async () => {
    if (!checking.value || !qrDialogVisible.value) {
      return
    }
    if (pollCount >= MAX_POLL_COUNT) {
      stopPolling()
      ElMessage.warning('二维码已过期，请重新获取')
      qrDialogVisible.value = false
      return
    }
    pollCount++

    try {
      const res = await checkLogin({
        appId,
        uuid,
        autoSliding: true,
        regionId,
        regionName,
        type
      })
      // 后端返回结构：{ code, msg, data: { ret, msg, data: { ...登录信息 } } }
      const wxRes = res.data || {}
      const loginData = wxRes.data || {}
      const status = loginData.status

      // 更新扫码用户头像和昵称（清洗反引号包裹）
      if (loginData.headImgUrl) {
        scannedAvatar.value = String(loginData.headImgUrl).trim().replace(/^`|`$/g, '')
      }
      if (loginData.nickName) {
        scannedNickname.value = String(loginData.nickName).trim().replace(/^`|`$/g, '')
      }

      if (status === 2) {
        stopPolling()
        ElMessage.success(`登录成功：${loginData.nickName || ''}`)
        qrDialogVisible.value = false
        loadAccountList()
        return
      } else if (status === 0) {
        pollingStatusText.value = '等待扫码...'
      } else if (status === 1) {
        pollingStatusText.value = '扫码成功，请在手机上确认登录...'
      } else if (status !== undefined && status !== null) {
        pollingStatusText.value = `登录状态：${status}`
      } else {
        pollingStatusText.value = '正在检测登录状态...'
      }
    } catch (error) {
      console.error('轮询登录状态失败', error)
    }

    if (checking.value && qrDialogVisible.value) {
      pollTimer = setTimeout(doPoll, 2000)
    }
  }

  pollTimer = setTimeout(doPoll, 2000)
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
  checking.value = false
}

// 加载账号列表
const loadAccountList = async () => {
  loading.value = true
  try {
    const res = await listAccounts()
    accountList.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 下线账号
const handleLogout = (row) => {
  ElMessageBox.confirm(`确定要让「${row.nickName || row.wxid || '该账号'}」下线吗？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await logoutAccount({ appId: row.appId })
      ElMessage.success('下线成功')
      loadAccountList()
    } catch (error) {
      console.error(error)
    }
  })
}

// 删除账号
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除「${row.nickName || row.wxid || '该账号'}」吗？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAccount(row.id)
      ElMessage.success('删除成功')
      loadAccountList()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  loadAccountList()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style lang="scss" scoped>
.wx-container {
  padding-bottom: 20px;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 24px;
  border-radius: 20px;
  background: rgba(10, 22, 45, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.15);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #07c160, #00d4ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.title-text h2 {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.title-text p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.token-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 13px;
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.2);
  color: rgba(245, 108, 108, 0.9);
  transition: all 0.3s ease;
}

.token-status.has-token {
  background: rgba(103, 194, 58, 0.1);
  border-color: rgba(103, 194, 58, 0.25);
  color: rgba(103, 194, 58, 0.9);
}

.login-btn {
  background: linear-gradient(135deg, #07c160, #00a854);
  border: none;
  box-shadow: 0 4px 20px rgba(7, 193, 96, 0.3);
}

.login-btn:hover {
  background: linear-gradient(135deg, #06ad56, #009648);
  box-shadow: 0 6px 25px rgba(7, 193, 96, 0.4);
}

/* 账号列表 */
.account-section {
  border-radius: 20px;
  background: rgba(10, 22, 45, 0.4);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.1);
  padding: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.section-header .el-icon {
  color: #00d4ff;
  font-size: 20px;
}

:deep(.sci-table) {
  background: transparent;
}

:deep(.sci-table .el-table__header-wrapper th) {
  background: rgba(0, 212, 255, 0.08) !important;
  color: rgba(255, 255, 255, 0.7) !important;
  font-weight: 600;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1) !important;
}

:deep(.sci-table .el-table__body-wrapper td) {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.8) !important;
  border-bottom: 1px solid rgba(0, 212, 255, 0.05) !important;
}

:deep(.sci-table .el-table__row:hover td) {
  background: rgba(0, 212, 255, 0.05) !important;
}

:deep(.sci-table .el-table__empty-block) {
  background: transparent;
}

.wx-avatar {
  border: 2px solid rgba(0, 212, 255, 0.2);
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.1);
}

.nickname {
  font-weight: 600;
  color: #fff;
}

.text-mono {
  font-family: 'Courier New', monospace;
  color: rgba(255, 255, 255, 0.7);
}

.region-tag,
.type-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.15);
  color: rgba(0, 212, 255, 0.9);
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
}

.status-tag.online {
  background: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.2);
  color: rgba(103, 194, 58, 0.9);
}

.status-tag.offline {
  background: rgba(144, 147, 153, 0.1);
  border: 1px solid rgba(144, 147, 153, 0.2);
  color: rgba(144, 147, 153, 0.9);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.text-dim {
  color: rgba(255, 255, 255, 0.5);
}

.action-link-warning {
  color: #f59e0b !important;
}

.action-link-warning:hover {
  color: #fbbf24 !important;
}

.action-link-danger {
  color: #f56c6c !important;
}

.action-link-danger:hover {
  color: #ff8585 !important;
}

.action-link-danger.icon-only {
  margin-left: 4px;
  padding: 4px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.4);
}

.empty-state .el-icon {
  color: rgba(0, 212, 255, 0.3);
  margin-bottom: 12px;
}

.empty-state p {
  margin-bottom: 12px;
}

/* 登录弹窗 */
.login-form {
  padding: 8px 4px;
}

.form-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 20px;
  border-radius: 8px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.15);
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  line-height: 1.5;
}

.form-tip .el-icon {
  color: #00d4ff;
  font-size: 14px;
  margin-top: 1px;
  flex-shrink: 0;
}

:deep(.sci-form .el-form__label) {
  color: rgba(255, 255, 255, 0.7) !important;
}

.region-select {
  width: 100%;
}

.login-type-tag {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, rgba(0, 168, 255, 0.15), rgba(0, 212, 255, 0.08));
  border: 1px solid rgba(0, 212, 255, 0.25);
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.login-type-tag .el-icon {
  font-size: 20px;
  color: #00d4ff;
}

/* 二维码弹窗 */
.qr-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
}

.qr-status {
  position: relative;
  width: 100%;
  text-align: center;
  margin-bottom: 16px;
  overflow: hidden;
  border-radius: 8px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.15);
  padding: 10px 0;
}

.scanning-line {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.2), transparent);
  animation: scanMove 2s linear infinite;
}

@keyframes scanMove {
  0% { left: -100%; }
  100% { left: 100%; }
}

.status-text {
  position: relative;
  color: #00d4ff;
  font-size: 14px;
  font-weight: 500;
  z-index: 1;
}

.qr-box {
  position: relative;
  width: 260px;
  height: 260px;
  border-radius: 16px;
  background: #fff;
  padding: 16px;
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.qr-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.qr-placeholder {
  text-align: center;
  color: rgba(0, 0, 0, 0.4);
}

.scan-avatar-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
  margin-bottom: 20px;
}

.avatar-ring {
  position: relative;
  padding: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00a8ff, #00d4ff);
  box-shadow: 0 0 40px rgba(0, 212, 255, 0.35);
  animation: ringPulse 2s ease-in-out infinite;
}

@keyframes ringPulse {
  0%, 100% { box-shadow: 0 0 40px rgba(0, 212, 255, 0.35); }
  50% { box-shadow: 0 0 60px rgba(0, 212, 255, 0.55); }
}

.avatar-img-large {
  border: 4px solid rgba(10, 22, 45, 0.8);
  background: #fff;
}

.scan-nickname-large {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.scan-hint {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.qr-tip {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  text-align: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
