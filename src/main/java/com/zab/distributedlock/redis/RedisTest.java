package com.zab.distributedlock.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/redis")
public class RedisTest {
    @Autowired
    private RedisDistributedLock lock;

    private int j = 0;

    @RequestMapping("/test")
    public void test() throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();
                    lock.lock();
                    System.out.println(Thread.currentThread().getName()+"正在执行！");
                    System.out.println("j变为了："+ j++);
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
