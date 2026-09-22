<template>
  <div>
    <div class="page-title">在线咨询</div>
    <el-card class="chat-card">
      <div class="chat-layout">
        <!-- 左侧会话列表 -->
        <div class="chat-sidebar">
          <div class="sidebar-search">
            <el-input v-model="searchKey" placeholder="搜索用户" clearable prefix-icon="Search" size="small" />
          </div>
          <div class="conversation-list">
            <div v-if="!filteredConversations.length" class="no-conv">暂无咨询消息</div>
            <div v-for="c in filteredConversations" :key="c.targetId + '_' + c.targetType" class="conv-swipe-wrapper">
              <div class="conv-item" :class="{ active: currentTarget?.targetId === c.targetId }"
                   :style="{ transform: 'translateX(' + (c._swipeX || 0) + 'px)' }"
                   @click="selectConversation(c)"
                   @touchstart="onSwipeStart($event, c)" @touchmove="onSwipeMove($event, c)" @touchend="onSwipeEnd(c)"
                   @mousedown="onSwipeStart($event, c)" @mousemove="onSwipeMove($event, c)" @mouseup="onSwipeEnd(c)" @mouseleave="onSwipeEnd(c)">
                <div class="conv-avatar">
                  <img v-if="c.targetAvatar" :src="c.targetAvatar" class="conv-avatar-img" />
                  <el-icon v-else :size="18"><User /></el-icon>
                </div>
                <div class="conv-info">
                  <div class="conv-name">{{ c.targetName || '用户#' + c.targetId }}</div>
                  <div class="conv-last">{{ c.lastMessage }}</div>
                </div>
                <div class="conv-meta">
                  <span class="conv-time">{{ formatTime(c.lastTime) }}</span>
                  <el-badge v-if="c.unreadCount > 0" :value="c.unreadCount" class="conv-badge-wrap" />
                </div>
              </div>
              <div class="conv-delete-btn" @click="deleteConversation(c)">删除</div>
            </div>
          </div>
        </div>

        <!-- 右侧聊天窗口 -->
        <div class="chat-main">
          <template v-if="currentTarget">
            <div class="chat-header">
              <div class="chat-header-info">
                <img v-if="currentTarget.targetAvatar" :src="currentTarget.targetAvatar" class="chat-header-avatar" />
                <el-icon v-else :size="20"><User /></el-icon>
                <span>{{ currentTarget.targetName || '用户#' + currentTarget.targetId }}</span>
              </div>
            </div>
            <div class="chat-messages" ref="messagesRef">
              <div v-for="msg in messages" :key="msg.id || msg.createTime"
                   class="msg-item" :class="{ mine: msg.senderId === myMerchantId && msg.senderType === 2 }">
                <div v-if="msg.recalled" class="recall-tip">{{ msg.senderId === myMerchantId && msg.senderType === 2 ? '你' : '对方' }}撤回了一条消息</div>
                <div v-else class="msg-bubble" @contextmenu.prevent="onMsgRightClick($event, msg)">
                  <div class="msg-text">{{ msg.content }}</div>
                  <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
                </div>
              </div>
            </div>
            <!-- 右键菜单 -->
            <div v-if="contextMenu.visible" class="ctx-menu" :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }">
              <div class="ctx-menu-item" @click="recallMessage">撤回</div>
            </div>
            <div class="chat-input-area">
              <textarea v-model="inputText" class="merchant-chat-input" placeholder="输入回复..." @keydown.enter.exact.prevent="sendMessage" rows="2"></textarea>
              <el-button type="primary" @click="sendMessage" :disabled="!inputText.trim()">发送</el-button>
            </div>
          </template>
          <template v-else>
            <div class="chat-empty">
              <el-icon :size="48" color="#ddd"><ChatDotRound /></el-icon>
              <p>选择左侧对话开始回复</p>
            </div>
          </template>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import { User, ChatDotRound } from '@element-plus/icons-vue'

const conversations = ref([])
const currentTarget = ref(null)
const messages = ref([])
const inputText = ref('')
const messagesRef = ref(null)
const searchKey = ref('')
const myMerchantId = ref(null)
let ws = null
const contextMenu = reactive({ visible: false, x: 0, y: 0, msg: null })

