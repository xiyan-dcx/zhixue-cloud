-- ======================================================================
--  智学云 zhixue-cloud  数据库初始化脚本
--  适用：MySQL 8.x（数据看板用到窗口函数 ROW_NUMBER / LAG，必须 8.0+）
--  说明：完整初始化，包含章节讲义、学习清单、令牌版本与模拟购买记录。
--  默认登录：admin / 123456（超级管理员）、teacher01 / 123456（讲师）、
--           student01 / 123456（学员）
-- ======================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS zhixue DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE zhixue;

DROP TABLE IF EXISTS simulated_purchase;
DROP TABLE IF EXISTS my_course;

-- ----------------------------
-- 1. 角色表 sys_role
--    注意：实体 SysRole.orderNum 标了 @TableField(exist=false)，但 SysRoleService
--    分页时用它排序会生成 order_num 列，故这里必须建 order_num 列。
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(50)  NOT NULL COMMENT '角色标识：admin/teacher/student',
    order_num   INT          DEFAULT 0 COMMENT '显示顺序',
    status      TINYINT      DEFAULT 0 COMMENT '状态：0正常 1停用',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

INSERT INTO sys_role (id, role_name, role_key, order_num, status, create_time, update_time) VALUES
(1, '超级管理员', 'admin',   1, 0, NOW(), NOW()),
(2, '讲师',       'teacher', 2, 0, NOW(), NOW()),
(3, '学员',       'student', 3, 0, NOW(), NOW());

-- ----------------------------
-- 2. 用户表 sys_user
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 密文）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    role_id     BIGINT       DEFAULT NULL COMMENT '角色ID',
    status      TINYINT      DEFAULT 0 COMMENT '状态：0正常 1停用',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 密码均为 123456 的 BCrypt 密文
INSERT INTO sys_user (id, username, password, nickname, avatar, role_id, status, create_time, update_time) VALUES
(1, 'admin',     '$2a$10$MvKkaUax82zoVqjeL71NYu3M47SqN1qERd7lONgccFfGkCgwQbudW', '超级管理员', NULL, 1, 0, NOW(), NOW()),
(2, 'teacher01', '$2a$10$MvKkaUax82zoVqjeL71NYu3M47SqN1qERd7lONgccFfGkCgwQbudW', '张老师',     NULL, 2, 0, NOW(), NOW()),
(3, 'student01', '$2a$10$MvKkaUax82zoVqjeL71NYu3M47SqN1qERd7lONgccFfGkCgwQbudW', '小学员',     NULL, 3, 0, NOW(), NOW());

-- ----------------------------
-- 3. 菜单/权限表 sys_menu
--    menu_type：M目录 C菜单 F按钮；perms 是 @PreAuthorize 的权限标识
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    parent_id   BIGINT       DEFAULT 0 COMMENT '父菜单ID，0为顶级',
    menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单名称',
    path        VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component   VARCHAR(255) DEFAULT NULL COMMENT '前端组件',
    perms       VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    menu_type   CHAR(1)      DEFAULT NULL COMMENT '类型：M目录 C菜单 F按钮',
    icon        VARCHAR(100) DEFAULT NULL COMMENT '图标',
    order_num   INT          DEFAULT 0 COMMENT '显示顺序',
    status      TINYINT      DEFAULT 0 COMMENT '状态：0正常 1停用',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '菜单权限表';

INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, menu_type, icon, order_num) VALUES
(1,  0, '数据看板', '/dashboard', 'dashboard/index', NULL,               'C', 'DataLine', 1),
(2,  0, '课程管理', '/course',    'course/index',    'course:list',       'C', 'Reading',  2),
(21, 2, '新增课程', '', '', 'course:add',    'F', '', 1),
(22, 2, '编辑课程', '', '', 'course:edit',   'F', '', 2),
(23, 2, '删除课程', '', '', 'course:remove', 'F', '', 3),
(3,  0, '学员管理', '/student',   'student/index',   'student:list',      'C', 'User',     3),
(31, 3, '新增学员', '', '', 'student:add',    'F', '', 1),
(32, 3, '编辑学员', '', '', 'student:edit',   'F', '', 2),
(33, 3, '删除学员', '', '', 'student:remove', 'F', '', 3),
(34, 3, '导入学员', '', '', 'student:import', 'F', '', 4),
(4,  0, '订单管理', '/order',     'order/index',     'order:list',        'C', 'List',     4),
(5,  0, '系统管理', '/system',    NULL,               NULL,               'M', 'Setting',  5),
(51, 5, '用户管理', '/system/user', 'system/user/index', 'system:user:list', 'C', 'Avatar', 1),
(511, 51, '新增用户', '', '', 'system:user:add',    'F', '', 1),
(512, 51, '编辑用户', '', '', 'system:user:edit',   'F', '', 2),
(513, 51, '删除用户', '', '', 'system:user:remove', 'F', '', 3);

-- ----------------------------
-- 4. 角色-菜单关联表 sys_role_menu（无实体类，被原生 SQL JOIN 使用，必须建）
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色菜单关联表';

-- admin：拥有全部菜单（侧边栏菜单也按本表查询，故必须分配）
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;

