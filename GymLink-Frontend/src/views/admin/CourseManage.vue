<template>
  <AdminLayout>
    <div class="manage-container">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="课程名称">
            <el-input v-model="searchForm.name" placeholder="请输入课程名称" clearable />
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="searchForm.difficulty" placeholder="请选择" clearable>
              <el-option label="初级" value="初级" />
              <el-option label="中级" value="中级" />
              <el-option label="高级" value="高级" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.type" placeholder="请选择分类" clearable>
              <el-option v-for="item in courseTypes" :key="item.value" :label="item.label" :value="item.value" />
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
            <span>课程列表</span>
            <el-button type="primary" @click="handleAdd">添加课程</el-button>
          </div>
        </template>
        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="封面" width="100">
            <template #default="{ row }">
              <el-image :src="row.image" style="width: 60px; height: 40px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="课程名称" width="150" />
          <el-table-column prop="coachId" label="教练ID" width="100" />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="duration" label="时长(分钟)" width="100" />
          <el-table-column prop="difficulty" label="难度" width="80" />
          <el-table-column prop="type" label="分类" width="140" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="pagination" v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize" :total="pagination.total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" @change="loadData" />
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="教练ID" prop="coachId">
          <el-input-number v-model="form.coachId" :min="1" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="form.image" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="form.duration" :min="1" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" placeholder="请选择">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.type" placeholder="请选择分类" clearable>
            <el-option v-for="item in courseTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
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
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 课程分类数据
const courseTypes = [
  { value: '私教课程', label: '私教课程' },
  { value: '团体训练课程', label: '团体训练课程' },
  { value: '功能性训练课程', label: '功能性训练课程' },
  { value: '力量训练课程', label: '力量训练课程' },
  { value: '瑜伽课程', label: '瑜伽课程' },
  { value: '普拉提课程', label: '普拉提课程' },
  { value: '康复/矫正训练课程', label: '康复/矫正训练课程' },
  { value: '专项运动表现课程', label: '专项运动表现课程' },
  { value: '孕产/产后修复课程', label: '孕产/产后修复课程' },
  { value: '老年/青少年体适能课程', label: '老年/青少年体适能课程' },
  { value: '线上直播/录播课程', label: '线上直播/录播课程' }
]

const searchForm = reactive({ name: '', difficulty: '', type: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const form = reactive({ id: 0, name: '', coachId: 1, image: '', price: 0, duration: 60, difficulty: '初级', type: '', description: '' })

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  coachId: [{ required: true, message: '请输入教练ID', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const dialogTitle = ref('添加课程')
const formatDate = (date: string) => date ? new Date(date).toLocaleString('zh-CN') : ''

const loadData = async (params?: any) => {
  loading.value = true
  try {
    const res = await request.post('/course/listCourse', {
      pageNum: pagination.current, pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      difficulty: searchForm.difficulty || undefined,
      type: searchForm.type || undefined,
      ...params
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const resetSearch = () => { Object.assign(searchForm, { name: '', difficulty: '', type: '' }); handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加课程'
  Object.assign(form, { id: 0, name: '', coachId: 1, image: '', price: 0, duration: 60, difficulty: '初级', type: '', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑课程'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？', '提示', { type: 'warning' })
    await request.post('/course/deleteCourse', null, { params: { id: row.id } })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await request.post(`/course/updateCourse?id=${form.id}`, { name: form.name, coachId: form.coachId, image: form.image, price: form.price, duration: form.duration, difficulty: form.difficulty, type: form.type, description: form.description })
    } else {
      await request.post('/course/addCourse', { name: form.name, coachId: form.coachId, image: form.image, price: form.price, duration: form.duration, difficulty: form.difficulty, type: form.type, description: form.description })
    }
    ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { console.error(e) }
  finally { submitLoading.value = false }
}

onMounted(() => loadData())
</script>

<style scoped>
.manage-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-card {
  margin-bottom: 0;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

/* 增加下拉框宽度以改善可读性 */
.search-form :deep(.el-select) {
  width: 200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
