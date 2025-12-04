import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import {
  getEquipmentList,
  getEquipmentDetail,
  reserveEquipment,
  cancelReservation,
  type Equipment,
  type EquipmentQueryParams,
  type BookEquipmentRequest
} from '@/api/equipment'
import type { Page } from '@/api/types'

export const useEquipmentStore = defineStore('equipment', () => {
  // 状态
  const equipmentList = ref<Equipment[]>([])
  const equipmentDetail = ref<Equipment | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const detailLoading = ref(false)
  const reservationSubmitting = ref(false)
  const error = ref<string | null>(null)
  const detailError = ref<string | null>(null)

  // 计算属性
  const hasEquipment = computed(() => equipmentList.value.length > 0)
  const hasEquipmentDetail = computed(() => equipmentDetail.value !== null)

  // 获取器材列表
  const fetchEquipmentList = async (params: EquipmentQueryParams = {}) => {
    loading.value = true
    error.value = null

    try {
      const response: Page<Equipment> = await getEquipmentList(params)
      equipmentList.value = response.records || []
      total.value = response.total || 0
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取器材列表失败'
      equipmentList.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  // 获取器材详情
  const fetchEquipmentDetail = async (id: number) => {
    detailLoading.value = true
    detailError.value = null

    try {
      equipmentDetail.value = await getEquipmentDetail(id)
    } catch (err) {
      detailError.value = err instanceof Error ? err.message : '获取器材详情失败'
      equipmentDetail.value = null
    } finally {
      detailLoading.value = false
    }
  }

  // 预约器材
  const reserveEquipmentItem = async (data: BookEquipmentRequest) => {
    reservationSubmitting.value = true

    try {
      const response = await reserveEquipment(data)
      return response
    } catch (err) {
      throw err
    } finally {
      reservationSubmitting.value = false
    }
  }

  // 取消预约
  const cancelReservationItem = async (bookingId: number) => {
    try {
      const response = await cancelReservation(bookingId)
      return response
    } catch (err) {
      throw err
    }
  }

  // 重置状态
  const resetState = () => {
    equipmentList.value = []
    equipmentDetail.value = null
    total.value = 0
    loading.value = false
    detailLoading.value = false
    reservationSubmitting.value = false
    error.value = null
    detailError.value = null
  }

  return {
    // 状态
    equipmentList,
    equipmentDetail,
    total,
    loading,
    detailLoading,
    reservationSubmitting,
    error,
    detailError,

    // 计算属性
    hasEquipment,
    hasEquipmentDetail,

    // 方法
    fetchEquipmentList,
    fetchEquipmentDetail,
    reserveEquipmentItem,
    cancelReservationItem,
    resetState
  }
})