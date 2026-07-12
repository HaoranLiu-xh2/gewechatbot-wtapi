<template>
  <div class="user-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><UserFilled /></el-icon>
        <div class="title-text">
          <h2>用户管理</h2>
          <p>管理系统用户账号、权限及状态</p>
        </div>
      </div>
      <el-button type="primary" class="sci-btn-primary add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="sci-card search-card">
      <div class="card-glow"></div>
      <div class="card-border"></div>
      <div class="card-content">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入用户名或昵称"
              clearable
              class="sci-input-sm"
              style="width: 280px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="sci-btn-primary" @click="handleSearch">
              <el-icon><Search /></el-icon>查询
            </el-button>
            <el-button class="sci-btn-default" @click="handleReset">
              <el-icon><RefreshRight /></el-icon>重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="sci-card table-card">
      <div class="card-glow"></div>
      <div class="card-border"></div>
      <div class="card-content">
        <!-- 数据表格 -->
        <el-table :data="tableData" v-loading="loading" class="sci-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="text-primary">{{ row.username }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="phone" label="手机号" min-width="130" show-overflow-tooltip />
          <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="110" align="center">
            <template #default="{ row }">
              <div class="status-badge" :class="row.status === 1 ? 'online' : 'offline'">
                <span class="status-dot"></span>
                <span>{{ row.status === 1 ? '正常' : '禁用' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="170" />
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-btns">
                <el-button circle class="action-icon-btn" title="详情" @click="handleDetail(row)">
                  <el-icon><View /></el-icon>
                </el-button>
                <el-button circle class="action-icon-btn edit" title="编辑" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button circle class="action-icon-btn danger" title="删除" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
      class="sci-dialog"
    >
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" class="sci-input" />
        </el-form-item>
        <el-form-item :label="isEdit ? '密码' : '密码'" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            :placeholder="isEdit ? '不修改请留空' : '请输入密码'"
            show-password
            class="sci-input"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" class="sci-input" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" class="sci-input" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" class="sci-input" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status" class="sci-radio">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="sci-btn-default" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" class="sci-btn-primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px" class="sci-dialog">
      <el-descriptions :column="1" border class="sci-descriptions">
        <el-descriptions-item label="用户名">{{ detailData.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailData.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailData.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'danger'" effect="dark">
            {{ detailData.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageList, getById, addUser, updateUser, deleteUser } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const userFormRef = ref()
const submitLoading = ref(false)
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  status: 1
})

// 表单校验规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3-50 个字符之间', trigger: 'blur' }
  ]
}

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref({})

// 获取用户列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    })
    tableData.value = res.data.list
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  pagination.pageNum = 1
  fetchData()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchData()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchData()
}

