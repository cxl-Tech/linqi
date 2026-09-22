<template>
  <div class="chat-page page-container">
    <div class="chat-layout card-static">
      <!-- 左侧会话列表 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <button v-if="fromProduct" class="back-btn" @click="goBack">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
            返回
          </button>
          <span>消息列表</span>
        </div>
        <div class="conversation-list">
          <div v-if="!conversations.length" class="no-conversations">暂无消息</div>
          <div v-for="c in conversations" :key="c.targetId + '_' + c.targetType" class="conv-swipe-wrapper">
            <div class="conversation-item" :class="{ active: currentTarget?.targetId === c.targetId && currentTarget?.targetType === c.targetType }"
                 :style="{ transform: 'translateX(' + (c._swipeX || 0) + 'px)' }"
                 @click="selectConversation(c)"
                 @touchstart="onSwipeStart($event, c)" @touchmove="onSwipeMove($event, c)" @touchend="onSwipeEnd(c)"
                 @mousedown="onSwipeStart($event, c)" @mousemove="onSwipeMove($event, c)" @mouseup="onSwipeEnd(c)" @mouseleave="onSwipeEnd(c)">
              <div class="conv-avatar">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
              </div>
              <div class="conv-info">
                <div class="conv-name">{{ c.targetName || '商家' }}</div>
                <div class="conv-last">{{ c.lastMessage }}</div>
              </div>
              <div class="conv-meta">
                <span class="conv-time">{{ formatTime(c.lastTime) }}</span>
                <span v-if="c.unreadCount > 0" class="conv-badge">{{ c.unreadCount }}</span>
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
            <span class="chat-target-name">{{ currentTarget.targetName || '商家' }}</span>
          </div>
          <div class="chat-messages" ref="messagesRef">
            <div v-for="msg in messages" :key="msg.id || msg.createTime"
                 class="message-item" :class="{ mine: msg.senderId === myUserId && msg.senderType === 1 }">
              <div v-if="msg.recalled" class="recall-tip">{{ msg.senderId === myUserId && msg.senderType === 1 ? '你' : '对方' }}撤回了一条消息</div>
              <div v-else class="message-bubble" @contextmenu.prevent="onMsgRightClick($event, msg)">
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>
            <!-- 右键菜单 -->
            <div v-if="contextMenu.visible" class="ctx-menu" :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }">
              <div class="ctx-menu-item" @click="recallMessage">撤回</div>
            </div>
          </div>
          <div class="chat-input-area">
            <textarea v-model="inputText" class="chat-input" placeholder="输入消息..." @keydown.enter.exact.prevent="sendMessage" rows="2"></textarea>
            <button class="chat-send-btn" @click="sendMessage" :disabled="!inputText.trim()">发送</button>
          </div>
        </template>
        <template v-else>
          <div class="chat-empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            <p>选择一个会话开始聊天</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const fromProduct = ref(false)
const conversations = ref([])
const currentTarget = ref(null)
const messages = ref([])
const inputText = ref('')
const messagesRef = ref(null)
const myUserId = ref(null)
let ws = null
const contextMenu = reactive({ visible: false, x: 0, y: 0, msg: null })

