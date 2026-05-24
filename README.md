# 风止天气 (Fengzhi Weather)

一款简洁美观的天气应用，使用 Jetpack Compose 构建。

## 功能特性

- 实时天气信息展示
- 7天天气预报
- 小时级天气预报
- 空气质量指数
- 位置自动定位
- 深色/浅色主题切换

## 技术栈

- **UI**: Jetpack Compose + Material 3
- **架构**: MVVM + Hilt 依赖注入
- **网络**: Retrofit + OkHttp + Gson
- **图片加载**: Coil
- **图表**: Vico
- **数据存储**: DataStore Preferences
- **位置服务**: Play Services Location

## 项目结构

```
app/src/main/java/com/fengzhi/weather/
├── data/
│   ├── api/          # API 接口定义
│   ├── local/        # 本地数据存储
│   ├── model/        # 数据模型
│   └── repository/   # 数据仓库
├── di/               # 依赖注入模块
├── ui/
│   ├── components/   # 可复用组件
│   ├── navigation/   # 导航配置
│   ├── screens/      # 页面
│   └── theme/        # 主题配置
└── utils/            # 工具类
```

## 构建说明

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.4

### 构建命令

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 运行测试
./gradlew test

# 运行 lint 检查
./gradlew lint
```

## 版本历史

### v1.0.0 (2024-XX-XX)
- 初始版本发布
- 基础天气功能
- 主题切换支持

## 许可证

MIT License
