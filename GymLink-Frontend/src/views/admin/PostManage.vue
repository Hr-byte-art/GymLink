<template>
  <AdminLayout>
    <div class="manage-container">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
          </el-form-item>
          <el-form-item label="发布者角色">
            <el-select v-model="searchForm.userRole" placeholder="请选择" clearable>
              <el-option label="教练" :value="1" />
              <el-option label="学员" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="table-card">
        <template #header>
          <div class="card-header">
            <span>帖子列表</span>
          </div>
        </template>
        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
          <el-table-column prop="userId" label="发布者ID" width="100" />
          <el-table-column label="发布者角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.userRole === 1 ? 'success' : 'primary'">{{ row.userRole === 1 ? '教练' : '学员' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="80" />
          <el-table-column prop="likeCount" label="点赞数" width="80" />
          <el-table-column prop="content" label="内容" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-html="stripHtml(row.content)"></span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="pagination" v-model:current-page="pagination.current" v-model:page-size="pagination.pageSize"
          :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="loadData" />
      </el-card>
    </div>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="帖子详情" width="700px">
      <div class="post-detail">
        <h3>{{ currentPost.title }}</h3>
        <div class="post-meta">
          <span>发布者ID: {{ currentPost.userId }}</span>
          <span>角色: {{ currentPost.userRole === 1 ? '教练' : '学员' }}</span>
          <span>浏览量: {{ currentPost.viewCount }}</span>
          <span>点赞数: {{ currentPost.likeCount }}</span>
          <span>发布时间: {{ formatDate(currentPost.createTime) }}</span>
        </div>
        <el-divider />
        <div class="post-content" v-html="currentPost.content"></div>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminLayout from '@/components/AdminLayout.vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const viewDialogVisible = ref(false)
const currentPost = ref<any>({})

const searchForm = reactive({ title: '', userRole: null as number | null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const formatDate = (date: string) => date ? new Date(date).toLocaleString('zh-CN') : ''

// 去除HTML标签，用于表格显示
const stripHtml = (html: string) => {
  if (!html) return ''
  return html.replace(/<[^>]*>/g, '').substring(0, 100) + (html.length > 100 ? '...' : '')
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/experience/listExperience', {
      pageNum: pagination.current, pageSize: pagination.pageSize,
      title: searchForm.title || undefined, userRole: searchForm.userRole || undefined
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const resetSearch = () => { Object.assign(searchForm, { title: '', userRole: null }); handleSearch() }

const handleView = (row: any) => {
  currentPost.value = row
  viewDialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？', '提示', { type: 'warning' })
    await request.post('/experience/deleteExperience', null, { params: { id: row.id } })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => loadData())
</script>

<style scoped>
.manage-container { display: flex; flex-direction: column; gap: 20px; }
.search-card { margin-bottom: 0; }
.search-form { display: flex; flex-wrap: wrap; }
.search-form :deep(.el-select) { width: 200px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; justify-content: flex-end; }
.post-detail h3 { margin: 0 0 15px 0; color: #303133; }
.post-meta { display: flex; gap: 20px; color: #909399; font-size: 14px; flex-wrap: wrap; }
.post-content { line-height: 1.8; color: #606266; max-height: 400px; overflow-y: auto; }
</style>