function onMsgRightClick(e, msg) {
  // 只能撤回自己的消息
  if (msg.senderId !== myUserId.value || msg.senderType !== 1) return
  // 判断2分钟
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
  // 先收起其他已展开的
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

// 获取当前用户ID
async function loadUserInfo() {
  try {
    const r = await request.get('/api/user/info')
    myUserId.value = r.data?.id
  } catch {}
}

// 加载会话列表
async function loadConversations() {
  try {
    const r = await request.get('/api/chat/conversations')
    const list = r.data || []
    // 加载商家名称
    for (const c of list) {
      if (c.targetType === 2) {
        try { const m = await request.get(`/api/merchant/info/public/${c.targetId}`); c.targetName = m.data?.shopName || '商家' } catch { c.targetName = '商家#' + c.targetId }
      }
    }
    conversations.value = list
  } catch {}
}

// 选择会话
async function selectConversation(c) {
  currentTarget.value = c
  await loadHistory()
  // 标记已读
  try {
    await request.put('/api/chat/read', null, { params: { senderId: c.targetId, senderType: c.targetType } })
    c.unreadCount = 0
  } catch {}
}

// 加载聊天历史
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

// 发送消息
function sendMessage() {
  if (!inputText.value.trim() || !currentTarget.value || !ws || ws.readyState !== WebSocket.OPEN) return
  const msg = {
    receiverId: currentTarget.value.targetId,
    receiverType: currentTarget.value.targetType,
    content: inputText.value.trim(),
    productId: null
  }
  ws.send(JSON.stringify(msg))
  inputText.value = ''
}

// WebSocket 连接
function connectWebSocket() {
  const token = localStorage.getItem('user_token')
  if (!token) return
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${location.host}/ws/chat?token=${token}`
  ws = new WebSocket(wsUrl)
  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      // 处理撤回通知
      if (msg.type === 'recall') {
        const idx = messages.value.findIndex(m => m.id === msg.msgId)
        if (idx !== -1) messages.value[idx].recalled = true
        return
      }
      // 如果消息属于当前会话
      if (currentTarget.value) {
        const isCurrentConv = (
          (msg.senderId === currentTarget.value.targetId && msg.senderType === currentTarget.value.targetType) ||
          (msg.senderId === myUserId.value && msg.senderType === 1)
        )
        if (isCurrentConv) {
          // 避免重复
          if (!messages.value.find(m => m.id === msg.id)) {
            messages.value.push(msg)
            nextTick(() => scrollToBottom())
          }
        }
      }
      // 更新会话列表
      loadConversations()
    } catch {}
  }
  ws.onclose = () => { setTimeout(connectWebSocket, 3000) }
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

function formatTime(time) {
  if (!time) return ''
  const str = typeof time === 'string' ? time : ''
  if (str.includes('T')) return str.slice(5, 16).replace('T', ' ')
  return str.slice(5, 16)
}

// 从商品页跳转来：自动打开与商家的对话
async function initFromQuery() {
  const merchantId = route.query.merchantId
  if (merchantId) {
    fromProduct.value = true
    let targetName = '商家'
    try { const m = await request.get(`/api/merchant/info/public/${merchantId}`); targetName = m.data?.shopName || '商家' } catch {}
    const target = { targetId: Number(merchantId), targetType: 2, targetName, lastMessage: '', unreadCount: 0 }
    currentTarget.value = target
    // 如果会话列表中没有该商家，手动加入
    if (!conversations.value.find(c => c.targetId === Number(merchantId) && c.targetType === 2)) {
      conversations.value.unshift(target)
    }
    await loadHistory()
  }
}

function goBack() {
  router.back()
}

onMounted(async () => {
  await loadUserInfo()
  await loadConversations()
  connectWebSocket()
  await initFromQuery()
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  if (ws) { ws.onclose = null; ws.close() }
  document.removeEventListener('click', hideContextMenu)
})
</script>

<style scoped>
.chat-layout { display: flex; height: calc(100vh - 200px); min-height: 500px; overflow: hidden; }

/* 左侧会话列表 */
.chat-sidebar { width: 280px; border-right: 1px solid var(--border-light); display: flex; flex-direction: column; }
.sidebar-header { padding: 16px 20px; font-size: 16px; font-weight: 700; color: var(--text-primary); border-bottom: 1px solid var(--border-light); display: flex; align-items: center; gap: 10px; }
.back-btn {
  display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px;
  border: 1px solid var(--border-light); border-radius: 6px; background: #fff;
  color: var(--text-regular); font-size: 13px; cursor: pointer; transition: all 0.2s;
  flex-shrink: 0;
}
.back-btn:hover { background: var(--color-primary-bg); color: var(--color-primary); border-color: var(--color-primary); }
.conversation-list { flex: 1; overflow-y: auto; }
.no-conversations { padding: 40px 20px; text-align: center; color: var(--text-secondary); font-size: 14px; }
.conv-swipe-wrapper { position: relative; overflow: hidden; }
.conversation-item {
  display: flex; align-items: center; gap: 12px; padding: 14px 20px;
  cursor: pointer; transition: transform 0.2s, background 0.2s; border-bottom: 1px solid var(--border-light);
  position: relative; z-index: 1; background: #fff;
}
.conversation-item:hover { background: var(--bg-section); }
.conversation-item.active { background: var(--color-primary-bg); }
.conv-delete-btn {
  position: absolute; right: 0; top: 0; bottom: 0; width: 70px;
  background: #E53935; color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 500; cursor: pointer; user-select: none;
}
.conv-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.conv-info { flex: 1; min-width: 0; }
.conv-name { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.conv-last { font-size: 12px; color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; flex-shrink: 0; }
.conv-time { font-size: 11px; color: var(--text-secondary); }
.conv-badge {
  min-width: 18px; height: 18px; border-radius: 9px; background: #E53935;
  color: #fff; font-size: 11px; display: flex; align-items: center; justify-content: center;
  padding: 0 5px;
}

/* 右侧聊天区 */
.chat-main { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.chat-header { padding: 16px 24px; border-bottom: 1px solid var(--border-light); font-size: 15px; font-weight: 600; color: var(--text-primary); }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px 24px; display: flex; flex-direction: column; gap: 12px; }
.chat-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--text-secondary); gap: 12px; }

.message-item { display: flex; }
.message-item.mine { justify-content: flex-end; }
.message-bubble {
  max-width: 70%; padding: 10px 16px; border-radius: 12px;
  background: var(--bg-section); color: var(--text-primary); font-size: 14px; line-height: 1.6;
  word-break: break-all; overflow-wrap: break-word;
}
.message-item.mine .message-bubble {
  background: var(--color-primary); color: #fff;
}
.message-time { font-size: 11px; margin-top: 4px; opacity: 0.6; }

/* 输入区 */
.chat-input-area { display: flex; gap: 12px; padding: 16px 24px; border-top: 1px solid var(--border-light); }
.chat-input {
  flex: 1; padding: 10px 16px; border: 1px solid var(--border-light); border-radius: var(--radius-sm);
  font-size: 14px; outline: none; transition: border-color 0.2s;
  resize: none; font-family: inherit; line-height: 1.5;
  max-height: 100px; overflow-y: auto;
}
.chat-input:focus { border-color: var(--color-primary); }
.chat-send-btn {
  padding: 10px 24px; background: var(--color-primary); color: #fff; border: none;
  border-radius: var(--radius-sm); font-size: 14px; font-weight: 500; cursor: pointer;
  transition: background 0.2s;
}
.chat-send-btn:hover:not(:disabled) { background: var(--color-primary-dark); }
.chat-send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* 撤回提示 */
.recall-tip { text-align: center; font-size: 12px; color: var(--text-secondary); padding: 4px 0; }

/* 右键菜单 */
.ctx-menu {
  position: fixed; z-index: 999; background: #fff; border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15); padding: 4px 0; min-width: 80px;
}
.ctx-menu-item {
  padding: 8px 20px; font-size: 13px; cursor: pointer; transition: background 0.15s;
}
.ctx-menu-item:hover { background: var(--bg-section); }
</style>
