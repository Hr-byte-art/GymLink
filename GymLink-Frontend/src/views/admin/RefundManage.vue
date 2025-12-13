<template>
  <AdminLayout>
    <div class="manage-container">
      <el-card class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="学员姓名">
            <el-input v-model="searchForm.studentName" placeholder="请输入学员姓名" clearable />
          </el-form-item>
          <el-form-item label="课程名称">
            <el-input v-model="searchForm.courseName" placeholder="请输入课程名称" clearable />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="退款申请中" :value="3" />
              <el-option label="已支付" :value="1" />
              <el-option label="已退款" :value="2" />
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
            <span>退款申请列表</span>
            <el-tag type="warning">待处理: {{ pendingCount }}</el-tag>
          </div>
        </template>
        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column prop="orderId" label="订单ID" width="100" />
          <el-table-column prop="orderNo" label="订单号" width="200" />
          <el-table-column prop="studentName" label="学员" width="100" />
          <el-table-column prop="courseName" label="课程名称" min-width="150" />
          <el-table-column prop="coachName" label="教练" width="100" />
          <el-table-column label="金额" width="100">
            <template #default="{ row }">
              <span class="price">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="购买时间" width="170">
            <template #default="{ row }">{{ formatDate(row.purchaseTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 3">
                <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
                <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
              </template>
              <el-tag v-else type="info" size="small">已处理</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="pagination" v-model:current-page="pagination.current" v-model:page-size="pagination.pageSize"
          :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="loadData" />
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminLayout from '@/components/AdminLayout.vue'
import request from '@/utils/request'

interface RefundOrder {
  orderId: number
  orderNo: string
  courseId: number
  courseName: string
  courseImage: string
  coachId: number
  coachName: string
  studentId: number
  studentName: string
  price: number
  purchaseTime: string
  status: number
}

const loading = ref(false)
const tableData = ref<RefundOrder[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  studentName: '',
  courseName: '',
  status: 3 as number | undefined // 默认显示退款申请中
})

const pendingCount = computed(() => {
  return tableData.value.filter(item => item.status === 3).length
})

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'success', 2: 'info', 3: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '已支付', 2: '已退款', 3: '退款申请中' }
  return map[status] || '未知'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/student/getRefundOrders', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      status: searchForm.status,
      courseName: searchForm.courseName || undefined
    }) as { records: RefundOrder[]; total: number }
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载退款订单失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetSearch = () => {
  searchForm.studentName = ''
  searchForm.courseName = ''
  searchForm.status = 3
  handleSearch()
}

const handleApprove = async (row: RefundOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定通过该退款申请吗？将退还 ¥${row.price} 到学员 ${row.studentName} 的账户余额。`,
      '审核通过',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await request.get('/student/approveRefund', { params: { orderId: row.orderId } })
    ElMessage.success('退款已通过，金额已返还到学员账户')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      // 错误已在拦截器处理
    }
  }
}

const handleReject = async (row: RefundOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定拒绝该退款申请吗？订单将恢复为已支付状态。`,
      '拒绝退款',
      { confirmButtonText: '确定拒绝', cancelButtonText: '取消', type: 'warning' }
    )
    await request.get('/student/rejectRefund', { params: { orderId: row.orderId } })
    ElMessage.success('已拒绝退款申请')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      // 错误已在拦截器处理
    }
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

.search-card {
  padding: 10px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.table-card {
  flex: 1;
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

.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>
