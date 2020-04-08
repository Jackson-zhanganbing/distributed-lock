package com.zab.distributedlock.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * redis 实现分布式锁
 *
 * @author zab
 * @date 2020-02-09 09:40
 */
@Component
public class RedisDistributedLock {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DefaultRedisScript<Boolean> redisScript;

    public static final String LOCKKEY = "lockKey";
    public static final String LOCKVALUE = "lockValue";
    public static final long TIMEOUT = 20;
    private static final Boolean RELEASE_SUCCESS = true;

    public void lock(){
        while(true){
            if(tryLock()){
                System.out.println(Thread.currentThread().getName()+"->获得锁");
                return;
            }
        }
    }

    private boolean tryLock() {
        //尝试获得锁，就是尝试往redis的固定某个key写入数据
        if (redisUtil.setIfNotExist(LOCKKEY, LOCKVALUE+Thread.currentThread().getId(), TIMEOUT)) {
            return true;
        }
        return false;
    }

    public boolean releaseLock(){

        Object result = redisUtil.exeLuaScript(redisScript,
                Collections.singletonList(LOCKKEY),
                LOCKVALUE+Thread.currentThread().getId());
        System.out.println("releaseLock-lua脚本执行结果："+result);

        if(RELEASE_SUCCESS.equals(result)){
            return true;
        }
        return false;
    }

}
