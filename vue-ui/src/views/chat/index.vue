<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 顶部账号选择 -->
      <div class="chat-header">
        <div class="account-selector">
          <el-select v-model="currentAccountId" placeholder="选择微信账号" class="account-select" @change="handleAccountChange">
            <el-option
              v-for="account in accountList"
              :key="account.id"
              :label="account.nickName || account.alias || account.wxid"
              :value="account.id"
            >
              <div class="account-option">
                <el-avatar :size="24" :src="account.headImgUrl" class="account-avatar">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <span>{{ account.nickName || account.alias || account.wxid }}</span>
                <span class="account-status" :class="account.status === 1 ? 'online' : 'offline'">
                  {{ account.status === 1 ? '在线' : '离线' }}
                </span>
              </div>
            </el-option>
          </el-select>
        </div>
        <div class="ws-status" :class="wsConnected ? 'connected' : 'disconnected'">
          <span class="ws-dot"></span>
          <span>{{ wsConnected ? '实时连接中' : '未连接' }}</span>
        </div>
      </div>

      <div class="chat-body">
        <!-- 左侧联系人列表 -->
        <div class="contact-sidebar">
          <div class="contact-search">
            <el-input
              v-model="contactKeyword"
              placeholder="搜索联系人"
              prefix-icon="Search"
              clearable
            />
          </div>
          <div class="contact-list" v-if="filteredContacts.length">
            <div
              v-for="contact in filteredContacts"
              :key="contact.contactWxid"
              class="contact-item"
              :class="{ active: currentContact && currentContact.contactWxid === contact.contactWxid }"
              @click="selectContact(contact)"
            >
              <div class="contact-avatar">
                <el-avatar :size="44" :src="contact.smallHeadImgUrl || contact.bigHeadImgUrl">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <span v-if="contact.type === 2" class="group-badge">群</span>
              </div>
              <div class="contact-info">
                <div class="contact-name-row">
                  <span class="contact-name">{{ contact.nickName || contact.remark || contact.alias || contact.contactWxid }}</span>
                  <span class="contact-time">{{ formatTime(contact.lastMsgTime) }}</span>
                </div>
                <div class="contact-preview-row">
                  <span class="contact-preview">{{ contact.lastMsgContent || '暂无消息' }}</span>
                  <span v-if="contact.unreadCount" class="unread-badge">{{ contact.unreadCount }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="contact-empty">
            <el-empty description="暂无联系人" :image-size="80" />
          </div>
        </div>

        <!-- 右侧聊天区域 -->
        <div class="chat-main">
          <div v-if="currentContact" class="chat-main-inner">
            <!-- 聊天对象信息 -->
            <div class="chat-title">
              <el-avatar :size="36" :src="currentContact.smallHeadImgUrl || currentContact.bigHeadImgUrl">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="chat-title-name">{{ currentContact.nickName || currentContact.remark || currentContact.alias || currentContact.contactWxid }}</span>
            </div>

            <!-- 消息列表 -->
            <div ref="messageListRef" class="message-list">
              <div
                v-for="msg in currentMessages"
                :key="msg.id || msg.tempId"
                class="message-item"
                :class="{ self: msg.isSelf }"
              >
                <el-avatar :size="38" :src="msg.isSelf ? currentAccount.headImgUrl : currentContact.smallHeadImgUrl || currentContact.bigHeadImgUrl">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <div class="message-content">
                  <div class="message-bubble" :class="getMessageBubbleClass(msg.msgType)">
                    <!-- 文本 -->
                    <div v-if="msg.msgType === 1 || msg.msgType == null" class="message-text" v-html="formatMessageContent(msg.content)"></div>
                    <!-- 图片 -->
                    <div v-else-if="msg.msgType === 3" class="message-image" @click="handleImageClick(msg)">
                      <el-image
                        :src="getImageSrc(msg)"
                        :preview-src-list="[getImageSrc(msg)]"
                        fit="cover"
                        :class="{ 'image-downloading': imageLoadingMap.get(msg.id) }"
                      >
                        <template #error>
                          <div class="image-placeholder">
                            <el-icon><Picture /></el-icon>
                            <span>点击下载图片</span>
                          </div>
                        </template>
                        <template #placeholder>
                          <div class="image-placeholder">
                            <el-icon class="is-loading"><Loading /></el-icon>
                            <span>加载中...</span>
                          </div>
                        </template>
                      </el-image>
                      <div v-if="imageLoadingMap.get(msg.id)" class="image-overlay">
                        <el-icon class="is-loading"><Loading /></el-icon>
                      </div>
                    </div>
                    <!-- 文件 -->
                    <div v-else-if="msg.msgType === 6" class="message-file">
                      <el-icon><Document /></el-icon>
                      <a :href="msg.content" target="_blank" download>{{ msg.fileName || '点击下载文件' }}</a>
                    </div>
                    <!-- 视频 -->
                    <div v-else-if="msg.msgType === 43" class="message-video">
                      <video :src="msg.content" controls preload="metadata" />
                    </div>
                    <!-- 兜底 -->
                    <div v-else class="message-text" v-html="formatMessageContent(msg.content)"></div>
                  </div>
                  <div class="message-time">{{ formatFullTime(msg.msgTime || msg.createTime) }}</div>
                </div>
              </div>
              <div v-if="!currentMessages.length" class="message-empty">
                <span>开始和 {{ currentContact.nickName || currentContact.contactWxid }} 聊天吧</span>
              </div>
            </div>

            <!-- 输入区域 -->
            <div class="chat-input-area">
              <div class="input-toolbar">
                <el-popover
                  v-model:visible="emojiPanelVisible"
                  placement="top-start"
                  :width="300"
                  trigger="click"
                  popper-class="emoji-popover"
                >
                  <template #reference>
                    <el-button link class="tool-btn emoji-btn" title="表情">
                      <span class="emoji-icon">😊</span>
                    </el-button>
                  </template>
                  <div class="emoji-panel">
                    <span
                      v-for="emoji in commonEmojis"
                      :key="emoji"
                      class="emoji-item"
                      @click="insertEmoji(emoji)"
                    >{{ emoji }}</span>
                  </div>
                </el-popover>

                <el-button link class="tool-btn" title="图片" :loading="uploading && currentFileType === 'image'" @click="triggerFileUpload('image')">
                  <el-icon><Picture /></el-icon>
                </el-button>

                <el-button link class="tool-btn" title="文件" :loading="uploading && currentFileType === 'file'" @click="triggerFileUpload('file')">
                  <el-icon><Folder /></el-icon>
                </el-button>

                <input
                  ref="fileInputRef"
                  type="file"
                  style="display: none"
                  @change="handleFileSelect"
                />
              </div>
              <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="4"
                resize="none"
                placeholder="请输入消息..."
                class="chat-textarea"
                @keydown.enter.prevent="sendMessage"
              />
              <div class="input-footer">
                <span class="input-tip">按 Enter 发送，Shift + Enter 换行</span>
                <el-button type="primary" :disabled="!inputMessage.trim() || sending" :loading="sending" @click="sendMessage">
                  发送
                </el-button>
              </div>
            </div>
          </div>
          <div v-else class="chat-placeholder">
            <div class="placeholder-content">
              <el-icon size="64" color="rgba(0, 212, 255, 0.2)"><ChatLineRound /></el-icon>
              <p>选择一个联系人开始聊天</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { listWxAccounts, listContacts, listMessages, sendTextMessage, uploadFile, sendImageMessage, sendFileMessage, sendVideoMessage, downloadImage } from '@/api/chat'

const userStore = useUserStore()

// 账号相关
const accountList = ref([])
const currentAccountId = ref(null)
const currentAccount = computed(() => accountList.value.find(a => a.id === currentAccountId.value) || null)

// 联系人相关
const contactList = ref([])
const contactKeyword = ref('')
const currentContact = ref(null)
const messageListRef = ref(null)

// 消息相关
const messageMap = ref(new Map()) // key: contactWxid, value: message[]
const currentMessages = computed(() => {
  if (!currentContact.value) return []
  return messageMap.value.get(currentContact.value.contactWxid) || []
})

// 输入相关
const inputMessage = ref('')
const sending = ref(false)
const emojiPanelVisible = ref(false)
const uploading = ref(false)
const fileInputRef = ref(null)
const currentFileType = ref('file') // file / image / video

// WebSocket 相关
const ws = ref(null)
const wsConnected = ref(false)

// 图片下载相关
const imageLoadingMap = ref(new Map())
const imageCacheMap = ref(new Map())

const commonEmojis = ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣', '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽', '👾', '🤖', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾', '👍', '👎', '👏', '🙌', '👐', '🤲', '🤝', '🤜', '🤛', '✊', '👊', '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '🔥', '🎉', '✨', '🎁', '🌹', '💐', '🌸', '🌺', '🌻', '🌼', '🌷', '🌿', '☘️', '🍀', '🍎', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🫐', '🍈', '🍒', '🍑', '🍍', '🥝', '🍅', '🍆', '🥑', '🥦', '🥬', '🥒', '🌶️', '🫑', '🌽', '🥕', '🫒', '🧄', '🧅', '🥔', '🍠', '🥐', '🥯', '🍞', '🥖', '🥨', '🧀', '🥚', '🍳', '🧈', '🥞', '🧇', '🥓', '🥩', '🍗', '🍖', '🦴', '🌭', '🍔', '🍟', '🍕', '🫓', '🥪', '🥙', '🧆', '🌮', '🌯', '🫔', '🥗', '🥘', '🫕', '🥫', '🍝', '🍜', '🍲', '🍛', '🍣', '🍱', '🥟', '🦪', '🍤', '🍙', '🍚', '🍘', '🍥', '🥠', '🥮', '🍢', '🍡', '🍧', '🍨', '🍦', '🥧', '🧁', '🍰', '🎂', '🍮', '🍭', '🍬', '🍫', '🍿', '🍩', '🍪', '🌰', '🥜', '🍯', '🥛', '🍼', '☕', '🍵', '🧃', '🥤', '🧋', '🍶', '🍺', '🍻', '🥂', '🍷', '🥃', '🍸', '🍹', '🧉', '🍾', '🧊', '🥄', '🍴', '🍽️', '🥣', '🥡', '🥢', '🧂']

