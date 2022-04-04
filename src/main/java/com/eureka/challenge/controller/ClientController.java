package com.eureka.challenge.controller;


import com.eureka.challenge.model.Client;
import com.eureka.challenge.service.ClientService;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClientController {

    private ClientService clientService;

    @Autowired
    public ClientController(ClientService cs) { this.clientService = cs;}


    @PostMapping("/signUp")
    public ResponseEntity<Object> signUp(@Validated @RequestBody Client client){
        RateLimiter limiter = RateLimiter.create(1.0);
        return clientService.signUp(client,limiter);
    }

    @GetMapping("/getStockMarketInfo")
    public ResponseEntity<Object> getStockMarketInfo(@Validated @RequestHeader(value="Authorization") String apiKey, @RequestParam("stockSymbol") String stockSymbol){
        RateLimiter limiter = RateLimiter.create(1.0);
        return clientService.getStockMarketInfo(apiKey, stockSymbol,limiter);
    }

}
