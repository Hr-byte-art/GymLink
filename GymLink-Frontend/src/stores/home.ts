import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getCourseList, getCoachList, type Course, type Coach } from '@/api'

// 轮播图项接口
export interface CarouselItem {
  id: number
  title: string
  description: string
  image: string
  link?: string
}

// 特色服务接口
export interface Feature {
  id: number
  title: string
  description: string
  icon: string
}

export const useHomeStore = defineStore('home', () => {
  // 状态
  const carouselItems = ref<CarouselItem[]>([])
  const features = ref<Feature[]>([])
  const courses = ref<Course[]>([])
  const coaches = ref<Coach[]>([])
  const loading = ref(false)
  const error = ref('')

  // 计算属性
  const hasCarouselItems = computed(() => carouselItems.value.length > 0)
  const hasFeatures = computed(() => features.value.length > 0)
  const hasCourses = computed(() => courses.value.length > 0)
  const hasCoaches = computed(() => coaches.value.length > 0)
  const hasData = computed(() => hasCarouselItems.value || hasFeatures.value || hasCourses.value || hasCoaches.value)

  // 获取轮播图数据
  const fetchCarouselItems = async () => {
    try {
      // 使用模拟数据，实际项目中应该从API获取
      carouselItems.value = [
        {
          id: 1,
          title: '专业健身指导',
          description: '专业教练团队为您提供个性化健身方案，帮助您快速达成健身目标',
          image: '/carousel1.svg',
          link: '/coaches'
        },
        {
          id: 2,
          title: '多样化课程',
          description: '瑜伽、普拉提、有氧运动等多种课程选择，满足不同健身需求',
          image: '/carousel2.svg',
          link: '/courses'
        },
        {
          id: 3,
          title: '营养饮食方案',
          description: '科学饮食搭配，助力健康生活，让您的健身效果事半功倍',
          image: '/carousel3.svg',
          link: '/recipes'
        }
      ]
    } catch (err: any) {
      console.warn('获取轮播图数据失败:', err)
      carouselItems.value = []
    }
  }

  // 获取特色服务数据
  const fetchFeatures = async () => {
    try {
      // 使用模拟数据，实际项目中应该从API获取
      features.value = [
        {
          id: 1,
          title: '个性化训练',
          description: '根据您的身体状况和健身目标，定制专属训练计划',
          icon: '/feature1.svg'
        },
        {
          id: 2,
          title: '营养指导',
          description: '专业营养师提供饮食建议，科学搭配助力健身效果',
          icon: '/feature2.svg'
        },
        {
          id: 3,
          title: '进度追踪',
          description: '记录您的健身历程，可视化展示训练成果',
          icon: '/feature3.svg'
        },
        {
          id: 4,
          title: '社区互动',
          description: '与健身爱好者交流经验，分享健身心得',
          icon: '/feature4.svg'
        }
      ]
    } catch (err: any) {
      console.warn('获取特色服务数据失败:', err)
      features.value = []
    }
  }

  // 获取首页课程（使用真实API）
  const fetchHomeCourses = async (limit: number = 3) => {
    try {
      loading.value = true
      const data = await getCourseList({ current: 1, pageSize: limit })
      courses.value = data.records || []
      error.value = ''
    } catch (err: any) {
      error.value = err.message || '获取热门课程失败'
      courses.value = []
      console.warn('获取热门课程失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 获取首页教练团队（使用真实API）
  const fetchHomeCoaches = async (limit: number = 4) => {
    try {
      loading.value = true
      const data = await getCoachList({ current: 1, pageSize: limit })
      coaches.value = data.records || []
      error.value = ''
    } catch (err: any) {
      error.value = err.message || '获取教练团队失败'
      coaches.value = []
      console.warn('获取教练团队失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 获取所有首页数据
  const fetchHomeData = async () => {
    await Promise.all([
      fetchCarouselItems(),
      fetchFeatures(),
      fetchHomeCourses(),
      fetchHomeCoaches()
    ])
  }

  // 重置状态
  const resetState = () => {
    carouselItems.value = []
    features.value = []
    courses.value = []
    coaches.value = []
    loading.value = false
    error.value = ''
  }

  return {
    // 状态
    carouselItems,
    features,
    courses,
    coaches,
    loading,
    error,

    // 计算属性
    hasCarouselItems,
    hasFeatures,
    hasCourses,
    hasCoaches,
    hasData,

    // 方法
    fetchCarouselItems,
    fetchFeatures,
    fetchHomeCourses,
    fetchHomeCoaches,
    fetchHomeData,
    resetState
  }
})