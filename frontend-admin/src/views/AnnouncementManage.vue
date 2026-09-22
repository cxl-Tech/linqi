<template>
  <div>
    <div class="page-title">公告管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索公告标题/内容" clearable prefix-icon="Search" />
        <el-button type="primary" @click="showDialog(null)">新增</el-button>
      </div>
      <el-table :data="filteredList" stripe>
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="title" label="标题" />
        <el-table-column label="状态"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作"><template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template></el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="form.id?'编辑':'新增'" width="500px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" rows="4" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
const list = ref([]), dialogVisible = ref(false), keyword = ref('')
const filteredList = computed(() => { if (!keyword.value) return list.value; const k = keyword.value.toLowerCase(); return list.value.filter(i => i.title?.toLowerCase().includes(k) || i.content?.toLowerCase().includes(k)) })
const form = reactive({ id: null, title: '', content: '', status: 1 })
async function loadData() { const r = await request.get('/api/announcement/listAll'); list.value = r.data || [] }
function showDialog(row) { if (row) { Object.assign(form, row) } else { form.id = null; form.title = ''; form.content = ''; form.status = 1 }; dialogVisible.value = true }
async function save() { if (form.id) { await request.put('/api/announcement/update', form) } else { await request.post('/api/announcement/add', form) }; ElMessage.success('保存成功'); dialogVisible.value = false; loadData() }
async function del(id) { await ElMessageBox.confirm('确认删除？'); await request.delete(`/api/announcement/${id}`); ElMessage.success('已删除'); loadData() }
onMounted(loadData)
</script>
