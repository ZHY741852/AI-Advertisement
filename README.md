# AI-Advertisement

一个AI广告安卓客户端开发项目-字节跳动工程训练营

> AI 智能广告信息流应用 — 基于大语言模型的广告摘要、标签生成与智能推荐

## 项目介绍

AI-Advertisement 是一款基于 **Jetpack Compose + MVVM + Clean Architecture** 开发的现代化 Android 广告信息流应用。核心特色是将大语言模型能力深度集成到广告浏览体验中：

- **AI 智能摘要**：自动为每条广告生成简洁有吸引力的摘要文案
- **AI 分类标签**：自动识别广告的品类、风格、受众、场景四个维度，生成结构化标签
- **AI 助手对话**：用户可通过自然语言查询和筛选广告内容
- **多频道信息流**：精选/电商/杭州本地/美食/数码/旅游/时尚 七大频道分类
- **卡片动态化**：大图卡片/小图卡片/视频卡片三种形态，数据驱动渲染

## 功能概览

| 功能 | 描述 |
|------|------|
| 广告信息流 | 7 个频道 + 分页加载 + 下拉刷新 + 标签筛选 + 滚动位置记忆 |
| 卡片动态化 | 大图/小图/视频三种卡片类型，由后端数据驱动 |
| AI 智能摘要 | 调用通义千问 API 生成摘要，内存缓存避免重复请求 |
| AI 分类标签 | 品类/风格/受众/场景四维标签，不同颜色区分展示 |
| AI 对话助手 | 自然语言交互，智能广告推荐 |
| 视频播放 | 内置 ExoPlayer，支持卡片内联播放与详情页全屏播放 |
| 详情页 | 视频自动播放 + AI 摘要卡 + 互动操作 |
| 数据统计 | 曝光量/点击量/点赞/收藏/分享 多维统计 |
| 频道记忆 | 每个频道独立记忆滚动位置 |

## 技术栈

| 类别 | 技术 | 用途 |
|------|------|------|
| 语言 | Kotlin 1.9+ | 主要开发语言 |
| UI 框架 | Jetpack Compose + Material3 | 声明式 UI |
| 架构 | MVVM + Clean Architecture | 分层架构 |
| 依赖注入 | Hilt | DI 管理 |
| 网络 | Retrofit + OkHttp | HTTP 客户端 |
| 序列化 | kotlinx-serialization | JSON 解析 |
| 图片 | Coil | Compose 图片加载 |
| 视频 | ExoPlayer | 视频流媒体播放 |
| 存储 | MMKV | 轻量级键值存储 |
| AI | 通义千问 (Qwen) | 大语言模型 |
| 构建 | Gradle 8.5 / AGP 8.2 | 构建系统 |

## 模块划分

```
app/src/main/java/com/aiadvertisement/
├── ui/                          # 展示层 (Compose UI)
│   ├── screen/home/             # 首页信息流 (HomeScreen + ViewModel)
│   ├── screen/detail/           # 广告详情页
│   ├── screen/chat/             # AI 助手对话页
│   ├── screen/search/           # AI 搜索页
│   ├── screen/stats/            # 数据统计页
│   ├── components/              # 公共组件 (AdFeedCard/VideoPlayer/CachedImage/InteractionBar)
│   ├── navigation/              # 路由定义 (Screen.kt)
│   ├── theme/                   # Material3 主题
│   └── MainActivity.kt          # 主入口 + NavGraph
├── domain/                      # 领域层 (纯业务逻辑，无框架依赖)
│   ├── model/AdFeed.kt          # 广告领域模型
│   └── usecase/                 # 业务用例 (GenerateAdSummary/SearchWithAI/StreamChat)
├── data/                        # 数据层
│   ├── mock/MockDataProvider.kt # Mock 数据供给 (硬编码 + JSON assets)
│   ├── repository/              # 仓储实现 (AIRepository/AdRepository)
│   ├── model/                   # DTO 数据传输对象
│   └── mapper/                  # DTO → Domain 映射器
├── core/                        # 核心模块
│   ├── ai/                      # 通义千问 API 客户端 (QwenClient/QwenService/AIConfig)
│   ├── storage/                 # MMKV 本地存储 (ScrollPositionStore/InteractionStateStore)
│   ├── model/                   # 通用数据模型 (AIAdEnhance/AITag/AdStats)
│   ├── network/                 # 网络配置与拦截器
│   ├── common/                  # Result<T>/UiState<T> 基类
│   ├── image/                   # Coil 配置
│   └── exception/               # 异常处理
└── di/AppModule.kt              # Hilt 依赖注入模块
```

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17+
- Android SDK 34+
- Gradle 8.5+

