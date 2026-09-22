<template>
  <div class="image-upload">
    <div class="image-list">
      <div v-for="(url, index) in imageList" :key="index" class="image-item">
        <img :src="url" />
        <div class="image-mask">
          <el-icon @click="preview(url)"><ZoomIn /></el-icon>
          <el-icon @click="removeImage(index)"><Delete /></el-icon>
        </div>
      </div>
      <div v-if="imageList.length < limit" class="upload-trigger" @click="triggerUpload">
        <el-icon v-if="!uploading" :size="24"><Plus /></el-icon>
        <el-icon v-else :size="24" class="rotating"><Loading /></el-icon>
        <span class="upload-tip">上传图片</span>
      </div>
    </div>
    <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
    <el-dialog v-model="previewVisible" title="图片预览" width="600px" append-to-body>
      <img :src="previewUrl" style="width:100%;border-radius:8px" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' },
  limit: { type: Number, default: 1 }
})
const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const uploading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')

const imageList = computed(() => {
  if (!props.modelValue) return []
  return props.modelValue.split(',').filter(u => u.trim())
})

function triggerUpload() {
  if (uploading.value) return
  fileInput.value.click()
}

async function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过10MB')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await axios.post('/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data?.code === 200) {
      const url = res.data.data
      const newList = [...imageList.value, url]
      emit('update:modelValue', newList.join(','))
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.data?.message || '上传失败')
    }
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
    fileInput.value.value = ''
  }
}

function removeImage(index) {
  const newList = [...imageList.value]
  newList.splice(index, 1)
  emit('update:modelValue', newList.join(','))
}

function preview(url) {
  previewUrl.value = url
  previewVisible.value = true
}
</script>

<style scoped>
.image-list { display: flex; flex-wrap: wrap; gap: 8px; }
.image-item {
  width: 104px; height: 104px; border-radius: 8px; overflow: hidden;
  position: relative; border: 1px solid #E0D5CC;
}
.image-item img { width: 100%; height: 100%; object-fit: cover; }
.image-mask {
  position: absolute; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; gap: 12px;
  opacity: 0; transition: opacity 0.2s; cursor: pointer;
}
.image-mask .el-icon { color: #fff; font-size: 20px; }
.image-mask .el-icon:hover { color: #C8956C; }
.image-item:hover .image-mask { opacity: 1; }
.upload-trigger {
  width: 104px; height: 104px; border: 1px dashed #D5C4B5; border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 4px; cursor: pointer; transition: all 0.2s; color: #8C8C8C;
  background: #FDFAF7;
}
.upload-trigger:hover { border-color: #C8956C; color: #C8956C; background: #FDF6F0; }
.upload-tip { font-size: 11px; }
@keyframes spin { to { transform: rotate(360deg); } }
.rotating { animation: spin 1s linear infinite; }
</style>
