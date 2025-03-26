package com.concurrency.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisLockRepository {

    private final RedisTemplate<String, String> redisTemplate;


    public Boolean lock(Long key) {

       return redisTemplate
               .opsForValue()
                 /*
                 setIfAbsent(key, value, expiration)
                 generateKey(key): 락을 설정할 Redis 키 (예: "lock:123").
                 "lock": 락을 나타내는 값 (고정 문자열 "lock" 사용).
                 Duration.ofMillis(3_000): 3초 동안 락 유지.

                 ⚡ 이 코드가 실행되면 Redis에서 다음과 같은 SETNX 명령어가 실행됨
                 SET lock:123 "lock" NX PX 3000
                 NX: 키가 존재하지 않을 때만 설정 (존재하면 무시됨).
                 PX 3000: 키의 만료 시간(3초) 설정.
                 */
               .setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3000));
    }

    public Boolean unlock(final Long key) {
        return redisTemplate.delete(generateKey(key));
    }

    private String generateKey(final Long key) {
        return key.toString();
    }
}
