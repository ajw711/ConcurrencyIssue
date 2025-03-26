package com.concurrency.service;

import com.concurrency.domain.Stock;
import com.concurrency.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PessimisticLockStockService {

    private final StockRepository stockRepository;

    @Transactional
    public void orderProduct(Long productId, Long quantity){
        Stock stock = stockRepository.findByWithId(productId);
        stock.decrease(quantity); //재고 차감
        stockRepository.saveAndFlush(stock);

    }

}
