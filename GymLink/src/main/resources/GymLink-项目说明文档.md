# GymLink 健身房管理系统 - 项目说明文档

## 一、项目概述

GymLink 是一个综合性健身房管理平台，采用前后端分离架构，提供课程管理、教练预约、器材预约、健身食谱、社区分享等功能。系统集成了 AI 智能助手，可以为用户提供个性化的健身建议和系统使用指导。

### 1.1 技术栈

#### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.5.8 | 后端框架 |
| MyBatis-Plus | 3.5.14 | ORM框架 |
| MySQL | - | 主数据库 |
| PostgreSQL | - | 向量数据库（AI对话记忆） |
| Redis | - | 缓存、Session存储 |
| RabbitMQ | - | 消息队列（通知推送） |
| Sa-Token | 1.44.0 | 权限认证框架 |
| LangChain4j | 1.9.1 | AI大模型集成框架 |
| 阿里云 DashScope | - | AI大模型服务（通义千问） |
| 腾讯云 COS | - | 对象存储（图片上传） |
| Knife4j | 4.4.0 | API文档 |

#### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.25 | 前端框架 |
| TypeScript | 5.9.0 | 编程语言 |
| Vite | 7.2.4 | 构建工具 |
| Pinia | 3.0.4 | 状态管理 |
| Vue Router | 4.6.3 | 路由管理 |
| Element Plus | 2.11.9 | UI组件库 |
| Axios | 1.13.2 | HTTP客户端 |
| ECharts | 6.0.0 | 图表库 |

### 1.2 用户角色

系统支持三种用户角色：

| 角色 | 说明 | 主要功能 |
|------|------|----------|
| 学员 (student) | 健身房会员 | 购买课程、预约教练、预约器材、浏览食谱、发布经验分享 |
| 教练 (coach) | 健身教练 | 管理预约、发布经验分享、查看学员信息 |
| 管理员 (admin) | 系统管理员 | 管理所有数据、发布公告、处理退款、查看统计 |

---

## 二、系统功能模块

### 2.1 用户端功能

#### 2.1.1 首页 (/)
- **位置**: 导航栏 → 首页
- **功能**:
  - 轮播图展示
  - 热门课程推荐
  - 优秀教练展示
  - 平台特色介绍
  - AI智能助手入口（右下角悬浮按钮）

#### 2.1.2 课程模块 (/courses)
- **位置**: 导航栏 → 课程
- **功能**:
  - 课程列表展示（支持分页）
  - 按课程类型筛选（11种类型）
  - 按难度筛选（初级/中级/高级）
  - 按价格排序
  - 关键词搜索
- **课程详情页** (/courses/:id):
  - 课程基本信息（名称、价格、时长、难度）
  - 授课教练信息
  - 课程介绍
  - 用户评价（评分统计、评价列表）
  - 购买课程按钮
  - 收藏功能
  - 联系教练

**课程类型编码**:
| 编码 | 类型名称 |
|------|----------|
| 1 | 私教课程 |
| 2 | 团体训练课程 |
| 3 | 功能性训练课程 |
| 4 | 力量训练课程 |
| 5 | 瑜伽课程 |
| 6 | 普拉提课程 |
| 7 | 康复/矫正训练课程 |
| 8 | 专项运动表现课程 |
| 9 | 孕产/产后修复课程 |
| 10 | 老年/青少年体适能课程 |
| 11 | 线上直播/录播课程 |

#### 2.1.3 教练模块 (/coaches)
- **位置**: 导航栏 → 教练
- **功能**:
  - 教练列表展示
  - 按专业方向筛选（9种方向）
  - 按性别筛选
  - 按年龄段筛选
  - 关键词搜索
- **教练详情页** (/coaches/:id):
  - 教练基本信息（姓名、性别、年龄、电话）
  - 专业方向
  - 个人简介
  - 在售课程列表
  - 学员评价（评分统计、评价列表）
  - 预约教练按钮
  - 收藏功能