-- teacher：看板 + 课程 + 学员 + 订单（不含系统管理）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),
(2, 2), (2, 21), (2, 22), (2, 23),
(2, 3), (2, 31), (2, 32), (2, 33), (2, 34),
(2, 4);

-- student：看板 + 课程（只读，仅 course:list）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1),
(3, 2);

-- ----------------------------
-- 5. 课程表 course
-- ----------------------------
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    title         VARCHAR(100)  NOT NULL COMMENT '课程标题',
    cover         VARCHAR(255)  DEFAULT NULL COMMENT '封面图URL',
    teacher_id    BIGINT        DEFAULT NULL COMMENT '讲师ID',
    teacher_name  VARCHAR(50)   DEFAULT NULL COMMENT '讲师姓名',
    category      VARCHAR(50)   DEFAULT NULL COMMENT '分类',
    price         DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格',
    intro         VARCHAR(500)  DEFAULT NULL COMMENT '简介',
    student_count INT           DEFAULT 0 COMMENT '报名人数',
    status        TINYINT       DEFAULT 1 COMMENT '状态：0草稿 1上架 2下架',
    create_time   DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted       TINYINT       DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程表';

INSERT INTO course (id, title, cover, teacher_id, teacher_name, category, price, intro, student_count, status, create_time, update_time) VALUES
(1, 'Java 编程入门',        NULL, 101, '张伟', '后端',     199.00, '零基础学习 Java 语法、面向对象与常用 API。', 320, 1, NOW(), NOW()),
(2, 'Vue3 + TS 实战',       NULL, 102, '李娜', '前端',     299.00, '从组合式 API 到企业级后台管理系统实战。',     280, 1, NOW(), NOW()),
(3, 'Python 数据分析',      NULL, 103, '王芳', '数据',     259.00, 'NumPy/Pandas/可视化，玩转数据分析。',         210, 1, NOW(), NOW()),
(4, 'MySQL 从入门到精通',   NULL, 101, '张伟', '数据库',   199.00, '索引、事务、调优与窗口函数全掌握。',           260, 1, NOW(), NOW()),
(5, 'SpringBoot3 实战',     NULL, 101, '张伟', '后端',     399.00, '构建生产级 RESTful 后端服务。',                350, 1, NOW(), NOW()),
(6, '算法与数据结构',       NULL, 104, '刘强', '计算机基础', 349.00, '面试高频算法与数据结构精讲。',               180, 1, NOW(), NOW()),
(7, 'AI 大模型应用开发',    NULL, 103, '王芳', '人工智能', 599.00, '从 Prompt 到 RAG，落地大模型应用。',          400, 1, NOW(), NOW()),
(8, 'Linux 运维基础',       NULL, 104, '刘强', '运维',     159.00, '常用命令、Shell 脚本与服务部署。',             120, 1, NOW(), NOW());

