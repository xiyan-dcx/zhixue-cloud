# 智学云后端

技术栈、架构、运行方法、演示账号和截图见 [项目 README](../README.md)。

数据库只需执行 [sql/zhixue.sql](sql/zhixue.sql)，包含全部表及示例数据。初始化会清空并重建项目表，请仅在首次安装或重置演示库时执行。

配置 src/main/resources/application.yaml 中的数据库连接与 JWT 密钥后，在 PowerShell 中运行：

```powershell
$env:ZHIXUE_JAVA_HOME = 'C:\path\to\jdk-21'
.\dev.cmd
```

接口文档：[Swagger UI](http://localhost:8080/swagger-ui.html)。