package com.concurrency.service;


import com.concurrency.domain.Stock;
import com.concurrency.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptimisticLockStockService {

    private final StockRepository stockRepository;

    @Transactional
    public void orderProduct(Long id, Long quantity) {
        Stock stock = stockRepository.findByWithOptimisticLock(id);
        System.out.println("Before Update: version=" + stock.getVersion());
        stock.decrease(quantity);
        System.out.println("After Update: version=" + stock.getVersion());
        stockRepository.saveAndFlush(stock);
    }
}