package com.concurrency.service;

import com.concurrency.domain.Stock;
import com.concurrency.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LettuceLockServiceTest {

    @Autowired
    private LettuceLockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void setup() {
        Stock stock = Stock.create(1L, 100L);
        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void teardown() {
        stockRepository.deleteAll();
    }

    @Test
    public void LettuceLockTest() throws InterruptedException {

        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);


        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                        try {
                            stockService.orderProduct(1L, 1L);
                        }catch (IllegalArgumentException e) {
                            // 재고 부족 예외 처리
                            System.out.println(e.getMessage());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } finally {
                            latch.countDown();
                        }
                    }
            );
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        //0
        assertThat(stock.getQuantity()).isEqualTo(0L);

    }

}