function onMsgRightClick(e, msg) {
  if (msg.senderId !== myMerchantId.value || msg.senderType !== 2) return
  const sendTime = new Date(msg.createTime?.replace?.(' ', 'T') || msg.createTime)
  if (Date.now() - sendTime.getTime() > 2 * 60 * 1000) return
  contextMenu.visible = true
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.msg = msg
}
async function recallMessage() {
  if (!contextMenu.msg) return
  try {
    await request.delete(`/api/chat/recall/${contextMenu.msg.id}`)
    const idx = messages.value.findIndex(m => m.id === contextMenu.msg.id)
    if (idx !== -1) messages.value[idx].recalled = true
  } catch (e) {
    ElMessage.warning(e?.message || '撤回失败')
  }
  contextMenu.visible = false
}
function hideContextMenu() { contextMenu.visible = false }

// 左滑删除手势
let swipeStartX = 0, swiping = false
function onSwipeStart(e, c) {
  conversations.value.forEach(item => { if (item !== c && item._swipeX) item._swipeX = 0 })
  swipeStartX = e.touches ? e.touches[0].clientX : e.clientX
  swiping = true
}
function onSwipeMove(e, c) {
  if (!swiping) return
  const x = e.touches ? e.touches[0].clientX : e.clientX
  const dx = x - swipeStartX
  if (dx < 0) c._swipeX = Math.max(dx, -70)
  else c._swipeX = 0
}
function onSwipeEnd(c) {
  if (!swiping) return
  swiping = false
  c._swipeX = (c._swipeX || 0) < -35 ? -70 : 0
}
async function deleteConversation(c) {
  try {
    await request.delete('/api/chat/conversation', { params: { targetId: c.targetId, targetType: c.targetType } })
    conversations.value = conversations.value.filter(item => !(item.targetId === c.targetId && item.targetType === c.targetType))
    if (currentTarget.value?.targetId === c.targetId && currentTarget.value?.targetType === c.targetType) {
      currentTarget.value = null
      messages.value = []
    }
    ElMessage.success('会话已删除')
  } catch {}
}

const filteredConversations = computed(() => {
  if (!searchKey.value) return conversations.value
  const k = searchKey.value.toLowerCase()
  return conversations.value.filter(c => (c.targetName || '').toLowerCase().includes(k) || String(c.targetId).includes(k))
})

async function loadInfo() {
  try { const r = await request.get('/api/merchant/info'); myMerchantId.value = r.data?.id } catch {}
}

async function loadConversations() {
  try {
    const r = await request.get('/api/chat/conversations')
    conversations.value = r.data || []
  } catch {}
}

async function selectConversation(c) {
  currentTarget.value = c
  await loadHistory()
  try {
    await request.put('/api/chat/read', null, { params: { senderId: c.targetId, senderType: c.targetType } })
    c.unreadCount = 0
  } catch {}
}

async function loadHistory() {
  if (!currentTarget.value) return
  try {
    const r = await request.get('/api/chat/history', {
      params: { targetId: currentTarget.value.targetId, targetType: currentTarget.value.targetType, current: 1, size: 200 }
    })
    messages.value = r.data?.records || []
    await nextTick()
    scrollToBottom()
  } catch {}
}

function sendMessage() {
  if (!inputText.value.trim() || !currentTarget.value || !ws || ws.readyState !== WebSocket.OPEN) return
  ws.send(JSON.stringify({
    receiverId: currentTarget.value.targetId,
    receiverType: currentTarget.value.targetType,
    content: inputText.value.trim(),
    productId: null
  }))
  inputText.value = ''
}