-- ----------------------------
-- 6. 章节表 chapter
--    说明：真实项目前端预留了章节管理 UI 与接口，后端章节实现作为教程补全项，
--    本表配合补全后的 ChapterController 使用。
-- ----------------------------
DROP TABLE IF EXISTS chapter;
CREATE TABLE chapter (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '章节ID',
    course_id   BIGINT       NOT NULL COMMENT '所属课程ID',
    title       VARCHAR(100) NOT NULL COMMENT '章节标题',
    sort_order  INT          DEFAULT 0 COMMENT '排序',
    duration    INT          DEFAULT 0 COMMENT '时长（分钟）',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '章节表';

INSERT INTO chapter (course_id, title, sort_order, duration, create_time, update_time) VALUES
(1, '第1章 开发环境搭建', 1, 45, NOW(), NOW()),
(1, '第2章 基础语法',     2, 60, NOW(), NOW()),
(1, '第3章 面向对象',     3, 90, NOW(), NOW()),
(2, '第1章 Vite 工程化',  1, 50, NOW(), NOW()),
(2, '第2章 组合式 API',   2, 80, NOW(), NOW());

-- ----------------------------
-- 7. 学员表 student
-- ----------------------------
DROP TABLE IF EXISTS student;
CREATE TABLE student (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '学员ID',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    gender      VARCHAR(10)  DEFAULT NULL COMMENT '性别：男/女',
    status      TINYINT      DEFAULT 0 COMMENT '状态：0正常 1停用',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学员表';

INSERT INTO student (id, name, phone, email, gender, status, create_time, update_time) VALUES
(1,  '张三', '13800000001', 'zhangsan@zhixue.com', '男', 0, NOW(), NOW()),
(2,  '李四', '13800000002', 'lisi@zhixue.com',     '男', 0, NOW(), NOW()),
(3,  '王五', '13800000003', 'wangwu@zhixue.com',   '男', 0, NOW(), NOW()),
(4,  '赵丽', '13800000004', 'zhaoli@zhixue.com',   '女', 0, NOW(), NOW()),
(5,  '孙悦', '13800000005', 'sunyue@zhixue.com',   '女', 0, NOW(), NOW()),
(6,  '周杰', '13800000006', 'zhoujie@zhixue.com',  '男', 0, NOW(), NOW()),
(7,  '吴婷', '13800000007', 'wuting@zhixue.com',   '女', 0, NOW(), NOW()),
(8,  '郑爽', '13800000008', 'zhengshuang@zhixue.com', '女', 0, NOW(), NOW()),
(9,  '钱多', '13800000009', 'qianduo@zhixue.com',  '男', 0, NOW(), NOW()),
(10, '刘晓', '13800000010', 'liuxiao@zhixue.com',  '女', 0, NOW(), NOW());

-- ----------------------------
-- 8. 订单表 course_order（用于数据看板与窗口函数统计）
--    pay_time 以 NOW() 为基准动态生成，保证任何时间执行都有近30天/跨月数据
-- ----------------------------
DROP TABLE IF EXISTS course_order;
CREATE TABLE course_order (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no     VARCHAR(50)   NOT NULL COMMENT '订单号',
    student_id   BIGINT        DEFAULT NULL COMMENT '学员ID',
    student_name VARCHAR(50)   DEFAULT NULL COMMENT '学员姓名',
    course_id    BIGINT        DEFAULT NULL COMMENT '课程ID',
    course_title VARCHAR(100)  DEFAULT NULL COMMENT '课程标题',
    teacher_name VARCHAR(50)   DEFAULT NULL COMMENT '讲师姓名',
    amount       DECIMAL(10,2) DEFAULT 0.00 COMMENT '金额',
    status       TINYINT       DEFAULT 1 COMMENT '状态：0未支付 1已支付 2退款',
    pay_time     DATETIME      DEFAULT NULL COMMENT '支付时间',
    create_time  DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time  DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted      TINYINT       DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程订单表';

INSERT INTO course_order (order_no, student_id, student_name, course_id, course_title, teacher_name, amount, status, pay_time, create_time, update_time) VALUES
-- 近 30 天（报名趋势折线）
('NO20260001', 1,  '张三', 7, 'AI 大模型应用开发', '王芳', 599.00, 1, DATE_SUB(NOW(), INTERVAL 1  DAY), NOW(), NOW()),
('NO20260002', 2,  '李四', 2, 'Vue3 + TS 实战',    '李娜', 299.00, 1, DATE_SUB(NOW(), INTERVAL 2  DAY), NOW(), NOW()),
('NO20260003', 3,  '王五', 5, 'SpringBoot3 实战',  '张伟', 399.00, 1, DATE_SUB(NOW(), INTERVAL 3  DAY), NOW(), NOW()),
('NO20260004', 4,  '赵丽', 1, 'Java 编程入门',     '张伟', 199.00, 1, DATE_SUB(NOW(), INTERVAL 5  DAY), NOW(), NOW()),
('NO20260005', 5,  '孙悦', 7, 'AI 大模型应用开发', '王芳', 599.00, 1, DATE_SUB(NOW(), INTERVAL 7  DAY), NOW(), NOW()),
('NO20260006', 6,  '周杰', 2, 'Vue3 + TS 实战',    '李娜', 299.00, 1, DATE_SUB(NOW(), INTERVAL 9  DAY), NOW(), NOW()),
('NO20260007', 7,  '吴婷', 3, 'Python 数据分析',   '王芳', 259.00, 1, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW(), NOW()),
('NO20260008', 8,  '郑爽', 5, 'SpringBoot3 实战',  '张伟', 399.00, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW(), NOW()),
('NO20260009', 9,  '钱多', 1, 'Java 编程入门',     '张伟', 199.00, 1, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW(), NOW()),
('NO20260010', 10, '刘晓', 7, 'AI 大模型应用开发', '王芳', 599.00, 1, DATE_SUB(NOW(), INTERVAL 21 DAY), NOW(), NOW()),
('NO20260011', 1,  '张三', 6, '算法与数据结构',   '刘强', 349.00, 1, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW(), NOW()),
('NO20260012', 2,  '李四', 2, 'Vue3 + TS 实战',    '李娜', 299.00, 1, DATE_SUB(NOW(), INTERVAL 28 DAY), NOW(), NOW()),
-- 约 1 个月前
('NO20260013', 3,  '王五', 5, 'SpringBoot3 实战',  '张伟', 399.00, 1, DATE_SUB(NOW(), INTERVAL 35 DAY), NOW(), NOW()),
('NO20260014', 4,  '赵丽', 7, 'AI 大模型应用开发', '王芳', 599.00, 1, DATE_SUB(NOW(), INTERVAL 40 DAY), NOW(), NOW()),
('NO20260015', 5,  '孙悦', 4, 'MySQL 从入门到精通','张伟', 199.00, 1, DATE_SUB(NOW(), INTERVAL 45 DAY), NOW(), NOW()),
('NO20260016', 6,  '周杰', 2, 'Vue3 + TS 实战',    '李娜', 299.00, 1, DATE_SUB(NOW(), INTERVAL 50 DAY), NOW(), NOW()),
('NO20260017', 7,  '吴婷', 8, 'Linux 运维基础',   '刘强', 159.00, 1, DATE_SUB(NOW(), INTERVAL 55 DAY), NOW(), NOW()),
-- 约 2 个月前
('NO20260018', 8,  '郑爽', 5, 'SpringBoot3 实战',  '张伟', 399.00, 1, DATE_SUB(NOW(), INTERVAL 65 DAY), NOW(), NOW()),
('NO20260019', 9,  '钱多', 7, 'AI 大模型应用开发', '王芳', 599.00, 1, DATE_SUB(NOW(), INTERVAL 70 DAY), NOW(), NOW()),
('NO20260020', 10, '刘晓', 1, 'Java 编程入门',     '张伟', 199.00, 1, DATE_SUB(NOW(), INTERVAL 75 DAY), NOW(), NOW()),
('NO20260021', 1,  '张三', 3, 'Python 数据分析',   '王芳', 259.00, 1, DATE_SUB(NOW(), INTERVAL 80 DAY), NOW(), NOW()),
('NO20260022', 2,  '李四', 2, 'Vue3 + TS 实战',    '李娜', 299.00, 1, DATE_SUB(NOW(), INTERVAL 85 DAY), NOW(), NOW()),
-- 约 3 个月前
('NO20260023', 3,  '王五', 5, 'SpringBoot3 实战',  '张伟', 399.00, 1, DATE_SUB(NOW(), INTERVAL 95  DAY), NOW(), NOW()),
('NO20260024', 4,  '赵丽', 6, '算法与数据结构',   '刘强', 349.00, 1, DATE_SUB(NOW(), INTERVAL 100 DAY), NOW(), NOW()),
('NO20260025', 5,  '孙悦', 7, 'AI 大模型应用开发', '王芳', 599.00, 1, DATE_SUB(NOW(), INTERVAL 105 DAY), NOW(), NOW()),
('NO20260026', 6,  '周杰', 1, 'Java 编程入门',     '张伟', 199.00, 1, DATE_SUB(NOW(), INTERVAL 110 DAY), NOW(), NOW()),
-- 约 4 个月前
('NO20260027', 7,  '吴婷', 2, 'Vue3 + TS 实战',    '李娜', 299.00, 1, DATE_SUB(NOW(), INTERVAL 125 DAY), NOW(), NOW()),
('NO20260028', 8,  '郑爽', 4, 'MySQL 从入门到精通','张伟', 199.00, 1, DATE_SUB(NOW(), INTERVAL 130 DAY), NOW(), NOW()),
-- 未支付、退款各一条（演示状态过滤）
('NO20260029', 9,  '钱多', 5, 'SpringBoot3 实战',  '张伟', 399.00, 0, NULL, NOW(), NOW()),
('NO20260030', 10, '刘晓', 7, 'AI 大模型应用开发', '王芳', 599.00, 2, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;


-- ===== 合并模块：chapters-demo.sql =====
-- 在 zhixue 数据库执行；保留已有章节，只为同名章节补充空讲义。
-- 可重复执行，不覆盖用户已编辑的讲义，不恢复逻辑删除的章节。
SET NAMES utf8mb4;
SET @chapter_content_ddl = IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chapter' AND column_name = 'content'),
  'SELECT 1', 'ALTER TABLE chapter ADD COLUMN content TEXT NULL COMMENT ''纯文本章节讲义''');
PREPARE chapter_ddl FROM @chapter_content_ddl;
EXECUTE chapter_ddl;
DEALLOCATE PREPARE chapter_ddl;

CREATE TEMPORARY TABLE chapter_demo (
  course_title VARCHAR(200), title VARCHAR(100), sort_order INT, duration INT, content TEXT
) CHARACTER SET utf8mb4;

INSERT INTO chapter_demo VALUES
('Java 编程入门','第1章 开发环境搭建',1,45,'【示例讲义】
学习目标：区分 JDK、JVM 与 IDE，独立编译运行第一个程序。
知识讲解：JDK 提供 javac 编译器和 java 启动命令。源文件经编译生成字节码，再由 JVM 执行。IDE 是辅助开发工具，不等于 JDK。
操作实践：创建 Hello.java，在 Hello 类中声明 public static void main(String[] args)，使用 System.out.println("Hello Java"); 输出文本。执行 javac Hello.java，再执行 java Hello。
检查：分别运行 java -version、javac -version；使用 Maven 时再检查 mvn -v，确认它没有引用另一套 JDK。
练习：将输出改为自己的姓名，并解释源文件与 class 文件的区别。'),
('Java 编程入门','第2章 基础语法',2,60,'【示例讲义】
学习目标：使用变量、条件分支和循环表达业务规则。
知识讲解：int 表示整数，boolean 表示真假，String 表示字符串；if 处理条件，for 遍历已知范围，while 按条件重复执行。整数相除会舍弃小数部分。
示例：int total = 0; for (int i = 1; i <= 100; i++) { total += i; } 最终 total 为 5050。
练习：给定成绩数组，计算平均分及及格人数。先处理空数组，再考虑除法是否需要转换为 double。
自测：== 与 String.equals 的用途有什么不同？答案：前者比较基本类型值或对象引用，后者可比较字符串内容。'),
('Java 编程入门','第3章 面向对象',3,90,'【示例讲义】
学习目标：用类、对象与方法建模课程信息。
知识讲解：类描述数据与行为，对象是类的实例；封装通过受控方法维护有效状态，接口描述行为契约，组合让对象协作。
实践：设计 Course 类，包含 title 和 price。通过构造方法设置初始值，在修改价格的方法中拒绝负数；创建两个课程对象并打印各自标题。
练习：定义 Learnable 接口和 study() 方法，让 VideoCourse 与 TextCourse 分别实现它。
自测：修改一个对象的普通实例字段是否会影响另一个对象？通常不会；共享 static 字段则需要单独考虑。'),
('Java 编程入门','第4章 集合与异常处理',4,60,'【示例讲义】
学习目标：使用 List 和 Map 管理课程，并处理输入错误。
知识讲解：List 保留元素顺序并允许重复；Map 用键查找值。异常应在能够处理它的边界捕获，不应空 catch 后假装成功。
实践：用 List<String> 保存课程标题，用 Map<Long, String> 根据课程 ID 查询标题。查询不存在的键时为用户提供明确提示。
练习：将输入字符串转换为整数，捕获 NumberFormatException；测试空字符串、字母和合法数字。
自测：如何自动关闭文件流？使用 try-with-resources 管理实现 AutoCloseable 的资源。'),
('Vue3 + TS 实战','第1章 Vite 工程化',1,50,'【示例讲义】
学习目标：理解项目入口、开发服务与生产构建。
知识讲解：index.html 加载 main.ts，main.ts 创建 Vue 应用并挂载 App.vue。开发服务辅助调试，生产构建生成可部署的静态文件。
实践：阅读本项目 package.json 的 scripts，执行 npm run dev，按终端地址打开页面；再运行 npm run build，检查 dist 目录。
排错：出现 Failed to resolve import 时，先检查文件是否存在、路径大小写以及 @ 别名配置。
练习：添加一个 Welcome.vue 并正确导入，确认修改文本后页面能够更新。'),
('Vue3 + TS 实战','第2章 组合式 API',2,80,'【示例讲义】
学习目标：掌握 ref、reactive 与 computed 的使用场景。
知识讲解：ref 保存响应式值，在脚本中通过 .value 访问；reactive 包装对象；computed 根据依赖计算派生值，不用于直接发起写入请求。
实践：用 ref(0) 保存已完成章节数，用 computed 计算完成率；总章节数为零时返回 0，避免除零。
练习：实现课程关键词筛选，将原始列表和筛选后的列表分开管理。
自测：为什么异步请求需要 loading 和错误状态？它们让用户区分正在加载、无数据和加载失败。'),
('Vue3 + TS 实战','第3章 TypeScript 与组件通信',3,60,'【示例讲义】
学习目标：为章节数据定义类型，通过 props 和事件传递数据。
知识讲解：interface 描述对象结构，联合类型表达有限状态；类型检查发生在开发阶段，不能替代后端参数校验。
实践：定义 Chapter 接口，包含 id、title、duration 和 content。父组件传入章节，子组件发出 select 事件通知父组件。
练习：实现一个章节列表组件，使用章节 id 作为 key，选中章节后在右侧显示讲义。
自测：子组件是否应直接修改 props？不应直接修改，应通过事件由父组件更新。'),
('Vue3 + TS 实战','第4章 路由、请求与权限',4,70,'【示例讲义】
学习目标：串联登录、路由跳转、数据请求与权限反馈。
知识讲解：路由守卫控制页面访问，接口拦截器统一处理请求和错误。隐藏按钮只影响界面，真正的数据权限必须在后端校验。
实践：登录后加载用户身份，按角色进入首页；直接输入无权访问的地址时跳转到可访问页面。
练习：给章节请求添加失败重试，并测试退出后重新登录不同角色的菜单。
自测：浏览器能否安全保存数据库密码？不能，数据库连接信息只能由后端管理。'),
('Python 数据分析','第1章 Python 与数据结构',1,45,'【示例讲义】
学习目标：使用列表、字典和函数组织学习记录。
知识讲解：列表存储有序元素，字典存储键值映射；函数通过参数接收输入并通过 return 返回结果，避免依赖隐式全局变量。
实践：records = [{"name": "小明", "minutes": 30}, {"name": "小李", "minutes": 45}]；通过 sum(item["minutes"] for item in records) 得到总时长 75。
练习：编写计算平均学习时长的函数，空列表返回 0。
自测：读取不存在的字典键可能发生什么？直接索引会触发 KeyError，可按业务需要使用 get。'),
('Python 数据分析','第2章 表格读取与清洗',2,60,'【示例讲义】
学习目标：识别缺失值、重复记录和不一致的数据类型。
知识讲解：读取 CSV 或 Excel 后先检查列名、行数、类型和缺失分布。不能为了消除报错而无条件删除所有空行或重复值。
实践：使用 pandas 读取学员表，检查 df.info()，按明确的学员唯一标识判断重复；手机号应按字符串处理，保留前导零。
练习：将异常日期单独列入错误清单，保留原始文件，记录每一步清洗造成的行数变化。
自测：同名学员能否直接去重？不能，姓名不是可靠的唯一标识。'),
('Python 数据分析','第3章 分组聚合与指标',3,60,'【示例讲义】
学习目标：按课程计算报名人数、学习时长和完成率。
知识讲解：分组聚合前必须明确一行代表什么。订单数、报名人数和独立学员数不是同一指标，连接表可能放大记录数。
实践：对学习记录按 course_id 分组，对学员 ID 去重计数，对有效时长求和；完成率使用已完成报名关系数除以有效报名关系数。
练习：用 6 条手工记录分别算出结果，再与程序输出核对。
自测：分母为零时怎么处理？展示无数据或按约定返回零，并说明统计口径。'),
('Python 数据分析','第4章 图表与分析报告',4,50,'【示例讲义】
学习目标：选择合适图表并形成可验证结论。
知识讲解：折线图表现时间趋势，柱状图比较类别。图表必须标注时间范围、单位和数据来源；相关变化不能直接证明因果关系。
实践：绘制最近 7 天新增学员柱状图，补全没有新增的日期，明确零值与缺失值的区别。
练习：提交一页分析报告，包含一个发现、支持它的数据、可能的其他解释和后续验证方案。
自测：为何不能只展示增长率？基数很小时百分比容易放大变化，需要同时给出绝对数量。'),
('MySQL 从入门到精通','第1章 表设计与基础查询',1,50,'【示例讲义】
学习目标：理解主键、字段类型与 SELECT 查询。
知识讲解：主键唯一标识记录；金额使用 DECIMAL，文本选择合适长度，时间字段明确语义。逻辑删除后普通查询要主动排除已删除记录。
实践：SELECT id, title, price FROM course WHERE deleted = 0 AND status = 1 ORDER BY id DESC LIMIT 10;
练习：查询价格在 100 至 300 元的上架课程，只返回所需列。
自测：为什么数据库返回顺序不能默认依赖插入顺序？没有 ORDER BY 时不保证结果顺序。'),
('MySQL 从入门到精通','第2章 关联查询与统计',2,60,'【示例讲义】
学习目标：通过课程与章节关联统计章节数。
知识讲解：INNER JOIN 只保留匹配行，LEFT JOIN 保留左表记录。统计零章节课程时使用 LEFT JOIN，并把章节过滤条件放入 ON。
示例：SELECT c.id, COUNT(ch.id) FROM course c LEFT JOIN chapter ch ON ch.course_id=c.id AND ch.deleted=0 WHERE c.deleted=0 GROUP BY c.id;
练习：扩展查询，统计每门课程的预计学习总时长，并将空聚合值处理为零。
自测：为什么不用 COUNT(*)？左连接未匹配时仍有一条左表记录，会导致零章节被计为一。'),
('MySQL 从入门到精通','第3章 索引与执行计划',3,60,'【示例讲义】
学习目标：依据查询条件设计索引并阅读执行计划。
知识讲解：索引提升特定读取效率，也增加写入和存储成本。联合索引需要结合过滤条件、排序和数据分布评估，不能只按字段数量决定。
实践：对按 course_id、deleted 过滤并按 sort_order 排序的章节查询执行 EXPLAIN，观察使用的索引与估计扫描行数。
练习：在测试库对比有无相应索引时的查询计划和耗时。
自测：小表一次查询更快能否证明索引设计合理？不能，需要代表性的规模和多次测量。'),
('MySQL 从入门到精通','第4章 事务与数据一致性',4,70,'【示例讲义】
学习目标：用事务保证多个关联写入共同成功或失败。
知识讲解：事务具有原子性、一致性、隔离性和持久性。并发更新需要考虑锁、隔离级别和重复请求，事务不能自动解决所有业务冲突。
实践：设计订单支付成功后开通课程权限的流程，为同一支付事件建立唯一标识，重复通知不得重复开通或重复计数。
练习：在测试库执行 START TRANSACTION，修改示例数据后 ROLLBACK，确认数据恢复。
自测：是否应把很慢的外部网络请求放在持锁事务中？应尽量避免，以减少长事务和锁等待。'),
('SpringBoot3 实战','第1章 工程启动与配置',1,50,'【示例讲义】
学习目标：识别启动类、依赖管理和配置文件职责。
知识讲解：Spring Boot 自动配置根据类路径和配置条件创建组件；starter 需要匹配所使用的 Boot 大版本。Maven 使用的 JDK 由实际运行环境决定。
实践：阅读 pom.xml、启动类和 application.yaml，检查数据库地址与服务端口，执行项目提供的 dev.cmd。
练习：将测试环境端口改为 8081，确认前端代理目标同步调整。
自测：编译成功是否代表数据库可用？不代表，还需要验证连接、表结构和业务查询。'),
('SpringBoot3 实战','第2章 REST 接口与参数校验',2,60,'【示例讲义】
学习目标：实现结构清晰的章节查询和新增接口。
知识讲解：Controller 处理请求，Service 组织业务，Mapper 访问数据库。使用专门的请求字段和校验规则，拒绝空标题、负时长和不存在的课程。
实践：为新增章节定义 POST 接口，为查询定义 GET 接口，成功与错误响应保持一致结构。
练习：分别提交合法数据、空标题和非法课程 ID，记录返回结果。
自测：前端已经校验为何后端还要校验？接口可以被绕过界面直接调用。'),
('SpringBoot3 实战','第3章 MyBatis-Plus 数据访问',3,70,'【示例讲义】
学习目标：实现条件查询、分页、排序和逻辑删除。
知识讲解：实体字段映射数据库列，BaseMapper 提供常用操作，Wrapper 构造条件。避免将整个请求实体直接用于修改，防止意外更新受保护字段。
实践：按课程 ID 查询未删除章节，按 sort_order 和 id 排序；分页查询课程时限制最大页大小。
练习：编辑章节只更新标题、时长、顺序和讲义，不允许修改所属课程。
自测：逻辑删除等于物理删除吗？不是，记录仍保留，需要查询规则与唯一性设计共同配合。'),
('SpringBoot3 实战','第4章 身份认证与权限测试',4,70,'【示例讲义】
学习目标：理解认证和授权的区别并验证角色边界。
知识讲解：认证回答用户是谁，授权回答其可以执行什么操作。角色权限之外，还需要校验资源归属，例如讲师是否拥有该课程。
实践：对章节写接口增加权限校验；使用学生身份调用新增、编辑和删除接口，确认没有数据变化。
练习：编写管理员允许、学生拒绝和未登录拒绝三类测试。
自测：只隐藏学生界面的编辑按钮是否安全？不够，后端必须独立拒绝越权请求。'),
('算法与数据结构','第1章 复杂度与数组',1,50,'【示例讲义】
学习目标：用输入规模描述算法的时间与空间开销。
知识讲解：大 O 描述增长趋势，不是精确耗时。数组按下标读取通常是常数时间，查找未排序元素可能需要遍历。
实践：比较两段求和代码：一次循环遍历 n 项与两层循环各遍历 n 项，分别分析执行次数。
练习：编写查找数组最大值的方法，只遍历一次；测试空数组和全负数数组。
自测：O(n) 算法是否一定比 O(n log n) 在小输入上快？不一定，常数和实现开销也有影响。'),
('算法与数据结构','第2章 栈、队列与哈希表',2,60,'【示例讲义】
学习目标：根据访问顺序选择合适的数据结构。
知识讲解：栈后进先出，适合括号匹配；队列先进先出，适合任务排队；哈希表用于按键查找，需处理冲突。
实践：扫描括号字符串，左括号入栈，右括号与栈顶匹配，最后检查栈为空。
练习：设计课程标题出现次数统计，用哈希表保存计数。
自测：队列与优先队列的取出顺序相同吗？不同，优先队列依据优先级决定顺序。'),
('算法与数据结构','第3章 二分查找与排序',3,60,'【示例讲义】
学习目标：在有序数据中进行二分查找并正确处理边界。
知识讲解：二分查找要求明确的有序关系，每一步缩小候选区间。区间采用闭区间还是左闭右开必须保持一致。
实践：对排序后的课程价格数组查找目标价格，使用 mid = left + (right - left) / 2 计算中点。
练习：寻找第一个大于等于目标值的位置；测试空数组、重复值和目标超出范围的情况。
自测：能否直接对任意未排序数组使用二分查找？不能。'),
('算法与数据结构','第4章 树与图的遍历',4,70,'【示例讲义】
学习目标：理解 DFS 与 BFS 并应用于目录结构。
知识讲解：深度优先沿分支深入，广度优先逐层扩展。图可能存在环，遍历时要记录已访问节点；树也应防御异常循环数据。
实践：将课程、章和小节建模为树，递归打印目录，输出层级缩进。
练习：使用队列按层输出目录，并统计每一层的节点数量。
自测：层次很深时递归有什么风险？可能耗尽调用栈，可考虑显式栈和深度限制。'),
('AI 大模型应用开发','第1章 应用结构与模型调用',1,50,'【示例讲义】
学习目标：认识用户界面、应用后端与模型服务的职责。
知识讲解：应用后端组织输入、调用模型并处理结果。模型输出可能出错，不能直接视为可信业务指令；密钥保存在服务端配置中。
实践：设计课程学习助手接口，接收课程 ID 和问题，返回回答及引用来源；为调用设置超时和错误反馈。
练习：画出从学员提问到回答展示的数据流，标出哪些数据不能发送给模型。
自测：模型 API 密钥是否可以写入前端源码？不能。'),
('AI 大模型应用开发','第2章 提示设计与结构化输出',2,60,'【示例讲义】
学习目标：给出明确任务、约束、输入边界和输出格式。
知识讲解：提示应说明目的、受众和可用资料；要求结构化输出后仍要用程序校验字段、长度和取值范围。
实践：让模型依据一段章节讲义生成三道自测题，要求每题包含题干、选项、正确答案和解释，并核对答案是否能由讲义支持。
练习：准备正常输入、资料不足和含无关指令的输入，比较模型表现。
自测：来自课件或网页的文本是否应自动获得系统指令权限？不应，它们是待处理资料。'),
('AI 大模型应用开发','第3章 检索增强问答',3,70,'【示例讲义】
学习目标：理解讲义切分、检索、回答和引用的流程。
知识讲解：检索增强生成先查找相关资料，再让模型基于资料回答。检索前后都需要按用户可访问课程过滤，防止跨课程泄露内容。
实践：按章节和段落切分讲义，保留课程 ID、章节 ID 与来源位置；提问后返回相关段落及引用。
练习：分别测试资料中已有答案和资料中没有答案的问题，后者应明确说明依据不足。
自测：增加检索条数是否一定提高答案质量？不一定，无关资料可能引入干扰。'),
('AI 大模型应用开发','第4章 评估与使用成本',4,60,'【示例讲义】
学习目标：建立小型评估集并观察质量、时延与成本。
知识讲解：离线评估需固定问题集、资料版本和评分标准。分别评估答案正确性、引用支持程度、拒答表现和权限隔离。
实践：准备 20 个课程问题，记录回答是否有依据、响应时间和用量；提示或检索策略修改后重新对比。
练习：给出两种失败案例，定位是检索遗漏、上下文不足还是生成错误。
自测：只用一次满意回答能否证明系统可靠？不能，需要覆盖正常与边界场景。'),
('Linux 运维基础','第1章 文件系统与常用命令',1,45,'【示例讲义】
学习目标：理解路径、目录和基础文件操作。
知识讲解：绝对路径从根目录开始，相对路径基于当前目录。执行影响文件的命令前，先确认当前位置和目标路径。
实践：在专用练习目录中使用 pwd、ls、mkdir、cat，建立课程日志文件并阅读内容。
练习：用 cp 复制练习文件，用 mv 重命名，确认源文件和目标文件各自状态。
自测：路径包含空格时应如何处理？对完整路径正确加引号，避免被拆分成多个参数。'),
('Linux 运维基础','第2章 用户、权限与进程',2,60,'【示例讲义】
学习目标：理解读写执行权限和进程身份。
知识讲解：文件权限按所有者、组和其他用户分配；服务应使用所需的最小权限。进程 ID 用于定位进程，停止前确认归属。
实践：用 ls -l 查看练习文件权限，用 ps 查看自己启动的进程，识别命令和 PID。
练习：创建只允许自己读写的练习文件，解释权限变化。
自测：为什么不应把 chmod 777 当作通用修复？它会授予所有用户过多权限并掩盖实际配置问题。'),
('Linux 运维基础','第3章 网络与日志排查',3,60,'【示例讲义】
学习目标：区分服务未启动、端口错误和请求失败。
知识讲解：先检查进程和监听端口，再检查本机 HTTP 响应，最后分析代理与网络。日志应记录时间、级别和请求标识，避免记录密码及令牌。
实践：使用 ss -lnt 查看监听端口，使用 curl -I 请求测试服务，使用 tail 查看最近日志。
练习：模拟请求不存在的路径，区分连接失败、404 和应用业务错误。
自测：HTTP 200 是否一定表示业务成功？不一定，还要检查接口约定的响应内容。'),
('Linux 运维基础','第4章 部署与运行维护',4,70,'【示例讲义】
学习目标：建立可重复的部署、健康检查与回滚流程。
知识讲解：部署需记录版本、配置、启动方式和日志位置；配置与密钥应独立管理。健康检查与业务检查分别覆盖进程存活和关键功能。
实践：在测试环境部署后端与前端，检查监听端口、代理路径和接口文档，再执行一条只读业务请求。
练习：编写部署检查单，包含备份、数据库迁移、启动、验证和失败回滚步骤。
自测：有备份文件是否等于能恢复？不等于，需要定期进行恢复验证。');

START TRANSACTION;
UPDATE chapter ch
JOIN course c ON c.id = ch.course_id AND c.deleted = 0
JOIN chapter_demo d ON d.course_title = c.title AND d.title = ch.title
SET ch.content = d.content, ch.update_time = NOW()
WHERE ch.deleted = 0 AND (ch.content IS NULL OR TRIM(ch.content) = '');

INSERT INTO chapter (course_id, title, sort_order, duration, content, create_time, update_time, deleted)
SELECT c.id, d.title, d.sort_order, d.duration, d.content, NOW(), NOW(), 0
FROM chapter_demo d JOIN course c ON c.title = d.course_title AND c.deleted = 0
WHERE NOT EXISTS (SELECT 1 FROM chapter ch WHERE ch.course_id = c.id AND ch.title = d.title);
COMMIT;
DROP TEMPORARY TABLE chapter_demo;


-- ===== 合并模块：my-courses-profile.sql =====
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS my_course (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL COMMENT '登录账号ID，不是student表ID',
 course_id BIGINT NOT NULL COMMENT '课程ID',
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_my_course_user_course(user_id,course_id),
 KEY idx_my_course_user_time(user_id,create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人学习清单，非报名或购买凭证；移出时物理删除';

SET @profile_ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns
 WHERE table_schema=DATABASE() AND table_name='sys_user' AND column_name='token_version'),
 'SELECT 1', 'ALTER TABLE sys_user ADD COLUMN token_version INT NOT NULL DEFAULT 0 COMMENT ''登录令牌版本，改密时递增''');
PREPARE profile_ddl FROM @profile_ddl;
EXECUTE profile_ddl;
DEALLOCATE PREPARE profile_ddl;


-- ===== 合并模块：simulated-purchase.sql =====
CREATE TABLE IF NOT EXISTS simulated_purchase (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 order_no VARCHAR(64) NOT NULL,
 user_id BIGINT NOT NULL,
 course_id BIGINT NOT NULL,
 course_title VARCHAR(200) NOT NULL,
 amount DECIMAL(10,2) NOT NULL,
 status TINYINT NOT NULL DEFAULT 1 COMMENT '1=模拟支付成功，不涉及真实资金',
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_simulated_user_course(user_id,course_id),
 UNIQUE KEY uk_simulated_order_no(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟购买记录，与真实订单隔离';
