# Oracle 图书馆管理系统

基于 Vue 3、Spring Boot 和 Oracle 12c 的前后端分离图书馆管理系统。

## 本机快速启动

确保 Oracle 数据库服务已启动后，双击根目录的 `start-library.cmd`。后端尚未运行时，按提示输入 `LIBRARY_APP` 数据库密码。

系统启动后会自动打开管理页面。停止系统时双击 `stop-library.cmd`。

## 当前结构

```text
library-system/
├── database/       Oracle 用户、表结构和种子数据脚本
├── backend/        Spring Boot REST API
└── frontend/       Vue 3 管理界面
```

数据库密码和 JWT 密钥均通过环境变量传入，不写入仓库。

## 初始化数据库

以 PowerShell 运行：

```powershell
$env:LIBRARY_DB_PASSWORD = "请设置强密码"
.\database\init-database.ps1
```

脚本会在 `LJW4` PDB 中创建独立用户 `LIBRARY_APP`，然后创建表、索引和演示数据。

## 启动后端

```powershell
$env:LIBRARY_DB_PASSWORD = "与初始化时一致的密码"
$env:LIBRARY_AUTH_SECRET = "请设置至少 32 位的随机签名密钥"
cd backend
.\mvnw.cmd spring-boot:run
```

除 `/api/auth/login` 与健康检查外，后端接口均要求携带登录返回的 Bearer Token。令牌默认有效期为 8 小时。

本地演示管理员账号为 `admin`。公开部署前必须修改管理员密码，并设置独立的 `LIBRARY_AUTH_SECRET`。

## 启动前端

```powershell
cd frontend
npm install
npm run dev
```