// 过滤联系人
const filteredContacts = computed(() => {
  if (!contactKeyword.value.trim()) return contactList.value
  const kw = contactKeyword.value.trim().toLowerCase()
  return contactList.value.filter(c => {
    const name = (c.nickName || c.remark || c.alias || c.contactWxid || '').toLowerCase()
    return name.includes(kw)
  })
})

// 初始化
onMounted(() => {
  loadAccounts()
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

// 监听当前消息变化，滚动到底部
watch(currentMessages, () => {
  scrollToBottom()
}, { deep: true })

// 加载微信账号列表
const loadAccounts = async () => {
  try {
    const res = await listWxAccounts()
    accountList.value = res.data || []
    if (accountList.value.length && !currentAccountId.value) {
      currentAccountId.value = accountList.value[0].id
      await handleAccountChange(currentAccountId.value)
    }
  } catch (error) {
    console.error('加载账号列表失败', error)
  }
}

// 账号切换
const handleAccountChange = async (accountId) => {
  currentAccountId.value = accountId
  currentContact.value = null
  messageMap.value = new Map()
  await loadContacts()
}

// 加载联系人列表
const loadContacts = async () => {
  if (!currentAccount.value) return
  try {
    const res = await listContacts(currentAccount.value.wxid)
    const list = (res.data || []).map(item => ({
      ...item,
      lastMsgContent: '',
      lastMsgTime: null,
      unreadCount: 0
    }))
    contactList.value = list
    sortContactList()
  } catch (error) {
    console.error('加载联系人失败', error)
  }
}

// 选择联系人
const selectContact = async (contact) => {
  currentContact.value = contact
  contact.unreadCount = 0
  if (!messageMap.value.has(contact.contactWxid)) {
    await loadMessages(contact)
  }
}

// 加载历史消息
const loadMessages = async (contact) => {
  if (!currentAccount.value) return
  try {
    const res = await listMessages({
      appId: currentAccount.value.appId,
      wxid: currentAccount.value.wxid,
      contactWxid: contact.contactWxid
    })
    const list = (res.data || []).map(item => ({
      ...item,
      isSelf: item.fromWxid === currentAccount.value.wxid,
      content: item.content || '',
      msgType: item.msgType ?? 1,
      fileName: item.fileName || ''
    }))
    messageMap.value.set(contact.contactWxid, list)
    updateContactLastMsg(contact.contactWxid, list)
    scrollToBottom()
  } catch (error) {
    console.error('加载历史消息失败', error)
  }
}

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || !currentAccount.value || !currentContact.value || sending.value) return

  sending.value = true
  const contactWxid = currentContact.value.contactWxid
  inputMessage.value = ''

  try {
    const res = await sendTextMessage({
      appId: currentAccount.value.appId,
      toWxid: contactWxid,
      content
    })
    const message = res.data || {}
    // 接口返回后把真实消息加入列表
    pushMessage(contactWxid, {
      id: message.id,
      fromWxid: message.fromWxid,
      toWxid: message.toWxid,
      content: message.content,
      msgTime: message.msgTime,
      msgType: message.msgType ?? 1,
      isSelf: message.fromWxid === currentAccount.value.wxid
    })
  } catch (error) {
    ElMessage.error('发送失败')
    console.error('发送消息失败', error)
  } finally {
    sending.value = false
  }
}

