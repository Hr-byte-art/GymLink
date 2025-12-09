<template>
  <AdminLayout>
    <div class="manage-container">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="器材名称">
            <el-input v-model="searchForm.name" placeholder="请输入器材名称" clearable />
          </el-form-item>
          <el-form-item label="位置">
            <el-input v-model="searchForm.location" placeholder="请输入位置" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="正常" :value="1" />
              <el-option label="维护中" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-cascader
              v-model="searchForm.typeArray"
              :options="categoryOptions"
              :props="{ expandTrigger: 'hover' }"
              clearable
              placeholder="请选择类型"
              style="width: 200px"
              @change="handleTypeFilterChange"
            />
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
            <span>器材列表</span>
            <el-button type="primary" @click="handleAdd">添加器材</el-button>
          </div>
        </template>
        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="图片" width="100">
            <template #default="{ row }">
              <el-image :src="row.image" style="width: 60px; height: 40px" fit="cover">
                <template #error>
                  <div class="image-placeholder">暂无</div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="器材名称" width="150" />
          <el-table-column prop="location" label="位置" width="120" />
          <el-table-column prop="totalCount" label="数量" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '正常' : '维护中' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="150">
            <template #default="{ row }">{{ getTypeName(row.type) }}</template>
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
        <el-pagination class="pagination" v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize" :total="pagination.total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" @change="loadData" />
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="器材名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入器材名称" />
        </el-form-item>
        <el-form-item label="图片">
          <div class="image-upload-container">
            <div class="image-preview">
              <el-image v-if="form.image" :src="form.image + '?t=' + imageTimestamp" style="width: 120px; height: 80px"
                fit="cover" />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="upload-actions">
              <input type="file" ref="imageInputRef" accept="image/*" style="display: none"
                @change="handleImageChange" />
              <el-button size="small" @click="triggerImageUpload" :loading="imageUploading">
                {{ form.image ? '更换图片' : '上传图片' }}
              </el-button>
              <div class="upload-tip">支持 jpg、png、webp 格式</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.totalCount" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="正常" :value="1" />
            <el-option label="维护中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别">
          <el-cascader v-model="form.typeArray" :options="categoryOptions" :props="cascaderProps" clearable
            placeholder="请选择器材类别" style="width: 100%" @change="handleTypeChange" />
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
const imageUploading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const imageInputRef = ref<HTMLInputElement>()
const imageTimestamp = ref(Date.now())

const searchForm = reactive({ name: '', location: '', status: null as number | null, type: '', typeArray: [] as string[] })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const form = reactive({ id: 0, name: '', image: '', location: '', totalCount: 1, status: 1, description: '', type: '' as string, typeArray: [] as string[] })

// 器材类别选项
const categoryOptions = [
  {
    value: '1',
    label: '有氧健身器材',
    children: [
      { value: '1-1', label: '跑步机' },
      { value: '1-2', label: '椭圆机' },
      { value: '1-3', label: '动感单车' },
      { value: '1-4', label: '划船机' },
      { value: '1-5', label: '健身车' },
      { value: '1-6', label: '楼梯机' },
      { value: '1-7', label: '体适能运动机' }
    ]
  },
  {
    value: '2',
    label: '力量训练器材',
    children: [
      { value: '2-1', label: '固定器械' },
      { value: '2-2', label: '自由重量器材' },
      { value: '2-3', label: '综合训练器材' }
    ]
  },
  { value: '3', label: '功能性训练器材' },
  { value: '4', label: '小型健身器械' },
  { value: '5', label: '康复与辅助器材' },
  { value: '6', label: '其他辅助设备' },
  { value: '7', label: '商用专用器材' },
  { value: '8', label: '家用专用器材' }
]

// 级联选择器属性
const cascaderProps = {
  expandTrigger: 'hover' as const,
  multiple: false
}

const rules = {
  name: [{ required: true, message: '请输入器材名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

const dialogTitle = ref('添加器材')
const formatDate = (date: string) => date ? new Date(date).toLocaleString('zh-CN') : ''

// 类型名称映射
const typeNameMap: Record<string, string> = {
  '1': '有氧健身器材', '1-1': '跑步机', '1-2': '椭圆机', '1-3': '动感单车', '1-4': '划船机', '1-5': '健身车', '1-6': '楼梯机', '1-7': '体适能运动机',
  '2': '力量训练器材', '2-1': '固定器械', '2-2': '自由重量器材', '2-3': '综合训练器材',
  '3': '功能性训练器材', '4': '小型健身器械', '5': '康复与辅助器材', '6': '其他辅助设备', '7': '商用专用器材', '8': '家用专用器材'
}
const getTypeName = (type: string) => typeNameMap[type] || type || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/equipment/listEquipment', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      location: searchForm.location || undefined,
      status: searchForm.status || undefined,
      type: searchForm.type || undefined
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

// 类型筛选变化
const handleTypeFilterChange = (value: string[]) => {
  if (value && value.length > 0) {
    searchForm.type = value[value.length - 1]
  } else {
    searchForm.type = ''
  }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const resetSearch = () => {
  Object.assign(searchForm, { name: '', location: '', status: null, type: '', typeArray: [] })
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加器材'
  Object.assign(form, { id: 0, name: '', image: '', location: '', totalCount: 1, status: 1, description: '', type: '', typeArray: [] })
  imageTimestamp.value = Date.now()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑器材'
  // 将 type 字符串转换为级联选择器数组格式
  const typeArray = row.type ? parseTypeToArray(row.type) : []
  Object.assign(form, { ...row, typeArray })
  imageTimestamp.value = Date.now()
  dialogVisible.value = true
}

// 将 type 字符串解析为级联选择器数组
const parseTypeToArray = (type: string): string[] => {
  if (!type) return []
  if (type.includes('-')) {
    // 如 "1-1" -> ["1", "1-1"]
    const mainType = type.split('-')[0]
    return [mainType, type]
  }
  // 如 "3" -> ["3"]
  return [type]
}

// 级联选择器变化时更新 type 字段
const handleTypeChange = (value: string[]) => {
  if (value && value.length > 0) {
    // 取最后一个值作为 type
    form.type = value[value.length - 1]
  } else {
    form.type = ''
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该器材吗？', '提示', { type: 'warning' })
    await request.post('/equipment/deleteEquipment', null, { params: { id: row.id } })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

// 触发文件选择
const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

// 处理图片选择
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

  imageUploading.value = true
  try {
    const formData = new FormData()
    formData.append('image', file)
    formData.append('equipmentId', String(form.id))

    const imageUrl = await request.post('/equipment/updateEquipmentImage', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }) as string

    form.image = imageUrl
    imageTimestamp.value = Date.now()
    ElMessage.success('图片上传成功')
  } catch (e) {
    console.error('图片上传失败:', e)
    ElMessage.error('图片上传失败')
  } finally {
    imageUploading.value = false
    // 清空input，允许重复选择同一文件
    if (imageInputRef.value) imageInputRef.value.value = ''
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await request.post(`/equipment/updateEquipment?id=${form.id}`, {
        name: form.name, image: form.image, location: form.location,
        totalCount: form.totalCount, status: form.status, description: form.description, type: form.type
      })
    } else {
      await request.post('/equipment/addEquipment', {
        name: form.name, image: form.image, location: form.location,
        totalCount: form.totalCount, status: form.status, description: form.description, type: form.type
      })
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

.dialog-form :deep(.el-select) {
  width: 100%;
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

/* 类别复选框样式 */
.el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.el-checkbox {
  margin-right: 0;
}

.el-checkbox.is-disabled {
  color: #ccc;
}
</style>
