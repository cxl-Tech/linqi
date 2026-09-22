<template>
  <div class="profile-page page-container">
    <div class="profile-layout">
      <!-- 左侧菜单 -->
      <aside class="profile-sidebar card-static">
        <div class="user-avatar">
          <div class="avatar-circle" @click="triggerAvatarUpload" title="点击更换头像">
            <img v-if="form.avatar" :src="getImageUrl(form.avatar)" class="avatar-img" />
            <svg v-else width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.5"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <div class="avatar-overlay">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/></svg>
            </div>
          </div>
          <input type="file" ref="avatarInput" accept="image/*" style="display:none" @change="uploadAvatar" />
          <div class="user-name">{{ form.nickname || form.username }}</div>
        </div>
        <nav class="side-menu">
          <div class="menu-item" :class="{ active: currentSection === 'info' }" @click="currentSection='info'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            基本信息
          </div>
          <div class="menu-item" :class="{ active: currentSection === 'password' }" @click="currentSection='password'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
            修改密码
          </div>
          <div class="menu-item" :class="{ active: currentSection === 'address' }" @click="currentSection='address'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            收货地址
          </div>
          <div class="menu-item" :class="{ active: currentSection === 'favorites' }" @click="currentSection='favorites'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
            我的收藏
          </div>
          <div class="menu-item" :class="{ active: currentSection === 'reviews' }" @click="currentSection='reviews'; loadMyReviews()">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            我的评价
          </div>
          <div class="menu-item" :class="{ active: currentSection === 'history' }" @click="currentSection='history'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            浏览历史
          </div>
          <div class="menu-item" :class="{ active: currentSection === 'complaints' }" @click="currentSection='complaints'; loadMyComplaints()">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            我的投诉
          </div>
        </nav>
      </aside>

      <!-- 右侧内容 -->
      <div class="profile-content">
        <!-- 基本信息 -->
        <div v-show="currentSection === 'info'" class="content-section card-static">
          <h3 class="section-title">基本信息</h3>
          <el-form :model="form" label-width="80px" class="profile-form">
            <el-form-item label="头像">
              <div class="avatar-upload-area" @click="triggerAvatarUpload">
                <img v-if="form.avatar" :src="getImageUrl(form.avatar)" class="avatar-preview" />
                <div v-else class="avatar-placeholder">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="1.5"><path d="M12 5v14M5 12h14"/></svg>
                  <span>上传头像</span>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="用户名"><el-input :value="form.username" disabled /></el-form-item>
            <el-form-item label="昵称"><el-input v-model="form.nickname" placeholder="设置昵称" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
            <el-form-item><button class="btn-primary" @click="saveInfo">保存修改</button></el-form-item>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div v-show="currentSection === 'password'" class="content-section card-static">
          <h3 class="section-title">修改密码</h3>
          <el-form :model="pwdForm" label-width="80px" class="profile-form">
            <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
            <el-form-item><button class="btn-primary" @click="changePwd">确认修改</button></el-form-item>
          </el-form>
        </div>

        <!-- 收货地址 -->
        <div v-show="currentSection === 'address'" class="content-section card-static">
          <div class="section-header-row">
            <h3 class="section-title">收货地址</h3>
            <button class="btn-add" @click="resetAddrForm(); showAddr=true">+ 新增地址</button>
          </div>
          <el-empty v-if="!addresses.length" description="暂无收货地址" :image-size="80" />
          <div v-for="a in addresses" :key="a.id" class="address-item">
            <div class="addr-main">
              <div class="addr-info">
                <span class="addr-name">{{ a.receiverName }}</span>
                <span class="addr-phone">{{ a.phone }}</span>
                <span v-if="a.isDefault" class="addr-default">默认</span>
              </div>
              <div class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}</div>
            </div>
            <div class="addr-actions">
              <button v-if="!a.isDefault" class="btn-link" @click="setDefaultAddr(a.id)">设为默认</button>
              <button class="btn-link" @click="editAddr(a)">编辑</button>
              <button class="btn-link danger" @click="deleteAddr(a.id)">删除</button>
            </div>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-show="currentSection === 'favorites'" class="content-section card-static">
          <h3 class="section-title">我的收藏</h3>
          <el-empty v-if="!favorites.length" description="暂无收藏" :image-size="80" />
          <div class="product-grid">
            <div v-for="f in favorites" :key="f.id" class="product-card" @click="$router.push(`/product/${f.productId}`)">
              <div class="product-card-img">
                <img v-if="productMap[f.productId]?.images" :src="getImageUrl(productMap[f.productId].images.split(',')[0])" />
                <div v-else class="img-placeholder">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
                </div>
              </div>
              <div class="product-card-body">
                <div class="product-card-name">{{ productMap[f.productId]?.name || '加载中...' }}</div>
                <div class="product-card-bottom">
                  <span class="product-card-price" v-if="productMap[f.productId]">¥{{ productMap[f.productId].price }}</span>
                  <span class="product-card-time">{{ f.createTime?.slice(0, 10) }}</span>
                </div>
              </div>
              <button class="btn-unfav" @click.stop="removeFav(f)">×</button>
            </div>
          </div>
        </div>

        <!-- 我的评价 -->
        <div v-show="currentSection === 'reviews'" class="content-section card-static">
          <h3 class="section-title">我的评价</h3>
          <el-empty v-if="!myReviews.length" description="暂无评价" :image-size="80" />
          <div v-for="r in myReviews" :key="r.id" class="my-review-card">
            <div class="my-review-header">
              <span class="my-review-product" @click="$router.push(`/product/${r.productId}`)">{{ reviewProductMap[r.productId] || '商品#' + r.productId }}</span>
              <el-rate :model-value="r.rating" disabled size="small" />
            </div>
            <div class="my-review-content" v-if="r.content">{{ r.content }}</div>
            <div class="my-review-images" v-if="r.images">
              <el-image v-for="(img, i) in r.images.split(',')" :key="i" :src="getImageUrl(img)" class="my-review-img" fit="cover" :preview-src-list="r.images.split(',').map(getImageUrl)" />
            </div>
            <div class="my-review-time">评价于 {{ r.createTime }}</div>
            <div class="my-review-append" v-if="r.appendContent">
              <div class="append-badge">追加评价</div>
              <div class="append-content-text">{{ r.appendContent }}</div>
            </div>
            <div class="my-review-reply" v-if="r.merchantReply">
              <div class="reply-badge">商家回复</div>
              <div class="reply-content">{{ r.merchantReply }}</div>
              <div class="reply-time" v-if="r.replyTime">{{ r.replyTime }}</div>
            </div>
            <div class="my-review-no-reply" v-else>
              <span>商家暂未回复</span>
            </div>
          </div>
          <div class="reviews-pagination" v-if="myReviewTotal > 10">
            <el-pagination background layout="prev,pager,next" :total="myReviewTotal" :page-size="10" v-model:current-page="myReviewPage" @current-change="loadMyReviews" small />
          </div>
        </div>

        <!-- 浏览历史 -->
        <div v-show="currentSection === 'history'" class="content-section card-static">
          <h3 class="section-title">浏览历史</h3>
          <el-empty v-if="!histories.length" description="暂无浏览记录" :image-size="80" />
          <div class="product-grid">
            <div v-for="h in histories" :key="h.id" class="product-card" @click="$router.push(`/product/${h.productId}`)">
              <div class="product-card-img">
                <img v-if="productMap[h.productId]?.images" :src="getImageUrl(productMap[h.productId].images.split(',')[0])" />
                <div v-else class="img-placeholder">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
                </div>
              </div>
              <div class="product-card-body">
                <div class="product-card-name">{{ productMap[h.productId]?.name || '加载中...' }}</div>
                <div class="product-card-bottom">
                  <span class="product-card-price" v-if="productMap[h.productId]">¥{{ productMap[h.productId].price }}</span>
                  <span class="product-card-time">{{ h.createTime?.slice(0, 10) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的投诉 -->
        <div v-show="currentSection === 'complaints'" class="content-section card-static">
          <h3 class="section-title">我的投诉</h3>
          <el-empty v-if="!complaints.length" description="暂无投诉记录" :image-size="80" />
          <div class="complaint-list">
            <div v-for="c in complaints" :key="c.id" class="complaint-card">
              <div class="complaint-card-header">
                <el-tag :type="c.status===0 ? 'warning' : 'success'" size="small">{{ c.status===0 ? '待处理' : '已处理' }}</el-tag>
                <span class="complaint-type">{{ ['','态度恶劣','辱骂消费者','虚假宣传','其他'][c.type] || '未知' }}</span>
                <span class="complaint-time">{{ c.createTime }}</span>
              </div>
              <div class="complaint-card-body">{{ c.content }}</div>
              <div v-if="c.images" class="complaint-card-images">
                <el-image v-for="(img, i) in c.images.split(',')" :key="i" :src="getImageUrl(img)" class="complaint-thumb" fit="cover" :preview-src-list="c.images.split(',').map(getImageUrl)" />
              </div>
              <div v-if="c.adminReply" class="complaint-reply">
                <span class="reply-label">平台回复：</span>{{ c.adminReply }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showAddr" :title="editingAddrId ? '编辑收货地址' : '新增收货地址'" width="480px">
      <el-form :model="addrForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="addrForm.receiverName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="addrForm.phone" /></el-form-item>
        <el-form-item label="所在地区">
          <el-cascader v-model="regionValue" :options="regionData" placeholder="请选择省/市/区" style="width:100%" :props="{ expandTrigger: 'hover' }" clearable filterable />
        </el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addrForm.detail" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddr=false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import regionData from '../utils/region-data'
import { getImageUrl } from '../utils/image'

const currentSection = ref('info')
const form = reactive({ username: '', nickname: '', phone: '', email: '', avatar: '' })
const avatarInput = ref(null)
function triggerAvatarUpload() { avatarInput.value?.click() }
async function uploadAvatar(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) { ElMessage.warning('图片不能超过2MB'); return }
  const fd = new FormData()
  fd.append('file', file)
  try {
    const r = await request.post('/api/file/upload', fd)
    form.avatar = r.data
    await request.put('/api/user/update', { avatar: form.avatar })
    ElMessage.success('头像更新成功')
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const addresses = ref([])
const showAddr = ref(false)
const editingAddrId = ref(null)
const addrForm = reactive({ receiverName: '', phone: '', province: '', city: '', district: '', detail: '' })
const regionValue = ref([])
watch(regionValue, (val) => {
  addrForm.province = val[0] || ''
  addrForm.city = val[1] || ''
  addrForm.district = val[2] || ''
})
const favorites = ref([])
const histories = ref([])
const productMap = ref({})
const myReviews = ref([])
const myReviewTotal = ref(0)
const myReviewPage = ref(1)
const reviewProductMap = ref({})

async function loadMyReviews() {
  try {
    const r = await request.get('/api/review/user/list', { params: { current: myReviewPage.value, size: 10 } })
    myReviews.value = r.data?.records || []
    myReviewTotal.value = r.data?.total || 0
    // 加载商品名称
    for (const item of myReviews.value) {
      if (item.productId && !reviewProductMap.value[item.productId]) {
        try { const p = await request.get(`/api/product/detail/${item.productId}`); reviewProductMap.value[item.productId] = p.data?.name } catch {}
      }
    }
  } catch {}
}

// ===== 投诉 =====
const complaints = ref([])
async function loadMyComplaints() {
  try {
    const r = await request.get('/api/complaint/user/list', { params: { current: 1, size: 50 } })
    complaints.value = r.data?.records || []
  } catch {}
}

onMounted(async () => {
  const res = await request.get('/api/user/info')
  Object.assign(form, res.data)
  try { const r = await request.get('/api/address/list'); addresses.value = r.data || [] } catch {}
  try { const r = await request.get('/api/favorite/list'); favorites.value = r.data?.records || [] } catch {}
  try { const r = await request.get('/api/history/list'); histories.value = r.data?.records || [] } catch {}
  // 批量加载商品详情
  const allIds = new Set([...favorites.value.map(f => f.productId), ...histories.value.map(h => h.productId)])
  for (const pid of allIds) {
    try { const r = await request.get(`/api/product/detail/${pid}`); productMap.value[pid] = r.data } catch {}
  }
})

async function saveInfo() {
  await request.put('/api/user/update', form)
  ElMessage.success('保存成功')
}
async function changePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) return ElMessage.warning('请填写完整')
  await request.put('/api/user/password', pwdForm)
  ElMessage.success('密码修改成功')
  pwdForm.oldPassword = ''; pwdForm.newPassword = ''
}
function resetAddrForm() {
  Object.assign(addrForm, { receiverName: '', phone: '', province: '', city: '', district: '', detail: '' })
  regionValue.value = []
  editingAddrId.value = null
}
function editAddr(a) {
  editingAddrId.value = a.id
  Object.assign(addrForm, { receiverName: a.receiverName, phone: a.phone, province: a.province, city: a.city, district: a.district, detail: a.detail })
  regionValue.value = [a.province, a.city, a.district].filter(Boolean)
  showAddr.value = true
}
async function saveAddress() {
  if (!addrForm.receiverName || !addrForm.phone) return ElMessage.warning('请填写姓名和电话')
  if (editingAddrId.value) {
    await request.put('/api/address/update', { id: editingAddrId.value, ...addrForm })
    ElMessage.success('修改成功')
  } else {
    await request.post('/api/address/add', addrForm)
    ElMessage.success('添加成功')
  }
  showAddr.value = false
  resetAddrForm()
  const r = await request.get('/api/address/list'); addresses.value = r.data || []
}
async function deleteAddr(id) {
  await request.delete(`/api/address/${id}`)
  ElMessage.success('删除成功')
  addresses.value = addresses.value.filter(a => a.id !== id)
}
async function setDefaultAddr(id) {
  await request.put(`/api/address/default/${id}`)
  ElMessage.success('已设为默认')
  const r = await request.get('/api/address/list'); addresses.value = r.data || []
}
async function removeFav(f) {
  await request.post(`/api/favorite/toggle/${f.productId}`)
  favorites.value = favorites.value.filter(item => item.id !== f.id)
  ElMessage.success('已取消收藏')
}
</script>

<style scoped>
.profile-layout { display: flex; gap: 24px; align-items: flex-start; }

/* ===== 左侧侧边栏 ===== */
.profile-sidebar { width: 220px; flex-shrink: 0; padding: 24px 0; }
.user-avatar { display: flex; flex-direction: column; align-items: center; padding-bottom: 20px; border-bottom: 1px solid var(--border-light); }
.avatar-circle {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  display: flex; align-items: center; justify-content: center; margin-bottom: 10px;
  cursor: pointer; position: relative; overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.avatar-circle:hover .avatar-overlay { opacity: 1; }
.avatar-upload-area {
  width: 80px; height: 80px; border-radius: 50%; cursor: pointer;
  border: 2px dashed var(--border-light); display: flex; align-items: center; justify-content: center;
  overflow: hidden; transition: border-color 0.2s;
}
.avatar-upload-area:hover { border-color: var(--color-primary); }
.avatar-preview { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }
.avatar-placeholder { display: flex; flex-direction: column; align-items: center; gap: 2px; font-size: 11px; color: #999; }
.user-name { font-size: 15px; font-weight: 600; color: var(--text-primary); }

.side-menu { padding: 12px 0; }
.menu-item {
  display: flex; align-items: center; gap: 10px; padding: 12px 24px;
  font-size: 14px; color: var(--text-regular); cursor: pointer;
  border-left: 3px solid transparent; transition: all 0.2s;
}
.menu-item:hover { background: var(--color-primary-bg); color: var(--color-primary-dark); }
.menu-item.active {
  background: var(--color-primary-bg); color: var(--color-primary-dark);
  font-weight: 600; border-left-color: var(--color-primary);
}

/* ===== 右侧内容 ===== */
.profile-content { flex: 1; min-width: 0; }
.content-section { padding: 28px; }
.section-title {
  font-size: 18px; font-weight: 700; color: var(--text-primary);
  padding-bottom: 16px; margin-bottom: 20px; border-bottom: 1px solid var(--border-light);
}
.section-header-row { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; margin-bottom: 20px; border-bottom: 1px solid var(--border-light); }
.section-header-row .section-title { border: none; padding: 0; margin: 0; }

.profile-form { max-width: 480px; }
.btn-primary {
  padding: 10px 28px; background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-sm); font-size: 14px;
  cursor: pointer; font-weight: 500; transition: background 0.2s;
}
.btn-primary:hover { background: var(--color-primary-dark); }
.btn-add {
  padding: 6px 16px; background: var(--bg-white); color: var(--color-primary);
  border: 1px solid var(--color-primary); border-radius: var(--radius-sm);
  font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.btn-add:hover { background: var(--color-primary); color: #fff; }

/* ===== 地址列表 ===== */
.address-item {
  padding: 14px 0; border-bottom: 1px solid var(--border-light);
  display: flex; justify-content: space-between; align-items: center; gap: 16px;
}
.address-item:last-child { border-bottom: none; }
.addr-main { flex: 1; min-width: 0; }
.addr-info { display: flex; align-items: center; gap: 12px; margin-bottom: 4px; }
.addr-actions { display: flex; gap: 8px; flex-shrink: 0; }
.btn-link {
  background: none; border: none; color: var(--color-primary); font-size: 13px;
  cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: all 0.2s;
}
.btn-link:hover { background: var(--color-primary-bg); }
.btn-link.danger { color: #E53935; }
.btn-link.danger:hover { background: #FEF2F2; }
.addr-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.addr-phone { font-size: 13px; color: var(--text-secondary); }
.addr-default {
  font-size: 11px; padding: 1px 8px; border-radius: 3px;
  background: var(--color-primary-bg); color: var(--color-primary-dark); font-weight: 600;
}
.addr-detail { font-size: 13px; color: var(--text-secondary); }

/* ===== 收藏/历史商品网格 ===== */
.product-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px;
}
.product-card {
  position: relative; border: 1px solid var(--border-light); border-radius: var(--radius-md);
  overflow: hidden; cursor: pointer; transition: all 0.25s; background: var(--bg-white);
}
.product-card:hover { box-shadow: var(--shadow-hover); transform: translateY(-2px); }
.product-card-img { width: 100%; height: 160px; overflow: hidden; background: #fafafa; }
.product-card-img img { width: 100%; height: 100%; object-fit: cover; }
.img-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f8f5f2; }
.product-card-body { padding: 10px 12px; }
.product-card-name {
  font-size: 13px; font-weight: 500; color: var(--text-primary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 6px;
}
.product-card-bottom { display: flex; justify-content: space-between; align-items: center; }
.product-card-price { font-size: 15px; font-weight: 700; color: var(--color-accent); }
.product-card-time { font-size: 11px; color: var(--text-secondary); }
.btn-unfav {
  position: absolute; top: 6px; right: 6px; width: 24px; height: 24px;
  border-radius: 50%; border: none; background: rgba(0,0,0,0.4); color: #fff;
  font-size: 14px; cursor: pointer; display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.product-card:hover .btn-unfav { opacity: 1; }
.btn-unfav:hover { background: var(--color-accent); }

/* ===== 我的评价 ===== */
.my-review-card {
  padding: 16px 0; border-bottom: 1px solid var(--border-light);
}
.my-review-card:last-child { border-bottom: none; }
.my-review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.my-review-product {
  font-size: 14px; font-weight: 600; color: var(--color-primary-dark);
  cursor: pointer; transition: color 0.2s;
}
.my-review-product:hover { color: var(--color-primary); text-decoration: underline; }
.my-review-content { font-size: 14px; color: var(--text-regular); line-height: 1.8; margin-bottom: 8px; }
.my-review-images { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
.my-review-img { width: 72px; height: 72px; border-radius: 6px; }
.my-review-time { font-size: 12px; color: var(--text-secondary); }
.my-review-append {
  margin-top: 10px; padding: 10px 14px; border-radius: var(--radius-sm);
  background: #F0F9FF; border-left: 3px solid #1890FF;
}
.append-badge { font-size: 12px; font-weight: 600; color: #1890FF; margin-bottom: 4px; }
.append-content-text { font-size: 13px; color: var(--text-regular); line-height: 1.6; }
.my-review-reply {
  margin-top: 12px; padding: 12px 16px; border-radius: var(--radius-sm);
  background: var(--color-primary-bg); border-left: 3px solid var(--color-primary);
}
.reply-badge {
  font-size: 12px; font-weight: 600; color: var(--color-primary-dark); margin-bottom: 6px;
}
.reply-content { font-size: 13px; color: var(--text-regular); line-height: 1.6; }
.reply-time { font-size: 11px; color: var(--text-secondary); margin-top: 6px; }
.my-review-no-reply {
  margin-top: 8px; font-size: 12px; color: var(--text-secondary); font-style: italic;
}
.reviews-pagination { display: flex; justify-content: center; padding: 16px 0; }

/* ===== 我的投诉 ===== */
.complaint-list { display: flex; flex-direction: column; gap: 12px; }
.complaint-card { padding: 16px; border: 1px solid var(--border-light); border-radius: var(--radius-sm); }
.complaint-card-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.complaint-type { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.complaint-time { font-size: 12px; color: var(--text-secondary); margin-left: auto; }
.complaint-card-body { font-size: 14px; color: var(--text-regular); line-height: 1.6; margin-bottom: 8px; }
.complaint-card-images { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
.complaint-thumb { width: 72px; height: 72px; border-radius: 6px; }
.complaint-reply { padding: 10px 14px; background: #E6F7FF; border-left: 3px solid #1890FF; border-radius: 4px; font-size: 13px; color: var(--text-regular); }
.complaint-reply .reply-label { font-weight: 600; color: #1890FF; }
</style>