**教练专业方向编码**:
| 编码 | 专业方向 |
|------|----------|
| 1 | 私人健身教练 |
| 2 | 团体课教练 |
| 3 | 力量训练 |
| 4 | 瑜伽 |
| 5 | 有氧运动 |
| 6 | 康复/矫正训练教练 |
| 7 | 营养与生活方式教练 |
| 8 | 专项运动教练 |
| 9 | 线上健身教练 |

#### 2.1.4 器材模块 (/equipment)
- **位置**: 导航栏 → 器材
- **功能**:
  - 器材列表展示
  - 按器材类型筛选（8大类）
  - 按状态筛选（可用/维护中）
  - 关键词搜索
- **器材详情页** (/equipment/:id):
  - 器材基本信息（名称、类型、位置）
  - 器材图片
  - 使用说明
  - 当前状态
  - 预约器材按钮
  - 收藏功能

**器材类型编码**:
| 编码 | 类型名称 | 子类型 |
|------|----------|--------|
| 1 | 有氧健身器材 | 1-1跑步机、1-2椭圆机、1-3动感单车、1-4划船机、1-5健身车、1-6楼梯机、1-7体适能运动机 |
| 2 | 力量训练器材 | 2-1固定器械、2-2自由重量器材、2-3综合训练器材 |
| 3 | 功能性训练器材 | - |
| 4 | 小型健身器械 | - |
| 5 | 康复与辅助器材 | - |
| 6 | 其他辅助设备 | - |
| 7 | 商用专用器材 | - |
| 8 | 家用专用器材 | - |

#### 2.1.5 食谱模块 (/recipes)
- **位置**: 导航栏 → 食谱
- **功能**:
  - 食谱列表展示
  - 按标签筛选（10种标签）
  - 关键词搜索
- **食谱详情页** (/recipes/:id):
  - 食谱标题和封面图
  - 营养信息（热量、蛋白质、碳水、脂肪）- AI自动分析
  - 准备时间、烹饪时间、份数、难度
  - 食谱内容（食材、做法）
  - 收藏功能

**食谱标签编码**:
| 编码 | 标签名称 |
|------|----------|
| 1 | 增肌食谱 |
| 2 | 减脂食谱 |
| 3 | 维持期食谱 |
| 4 | 高蛋白食谱 |
| 5 | 低碳/生酮食谱 |
| 6 | 力量训练专用 |
| 7 | 耐力训练专用 |
| 8 | 素食健身食谱 |
| 9 | 清单饮食 |
| 10 | 周期化食谱 |

#### 2.1.6 社区模块 (/posts)
- **位置**: 导航栏 → 社区
- **功能**:
  - 健身经验分享列表
  - 发布经验分享
  - 点赞/踩功能
  - 评论功能
- **帖子详情页** (/posts/:id):
  - 帖子内容
  - 作者信息
  - 点赞/踩数量
  - 评论列表
  - 发表评论

#### 2.1.7 个人中心 (/profile)
- **位置**: 导航栏 → 头像下拉菜单 → 个人中心
- **功能模块**:

| 菜单项 | 适用角色 | 功能说明 |
|--------|----------|----------|
| 基本信息 | 学员/教练 | 修改个人资料（姓名、电话、性别等）、头像上传、账户充值（学员） |
| 安全设置 | 全部 | 修改密码 |
| 我的课程 | 学员 | 已购课程列表、评价课程、申请退款 |
| 我的预约 | 学员 | 教练预约记录、器材预约记录、取消预约、评价教练 |
| 我的收藏 | 全部 | 收藏的课程/教练/器材/食谱 |
| 公告通知 | 全部 | 系统公告列表 |
| 消息通知 | 全部 | 个人消息（预约状态变更等） |
| 预约管理 | 教练 | 管理学员预约（确认/拒绝） |

#### 2.1.8 AI智能助手
- **位置**: 页面右下角悬浮按钮
- **功能**:
  - 智能对话（流式输出）
  - 课程推荐
  - 教练推荐
  - 器材推荐
  - 食谱推荐
  - 健身建议
  - BMI计算
  - 新手指南
  - 常见问题解答
- **AI工具能力**:
  - 搜索课程（按类型、名称）
  - 搜索教练（按专业、名称）
  - 搜索器材（按类型、名称）
  - 搜索食谱（按标签、标题）
  - 获取热门课程/高评分教练/可用器材
  - 健身建议生成

