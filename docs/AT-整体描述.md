
```text
前提约定



1. TM = 订单服务（同时也是 RM1）
2. RM2 = 库存服务
3. RM3 = 账户服务
4. TC = Seata Server
5. 所有 RM 都被 Seata DataSourceProxy 代理

一阶段：undo_log 插入 + 业务 SQL 执行 + 本地事务提交
二阶段：全局提交（删 undo_log）或 全局回滚（恢复数据 + 删 undo_log）
```

```text
场景一：正常提交（无异常，全部成功）


1. TM 开启全局事务
    执行 @GlobalTransactional
    TM 通过 RPC 调用 TC：begin()
    TC 生成全局 XID，返回 TM
    XID 放入 ThreadLocal
2. RM1（订单服务）执行 insert order
    DataSource 被拦截，进入 AT 流程
    开启本地事务(即setAutoCommit(false))
    生成 beforeImage（插入 undo_log）
    RM1 → TC：branchRegister 注册分支 + 申请全局锁
    TC 内存加锁，返回 branchId
    执行 insert order
    生成 afterImage(插入 undo_log)
    本地事务提交（订单入库 + undo_log 一同提交）
    数据库行锁释放，全局锁仍持有
3. 调用库存服务（RM2）
    XID 通过 Feign 透传。
    RM2 执行 update stock
    开启本地事务
    生成 beforeImage
    RM2 → TC：branchRegister + 申请全局锁
    TC 加锁成功
    执行 update
    生成 afterImage(插入 undo_log)
    本地事务提交
    数据库行锁释放
4. 调用账户服务（RM3）
    流程同上，最终：
    余额扣减完成
    undo_log 已写
    本地提交
    持有全局锁
5. TM 发起全局提交
    业务代码正常结束
    TM → TC：globalCommit(XID)
6. TC 异步二阶段提交
    TC对RM1/RM2/RM3 分别发送：branchCommit(XID, branchId)
    每个 RM 做：
    根据 XID+branchId 找到 undo_log
    直接删除 undo_log
    本地无任何数据变化
7. TC 清理全局锁 & 事务记录
    释放该 XID 所有全局锁
    标记全局事务为 Committed
    清理事务状态
    
    
场景一最终状态
    订单、库存、账户数据均生效
    undo_log 全部删除
    全局锁全部释放
    数据一致
```

```text
场景二：正常回滚（扣库存抛异常）



1～3 步同上
    TM 开启全局事务
    RM1 插入订单（本地已提交，undo_log 已写，全局锁持有）
    调用 RM2 扣库存
4. 扣库存抛出异常
    异常向上抛到 TM 的 @GlobalTransactional 切面。
5. TM 触发全局回滚
    TM → TC：globalRollback(XID)
6. TC 同步二阶段回滚
    TC 向 所有已注册的分支（RM1、RM2） 发送：branchRollback(XID, branchId)
7. RM 执行回滚（核心）
    RM2（库存）：
        读取 undo_log
        校验当前数据 == afterImage（无脏写）
        使用 beforeImage 生成回滚 SQL： update stock set stock = ? where id = ?
        执行回滚，库存恢复
        删除 undo_log
    RM1（订单）：
        读取 undo_log
        生成回滚 SQL： delete from order where id = ?
        执行删除
        删除 undo_log
8. TC 释放全局锁 & 标记回滚完成
    所有锁释放
    全局事务状态：Rollbacked


场景二最终状态
    订单被删除
    库存恢复
    账户未执行，无变化
    undo_log 全部删除
    全局锁释放
    数据完全一致
```

```text
场景三：TM ↔ TC 网络超时 / 断连（最危险）



1～4 步正常执行
    订单已插入
    库存已扣减
    账户已扣减
    全部本地提交
    undo_log 已写
    全局锁全部持有
5. TM 准备通知 TC 提交 / 回滚
    此时 TM ↔ TC 网络断开 / 超时
    结果：
        TM 抛异常，线程结束
        TM 不记录任何事务状态，直接 “忘掉” XID
        TC 仍持有全局锁，事务状态为 Begin
6. TC 超时检查（核心机制）
    TC 后台定时任务扫描：
        超过 global-transaction-timeout（默认60s） 仍未收到 commit/rollback
7. TC 强制裁决：回滚
    TC 主动向 RM1/RM2/RM3 发送 branchRollback
8. RM 执行回滚
    恢复数据
    删除 undo_log
9. TC 释放全局锁
    避免锁长期占用导致系统阻塞。


场景三最终状态
所有数据恢复
undo_log 删除
锁释放
数据一致

设计哲学：不确定 = 安全回滚
```

