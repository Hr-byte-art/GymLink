# GymLink

GymLink 是一个前后端分离的健身房管理系统，包含课程管理、教练预约、器材预约、用户与权限、社区内容等模块。  
仓库由 Java Spring Boot 后端与 Vue 3 前端组成，适合本地开发与二次扩展。

## 项目结构

```text
GymLink/
├─ GymLink/                # 后端（Spring Boot, Java 21）
│  ├─ src/main/java/com/ldr/gymlink
│  └─ src/main/resources
│     ├─ application.yml
│     ├─ application-local.yml
│     ├─ mapper/
│     └─ sql/gymlink.sql
└─ GymLink-Frontend/       # 前端（Vue 3 + TypeScript + Vite）
   └─ src/
```

## 技术栈

- 后端：Spring Boot 3.5.x、MyBatis-Plus、Sa-Token、MySQL、PostgreSQL、Redis、RabbitMQ
- 前端：Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus、Axios

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 20.19+（或 22.12+）
- MySQL 8+
- PostgreSQL 14+（如启用向量/AI相关能力）
- Redis 6+
- RabbitMQ 3.11+

## 快速开始

### 1. 克隆仓库

```bash
git clone <your-repo-url>
cd GymLink
```

### 2. 初始化数据库

后端 SQL 位于：

`GymLink/src/main/resources/sql/gymlink.sql`

请先创建数据库（例如 `gymlink`），再导入该 SQL 文件。

### 3. 配置后端

后端默认激活 `local` 配置（见 `application.yml` 中 `spring.profiles.active: local`），请检查并按你本地环境修改：

- `GymLink/src/main/resources/application-local.yml`

重点确认：

- MySQL 连接（`spring.datasource.primary.*`）
- PostgreSQL 连接（`spring.datasource.secondary.*`）
- Redis（`spring.data.redis.*`）
- RabbitMQ（`spring.rabbitmq.*`）
- 对象存储与 AI 服务相关配置（`cos.*`、`langchain4j.*`）

### 4. 启动后端

在 `GymLink/` 目录下执行：

```bash
mvn spring-boot:run
```

默认地址：

- 服务端口：`8080`
- 接口前缀：`/api`
- 示例：`http://localhost:8080/api`

### 5. 启动前端

在 `GymLink-Frontend/` 目录下执行：

```bash
npm install
npm run dev
```

Vite 默认地址通常为：`http://localhost:5173`

## 常用命令

### 前端（`GymLink-Frontend/`）

```bash
npm run dev         # 本地开发
npm run build       # 生产构建（含类型检查）
npm run test:unit   # 单元测试
npm run lint        # 代码检查并修复
npm run format      # 格式化
```

### 后端（`GymLink/`）

```bash
mvn spring-boot:run # 本地启动
mvn test            # 运行测试
mvn clean package   # 打包
```

## 开发规范（简版）

- 前端组件使用 PascalCase 命名（如 `CourseDetailView.vue`）
- 后端包名根路径：`com.ldr.gymlink`
- 提交信息建议：`<type>: <summary>`，例如 `feat: add coach appointment filtering`

## 安全提醒

当前仓库配置文件中出现了敏感配置项（如数据库密码、云服务密钥、API Key）。  
建议尽快完成以下处理：

- 将敏感信息迁移到环境变量或本地未提交配置
- 轮换已暴露的密钥与密码
- 在 `.gitignore` 中排除私密配置文件

## 许可证

如需开源，请在仓库根目录补充 `LICENSE` 文件并在此处声明许可协议。
