package com.zab.distributedlock.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * lua脚本配置
 *
 * @author zab
 * @date 2020-02-09 19:20
 */
@Configuration
public class LuaConfiguration {

    @Bean("redisLockScript")
    public DefaultRedisScript<Boolean> redisLockScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/Test.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }
    @Bean("limitFlowRedisScript")
    public DefaultRedisScript<Boolean> limitFlowRedisScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/limitFlow.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }
}