```text
场景四：TC ↔ RM 网络超时 / 断连



前提
一阶段已全部完成，全局事务进入二阶段（提交 or 回滚）。

情况 A：全局提交（branchCommit）
    TC 向 RM 发送 branchCommit
    网络不通 / 超时
    TC 重试 3 次
    仍失败 → 标记为 CommitRetrying
    后台定时任务继续重试
    RM 最终恢复连接后：
        接收 branchCommit
        删除 undo_log
        业务数据不受影响（早已提交）

情况 B：全局回滚（branchRollback）
    TC 发送 branchRollback
    网络不通
    重试、标记、定时重试
    RM 恢复后执行：
        数据恢复
        删除 undo_log

最终保证
提交场景：最终删日志
回滚场景：最终必恢复
不会出现 “半提交半回滚”
```

```text
🔥场景五：RM 获取全局锁冲突 → 进入等待 / 重试



🎯前置场景
两个并发全局事务，同时修改同一行库存：
    事务 A（XID-A）：用户 1 下单，扣 product_id=1
    事务 B（XID-B）：用户 2 下单，也扣 product_id=1
库存同一行 → 同一个 lockKey → 全局锁冲突。


0. 全局锁结构
lockKey = 数据源ID + 表名 + 主键值例如：jdbc:mysql://localhost:3306/stock_db:stock:1
同一 lockKey 同一时间只能被一个 XID 持有。



这是高并发下最真实、最常见的流程。
阶段 1：事务 A 正常执行，先持有全局锁
    事务 A 进入扣库存 SQL
    RM 生成 beforeImage
    RM → TC：branchRegister，申请 lockKey=stock:1
    TC 内存无锁 → 加锁成功，归属 XID-A
    执行 update stock，获取 DB 行锁
    写入 undo_log
    本地事务提交
        → DB 行锁释放
        → 全局锁仍被 XID-A 持有
    此时：lockKey=stock:1 被 A 持有，未释放


阶段 2：事务 B 到来，全局锁冲突 → 开始等待
    事务 B 执行同一条 update stock
    RM 生成 beforeImage
    RM → TC：branchRegister，申请 lockKey=stock:1
    TC 判断：
        该 lockKey 已被 XID-A 持有 → 锁冲突
    RM 不直接抛异常，而是进入 重试等待机制：(默认配置 每次重试间隔 10ms,最多重试 30 次)

    在这段等待时间里：
        RM 本地事务还处于开启状态
        还没有执行业务 SQL
        还没有获取 DB 行锁
        还没有生成 undo_log
        还没有向数据库写入任何东西
        只在 RM 内存里循环重试

        
阶段 3：两种结局
结局 ①：在重试次数内，事务 A 释放全局锁
    事务 A 全局提交 / 回滚完成
    TC 释放 lockKey=stock:1
    事务 B 下一次重试：
        TC 加锁成功 → 返回 branchId
    事务 B 继续执行：
        执行 update stock
        生成 afterImage
        插入 undo_log
        本地提交
最终两个事务都正常完成，串行执行，无超卖。  

结局 ②：30 次重试都失败 → 抛出异常
    30 次、总共 300ms 后仍拿不到锁
    RM 抛出：io.seata.rm.datasource.exec.LockConflictException: get global lock fail
    RM 回滚本地事务（什么都没做，直接回滚）
    异常抛到 TM
    TM 触发 全局回滚
    TC 释放事务 B 已获取的其他全局锁（如订单锁）
    
    
💥💥💥💥事务 B 再每次重试时都会重新生成 beforeImage吗? 
不会！完全不会！事务 B 在全局锁重试等待期间，不会重新生成 beforeImage。
（生成 beforeImage）只在最开始执行一次！后面几十次重试全局锁，都不会再查库、不会再生成镜像。
```

```text
beforeImage一定要在全局锁之前完成
beforeImage用来防止【脏回滚】,beforeImage 必须在【申请全局锁之前】生成，不能放在后面。主要是为了防止行数据可能被其他非 Seata 事务修改.💥💥💥💥

如果你：
①先拿全局锁
②再去数据库查 beforeImage
那么在 ① 和 ② 之间，数据库这行数据仍然可能被其他非 Seata 事务修改.


seata将beforeImage和全局锁作为seata内部的一个原子操作来对待,通过先beforeImage再获取全局锁的顺序来实现逻辑原子性操作,顺序颠倒的话,seata无法阻止其他非Seata事务操作这样无法实现逻辑原子性操作.
```