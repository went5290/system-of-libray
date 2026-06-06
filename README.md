# Oracle 图书馆管理系统

基于 Vue 3、Spring Boot 和 Oracle 12c 的前后端分离图书馆管理系统。

## 当前结构

```text
library-system/
├── database/       Oracle 用户、表结构和种子数据脚本
├── backend/        Spring Boot REST API
└── frontend/       Vue 3 管理界面
```

## 本机 Oracle 环境

- Oracle Home：`D:\app\humber\product\12.1.0\dbhome_1`
- 监听地址：`127.0.0.1:1521`
- 推荐服务名：`ljw4`
- 应用数据库用户：`LIBRARY_APP`

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
cd backend
.\mvnw.cmd spring-boot:run
```

后端默认地址：`http://localhost:8080/api`

## 启动前端

```powershell
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

