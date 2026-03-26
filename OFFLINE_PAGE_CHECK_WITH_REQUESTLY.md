# GymLink 前端离线页面检查（Requestly）

用于后端不可用时，在本地查看页面与交互。

## 1) 启动前端

```powershell
cd GymLink-Frontend
npm install
npm run dev
```

## 2) 导入规则

1. 打开 Requestly。
2. 选择 `Import`。
3. 导入根目录文件：`requestly-rules-gylink-offline.json`。
4. 确保规则状态是 `Active`。

## 3) 验证

1. 打开页面（课程、教练、器材、帖子、管理端）。
2. 确认列表有 mock 数据、提交类接口返回成功。
3. 若某页面仍报错，打开浏览器 Network 看未命中的 URL，再补规则。

## 4) 说明

- 该规则用 `URL contains /api/` 匹配并统一分发到不同接口响应。
- 上传接口返回图片 URL 字符串。
- AI 聊天接口返回一条文本 mock。
