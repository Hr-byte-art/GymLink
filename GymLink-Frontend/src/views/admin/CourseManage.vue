<template>
  <AdminLayout>
    <div class="manage-container">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="课程名称">
            <el-input v-model="searchForm.name" placeholder="请输入课程名称" clearable />
          </el-form-item>
          <el-form-item label="难度等级">
            <el-select v-model="searchForm.difficulty" placeholder="请选择" clearable>
              <el-option label="初级" value="初级" />
              <el-option label="中级" value="中级" />
              <el-option label="高级" value="高级" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程分类">
            <el-select v-model="searchForm.type" placeholder="请选择课程分类" clearable>
              <el-option v-for="item in courseTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="授课形式">
            <el-select v-model="searchForm.deliveryMode" placeholder="请选择授课形式" clearable>
              <el-option v-for="item in deliveryModeOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          <el-table-column prop="name" label="课程名称" min-width="160" />
          <el-table-column label="教练" width="160">
            <template #default="{ row }">{{ row.coachName || `ID: ${row.coachId}` }}</template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="duration" label="时长(分钟)" width="110" />
          <el-table-column prop="difficulty" label="难度" width="90" />
          <el-table-column label="分类" width="140">
            <template #default="{ row }">{{ getCourseTypeName(row.type) }}</template>
          </el-table-column>
          <el-table-column label="授课形式" width="100">
            <template #default="{ row }">{{ getDeliveryModeName(row.deliveryMode) }}</template>
          </el-table-column>
          <el-table-column prop="totalSessions" label="总课次" width="90" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button
                v-if="row.deliveryMode === 2"
                size="small"
                type="success"
                @click="openScheduleDialog(row)"
              >
                排期管理
              </el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
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
              :label="`${coach.name} - ${getCoachSpecialtyName(coach.specialty || '') || '综合'}`"
              :value="coach.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="image-upload-container">
            <div class="image-preview">
              <el-image v-if="previewImageUrl" :src="previewImageUrl" style="width: 120px; height: 80px" fit="cover" />
              <el-image v-else-if="form.image" :src="form.image + '?t=' + imageTimestamp" style="width: 120px; height: 80px" fit="cover" />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="upload-actions">
              <input ref="imageInputRef" type="file" accept="image/*" style="display: none" @change="handleImageChange" />
              <el-button size="small" @click="triggerImageUpload" :loading="imageUploading">
                {{ form.image || previewImageUrl ? '更换图片' : '上传图片' }}
              </el-button>
              <div class="upload-tip">支持 jpg、png、webp 格式，大小不超过 5MB</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input-number v-model="form.duration" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择" style="width: 100%">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程分类" prop="type">
          <el-select v-model="form.type" placeholder="请选择课程分类" clearable style="width: 100%">
            <el-option v-for="item in courseTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课形式" prop="deliveryMode">
          <el-select v-model="form.deliveryMode" placeholder="请选择授课形式" style="width: 100%">
            <el-option v-for="item in deliveryModeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="总课次" prop="totalSessions">
          <el-input-number v-model="form.totalSessions" :min="1" style="width: 100%" />
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

    <el-dialog v-model="scheduleDialogVisible" :title="scheduleDialogTitle" width="920px">
      <div class="schedule-topbar">
        <div class="schedule-course-name">{{ currentScheduleCourse?.name }}</div>
        <el-button type="primary" @click="openScheduleForm()">新增排期</el-button>
      </div>

      <el-table :data="scheduleList" v-loading="scheduleLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="开始时间" min-width="180">
          <template #default="{ row }">{{ formatDate(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" min-width="180">
          <template #default="{ row }">{{ formatDate(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="capacity" label="人数上限" width="100" />
        <el-table-column prop="bookedCount" label="已报名" width="90" />
        <el-table-column prop="remainingCapacity" label="剩余名额" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '可报名' : '已关闭' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openScheduleForm(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteSchedule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog
        v-model="scheduleFormVisible"
        :title="scheduleForm.id ? '编辑排期' : '新增排期'"
        width="520px"
        append-to-body
      >
        <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="100px">
          <div v-if="currentScheduleCourse?.duration" class="schedule-tip">
            当前课程总时长 {{ currentScheduleCourse.duration }} 分钟，允许累计排期不超过
            {{ getMaxScheduleDuration(currentScheduleCourse.duration) }} 分钟
            （含缓冲 {{ getScheduleBufferMinutes(currentScheduleCourse.duration) }} 分钟）。
            当前已排 {{ getScheduledDurationTotal(scheduleForm.id) }} 分钟，
            本次最多还能提交 {{ getRemainingSchedulableMinutes(scheduleForm.id) }} 分钟。
          </div>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="scheduleForm.startTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="scheduleForm.endTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="人数上限" prop="capacity">
            <el-input-number v-model="scheduleForm.capacity" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item v-if="scheduleForm.id" label="状态" prop="status">
            <el-select v-model="scheduleForm.status" style="width: 100%">
              <el-option label="可报名" :value="1" />
              <el-option label="已关闭" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="scheduleFormVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitSchedule" :loading="scheduleSubmitLoading">保存</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminLayout from '@/components/AdminLayout.vue'
import request from '@/utils/request'
import {
  addSchedule,
  deleteSchedule,
  listSchedule,
  updateSchedule,
  type Course,
  type CourseSchedule
} from '@/api/course'
import {
  courseTypeOptions,
  deliveryModeOptions,
  getCoachSpecialtyName,
  getCourseTypeName,
  getDeliveryModeName
} from '@/constants/categories'

type CoachOption = {
  id: number | string
  name?: string
  specialty?: string
}

const loading = ref(false)
const submitLoading = ref(false)
const imageUploading = ref(false)
const coachSearchLoading = ref(false)
const dialogVisible = ref(false)
const scheduleDialogVisible = ref(false)
const scheduleFormVisible = ref(false)
const scheduleLoading = ref(false)
const scheduleSubmitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const scheduleFormRef = ref()
const imageInputRef = ref<HTMLInputElement>()
const imageTimestamp = ref(Date.now())
const previewImageUrl = ref('')
const tableData = ref<Course[]>([])
const coachOptions = ref<CoachOption[]>([])
const scheduleList = ref<CourseSchedule[]>([])
const currentScheduleCourse = ref<Course | null>(null)

const searchForm = reactive({
  name: '',
  difficulty: '',
  type: '',
  deliveryMode: undefined as number | undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null as number | string | null,
  name: '',
  coachId: null as number | string | null,
  image: '',
  price: 0,
  duration: 60,
  difficulty: '初级',
  type: '',
  deliveryMode: 1,
  totalSessions: 1,
  description: ''
})

const scheduleForm = reactive({
  id: null as number | string | null,
  startTime: '',
  endTime: '',
  capacity: 20,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  coachId: [{ required: true, message: '请选择授课教练', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  type: [{ required: true, message: '请选择课程分类', trigger: 'change' }],
  deliveryMode: [{ required: true, message: '请选择授课形式', trigger: 'change' }],
  totalSessions: [{ required: true, message: '请输入总课次', trigger: 'blur' }]
}

const scheduleRules = {
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入人数上限', trigger: 'blur' }]
}

const dialogTitle = ref('添加课程')
const scheduleDialogTitle = ref('团课排期管理')

const formatDate = (date?: string) => (date ? new Date(date).toLocaleString('zh-CN') : '')
const getScheduleBufferMinutes = (duration?: number) => {
  if (!duration || duration <= 0) return 0
  return Math.floor(duration / 60) * 10
}
const getMaxScheduleDuration = (duration?: number) => {
  if (!duration || duration <= 0) return 0
  return duration + getScheduleBufferMinutes(duration)
}
const formatDateTimeValue = (date?: string) => {
  if (!date) return ''
  const current = new Date(date)
  if (Number.isNaN(current.getTime())) {
    return date.replace('T', ' ').split('.')[0]
  }

  const year = current.getFullYear()
  const month = String(current.getMonth() + 1).padStart(2, '0')
  const day = String(current.getDate()).padStart(2, '0')
  const hours = String(current.getHours()).padStart(2, '0')
  const minutes = String(current.getMinutes()).padStart(2, '0')
  const seconds = String(current.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
const getScheduleDurationMinutes = (startTime?: string, endTime?: string) => {
  if (!startTime || !endTime) return 0
  const start = new Date(startTime)
  const end = new Date(endTime)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end.getTime() <= start.getTime()) {
    return 0
  }
  return Math.round((end.getTime() - start.getTime()) / 60000)
}
const getScheduledDurationTotal = (excludeId?: number | string | null) => {
  return scheduleList.value.reduce((sum, item) => {
    if (excludeId != null && String(item.id) === String(excludeId)) {
      return sum
    }
    return sum + getScheduleDurationMinutes(item.startTime, item.endTime)
  }, 0)
}
const getRemainingSchedulableMinutes = (excludeId?: number | string | null) => {
  const totalDuration = currentScheduleCourse.value?.duration || 0
  const maxDuration = getMaxScheduleDuration(totalDuration)
  return Math.max(maxDuration - getScheduledDurationTotal(excludeId), 0)
}

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
    }) as { records?: CoachOption[] }
    coachOptions.value = res.records || []
  } finally {
    coachSearchLoading.value = false
  }
}

const loadAllCoaches = async () => {
  const res = await request.post('/coach/ListCoach', {
    pageNum: 1,
    pageSize: 20
  }) as { records?: CoachOption[] }
  coachOptions.value = res.records || []
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/course/listCourse', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      difficulty: searchForm.difficulty || undefined,
      type: searchForm.type || undefined,
      deliveryMode: searchForm.deliveryMode
    }) as { records?: Course[]; total?: number }

    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.difficulty = ''
  searchForm.type = ''
  searchForm.deliveryMode = undefined
  handleSearch()
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.coachId = null
  form.image = ''
  form.price = 0
  form.duration = 60
  form.difficulty = '初级'
  form.type = ''
  form.deliveryMode = 1
  form.totalSessions = 1
  form.description = ''
  previewImageUrl.value = ''
  imageTimestamp.value = Date.now()
}

