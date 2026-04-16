export const coachSpecialtyOptions = [
  { value: '1', label: '私人健身教练', description: '提供一对一训练指导，适合减脂、塑形和体能提升。' },
  { value: '2', label: '团体课教练', description: '负责团操、动感单车、燃脂课程等多人课堂。' },
  { value: '3', label: '力量训练', description: '专注增肌、力量提升和器械训练指导。' },
  { value: '4', label: '瑜伽', description: '提供瑜伽、拉伸、呼吸与身心平衡训练。' },
  { value: '5', label: '有氧训练', description: '偏向心肺耐力、燃脂和节奏训练。' },
  { value: '6', label: '康复训练', description: '适合姿态矫正、运动康复和功能恢复。' },
  { value: '7', label: '营养指导', description: '提供饮食建议和生活方式管理支持。' },
  { value: '8', label: '专项运动', description: '针对普拉提、搏击、孕产等专项人群或项目。' },
  { value: '9', label: '线上教练', description: '提供线上计划、远程打卡和持续陪练服务。' }
] as const

export const coachSpecialtyMap: Record<string, string> = Object.fromEntries(
  coachSpecialtyOptions.map(item => [item.value, item.label])
)

export const courseTypeOptions = [
  { value: '1', label: '私教课程', description: '适合一对一深度训练和定制化提升。' },
  { value: '2', label: '团体训练课程', description: '适合多人一起参与的固定课程。' },
  { value: '3', label: '功能训练课程', description: '强调灵活性、协调性和核心稳定。' },
  { value: '4', label: '力量训练课程', description: '专注增肌、塑形和力量提升。' },
  { value: '5', label: '瑜伽课程', description: '适合拉伸放松和身心平衡。' },
  { value: '6', label: '普拉提课程', description: '强调核心控制和姿态改善。' },
  { value: '7', label: '康复矫正课程', description: '适合功能恢复和姿势矫正。' },
  { value: '8', label: '专项表现课程', description: '用于提升某类专项运动表现。' },
  { value: '9', label: '孕产修复课程', description: '面向孕期和产后阶段的安全训练。' },
  { value: '10', label: '青少儿/长者课程', description: '针对特定年龄阶段的适配课程。' },
  { value: '11', label: '线上课程', description: '通过直播或录播方式进行训练。' }
] as const

export const courseTypeMap: Record<string, string> = Object.fromEntries(
  courseTypeOptions.map(item => [item.value, item.label])
)

export const deliveryModeOptions = [
  { value: 1, label: '私教课', description: '购买后可预约对应教练的一对一上课时段。' },
  { value: 2, label: '团体课', description: '购买后可选择该课程的具体团课场次报名。' }
] as const

export const deliveryModeMap: Record<number, string> = Object.fromEntries(
  deliveryModeOptions.map(item => [item.value, item.label])
) as Record<number, string>

export const recipeTagOptions = [
  { value: '1', label: '增肌食谱', description: '高蛋白高热量，适合增肌期。' },
  { value: '2', label: '减脂食谱', description: '控制热量，兼顾饱腹感和蛋白摄入。' },
  { value: '3', label: '维持食谱', description: '适合日常健康饮食和体重维持。' },
  { value: '4', label: '高蛋白菜谱', description: '突出蛋白质摄入比例。' },
  { value: '5', label: '低碳食谱', description: '适合阶段性控制碳水需求。' },
  { value: '6', label: '力量训练专用', description: '更强调训练前后营养补充。' },
  { value: '7', label: '耐力训练专用', description: '适合长时间有氧和耐力训练。' },
  { value: '8', label: '素食健身食谱', description: '植物蛋白为主的健身饮食方案。' },
  { value: '9', label: '清淡饮食', description: '低油低盐，适合日常调理。' },
  { value: '10', label: '周期饮食', description: '按训练周期动态调整营养结构。' }
] as const

export const recipeTagMap: Record<string, string> = Object.fromEntries(
  recipeTagOptions.map(item => [item.value, item.label])
)

export const equipmentTypeOptions = [
  {
    value: '1',
    label: '有氧器材',
    children: [
      { value: '1-1', label: '跑步机' },
      { value: '1-2', label: '椭圆机' },
      { value: '1-3', label: '动感单车' },
      { value: '1-4', label: '划船机' },
      { value: '1-5', label: '健身车' },
      { value: '1-6', label: '登楼机' },
      { value: '1-7', label: '体适能训练机' }
    ]
  },
  {
    value: '2',
    label: '力量器材',
    children: [
      { value: '2-1', label: '固定器械' },
      { value: '2-2', label: '自由重量器材' },
      { value: '2-3', label: '综合训练器材' }
    ]
  },
  { value: '3', label: '功能训练器材' },
  { value: '4', label: '小型健身器械' },
  { value: '5', label: '康复辅助器材' },
  { value: '6', label: '其他设备' },
  { value: '7', label: '商用器材' },
  { value: '8', label: '家用器材' }
] as const

export const equipmentTypeMap: Record<string, string> = {
  '1': '有氧器材',
  '1-1': '跑步机',
  '1-2': '椭圆机',
  '1-3': '动感单车',
  '1-4': '划船机',
  '1-5': '健身车',
  '1-6': '登楼机',
  '1-7': '体适能训练机',
  '2': '力量器材',
  '2-1': '固定器械',
  '2-2': '自由重量器材',
  '2-3': '综合训练器材',
  '3': '功能训练器材',
  '4': '小型健身器械',
  '5': '康复辅助器材',
  '6': '其他设备',
  '7': '商用器材',
  '8': '家用器材'
}

export const getCoachSpecialtyName = (code: string): string => coachSpecialtyMap[code] || code || '-'
export const getCourseTypeName = (code: string): string => courseTypeMap[code] || code || '-'
export const getDeliveryModeName = (code: number | string | undefined | null): string => {
  if (code === undefined || code === null || code === '') return '-'
  return deliveryModeMap[Number(code)] || String(code)
}
export const getRecipeTagName = (code: string): string => recipeTagMap[code] || code || '-'
export const getEquipmentTypeName = (code: string): string => equipmentTypeMap[code] || code || '-'

export const getRecipeTagNames = (codes: string): string => {
  if (!codes) return '-'
  return codes.split(',').map(code => getRecipeTagName(code.trim())).join(', ')
}

export const genderMap: Record<number, string> = {
  1: '男',
  2: '女',
  3: '未知'
}

export const getGenderName = (code: number | undefined | null): string => {
  if (code === undefined || code === null) return '-'
  return genderMap[code] || '未知'
}
