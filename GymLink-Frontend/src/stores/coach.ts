import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import {
  getCoachList,
  getCoachDetail,
  bookCoach,
  cancelCoachBooking,
  type Coach,
  type CoachQueryParams,
  type BookCoachRequest
} from '@/api/coach'
import type { Page } from '@/api/types'

export const useCoachStore = defineStore('coach', () => {
  // 状态
  const coaches = ref<Coach[]>([])
  const specialties = ref<string[]>([]) // 添加 specialties 状态
  const coachDetail = ref<Coach | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const detailLoading = ref(false)
  const error = ref<string | null>(null)

  // 计算属性
  const hasCoaches = computed(() => coaches.value.length > 0)
  const hasCoachDetail = computed(() => coachDetail.value !== null)

  // 获取教练列表
  const fetchCoaches = async (params: CoachQueryParams = {}) => {
    loading.value = true
    error.value = null

    try {
      const response: Page<Coach> = await getCoachList(params)
      coaches.value = response.records || []
      total.value = response.total || 0
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取教练列表失败'
      coaches.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  // 获取专长列表（模拟或从数据中提取）
  const fetchSpecialties = async () => {
    // 如果没有后端接口，我们可以暂时提供一些静态数据
    // 或者将来可以从后端获取
    specialties.value = ['增肌', '减脂', '塑形', '瑜伽', '普拉提', '康复', '拳击']
  }

  // 获取教练详情
  const fetchCoachDetail = async (id: string | number) => {
    detailLoading.value = true
    error.value = null

    try {
      coachDetail.value = await getCoachDetail(id)
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取教练详情失败'
      coachDetail.value = null
    } finally {
      detailLoading.value = false
    }
  }

  // 预约教练
  const bookCoachAppointment = async (data: BookCoachRequest) => {
    try {
      const result = await bookCoach(data)
      return result
    } catch (err) {
      throw err
    }
  }

  // 取消预约
  const cancelBooking = async (appointmentId: number) => {
    try {
      const result = await cancelCoachBooking(appointmentId)
      return result
    } catch (err) {
      throw err
    }
  }

  // 重置状态
  const resetState = () => {
    coaches.value = []
    coachDetail.value = null
    total.value = 0
    loading.value = false
    detailLoading.value = false
    error.value = null
  }

  return {
    // 状态
    coaches,
    specialties,
    coachDetail,
    total,
    loading,
    detailLoading,
    error,

    // 计算属性
    hasCoaches,
    hasCoachDetail,

    // 方法
    fetchCoaches,
    fetchSpecialties,
    fetchCoachDetail,
    bookCoachAppointment,
    cancelBooking,
    resetState
  }
})