const handleAdd = async () => {
  isEdit.value = false
  dialogTitle.value = '添加课程'
  resetForm()
  await loadAllCoaches()
  dialogVisible.value = true
}

const handleEdit = async (row: Course) => {
  isEdit.value = true
  dialogTitle.value = '编辑课程'
  form.id = row.id
  form.name = row.name
  form.coachId = row.coachId
  form.image = row.image || ''
  form.price = row.price || 0
  form.duration = row.duration || 60
  form.difficulty = row.difficulty || '初级'
  form.type = row.type || ''
  form.deliveryMode = row.deliveryMode || 1
  form.totalSessions = row.totalSessions || 1
  form.description = row.description || ''
  previewImageUrl.value = ''
  imageTimestamp.value = Date.now()
  await loadAllCoaches()
  dialogVisible.value = true
}

const handleDelete = async (row: Course) => {
  await ElMessageBox.confirm('确定要删除该课程吗？', '提示', { type: 'warning' })
  await request.post('/course/deleteCourse', null, { params: { id: String(row.id) } })
  ElMessage.success('删除成功')
  loadData()
}

const triggerImageUpload = () => imageInputRef.value?.click()

const handleImageChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    ElMessage.error('请选择 jpg、png 或 webp 格式的图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  previewImageUrl.value = URL.createObjectURL(file)
  imageUploading.value = true
  try {
    const formData = new FormData()
    formData.append('image', file)
    formData.append('type', 'course')
    const imageUrl = await request.post('/file/uploadImage', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }) as string

    form.image = imageUrl
    imageTimestamp.value = Date.now()
    URL.revokeObjectURL(previewImageUrl.value)
    previewImageUrl.value = ''
    ElMessage.success('图片上传成功')
  } finally {
    imageUploading.value = false
    target.value = ''
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  const payload = {
    name: form.name,
    coachId: form.coachId,
    image: form.image,
    price: form.price,
    duration: form.duration,
    difficulty: form.difficulty,
    type: form.type,
    deliveryMode: form.deliveryMode,
    totalSessions: form.totalSessions,
    description: form.description
  }

  try {
    if (isEdit.value && form.id) {
      await request.post('/course/updateCourse', payload, {
        params: { id: String(form.id) }
      })
      ElMessage.success('更新成功')
    } else {
      await request.post('/course/addCourse', payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const loadSchedules = async (courseId: number | string) => {
  scheduleLoading.value = true
  try {
    const res = await listSchedule({
      courseId,
      pageNum: 1,
      pageSize: 100
    })
    scheduleList.value = res.records || []
  } finally {
    scheduleLoading.value = false
  }
}

const openScheduleDialog = async (course: Course) => {
  currentScheduleCourse.value = course
  scheduleDialogTitle.value = `${course.name} - 团课排期管理`
  scheduleDialogVisible.value = true
  await loadSchedules(course.id)
}

const openScheduleForm = (row?: CourseSchedule) => {
  scheduleForm.id = row?.id || null
  scheduleForm.startTime = formatDateTimeValue(row?.startTime)
  scheduleForm.endTime = formatDateTimeValue(row?.endTime)
  scheduleForm.capacity = row?.capacity || 20
  scheduleForm.status = row?.status ?? 1
  scheduleFormVisible.value = true
}

const handleSubmitSchedule = async () => {
  const valid = await scheduleFormRef.value?.validate().catch(() => false)
  if (!valid || !currentScheduleCourse.value) return

  const courseDuration = currentScheduleCourse.value.duration || 0
  const maxScheduleDuration = getMaxScheduleDuration(courseDuration)
  const scheduleDuration = getScheduleDurationMinutes(scheduleForm.startTime, scheduleForm.endTime)
  const scheduledDurationTotal = getScheduledDurationTotal(scheduleForm.id)
  const nextTotalDuration = scheduledDurationTotal + scheduleDuration
  if (courseDuration > 0 && scheduleDuration > 0) {
    if (nextTotalDuration > maxScheduleDuration) {
      ElMessage.error(
        `当前课程排期累计总时长不能超过${maxScheduleDuration}分钟：课程${courseDuration}分钟 + 缓冲${getScheduleBufferMinutes(courseDuration)}分钟，当前已排${scheduledDurationTotal}分钟，本次提交后将达到${nextTotalDuration}分钟`
      )
      return
    }
  }

  scheduleSubmitLoading.value = true
  try {
    if (scheduleForm.id) {
      await updateSchedule(scheduleForm.id, {
        startTime: formatDateTimeValue(scheduleForm.startTime),
        endTime: formatDateTimeValue(scheduleForm.endTime),
        capacity: scheduleForm.capacity,
        status: scheduleForm.status
      })
      ElMessage.success('排期更新成功')
    } else {
      await addSchedule({
        courseId: currentScheduleCourse.value.id,
        startTime: formatDateTimeValue(scheduleForm.startTime),
        endTime: formatDateTimeValue(scheduleForm.endTime),
        capacity: scheduleForm.capacity
      })
      ElMessage.success('排期新增成功')
    }
    scheduleFormVisible.value = false
    loadSchedules(currentScheduleCourse.value.id)
  } finally {
    scheduleSubmitLoading.value = false
  }
}

const handleDeleteSchedule = async (row: CourseSchedule) => {
  await ElMessageBox.confirm('确定要删除该排期吗？', '提示', { type: 'warning' })
  await deleteSchedule(row.id)
  ElMessage.success('排期删除成功')
  if (currentScheduleCourse.value) {
    loadSchedules(currentScheduleCourse.value.id)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.manage-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.schedule-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.schedule-course-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.schedule-tip {
  margin-bottom: 16px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #9a3412;
  line-height: 1.6;
  font-size: 13px;
}
</style>
