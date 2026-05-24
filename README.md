# 风止天气 (Fengzhi Weather)

一款功能丰富的气象聚合应用，支持卫星云图、雷达图、天气预报等功能，使用 Jetpack Compose 构建。

## 功能特性

### 🌤️ 天气信息
- 实时天气展示（温度、体感温度、湿度、气压、风速）
- 24 小时逐小时预报
- 7 天天气预报
- 空气质量指数（AQI）及污染物详情
- 天气预警信息

### 🛰️ 卫星云图
- 向日葵 8/9 号卫星云图
- 三种波段切换（可见光、红外、水汽）
- 过去 24 小时时间轴
- 动画播放功能
- 双指缩放查看

### 📡 降水雷达
- 实时降水雷达图
- 未来 2 小时降水预报
- 时间轴动画播放
- 降水强度颜色图例

### ⚙️ 设置功能
- 城市管理（添加、删除、切换）
- 主题切换（跟随系统、浅色、深色）
- 自动刷新间隔设置
- 温度/风速单位切换

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 1.9.22 |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Hilt 依赖注入 |
| 网络 | Retrofit + OkHttp + Gson |
| 图片加载 | Coil |
| 图表 | Vico |
| 数据存储 | DataStore Preferences |
| 位置服务 | Play Services Location |

## 数据来源

- **天气数据**：[和风天气](https://www.qweather.com/) (QWeather)
- **卫星云图**：[NICT](https://himawari8.nict.go.jp/) / JMA 向日葵 8/9 号
- **降水雷达**：[RainViewer](https://www.rainviewer.com/)

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
│   │   ├── home/     # 首页天气
│   │   ├── satellite/# 卫星云图
│   │   ├── radar/    # 雷达图
│   │   └── settings/ # 设置
│   └── theme/        # 主题配置
└── utils/            # 工具类
```

## 构建说明

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.4

### API Key 配置

1. 在 [和风天气开发者平台](https://dev.qweather.com/) 注册账号并获取 API Key
2. 在项目根目录创建 `local.properties` 文件：
   ```properties
   QWEATHER_API_KEY=你的API密钥
   ```

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

## 下载安装

从 [Releases](https://github.com/Everett406/fengzhi-weather/releases) 页面下载最新版本的 APK。

## 版本历史

### v1.0.0 (2026-05-24)
- 🎉 初始版本发布
- ✅ 首页天气功能（实时天气、预报、空气质量、预警）
- ✅ 卫星云图功能（向日葵 8/9 号，波段切换，动画播放）
- ✅ 降水雷达功能（实时雷达，预报动画）
- ✅ 设置页面（城市管理，主题切换，单位设置）

## 许可证

MIT License

## 致谢

- [和风天气](https://www.qweather.com/) - 天气数据 API
- [NICT](https://himawari8.nict.go.jp/) - 向日葵卫星云图
- [RainViewer](https://www.rainviewer.com/) - 降水雷达数据
