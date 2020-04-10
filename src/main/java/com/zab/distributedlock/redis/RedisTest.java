package com.zab.distributedlock.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/redis")
public class RedisTest {
    @Autowired
    private RedisDistributedLock lock;

    @Autowired
    @Qualifier("limitFlowRedisScript")
    private DefaultRedisScript<Boolean> limitFlowRedisScript;

    @Autowired
    private RedisUtil redisUtil;

    private int j = 0;

    /**
     * 分布式锁测试
     *
     * @author zab
     * @date 2020/4/10 13:44
     */
    @RequestMapping("/test")
    public void test() throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();
                    lock.lock();
                    System.out.println(Thread.currentThread().getName() + "正在执行！");
                    System.out.println("j变为了：" + j++);
                    Thread.sleep(1000);
                    lock.releaseLock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Thread---" + i).start();
            countDownLatch.countDown();
        }

    }

    /**
     * lua限流测试
     */
    @RequestMapping("/test1")
    @ResponseBody
    public String test1() throws Exception {
        String key = "limitKey";
        String limit = "5";
        boolean b = redisUtil.exeLuaScript(limitFlowRedisScript, Collections.singletonList(key), Collections.singletonList(limit));
        if(b){
            return "频率不够，加大点击次数";
        }
        return "频率过高，请慢点点击";
    }


}
