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

interface PostDetail {
  id: number | string
  title: string
  content: string
  userId?: number | string
  userName?: string
  userAvatar?: string
  userRole?: number
  createTime: string
  likeCount?: number
  viewCount?: number
  userReactionType?: number | null
}

interface CommentNode {
  id: number | string
  userId?: number | string
  userName?: string
  userAvatar?: string
  userRole?: number
  content: string
  createTime: string
  likeCount?: number
  isLiked?: boolean
  replies?: CommentNode[]
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 帖子数据
const post = ref<PostDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const userReaction = ref<number | null>(null) // 1=点赞 2=不喜欢

// 评论数据
const comments = ref<CommentNode[]>([])
const commentsLoading = ref(false)
const newComment = ref('')
const submitting = ref(false)
const replyingTo = ref<number | string | null>(null)
const replyContent = ref('')
const replyToUser = ref<CommentNode | null>(null)

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
    }) as PostDetail
    post.value = res
    // 使用后端返回的用户反应状态
    userReaction.value = res.userReactionType ?? null
    // 加载评论
    loadComments()
  } catch (err: unknown) {
    error.value = err instanceof Error ? err.message : '加载失败'
    console.error('加载帖子详情失败:', err)
  } finally {
    loading.value = false
  }
}

// 处理点赞/不喜欢
const handleReaction = async (reaction: number) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  if (!post.value) return

  const currentPost = post.value
  try {
    const userId = authStore.user?.id
    // 确保点赞数量为数字类型
    const currentLikeCount = Number(currentPost.likeCount) || 0

    if (userReaction.value === reaction) {
      // 取消反应
      await request.post('/experience/cancel', { experienceId: currentPost.id, userId })
      if (reaction === 1) {
        currentPost.likeCount = Math.max(0, currentLikeCount - 1)
      }
      userReaction.value = null
      ElMessage.info('已取消')
    } else {
      // 添加反应
      await request.post('/experience/userReaction', { experienceId: currentPost.id, reaction, userId })
      // 如果之前是点赞，现在改成不喜欢，点赞数-1
      if (userReaction.value === 1 && reaction === 0) {
        currentPost.likeCount = Math.max(0, currentLikeCount - 1)
      } else if (reaction === 1 && userReaction.value !== 1) {
        // 新增点赞
        currentPost.likeCount = currentLikeCount + 1
      }
      userReaction.value = reaction
      ElMessage.success(reaction === 1 ? '点赞成功' : '已标记不喜欢')
    }
  } catch {
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
    }) as CommentNode[]
    comments.value = res || []
  } catch (error) {
    console.error('加载评论失败:', error)
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

  const currentPost = post.value
  if (!currentPost) return
  submitting.value = true
  try {
    const userRole = authStore.user?.role === 'coach' ? 1 : 2
    await request.post('/comment/addComment', {
      content: newComment.value,
      experienceId: currentPost.id,
      userId: authStore.user?.id,
      userRole
    })
    ElMessage.success('评论成功')
    newComment.value = ''
    loadComments()
  } catch {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

// 显示回复输入框
const showReplyInput = (comment: CommentNode, reply?: CommentNode) => {
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
const submitReply = async (parentComment: CommentNode) => {
  if (!replyContent.value.trim()) return

  const currentPost = post.value
  if (!currentPost) return
  submitting.value = true
  try {
    const userRole = authStore.user?.role === 'coach' ? 1 : 2
    await request.post('/comment/addComment', {
      content: replyContent.value,
      experienceId: currentPost.id,
      userId: authStore.user?.id,
      userRole,
      parentId: parentComment.id,
      replyToUserId: replyToUser.value?.userId
    })
    ElMessage.success('回复成功')
    cancelReply()
    loadComments()
  } catch {
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

// 评论点赞
const handleCommentLike = async (comment: CommentNode) => {
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

onMounted(() => {
  loadPost()
})
</script>


<style scoped>
.post-detail-view{min-height:calc(100vh - 140px);padding:22px 0 34px;background:radial-gradient(circle at 12% 2%,rgba(255,235,206,.45) 0%,rgba(255,235,206,0) 25%),#fffaf5;}
.container{max-width:920px;margin:0 auto;padding:0 20px;}
.back-button{margin-bottom:14px;}
.loading-container,.error-container{margin:18px 0;}
.post-detail,.comments-section{background:#fff;border:1px solid #f4e8da;border-radius:18px;box-shadow:0 10px 24px rgba(248,146,43,.06);}
.post-detail{padding:24px;margin-bottom:14px;}
.post-header{margin-bottom:14px;padding-bottom:12px;border-bottom:1px solid #f4e8da;}
.author-info{display:flex;align-items:center;gap:12px;}
.author-details{display:flex;flex-direction:column;gap:3px;}
.author-name{font-size:18px;font-weight:700;color:#2a1f12;}
.post-meta{display:flex;align-items:center;gap:10px;}
.post-time{color:#8f7660;font-size:13px;}
.post-title{margin:0 0 14px;font-size:34px;line-height:1.25;color:#2a1f12;}
.post-content{margin-bottom:18px;color:#5c4532;line-height:1.9;white-space:pre-wrap;}
.post-actions{padding-top:12px;border-top:1px solid #f4e8da;}
.action-buttons{display:flex;flex-wrap:wrap;gap:10px;}
.action-item{display:inline-flex;align-items:center;gap:6px;padding:8px 12px;border-radius:999px;border:1px solid transparent;color:#8f7660;cursor:pointer;transition:all .2s ease;}
.action-item:hover{background:#fff4e7;}
.action-item.active{color:#c2410c;border-color:#fed7aa;background:#fff0df;}
.reaction-icon{width:18px;height:18px;}
.reaction-icon.liked,.comment-like-icon.liked{filter:invert(24%) sepia(95%) saturate(2100%) hue-rotate(354deg) brightness(97%) contrast(101%);}
.reaction-icon.disliked{filter:invert(43%) sepia(7%) saturate(1405%) hue-rotate(347deg) brightness(97%) contrast(94%);}
.comment-like-icon{width:13px;height:13px;margin-right:3px;vertical-align:middle;}
.comments-section{padding:22px;}
.section-title{margin:0 0 14px;color:#2a1f12;font-size:22px;font-weight:700;}
.comment-input-box{display:flex;gap:12px;margin-bottom:18px;padding-bottom:14px;border-bottom:1px solid #f4e8da;}
.input-wrapper{flex:1;display:flex;flex-direction:column;gap:10px;}
.input-wrapper .el-button{align-self:flex-end;}
.comments-loading,.no-comments{padding:14px 0;}
.comments-list{display:grid;gap:14px;}
.comment-item{border-bottom:1px solid #f4e8da;padding-bottom:14px;}
.comment-item:last-child{border-bottom:none;padding-bottom:0;}
.comment-main{display:flex;gap:10px;}
.comment-body{flex:1;}
.comment-header{display:flex;align-items:center;flex-wrap:wrap;gap:6px;margin-bottom:4px;}
.comment-author,.reply-author{color:#2a1f12;font-weight:700;}
.comment-time,.reply-time{color:#8f7660;font-size:12px;}
.comment-text,.reply-text{color:#5c4532;line-height:1.75;margin-bottom:6px;}
.comment-actions,.reply-actions{display:flex;gap:14px;}
.action-btn{color:#8f7660;font-size:13px;cursor:pointer;transition:color .2s ease;}
.action-btn:hover{color:#c2410c;}
.reply-input-box{margin-top:8px;padding:10px;border-radius:10px;background:#fff7ee;}
.replies-list{margin-left:26px;margin-top:8px;padding-left:10px;border-left:2px solid #f4e8da;}
.reply-item{display:flex;gap:8px;padding:8px 0;}
.reply-body{flex:1;}
.reply-header{display:flex;align-items:center;flex-wrap:wrap;gap:6px;margin-bottom:2px;}
@media (max-width:768px){.container{padding:0 14px;}.post-detail,.comments-section{padding:14px;border-radius:14px;}.post-title{font-size:28px;}.action-item{flex:1;justify-content:center;}.comment-input-box{align-items:flex-start;}.replies-list{margin-left:10px;}}
</style>