---

### 2.2 管理端功能

管理端入口：登录管理员账号后，导航栏显示"管理后台"入口

#### 2.2.1 学员管理 (/admin/students)
- 学员列表（分页、搜索）
- 添加学员
- 编辑学员信息
- 删除学员

#### 2.2.2 教练管理 (/admin/coaches)
- 教练列表（分页、搜索）
- 添加教练
- 编辑教练信息
- 删除教练

#### 2.2.3 课程管理 (/admin/courses)
- 课程列表（分页、搜索）
- 添加课程
- 编辑课程信息
- 上传课程封面
- 删除课程

#### 2.2.4 器材管理 (/admin/equipment)
- 器材列表（分页、搜索）
- 添加器材
- 编辑器材信息
- 上传器材图片
- 修改器材状态
- 删除器材

#### 2.2.5 食谱管理 (/admin/recipes)
- 食谱列表（分页、搜索）
- 添加食谱（AI自动分析营养信息）
- 编辑食谱
- 上传食谱封面
- 删除食谱

#### 2.2.6 帖子管理 (/admin/posts)
- 帖子列表
- 审核帖子
- 删除帖子

#### 2.2.7 退款管理 (/admin/refunds)
- 退款申请列表
- 审批退款（通过/拒绝）

#### 2.2.8 公告管理 (/admin/announcements)
- 公告列表
- 发布公告
- 编辑公告
- 删除公告

#### 2.2.9 数据统计
- **器材统计** (/admin/equipment-stats): 器材使用率、预约趋势
- **课程统计** (/admin/course-stats): 课程销售、热门课程
- **教练统计** (/admin/coach-stats): 教练预约量、评分排名

---

## 三、核心业务流程

### 3.1 用户注册登录流程
```
1. 访问 /auth 页面
2. 选择"注册"标签
3. 填写用户名、密码、确认密码
4. 选择角色（学员/教练）
5. 提交注册
6. 注册成功后切换到登录页
7. 输入用户名密码登录
8. 登录成功，跳转首页
```

### 3.2 课程购买流程
```
1. 浏览课程列表 (/courses)
2. 点击课程进入详情页
3. 点击"立即购买"
4. 确认购买（扣除账户余额）
5. 购买成功，课程显示"已购买"
6. 在个人中心"我的课程"查看
```

### 3.3 教练预约流程
```
1. 浏览教练列表 (/coaches)
2. 点击教练进入详情页
3. 点击"预约教练"
4. 选择预约时间、时长
5. 填写备注信息
6. 提交预约
7. 等待教练确认
8. 教练确认后，预约生效
9. 完成后可评价教练
```

### 3.4 器材预约流程
```
1. 浏览器材列表 (/equipment)
2. 点击器材进入详情页
3. 点击"预约器材"
4. 选择预约时间段
5. 提交预约
6. 预约成功
7. 按时使用器材
```

### 3.5 退款流程
```
1. 进入个人中心"我的课程"
2. 找到要退款的课程
3. 点击"申请退款"
4. 确认退款申请
5. 等待管理员审批
6. 审批通过后，余额返还
```

---

## 四、项目结构