// 插入表情
const insertEmoji = (emoji) => {
  inputMessage.value += emoji
  emojiPanelVisible.value = false
}

// 触发文件上传
const triggerFileUpload = (type) => {
  if (!currentAccount.value || !currentContact.value || uploading.value) return
  currentFileType.value = type
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

// 选择文件后上传并发送
const handleFileSelect = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  uploading.value = true
  const contactWxid = currentContact.value.contactWxid
  const type = currentFileType.value

  try {
    // 1. 上传到对象存储
    const uploadRes = await uploadFile(file, `wx/${type}`)
    const url = uploadRes.data?.url
    if (!url) {
      ElMessage.error('上传失败')
      return
    }

    // 2. 根据类型发送消息
    let res
    const baseData = {
      appId: currentAccount.value.appId,
      toWxid: contactWxid
    }
    if (type === 'image') {
      res = await sendImageMessage({ ...baseData, imgUrl: url })
    } else if (type === 'video') {
      res = await sendVideoMessage({ ...baseData, videoUrl: url, videoDuration: 0 })
    } else {
      res = await sendFileMessage({ ...baseData, fileName: file.name, fileUrl: url })
    }

    const message = res.data || {}
    pushMessage(contactWxid, {
      id: message.id,
      fromWxid: message.fromWxid,
      toWxid: message.toWxid,
      content: message.content,
      msgTime: message.msgTime,
      msgType: message.msgType ?? getMsgTypeByFileType(type),
      fileName: file.name,
      isSelf: message.fromWxid === currentAccount.value.wxid
    })
  } catch (error) {
    ElMessage.error('发送失败')
    console.error('发送文件消息失败', error)
  } finally {
    uploading.value = false
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

// 根据文件类型获取消息类型
const getMsgTypeByFileType = (type) => {
  if (type === 'image') return 3
  if (type === 'video') return 43
  return 6
}

// 判断内容是否为图片 URL（http/https/data）
const isImageUrl = (content) => {
  return content && (content.startsWith('http://') || content.startsWith('https://') || content.startsWith('data:'))
}

// 解析图片消息的 content JSON（新格式：{"xml":"...","thumb":"..."}）
const parseImageContent = (content) => {
  if (!content) return null
  try {
    const str = content.trim()
    if (str.startsWith('{')) {
      return JSON.parse(str)
    }
  } catch (error) {
    // 不是 JSON，按旧格式处理
  }
  return null
}

// 从 content 中提取缩略图（新格式 thumb 字段，或发送时的图片 URL）
const getThumbFromContent = (content) => {
  const parsed = parseImageContent(content)
  if (parsed && parsed.thumb) return parsed.thumb
  if (isImageUrl(content)) return content
  return null
}

// 从 rawData 中解析缩略图 base64（兼容旧格式完整回调）
const getThumbFromRawData = (msg) => {
  if (!msg.rawData) return null
  try {
    const raw = typeof msg.rawData === 'string' ? JSON.parse(msg.rawData) : msg.rawData
    const buffer = raw?.Data?.ImgBuf?.buffer
    if (buffer) {
      return `data:image/jpeg;base64,${buffer}`
    }
  } catch (error) {
    console.error('解析图片 rawData 失败', error)
  }
  return null
}

// 获取图片消息显示源（优先已下载原图，其次缩略图）
const getImageSrc = (msg) => {
  if (!msg) return null
  const cached = imageCacheMap.value.get(msg.id)
  if (cached) return cached
  const thumb = getThumbFromContent(msg.content) || getThumbFromRawData(msg)
  if (thumb) return thumb
  return null
}

// 点击图片下载原图
const handleImageClick = async (msg) => {
  if (!msg || !msg.id) return
  // 发送的图片 URL 直接由 el-image 预览，无需下载
  const parsed = parseImageContent(msg.content)
  if (isImageUrl(msg.content) && !parsed?.xml && !getThumbFromRawData(msg)) return
  if (imageLoadingMap.value.get(msg.id)) return

  imageLoadingMap.value.set(msg.id, true)
  try {
    const res = await downloadImage({ messageId: msg.id, type: 1 })
    const data = res.data || {}
    const src = data.base64 || data.url
    if (src) {
      imageCacheMap.value.set(msg.id, src)
    } else {
      ElMessage.warning('图片下载失败，未返回有效数据')
    }
  } catch (error) {
    ElMessage.error('下载图片失败')
    console.error('下载图片失败', error)
  } finally {
    imageLoadingMap.value.set(msg.id, false)
  }
}

// WebSocket 连接
const connectWebSocket = () => {
  if (!userStore.token) return
  disconnectWebSocket()
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const url = `${protocol}//${host}/ws/chat?token=${userStore.token}`
  const socket = new WebSocket(url)

  socket.onopen = () => {
    wsConnected.value = true
    console.log('WebSocket 连接成功')
  }

  socket.onmessage = (event) => {
    try {
      const payload = JSON.parse(event.data)
      console.log('WebSocket 收到消息：', payload)
      handleWebSocketMessage(payload)
    } catch (error) {
      console.error('解析 WebSocket 消息失败', error)
    }
  }

  socket.onclose = () => {
    wsConnected.value = false
    console.log('WebSocket 连接关闭')
    // 3 秒后自动重连
    setTimeout(() => connectWebSocket(), 3000)
  }

  socket.onerror = (error) => {
    console.error('WebSocket 错误', error)
  }

  ws.value = socket
}

// 断开 WebSocket
const disconnectWebSocket = () => {
  if (ws.value) {
    ws.value.onclose = null
    ws.value.close()
    ws.value = null
  }
}

// 处理 WebSocket 推送的消息
const handleWebSocketMessage = (payload) => {
  if (payload.TypeName !== 'AddMsg') return
  const appId = payload.Appid
  const wxid = payload.Wxid
  const data = payload.Data || {}
  const fromWxid = data.FromUserName?.string
  const toWxid = data.ToUserName?.string
  const content = data.Content?.string || ''
  const msgType = data.MsgType
  const msgTime = data.CreateTime
  const msgId = data.MsgId || data.NewMsgId
  const localMessageId = data.LocalMessageId || msgId

  // 只处理当前选中账号的消息
  if (!currentAccount.value || currentAccount.value.appId !== appId) return

  const accountWxid = currentAccount.value.wxid
  // 回调里的 Wxid 就是当前登录账号的 wxid，如果不是当前选中账号则忽略
  if (wxid !== accountWxid) return
  // 如果对方是当前登录账号，则联系人是发送方；否则联系人是接收方（自己发出去的消息）
  const contactWxid = toWxid === accountWxid ? fromWxid : toWxid
  if (!contactWxid || contactWxid === accountWxid) return

  const previewText = getMsgTypePreviewText(msgType, content)

  // 确保联系人存在（不存在则创建临时联系人）
  let contact = contactList.value.find(c => c.contactWxid === contactWxid)
  if (!contact) {
    contact = {
      contactWxid,
      userName: contactWxid,
      nickName: contactWxid,
      alias: null,
      remark: null,
      smallHeadImgUrl: '',
      bigHeadImgUrl: '',
      type: 1,
      lastMsgContent: previewText,
      lastMsgTime: msgTime,
      unreadCount: 0
    }
    contactList.value.push(contact)
  }

  // 更新联系人最后消息预览
  contact.lastMsgContent = previewText
  contact.lastMsgTime = msgTime
  // 重新排序，把有最新消息的联系人置顶
  sortContactList()

  // 是当前会话则加入消息列表并滚动到底部
  if (currentContact.value && currentContact.value.contactWxid === contactWxid) {
    pushMessage(contactWxid, {
      id: localMessageId,
      fromWxid,
      toWxid,
      content,
      msgTime,
      msgType,
      isSelf: fromWxid === accountWxid,
      rawData: msgType === 3 ? JSON.stringify(payload) : null
    })
  } else {
    // 非当前会话增加未读数
    contact.unreadCount = (contact.unreadCount || 0) + 1
  }
}

// 向指定会话添加消息
const pushMessage = (contactWxid, msg) => {
  if (!messageMap.value.has(contactWxid)) {
    messageMap.value.set(contactWxid, [])
  }
  const list = messageMap.value.get(contactWxid)
  // 避免重复（根据 id 或 tempId）
  const exists = list.some(m => (m.id && m.id === msg.id) || (m.tempId && m.tempId === msg.tempId))
  if (!exists) {
    list.push(msg)
    messageMap.value.set(contactWxid, [...list])
    const preview = getMsgTypePreviewText(msg.msgType, msg.content)
    updateContactPreview(contactWxid, preview, msg.msgTime)
    sortContactList()
    scrollToBottom()
  }
}

// 按最后消息时间倒序排列联系人列表
const sortContactList = () => {
  contactList.value.sort((a, b) => {
    const timeA = a.lastMsgTime || 0
    const timeB = b.lastMsgTime || 0
    return timeB - timeA
  })
}

// 更新联系人最后消息预览
const updateContactPreview = (contactWxid, content, time) => {
  const contact = contactList.value.find(c => c.contactWxid === contactWxid)
  if (contact) {
    contact.lastMsgContent = content
    contact.lastMsgTime = time
  }
}

// 根据消息列表更新联系人最后消息
const updateContactLastMsg = (contactWxid, list) => {
  if (!list.length) return
  const last = list[list.length - 1]
  const preview = getMsgTypePreviewText(last.msgType, last.content)
  updateContactPreview(contactWxid, preview, last.msgTime)
  sortContactList()
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const el = messageListRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

// 根据消息类型获取预览文案
const getMsgTypePreviewText = (msgType, content) => {
  if (msgType === 3) return '[图片]'
  if (msgType === 43) return '[视频]'
  if (msgType === 6) return '[文件]'
  return content || ''
}

// 根据消息类型获取气泡样式
const getMessageBubbleClass = (msgType) => {
  if (msgType === 3) return 'bubble-image'
  if (msgType === 6) return 'bubble-file'
  if (msgType === 43) return 'bubble-video'
  return ''
}

// 格式化消息内容（换行、表情等）
const formatMessageContent = (content) => {
  if (!content) return ''
  return content.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
}

// 格式化时间（列表）
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp * 1000)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 格式化完整时间
const formatFullTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp * 1000)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style lang="scss" scoped>
.chat-page {
  width: 100%;
  height: calc(100vh - 100px);
  display: flex;
}

.chat-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(10, 22, 45, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
}

/* 顶部栏 */
.chat-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: rgba(8, 18, 38, 0.7);
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.account-select {
  width: 280px;
}

.account-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.account-avatar {
  flex-shrink: 0;
}

.account-status {
  margin-left: auto;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.account-status.online {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.15);
}

.account-status.offline {
  color: #909399;
  background: rgba(144, 147, 153, 0.15);
}

.ws-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.ws-status.connected {
  color: #67c23a;
}

.ws-status.disconnected {
  color: #f56c6c;
}

.ws-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 8px currentColor;
}

