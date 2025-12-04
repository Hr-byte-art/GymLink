<template>
  <AppLayout>
    <div class="post-detail-view">
      <div class="container">
        <div class="back-button">
          <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        </div>

        <div v-if="postStore.loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>

        <div v-else-if="postStore.error" class="error-container">
          <el-result icon="error" title="加载失败" :sub-title="postStore.error">
            <template #extra>
              <el-button type="primary" @click="loadPost">重新加载</el-button>
            </template>
          </el-result>
        </div>

        <div v-else-if="post" class="post-detail">
          <!-- 帖子头部 -->
          <div class="post-header">
            <div class="author-info">
              <el-avatar :src="post.userAvatar" :size="50" />
              <div class="author-details">
                <div class="author-name">{{ post.userName }}</div>
                <div class="post-meta">
                  <span class="post-category">{{ post.category }}</span>
                  <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 帖子标题 -->
          <h1 class="post-title">{{ post.title }}</h1>

          <!-- 帖子标签 -->
          <div class="post-tags">
            <el-tag v-for="tag in post.tags" :key="tag" size="small" class="tag">
              {{ tag }}
            </el-tag>
          </div>

          <!-- 帖子内容 -->
          <div class="post-content">
            <p v-for="(paragraph, index) in contentParagraphs" :key="index">
              {{ paragraph }}
            </p>
          </div>

          <!-- 帖子图片 -->
          <div v-if="post.images && post.images.length > 0" class="post-images">
            <img v-for="(image, index) in post.images" :key="index" :src="image" alt="帖子图片" class="post-image" />
          </div>

          <!-- 帖子操作 -->
          <div class="post-actions">
            <div class="action-buttons">
              <div class="action-item" @click="toggleLike">
                <span class="like-icon">👍</span>
                <span>{{ post.likes }}</span>
              </div>
              <div class="action-item" @click="toggleDislike">
                <span class="dislike-icon">👎</span>
                <span>{{ post.dislikes }}</span>
              </div>
              <div class="action-item">
                <el-icon>
                  <ChatLineSquare />
                </el-icon>
                <span>{{ post.comments }}</span>
              </div>
              <div class="action-item">
                <el-icon>
                  <View />
                </el-icon>
                <span>{{ post.views }}</span>
              </div>
            </div>
          </div>

          <!-- 评论区 -->
          <div class="comments-section">
            <h2>评论 ({{ post.comments }})</h2>

            <!-- 发表评论 -->
            <div class="add-comment">
              <el-input v-model="newComment" type="textarea" placeholder="发表你的看法..." :rows="3" maxlength="500"
                show-word-limit />
              <div class="comment-actions">
                <el-button type="primary" @click="submitComment" :disabled="!newComment.trim()">
                  发表评论
                </el-button>
              </div>
            </div>

            <!-- 评论列表 -->
            <div v-if="postStore.comments.length === 0" class="no-comments">
              <el-empty description="暂无评论，快来发表第一条评论吧！" />
            </div>

            <div v-else class="comments-list">
              <div v-for="comment in postStore.comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <el-avatar :src="comment.userAvatar" :size="40" />
                  <div class="comment-info">
                    <div class="comment-author">{{ comment.userName }}</div>
                    <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
                  </div>
                  <div class="comment-like" @click="toggleCommentLike(comment.id)">
                    <span class="like-icon">👍</span>
                    <span>{{ comment.likes }}</span>
                  </div>
                </div>
                <div class="comment-content">{{ comment.content }}</div>

                <!-- 回复按钮 -->
                <div class="comment-actions">
                  <el-button text type="primary" @click="toggleReplyInput(comment.id)">
                    回复
                  </el-button>
                </div>

                <!-- 回复输入框 -->
                <div v-if="replyInputs[comment.id]" class="reply-input">
                  <el-input v-model="replyTexts[comment.id]" type="textarea" placeholder="回复评论..." :rows="2"
                    maxlength="200" show-word-limit />
                  <div class="reply-actions">
                    <el-button size="small" @click="cancelReply(comment.id)">取消</el-button>
                    <el-button type="primary" size="small" @click="submitReply(comment.id)"
                      :disabled="!replyTexts[comment.id]?.trim()">
                      回复
                    </el-button>
                  </div>
                </div>

                <!-- 回复列表 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="reply-header">
                      <el-avatar :src="reply.userAvatar" :size="30" />
                      <div class="reply-info">
                        <div class="reply-author">{{ reply.userName }}</div>
                        <div class="reply-time">{{ formatTime(reply.createdAt) }}</div>
                      </div>
                      <div class="reply-like" @click="toggleCommentLike(reply.id)">
                        <span class="like-icon">👍</span>
                        <span>{{ reply.likes }}</span>
                      </div>
                    </div>
                    <div class="reply-content">{{ reply.content }}</div>
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
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePostStore } from '@/stores/post'
import { ArrowLeft, ChatLineSquare, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()

// 评论相关状态
const newComment = ref('')
const replyInputs = reactive<Record<number, boolean>>({})
const replyTexts = reactive<Record<number, string>>({})

// 获取当前帖子
const post = computed(() => postStore.postDetail)

// 将帖子内容按段落分割
const contentParagraphs = computed(() => {
  if (!post.value) return []
  return post.value.content.split('\n').filter(p => p.trim())
})

// 返回上一页
const goBack = () => {
  router.push('/posts')
}

// 加载帖子
const loadPost = () => {
  const id = Number(route.params.id)
  if (id) {
    postStore.fetchPostDetail(id)
  }
}

// 切换点赞状态
const toggleLike = async () => {
  if (!post.value) return

  try {
    await postStore.likePost(post.value.id)
    if (post.value.isLiked) {
      ElMessage.success('点赞成功')
    } else {
      ElMessage.info('已取消点赞')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 切换踩状态
const toggleDislike = async () => {
  if (!post.value) return

  try {
    await postStore.dislikePost(post.value.id)
    if (post.value.isDisliked) {
      ElMessage.success('操作成功')
    } else {
      ElMessage.info('已取消')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 切换评论点赞状态
const toggleCommentLike = async (commentId: number) => {
  try {
    await postStore.likeComment(commentId)
    ElMessage.success('操作成功')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 提交评论
const submitComment = async () => {
  if (!post.value || !newComment.value.trim()) return

  try {
    const result = await postStore.addComment(post.value.id, newComment.value)
    if (result) {
      newComment.value = ''
      ElMessage.success('评论发表成功')
    } else {
      ElMessage.error('评论发表失败，请重试')
    }
  } catch (error) {
    ElMessage.error('评论发表失败，请重试')
  }
}

// 切换回复输入框
const toggleReplyInput = (commentId: number) => {
  replyInputs[commentId] = !replyInputs[commentId]
  if (replyInputs[commentId]) {
    replyTexts[commentId] = ''
  }
}

// 取消回复
const cancelReply = (commentId: number) => {
  replyInputs[commentId] = false
  replyTexts[commentId] = ''
}

// 提交回复
const submitReply = async (commentId: number) => {
  if (!post.value || !replyTexts[commentId]?.trim()) return

  try {
    const result = await postStore.replyComment(post.value.id, commentId, replyTexts[commentId])
    if (result) {
      replyInputs[commentId] = false
      replyTexts[commentId] = ''
      ElMessage.success('回复发表成功')
    } else {
      ElMessage.error('回复发表失败，请重试')
    }
  } catch (error) {
    ElMessage.error('回复发表失败，请重试')
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

// 页面加载时获取帖子详情
onMounted(() => {
  loadPost()
})

// 页面卸载时清除帖子详情
onUnmounted(() => {
  postStore.clearPostDetail()
})
</script>

<style scoped>
.post-detail-view {
  padding: 2rem 0;
  min-height: calc(100vh - 140px);
}

.container {
  max-width: 900px;
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
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 2rem;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  font-size: 1.1rem;
  color: #2c3e50;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.post-category {
  background-color: #e3f2fd;
  color: #1976d2;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.post-time {
  font-size: 0.875rem;
  color: #7f8c8d;
}

.post-title {
  font-size: 2rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  line-height: 1.3;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.tag {
  background-color: #f0f2f5;
  color: #606266;
}

.post-content {
  font-size: 1.1rem;
  line-height: 1.8;
  color: #2c3e50;
  margin-bottom: 1.5rem;
}

.post-content p {
  margin-bottom: 1rem;
}

.post-content p:last-child {
  margin-bottom: 0;
}

.post-images {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.post-image {
  width: 100%;
  border-radius: 8px;
  object-fit: cover;
}

.post-actions {
  padding: 1.5rem 0;
  border-top: 1px solid #f0f2f5;
  border-bottom: 1px solid #f0f2f5;
  margin-bottom: 2rem;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 2rem;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #909399;
  font-size: 1rem;
  cursor: pointer;
  transition: color 0.3s;
}

.action-item:hover {
  color: #409eff;
}

.liked {
  color: #f56c6c !important;
}

.disliked {
  color: #909399 !important;
}

.like-icon {
  font-size: 18px;
  margin-right: 4px;
}

.like-icon.liked {
  color: #409EFF;
}

.comments-section {
  margin-top: 2rem;
}

.comments-section h2 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin-bottom: 1.5rem;
}

.add-comment {
  margin-bottom: 2rem;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

.no-comments {
  margin: 2rem 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comment-item {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.comment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.comment-author {
  font-weight: 600;
  color: #2c3e50;
}

.comment-time {
  font-size: 0.875rem;
  color: #7f8c8d;
}

.comment-like {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #909399;
  font-size: 0.875rem;
  cursor: pointer;
}

.comment-like:hover {
  color: #409eff;
}

.comment-content {
  color: #2c3e50;
  line-height: 1.6;
  margin-bottom: 0.75rem;
}

.comment-actions {
  display: flex;
  gap: 0.5rem;
}

.reply-input {
  margin-top: 1rem;
  padding: 1rem;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.replies-list {
  margin-top: 1rem;
  padding-left: 1rem;
  border-left: 2px solid #e4e7ed;
}

.reply-item {
  padding: 1rem;
  background-color: white;
  border-radius: 8px;
  margin-bottom: 0.75rem;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.reply-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.reply-author {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.reply-time {
  font-size: 0.8rem;
  color: #7f8c8d;
}

.reply-like {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #909399;
  font-size: 0.8rem;
  cursor: pointer;
}

.reply-like:hover {
  color: #409eff;
}

.reply-content {
  color: #2c3e50;
  line-height: 1.5;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .post-detail {
    padding: 1.5rem;
  }

  .post-title {
    font-size: 1.5rem;
  }

  .action-buttons {
    gap: 1rem;
  }

  .action-item {
    font-size: 0.9rem;
  }
}
</style>