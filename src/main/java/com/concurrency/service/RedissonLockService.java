package com.concurrency.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedissonLockService {

    private final RedissonClient redissonClient;
    private final StockService stockService;

    public void orderProduct(Long productId, Long quantity) throws InterruptedException {
        // key 값으로 Lock 객체 가져오기
        RLock lock = redissonClient.getLock(productId.toString());

        try{
            // 5초 동안 락을 획득 시도, 락 획득 후 10초 동안 유지 10초 넘어가면 자동 락 해체
            boolean available = lock.tryLock(5, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("lock 획득 실패");
                return;
            }

            stockService.orderProduct(productId, quantity);
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            // lock.unlock()는 반드시 락을 획득한 스레드만 해체 가능
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