// 打开新增弹窗
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  resetForm()
  try {
    const res = await getById(row.id)
    Object.assign(userForm, res.data)
    userForm.password = ''
    dialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

// 查看详情
const handleDetail = async (row) => {
  try {
    const res = await getById(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户【${row.username}】吗？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 提交表单
const handleSubmit = () => {
  userFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateUser(userForm)
        ElMessage.success('修改成功')
      } else {
        await addUser(userForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  userForm.id = null
  userForm.username = ''
  userForm.password = ''
  userForm.nickname = ''
  userForm.phone = ''
  userForm.email = ''
  userForm.status = 1
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.user-container {
  min-height: 100%;
  padding-bottom: 20px;
  background: transparent;
}

/* 页面标题 */
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
  border-radius: 14px;
  background: linear-gradient(135deg, #00a8ff, #00d4ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.25);
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

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 22px;
  font-size: 14px;
  letter-spacing: 1px;
}

.search-card {
  margin-bottom: 20px;
}

/* 科技卡片 */
.sci-card {
  position: relative;
  border-radius: 20px;
  background: rgba(10, 22, 45, 0.45);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.12);
  overflow: hidden;
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 50% 50%, rgba(0, 212, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 20px;
  padding: 1.5px;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.25), rgba(168, 85, 247, 0.12), rgba(0, 212, 255, 0.25));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.card-content {
  position: relative;
  padding: 22px 26px;
  z-index: 1;
}

/* 科技按钮 */
.sci-btn-primary {
  background: linear-gradient(135deg, #00a8ff 0%, #0066ff 50%, #00d4ff 100%) !important;
  border: none !important;
  border-radius: 10px !important;
  transition: all 0.3s ease !important;
}

.sci-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.35) !important;
}

.sci-btn-default {
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(0, 212, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.7) !important;
  border-radius: 10px !important;
  transition: all 0.3s ease !important;
}

.sci-btn-default:hover {
  background: rgba(0, 212, 255, 0.1) !important;
  border-color: rgba(0, 212, 255, 0.4) !important;
  color: #fff !important;
}

/* 小输入框 */
:deep(.sci-input-sm .el-input__wrapper) {
  background: rgba(0, 0, 0, 0.25) !important;
  border: 1px solid rgba(0, 212, 255, 0.2) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.1) inset !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
}

:deep(.sci-input-sm .el-input__wrapper:hover) {
  border-color: rgba(0, 212, 255, 0.4) !important;
}

:deep(.sci-input-sm .el-input__wrapper.is-focus) {
  border-color: rgba(0, 212, 255, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.2) inset, 0 0 20px rgba(0, 212, 255, 0.12) !important;
}

:deep(.sci-input-sm .el-input__inner) {
  color: #fff !important;
}

:deep(.sci-input-sm .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.sci-input-sm .el-input__prefix-inner) {
  color: rgba(0, 212, 255, 0.6) !important;
  margin-right: 6px;
}

/* 表格 */
:deep(.sci-table),
:deep(.sci-table .el-table__inner-wrapper),
:deep(.sci-table .el-table__body-wrapper),
:deep(.sci-table .el-table__header-wrapper),
:deep(.sci-table .el-table__body),
:deep(.sci-table .el-table__header) {
  background: transparent !important;
}

:deep(.sci-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.sci-table .el-table__header-wrapper th) {
  background: rgba(0, 212, 255, 0.1) !important;
  color: #00d4ff !important;
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.15) !important;
  padding: 14px 0 !important;
}

:deep(.sci-table .el-table__body-wrapper td) {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.8) !important;
  border-bottom: 1px solid rgba(0, 212, 255, 0.06) !important;
  padding: 16px 0 !important;
  transition: all 0.3s ease;
}

:deep(.sci-table .el-table__body-wrapper tr) {
  transition: all 0.3s ease;
  background: transparent !important;
}

:deep(.sci-table .el-table__body-wrapper tr:hover td) {
  background: rgba(0, 212, 255, 0.08) !important;
  color: #fff !important;
}

:deep(.sci-table .el-table__body-wrapper tr.el-table__row--striped td) {
  background: rgba(0, 212, 255, 0.02) !important;
}

:deep(.sci-table::before) {
  display: none;
}

:deep(.sci-table .el-table__inner-wrapper::before) {
  display: none;
}

:deep(.sci-table .el-table__empty-block) {
  background: transparent !important;
}

:deep(.sci-table .el-scrollbar),
:deep(.sci-table .el-scrollbar__wrap),
:deep(.sci-table .el-scrollbar__view) {
  background: transparent !important;
}

:deep(.sci-table .el-table__fixed),
:deep(.sci-table .el-table__fixed-right),
:deep(.sci-table .el-table__fixed-right-patch),
:deep(.sci-table .el-table__fixed-body-wrapper),
:deep(.sci-table .el-table__fixed-header-wrapper) {
  background: transparent !important;
}

:deep(.sci-table .el-table__fixed-body-wrapper td),
:deep(.sci-table .el-table__fixed-header-wrapper th) {
  background: transparent !important;
}

:deep(.sci-table .el-table__fixed-body-wrapper tr:hover td) {
  background: rgba(0, 212, 255, 0.08) !important;
}

.text-primary {
  color: #00d4ff;
  font-weight: 500;
}

/* 状态徽章 */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.online {
  background: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.25);
  color: rgba(103, 194, 58, 0.95);
  box-shadow: 0 0 12px rgba(103, 194, 58, 0.1);
}

.status-badge.offline {
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.25);
  color: rgba(245, 108, 108, 0.95);
  box-shadow: 0 0 12px rgba(245, 108, 108, 0.1);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 6px currentColor;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-icon-btn {
  width: 32px;
  height: 32px;
  padding: 0 !important;
  background: rgba(0, 212, 255, 0.08) !important;
  border: 1px solid rgba(0, 212, 255, 0.15) !important;
  color: rgba(0, 212, 255, 0.85) !important;
  transition: all 0.3s ease !important;
}

.action-icon-btn:hover {
  background: rgba(0, 212, 255, 0.2) !important;
  border-color: rgba(0, 212, 255, 0.4) !important;
  color: #00d4ff !important;
  box-shadow: 0 0 12px rgba(0, 212, 255, 0.2);
  transform: translateY(-1px);
}

.action-icon-btn.edit {
  background: rgba(168, 85, 247, 0.08) !important;
  border-color: rgba(168, 85, 247, 0.15) !important;
  color: rgba(168, 85, 247, 0.85) !important;
}

.action-icon-btn.edit:hover {
  background: rgba(168, 85, 247, 0.2) !important;
  border-color: rgba(168, 85, 247, 0.4) !important;
  color: #a855f7 !important;
  box-shadow: 0 0 12px rgba(168, 85, 247, 0.2);
}

