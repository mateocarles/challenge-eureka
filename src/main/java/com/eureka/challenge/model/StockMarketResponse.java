package com.eureka.challenge.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockMarketResponse {

    String stockSymbol;
    Double openPrice;
    Double higherPrice;
    Double lowerPrice;
    Double variation;

    public StockMarketResponse(String stockSymbol, Double openPrice, Double higherPrice, Double lowerPrice, Double variation) {
        this.stockSymbol = stockSymbol;
        this.openPrice = openPrice;
        this.higherPrice = higherPrice;
        this.lowerPrice = lowerPrice;
        this.variation = variation;
    }
}
