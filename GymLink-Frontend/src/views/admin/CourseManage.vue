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
              <el-option
                v-for="item in courseTypes"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <el-tooltip :content="item.description" placement="right" :show-after="300">
                  <span>{{ item.label }}</span>
                </el-tooltip>
              </el-option>
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
              <el-image :src="row.image" style="width: 60px; height: 40px" fit="cover">
                <template #error>
                  <div class="image-placeholder">暂无</div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="课程名称" width="150" />
          <el-table-column label="教练" width="120">
            <template #default="{ row }">
              {{ row.coachName || `ID: ${row.coachId}` }}
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="duration" label="时长(分钟)" width="100" />
          <el-table-column prop="difficulty" label="难度" width="80" />
          <el-table-column label="分类" width="140">
            <template #default="{ row }">{{ getCourseTypeName(row.type) }}</template>
          </el-table-column>
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
        <el-pagination
          class="pagination"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="授课教练" prop="coachId">
          <el-select
            v-model="form.coachId"
            filterable
            remote
            reserve-keyword
            placeholder="输入教练姓名搜索"
            :remote-method="searchCoaches"
            :loading="coachSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="coach in coachOptions"
              :key="coach.id"
              :label="`${coach.name} - ${getCoachSpecialtyName(coach.specialty) || '综合'}`"
              :value="coach.id"
            >
              <div class="coach-option">
                <span class="coach-name">{{ coach.name }}</span>
                <span class="coach-specialty">{{
                  getCoachSpecialtyName(coach.specialty) || '综合'
                }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="image-upload-container">
            <div class="image-preview">
              <el-image
                v-if="previewImageUrl"
                :src="previewImageUrl"
                style="width: 120px; height: 80px"
                fit="cover"
              />
              <el-image
                v-else-if="form.image"
                :src="form.image + '?t=' + imageTimestamp"
                style="width: 120px; height: 80px"
                fit="cover"
              />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="upload-actions">
              <input
                type="file"
                ref="imageInputRef"
                accept="image/*"
                style="display: none"
                @change="handleImageChange"
              />
              <el-button size="small" @click="triggerImageUpload" :loading="imageUploading">
                {{ form.image || previewImageUrl ? '更换图片' : '上传图片' }}
              </el-button>
              <div class="upload-tip">支持 jpg、png、webp 格式</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="form.duration" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" placeholder="请选择" style="width: 100%">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.type" placeholder="请选择分类" clearable style="width: 100%">
            <el-option
              v-for="item in courseTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <el-tooltip :content="item.description" placement="right" :show-after="300">
                <span>{{ item.label }}</span>
              </el-tooltip>
            </el-option>
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
import { courseTypeOptions, getCourseTypeName, getCoachSpecialtyName } from '@/constants/categories'

const loading = ref(false)
const submitLoading = ref(false)
const imageUploading = ref(false)
const coachSearchLoading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const imageInputRef = ref<HTMLInputElement>()
const imageTimestamp = ref(Date.now())
const coachOptions = ref<any[]>([])
const previewImageUrl = ref('') // 上传中的本地预览URL

// 使用统一的课程分类数据
const courseTypes = courseTypeOptions

const searchForm = reactive({ name: '', difficulty: '', type: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const form = reactive({
  id: null as string | number | null,
  name: '',
  coachId: null as string | number | null,
  image: '',
  price: 0,
  duration: 60,
  difficulty: '初级',
  type: '',
  description: '',
})

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  coachId: [{ required: true, message: '请选择授课教练', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

const dialogTitle = ref('添加课程')
const formatDate = (date: string) => (date ? new Date(date).toLocaleString('zh-CN') : '')

// 搜索教练
const searchCoaches = async (query: string) => {
  if (!query) {
    coachOptions.value = []
    return
  }
  coachSearchLoading.value = true
  try {
    const res = await request.post('/coach/ListCoach', {
      pageNum: 1,
      pageSize: 20,
      name: query
    })
    coachOptions.value = res.records || []
  } catch (e) {
    console.error('搜索教练失败:', e)
    coachOptions.value = []
  } finally {
    coachSearchLoading.value = false
  }
}

// 加载所有教练（用于初始化和编辑时显示）
const loadAllCoaches = async () => {
  try {
    const res = await request.post('/coach/ListCoach', {
      pageNum: 1,
      pageSize: 20 // 后端限制每页最多20条
    })
    coachOptions.value = res.records || []
  } catch (e) {
    console.error('加载教练列表失败:', e)
  }
}

const loadData = async (params?: any) => {
  loading.value = true
  try {
    const res = await request.post('/course/listCourse', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      difficulty: searchForm.difficulty || undefined,
      type: searchForm.type || undefined,
      ...params
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}
const resetSearch = () => {
  Object.assign(searchForm, { name: '', difficulty: '', type: '' })
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加课程'
  Object.assign(form, {
    id: null,
    name: '',
    coachId: null,
    image: '',
    price: 0,
    duration: 60,
    difficulty: '初级',
    type: '',
    description: ''
  })
  previewImageUrl.value = ''
  coachOptions.value = []
  imageTimestamp.value = Date.now()
  loadAllCoaches()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑课程'
  // 确保所有字段都正确复制
  Object.assign(form, {
    id: row.id,
    name: row.name || '',
    coachId: row.coachId || null,
    image: row.image || '',
    price: row.price || 0,
    duration: row.duration || 60,
    difficulty: row.difficulty || '初级',
    type: row.type || '',
    description: row.description || ''
  })
  previewImageUrl.value = ''
  imageTimestamp.value = Date.now()
  loadAllCoaches()
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？', '提示', { type: 'warning' })
    await request.post('/course/deleteCourse', null, { params: { id: row.id } })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

// 触发文件选择
const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

// 处理图片选择 - 选择后立即上传
const handleImageChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('请选择 jpg、png 或 webp 格式的图片')
    return
  }

  // 验证文件大小（最大5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  // 先显示本地预览
  previewImageUrl.value = URL.createObjectURL(file)
  imageUploading.value = true

  try {
    // 立即上传到通用图片接口
    const formData = new FormData()
    formData.append('image', file)
    formData.append('type', 'course')

    const imageUrl = (await request.post('/file/uploadImage', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })) as string

    // 上传成功，保存URL
    form.image = imageUrl
    imageTimestamp.value = Date.now()
    // 清除本地预览，使用服务器URL
    if (previewImageUrl.value) {
      URL.revokeObjectURL(previewImageUrl.value)
      previewImageUrl.value = ''
    }
    ElMessage.success('图片上传成功')
  } catch (e) {
    console.error('图片上传失败:', e)
    // 上传失败，清除预览
    if (previewImageUrl.value) {
      URL.revokeObjectURL(previewImageUrl.value)
      previewImageUrl.value = ''
    }
  } finally {
    imageUploading.value = false
    if (imageInputRef.value) imageInputRef.value.value = ''
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await request.post(`/course/updateCourse?id=${form.id}`, {
        name: form.name,
        coachId: form.coachId,
        image: form.image,
        price: form.price,
        duration: form.duration,
        difficulty: form.difficulty,
        type: form.type,
        description: form.description
      })
      ElMessage.success('更新成功')
    } else {
      // 新建：图片已经上传好了，直接使用 form.image
      await request.post('/course/addCourse', {
        name: form.name,
        coachId: form.coachId,
        image: form.image, // 图片URL已在选择时上传获得
        price: form.price,
        duration: form.duration,
        difficulty: form.difficulty,
        type: form.type,
        description: form.description
      })
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
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

.image-placeholder {
  width: 60px;
  height: 40px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
}

.image-upload-container {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.image-preview {
  width: 120px;
  height: 80px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
}

.upload-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

/* 教练选择下拉框样式 */
.coach-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.coach-name {
  font-weight: 500;
}

.coach-specialty {
  font-size: 12px;
  color: #909399;
}
</style>