.action-icon-btn.danger {
  background: rgba(245, 108, 108, 0.08) !important;
  border-color: rgba(245, 108, 108, 0.15) !important;
  color: rgba(245, 108, 108, 0.85) !important;
}

.action-icon-btn.danger:hover {
  background: rgba(245, 108, 108, 0.2) !important;
  border-color: rgba(245, 108, 108, 0.4) !important;
  color: #f56c6c !important;
  box-shadow: 0 0 12px rgba(245, 108, 108, 0.2);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 212, 255, 0.08);
}

:deep(.pagination .el-pagination) {
  color: rgba(255, 255, 255, 0.6) !important;
}

:deep(.pagination .el-pagination .el-pager li) {
  background: rgba(0, 212, 255, 0.08) !important;
  border: 1px solid rgba(0, 212, 255, 0.15) !important;
  color: rgba(255, 255, 255, 0.7) !important;
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}

:deep(.pagination .el-pagination .el-pager li:hover) {
  background: rgba(0, 212, 255, 0.15) !important;
  border-color: rgba(0, 212, 255, 0.3) !important;
}

:deep(.pagination .el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #00a8ff, #00d4ff) !important;
  color: #fff !important;
  border-color: transparent !important;
  box-shadow: 0 0 12px rgba(0, 212, 255, 0.3);
}

:deep(.pagination .el-pagination button) {
  background: rgba(0, 212, 255, 0.08) !important;
  border: 1px solid rgba(0, 212, 255, 0.15) !important;
  color: rgba(255, 255, 255, 0.7) !important;
  border-radius: 8px !important;
}

:deep(.pagination .el-pagination .el-input__wrapper) {
  background: rgba(0, 212, 255, 0.08) !important;
  border: 1px solid rgba(0, 212, 255, 0.15) !important;
  box-shadow: none !important;
  border-radius: 8px !important;
}

:deep(.pagination .el-pagination .el-input__inner) {
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.pagination .el-pagination__total,
      .pagination .el-pagination__jump) {
  color: rgba(255, 255, 255, 0.5) !important;
}

/* 弹窗 */
:deep(.sci-dialog .el-dialog) {
  background: linear-gradient(135deg, #0a1628, #0d1b3e) !important;
  border: 1px solid rgba(0, 212, 255, 0.2) !important;
  border-radius: 20px !important;
  box-shadow: 0 0 50px rgba(0, 212, 255, 0.15) !important;
}

:deep(.sci-dialog .el-dialog__header) {
  border-bottom: 1px solid rgba(0, 212, 255, 0.1) !important;
  margin-right: 0 !important;
  padding: 20px 26px !important;
}

:deep(.sci-dialog .el-dialog__title) {
  color: #fff !important;
  font-weight: 700;
  letter-spacing: 1px;
  font-size: 17px;
}

:deep(.sci-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.5) !important;
}

:deep(.sci-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
  color: #00d4ff !important;
}

:deep(.sci-dialog .el-dialog__body) {
  color: rgba(255, 255, 255, 0.8) !important;
  padding: 26px !important;
}

:deep(.sci-dialog .el-dialog__footer) {
  border-top: 1px solid rgba(0, 212, 255, 0.1) !important;
  padding: 16px 26px !important;
}

/* 弹窗输入框 */
:deep(.sci-input .el-input__wrapper) {
  background: rgba(0, 0, 0, 0.25) !important;
  border: 1px solid rgba(0, 212, 255, 0.2) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.1) inset !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
}

:deep(.sci-input .el-input__wrapper:hover) {
  border-color: rgba(0, 212, 255, 0.4) !important;
}

:deep(.sci-input .el-input__wrapper.is-focus) {
  border-color: rgba(0, 212, 255, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.2) inset, 0 0 15px rgba(0, 212, 255, 0.1) !important;
}

:deep(.sci-input .el-input__inner) {
  color: #fff !important;
}

:deep(.sci-input .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

/* 单选 */
:deep(.sci-radio .el-radio__input.is-checked .el-radio__inner) {
  border-color: #00d4ff !important;
  background: #00d4ff !important;
}

:deep(.sci-radio .el-radio__input.is-checked + .el-radio__label) {
  color: #00d4ff !important;
}

:deep(.sci-radio .el-radio__label) {
  color: rgba(255, 255, 255, 0.7) !important;
}

/* 描述列表 */
:deep(.sci-descriptions .el-descriptions__body) {
  background: transparent !important;
}

:deep(.sci-descriptions .el-descriptions__cell) {
  background: rgba(0, 212, 255, 0.05) !important;
  border: 1px solid rgba(0, 212, 255, 0.1) !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.sci-descriptions .el-descriptions__label) {
  background: rgba(0, 212, 255, 0.08) !important;
  color: #00d4ff !important;
  font-weight: 500;
}

/* 表单标签 */
:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.7) !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .action-btns {
    gap: 4px;
  }
}
</style>
