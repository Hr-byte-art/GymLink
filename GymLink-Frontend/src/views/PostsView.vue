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
            <el-select v-model="selectedCategory" placeholder="选择分类" clearable @change="handleCategoryChange"
              class="category-select">
              <el-option v-for="category in postStore.categories" :key="category" :label="category" :value="category" />
            </el-select>
          </div>
          <div class="filter-right">
            <el-input v-model="searchQuery" placeholder="搜索帖子..." @input="handleSearch" class="search-input" clearable>
              <template #prefix>
                <el-icon>
                  <Search />
                </el-icon>
              </template>
            </el-input>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div v-if="postStore.loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="postStore.error" class="error-container">
          <el-result icon="error" title="加载失败" :sub-title="postStore.error">
            <template #extra>
              <el-button type="primary" @click="postStore.fetchPosts">重新加载</el-button>
            </template>
          </el-result>
        </div>

        <div v-else-if="filteredPosts.length === 0" class="empty-container">
          <el-empty description="暂无相关帖子" />
        </div>

        <div v-else class="posts-list">
          <div v-for="post in filteredPosts" :key="post.id" class="post-card" @click="goToPostDetail(post.id)">
            <div class="post-header">
              <div class="author-info">
                <el-avatar :src="post.userAvatar" :size="40" />
                <div class="author-details">
                  <div class="author-name">{{ post.userName }}</div>
                  <div class="post-time">{{ formatTime(post.createdAt) }}</div>
                </div>
              </div>
              <div class="post-category">{{ post.category }}</div>
            </div>

            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-content">{{ post.content.substring(0, 150) }}{{ post.content.length > 150 ? '...' : '' }}</p>

            <div v-if="post.images && post.images.length > 0" class="post-images">
              <div class="images-grid">
                <img v-for="(image, index) in post.images.slice(0, 3)" :key="index" :src="image" alt="帖子图片"
                  class="post-image" />
                <div v-if="post.images.length > 3" class="more-images">
                  +{{ post.images.length - 3 }}
                </div>
              </div>
            </div>

            <div class="post-tags">
              <el-tag v-for="tag in post.tags" :key="tag" size="small" class="tag">
                {{ tag }}
              </el-tag>
            </div>

            <div class="post-stats">
              <div class="stat-item">
                <el-icon>
                  <View />
                </el-icon>
                <span>{{ post.views }}</span>
              </div>
              <div class="stat-item">
                <el-icon>
                  <ChatLineSquare />
                </el-icon>
                <span>{{ post.comments }}</span>
              </div>
              <div class="stat-item like-item" @click.stop="toggleLike(post)">
                <span class="like-icon" :class="{ 'liked': post.isLiked }">👍</span>
                <span>{{ post.likes }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePostStore } from '@/stores/post'
import { Search, View, ChatLineSquare } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()
const postStore = usePostStore()

// 筛选和搜索状态
const selectedCategory = ref('')
const searchQuery = ref('')

// 计算筛选后的帖子列表
const filteredPosts = computed(() => {
  let result = postStore.posts

  // 按分类筛选
  if (selectedCategory.value) {
    result = postStore.getPostsByCategory(selectedCategory.value)
  }

  // 按搜索关键词筛选
  if (searchQuery.value) {
    result = postStore.searchPosts(searchQuery.value)
  }

  return result
})

// 处理分类变化
const handleCategoryChange = () => {
  // 分类变化时不需要额外操作，因为使用了computed属性
}

// 处理搜索
const handleSearch = () => {
  // 搜索时不需要额外操作，因为使用了computed属性
}

// 跳转到帖子详情
const goToPostDetail = (id: number) => {
  router.push(`/posts/${id}`)
}

// 切换点赞状态
const toggleLike = async (post: any) => {
  try {
    await postStore.likePost(post.id)
    if (post.isLiked) {
      ElMessage.success('点赞成功')
    } else {
      ElMessage.info('已取消点赞')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 格式化时间
const formatTime = (timeString: string) => {
  const date = new Date(timeString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 计算天数差
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    // 计算小时差
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      // 计算分钟差
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
    } else {
      return `${hours}小时前`
    }
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    // 返回具体日期
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  }
}

// 页面加载时获取帖子列表
onMounted(() => {
  postStore.fetchPosts()
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
  width: 150px;
}

.search-input {
  width: 300px;
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
  font-size: 16px;
  margin-right: 4px;
}

.like-icon.liked {
  color: #409EFF;
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