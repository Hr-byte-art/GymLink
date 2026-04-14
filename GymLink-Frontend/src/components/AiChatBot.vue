<template>
  <div class="ai-chatbot">
    <!-- 悬浮按钮 -->
    <div class="chat-trigger" @click="toggleChat" :class="{ active: isOpen }">
      <img src="/AI.png" alt="AI助手" class="ai-avatar" />
      <span class="pulse" v-if="!isOpen"></span>
    </div>

    <!-- 聊天窗口 -->
    <Transition name="chat-window">
      <div class="chat-window" v-if="isOpen">
        <div class="chat-header">
          <div class="header-info">
            <img src="/AI.png" alt="小健" class="header-avatar" />
            <div class="header-text">
              <span class="bot-name">小健</span>
              <span class="bot-status">AI健身助手</span>
            </div>
          </div>
          <el-button :icon="Close" circle size="small" @click="toggleChat" />
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message"
            :class="msg.role"
          >
            <img
              v-if="msg.role === 'assistant'"
              src="/AI.png"
              alt="小健"
              class="message-avatar"
            />
            <div class="message-content">
              <!-- 如果是流式返回中的最后一条空消息，显示 loading 动画 -->
              <div
                v-if="msg.role === 'assistant' && isLoading && index === messages.length - 1 && !msg.content"
                class="message-bubble typing"
              >
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
              <!-- 否则显示消息内容 -->
              <div v-else class="message-bubble" v-html="formatMessage(msg.content)"></div>
            </div>
            <img
              v-if="msg.role === 'user'"
              :src="userAvatar"
              alt="用户"
              class="message-avatar"
            />
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
            :disabled="isLoading"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            @click="sendMessage"
            :loading="isLoading"
            :disabled="!inputMessage.trim()"
          />
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { Close, Promotion } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { chatStream } from '@/api/ai'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const authStore = useAuthStore()
const isOpen = ref(false)
const inputMessage = ref('')
const messages = ref<Message[]>([])
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

// 用户头像
const userAvatar = computed(() => {
  return authStore.user?.avatar || '/avatar-placeholder.svg'
})

// 用户 ID
const userId = computed(() => {
  return authStore.user?.id || 'guest'
})

// 切换聊天窗口
const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value && messages.value.length === 0) {
    // 首次打开，添加欢迎消息
    messages.value.push({
      role: 'assistant',
      content: '你好！我是小健，你的 AI 健身助手 💪\n\n我可以帮你：\n• 推荐适合你的健身课程\n• 解答器材使用问题\n• 提供饮食建议\n• 指导系统功能使用\n\n有什么我可以帮你的吗？'
    })
  }
}

// 用于保存关闭流式请求的方法
let closeStream: (() => void) | null = null

// 发送消息（流式）
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message
  })
  inputMessage.value = ''
  isLoading.value = true

  // 添加一条空的助手消息用于流式填充
  const assistantMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: ''
  })

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 调用流式 AI 接口
  closeStream = chatStream(
    userId.value,
    message,
    // 消息回调：收到流式片段
    (text: string) => {
      const assistantMessage = messages.value[assistantMessageIndex]
      if (assistantMessage) {
        assistantMessage.content += text
      }
      scrollToBottom()
    },
    // 错误回调：发生错误
    (error: Error) => {
      console.error('AI 对话失败:', error)
      const assistantMessage = messages.value[assistantMessageIndex]
      if (assistantMessage && !assistantMessage.content) {
        assistantMessage.content = '抱歉，我暂时无法回复，请检查您的登录状态后重试 😅'
      }
      isLoading.value = false
    },
    // 完成回调：流式结束
    () => {
      isLoading.value = false
      closeStream = null
      nextTick(() => scrollToBottom())
    }
  )
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化消息（支持换行）
const formatMessage = (content: string) => {
  return content.replace(/\n/g, '<br>')
}

onUnmounted(() => {
  if (closeStream) {
    closeStream()
    closeStream = null
  }
})
</script>

<style scoped>
.ai-chatbot {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

/* 悬浮按钮 */
.chat-trigger {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1f3b76 0%, #14393d 100%);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
}

.chat-trigger:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.5);
}

.chat-trigger.active {
  transform: scale(0.9);
}

.ai-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  object-fit: cover;
}

.pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

/* 聊天窗口 */
.chat-window {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  padding: 16px;
  background: linear-gradient(135deg, #6e88db 0%, #99bf38 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.header-text {
  display: flex;
  flex-direction: column;
}

.bot-name {
  font-size: 16px;
  font-weight: 600;
}

.bot-status {
  font-size: 12px;
  opacity: 0.8;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f8f9fa;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message.user {
  justify-content: flex-end;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message.assistant .message-bubble {
  background: #fff;
  color: #333;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message.user .message-bubble {
  background: #fff;
  color: #333;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 4px;
  padding: 16px 20px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

/* 输入区域 */
.chat-input {
  padding: 16px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
}

.chat-input .el-input {
  flex: 1;
}

/* 动画 */
.chat-window-enter-active,
.chat-window-leave-active {
  transition: all 0.3s ease;
}

.chat-window-enter-from,
.chat-window-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

/* 响应式 */
@media (max-width: 480px) {
  .chat-window {
    width: calc(100vw - 32px);
    height: calc(100vh - 120px);
    right: -8px;
  }
}
</style>
