package com.concurrency.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Long quantity;

    @Version
    private Long version;

    private Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public static Stock create(Long productId, Long quantity) {
        return new Stock(productId, quantity);
    }


    public void decrease(Long quantity){
        if (this.quantity < quantity){
            throw new IllegalArgumentException("재고 부족");
        }
        this.quantity -= quantity;
    }


}
