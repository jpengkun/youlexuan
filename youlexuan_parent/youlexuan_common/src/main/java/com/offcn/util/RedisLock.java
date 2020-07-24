package com.offcn.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

public class RedisLock {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 加锁
     *
     * @param key   键
     * @param value 当前时间 + 超时时间
     * @return 是否拿到锁
     */

    public boolean lock(String key, String value) {

        // opsForValue：获取值的操作接口
        // setIfAbsent：如果键不存在则新增，存在则不改变已有的值
        if (redisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }
        String currentValue = redisTemplate.opsForValue().get(key);
        //如果锁过期
        if (!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()) {
            // getAndSet：获取原来key键对应的值，并重新赋新值
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            //是否已被别人抢占：比对 currentValue 和 oldValue 是否一致确保未被其他人抢占
            return !StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue);
        }
        return false;
    }

    /**
     * 解锁
     *
     * @param key   键
     * @param value 当前时间 + 超时时间
     */
    public void unlock(String key, String value) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(">>>>>redis解锁异常");
        }
    }

}

