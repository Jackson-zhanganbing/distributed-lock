package com.zab.distributedlock.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis 工具类
 *
 * @author zab
 * @date 2020-02-09 09:48
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;
    private static final Boolean RELEASE_SUCCESS = true;

    public boolean set(String key, Object value, long time) {
        try {

            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
                return true;
            } else {
                this.set(key, value);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean setIfNotExist(String key, Object value, long timeout) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean exeLuaScript(DefaultRedisScript redisScript, List<String> keys, Object... value) {
        try {
            Object result = redisTemplate.execute(redisScript, keys, value);
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean delete(String... keys) {
        if (keys != null && keys.length > 0) {
            if (keys.length == 1) {
                return redisTemplate.delete(keys[0]);
            } else {
                long successNum = redisTemplate.delete(CollectionUtils.arrayToList(keys));
                if (successNum > 0) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