/* 主体 */
.chat-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧联系人 */
.contact-sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  background: rgba(8, 18, 38, 0.4);
  border-right: 1px solid rgba(0, 212, 255, 0.08);
}

.contact-search {
  padding: 12px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.06);
}

.contact-list {
  flex: 1;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.25s ease;
  border-bottom: 1px solid rgba(0, 212, 255, 0.04);
}

.contact-item:hover {
  background: rgba(0, 212, 255, 0.06);
}

.contact-item.active {
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.12), rgba(0, 212, 255, 0.02));
  border-left: 3px solid #00d4ff;
}

.contact-avatar {
  position: relative;
  flex-shrink: 0;
}

.group-badge {
  position: absolute;
  right: -4px;
  bottom: -4px;
  font-size: 10px;
  color: #fff;
  background: linear-gradient(135deg, #00d4ff, #0099ff);
  padding: 1px 4px;
  border-radius: 6px;
}

.contact-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.contact-name-row,
.contact-preview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.contact-name {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.35);
  flex-shrink: 0;
}

.contact-preview {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.unread-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: linear-gradient(135deg, #f56c6c, #ff8585);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.contact-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 右侧聊天 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(6, 14, 30, 0.4);
  min-width: 0;
}

.chat-main-inner {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-title {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  background: rgba(8, 18, 38, 0.5);
  border-bottom: 1px solid rgba(0, 212, 255, 0.08);
}

.chat-title-name {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-empty {
  margin: auto;
  color: rgba(255, 255, 255, 0.3);
  font-size: 14px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-item.self .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.message-item.self .message-bubble {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.18), rgba(0, 153, 255, 0.12));
  border: 1px solid rgba(0, 212, 255, 0.25);
  color: #fff;
}

.message-text {
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.3);
}

/* 图片消息 */
.message-bubble.bubble-image {
  padding: 6px;
  max-width: 240px;
}

.message-image {
  border-radius: 10px;
  overflow: hidden;
}

.message-image .el-image {
  width: 100%;
  max-height: 240px;
  display: block;
  cursor: pointer;
}

.message-image {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  min-width: 120px;
  min-height: 80px;
}

.image-placeholder {
  width: 120px;
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
}

.image-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  font-size: 24px;
  border-radius: 10px;
}

