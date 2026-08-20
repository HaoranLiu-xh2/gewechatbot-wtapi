<img width="2168" height="1610" alt="d9efd887537e98c688e65450be1982d3" src="https://github.com/user-attachments/assets/29cf8473-adf9-4b22-a48e-e1ec80a0d7f7" />


# gewechatbot-wtapi

基于 WTAPI 实现的微信个人号机器人接口，支持自动消息回复、关键词触发、群聊管控、私聊自动应答，可对接 ChatGPT 做智能聊天、消息定时推送。

---

## 👉 WTAPI🤖

个人微信智能机器人 API 框架，支持二次开发、任意语言接入，RESTful API 调用。

### 框架优势

WTAPI 采用轻量化 RESTful API 设计，接入门槛低，开发者无需关注微信底层协议细节。区别于传统方案，本框架无需安装电脑微信客户端，无需手机破解插件，只需扫码登录即可完成设备绑定，操作简单、部署方便，是个人微信自动化业务的稳定接入方案。

后端基于 **Spring Boot 3.x + Java 17** 构建，架构清晰、扩展性强，支持 SQLite 快速体验和 MySQL 生产部署两种模式。

### 主要能力

- **消息自动化**：给指定对象（好友、群组）发送文本、图片、文件、视频等消息。
- **自定义消息处理**：自动回复、关键词回复、AI 回复、RPA 自动化业务交互。
- **联系人管理**：获取好友列表、同步联系人信息、查询历史消息。
- **账号管理**：扫码登录、登录状态轮询、退出登录、多账号列表管理。
- **消息回调**：接收微信消息推送，实现被动触发与实时处理。
- **业务模型接入**：可对接 ChatGPT、Dify、Coze、DeepSeek 等 AI 模型及客服系统。
- **完善的后台管理**：基于 Web 的管理端，支持用户注册登录、账号管理、消息查看。

基于 WTAPI，你可以创造更多有趣且实用的功能...

---

## 免责声明【必读】

⚠️ 本框架仅供个人学习、研究和娱乐使用，严禁用于任何违法违规、骚扰他人、群发营销、欺诈等商用或非法场景。使用本框架所产生的一切法律责任由使用者自行承担。

---

## 🚀 快速入门

### 环境准备

- 系统：CentOS 7 / Ubuntu 22.04
- 配置：4 核 8G 及以上
- Docker：建议 26.x 稳定版本

### 安装 Docker

```bash
# 1. 安装基础依赖
yum -y install gcc gcc-c++ yum-utils

# 2. 配置镜像源
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
yum makecache fast

# 3. 安装 Docker
yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. 启动 Docker 并设置开机自启
systemctl start docker
systemctl enable docker.service
```

### 启动 WTAPI 服务

> 请将以下命令中的镜像地址、端口、挂载目录替换为 WTAPI 实际发布版本。

```bash
# 1. 拉取镜像
docker pull {WTAPI镜像地址}:latest

# 2. 运行容器
mkdir -p /root/wtapi-data
docker run -itd \
  -v /root/wtapi-data:/app/data \
  -p 8080:8080 \
  --name=wtapi \
  --restart=always \
  {WTAPI镜像名称}

# 3. 设置开机自启
docker update --restart=always wtapi
```

服务启动后，访问 `http://{服务ip}:8080` 即可进入管理端。

---

## API 服务调用

### 接口基地址

```text
http://{服务ip}:8080/api/{接口路径}
```

### 认证方式

WTAPI 采用 JWT Token 鉴权，登录成功后获取 token，后续请求在 Header 中携带：

```text
Authorization: Bearer {token}
```

---

## 基本用法（Java 示例）

其他语言执行 RESTful 接口可实现相同功能，支持各类语言接入。

```java
// 1. 注册账号
String registerUrl = "http://{服务ip}:8080/api/register";
Map<String, String> registerBody = new HashMap<>();
registerBody.put("username", "admin");
registerBody.put("password", "123456");
registerBody.put("confirmPassword", "123456");
// 调用注册接口...

// 2. 登录获取 token
String loginUrl = "http://{服务ip}:8080/api/login";
Map<String, String> loginBody = new HashMap<>();
loginBody.put("username", "admin");
loginBody.put("password", "123456");
String token = callLoginApi(loginUrl, loginBody);

// 3. 将 token 放入请求头
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer " + token);

// 4. 获取微信登录二维码
String qrUrl = "http://{服务ip}:8080/api/wx/login-qr";
Map<String, Object> qrBody = new HashMap<>();
qrBody.put("appId", ""); // 首次登录传空
Map<String, Object> qrResult = callPostApi(qrUrl, headers, qrBody);

// 5. 轮询检查登录状态
String checkUrl = "http://{服务ip}:8080/api/wx/check-login";
Map<String, Object> checkBody = new HashMap<>();
checkBody.put("appId", qrResult.get("appId"));
checkBody.put("uuid", qrResult.get("uuid"));
Map<String, Object> loginResult = callPostApi(checkUrl, headers, checkBody);

// 6. 登录成功后，可调用以下模块实现不同功能
GET  /api/wx/list              // 查询已登录账号列表
GET  /api/wx/contact/list      // 查询联系人列表
POST /api/wx/message/send-text // 发送文本消息
POST /api/wx/message/send-image// 发送图片消息
POST /api/wx/message/send-file // 发送文件消息
POST /api/wx/message/send-video// 发送视频消息
GET  /api/wx/message/list      // 查询历史消息
POST /api/wx/message/callback  // 接收消息回调
```

## 界面
<img width="2560" height="1317" alt="6eb6c883d1e246374e4ea7447a259f18" src="https://github.com/user-attachments/assets/c6605f13-730a-4d9c-a684-79fa1e490025" />
<img width="2560" height="1317" alt="53f026f4fe7f2d27d6d5e628fd284baa" src="https://github.com/user-attachments/assets/39561ea6-cfeb-4c1c-bad3-e891e71a3349" />

---

## 注意事项

1. 系统环境推荐：CentOS 7 或 Ubuntu 22.04。
2. 硬件环境推荐：4 核 8G 及以上。
3. 请确保服务器 8080 端口未被占用。
4. 服务启动后需要访问外网，请保证服务器出网正常。
5. 为保证账号稳定，建议将服务部署到与登录微信同省的网络环境中。
6. 本框架面向个人娱乐和学习使用，请勿用于任何商用场景。
7. 首次部署后请先调用注册接口创建账号，再登录获取 Token。
8. 官网www.chuapi.com
9. <img width="606" height="618" alt="image" src="https://github.com/user-attachments/assets/d981f77a-cdfa-4eeb-9c8f-e7288fbb8f71" />
10. 企鹅2560102000
---

## 版本更新

### 1.0.0

- WTAPI 正式版发布。
- 支持微信扫码登录、联系人管理、消息收发、消息回调。
- 提供后台管理端，支持用户账号管理和多微信账号管理。
- 支持 SQLite 快速体验和 MySQL 生产部署。

### 6.8.20

- WTAPI 版本更新。
- 更新 Demo 示例，新增群发消息相关功能，支持批量消息发送能力，完善示例演示。
