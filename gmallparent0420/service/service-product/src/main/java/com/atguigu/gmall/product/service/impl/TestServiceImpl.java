package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mqx
 * @date 2020-10-10 15:37:33
 */
@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public void testLock() {
        // 使用Redisson 做分布式锁
        RLock lock = redisson.getLock("lock");
        // 上锁
        // lock.lock();
        // lock.lock(10,TimeUnit.SECONDS);
//        boolean res = false;
//        try {
//            res = lock.tryLock(100, 10, TimeUnit.SECONDS);
//            if (res){
//                try {
//                    // get(num)
//                    String numValue = redisTemplate.opsForValue().get("num");
//                    // 如果缓存中没有数据，则返回！
//                    if (StringUtils.isEmpty(numValue)){
//                        return;
//                    }
//                    // 将数据进行+1
//                    int num = Integer.parseInt(numValue);
//                    // 写入缓存 set num ++num
//                    redisTemplate.opsForValue().set("num",String.valueOf(++num));
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                } finally {
//                    // 解锁
//                    lock.unlock();
//                }
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        lock.lock();
        String numValue = redisTemplate.opsForValue().get("num");
        // 如果缓存中没有数据，则返回！
        if (StringUtils.isEmpty(numValue)){
            return;
        }
        // 将数据进行+1
        int num = Integer.parseInt(numValue);
        // 写入缓存 set num ++num
        redisTemplate.opsForValue().set("num",String.valueOf(++num));
        lock.unlock();
    }

    @Override
    public String readLock() {
        // 声明rwlock
        RReadWriteLock rwlock = redisson.getReadWriteLock("atguiguLock");
        // 读锁对象
        RLock rLock = rwlock.readLock();
        // 上锁
        rLock.lock(10,TimeUnit.SECONDS);
        // 从缓存中读取数据，并返回
        String msg = redisTemplate.opsForValue().get("msg");

        return msg;
    }

    @Override
    public String writeLock() {
        // 声明rwlock
        RReadWriteLock rwlock = redisson.getReadWriteLock("atguiguLock");

        // 获取写锁对象
        RLock rLock = rwlock.writeLock();
        // 上锁
        rLock.lock(10,TimeUnit.SECONDS);
        // 将一个随机的字符串，写入缓存
        String uuid = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("msg",uuid);

        return "写入数据完成。。。。。";
    }

    /*
    1.  需要在缓存中设置一个key=num，初始值 0
    2.  通过应用程序，查询缓存，
        2.1 如果缓存中有数据，则将数据进行+1.
        2.2 如果缓存中没有数据，则返回！
     */
//    @Override
//    public synchronized void testLock() {
//        // 直接获取缓存数据
//        // get(num)
//        String numValue = redisTemplate.opsForValue().get("num");
//        // 如果缓存中没有数据，则返回！
//        if (StringUtils.isEmpty(numValue)){
//            return;
//        }
//        // 将数据进行+1
//        int num = Integer.parseInt(numValue);
//        // 写入缓存 set num ++num
//        redisTemplate.opsForValue().set("num",String.valueOf(++num));
//
//    }

//    // 分布式锁案例：
//    @Override
//    public void testLock() {
//        // 使用redis setnx 命令 setnx(key,value)
//        // set k1 v1 px 3 nx
//        // 声明一个随机值
//        String uuid = UUID.randomUUID().toString();
//        // index1=lock uuid1  index2 lock uuid2  index3  lock uuid3
//        // uuid 其实对应的lua 脚本 叫 token
//        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3, TimeUnit.SECONDS);
//        // 判断获取锁是否成功
//        if(flag){
//            // 获取锁成功！
//            String numValue = redisTemplate.opsForValue().get("num");
//            // 如果缓存中没有数据，则返回！
//            if (StringUtils.isEmpty(numValue)){
//                return;
//            }
//            // 在此出现错误！ int i = 1/0;
//            // 将数据进行+1
//            int num = Integer.parseInt(numValue);
//            // 写入缓存 set num ++num
//            redisTemplate.opsForValue().set("num",String.valueOf(++num));
//            // 释放锁
////            if (uuid.equals(redisTemplate.opsForValue().get("lock"))){
////                // 如果key 对应的值相同，则释放锁！否则不释放！
////                // index1 if 判断完成！ 执行delete(); 那么锁的时间正好过期！ cpu 可能会让index2 得到执行权限！
////                redisTemplate.delete("lock");
////            }
//            /*
//            推荐使用lua 脚本删除保证原子性！
//            if redis.call("get",KEYS[1]) == ARGV[1]
//            then
//                return redis.call("del",KEYS[1])
//            else
//                return 0
//            end
//             */
//            // 定义一个字符串 记录是lua 脚本的命令。
//            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            // java 客户端如何调用，并执行！
//            // RedisScript
//            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script);
//            // 设置返回类型 , 默认是Object ，设置成Long 数据类型是因为：lua 脚本。
//            redisScript.setResultType(Long.class);
//            // redis 中，执行lur 脚本。
//            redisTemplate.execute(redisScript, Arrays.asList("lock"),uuid);
//
//        }else {
//            // 获取到锁失败！
//            try {
//                Thread.sleep(500);
//                // 自旋
//                testLock();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
