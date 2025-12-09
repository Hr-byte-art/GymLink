<template>
  <AdminLayout>
    <div class="manage-container">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="searchForm.tags" placeholder="请选择标签" clearable filterable multiple>
              <el-option v-for="category in recipeCategories" :key="category.value" :label="category.label"
                :value="category.value">
                <el-tooltip :content="category.description" placement="right">
                  <div style="display: flex; justify-content: space-between; width: 100%;">
                    <span>{{ category.label }}</span>
                  </div>
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
            <span>菜谱列表</span>
            <el-button type="primary" @click="handleAdd">添加菜谱</el-button>
          </div>
        </template>
        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="封面" width="100">
            <template #default="{ row }">
              <el-image :src="row.coverImage" style="width: 60px; height: 40px" fit="cover">
                <template #error>
                  <div class="image-placeholder">暂无</div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" width="200" />
          <el-table-column prop="tags" label="标签" width="150" />
          <el-table-column prop="content" label="内容" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="image-upload-container">
            <div class="image-preview">
              <el-image v-if="form.coverImage" :src="form.coverImage + '?t=' + imageTimestamp"
                style="width: 120px; height: 80px" fit="cover" />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="upload-actions">
              <input type="file" ref="imageInputRef" accept="image/*" style="display: none"
                @change="handleImageChange" />
              <el-button size="small" @click="triggerImageUpload" :loading="imageUploading">
                {{ form.coverImage ? '更换图片' : '上传图片' }}
              </el-button>
              <div class="upload-tip">支持 jpg、png、webp 格式</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tags" placeholder="请选择标签" clearable filterable multiple>
            <el-option v-for="category in recipeCategories" :key="category.value" :label="category.label"
              :value="category.value">
              <el-tooltip :content="category.description" placement="right">
                <div style="display: flex; justify-content: space-between; width: 100%;">
                  <span>{{ category.label }}</span>
                </div>
              </el-tooltip>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入内容" />
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

// 食谱分类选项
const recipeCategories = [
  { value: '增肌食谱', label: '增肌食谱（Muscle Gain / Bulking）', description: '高热量、高蛋白、适量碳水，支持肌肉合成与训练恢复' },
  { value: '减脂食谱', label: '减脂食谱（Fat Loss / Cutting）', description: '热量赤字、高蛋白、中低碳水，保留肌肉同时减少体脂' },
  { value: '维持期食谱', label: '维持期食谱（Maintenance）', description: '热量平衡，营养均衡，用于体重/体脂稳定阶段' },
  { value: '高蛋白食谱', label: '高蛋白食谱（High-Protein Diet）', description: '蛋白质占比显著提高（≥30%总热量），适用于各类训练者' },
  { value: '低碳食谱', label: '低碳/生酮食谱（Low-Carb / Keto）', description: '极低碳水（<50g/天）、高脂肪、中高蛋白，用于特定减脂或代谢需求' },
  { value: '力量训练专用', label: '力量训练专用食谱', description: '强调训练前后营养时机（如练前碳水+练后蛋白），提升表现与恢复' },
  { value: '耐力训练专用', label: '耐力训练专用食谱', description: '高碳水储备（如赛前糖原填充），支持长时间有氧运动' },
  { value: '素食健身食谱', label: '素食/纯素食健身食谱（Vegetarian / Vegan）', description: '通过植物蛋白（豆类、藜麦、豆腐等）满足蛋白需求，注重氨基酸互补' },
  { value: '清单饮食', label: '清单饮食（Clean Eating）', description: '以天然、少加工食材为主，控制添加糖与反式脂肪，强调食物质量' },
  { value: '周期化食谱', label: '分阶段/周期化食谱（Periodized Nutrition）', description: '根据训练周期（如休赛期、备赛期）动态调整热量与宏量营养素比例' }
]

const searchForm = reactive({ title: '', tags: [] as string[] })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const form = reactive({ id: 0, title: '', coverImage: '', tags: [] as string[], content: '', adminId: 1 })

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const dialogTitle = ref('添加菜谱')
const formatDate = (date: string) => date ? new Date(date).toLocaleString('zh-CN') : ''

const loadData = async () => {
  loading.value = true
  try {
    // 将搜索标签数组转换为逗号分隔的字符串
    const tagsStr = searchForm.tags.length > 0 ? searchForm.tags.join(',') : undefined
    const res = await request.post('/recipe/listRecipe', {
      pageNum: pagination.current, pageSize: pagination.pageSize,
      title: searchForm.title || undefined, tags: tagsStr
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const resetSearch = () => { Object.assign(searchForm, { title: '', tags: [] }); handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加菜谱'
  Object.assign(form, { id: 0, title: '', coverImage: '', tags: [], content: '', adminId: 1 })
  imageTimestamp.value = Date.now()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑菜谱'
  // 将标签字符串转换为数组
  const tagsArray = row.tags ? row.tags.split(',').filter((t: string) => t.trim()) : []
  Object.assign(form, { ...row, tags: tagsArray })
  imageTimestamp.value = Date.now()
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜谱吗？', '提示', { type: 'warning' })
    await request.post('/recipe/deleteRecipe', null, { params: { id: row.id } })
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

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('请选择 jpg、png 或 webp 格式的图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  imageUploading.value = true
  try {
    const formData = new FormData()
    formData.append('image', file)
    formData.append('recipeId', String(form.id))

    const imageUrl = await request.post('/recipe/updateRecipeImage', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }) as string

    form.coverImage = imageUrl
    imageTimestamp.value = Date.now()
    ElMessage.success('图片上传成功')
  } catch (e) {
    console.error('图片上传失败:', e)
    ElMessage.error('图片上传失败')
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
    // 将标签数组转换为逗号分隔的字符串
    const tagsStr = Array.isArray(form.tags) ? form.tags.join(',') : form.tags
    if (isEdit.value) {
      await request.post(`/recipe/updateRecipe?id=${form.id}`, {
        title: form.title, coverImage: form.coverImage, tags: tagsStr, content: form.content
      })
    } else {
      await request.post('/recipe/addRecipe', {
        title: form.title, coverImage: form.coverImage, tags: tagsStr, content: form.content, adminId: form.adminId
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
</style>
