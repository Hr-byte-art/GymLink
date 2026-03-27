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

interface ExperiencePost {
  id: number | string
  userAvatar?: string
  userName?: string
  userId?: number | string
  userRole?: number
  title: string
  content?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  isLiked?: boolean
  createTime: string
}

interface ExperiencePageResponse {
  records?: ExperiencePost[]
  total?: number
}

// 帖子列表数据
const posts = ref<ExperiencePost[]>([])
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
    }) as ExperiencePageResponse
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
const goToPostDetail = (id: number | string) => {
  router.push(`/posts/${id}`)
}

// 切换点赞状态
const toggleLike = async (post: ExperiencePost) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    // 使用用户主表 ID，与后端登录 ID 保持一致
    const userId = authStore.user?.id
    // 确保点赞数量为数字类型
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
  } catch {
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
    // 使用用户主表 ID（全局唯一），避免教练/学员 ID 冲突
    const userId = authStore.user?.id


    if (!userId) {
      ElMessage.error('用户信息异常，请重新登录')
      return
    }

    // 直接传入用户标识，后端会自动转换类型
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
  --primary: #f97316;
  --primary-dark: #ea580c;
  --ink: #0f172a;
  --muted: #475569;
  --line: #e2e8f0;
  --surface: #ffffff;
  --soft: #f8fafc;
  min-height: calc(100vh - 140px);
  padding: 30px 0 70px;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  border-radius: 20px;
  padding: 42px 28px;
  margin-bottom: 24px;
  text-align: center;
  background:
    radial-gradient(circle at 10% 20%, rgba(249, 115, 22, 0.2), transparent 42%),
    linear-gradient(135deg, #0f172a 0%, #1e293b 48%, #334155 100%);
  color: #f8fafc;
}

.page-header h1 {
  margin: 0;
  font-size: clamp(32px, 5vw, 46px);
  font-weight: 800;
}

.page-header p {
  margin: 12px 0 0;
  color: #e2e8f0;
  font-size: 17px;
}

.filter-section {
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--surface);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.filter-left,
.filter-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-select {
  width: 180px;
}

.search-input {
  width: 280px;
}

.filter-section :deep(.el-input__wrapper),
.filter-section :deep(.el-select__wrapper) {
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  box-shadow: none;
}

.filter-section :deep(.el-input__wrapper.is-focus),
.filter-section :deep(.el-select__wrapper.is-focused) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.16);
}

.filter-right > .el-button {
  border-radius: 10px;
  min-height: 40px;
}

.filter-right > .el-button:last-child {
  border: none;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
}

.publish-btn {
  font-weight: 700;
  white-space: nowrap;
}

.loading-container,
.error-container,
.empty-container {
  margin: 24px 0;
}

.posts-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 18px;
}

.post-card {
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 18px;
  background: var(--surface);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-4px);
  border-color: rgba(249, 115, 22, 0.42);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.14);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-name {
  color: var(--ink);
  font-weight: 700;
}

.post-time {
  font-size: 13px;
  color: #64748b;
}

.post-title {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 20px;
  font-weight: 800;
  line-height: 1.4;
}

.post-content {
  margin: 0 0 14px;
  color: var(--muted);
  line-height: 1.7;
  min-height: 76px;
}

.like-icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
  vertical-align: middle;
  transition: filter 0.2s;
}

.like-icon.liked {
  filter: invert(27%) sepia(95%) saturate(5000%) hue-rotate(355deg) brightness(95%) contrast(95%);
}

.post-stats {
  border-top: 1px solid var(--line);
  padding-top: 12px;
  display: flex;
  gap: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  font-size: 13px;
}

.like-item {
  cursor: pointer;
}

.like-item:hover {
  color: #be123c;
}

@media (max-width: 900px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left,
  .filter-right {
    flex-wrap: wrap;
  }

  .category-select,
  .search-input {
    width: 100%;
  }

  .posts-list {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .post-card {
    transition: none;
  }

  .post-card:hover {
    transform: none;
  }
}
</style>
