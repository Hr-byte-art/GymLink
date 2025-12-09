<template>
  <AppLayout>
    <div class="post-detail-view">
      <div class="container">
        <div class="back-button">
          <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        </div>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>

        <div v-else-if="error" class="error-container">
          <el-result icon="error" title="加载失败" :sub-title="error">
            <template #extra>
              <el-button type="primary" @click="loadPost">重新加载</el-button>
            </template>
          </el-result>
        </div>

        <div v-else-if="post" class="post-detail">
          <!-- 帖子头部 -->
          <div class="post-header">
            <div class="author-info">
              <el-avatar :size="50" :src="post.userAvatar" />
              <div class="author-details">
                <div class="author-name">{{ post.userName || '用户' + post.userId }}</div>
                <div class="post-meta">
                  <el-tag :type="post.userRole === 1 ? 'success' : 'primary'" size="small">
                    {{ post.userRole === 1 ? '教练' : '学员' }}
                  </el-tag>
                  <span class="post-time">{{ formatTime(post.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 帖子标题 -->
          <h1 class="post-title">{{ post.title }}</h1>

          <!-- 帖子内容 -->
          <div class="post-content">{{ post.content }}</div>

          <!-- 帖子操作 -->
          <div class="post-actions">
            <div class="action-buttons">
              <div class="action-item" :class="{ active: userReaction === 1 }" @click="handleReaction(1)">
                <img src="/like.svg" class="reaction-icon" :class="{ liked: userReaction === 1 }" alt="点赞" />
                <span>{{ post.likeCount || 0 }}</span>
              </div>
              <div class="action-item" :class="{ active: userReaction === 0 }" @click="handleReaction(0)">
                <img src="/dislike.svg" class="reaction-icon" :class="{ disliked: userReaction === 0 }" alt="不喜欢" />
              </div>
              <div class="action-item">
                <el-icon><View /></el-icon>
                <span>{{ post.viewCount || 0 }}</span>
              </div>
              <div class="action-item">
                <el-icon><ChatLineSquare /></el-icon>
                <span>{{ getTotalCommentCount() }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 评论区 -->
        <div v-if="post" class="comments-section">
          <h3 class="section-title">评论 ({{ getTotalCommentCount() }})</h3>
          
          <!-- 发表评论 -->
          <div class="comment-input-box">
            <el-avatar :size="40" :src="authStore.user?.avatar" />
            <div class="input-wrapper">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="2"
                placeholder="发表你的看法..."
                maxlength="500"
                show-word-limit
              />
              <el-button type="primary" @click="submitComment" :loading="submitting" :disabled="!newComment.trim()">
                发表评论
              </el-button>
            </div>
          </div>

          <!-- 评论列表 -->
          <div v-if="commentsLoading" class="comments-loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="comments.length === 0" class="no-comments">
            <el-empty description="暂无评论，快来发表第一条评论吧" :image-size="80" />
          </div>
          <div v-else class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <!-- 主评论 -->
              <div class="comment-main">
                <el-avatar :size="40" :src="comment.userAvatar" />
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.userName || '用户' + comment.userId }}</span>
                    <el-tag v-if="comment.userRole === 1" type="success" size="small">教练</el-tag>
                    <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <span class="action-btn" @click="handleCommentLike(comment)">
                      <img src="/like.svg" class="comment-like-icon" :class="{ liked: comment.isLiked }" alt="点赞" />
                      {{ comment.likeCount || 0 }}
                    </span>
                    <span class="action-btn" @click="showReplyInput(comment)">回复</span>
                  </div>
                  
                  <!-- 回复输入框 -->
                  <div v-if="replyingTo === comment.id" class="reply-input-box">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="'回复 ' + (comment.userName || '用户')"
                      maxlength="500"
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button size="small" type="primary" @click="submitReply(comment)" :loading="submitting">回复</el-button>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 子评论/回复 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <el-avatar :size="32" :src="reply.userAvatar" />
                  <div class="reply-body">
                    <div class="reply-header">
                      <span class="reply-author">{{ reply.userName || '用户' + reply.userId }}</span>
                      <el-tag v-if="reply.userRole === 1" type="success" size="small">教练</el-tag>
                      <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-text">{{ reply.content }}</div>
                    <div class="reply-actions">
                      <span class="action-btn" @click="handleCommentLike(reply)">
                        <img src="/like.svg" class="comment-like-icon" :class="{ liked: reply.isLiked }" alt="点赞" />
                        {{ reply.likeCount || 0 }}
                      </span>
                      <span class="action-btn" @click="showReplyInput(comment, reply)">回复</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>


<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ArrowLeft, View, ChatLineSquare } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 帖子数据
const post = ref<any>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const userReaction = ref<number | null>(null) // 1=点赞 2=不喜欢

// 评论数据
const comments = ref<any[]>([])
const commentsLoading = ref(false)
const newComment = ref('')
const submitting = ref(false)
const replyingTo = ref<number | null>(null)
const replyContent = ref('')
const replyToUser = ref<any>(null)

// 返回上一页
const goBack = () => {
  router.back()
}

// 加载帖子详情
const loadPost = async () => {
  const postId = route.params.id
  if (!postId) {
    error.value = '帖子ID不存在'
    return
  }

  loading.value = true
  error.value = null
  try {
    const res = await request.get('/experience/getExperienceById', {
      params: { id: postId }
    })
    post.value = res
    // 使用后端返回的用户反应状态
    userReaction.value = res.userReactionType
    // 加载评论
    loadComments()
  } catch (e: any) {
    error.value = e.message || '加载失败'
    console.error('加载帖子详情失败:', e)
  } finally {
    loading.value = false
  }
}

// 加载用户对帖子的反应状态
const loadUserReaction = async () => {
  try {
    const res = await request.get('/experience/getCurrentUserReaction', {
      params: { experienceId: post.value.id, userId: authStore.user?.id }
    })
    userReaction.value = res
  } catch (e) {
    // 忽略错误
  }
}

// 处理点赞/不喜欢
const handleReaction = async (reaction: number) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  if (!post.value) return

  try {
    const userId = authStore.user?.id
    // 确保 likeCount 是数字类型
    const currentLikeCount = Number(post.value.likeCount) || 0
    
    if (userReaction.value === reaction) {
      // 取消反应
      await request.post('/experience/cancel', { experienceId: post.value.id, userId })
      if (reaction === 1) {
        post.value.likeCount = Math.max(0, currentLikeCount - 1)
      }
      userReaction.value = null
      ElMessage.info('已取消')
    } else {
      // 添加反应
      await request.post('/experience/userReaction', { experienceId: post.value.id, reaction, userId })
      // 如果之前是点赞，现在改成不喜欢，点赞数-1
      if (userReaction.value === 1 && reaction === 0) {
        post.value.likeCount = Math.max(0, currentLikeCount - 1)
      } else if (reaction === 1 && userReaction.value !== 1) {
        // 新增点赞
        post.value.likeCount = currentLikeCount + 1
      }
      userReaction.value = reaction
      ElMessage.success(reaction === 1 ? '点赞成功' : '已标记不喜欢')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 加载评论列表
const loadComments = async () => {
  if (!post.value) return
  commentsLoading.value = true
  try {
    const res = await request.get('/comment/getCommentTree', {
      params: { experienceId: post.value.id }
    })
    comments.value = res || []
  } catch (e) {
    console.error('加载评论失败:', e)
  } finally {
    commentsLoading.value = false
  }
}

// 获取总评论数
const getTotalCommentCount = () => {
  let count = comments.value.length
  comments.value.forEach(c => {
    if (c.replies) count += c.replies.length
  })
  return count
}

// 发表评论
const submitComment = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  if (!newComment.value.trim()) return

  submitting.value = true
  try {
    const userRole = authStore.user?.role === 'coach' ? 1 : 2
    await request.post('/comment/addComment', {
      content: newComment.value,
      experienceId: post.value.id,
      userId: authStore.user?.id,
      userRole
    })
    ElMessage.success('评论成功')
    newComment.value = ''
    loadComments()
  } catch (e) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

// 显示回复输入框
const showReplyInput = (comment: any, reply?: any) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  replyingTo.value = comment.id
  replyToUser.value = reply || comment
  replyContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
  replyToUser.value = null
}

// 提交回复
const submitReply = async (parentComment: any) => {
  if (!replyContent.value.trim()) return

  submitting.value = true
  try {
    const userRole = authStore.user?.role === 'coach' ? 1 : 2
    await request.post('/comment/addComment', {
      content: replyContent.value,
      experienceId: post.value.id,
      userId: authStore.user?.id,
      userRole,
      parentId: parentComment.id,
      replyToUserId: replyToUser.value?.userId
    })
    ElMessage.success('回复成功')
    cancelReply()
    loadComments()
  } catch (e) {
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

// 评论点赞
const handleCommentLike = async (comment: any) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (comment.isLiked) {
      await request.post('/comment/unlikeComment', null, { params: { id: comment.id } })
      comment.likeCount = Math.max(0, (comment.likeCount || 1) - 1)
      comment.isLiked = false
    } else {
      await request.post('/comment/likeComment', null, { params: { id: comment.id } })
      comment.likeCount = (comment.likeCount || 0) + 1
      comment.isLiked = true
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

onMounted(() => {
  loadPost()
})
</script>


<style scoped>
.post-detail-view {
  padding: 2rem 0;
  min-height: calc(100vh - 140px);
  background-color: #f5f7fa;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1rem;
}

.back-button {
  margin-bottom: 1.5rem;
}

.loading-container,
.error-container {
  margin: 2rem 0;
}

.post-detail {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 1.5rem;
}

.post-header {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f2f5;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.author-name {
  font-weight: 600;
  font-size: 1.1rem;
  color: #2c3e50;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.post-time {
  font-size: 0.875rem;
  color: #7f8c8d;
}

.post-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  line-height: 1.4;
}

.post-content {
  font-size: 1rem;
  line-height: 1.8;
  color: #34495e;
  margin-bottom: 2rem;
  white-space: pre-wrap;
}

.post-actions {
  padding-top: 1.5rem;
  border-top: 1px solid #f0f2f5;
}

.action-buttons {
  display: flex;
  gap: 2rem;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #909399;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  padding: 0.5rem 1rem;
  border-radius: 20px;
}

.action-item:hover {
  background-color: #f5f7fa;
}

.action-item.active {
  color: #409EFF;
  background-color: #ecf5ff;
}

.reaction-icon {
  width: 20px;
  height: 20px;
  transition: filter 0.3s;
}

.reaction-icon.liked {
  /* 红色 */
  filter: invert(27%) sepia(95%) saturate(5000%) hue-rotate(355deg) brightness(95%) contrast(95%);
}

.reaction-icon.disliked {
  /* 深黑色 */
  filter: invert(0%) sepia(0%) saturate(0%) hue-rotate(0deg) brightness(0%) contrast(100%);
}

.comment-like-icon {
  width: 14px;
  height: 14px;
  vertical-align: middle;
  margin-right: 4px;
  transition: filter 0.3s;
}

.comment-like-icon.liked {
  /* 红色 */
  filter: invert(27%) sepia(95%) saturate(5000%) hue-rotate(355deg) brightness(95%) contrast(95%);
}

/* 评论区样式 */
.comments-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1.5rem;
}

.comment-input-box {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0f2f5;
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.input-wrapper .el-button {
  align-self: flex-end;
}

.comments-loading,
.no-comments {
  padding: 2rem 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comment-item {
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 1.5rem;
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-main {
  display: flex;
  gap: 1rem;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.comment-author {
  font-weight: 600;
  color: #2c3e50;
}

.comment-time {
  font-size: 0.75rem;
  color: #909399;
}

.comment-text {
  color: #34495e;
  line-height: 1.6;
  margin-bottom: 0.75rem;
}

.comment-actions {
  display: flex;
  gap: 1rem;
}

.action-btn {
  font-size: 0.875rem;
  color: #909399;
  cursor: pointer;
  transition: color 0.3s;
}

.action-btn:hover {
  color: #409EFF;
}

.action-btn .liked {
  color: #409EFF;
}

.reply-input-box {
  margin-top: 1rem;
  padding: 1rem;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

/* 回复列表样式 */
.replies-list {
  margin-left: 3.5rem;
  margin-top: 1rem;
  padding-left: 1rem;
  border-left: 2px solid #f0f2f5;
}

.reply-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem 0;
}

.reply-item:first-child {
  padding-top: 0;
}

.reply-body {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.reply-author {
  font-weight: 600;
  font-size: 0.875rem;
  color: #2c3e50;
}

.reply-time {
  font-size: 0.75rem;
  color: #909399;
}

.reply-text {
  font-size: 0.875rem;
  color: #34495e;
  line-height: 1.5;
  margin-bottom: 0.5rem;
}

.reply-actions {
  display: flex;
  gap: 1rem;
}

@media (max-width: 768px) {
  .post-detail,
  .comments-section {
    padding: 1.5rem;
  }

  .post-title {
    font-size: 1.5rem;
  }

  .action-buttons {
    gap: 1rem;
  }

  .replies-list {
    margin-left: 2rem;
  }
}
</style>