### 构建与运行

```bash
# 1. 克隆仓库
git clone <your-repo-url>
cd AI-Advertisement

# 2. 配置 API Key（可选，不影响核心功能使用）
# 编辑 app/src/main/java/com/aiadvertisement/core/ai/AIConfig.kt
# 将 API_KEY 替换为你的通义千问 API Key
# 不配置时 AI 功能使用本地 fallback 方案

# 3. 构建 Debug APK
./gradlew assembleDebug

# 4. 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 最低要求

- Android 8.0 (API 26) 及以上

## 开发规范

1. **分层原则**：UI → Domain ← Data，Domain 层零依赖
2. **状态管理**：ViewModel 通过 StateFlow 暴露状态，View 单向订阅
3. **组件复用**：公共 UI 组件放在 `ui/components/` 下，使用 `@Composable` 函数
4. **命名约定**：数据类用 `data class`，DTO 以 `DTO` 后缀区分，Mapper 函数命名为 `toDomain()`
5. **异常处理**：统一使用 `Result<T>` 封装，UI 层通过 `UiState.error` 展示
6. **Compose Key**：`LazyColumn` 的 `items` 必须带 `key` 参数，`remember` 必须绑定业务 ID
7. **资源释放**：ExoPlayer 等重量级对象在 `DisposableEffect.onDispose` 中释放

## AI 使用声明

### 使用情况
- **AI 工具**：Trae（基于 DeepSeek-V4-Pro）
- **使用范围**：UI 组件代码生成、数据模型定义、Mock 数据构造、网络层代码、文档撰写
- **代码生成占比**：约 90%（AI 辅助生成），人工负责架构决策、跨模块数据流设计、代码重构与整合
- **文档生成占比**：约 95%（AI 辅助撰写），人工负责审校与调整

### 对功能的理解
- 广告信息流的核心体验在于"内容+AI":内容由 mock 数据驱动展示,AI 增强内容在卡片渲染时异步加载
- 卡片动态化的本质是"数据驱动 UI":卡片类型由 `AdCardType` 枚举决定,AI 内容通过独立的 `AISummaryRow`/`AITagRow` 组件叠加渲染

### 对 AI 结果的验证方式
1. **编译验证**：Kotlin 编译器对 `data class`、`sealed class` 的严格检查保证数据模型正确性
2. **真机运行测试**：所有页面在真机上完整走通（首页滚动→详情页→视频播放→AI 摘要加载→统计查看）
3. **边缘 case 手动测试**：无网络时 AI 功能优雅降级、视频无 URL 时只展示封面、JSON 损坏时的 fallback 逻辑

### 对 AI 输出的结构化约束
- AI 生成的摘要和标签强制使用 JSON Schema 约束，通过 `kotlinx.serialization` 反序列化验证
- 标签分类使用 `TagCategory` 枚举限制（CATEGORY/STYLE/AUDIENCE/SCENE），避免模型自由发挥
- 解析失败自动降级到本地 fallback，保证 UI 始终有内容展示

## 演示视频

> 请将演示视频（3-8 分钟）放在本仓库根目录下，命名为 `demo.mp4`

## 文档索引

- [技术设计文档](docs/技术设计文档.md) — 架构设计、难点分析与方案对比、效果评估
- [学习总结](docs/学习总结.md) — 棘手问题与解决方案、踩坑复盘、可改进点