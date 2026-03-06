import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getCourseList, getCoachList, type Course, type Coach } from '@/api'

export interface CarouselItem {
  id: number
  title: string
  description: string
  image: string
  link?: string
}

export interface Feature {
  id: number
  title: string
  description: string
  icon: string
}

export const useHomeStore = defineStore('home', () => {
  const getErrorMessage = (err: unknown, fallback: string) => {
    if (err && typeof err === 'object' && 'message' in err) {
      const message = (err as { message?: unknown }).message
      if (typeof message === 'string' && message) return message
    }
    return fallback
  }

  const carouselItems = ref<CarouselItem[]>([])
  const features = ref<Feature[]>([])
  const courses = ref<Course[]>([])
  const coaches = ref<Coach[]>([])
  const loading = ref(false)
  const error = ref('')

  const hasCarouselItems = computed(() => carouselItems.value.length > 0)
  const hasFeatures = computed(() => features.value.length > 0)
  const hasCourses = computed(() => courses.value.length > 0)
  const hasCoaches = computed(() => coaches.value.length > 0)
  const hasData = computed(
    () => hasCarouselItems.value || hasFeatures.value || hasCourses.value || hasCoaches.value
  )

  const fetchCarouselItems = async () => {
    try {
      carouselItems.value = [
        {
          id: 1,
          title: '专业健身指导',
          description: '专业教练团队为您提供个性化健身方案，帮助您快速达成健身目标。',
          image: '/carousel1.svg',
          link: '/coaches'
        },
        {
          id: 2,
          title: '多样化课程',
          description: '瑜伽、普拉提、有氧训练等多种课程选择，满足不同健身需求。',
          image: '/carousel2.svg',
          link: '/courses'
        },
        {
          id: 3,
          title: '营养饮食方案',
          description: '科学饮食搭配，助力健康生活，让您的健身效果事半功倍。',
          image: '/carousel3.svg',
          link: '/recipes'
        }
      ]
    } catch (err: unknown) {
      console.warn('获取轮播图数据失败:', err)
      carouselItems.value = []
    }
  }

  const fetchFeatures = async () => {
    try {
      features.value = [
        {
          id: 1,
          title: '个性化训练',
          description: '根据您的身体情况和健身目标，定制专属训练计划。',
          icon: '/feature1.svg'
        },
        {
          id: 2,
          title: '营养指导',
          description: '专业营养师提供饮食建议，科学搭配助力健身效果。',
          icon: '/feature2.svg'
        },
        {
          id: 3,
          title: '进度追踪',
          description: '记录您的健身历程，可视化展示训练成果。',
          icon: '/feature3.svg'
        },
        {
          id: 4,
          title: '社区互动',
          description: '与健身爱好者交流经验，分享健身心得。',
          icon: '/feature4.svg'
        }
      ]
    } catch (err: unknown) {
      console.warn('获取特色服务数据失败:', err)
      features.value = []
    }
  }

  const fetchHomeCourses = async (limit = 3) => {
    try {
      loading.value = true
      const data = await getCourseList({ current: 1, pageSize: limit })
      courses.value = data.records || []
      error.value = ''
    } catch (err: unknown) {
      error.value = getErrorMessage(err, '获取热门课程失败')
      courses.value = []
      console.warn('获取热门课程失败:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchHomeCoaches = async (limit = 4) => {
    try {
      loading.value = true
      const data = await getCoachList({ current: 1, pageSize: limit })
      coaches.value = data.records || []
      error.value = ''
    } catch (err: unknown) {
      error.value = getErrorMessage(err, '获取教练团队失败')
      coaches.value = []
      console.warn('获取教练团队失败:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchHomeData = async () => {
    await Promise.all([fetchCarouselItems(), fetchFeatures(), fetchHomeCourses(), fetchHomeCoaches()])
  }

  const resetState = () => {
    carouselItems.value = []
    features.value = []
    courses.value = []
    coaches.value = []
    loading.value = false
    error.value = ''
  }

  return {
    carouselItems,
    features,
    courses,
    coaches,
    loading,
    error,
    hasCarouselItems,
    hasFeatures,
    hasCourses,
    hasCoaches,
    hasData,
    fetchCarouselItems,
    fetchFeatures,
    fetchHomeCourses,
    fetchHomeCoaches,
    fetchHomeData,
    resetState
  }
})
