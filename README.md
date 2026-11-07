# Login Lab

Vue 3 前端和 Spring Boot 后端分离，默认由 Nginx 提供页面及反向代理。

```text
浏览器 http://127.0.0.1:8080
                │
              Nginx
                ├── 页面及 /assets/* → dist/ Vue 构建文件
                └── /user/*、/test/* → Spring Boot 127.0.0.1:8092
                                             └── MySQL / Redis
```

## 启动项目

1. 初始化 `sql/01_schema.sql`，启动 MySQL、Redis。后端配置在 `src/main/resources/application.yaml`，执行 `mvn spring-boot:run`。
2. 执行：

   ```powershell
   npm install
   npm start
   ```

3. 打开 **http://127.0.0.1:8080/**。无需启动 Vite，网页由 Nginx 托管。

脚本自动查找 PATH 内的 `nginx.exe`，或本机已有的 `D:/nginx/hmdp/nginx-1.18.0/nginx.exe`。其他安装位置可先指定：

```powershell
$env:NGINX_EXE = 'D:/tools/nginx/nginx.exe'
npm start
```

脚本以工作区根目录作为 Nginx prefix，读取 `nginx/nginx.conf`，不会修改安装目录下其他项目的配置。8080 被占用会提示，不会强制结束其他进程。默认只监听本机 `127.0.0.1:8080`。

## Nginx 管理

```powershell
npm run nginx:status   # 当前项目实例状态
npm run nginx:test     # 检查配置
npm run nginx:reload   # 校验配置后平滑重载
npm run nginx:stop     # 优雅停止当前项目实例
npm start              # 重新构建；未运行则启动，已运行则重载
```

运行文件保存在 `.runtime/nginx/`：`access.log`、`error.log`、`nginx.pid` 和临时目录。根目录 `logs/` 用于 Nginx 初始化阶段日志，两者均已忽略。

后端地址在 `nginx/nginx.conf` 的 `upstream login_backend` 中配置，修改后执行 `npm run nginx:reload`。修改 Vue 源码后执行 `npm start`。HTML 禁用缓存，带内容哈希的 `/assets/` 文件使用长期缓存。

`/healthz` 只验证 Nginx；`/test/t1` 同时验证后端代理。后端停机时接口返回 502，不会回退到 Vue HTML。接口的 401、403、409 及 JSON 原样透传。

## 登录与 Cookie

- 登录与注册调用 `POST /user/login`，`isLogin` 分别为字符串 `1`、`0`；注册后需登录。
- accessToken 保存在 Local Storage 的 `accessToken` 键中，刷新页面后读取；后续请求使用 `Authorization: Bearer ...`。浏览器禁止本地存储时回退到页面内存。
- `POST /user/login/refresh` 携带 HttpOnly refreshToken Cookie；成功后前端替换 accessToken，浏览器处理新的 Set-Cookie。
- `/test/t2` 收到 401 时刷新并最多重试一次，并发刷新合并。403 表示权限不足，不刷新。
- Nginx 保留 `/user`、`/test` 原路径，不增加 `/api` 前缀；Cookie Path 保持 `/user/login/refresh`。所有请求均同源，无需配置 CORS。
- 浏览器存储区属于所用主机，请始终从同一主机访问。切换入口后重新登录。
- 登录和刷新成功后同步替换本地 accessToken。刷新失败、“清除本页会话”或切换模拟模式时删除此键。后端没有注销接口，清除访问令牌不会撤销刷新 Cookie。
- 本地 HTTP 允许非 Secure Cookie；生产 HTTPS 应统一为登录和刷新 Cookie 启用 Secure，并为 Nginx 配置证书。

无后端时开启右上角“本地模拟”，使用 `demo_user` / `Demo@123`。模拟账号只存在页面内存，不写 Cookie。

## 开发与验证

```powershell
npm test
npm run build
mvn -DskipTests compile
```

需要热更新时才使用 `npm run dev`，Vite 备用入口为 `http://127.0.0.1:5173`。`BACKEND_URL` 仅影响此开发代理，Nginx 后端地址由自己的配置控制。`npm run preview` 只预览构建文件，不提供接口代理；正常联调使用 `npm start` 的 Nginx 入口。