### 4.1 后端目录结构
```
GymLink/src/main/java/com/ldr/gymlink/
├── ai/                          # AI相关
│   ├── aiService/               # AI服务接口
│   │   ├── GymLinkAiService.java        # 流式AI服务
│   │   ├── GymLinkSyncAiService.java    # 同步AI服务
│   │   └── RecipeNutritionAnalyzer.java # 食谱营养分析
│   ├── memory/                  # 对话记忆
│   │   └── PersistentChatMemoryStore.java
│   └── tools/                   # AI工具
│       ├── GymLinkTools.java    # 工具聚合
│       ├── CourseTools.java     # 课程工具
│       ├── CoachTools.java      # 教练工具
│       ├── EquipmentTools.java  # 器材工具
│       ├── RecipeTools.java     # 食谱工具
│       └── FitnessAdvisorTools.java # 健身顾问工具
├── controller/                  # 控制器层
│   ├── UserController.java      # 用户认证
│   ├── StudentController.java   # 学员管理
│   ├── CoachController.java     # 教练管理
│   ├── CourseController.java    # 课程管理
│   ├── EquipmentController.java # 器材管理
│   ├── RecipeController.java    # 食谱管理
│   ├── AppointmentController.java # 预约管理
│   ├── FavoriteController.java  # 收藏管理
│   ├── ExperienceController.java # 经验分享
│   ├── CommentController.java   # 评论管理
│   ├── CourseReviewController.java # 课程评价
│   ├── NotificationController.java # 通知管理
│   ├── AnnouncementController.java # 公告管理
│   ├── AiChatController.java    # AI对话
│   ├── FileController.java      # 文件上传
│   └── AdminController.java     # 管理员
├── service/                     # 服务层
│   ├── impl/                    # 服务实现
│   └── ...Service.java          # 服务接口
├── mapper/                      # 数据访问层
├── model/                       # 数据模型
│   ├── entity/                  # 实体类
│   ├── dto/                     # 数据传输对象
│   ├── vo/                      # 视图对象
│   └── enums/                   # 枚举类
├── config/                      # 配置类
├── mq/                          # 消息队列
│   ├── config/                  # MQ配置
│   ├── producer/                # 消息生产者
│   ├── consumer/                # 消息消费者
│   └── message/                 # 消息体
├── exception/                   # 异常处理
├── common/                      # 公共类
└── utils/                       # 工具类
```

### 4.2 前端目录结构
```
GymLink-Frontend/src/
├── api/                         # API接口
│   ├── user.ts                  # 用户接口
│   ├── student.ts               # 学员接口
│   ├── coach.ts                 # 教练接口
│   ├── course.ts                # 课程接口
│   ├── equipment.ts             # 器材接口
│   ├── recipe.ts                # 食谱接口
│   ├── appointment.ts           # 预约接口
│   ├── favorite.ts              # 收藏接口
│   ├── review.ts                # 评价接口
│   ├── notification.ts          # 通知接口
│   ├── announcement.ts          # 公告接口
│   └── ai.ts                    # AI接口
├── components/                  # 公共组件
│   ├── AppLayout.vue            # 用户端布局
│   ├── AdminLayout.vue          # 管理端布局
│   ├── NavBar.vue               # 导航栏
│   ├── Footer.vue               # 页脚
│   ├── AiChatBot.vue            # AI聊天组件
│   └── NotificationBell.vue     # 通知铃铛
├── views/                       # 页面视图
│   ├── HomeView.vue             # 首页
│   ├── AuthView.vue             # 登录注册
│   ├── CoursesView.vue          # 课程列表
│   ├── CourseDetailView.vue     # 课程详情
│   ├── CoachesView.vue          # 教练列表
│   ├── CoachDetailView.vue      # 教练详情
│   ├── EquipmentView.vue        # 器材列表
│   ├── EquipmentDetailView.vue  # 器材详情
│   ├── RecipesView.vue          # 食谱列表
│   ├── RecipeDetailView.vue     # 食谱详情
│   ├── PostsView.vue            # 社区列表
│   ├── PostDetailView.vue       # 帖子详情
│   ├── ProfileView.vue          # 个人中心
│   └── admin/                   # 管理端页面
│       ├── StudentManage.vue    # 学员管理
│       ├── CoachManage.vue      # 教练管理
│       ├── CourseManage.vue     # 课程管理
│       ├── EquipmentManage.vue  # 器材管理
│       ├── RecipeManage.vue     # 食谱管理
│       ├── PostManage.vue       # 帖子管理
│       ├── RefundManage.vue     # 退款管理
│       ├── AnnouncementManage.vue # 公告管理
│       ├── EquipmentStatistics.vue # 器材统计
│       ├── CourseStatistics.vue # 课程统计
│       └── CoachStatistics.vue  # 教练统计
├── stores/                      # 状态管理
│   ├── auth.ts                  # 认证状态
│   ├── course.ts                # 课程状态
│   ├── coach.ts                 # 教练状态
│   ├── equipment.ts             # 器材状态
│   └── recipe.ts                # 食谱状态
├── router/                      # 路由配置
│   └── index.ts
├── constants/                   # 常量定义
│   └── categories.ts            # 分类编码
└── utils/                       # 工具函数
    ├── request.ts               # HTTP请求封装
    └── message.ts               # 消息提示
```

