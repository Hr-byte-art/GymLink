import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getAnnouncementList,
  getAnnouncementDetail,
  type Announcement,
  type AnnouncementQueryParams
} from '@/api/announcement'
import type { Page } from '@/api/types'

export const usePostStore = defineStore('post', () => {
  // 状态 - Posts对应后端的Announcements
  const posts = ref<Announcement[]>([])
  const postDetail = ref<Announcement | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 计算属性
  const hasPosts = computed(() => posts.value.length > 0)
  const hasPostDetail = computed(() => postDetail.value !== null)

  // 获取所有公告/帖子
  const fetchPosts = async (params: AnnouncementQueryParams = {}) => {
    loading.value = true
    error.value = null

    try {
      const response: Page<Announcement> = await getAnnouncementList(params)
      posts.value = response.records || []
      total.value = response.total || 0
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取帖子列表失败'
      posts.value = []
      total.value = 0
      console.error('获取帖子列表失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 获取帖子详情
  const fetchPostDetail = async (id: number) => {
    loading.value = true
    error.value = null

    try {
      postDetail.value = await getAnnouncementDetail(id)
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取帖子详情失败'
      postDetail.value = null
      console.error('获取帖子详情失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 清除帖子详情
  const clearPostDetail = () => {
    postDetail.value = null
  }

  // 搜索帖子
  const searchPosts = async (keyword: string) => {
    await fetchPosts({ title: keyword })
  }

  // 重置状态
  const resetState = () => {
    posts.value = []
    postDetail.value = null
    total.value = 0
    loading.value = false
    error.value = null
  }

  return {
    // 状态
    posts,
    postDetail,
    total,
    loading,
    error,

    // 计算属性
    hasPosts,
    hasPostDetail,

    // 方法
    fetchPosts,
    fetchPostDetail,
    clearPostDetail,
    searchPosts,
    resetState
  }
})