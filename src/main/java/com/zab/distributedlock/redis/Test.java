package com.zab.distributedlock.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/test")
public class Test {
    @Autowired
    private RedisDistributedLock lock;

    @RequestMapping("/test")
    public void test() throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();
                    lock.lock();
                    Thread.sleep(1000);
                    lock.releaseLock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Thread---" + i).start();
            countDownLatch.countDown();
        }

    }

}
