# Login-Test 认证接口与联调练习

本文档把当前接口行为和建议采用的目标契约分开。联调现有代码时以「当前接口」为准，修改前后端时以「目标契约」作为验收清单。

## 当前接口

默认地址为 `http://localhost:8092`。登录相关接口使用 JSON 响应，常规结构为 `{"code":200,"message":"操作成功","data":null}`。`code` 是业务码，HTTP 状态在响应行中单独读取。测试接口成功时返回文本。

| 方法和路径 | 凭据 | 成功结果 |
|---|---|---|
| `POST /user/login`，`isLogin=0` | JSON 用户名和密码 | HTTP `200`，业务码 `200`，`data` 为空。注册成功后再请求登录 |
| `POST /user/login`，`isLogin=1` | JSON 用户名和密码 | HTTP `200`，业务码 `200`，`data.accessToken` 是 JWT，通过 `Set-Cookie` 发放 `refreshToken` |
| `POST /user/login/refresh` | `refreshToken` Cookie | HTTP `200`，业务码 `200`，`data.accessToken` 是新 JWT，`Set-Cookie` 更新刷新令牌。当前响应体也含 `data.refreshToken` |
| `GET /test/t1` | 无 | HTTP `200`，文本 `t1` |
| `GET /test/t2` | `Authorization: <原始 JWT>` | HTTP `200`，文本以 `t2，我是：` 开头。当前请求头值直接传 JWT，省略 `Bearer ` 前缀 |

登录与注册都要求 `Content-Type: application/json`。请求示例使用练习账号。

```json
{
  "username": "api_demo",
  "password": "Demo123!",
  "isLogin": 1
}
```

- `username` 为必填字符串，允许 4–20 个英文字母、数字或下划线。
- `password` 为必填字符串，允许 6–26 个可打印 ASCII 字符（空格除外），至少各含一个英文字母、数字和标点。
- `isLogin` 为必填整数，`0` 注册，`1` 登录。
- 刷新接口无请求体。Cookie 带 `HttpOnly`、`Secure`、`SameSite=Lax` 和 `Path=/user`。浏览器联调需确保 Cookie 能在所用源和传输协议下被接受并随刷新请求发送。

## 双令牌如何续期

登录后，前端将 Access Token 放在内存中，调用受保护接口时写入请求头。Shiro 的 `JwtFilter` 验证签名和有效期，`UserRealm` 从数据库检查用户和账号状态。当前测试接口只能验证身份认证。`UserRealm.doGetAuthorizationInfo()` 尚未加载角色或权限。

Access Token 配置为 15 分钟，到期时间写在签发的 JWT 中。Refresh Token 是带用户 ID 前缀的随机令牌，Redis 按用户保存它的哈希，默认有效期为 7 天。成功刷新时，后端替换 Redis 里的哈希，把 TTL 重设为 7 天，同时签发新的 Access Token 并更新 Cookie。这就是滑动过期。普通业务请求不会延长 TTL。当前按用户存一份哈希，重新登录会替换该用户原来的刷新凭据。已签发的 Access Token 仍按其自身有效期和账号状态校验。

## 目标契约与当前差异

下表的目标列用于后续改造。`HTTP / code` 分别指响应行的状态与 JSON 业务码。成功统一用 HTTP `200`、业务码 `200`，注册的 `data` 为空，登录和刷新的 `data` 只含 Access Token。前端用业务码做分支，用 `message` 展示提示。

| 场景 | 当前 `HTTP / code` | 目标 `HTTP / code` | 前端动作 |
|---|---|---|---|
| 登录字段缺失或格式不符 | `200 / 400` | `400 / 400` | 提示修正输入 |
| `isLogin` 为 `2` 等非法值 | `200 / 500` | `400 / 400` | 修正请求 |
| 用户名不存在或密码错误 | `200 / 404`，两种提示 | `401 / 401`，统一提示“用户名或密码错误” | 留在登录页 |
| 密码正确但账号锁定 | `200 / 403` | `403 / 403` | 展示锁定提示 |
| 注册时用户名唯一约束冲突 | `200 / 500` | `409 / 409` | 提示换一个用户名 |
| 受保护接口缺失、过期或无法验证 Access Token | `200 / 401` | `401 / 401` | 通过刷新接口尝试续期一次 |
| 刷新 Cookie 缺失 | `200 / 500` | `401 / 40102` | 清理登录状态并跳转登录页 |
| 刷新令牌格式错误、已过期或已被替换 | 格式错误或哈希不匹配时为 `401 / 40102` | `401 / 40102` | 清理登录状态并跳转登录页 |
| 已认证但缺少业务权限 | 角色和权限检查待实现 | `403 / 403` | 展示无权限提示 |
| Controller 或 Service 内出现未处理的系统异常 | `200 / 500` | `500 / 500` | 提示稍后重试 |

