package com.concurrency.service;

import com.concurrency.repository.RedisLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LettuceLockService {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public void orderProduct(Long productId, Long quantity) throws InterruptedException {
        // Lock 획득 시도
        while (!redisLockRepository.lock(productId)) {
            //SpinLock 방식이 redis 에게 주는 부하를 줄여주기위한 sleep
            Thread.sleep(100);
        }


        try {
            // 재고 감소 로직 실행
            System.out.println("재고 처리 진행...");
            stockService.orderProduct(productId, quantity);
        } finally {
            // 락 해제
            redisLockRepository.unlock(productId);
        }
    }
}