.image-downloading {
  opacity: 0.6;
}

/* 文件消息 */
.message-file {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 160px;
}

.message-file a {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: underline;
  word-break: break-all;
}

.message-file a:hover {
  color: #00d4ff;
}

/* 视频消息 */
.message-bubble.bubble-video {
  padding: 6px;
  max-width: 280px;
}

.message-video video {
  width: 100%;
  max-height: 220px;
  border-radius: 10px;
  display: block;
}

/* 输入区域 */
.chat-input-area {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  background: rgba(8, 18, 38, 0.5);
  border-top: 1px solid rgba(0, 212, 255, 0.08);
}

.input-toolbar {
  height: 44px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 8px;
}

.tool-btn {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.5) !important;
}

.tool-btn:hover {
  color: #00d4ff !important;
}

.emoji-btn {
  padding: 0 6px !important;
}

.emoji-icon {
  font-size: 20px;
  line-height: 1;
  filter: grayscale(30%);
  transition: all 0.2s ease;
}

.emoji-btn:hover .emoji-icon {
  filter: grayscale(0%);
  transform: scale(1.15);
}

.chat-textarea {
  flex: 1;
  padding: 0 16px;
}

:deep(.chat-textarea .el-textarea__inner) {
  background: rgba(255, 255, 255, 0.04) !important;
  border: 1px solid rgba(0, 212, 255, 0.1) !important;
  color: rgba(255, 255, 255, 0.9) !important;
  border-radius: 10px;
  box-shadow: none !important;
}

:deep(.chat-textarea .el-textarea__inner:focus) {
  border-color: rgba(0, 212, 255, 0.4) !important;
}

.input-footer {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.input-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  text-align: center;
  color: rgba(255, 255, 255, 0.3);
}

.placeholder-content p {
  margin-top: 16px;
  font-size: 14px;
}

/* 表情面板 */
.emoji-panel {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 6px;
  padding: 8px;
  max-height: 220px;
  overflow-y: auto;
}

.emoji-item {
  font-size: 22px;
  cursor: pointer;
  text-align: center;
  padding: 4px;
  border-radius: 6px;
  transition: background 0.2s;
}

.emoji-item:hover {
  background: rgba(0, 212, 255, 0.12);
}
</style>