function connectWebSocket() {
  const token = localStorage.getItem('merchant_token')
  if (!token) return
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${location.host}/ws/chat?token=${token}`
  ws = new WebSocket(wsUrl)
  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'recall') {
        const idx = messages.value.findIndex(m => m.id === msg.msgId)
        if (idx !== -1) messages.value[idx].recalled = true
        return
      }
      if (currentTarget.value) {
        const isCurrentConv = (
          (msg.senderId === currentTarget.value.targetId && msg.senderType === currentTarget.value.targetType) ||
          (msg.senderId === myMerchantId.value && msg.senderType === 2)
        )
        if (isCurrentConv) {
          if (!messages.value.find(m => m.id === msg.id)) {
            messages.value.push(msg)
            nextTick(() => scrollToBottom())
          }
        }
      }
      loadConversations()
    } catch {}
  }
  ws.onclose = () => { setTimeout(connectWebSocket, 3000) }
}

function scrollToBottom() {
  if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
}

function formatTime(time) {
  if (!time) return ''
  const str = typeof time === 'string' ? time : ''
  if (str.includes('T')) return str.slice(5, 16).replace('T', ' ')
  return str.slice(5, 16)
}

onMounted(async () => {
  await loadInfo()
  await loadConversations()
  connectWebSocket()
  document.addEventListener('click', hideContextMenu)
})
onUnmounted(() => {
  if (ws) { ws.onclose = null; ws.close() }
  document.removeEventListener('click', hideContextMenu)
})
</script>

<style scoped>
.chat-card :deep(.el-card__body) { padding: 0; }
.chat-layout { display: flex; height: 600px; }

.chat-sidebar { width: 260px; border-right: 1px solid #eee; display: flex; flex-direction: column; }
.sidebar-search { padding: 12px; border-bottom: 1px solid #f0f0f0; }
.conversation-list { flex: 1; overflow-y: auto; }
.no-conv { padding: 40px 16px; text-align: center; color: #999; font-size: 13px; }
.conv-swipe-wrapper { position: relative; overflow: hidden; }
.conv-item { display: flex; align-items: center; gap: 10px; padding: 12px 16px; cursor: pointer; border-bottom: 1px solid #f5f5f5; transition: transform 0.2s, background 0.15s; position: relative; z-index: 1; background: #fff; }
.conv-item:hover { background: #f9f9f9; }
.conv-item.active { background: #f0f7ff; }
.conv-delete-btn {
  position: absolute; right: 0; top: 0; bottom: 0; width: 70px;
  background: #E53935; color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 500; cursor: pointer; user-select: none;
}
.conv-avatar { width: 36px; height: 36px; border-radius: 50%; background: #e8e8e8; display: flex; align-items: center; justify-content: center; flex-shrink: 0; overflow: hidden; }
.conv-avatar-img { width: 100%; height: 100%; object-fit: cover; }
.conv-info { flex: 1; min-width: 0; }
.conv-name { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 2px; }
.conv-last { font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; flex-shrink: 0; }
.conv-time { font-size: 11px; color: #bbb; }

.chat-main { flex: 1; display: flex; flex-direction: column; }
.chat-header { padding: 14px 20px; border-bottom: 1px solid #eee; font-weight: 600; font-size: 14px; }
.chat-header-info { display: flex; align-items: center; gap: 8px; }
.chat-header-avatar { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px 20px; display: flex; flex-direction: column; gap: 10px; }
.chat-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #bbb; gap: 8px; }

.msg-item { display: flex; }
.msg-item.mine { justify-content: flex-end; }
.msg-bubble { max-width: 65%; padding: 10px 14px; border-radius: 10px; background: #f5f5f5; font-size: 13px; line-height: 1.6; word-break: break-all; overflow-wrap: break-word; }
.msg-item.mine .msg-bubble { background: #409EFF; color: #fff; }
.msg-time { font-size: 10px; margin-top: 4px; opacity: 0.6; }

.chat-input-area { display: flex; gap: 10px; padding: 12px 20px; border-top: 1px solid #eee; align-items: flex-end; }
.merchant-chat-input {
  flex: 1; padding: 8px 12px; border: 1px solid #dcdfe6; border-radius: 4px;
  font-size: 13px; outline: none; resize: none; font-family: inherit; line-height: 1.5;
  max-height: 100px; overflow-y: auto; transition: border-color 0.2s;
}
.merchant-chat-input:focus { border-color: #409EFF; }
.recall-tip { text-align: center; font-size: 12px; color: #999; padding: 4px 0; }
.ctx-menu { position: fixed; z-index: 999; background: #fff; border-radius: 6px; box-shadow: 0 2px 12px rgba(0,0,0,0.15); padding: 4px 0; min-width: 80px; }
.ctx-menu-item { padding: 8px 20px; font-size: 13px; cursor: pointer; transition: background 0.15s; }
.ctx-menu-item:hover { background: #f5f5f5; }
</style>
