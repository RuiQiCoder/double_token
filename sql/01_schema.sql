-- 用户名密码登录、注册与 RBAC 权限管理 demo
-- 目标数据库：MySQL 8.0.16+（CHECK 约束需要此版本或更高版本）
-- 使用方式：先创建并选中一个空数据库，再执行本脚本。
-- 本脚本用于首次初始化，不可重复执行；不包含删除已有表的操作。
-- 所有连接统一使用 UTC 保存时间，展示时由应用转换时区。
SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    username VARCHAR(64) NOT NULL COMMENT '登录用户名，不区分大小写，应用层去除首尾空白',
    password_hash VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'BCrypt 等安全密码哈希，禁止保存明文或直接 SHA/MD5',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    CONSTRAINT ck_sys_user_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci COMMENT='用户';

CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
    role_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '角色标识，例如 admin、user，对应 Shiro 角色',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code),
    CONSTRAINT ck_sys_role_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci COMMENT='角色';

CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限 ID',
    permission_code VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '权限标识，例如 user:read，对应 Shiro 权限字符串',
    permission_name VARCHAR(64) NOT NULL COMMENT '权限名称',
    description VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (permission_code),
    CONSTRAINT ck_sys_permission_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci COMMENT='接口及操作权限';

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, role_id),
    KEY idx_sys_user_role_role (role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci COMMENT='用户与角色多对多关联';

CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (role_id, permission_id),
    KEY idx_sys_role_permission_permission (permission_id),
    CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci COMMENT='角色与权限多对多关联';

-- 初始化角色和用于测试的权限。不预置固定密码或默认管理员账号。
START TRANSACTION;

INSERT INTO sys_role (id, role_code, role_name, description) VALUES
    (1, 'admin', '管理员', '通过显式权限关联管理用户和角色'),
    (2, 'user', '普通用户', '注册后由服务端分配的默认角色');

INSERT INTO sys_permission (id, permission_code, permission_name, description) VALUES
    (1, 'profile:read', '查看本人资料', '建议测试接口 GET /api/me，用户 ID 从认证主体获取'),
    (2, 'user:read', '查看用户列表', '建议测试接口 GET /api/users'),
    (3, 'user:disable', '禁用用户', '建议测试接口 PATCH /api/users/{id}/status'),
    (4, 'role:assign', '分配用户角色', '建议测试接口 PUT /api/users/{id}/roles');

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4),
    (2, 1);

COMMIT;

-- 后续实现约定：
-- 1. 注册：插入 sys_user，并分配 role_code='user' 的角色，必须在同一事务中完成。
--    不接受注册请求自行传入角色或权限；用户名冲突依赖唯一索引兜底。
-- 2. 管理员：通过受控初始化流程给指定用户绑定 admin，不开放自助注册管理员。
-- 3. Shiro：只加载启用用户、启用角色、启用权限；JWT 的 subject 使用用户 ID。
--    admin 角色本身不会自动绕过权限检查，新增权限需显式关联。
-- 4. 密码：推荐 BCrypt，哈希串本身包含盐及成本参数，因此不另设 salt 字段。
-- 5. Redis：保存登录会话或撤销状态，并按 Token 有效期设置 TTL；无需 JWT 数据表。
--    JWT 签名与过期时间仍须验证，若要求禁用立即生效，每次请求需检查 Redis 会话状态。
-- 6. Caffeine：缓存权限查询结果；用户禁用或角色/权限变更后，需要同步失效 Redis
--    与各应用实例的本地缓存。只清一个实例的缓存无法保证多实例即时生效。
-- 7. 删除：先显式删除关联记录再删除实体；本 demo 不引入软删除与审计历史。
SHOW DATABASES;