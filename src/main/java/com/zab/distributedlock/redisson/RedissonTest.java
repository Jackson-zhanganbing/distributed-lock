package com.zab.distributedlock.redisson;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/redisson")
public class RedissonTest {

    private int j = 0;

    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("/test")
    public void cameraCallback() throws Exception {

        RLock rlock = redissonClient.getLock("redisson:lock:personId" + 123);

        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    rlock.lock(20, TimeUnit.SECONDS);
                    System.out.println(Thread.currentThread().getName() + "正在执行！");
                    for (int k = 0; k < 1000; k++) {
                        j++;
                    }
                    countDownLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    rlock.unlock();
                }
            }, "Thread---" + i).start();

        }

        countDownLatch.await();
        System.out.println("j=" + j);

    }

}
