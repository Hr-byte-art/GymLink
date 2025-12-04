import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getCourseList, type Course, type CourseQueryParams } from '@/api/course'

export const useCourseStore = defineStore('course', () => {
  // 状态
  const courses = ref<Course[]>([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 计算属性
  const hasCourses = computed(() => courses.value.length > 0)

  // 获取课程列表
  const fetchCourses = async (params: CourseQueryParams = {}) => {
    loading.value = true
    error.value = null

    try {
      const response = await getCourseList(params)
      courses.value = response.records || []
      total.value = response.total || 0
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取课程列表失败'
      courses.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  // 重置状态
  const resetState = () => {
    courses.value = []
    total.value = 0
    loading.value = false
    error.value = null
  }

  return {
    courses,
    total,
    loading,
    error,
    hasCourses,
    fetchCourses,
    resetState
  }
})