---

## 五、API接口文档

后端启动后，访问 Knife4j 接口文档：
```
http://localhost:8080/doc.html
```

### 5.1 主要接口分组

| 分组 | 路径前缀 | 说明 |
|------|----------|------|
| 用户认证 | /user | 登录、注册、修改密码 |
| 学员管理 | /student | 学员CRUD、充值、购课 |
| 教练管理 | /coach | 教练CRUD、预约管理 |
| 课程管理 | /course | 课程CRUD |
| 器材管理 | /equipment | 器材CRUD、预约 |
| 食谱管理 | /recipe | 食谱CRUD |
| 预约管理 | /appointment | 教练预约、器材预约 |
| 收藏管理 | /favorite | 收藏/取消收藏 |
| 评价管理 | /review | 课程评价 |
| 经验分享 | /experience | 帖子CRUD |
| 评论管理 | /comment | 评论CRUD |
| 通知管理 | /notification | 消息通知 |
| 公告管理 | /announcement | 系统公告 |
| AI对话 | /ai | AI聊天接口 |
| 文件上传 | /file | 图片上传 |

---

## 六、部署配置

### 6.1 后端配置文件
配置文件位置：

`GymLink/src/main/resources/application-local.yml`

`GymLink/src/main/resources/application.yml`

需要配置的内容：
- MySQL 数据库连接
- PostgreSQL 数据库连接（AI对话记忆）
- Redis 连接
- RabbitMQ 连接
- 腾讯云 COS 配置
- 阿里云 DashScope API Key

上面的我已经配置了，但是是我的，要花钱，你如果用的少的话就直接用我的，用的多的话就自己创建

### 6.2 前端配置
- 开发环境：`npm run dev`
- 生产构建：`npm run build`
- API 地址配置在 `src/utils/request.ts`

---

## 七、特色功能

### 7.1 AI智能助手
- 基于阿里云通义千问大模型
- 支持流式输出（SSE）
- 支持工具调用（Function Calling）
- 对话记忆持久化（PostgreSQL）
- 可查询系统数据（课程、教练、器材、食谱）

### 7.2 食谱营养AI分析
- 添加食谱时自动调用AI分析
- 分析内容：热量、蛋白质、碳水、脂肪
- 估算：准备时间、烹饪时间、份数、难度

### 7.3 消息通知系统
- 基于 RabbitMQ 消息队列
- 预约状态变更自动通知
- 退款审批结果通知
- 实时未读数量轮询

### 7.4 图片压缩上传
- 自动压缩为 WebP 格式
- 上传至腾讯云 COS
- 支持头像、课程封面、器材图片、食谱封面

---

## 八、数据库表结构

### 主要数据表

| 表名 | 说明 |
|------|------|
| user | 用户表（登录账号） |
| student | 学员表 |
| coach | 教练表 |
| admin | 管理员表 |
| course | 课程表 |
| course_order | 课程订单表 |
| course_review | 课程评价表 |
| equipment | 器材表 |
| equipment_reservation | 器材预约表 |
| coach_appointment | 教练预约表 |
| recipe | 食谱表 |
| experience | 经验分享表 |
| experience_reaction | 点赞/踩记录表 |
| comment | 评论表 |
| favorite | 收藏表 |
| notification | 通知表 |
| announcement | 公告表 |
| recharge_record | 充值记录表 |
| chat_memory | AI对话记忆表 |

---

## 九、快速开始

### 9.1 后端启动
```bash
cd GymLink
# 配置 application-local.yml
mvn spring-boot:run
```

### 9.2 前端启动
```bash
cd GymLink-Frontend
npm install
npm run dev
```

### 9.3 访问地址
- 前端：http://localhost:5173
- 后端API：http://localhost:8080
- API文档：http://localhost:8080/api/doc.html

---

## 十、测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | zdf040228 |
| 学员 | student | zdf040228 |
| 教练 | coach | zdf040228 |

---

*文档版本：1.0*
*更新日期：2025-12-21*
