<template>
  <div class="mass-message-container">
    <!-- 顶部操作栏 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Promotion /></el-icon>
        <div class="title-text">
          <h2>消息群发任务</h2>
          <p>批量向多个微信好友或群聊发送消息，支持定时发送、时段控制与间隔策略</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="create-btn" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          <span>新建任务</span>
        </el-button>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="task-section">
      <div class="section-toolbar">
        <div class="section-header">
          <el-icon><List /></el-icon>
          <span>任务列表</span>
        </div>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索任务名称"
          clearable
          class="search-input"
          @keyup.enter="loadTaskList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <el-table
        :data="taskList"
        v-loading="loading"
        class="sci-table"
        row-class-name="sci-table-row"
      >
        <el-table-column prop="name" label="任务名称" min-width="160">
          <template #default="{ row }">
            <span class="task-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="微信账号" min-width="160">
          <template #default="{ row }">
            <div class="account-name">{{ getAccountDisplayName(row.appId) }}</div>
            <div v-if="getAccountDisplayName(row.appId) !== row.appId" class="account-id text-mono">
              {{ row.appId }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="群发对象" width="100">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.targetType === 1 ? 'success' : row.targetType === 2 ? 'warning' : 'danger'"
              effect="dark"
            >
              {{ row.targetType === 1 ? '好友' : row.targetType === 2 ? '群聊' : '混合' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sendType" label="发送方式" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.sendType === 1 ? 'primary' : 'info'" effect="dark">
              {{ row.sendType === 1 ? '立即' : '定时' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)" effect="dark">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="160">
          <template #default="{ row }">
            <div class="progress-text">
              <span class="success">{{ row.successCount || 0 }}</span>
              <span>/</span>
              <span class="total">{{ row.totalCount || 0 }}</span>
              <span class="fail" v-if="row.failCount">（失败 {{ row.failCount }}）</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="intervalSeconds" label="间隔" width="90">
          <template #default="{ row }">
            <span>{{ row.intervalSeconds || 0 }} 秒</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160">
          <template #default="{ row }">
            <span class="text-dim">{{ row.createTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button link class="action-btn action-record" @click="showRecords(row)">
                记录
              </el-button>
              <el-button
                v-if="row.status === 0 || row.status === 1"
                link
                class="action-btn action-pause"
                @click="handlePause(row)"
              >
                暂停
              </el-button>
              <el-button
                v-if="row.status === 0 || row.status === 1 || row.status === 3"
                link
                class="action-btn action-cancel"
                @click="handleCancel(row)"
              >
                取消
              </el-button>
              <el-button link class="action-btn action-delete" @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon size="48"><Promotion /></el-icon>
            <p>暂无群发任务</p>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="loadTaskList"
          @current-change="loadTaskList"
        />
      </div>
    </div>

    <!-- 创建任务弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建群发任务"
      width="720px"
      :close-on-click-modal="false"
      class="sci-dialog create-task-dialog"
      destroy-on-close
    >
      <div class="create-task-form">
        <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="90px">
          <!-- 基础设置 -->
          <div class="form-section compact">
            <div class="section-title">
              <el-icon><Setting /></el-icon>
              <span>基础设置</span>
            </div>
            <div class="section-body">
              <el-form-item label="任务名称" prop="name">
                <el-input v-model="createForm.name" placeholder="请输入任务名称" clearable />
              </el-form-item>
              <el-form-item label="微信账号" prop="appId">
                <el-select
                  v-model="createForm.appId"
                  placeholder="请选择微信账号"
                  class="full-select"
                  @change="onAccountChange"
                >
                  <el-option
                    v-for="item in accountList"
                    :key="item.appId"
                    :label="`${item.nickName || item.wxid || item.appId} (${item.appId})`"
                    :value="item.appId"
                  />
                </el-select>
              </el-form-item>
            </div>
          </div>

          <!-- 发送对象 -->
          <div class="form-section compact">
            <div class="section-title">
              <el-icon><User /></el-icon>
              <span>发送对象</span>
              <span class="section-count" v-if="createForm.contactWxids.length">
                {{ createForm.contactWxids.length }} 个
              </span>
            </div>
            <div class="section-body">
              <el-form-item prop="contactWxids">
                <div class="recipients-area" :class="{ empty: !createForm.contactWxids.length }">
                  <div v-if="!createForm.contactWxids.length" class="recipients-empty">
                    <el-icon size="32"><User /></el-icon>
                    <p>请选择要发送的好友或群聊</p>
                    <el-button type="primary" plain @click="openContactSelector">
                      <el-icon><Plus /></el-icon>
                      <span>选择联系人</span>
                    </el-button>
                  </div>
                  <div v-else class="recipients-summary">
                    <div class="summary-tags">
                      <el-tag
                        v-for="item in displayedSelectedContacts"
                        :key="item.contactWxid"
                        closable
                        effect="dark"
                        class="recipient-chip"
                        @close="removeContact(item.contactWxid)"
                      >
                        <el-avatar :size="18" :src="item.smallHeadImgUrl || item.bigHeadImgUrl" class="chip-avatar">
                          <el-icon size="12"><UserFilled /></el-icon>
                        </el-avatar>
                        <span class="chip-name">{{ item.remark || item.nickName || item.contactWxid }}</span>
                      </el-tag>
                      <el-tag
                        v-if="selectedContactDetails.length > displayedSelectedContacts.length"
                        effect="dark"
                        class="recipient-chip more-chip"
                        @click="openContactSelector"
                      >
                        +{{ selectedContactDetails.length - displayedSelectedContacts.length }}
                      </el-tag>
                    </div>
                    <div class="summary-actions">
                      <el-button type="primary" plain size="small" @click="openContactSelector">
                        <el-icon><Plus /></el-icon>
                        <span>添加</span>
                      </el-button>
                      <el-button type="danger" link size="small" @click="clearAllContacts">
                        <el-icon><Delete /></el-icon>
                        <span>清空</span>
                      </el-button>
                    </div>
                  </div>
                </div>
              </el-form-item>
            </div>
          </div>

          <!-- 消息内容 -->
          <div class="form-section compact">
            <div class="section-title">
              <el-icon><ChatDotRound /></el-icon>
              <span>消息内容</span>
            </div>
            <div class="section-body">
              <el-form-item prop="content">
                <div class="content-wrapper">
                  <el-input
                    v-model="createForm.content"
                    type="textarea"
                    :rows="4"
                    :placeholder="createForm.materialId ? '已选择素材，可直接修改或清空后重新选择' : '请输入要群发的消息内容，或点击下方按钮选择素材'"
                    show-word-limit
                    maxlength="2000"
                  />
                  <div class="content-actions">
                    <el-button type="primary" plain @click="openMaterialSelector">
                      <el-icon><PictureRounded /></el-icon>
                      <span>选择素材</span>
                    </el-button>
                    <el-tag v-if="selectedMaterial.name" size="small" effect="dark" class="material-tag">
                      已选：{{ selectedMaterial.name }}
                    </el-tag>
                    <el-button v-if="createForm.materialId" type="danger" link @click="clearMaterial">
                      <el-icon><CircleClose /></el-icon>
                      <span>清除素材</span>
                    </el-button>
                  </div>
                  <!-- 消息内容预览 -->
                  <div v-if="hasMessageContent" class="content-preview-box">
                    <div class="preview-label">
                      <el-icon><View /></el-icon>
                      <span>发送预览</span>
                    </div>
                    <div class="preview-body">
                      <!-- 图片 -->
                      <div v-if="messagePreviewType === 1" class="preview-media">
                        <el-image :src="messagePreviewUrl" fit="contain" class="preview-img" />
                      </div>
                      <!-- 视频 -->
                      <div v-else-if="messagePreviewType === 4" class="preview-media">
                        <video :src="messagePreviewUrl" :poster="messagePreviewThumbUrl" controls class="preview-video-player" />
                        <div v-if="messagePreviewVideoDuration" class="preview-meta">时长：{{ messagePreviewVideoDuration }} 秒</div>
                      </div>
                      <!-- 文件 -->
                      <div v-else-if="messagePreviewType === 6" class="preview-file">
                        <el-icon class="preview-file-icon"><Document /></el-icon>
                        <div class="preview-file-info">
                          <div class="preview-file-name">{{ messagePreviewFileName || selectedMaterial.name || '文件' }}</div>
                          <el-link type="primary" :href="messagePreviewUrl" target="_blank">下载 / 预览</el-link>
                        </div>
                      </div>
                      <!-- 文本 / 链接 -->
                      <div v-else-if="messagePreviewType === 2 || messagePreviewType === 5" class="preview-text">
                        {{ messagePreviewText }}
                      </div>
                      <!-- 默认 -->
                      <div v-else class="preview-text">
                        {{ createForm.content }}
                      </div>
                    </div>
                  </div>
                </div>
              </el-form-item>
            </div>
          </div>

          <!-- 发送策略 -->
          <div class="form-section compact">
            <div class="section-title">
              <el-icon><Timer /></el-icon>
              <span>发送策略</span>
            </div>
            <div class="section-body">
                <el-form-item label="发送方式" prop="sendType">
                  <el-radio-group v-model="createForm.sendType">
                    <el-radio-button :value="1">立即发送</el-radio-button>
                    <el-radio-button :value="2">定时发送</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="定时时间" prop="scheduleTime" v-if="createForm.sendType === 2">
                  <el-date-picker
                    v-model="createForm.scheduleTime"
                    type="datetime"
                    placeholder="请选择定时发送时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    class="full-picker"
                  />
                </el-form-item>
                <el-form-item label="日期区间" prop="dateRange" v-if="createForm.sendType === 2">
                  <el-date-picker
                    v-model="createForm.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    class="full-picker"
                  />
                </el-form-item>
                <el-form-item label="发送时段" prop="timeRange" v-if="createForm.sendType === 2">
                  <el-time-picker
                    v-model="createForm.timeRange"
                    is-range
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    format="HH:mm:ss"
                    value-format="HH:mm:ss"
                    class="full-picker"
                  />
                </el-form-item>
                <el-form-item label="间隔秒数" prop="intervalSeconds">
                  <div class="interval-row">
                    <el-input-number v-model="createForm.intervalSeconds" :min="0" :max="3600" :step="1" />
                    <span class="form-tip-text">0 表示连续发送</span>
                  </div>
                </el-form-item>
              </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createLoading" @click="handleCreate">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 联系人选择弹窗 -->
    <el-dialog
      v-model="contactDialogVisible"
      title="选择联系人"
      width="640px"
      :close-on-click-modal="false"
      class="sci-dialog contact-selector-dialog"
      destroy-on-close
    >
      <div class="contact-selector">
        <div class="selector-sidebar">
          <div
            v-for="tab in contactTabs"
            :key="tab.value"
            class="selector-tab"
            :class="{ active: contactTab === tab.value }"
            @click="contactTab = tab.value"
          >
            <el-icon size="18"><component :is="tab.icon" /></el-icon>
            <span>{{ tab.label }}</span>
            <span class="tab-count">{{ tab.count }}</span>
          </div>
        </div>
        <div class="selector-main">
          <div class="selector-search-bar">
            <el-input
              v-model="contactKeyword"
              placeholder="搜索昵称/备注/wxid"
              clearable
              class="contact-search"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" link @click="selectAllContacts">
              {{ isAllSelected ? '取消全选' : '全选' }}
            </el-button>
          </div>
          <div class="selector-list" v-loading="contactLoading">
            <div
              v-for="item in filteredContacts"
              :key="item.contactWxid"
              class="selector-item"
              :class="{ selected: isContactSelected(item.contactWxid) }"
              @click="toggleContactSelection(item)"
            >
              <div class="selector-checkbox">
                <el-checkbox :model-value="isContactSelected(item.contactWxid)" />
              </div>
              <el-avatar :size="42" :src="item.smallHeadImgUrl || item.bigHeadImgUrl">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="selector-item-info">
                <div class="selector-item-name">{{ item.remark || item.nickName || '-' }}</div>
                <div class="selector-item-wxid">{{ item.contactWxid }}</div>
              </div>
              <el-tag size="small" effect="dark" class="selector-item-type">
                {{ item.type === 1 ? '好友' : '群聊' }}
              </el-tag>
            </div>
          </div>
          <div class="selector-footer">
            <span class="selector-tip">已选择 {{ selectedContacts.length }} 个</span>
            <div>
              <el-button @click="contactDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="confirmContacts">确定</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 素材选择弹窗 -->
    <el-dialog
      v-model="materialDialogVisible"
      title="选择素材"
      width="720px"
      :close-on-click-modal="false"
      class="sci-dialog"
      destroy-on-close
    >
      <div class="material-toolbar">
        <el-input
          v-model="materialKeyword"
          placeholder="搜索素材名称"
          clearable
          class="material-search"
          @keyup.enter="loadMaterials"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="materialType"
          placeholder="全部类型"
          clearable
          class="material-type-select"
          @change="loadMaterials"
        >
          <el-option
            v-for="item in materialTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" @click="loadMaterials">
          <el-icon><Search /></el-icon>
          <span>查询</span>
        </el-button>
      </div>
      <el-table
        :data="materialList"
        height="360"
        class="sci-table"
        highlight-current-row
        @current-change="handleMaterialSelect"
      >
        <el-table-column width="60" align="center">
          <template #default="{ row }">
            <el-radio :value="row.id" v-model="selectedMaterialId" style="margin: 0" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="素材名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :color="materialTypeColor(row.type)" effect="dark" class="type-tag">
              {{ materialTypeMap[row.type] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容预览" min-width="220">
          <template #default="{ row }">
            <el-popover
              placement="top-start"
              :width="260"
              trigger="hover"
              :show-after="200"
              :hide-after="200"
              popper-class="material-preview-popover"
            >
              <template #reference>
                <span class="material-content-preview">
                  <el-icon class="preview-icon"><View /></el-icon>
                  {{ formatMaterialPreview(row) }}
                </span>
              </template>
              <div class="material-hover-preview">
                <div class="hover-preview-title">{{ row.name }}</div>
                <div class="hover-preview-body">
                  <!-- 图片 -->
                  <el-image
                    v-if="row.type === 1"
                    :src="getMaterialPreviewUrl(row.content)"
                    fit="contain"
                    class="hover-preview-image"
                  />
                  <!-- 视频 -->
                  <video
                    v-else-if="row.type === 4"
                    :src="getMaterialPreviewUrl(row.content)"
                    :poster="getMaterialPreviewThumbUrl(row.content)"
                    controls
                    class="hover-preview-video"
                  />
                  <!-- 文件 -->
                  <div v-else-if="row.type === 6" class="hover-preview-file">
                    <el-icon class="hover-file-icon"><Document /></el-icon>
                    <div class="hover-file-info">
                      <div class="hover-file-name">{{ getMaterialPreviewFileName(row.content) || row.name }}</div>
                      <el-link type="primary" :href="getMaterialPreviewUrl(row.content)" target="_blank">下载 / 预览</el-link>
                    </div>
                  </div>
                  <!-- 文本 / 链接 / 小程序 -->
                  <div v-else class="hover-preview-text">
                    {{ formatMaterialPreview(row) }}
                  </div>
                </div>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="materialPageNum"
          v-model:page-size="materialPageSize"
          :page-sizes="[10, 20, 50]"
          :total="materialTotal"
          layout="total, prev, pager, next"
          background
          @size-change="loadMaterials"
          @current-change="loadMaterials"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="materialDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmMaterial">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务记录抽屉 -->
    <el-drawer
      v-model="recordDrawerVisible"
      title="发送记录"
      size="680px"
      class="sci-drawer"
      destroy-on-close
    >
      <div class="record-header">
        <div class="record-account" v-if="currentTask">
          <el-icon><User /></el-icon>
          <span class="record-account-name">{{ getAccountDisplayName(currentTask.appId) }}</span>
          <span v-if="getAccountDisplayName(currentTask.appId) !== currentTask.appId" class="record-account-id text-mono">
            {{ currentTask.appId }}
          </span>
        </div>
        <div class="record-stat">
          <div class="stat-item">
            <span class="stat-label">总数</span>
            <span class="stat-value">{{ currentTask?.totalCount || 0 }}</span>
          </div>
          <div class="stat-item success">
            <span class="stat-label">成功</span>
            <span class="stat-value">{{ currentTask?.successCount || 0 }}</span>
          </div>
          <div class="stat-item fail">
            <span class="stat-label">失败</span>
            <span class="stat-value">{{ currentTask?.failCount || 0 }}</span>
          </div>
        </div>
        <el-input
          v-model="recordKeyword"
          placeholder="搜索联系人"
          clearable
          class="record-search"
          @keyup.enter="loadRecordList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-table :data="recordList" v-loading="recordLoading" class="sci-table">
        <el-table-column label="联系人" min-width="160">
          <template #default="{ row }">
            <div class="record-name">{{ row.nickName || row.contactWxid }}</div>
            <div class="record-wxid">{{ row.contactWxid }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="recordStatusType(row.status)" effect="dark">
              {{ recordStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="失败原因" min-width="160">
          <template #default="{ row }">
            <span class="fail-text">{{ row.errorMsg || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sendTime" label="发送时间" min-width="160">
          <template #default="{ row }">
            <span class="text-dim">{{ row.sendTime || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="recordPageNum"
          v-model:page-size="recordPageSize"
          :page-sizes="[10, 20, 50]"
          :total="recordTotal"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="loadRecordList"
          @current-change="loadRecordList"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createMassTask,
  pageMassTasks,
  pauseMassTask,
  cancelMassTask,
  deleteMassTask,
  pageMassTaskRecords
} from '@/api/massTask'
import { listAccounts } from '@/api/wx'
import { pageList as pageMaterials } from '@/api/material'
import request from '@/utils/request'

// 联系人类型
const CONTACT_TYPE_FRIEND = 1
const CONTACT_TYPE_GROUP = 2
const CONTACT_TYPE_ALL = 0

// 任务列表
const taskList = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 创建任务
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref()
const createForm = reactive({
  name: '',
  appId: '',
  content: '',
  materialId: null,
  sendType: 1,
  scheduleTime: '',
  dateRange: [],
  timeRange: ['09:00:00', '18:00:00'],
  intervalSeconds: 3,
  contactWxids: []
})

// 根据已选联系人推导目标类型：1-好友，2-群聊，3-混合
const targetType = computed(() => {
  const types = new Set(selectedContactDetails.value.map((item) => item.type))
  if (types.size === 0) return 1
  if (types.size === 1) {
    return types.has(CONTACT_TYPE_FRIEND) ? 1 : 2
  }
  return 3
})

const targetTypeText = computed(() => {
  const map = { 1: '好友', 2: '群聊', 3: '混合' }
  return map[targetType.value] || '未知'
})

// 已选联系人最多展示前 8 个，剩余以 +N 折叠
const displayedSelectedContacts = computed(() => {
  return selectedContactDetails.value.slice(0, 8)
})

const createRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  appId: [{ required: true, message: '请选择微信账号', trigger: 'change' }],
  sendType: [{ required: true, message: '请选择发送方式', trigger: 'change' }],
  scheduleTime: [{ required: true, message: '请选择定时发送时间', trigger: 'change' }],
  contactWxids: [{ required: true, message: '请选择联系人', trigger: 'change', type: 'array' }],
  content: [
    {
      validator: (rule, value, callback) => {
        if (!value && !createForm.materialId) {
          callback(new Error('请输入消息内容或选择素材'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 账号列表
const accountList = ref([])

// 素材选择
const materialDialogVisible = ref(false)
const materialKeyword = ref('')
const materialType = ref(null)
const materialList = ref([])
const materialPageNum = ref(1)
const materialPageSize = ref(10)
const materialTotal = ref(0)
const selectedMaterialId = ref(null)
const selectedMaterial = reactive({
  id: null,
  name: '',
  type: null,
  content: ''
})

const materialTypeOptions = [
  { value: 1, label: '图片' },
  { value: 2, label: '文本' },
  { value: 3, label: '小程序' },
  { value: 4, label: '视频' },
  { value: 5, label: '链接' },
  { value: 6, label: '文件' }
]

const materialTypeMap = {
  1: '图片',
  2: '文本',
  3: '小程序',
  4: '视频',
  5: '链接',
  6: '文件'
}

const materialTypeColors = {
  1: '#5b8cff',
  2: '#67c23a',
  3: '#a855f7',
  4: '#f59e0b',
  5: '#409eff',
  6: '#f56c6c'
}

const materialTypeColor = (type) => materialTypeColors[type] || '#909399'

// 根据 appId 获取微信账号展示名称：优先昵称，其次 wxid，兜底 appId
const getAccountDisplayName = (appId) => {
  const account = accountList.value.find((item) => item.appId === appId)
  if (!account) return appId || '-'
  return account.nickName || account.wxid || account.appId || '-'
}

const formatMaterialPreview = (row) => {
  if (!row.content) return '-'
  try {
    const obj = JSON.parse(row.content)
    if (typeof obj === 'string') return obj
    if (obj.fileName && obj.url) return `${obj.fileName} · ${obj.url}`
    if (obj.fileName) return obj.fileName
    if (obj.text) return obj.text
    if (obj.url) return obj.url
    if (obj.title) return obj.title
    return JSON.stringify(obj)
  } catch (e) {
    return row.content
  }
}

// 解析素材 content JSON
const parseMaterialContent = (content) => {
  if (!content) return {}
  try {
    return JSON.parse(content)
  } catch (e) {
    return { text: content }
  }
}

const getMaterialPreviewUrl = (content) => {
  const obj = parseMaterialContent(content)
  return obj.imgUrl || obj.imageUrl || obj.videoUrl || obj.fileUrl || obj.url || ''
}

const getMaterialPreviewThumbUrl = (content) => {
  const obj = parseMaterialContent(content)
  return obj.thumbUrl || ''
}

const getMaterialPreviewFileName = (content) => {
  const obj = parseMaterialContent(content)
  return obj.fileName || ''
}

// 已选联系人详情（用于展示）
const selectedContactDetails = computed(() => {
  return createForm.contactWxids
    .map((wxid) => contactList.value.find((item) => item.contactWxid === wxid))
    .filter(Boolean)
})

// 是否有可预览的消息内容
const hasMessageContent = computed(() => {
  return !!(createForm.content || selectedMaterial.content)
})

// 消息预览类型：优先以素材类型为准
const messagePreviewType = computed(() => {
  if (selectedMaterial.type) return selectedMaterial.type
  if (createForm.content) {
    try {
      const obj = JSON.parse(createForm.content)
      if (obj.imgUrl || obj.imageUrl) return 1
      if (obj.videoUrl) return 4
      if (obj.fileUrl) return 6
      if (obj.text) return 2
      if (obj.title || obj.url) return 5
    } catch (e) {
      return 2
    }
  }
  return 0
})

// 解析当前预览内容
const previewContentObj = computed(() => {
  const raw = selectedMaterial.content || createForm.content
  if (!raw) return {}
  try {
    return JSON.parse(raw)
  } catch (e) {
    return { text: raw }
  }
})

const messagePreviewUrl = computed(() => {
  const obj = previewContentObj.value
  return obj.imgUrl || obj.imageUrl || obj.videoUrl || obj.fileUrl || obj.url || ''
})

const messagePreviewThumbUrl = computed(() => {
  return previewContentObj.value.thumbUrl || ''
})

const messagePreviewVideoDuration = computed(() => {
  return previewContentObj.value.videoDuration || 0
})

const messagePreviewFileName = computed(() => {
  return previewContentObj.value.fileName || ''
})

const messagePreviewText = computed(() => {
  const obj = previewContentObj.value
  if (obj.text) return obj.text
  if (obj.title) {
    const parts = [obj.title]
    if (obj.desc || obj.description) parts.push(obj.desc || obj.description)
    if (obj.url) parts.push(obj.url)
    return parts.join('\n')
  }
  return obj.url || createForm.content
})

// 联系人选择
const contactDialogVisible = ref(false)
const contactKeyword = ref('')
const contactList = ref([])
const selectedContacts = ref([])
const contactLoading = ref(false)
const contactTab = ref(CONTACT_TYPE_ALL)

const contactTabs = computed(() => {
  const allCount = contactList.value.length
  const friendCount = contactList.value.filter((item) => item.type === CONTACT_TYPE_FRIEND).length
  const groupCount = contactList.value.filter((item) => item.type === CONTACT_TYPE_GROUP).length
  return [
    { value: CONTACT_TYPE_ALL, label: '全部', icon: 'User', count: allCount },
    { value: CONTACT_TYPE_FRIEND, label: '好友', icon: 'User', count: friendCount },
    { value: CONTACT_TYPE_GROUP, label: '群聊', icon: 'ChatRound', count: groupCount }
  ]
})

const filteredContacts = computed(() => {
  let list = contactList.value
  if (contactTab.value !== CONTACT_TYPE_ALL) {
    list = list.filter((item) => item.type === contactTab.value)
  }
  if (!contactKeyword.value) return list
  const keyword = contactKeyword.value.toLowerCase()
  return list.filter(
    (item) =>
      (item.nickName && item.nickName.toLowerCase().includes(keyword)) ||
      (item.remark && item.remark.toLowerCase().includes(keyword)) ||
      (item.contactWxid && item.contactWxid.toLowerCase().includes(keyword))
  )
})

const isAllSelected = computed(() => {
  return filteredContacts.value.length > 0 && selectedContacts.value.length === filteredContacts.value.length
})

const isContactSelected = (wxid) => {
  return selectedContacts.value.some((item) => item.contactWxid === wxid)
}

const toggleContactSelection = (item) => {
  const index = selectedContacts.value.findIndex((c) => c.contactWxid === item.contactWxid)
  if (index > -1) {
    selectedContacts.value.splice(index, 1)
  } else {
    selectedContacts.value.push(item)
  }
}

const removeContact = (wxid) => {
  createForm.contactWxids = createForm.contactWxids.filter((id) => id !== wxid)
}

const clearAllContacts = () => {
  createForm.contactWxids = []
  selectedContacts.value = []
  createFormRef.value?.validateField('contactWxids')
}

// 任务记录
const recordDrawerVisible = ref(false)
const recordList = ref([])
const recordLoading = ref(false)
const recordKeyword = ref('')
const recordPageNum = ref(1)
const recordPageSize = ref(10)
const recordTotal = ref(0)
const currentTask = ref(null)

// 状态映射
const statusMap = {
  0: { text: '待执行', type: 'info' },
  1: { text: '执行中', type: 'primary' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已暂停', type: 'warning' },
  4: { text: '已取消', type: 'danger' },
  5: { text: '已失败', type: 'danger' }
}

const statusText = (status) => statusMap[status]?.text || '未知'
const statusType = (status) => statusMap[status]?.type || 'info'

const recordStatusMap = {
  0: { text: '待发送', type: 'info' },
  1: { text: '成功', type: 'success' },
  2: { text: '失败', type: 'danger' }
}

const recordStatusText = (status) => recordStatusMap[status]?.text || '未知'
const recordStatusType = (status) => recordStatusMap[status]?.type || 'info'

// 加载账号列表
const loadAccountList = async () => {
  try {
    const res = await listAccounts()
    accountList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 打开素材选择器
const openMaterialSelector = () => {
  materialDialogVisible.value = true
  materialKeyword.value = ''
  materialType.value = null
  materialPageNum.value = 1
  selectedMaterialId.value = createForm.materialId
  loadMaterials()
}

// 加载素材列表（仅当前用户自己的素材）
const loadMaterials = async () => {
  try {
    const res = await pageMaterials({
      pageNum: materialPageNum.value,
      pageSize: materialPageSize.value,
      keyword: materialKeyword.value,
      type: materialType.value
    })
    const data = res.data || {}
    materialList.value = data.list || []
    materialTotal.value = data.total || 0
  } catch (error) {
    console.error(error)
  }
}

// 表格行点击选择素材
const handleMaterialSelect = (row) => {
  if (row) {
    selectedMaterialId.value = row.id
  }
}

// 确认选择素材
const confirmMaterial = () => {
  const row = materialList.value.find((item) => item.id === selectedMaterialId.value)
  if (!row) {
    ElMessage.warning('请先选择一个素材')
    return
  }
  createForm.materialId = row.id
  selectedMaterial.id = row.id
  selectedMaterial.name = row.name
  selectedMaterial.type = row.type
  selectedMaterial.content = row.content
  // 文本/链接类型默认将预览内容回填到 content 输入框，方便用户二次编辑
  createForm.content = formatMaterialPreview(row)
  materialDialogVisible.value = false
  createFormRef.value?.validateField('content')
}

// 清除已选素材
const clearMaterial = () => {
  createForm.materialId = null
  selectedMaterial.id = null
  selectedMaterial.name = ''
  selectedMaterial.type = null
  selectedMaterial.content = ''
  createFormRef.value?.validateField('content')
}

// 加载任务列表
const loadTaskList = async () => {
  loading.value = true
  try {
    const res = await pageMassTasks({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    })
    const data = res.data || {}
    taskList.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 打开创建弹窗
const openCreateDialog = () => {
  resetCreateForm()
  createDialogVisible.value = true
}

const resetCreateForm = () => {
  createForm.name = ''
  createForm.appId = ''
  createForm.content = ''
  createForm.materialId = null
  createForm.sendType = 1
  createForm.scheduleTime = ''
  createForm.dateRange = []
  createForm.timeRange = ['09:00:00', '18:00:00']
  createForm.intervalSeconds = 3
  createForm.contactWxids = []
  selectedContacts.value = []
  selectedMaterial.id = null
  selectedMaterial.name = ''
  selectedMaterial.type = null
  selectedMaterial.content = ''
  nextTick(() => {
    createFormRef.value?.clearValidate()
  })
}

// 账号切换
const onAccountChange = () => {
  createForm.contactWxids = []
  selectedContacts.value = []
}

// 打开联系人选择器
const openContactSelector = async () => {
  if (!createForm.appId) {
    ElMessage.warning('请先选择微信账号')
    return
  }
  const account = accountList.value.find((item) => item.appId === createForm.appId)
  if (!account || !account.wxid) {
    ElMessage.warning('所选账号缺少 wxid')
    return
  }
  contactDialogVisible.value = true
  contactKeyword.value = ''
  contactTab.value = CONTACT_TYPE_ALL
  contactLoading.value = true
  try {
    const res = await request({
      url: '/wx/contact/list',
      method: 'get',
      params: { ownerWxid: account.wxid }
    })
    const list = res.data || []
    contactList.value = list
    // 回显已选联系人
    selectedContacts.value = list.filter((row) =>
      createForm.contactWxids.includes(row.contactWxid)
    )
  } catch (error) {
    console.error(error)
  } finally {
    contactLoading.value = false
  }
}

// 全选/取消全选
const selectAllContacts = () => {
  if (isAllSelected.value) {
    filteredContacts.value.forEach((row) => {
      const index = selectedContacts.value.findIndex((c) => c.contactWxid === row.contactWxid)
      if (index > -1) {
        selectedContacts.value.splice(index, 1)
      }
    })
  } else {
    filteredContacts.value.forEach((row) => {
      if (!isContactSelected(row.contactWxid)) {
        selectedContacts.value.push(row)
      }
    })
  }
}

const confirmContacts = () => {
  createForm.contactWxids = selectedContacts.value.map((item) => item.contactWxid)
  contactDialogVisible.value = false
  createFormRef.value?.validateField('contactWxids')
}

// 创建任务
const handleCreate = () => {
  createFormRef.value.validate(async (valid) => {
    if (!valid) return
    createLoading.value = true
    try {
      const isScheduled = createForm.sendType === 2
      const params = {
        name: createForm.name,
        appId: createForm.appId,
        targetType: targetType.value,
        content: createForm.content,
        materialId: createForm.materialId,
        sendType: createForm.sendType,
        startDate: isScheduled ? createForm.dateRange?.[0] || null : null,
        endDate: isScheduled ? createForm.dateRange?.[1] || null : null,
        startTime: isScheduled ? createForm.timeRange?.[0] || null : null,
        endTime: isScheduled ? createForm.timeRange?.[1] || null : null,
        intervalSeconds: createForm.intervalSeconds,
        scheduleTime: isScheduled ? createForm.scheduleTime : null,
        contactWxids: createForm.contactWxids
      }
      await createMassTask(params)
      ElMessage.success('任务创建成功')
      createDialogVisible.value = false
      loadTaskList()
    } catch (error) {
      console.error(error)
    } finally {
      createLoading.value = false
    }
  })
}

// 暂停任务
const handlePause = (row) => {
  ElMessageBox.confirm(`确定要暂停任务「${row.name}」吗？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await pauseMassTask(row.id)
      ElMessage.success('暂停成功')
      loadTaskList()
    } catch (error) {
      console.error(error)
    }
  })
}

// 取消任务
const handleCancel = (row) => {
  ElMessageBox.confirm(`确定要取消任务「${row.name}」吗？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelMassTask(row.id)
      ElMessage.success('取消成功')
      loadTaskList()
    } catch (error) {
      console.error(error)
    }
  })
}

// 删除任务
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除任务「${row.name}」吗？删除后不可恢复。`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'danger'
  }).then(async () => {
    try {
      await deleteMassTask(row.id)
      ElMessage.success('删除成功')
      loadTaskList()
    } catch (error) {
      console.error(error)
    }
  })
}

// 查看记录
const showRecords = (row) => {
  currentTask.value = row
  recordDrawerVisible.value = true
  recordKeyword.value = ''
  recordPageNum.value = 1
  loadRecordList()
}

const loadRecordList = async () => {
  if (!currentTask.value) return
  recordLoading.value = true
  try {
    const res = await pageMassTaskRecords(currentTask.value.id, {
      pageNum: recordPageNum.value,
      pageSize: recordPageSize.value,
      keyword: recordKeyword.value
    })
    const data = res.data || {}
    recordList.value = data.list || []
    recordTotal.value = data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    recordLoading.value = false
  }
}

onMounted(() => {
  loadAccountList()
  loadTaskList()
})
</script>

<style lang="scss" scoped>
.mass-message-container {
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
  background: rgba(21, 28, 44, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(91, 140, 255, 0.15);
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
  background: linear-gradient(135deg, #5b8cff, #3b82f6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f1f5f9;
  font-size: 24px;
}

.title-text h2 {
  font-size: 20px;
  font-weight: 700;
  color: #f1f5f9;
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.title-text p {
  font-size: 13px;
  color: #64748b;
}

.create-btn {
  background: linear-gradient(135deg, #5b8cff, #3b82f6);
  border: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.create-btn:hover {
  background: linear-gradient(135deg, #4a7dff, #2563eb);
  box-shadow: 0 6px 15px rgba(59, 130, 246, 0.25);
}

/* 任务列表 */
.task-section {
  border-radius: 20px;
  background: rgba(21, 28, 44, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(148, 163, 184, 0.1);
  padding: 24px;
}

.section-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #f1f5f9;
}

.section-header .el-icon {
  color: #5b8cff;
  font-size: 20px;
}

.search-input {
  width: 260px;
}

:deep(.sci-table) {
  background: transparent;
}

:deep(.sci-table .el-table__header-wrapper th) {
  background: rgba(148, 163, 184, 0.08) !important;
  color: #cbd5e1 !important;
  font-weight: 600;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1) !important;
}

:deep(.sci-table .el-table__body-wrapper td) {
  background: transparent !important;
  color: #e2e8f0 !important;
  border-bottom: 1px solid rgba(148, 163, 184, 0.05) !important;
  padding: 18px 0 !important;
  line-height: 1.6 !important;
}

:deep(.sci-table .el-table__row:hover td) {
  background: rgba(148, 163, 184, 0.05) !important;
}

:deep(.sci-table .el-table__empty-block) {
  background: transparent;
}

.task-name {
  font-weight: 600;
  color: #f1f5f9;
}

.account-name {
  color: #f1f5f9;
  font-weight: 600;
  font-size: 14px;
}

.account-id {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.text-mono {
  font-family: 'Courier New', monospace;
  color: #cbd5e1;
}

.progress-text {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e2e8f0;
}

.progress-text .success {
  color: #67c23a;
  font-weight: 600;
}

.progress-text .total {
  color: #94a3b8;
}

.progress-text .fail {
  color: #f56c6c;
  font-size: 12px;
}

.text-dim {
  color: #64748b;
}

:deep(.action-btn) {
  background: transparent !important;
  border-color: transparent !important;
  box-shadow: none !important;
  padding: 4px 6px !important;
  font-size: 13px;
  transition: all 0.25s ease;
}

:deep(.action-btn.is-hovering),
:deep(.action-btn:hover),
:deep(.action-btn:focus),
:deep(.action-btn:active) {
  background: transparent !important;
  border-color: transparent !important;
  box-shadow: none !important;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.action-record {
  color: #22d3ee !important;
}

.action-record:hover {
  color: #67e8f9 !important;
}

.action-pause {
  color: #f59e0b !important;
}

.action-pause:hover {
  color: #fbbf24 !important;
}

.action-cancel {
  color: #94a3b8 !important;
}

.action-cancel:hover {
  color: #cbd5e1 !important;
}

.action-delete {
  color: #f56c6c !important;
}

.action-delete:hover {
  color: #ff8585 !important;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
  color: #64748b;
}

.empty-state .el-icon {
  color: rgba(91, 140, 255, 0.25);
  margin-bottom: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 表单 */
.full-select,
.full-picker {
  width: 100%;
}

:deep(.create-task-dialog .el-dialog__body) {
  padding: 20px 24px 0 !important;
}

.create-task-form {
  max-height: 68vh;
  overflow-y: auto;
  padding-right: 6px;
}

.form-section {
  border-radius: 14px;
  background: rgba(148, 163, 184, 0.03);
  border: 1px solid rgba(148, 163, 184, 0.08);
  margin-bottom: 16px;
  overflow: hidden;
}

.form-section.compact .section-body {
  padding: 14px 16px;
}

.form-section.compact :deep(.el-form-item) {
  margin-bottom: 14px;
}

.form-section.compact :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: rgba(148, 163, 184, 0.05);
  color: #5b8cff;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.08);
}

.section-title .el-icon {
  font-size: 16px;
}

.section-count {
  margin-left: auto;
  font-size: 11px;
  color: #94a3b8;
  background: rgba(91, 140, 255, 0.12);
  padding: 1px 8px;
  border-radius: 8px;
}

.section-body {
  padding: 16px;
}

.form-tip-text {
  margin-left: 12px;
  color: #64748b;
  font-size: 12px;
}

.interval-row {
  display: flex;
  align-items: center;
}

:deep(.create-task-form .el-form-item:last-child) {
  margin-bottom: 0;
}

/* 发送对象区域 */
.recipients-area {
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.18);
  border: 1px dashed rgba(91, 140, 255, 0.2);
  padding: 14px;
  min-height: 90px;
  transition: all 0.3s ease;
}

.recipients-area.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.recipients-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #64748b;
}

.recipients-empty .el-icon {
  color: rgba(91, 140, 255, 0.2);
}

.recipients-empty p {
  font-size: 12px;
  margin: 0;
}

.recipients-summary {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 96px;
  overflow-y: auto;
  padding-right: 4px;
}

.recipient-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px 4px 4px !important;
  height: 28px !important;
  background: rgba(91, 140, 255, 0.12) !important;
  border-color: rgba(91, 140, 255, 0.2) !important;
  color: #f1f5f9 !important;
  font-size: 12px !important;
  border-radius: 14px !important;
}

.recipient-chip .el-tag__close {
  color: #64748b;
  margin-left: 4px;
}

.recipient-chip .el-tag__close:hover {
  background: rgba(245, 108, 108, 0.8);
  color: #f1f5f9;
}

.chip-avatar {
  flex-shrink: 0;
}

.chip-name {
  max-width: 100px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.more-chip {
  background: rgba(91, 140, 255, 0.2) !important;
  border-color: rgba(91, 140, 255, 0.3) !important;
  color: #5b8cff !important;
  cursor: pointer;
}

.summary-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 联系人选择弹窗 */
.contact-selector-dialog :deep(.el-dialog__body) {
  padding: 0 !important;
}

.contact-selector {
  display: flex;
  height: 520px;
}

.selector-sidebar {
  width: 110px;
  background: rgba(0, 0, 0, 0.2);
  border-right: 1px solid rgba(148, 163, 184, 0.08);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selector-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 10px;
  border-radius: 10px;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 13px;
}

.selector-tab:hover {
  background: rgba(148, 163, 184, 0.08);
  color: #f1f5f9;
}

.selector-tab.active {
  background: linear-gradient(135deg, #5b8cff, #5b8cff);
  color: #f1f5f9;
  box-shadow: 0 4px 9px rgba(91, 140, 255, 0.15);
}

.tab-count {
  margin-left: auto;
  font-size: 11px;
  background: #334155;
  padding: 1px 6px;
  border-radius: 8px;
}

.selector-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.selector-search-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.contact-search {
  width: 260px;
}

.selector-list {
  flex: 1;
  overflow-y: auto;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(148, 163, 184, 0.08);
  padding: 8px;
}

.selector-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  margin-bottom: 6px;
}

.selector-item:last-child {
  margin-bottom: 0;
}

.selector-item:hover {
  background: rgba(148, 163, 184, 0.06);
}

.selector-item.selected {
  background: rgba(91, 140, 255, 0.12);
  border: 1px solid rgba(91, 140, 255, 0.2);
}

.selector-checkbox {
  pointer-events: none;
}

.selector-item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.selector-item-name {
  color: #f1f5f9;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.selector-item-wxid {
  color: #64748b;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.selector-item-type {
  background: rgba(91, 140, 255, 0.12) !important;
  border-color: rgba(91, 140, 255, 0.2) !important;
  color: #5b8cff !important;
}

.selector-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.08);
}

.selector-tip {
  color: #64748b;
  font-size: 13px;
}

.contact-name {
  color: #f1f5f9;
  font-weight: 500;
}

.contact-wxid {
  color: #64748b;
  font-size: 12px;
}

/* 记录抽屉 */
.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}

.record-account {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 10px;
  background: rgba(91, 140, 255, 0.1);
  border: 1px solid rgba(91, 140, 255, 0.18);
  color: #5b8cff;
}

.record-account .el-icon {
  font-size: 16px;
}

.record-account-name {
  color: #f1f5f9;
  font-weight: 600;
  font-size: 14px;
}

.record-account-id {
  color: #64748b;
  font-size: 12px;
}

.record-stat {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 16px;
  border-radius: 10px;
  background: rgba(148, 163, 184, 0.08);
  border: 1px solid rgba(91, 140, 255, 0.15);
}

.stat-item.success {
  background: rgba(103, 194, 58, 0.1);
  border-color: rgba(103, 194, 58, 0.2);
}

.stat-item.fail {
  background: rgba(245, 108, 108, 0.1);
  border-color: rgba(245, 108, 108, 0.2);
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #f1f5f9;
}

.record-search {
  width: 260px;
}

.record-name {
  color: #f1f5f9;
  font-weight: 500;
}

.record-wxid {
  color: #64748b;
  font-size: 12px;
}

.fail-text {
  color: #f56c6c;
}

/* 消息内容区 */
.content-wrapper {
  width: 100%;
}

.content-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.material-tag {
  background: rgba(245, 158, 11, 0.15) !important;
  border-color: rgba(245, 158, 11, 0.3) !important;
  color: #f59e0b !important;
}

/* 消息内容预览 */
.content-preview-box {
  margin-top: 16px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(91, 140, 255, 0.12);
  overflow: hidden;
}

.preview-label {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: rgba(148, 163, 184, 0.06);
  color: #5b8cff;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
}

.preview-body {
  padding: 14px;
}

.preview-media {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-img {
  max-width: 100%;
  max-height: 260px;
  border-radius: 10px;
  overflow: hidden;
}

.preview-img :deep(.el-image__inner) {
  max-height: 260px;
  object-fit: contain;
}

.preview-video-player {
  width: 100%;
  max-height: 260px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.25);
}

.preview-meta {
  font-size: 12px;
  color: #64748b;
}

.preview-file {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.15);
}

.preview-file-icon {
  font-size: 40px;
  color: #5b8cff;
}

.preview-file-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-file-name {
  font-size: 14px;
  color: #f1f5f9;
  word-break: break-all;
}

.preview-text {
  padding: 12px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.15);
  color: #e2e8f0;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 240px;
  overflow-y: auto;
}

/* 素材选择弹窗 */
.material-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.material-search {
  width: 260px;
}

.material-type-select {
  width: 140px;
}

.material-content-preview {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #94a3b8;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
}

.material-content-preview:hover {
  color: #5b8cff;
}

.material-content-preview .preview-icon {
  font-size: 14px;
  color: inherit;
}

.type-tag {
  border: none !important;
  color: #f1f5f9 !important;
  font-weight: 500;
}

/* 素材悬停预览popover */
.material-preview-popover.el-popover {
  background: rgba(18, 22, 35, 0.98) !important;
  border: 1px solid rgba(91, 140, 255, 0.2) !important;
  border-radius: 12px !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.45) !important;
  padding: 14px !important;
}

.material-preview-popover.el-popover .el-popper__arrow::before {
  background: rgba(18, 22, 35, 0.98) !important;
  border-color: rgba(91, 140, 255, 0.2) !important;
}

.material-hover-preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hover-preview-title {
  font-size: 13px;
  font-weight: 600;
  color: #f1f5f9;
  word-break: break-all;
}

.hover-preview-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hover-preview-image {
  max-width: 100%;
  max-height: 180px;
  border-radius: 8px;
  overflow: hidden;
}

.hover-preview-image :deep(.el-image__inner) {
  max-height: 180px;
  object-fit: contain;
}

.hover-preview-video {
  width: 100%;
  max-height: 180px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.3);
}

.hover-preview-file {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.25);
}

.hover-file-icon {
  font-size: 32px;
  color: #5b8cff;
}

.hover-file-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hover-file-name {
  font-size: 12px;
  color: #f1f5f9;
  word-break: break-all;
}

.hover-preview-text {
  padding: 10px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.25);
  color: #e2e8f0;
  font-size: 12px;
  line-height: 1.5;
  word-break: break-all;
  max-height: 160px;
  overflow-y: auto;
}

/* 记录抽屉深色主题 */
:deep(.sci-drawer) {
  background: rgba(12, 16, 28, 0.98) !important;
}

:deep(.sci-drawer .el-drawer__header) {
  margin-bottom: 0;
  padding: 18px 24px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  color: #f1f5f9;
  font-size: 16px;
  font-weight: 600;
}

:deep(.sci-drawer .el-drawer__body) {
  padding: 20px 24px;
  background: transparent;
}

:deep(.sci-drawer .el-drawer__close-btn) {
  color: #64748b;
}

:deep(.sci-drawer .el-drawer__close-btn:hover) {
  color: #5b8cff;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .section-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .search-input,
  .contact-search,
  .record-search {
    width: 100%;
  }
  .record-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

<style>
/* 弹窗全局背景优化 - 科技暗色毛玻璃风格 */
.sci-dialog .el-dialog {
  background: linear-gradient(135deg, rgba(12, 20, 35, 0.98), rgba(21, 28, 44, 0.97)) !important;
  backdrop-filter: blur(24px) saturate(180%) !important;
  -webkit-backdrop-filter: blur(24px) saturate(180%) !important;
  border: 1px solid rgba(91, 140, 255, 0.2) !important;
  border-radius: 20px !important;
  box-shadow:
    0 0 0 1px rgba(91, 140, 255, 0.06) inset,
    0 25px 60px rgba(0, 0, 0, 0.55),
    0 0 40px rgba(91, 140, 255, 0.1) !important;
  overflow: hidden;
}

/* 弹窗顶部装饰光带 */
.sci-dialog .el-dialog::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(91, 140, 255, 0.6), rgba(139, 92, 246, 0.4), transparent);
  pointer-events: none;
}

.sci-dialog .el-dialog__header {
  margin-right: 0 !important;
  padding: 20px 26px !important;
  background: linear-gradient(90deg, rgba(91, 140, 255, 0.08), transparent) !important;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1) !important;
}

.sci-dialog .el-dialog__title {
  color: #f1f5f9 !important;
  font-weight: 700;
  letter-spacing: 1px;
  font-size: 17px;
  text-shadow: 0 0 12px rgba(91, 140, 255, 0.25);
}

.sci-dialog .el-dialog__headerbtn .el-dialog__close {
  color: #64748b !important;
  transition: all 0.25s ease;
}

.sci-dialog .el-dialog__headerbtn:hover .el-dialog__close {
  color: #5b8cff !important;
  transform: rotate(90deg);
}

.sci-dialog .el-dialog__body {
  color: #e2e8f0 !important;
  padding: 26px !important;
  background: radial-gradient(ellipse at top center, rgba(91, 140, 255, 0.04), transparent 60%);
}

.sci-dialog .el-dialog__footer {
  padding: 16px 26px !important;
  background: rgba(15, 23, 42, 0.5) !important;
  border-top: 1px solid rgba(148, 163, 184, 0.1) !important;
}

/* 新建/编辑弹窗表单区适配 */
.create-task-dialog .create-task-form {
  scrollbar-width: thin;
  scrollbar-color: rgba(91, 140, 255, 0.3) transparent;
}

.create-task-dialog .create-task-form::-webkit-scrollbar {
  width: 6px;
}

.create-task-dialog .create-task-form::-webkit-scrollbar-thumb {
  background: rgba(91, 140, 255, 0.3);
  border-radius: 3px;
}

/* 联系人选择弹窗内部深色适配 */
.contact-selector-dialog .el-dialog__body {
  background: linear-gradient(135deg, rgba(12, 20, 35, 0.98), rgba(21, 28, 44, 0.97)) !important;
}

/* 素材选择弹窗表格深色适配 */
.sci-dialog .el-table {
  background: transparent !important;
  --el-table-row-hover-bg-color: rgba(91, 140, 255, 0.08) !important;
}

.sci-dialog .el-table tr,
.sci-dialog .el-table th.el-table__cell {
  background: transparent !important;
}

.sci-dialog .el-table td.el-table__cell {
  background: transparent !important;
  border-bottom: 1px solid rgba(148, 163, 184, 0.08) !important;
}

.sci-dialog .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
  background: rgba(91, 140, 255, 0.08) !important;
}

.sci-dialog .el-table__empty-block {
  background: transparent !important;
}

/* 记录抽屉深色主题优化 */
.sci-drawer .el-drawer {
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.98), rgba(12, 20, 35, 0.98)) !important;
  backdrop-filter: blur(24px) saturate(180%) !important;
  -webkit-backdrop-filter: blur(24px) saturate(180%) !important;
  border-left: 1px solid rgba(91, 140, 255, 0.18);
  box-shadow:
    -8px 0 40px rgba(0, 0, 0, 0.55),
    0 0 40px rgba(91, 140, 255, 0.08);
}

.sci-drawer .el-drawer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 1px;
  bottom: 0;
  background: linear-gradient(180deg, transparent, rgba(91, 140, 255, 0.5), rgba(139, 92, 246, 0.3), transparent);
  pointer-events: none;
}

.sci-drawer .el-drawer__header {
  margin-bottom: 0;
  padding: 18px 24px;
  background: linear-gradient(90deg, rgba(91, 140, 255, 0.08), transparent);
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  color: #f1f5f9;
  font-size: 16px;
  font-weight: 600;
  text-shadow: 0 0 12px rgba(91, 140, 255, 0.25);
}

.sci-drawer .el-drawer__body {
  padding: 20px 24px;
  background: transparent;
}

.sci-drawer .el-drawer__close-btn {
  color: #64748b;
  transition: all 0.25s ease;
}

.sci-drawer .el-drawer__close-btn:hover {
  color: #5b8cff;
  transform: rotate(90deg);
}
</style>
