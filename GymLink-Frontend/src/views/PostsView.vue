<template>
  <AppLayout>
    <div class="posts-view">
      <div class="container">
        <div class="page-header">
          <h1>经验分享</h1>
          <p>分享健身经验，交流心得体会</p>
        </div>

        <!-- 筛选和搜索区域 -->
        <div class="filter-section">
          <div class="filter-left">
            <el-select v-model="selectedUserRole" placeholder="选择发布者" clearable @change="handleCategoryChange"
              class="category-select">
              <el-option label="教练" :value="1" />
              <el-option label="学员" :value="2" />
            </el-select>
          </div>
          <div class="filter-right">
            <el-input v-model="searchQuery" placeholder="搜索帖子..." class="search-input" clearable @keyup.enter="handleSearch">
              <template #prefix>
                <el-icon>
                  <Search />
                </el-icon>
              </template>
            </el-input>
            <el-button @click="handleSearch">搜索</el-button>
            <el-button type="primary" @click="handlePublish" class="publish-btn">
              <el-icon><Edit /></el-icon>
              发表帖子
            </el-button>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="error" class="error-container">
          <el-result icon="error" title="加载失败" :sub-title="error">
            <template #extra>
              <el-button type="primary" @click="loadPosts">重新加载</el-button>
            </template>
          </el-result>
        </div>

        <div v-else-if="filteredPosts.length === 0" class="empty-container">
          <el-empty description="暂无相关帖子">
            <el-button type="primary" @click="handlePublish">发表第一篇帖子</el-button>
          </el-empty>
        </div>

        <div v-else class="posts-list">
          <div v-for="post in filteredPosts" :key="post.id" class="post-card" @click="goToPostDetail(post.id)">
            <div class="post-header">
              <div class="author-info">
                <el-avatar :size="40" :src="post.userAvatar" />
                <div class="author-details">
                  <div class="author-name">{{ post.userName || '用户' + post.userId }}</div>
                  <div class="post-time">{{ formatTime(post.createTime) }}</div>
                </div>
              </div>
              <el-tag :type="post.userRole === 1 ? 'success' : 'primary'" size="small">
                {{ post.userRole === 1 ? '教练' : '学员' }}
              </el-tag>
            </div>

            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-content">{{ (post.content || '').substring(0, 150) }}{{ (post.content || '').length > 150 ? '...' : '' }}</p>

            <div class="post-stats">
              <div class="stat-item">
                <el-icon>
                  <View />
                </el-icon>
                <span>{{ post.viewCount || 0 }}</span>
              </div>
              <div class="stat-item like-item" @click.stop="toggleLike(post)">
                <img src="/like.svg" class="like-icon" :class="{ 'liked': post.isLiked }" alt="点赞" />
                <span>{{ post.likeCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <el-icon>
                  <ChatLineSquare />
                </el-icon>
                <span>{{ post.commentCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发表帖子对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发表帖子" width="600px">
      <el-form :model="publishForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="publishForm.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="publishForm.content" type="textarea" :rows="8" placeholder="分享你的健身经验..." maxlength="5000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost" :loading="publishLoading">发表</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Search, View, ChatLineSquare, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import request from '@/utils/request'

const router = useRouter()
const authStore = useAuthStore()

// 帖子列表数据
const posts = ref<any[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const pagination = ref({ current: 1, pageSize: 20, total: 0 })

// 筛选和搜索状态
const searchQuery = ref('')
const selectedUserRole = ref<number | null>(null)

// 发表帖子相关
const publishDialogVisible = ref(false)
const publishLoading = ref(false)
const publishForm = ref({ title: '', content: '' })

// 加载帖子列表
const loadPosts = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await request.post('/experience/listExperience', {
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      title: searchQuery.value || undefined,
      userRole: selectedUserRole.value || undefined
    })
    posts.value = res.records || []
    pagination.value.total = res.total || 0
  } catch (e) {
    error.value = '加载失败'
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 计算筛选后的帖子列表
const filteredPosts = computed(() => posts.value)

// 处理搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadPosts()
}

// 处理分类变化
const handleCategoryChange = () => {
  pagination.value.current = 1
  loadPosts()
}

// 跳转到帖子详情
const goToPostDetail = (id: number) => {
  router.push(`/posts/${id}`)
}

// 切换点赞状态
const toggleLike = async (post: any) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    // 使用 User 表的 id，与后端 StpUtil.getLoginIdAsLong() 一致
    const userId = authStore.user?.id
    // 确保 likeCount 是数字类型
    const currentLikeCount = Number(post.likeCount) || 0
    if (post.isLiked) {
      await request.post('/experience/cancel', { experienceId: post.id, userId })
      post.isLiked = false
      post.likeCount = Math.max(0, currentLikeCount - 1)
      ElMessage.info('已取消点赞')
    } else {
      await request.post('/experience/userReaction', { experienceId: post.id, reaction: 1, userId })
      post.isLiked = true
      post.likeCount = currentLikeCount + 1
      ElMessage.success('点赞成功')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 格式化时间
const formatTime = (timeString: string) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  }
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 发表帖子
const handlePublish = () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再发表帖子')
    router.push('/auth')
    return
  }
  publishForm.value = { title: '', content: '' }
  publishDialogVisible.value = true
}

// 提交帖子
const submitPost = async () => {
  if (!publishForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!publishForm.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  publishLoading.value = true
  try {
    // 获取用户角色：1-教练 2-学员
    const userRole = authStore.user?.role === 'coach' ? 1 : 2
    // 使用 User 表的 id，全局唯一，避免教练/学员 ID 冲突
    const userId = authStore.user?.id
    
    console.log('发表帖子 - userId:', userId, 'typeof userId:', typeof userId, 'userRole:', userRole)
    console.log('完整用户信息:', JSON.stringify(authStore.user))
    
    if (!userId) {
      ElMessage.error('用户信息异常，请重新登录')
      return
    }
    
    // 直接传递 userId，后端会自动转换类型
    await request.post('/experience/addExperience', {
      title: publishForm.value.title,
      content: publishForm.value.content,
      userId: userId,
      userRole: userRole
    })
    ElMessage.success('发表成功')
    publishDialogVisible.value = false
    loadPosts() // 刷新列表
  } catch (e) {
    console.error('发表失败:', e)
    ElMessage.error('发表失败，请重试')
  } finally {
    publishLoading.value = false
  }
}

// 页面加载时获取帖子列表
onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.posts-view {
  padding: 2rem 0;
  min-height: calc(100vh - 140px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.page-header p {
  font-size: 1.1rem;
  color: #7f8c8d;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.category-select {
  width: 200px;
}

.search-input {
  width: 300px;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.publish-btn {
  white-space: nowrap;
}

.loading-container,
.error-container,
.empty-container {
  margin: 2rem 0;
}

.posts-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: #2c3e50;
}

.post-time {
  font-size: 0.875rem;
  color: #7f8c8d;
}

.post-category {
  background-color: #e3f2fd;
  color: #1976d2;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.post-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.75rem;
  line-height: 1.4;
}

.post-content {
  color: #7f8c8d;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.post-images {
  margin-bottom: 1rem;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
  position: relative;
}

.post-image {
  width: 100%;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.more-images {
  position: absolute;
  bottom: 0.5rem;
  right: 0.5rem;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag {
  background-color: #f0f2f5;
  color: #606266;
}

.like-icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
  vertical-align: middle;
  transition: filter 0.3s;
}

.like-icon.liked {
  /* 红色 */
  filter: invert(27%) sepia(95%) saturate(5000%) hue-rotate(355deg) brightness(95%) contrast(95%);
}

.post-stats {
  display: flex;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f2f5;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #909399;
  font-size: 0.875rem;
}

.like-item {
  cursor: pointer;
}

.liked {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left {
    justify-content: center;
  }

  .search-input {
    width: 100%;
  }

  .posts-list {
    grid-template-columns: 1fr;
  }
}
</style>