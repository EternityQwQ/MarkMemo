# MdNote - Android Markdown 笔记应用

> **本项目 100% 由 AI 驱动开发完成，无任何人工编码干预。**

一款基于 Material Design 3 设计规范的 Android Markdown 笔记应用，使用 Jetpack Compose 构建，提供简洁优雅的笔记体验。

---

## 功能特性

- **Markdown 编辑与预览** — 内置 Markdown 渲染引擎，支持实时预览
  - 标题 (H1-H6)
  - 粗体 / 斜体 / 删除线
  - 行内代码 & 代码块
  - 引用块
  - 无序列表
  - 链接
  - 分割线

- **笔记管理** — 完整的 CRUD 操作
  - 创建 / 编辑 / 删除笔记
  - 笔记置顶
  - 字数统计
  - 内容预览摘要

- **分类系统** — 四大分类快速筛选
  - 工作 / 个人 / 学习 / 其他
  - 分类筛选与标签展示

- **搜索** — 实时搜索笔记标题和内容

- **排序方式** — 三种排序可选
  - 最近修改
  - 最近创建
  - 按标题

- **Material Design 3 主题**
  - 浅色 / 深色模式
  - 跟随系统主题
  - 动态取色 (Android 12+)
  - 完整的 MD3 色彩体系

- **数据持久化** — Room 数据库本地存储
- **偏好设置** — DataStore 持久化用户偏好

---

## 技术栈

| 技术 | 用途 |
|------|------|
| Kotlin | 开发语言 |
| Jetpack Compose | UI 框架 |
| Material Design 3 | 设计系统 |
| Room | 本地数据库 |
| Navigation Compose | 页面导航 |
| DataStore Preferences | 偏好存储 |
| ViewModel | 状态管理 |
| Flow | 响应式数据流 |

---

## 项目结构

```
mdnote/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/mdnote/app/
│       │   ├── MainActivity.kt              # 主 Activity
│       │   ├── MdNoteApp.kt                 # Application 类
│       │   ├── data/
│       │   │   ├── db/
│       │   │   │   ├── NoteDatabase.kt      # Room 数据库
│       │   │   │   └── NoteDao.kt           # 数据访问层
│       │   │   ├── model/
│       │   │   │   └── Note.kt              # 笔记实体
│       │   │   └── repository/
│       │   │       └── NoteRepository.kt    # 数据仓库
│       │   ├── ui/
│       │   │   ├── components/
│       │   │   │   ├── MarkdownRenderer.kt  # Markdown 渲染器
│       │   │   │   ├── NoteCard.kt          # 笔记卡片
│       │   │   │   └── SearchBar.kt         # 搜索栏
│       │   │   ├── navigation/
│       │   │   │   ├── NavGraph.kt          # 导航图
│       │   │   │   └── Screen.kt            # 路由定义
│       │   │   ├── screens/
│       │   │   │   ├── NoteEditScreen.kt    # 编辑页
│       │   │   │   ├── NoteListScreen.kt    # 列表页
│       │   │   │   ├── NotePreviewScreen.kt # 预览页
│       │   │   │   └── SettingsScreen.kt    # 设置页
│       │   │   └── theme/
│       │   │       ├── Color.kt             # 调色板
│       │   │       ├── Theme.kt             # 主题
│       │   │       └── Type.kt              # 字体排版
│       │   └── viewmodel/
│       │       ├── NoteEditViewModel.kt     # 编辑 ViewModel
│       │       ├── NoteListViewModel.kt     # 列表 ViewModel
│       │       └── SettingsViewModel.kt     # 设置 ViewModel
│       └── res/
│           └── values/
│               ├── strings.xml
│               └── themes.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 开发进度

| 模块 | 状态 | 说明 |
|------|------|------|
| 项目结构与构建配置 | 已完成 | Gradle KTS 配置、依赖管理 |
| 数据层 (Model/DAO/Database/Repository) | 已完成 | Room 数据库 + Flow 响应式查询 |
| MD3 主题系统 | 已完成 | 浅色/深色/动态取色全支持 |
| Markdown 渲染引擎 | 已完成 | 自研 Compose Markdown 解析器 |
| 笔记列表页 | 已完成 | 搜索、排序、分类筛选、空状态 |
| 笔记编辑页 | 已完成 | 标题/内容编辑、分类选择、字数统计 |
| 笔记预览页 | 已完成 | Markdown 渲染、删除确认 |
| 设置页 | 已完成 | 主题切换、排序偏好、动态取色开关 |
| 导航系统 | 已完成 | Navigation Compose 多页面路由 |
| 测试 | 待开发 | - |
| 导出功能 | 待开发 | Markdown / PDF 导出 |
| 云同步 | 待开发 | - |

---

## AI 驱动开发声明

本项目是 **完全由 AI 驱动的开发实验**：

- 所有代码文件由 AI 生成，未经过人工手动编写或修改
- 功能设计由 AI 自主决策，不受人工限制
- 架构设计、技术选型、UI 布局均由 AI 独立完成
- 项目旨在验证 AI 在完整 Android 应用开发中的能力边界

---

## 构建运行

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.x

### 构建步骤

```bash
# 克隆项目
git clone <repo-url>
cd mdnote

# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

或在 Android Studio 中直接打开项目运行。

---

## 最低要求

- Android 8.0 (API 26) 及以上
- 推荐 Android 12+ 以获得动态取色体验

---

## License

MIT License

---

*Made with AI - No human code written*