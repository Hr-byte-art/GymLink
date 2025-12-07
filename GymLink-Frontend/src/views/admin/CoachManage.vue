<template>
  <AdminLayout>
    <div class="manage-container">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
          </el-form-item>
          <el-form-item label="专业方向">
            <el-select v-model="searchForm.specialty" placeholder="请选择专业方向" clearable>
              <el-option v-for="coachType in coachTypes" :key="coachType.value" :label="coachType.label"
                :value="coachType.value">
                <el-tooltip :content="coachType.description" placement="right">
                  <span>{{ coachType.label }}</span>
                </el-tooltip>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="searchForm.gender" placeholder="请选择" clearable>
              <el-option label="男" :value="1" />
              <el-option label="女" :value="2" />
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
            <span>教练列表</span>
            <el-button type="primary" @click="handleAdd">添加教练</el-button>
          </div>
        </template>
        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="头像" width="80">
            <template #default="{ row }">
              <el-avatar :size="40" :src="row.avatar || '/avatar-placeholder.svg'" />
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column label="性别" width="80">
            <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="age" label="年龄" width="80" />
          <el-table-column label="专业方向" width="150">
            <template #default="{ row }">{{ getCoachSpecialtyName(row.specialty) }}</template>
          </el-table-column>
          <el-table-column prop="intro" label="简介" show-overflow-tooltip />
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
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="18" :max="100" />
        </el-form-item>
        <el-form-item label="专业方向" prop="specialty">
          <el-select v-model="form.specialty" placeholder="请选择专业方向">
            <el-option v-for="coachType in coachTypes" :key="coachType.value" :label="coachType.label"
              :value="coachType.value">
              <el-tooltip :content="coachType.description" placement="right">
                <span>{{ coachType.label }}</span>
              </el-tooltip>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="请输入简介" />
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
import { coachSpecialtyOptions, getCoachSpecialtyName } from '@/constants/categories'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const searchForm = reactive({ name: '', specialty: '', gender: null as number | null })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const form = reactive({ id: 0, username: '', password: '', name: '', gender: 1, phone: '', age: 25, specialty: '', intro: '' })

// 使用统一的教练类型数据
const coachTypes = coachSpecialtyOptions

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  specialty: [{ required: true, message: '请选择专业方向', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const dialogTitle = ref('添加教练')
const formatDate = (date: string) => date ? new Date(date).toLocaleString('zh-CN') : ''

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/coach/ListCoach', {
      pageNum: pagination.current, pageSize: pagination.pageSize,
      name: searchForm.name || undefined, specialty: searchForm.specialty || undefined, gender: searchForm.gender || undefined
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const resetSearch = () => { Object.assign(searchForm, { name: '', specialty: '', gender: null }); handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加教练'
  Object.assign(form, { id: 0, username: '', password: '', name: '', gender: 1, phone: '', age: 25, specialty: '', intro: '' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑教练'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该教练吗？', '提示', { type: 'warning' })
    await request.post('/coach/deleteCoach', null, { params: { id: row.id } })
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
      await request.post(`/coach/updateCoach?id=${form.id}`, { name: form.name, gender: form.gender, phone: form.phone, age: form.age, specialty: form.specialty, intro: form.intro })
    } else {
      await request.post('/coach/addCoach', { username: form.username, password: form.password, name: form.name, gender: form.gender, phone: form.phone, age: form.age, specialty: form.specialty, intro: form.intro })
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