前端只在受保护接口认证失败时触发刷新，登录失败直接在表单显示。多个请求同时需要刷新时，共用一个正在进行的刷新请求。刷新成功后用新 Access Token 重发原请求一次，刷新接口自身不进入这个重试流程。遇到 `40102` 才清理会话，遇到网络或系统故障则提示重试。

后端实现目标契约时，让 Controller 和全局处理器统一设置 HTTP 状态和 JSON。刷新时先校验旧令牌并准备新 JWT，再用 Redis 原子比较并替换哈希与 TTL，最后写 Cookie。原子比较能保证两个同时到达的请求只有一个能消费同一枚旧刷新令牌。授权阶段由 Realm 加载角色和权限，并在业务入口执行权限检查。`/test/t2` 当前会拼接用户实体文本，联调时仅用练习账号，后续改为只返回必要字段，避免输出密码哈希。

## 如何在 PowerShell 验证

准备 Java 17、Maven、MySQL 和 Redis。用 `sql/01_schema.sql` 初始化独立的本地练习库，按 `src/main/resources/application.yaml` 配置连接。运行 `mvn spring-boot:run` 后，先验证无需数据服务的分支。

```powershell
$base = 'http://localhost:8092'
curl.exe -i "$base/test/t1"
curl.exe -i "$base/test/t2"
'{}' | curl.exe -i -H 'Content-Type: application/json' --data-binary '@-' "$base/user/login"
curl.exe -i -X POST "$base/user/login/refresh"
curl.exe -i -X POST --cookie 'refreshToken=bad' "$base/user/login/refresh"
```

每次同时看第一行 HTTP 状态和响应体 `code`。现有实现中，以上请求依次对应 `200` 文本 `t1`、`200 / 401`、`200 / 400`、`200 / 500`、`401 / 40102`。

完整链路可在 Postman 中按下面的顺序练习。使用独立练习账号和测试数据库，避免覆盖已有会话。

1. 用登录请求示例将 `isLogin` 改为 `0` 注册，再以 `1` 登录。检查 JSON 中的 Access Token，以及响应头中的 `Set-Cookie`。
2. 用 Access Token 访问 `/test/t2`，再移除 `Authorization` 重试。对照两个请求，确认后端用什么判断登录状态。
3. 向 `/user/login/refresh` 发送 Cookie 中的原始值，检查新 Access Token 与新 Cookie。使用明文 HTTP 做本地练习时，可在测试客户端手动设置 `Cookie: refreshToken=<值>`，以便单独验证后端。浏览器自动发送 Cookie 的场景使用 HTTPS 再验证。
4. 依次提交旧刷新令牌和新刷新令牌。预期旧令牌返回 `401 / 40102`，新令牌可再次轮换。刷新前后执行 `TTL login:refreshtoken:<userId>`，可观察成功刷新把剩余有效期重设到接近 7 天。

练习时每条请求都记录「前端传了什么、后端如何判断、前端接下来做什么」。尤其可以比较同样需要重新登录的两条路径，为什么当前缺 Cookie 和坏 Cookie 返回不同，以及如何用目标契约让两者走同一处理分支。

## 本次测试记录

2026-11-06 使用 Spring Boot MockMvc 加载应用上下文和 Shiro 过滤链。上面的五条基础用例全部通过，实际状态与文档中的当前行为一致。当前环境启动 Tomcat 时无法建立回环连接，注册、登录成功、浏览器自动携带 Cookie 以及 Redis 轮换和 TTL 留待本地完整链路验证。
