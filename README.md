# 弹药市场模拟应用 (Delta Force Demo)

弹股通——基于Android平台的模拟弹药市场应用，支持用户进行弹药类物品的买卖交易、仓库管理及交易记录查询等功能。

## 功能特点

### 1. 用户账号管理
- 支持用户注册与登录功能
- 记录最近登录用户，提供快速选择历史账号登录的便捷方式
- 通过`UserAccountManager`统一管理账号信息，数据本地持久化存储

### 2. 市场交易系统
- 展示多种规格弹药（如7.62x39mm、5.56x45mm等）的市场物品信息，包括名称、价格、库存等
- 市场物品价格和库存每8秒自动波动，每日0点重置为初始值
- 支持物品搜索过滤功能，快速定位特定弹药

### 3. 仓库管理
- 通过`WarehouseManager`管理用户仓库中的物品，支持存入和取出操作
- 仓库数据通过SharedPreferences本地存储，确保数据持久化
- 实时同步市场与仓库的物品库存变化

### 4. 货币与交易记录
- 内置"哈夫币"货币系统，由`HafCurrencyManager`管理用户余额
- 支持"金手指"快速添加货币功能（用于测试/演示）
- `TransactionManager`记录所有买卖交易历史，可随时查看过往记录

## 技术栈

- **开发语言**：Java
- **平台框架**：Android SDK
- **数据序列化**：Gson
- **UI展示**：ListView + Adapter模式
- **定时任务**：Handler + Timer（实现价格/库存波动）
- **本地存储**：SharedPreferences
- **构建工具**：Gradle

## 安装与运行

1. 克隆代码库到本地：
   ```bash
   git clone https://github.com/your-username/2025-android-shi-s-class.git
   ```

2. 使用Android Studio打开项目

3. 配置Android SDK（建议API 25及以上）

4. 连接Android设备或启动模拟器，点击运行按钮

## 许可证
本项目遵循**GNU General Public License v3.0**开源协议，详情请查看[LICENSE](LICENSE)文件。

## 联系方式

如需问题反馈或合作洽谈，请联系：[your-email@example.com]
