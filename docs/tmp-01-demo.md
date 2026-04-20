

```text
Spring Cloud Alibaba 与 Seata 版本要兼容



按你用的 Spring Boot 2.x，分两个主流分支（2.6.x/2.7.x、2.4.x/2.5.x），给出官方验证、无冲突、可直接复制的版本组合，Seata Server 必须和客户端版本一致：

✅ 推荐组合（Spring Boot 2.6.x/ 2.7.x，最常用）
Spring Boot：2.7.14（2.6.13 也兼容）
Spring Cloud：2021.0.5 (Jubilee)
Spring Cloud Alibaba：2021.0.5.0
Seata（客户端 + Server）：1.6.1
Nacos：2.2.3+

✅ 兼容组合（Spring Boot 2.4.x/ 2.5.x）
Spring Boot：2.4.13 / 2.5.14
Spring Cloud：2020.0.6 (Ilford)
Spring Cloud Alibaba：2.2.8.RELEASE
Seata：1.4.2（必须用这个，不能用 1.5+）

❌ 禁止混用（必踩坑）
Spring Cloud Alibaba 2021.x 系列 → 必须配 Seata 1.5.x/ 1.6.x，严禁用 1.4.x
Spring Cloud Alibaba 2.2.x 系列 → 必须配 Seata 1.4.x，严禁用 1.5+
Seata Server 版本 ≠ 客户端版本 → 全局事务不生效、连接失败、回滚异常
```