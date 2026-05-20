# Stock Manager 新人上手指南

## 1. 代码库整体结构

这是一个 **Maven 多模块项目**，根目录 `pom.xml` 聚合了三个模块：

- `stock-common`：公共模型与工具（DTO/VO、统一响应、常量、枚举、加密与Token工具）。
- `stock-backend`：Spring Boot 后端（Controller/Service/Mapper/Entity 分层，含权限、登录拦截、操作日志等）。
- `stock-client`：JavaFX 客户端（FXML 视图 + Controller + 简单 API 客户端）。

你可以把它理解为：

- `common` 定义“数据契约”；
- `backend` 提供“业务与接口”；
- `client` 消费接口并提供桌面端 UI。

---

## 2. 各模块重点

### 2.1 stock-common（先看）

建议先熟悉以下目录：

- `model/dto/create|update|query`：请求入参模型。
- `model/vo`：接口返回对象。
- `model/Result`、`model/PageResult`：统一响应包装。
- `enums`、`constant`：状态码、删除状态、权限类型、业务常量。

为什么先看这里：

- 新人最快建立“系统支持哪些业务对象和操作”的全局认知；
- 后续看后端接口和前端调用时，能快速对齐字段语义。

### 2.2 stock-backend（核心业务）

后端是标准分层：

- `controller`：路由层，按实体一一对应（如 `GoodsController`、`StockController`）。
- `service` / `service/impl`：业务规则。
- `mapper`：MyBatis-Plus 数据访问接口。
- `entity`：数据库实体。
- `config`、`interceptor`、`handler`、`aspect`：基础设施（鉴权、异常、响应包装、日志等）。

关键机制：

1. **登录/权限链路**：`LoginInterceptor`、`PermissionInterceptor` + `UserContext`。
2. **统一异常与响应**：`GlobalExceptionHandler`、`GlobalResponseAdvice`。
3. **审计日志**：`OperationLogAspect` + `OperateLog` 相关服务。
4. **通用 CRUD 基建**：`BaseService`/`BaseServiceImpl`，很多模块复用。

### 2.3 stock-client（桌面端）

客户端结构轻量：

- `fxml`：页面布局（登录页、主页面、模块表单等）。
- `controller`：UI 事件与调用逻辑。
- `util/ApiClient`：HTTP 调用封装。
- `model/Session`：会话态。

新人重点：看清 `LoginController -> Session -> ApiClient -> MainController` 的最小闭环，能快速理解前后端如何交互。

---

## 3. 需要优先了解的重要内容（按优先级）

1. **数据模型与命名规则**（common 模块 DTO/VO/Enum）。
2. **鉴权与权限体系**（User / Role / Permission / Token 以及拦截器行为）。
3. **库存主链路**（Goods、Stock、StockRecord、StockOrder、RequestForm/RequestItem）。
4. **统一错误码与国际化消息**（`ResultCode`、`messages.properties`）。
5. **数据库初始化与核心表结构**（`sql/handk_stock.sql`、`sql/mock_full.sql`）。

---

## 4. 推荐学习路径（2~3 周）

### 第 1 阶段（1~2 天）

- 读根 `pom.xml` 与三个子模块 `pom.xml`，了解依赖与模块边界。
- 浏览 `stock-common` 全部包，画出“实体/DTO/VO”关系草图。

### 第 2 阶段（3~5 天）

- 从登录开始：`LoginController`、`LoginServiceImpl`、`UserToken`、拦截器。
- 跑通一次“查询商品 + 库存变更”链路，从 Controller 跟到 Mapper。

### 第 3 阶段（1 周）

- 挑 1 个模块（如 `StockRecord`）做一次小改造：加一个查询字段或返回字段。
- 同步修改 common 的 DTO/VO、backend 的 service/mapper、client 的界面展示。

### 第 4 阶段（持续）

- 复盘重复逻辑，沉淀通用方法到 BaseService 或工具类。
- 补充模块级文档：接口说明、字段字典、边界条件。

---

## 5. 给新人的实操建议

- **先看“贯穿流程”，再看“局部细节”**：避免陷入单文件阅读。
- **任何字段都追到定义处**：DTO/VO/Entity 三处一致性要特别注意。
- **改动尽量小步提交**：一个业务点一个 commit，便于回滚与评审。
- **优先补测试数据脚本与日志**：比“只改代码”更快定位问题。
- **遇到不确定规则先查枚举/常量**：这个项目很多业务语义沉淀在 `constant` 和 `enums` 中。

