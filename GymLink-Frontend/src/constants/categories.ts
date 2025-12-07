/**
 * 统一分类编码常量
 * 所有分类字段统一使用编码方式存储
 */

// 教练专业方向
export const coachSpecialtyOptions = [
    { value: '1', label: '私人健身教练', description: '提供一对一训练指导，涵盖体能评估、计划制定、动作纠正与基础营养建议。' },
    { value: '2', label: '团体课教练', description: '带领多人课程，如动感单车、有氧操、搏击操、Zumba、HIIT、瑜伽、普拉提等。' },
    { value: '3', label: '力量训练', description: '专注力量训练指导，帮助学员增肌塑形，提升肌肉力量。' },
    { value: '4', label: '瑜伽', description: '通过体式、呼吸和冥想，提升柔韧性和身心平衡。' },
    { value: '5', label: '有氧运动', description: '指导有氧训练，提升心肺功能和耐力，帮助减脂。' },
    { value: '6', label: '康复/矫正训练教练', description: '针对体态问题、术后恢复或慢性疼痛，进行功能性筛查与矫正性训练。' },
    { value: '7', label: '营养与生活方式教练', description: '提供体重管理、饮食建议与生活习惯指导（非医疗性质）。' },
    { value: '8', label: '专项运动教练', description: '专注细分领域，如普拉提、CrossFit、老年/孕产/青少年体适能等。' },
    { value: '9', label: '线上健身教练', description: '通过APP、视频或社群远程提供训练计划、饮食模板与打卡监督服务。' }
]

// 教练专业方向编码映射
export const coachSpecialtyMap: Record<string, string> = Object.fromEntries(
    coachSpecialtyOptions.map(item => [item.value, item.label])
)

// 课程分类
export const courseTypeOptions = [
    { value: '1', label: '私教课程', description: '一对一专属训练，根据个人目标定制计划，教练全程指导纠正动作' },
    { value: '2', label: '团体训练课程', description: '多人同时参与的集体课程，如动感单车、有氧操、搏击操等' },
    { value: '3', label: '功能性训练课程', description: '提升日常生活运动能力，改善身体协调性和稳定性' },
    { value: '4', label: '力量训练课程', description: '使用器械或自重进行肌肉力量训练，增肌塑形' },
    { value: '5', label: '瑜伽课程', description: '通过体式、呼吸和冥想，提升柔韧性和身心平衡' },
    { value: '6', label: '普拉提课程', description: '核心肌群训练，改善体态，增强身体控制力' },
    { value: '7', label: '康复/矫正训练课程', description: '针对体态问题、运动损伤或术后恢复的专业训练' },
    { value: '8', label: '专项运动表现课程', description: '针对特定运动项目的专业训练，提升运动表现' },
    { value: '9', label: '孕产/产后修复课程', description: '孕期安全运动和产后身体恢复的专业指导' },
    { value: '10', label: '老年/青少年体适能课程', description: '针对特定年龄段设计的安全有效的健身课程' },
    { value: '11', label: '线上直播/录播课程', description: '通过网络平台进行的远程健身指导课程' }
]

// 课程分类编码映射
export const courseTypeMap: Record<string, string> = Object.fromEntries(
    courseTypeOptions.map(item => [item.value, item.label])
)

// 菜谱标签
export const recipeTagOptions = [
    { value: '1', label: '增肌食谱', description: '高热量、高蛋白、适量碳水，支持肌肉合成与训练恢复' },
    { value: '2', label: '减脂食谱', description: '热量赤字、高蛋白、中低碳水，保留肌肉同时减少体脂' },
    { value: '3', label: '维持期食谱', description: '热量平衡，营养均衡，用于体重/体脂稳定阶段' },
    { value: '4', label: '高蛋白食谱', description: '蛋白质占比显著提高（≥30%总热量），适用于各类训练者' },
    { value: '5', label: '低碳/生酮食谱', description: '极低碳水（<50g/天）、高脂肪、中高蛋白，用于特定减脂或代谢需求' },
    { value: '6', label: '力量训练专用', description: '强调训练前后营养时机（如练前碳水+练后蛋白），提升表现与恢复' },
    { value: '7', label: '耐力训练专用', description: '高碳水储备（如赛前糖原填充），支持长时间有氧运动' },
    { value: '8', label: '素食健身食谱', description: '通过植物蛋白（豆类、藜麦、豆腐等）满足蛋白需求，注重氨基酸互补' },
    { value: '9', label: '清单饮食', description: '以天然、少加工食材为主，控制添加糖与反式脂肪，强调食物质量' },
    { value: '10', label: '周期化食谱', description: '根据训练周期（如休赛期、备赛期）动态调整热量与宏量营养素比例' }
]

// 菜谱标签编码映射
export const recipeTagMap: Record<string, string> = Object.fromEntries(
    recipeTagOptions.map(item => [item.value, item.label])
)

// 器材分类（已有，保持不变）
export const equipmentTypeOptions = [
    {
        value: '1',
        label: '有氧健身器材',
        children: [
            { value: '1-1', label: '跑步机' },
            { value: '1-2', label: '椭圆机' },
            { value: '1-3', label: '动感单车' },
            { value: '1-4', label: '划船机' },
            { value: '1-5', label: '健身车' },
            { value: '1-6', label: '楼梯机' },
            { value: '1-7', label: '体适能运动机' }
        ]
    },
    {
        value: '2',
        label: '力量训练器材',
        children: [
            { value: '2-1', label: '固定器械' },
            { value: '2-2', label: '自由重量器材' },
            { value: '2-3', label: '综合训练器材' }
        ]
    },
    { value: '3', label: '功能性训练器材' },
    { value: '4', label: '小型健身器械' },
    { value: '5', label: '康复与辅助器材' },
    { value: '6', label: '其他辅助设备' },
    { value: '7', label: '商用专用器材' },
    { value: '8', label: '家用专用器材' }
]

// 器材分类编码映射
export const equipmentTypeMap: Record<string, string> = {
    '1': '有氧健身器材', '1-1': '跑步机', '1-2': '椭圆机', '1-3': '动感单车', '1-4': '划船机', '1-5': '健身车', '1-6': '楼梯机', '1-7': '体适能运动机',
    '2': '力量训练器材', '2-1': '固定器械', '2-2': '自由重量器材', '2-3': '综合训练器材',
    '3': '功能性训练器材', '4': '小型健身器械', '5': '康复与辅助器材', '6': '其他辅助设备', '7': '商用专用器材', '8': '家用专用器材'
}

// 通用工具函数：根据编码获取标签名称
export const getCoachSpecialtyName = (code: string): string => coachSpecialtyMap[code] || code || '-'
export const getCourseTypeName = (code: string): string => courseTypeMap[code] || code || '-'
export const getRecipeTagName = (code: string): string => recipeTagMap[code] || code || '-'
export const getEquipmentTypeName = (code: string): string => equipmentTypeMap[code] || code || '-'

// 将逗号分隔的编码转换为标签名称（用于菜谱标签显示）
export const getRecipeTagNames = (codes: string): string => {
    if (!codes) return '-'
    return codes.split(',').map(code => getRecipeTagName(code.trim())).join(', ')
}
