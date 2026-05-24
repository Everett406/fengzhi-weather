# 风止天气 v1.0.0

首次发布！

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

## 数据来源

- 天气数据：[和风天气](https://www.qweather.com/)
- 卫星云图：[NICT](https://himawari8.nict.go.jp/) / JMA
- 降水雷达：[RainViewer](https://www.rainviewer.com/)

## 使用说明

1. 在 [和风天气开发者平台](https://dev.qweather.com/) 注册并获取 API Key
2. 在 APP 设置中配置 API Key（或通过 local.properties 构建）
3. 添加城市即可使用

## 技术栈

- Kotlin 1.9.22 + Jetpack Compose + Material 3
- MVVM 架构 + Hilt 依赖注入
- Retrofit + OkHttp + Coil
