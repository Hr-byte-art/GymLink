# GymLink 前端重构续作清单（更新于 2026-03-05）

## 1. 本轮已完成（已验证通过）

### 1.1 规则与边界
- 已按要求读取并遵守：
  - `E:\project\Personal\GymLink\AGENTS.md`
  - `E:\project\Personal\GymLink\GymLink-Frontend\AGENTS.md`
- 全程保持：
  - 不改 API URL
  - 不新增 API
  - 不改请求方法
  - 不改参数结构
  - 不改后端返回结构假设
  - 不改 `request.ts / api.ts` 协议封装逻辑

### 1.2 已完成的核心修复
- 清理并修复了多处历史乱码引发的 Vue 模板语法破损（未闭合引号、坏标签、损坏文案）。
- 清理 admin 相关页面中的 `no-explicit-any` 主要问题并补齐本地类型定义。
- 修复若干详情页的 TS 严格类型问题（可选字段、catch unknown、空值收敛等）。
- 修复多个管理页/统计页的模板闭合问题，恢复可构建状态。

### 1.3 本轮关键修改文件（摘要）
- `GymLink-Frontend/src/api/ai.ts`
- `GymLink-Frontend/src/api/user.ts`
- `GymLink-Frontend/src/stores/home.ts`
- `GymLink-Frontend/src/components/AdminLayout.vue`
- `GymLink-Frontend/src/components/Footer.vue`
- `GymLink-Frontend/src/views/CourseDetailView.vue`
- `GymLink-Frontend/src/views/CoachDetailView.vue`
- `GymLink-Frontend/src/views/EquipmentDetailView.vue`
- `GymLink-Frontend/src/views/RecipeDetailView.vue`
- `GymLink-Frontend/src/views/admin/CoachManage.vue`
- `GymLink-Frontend/src/views/admin/CourseManage.vue`
- `GymLink-Frontend/src/views/admin/EquipmentManage.vue`
- `GymLink-Frontend/src/views/admin/PostManage.vue`
- `GymLink-Frontend/src/views/admin/RecipeManage.vue`
- `GymLink-Frontend/src/views/admin/StudentManage.vue`
- `GymLink-Frontend/src/views/admin/CoachStatistics.vue`
- `GymLink-Frontend/src/views/admin/CourseStatistics.vue`
- `GymLink-Frontend/src/views/admin/EquipmentStatistics.vue`
- `GymLink-Frontend/src/views/admin/RefundManage.vue`

## 2. 当前验证结果（2026-03-05）

在 `GymLink-Frontend/` 目录执行：
- `npm.cmd run lint` ✅ 通过
- `npm.cmd run type-check` ✅ 通过
- `npm.cmd run build-only` ✅ 通过

结论：当前代码已达到“可 lint / 可 type-check / 可 build”的稳定状态。

## 3. 当前工作区状态
- 仍为未提交状态（按要求未自动提交）。
- 根目录保留交接文档：
  - `ToDo.md`（本文件）
  - `SESSION_HANDOFF.md`（如存在）

## 4. 下一步建议（可选优化，不是阻塞项）

### Step A（必做，开始前）
```powershell
git -C E:\project\Personal\GymLink status --short
git -C E:\project\Personal\GymLink diff --name-only -- GymLink-Frontend/src
```

### Step B（可选）文案与乱码统一清洗
- 目标：统一中文文案，清理仍存在的乱码文本（不改变接口契约）。
- 范围优先：`src/views/admin/*`、`src/components/AdminLayout.vue`、`src/stores/home.ts`。

### Step C（可选）回归冒烟
```powershell
cd E:\project\Personal\GymLink\GymLink-Frontend
npm.cmd run lint
npm.cmd run type-check
npm.cmd run build-only
```

### Step D（可选）提交准备
- 汇总改动清单（按模块：api / store / components / views / admin）。
- 记录验证结果与潜在风险（如仅文案层修改、无接口改动）。

## 5. 严格注意事项（持续有效）
- 不修改 `src/api` 的后端契约假设（URL、method、params、返回结构）。
- 不修改 `request.ts/api.ts` 的协议封装逻辑。
- 若未来发现需要接口变更：只能先说明原因并等待人工确认。

## 6. 给下一次模型的直接提示词（可复制）

```text
请先读取：
1) E:\project\Personal\GymLink\AGENTS.md
2) E:\project\Personal\GymLink\GymLink-Frontend\AGENTS.md
3) E:\project\Personal\GymLink\ToDo.md

然后按 ToDo.md 中的 Step A -> Step D 执行。
要求：
- 不修改 API 契约和 request/api 封装层；
- 先做 Step A 获取差异范围；
- 若执行文案清洗，仅改展示文案，不改接口调用；
- 每完成一步输出摘要；
- 最后输出：改动文件清单 + lint/type-check/build 结果 + 剩余待办。
```

---

## 7. 交接一句话
当前主流程已稳定并通过 `lint + type-check + build`，后续主要是可选的文案/乱码清洗与提交整理工作。

---

## 8. 本轮补充记录（更新于 2026-03-06）

### 8.1 本轮完成内容
- 完成前端页面乱码/文案清理，覆盖：
  - 管理端页面（`src/views/admin/*`）
  - 通用组件与页面（如 `AiChatBot.vue`、`PostsView.vue`、`CoachesView.vue` 等）
- 按“可用性优先”规则执行：
  - 不做无必要重构
  - 优先保证页面可用与构建通过
  - 对乱码注释执行直接清理
- 输出并交付离线联调方案：
  - 提供 Requestly 使用说明文档
  - 提供可导入的 Requestly 规则 JSON

### 8.2 前端验证结果
在 `GymLink-Frontend/` 目录多轮执行并通过：
- `npm run lint` ✅
- `npm run type-check` ✅
- `npm run build-only` ✅

结论：当前前端保持“可 lint / 可 type-check / 可 build”状态。

### 8.3 规则确认（持续有效）
- 可用性优先：以页面可使用为第一目标。
- 乱码注释可直接清理：不保留历史注释。
- 不做无必要重构：避免引入额外风险。
- 保持接口契约边界：不主动变更 API URL / method / params / 返回结构假设。

### 8.4 本次清理动作明细
已删除缓存/构建产物：
- `GymLink-Frontend/.eslintcache`
- `GymLink-Frontend/.npm-cache/`
- `GymLink-Frontend/dist/`
- `target/`（仓库根）
- `GymLink/target/`

已删除临时交付文档：
- `SESSION_HANDOFF.md`
- `OFFLINE_PAGE_CHECK_WITH_REQUESTLY.md`
- `requestly-rules-gylink-offline.json`

已重命名：
- `ToDo.md` -> `hasDo.md`

### 8.5 当前工作区与后续建议
- 当前仍为未提交状态（符合“未自动提交”约束）。
- 建议下一步：
  1. 先查看差异：`git status --short`
  2. 若确认无误，再按模块整理提交说明
  3. 在需要时结合离线 Mock 或真后端做一次关键路径冒烟
