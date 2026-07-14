# core-identity

Core Platform 的身份基础设施，提供统一的用户身份、认证、授权能力。

## 子项目

| 目录 | 说明 |
|---|---|
| `backend/` | 用户端 REST API（Spring Boot 3 + Java 21） |
| `frontend/` | 用户端前端（Vue3 + TypeScript，待实现） |
| `admin-backend/` | 管理端 API |
| `admin-frontend/` | 管理端前端（待实现） |

## 快速启动

```bash
cd backend
mvn spring-boot:run
```

默认使用 SQLite 数据库，无需安装任何外部服务。

## API 前缀

- 用户端: `/api/v1/`
- 管理端: `/admin-api/v1/`
