package com.eureka.challenge.service;

import com.eureka.challenge.api.ResponseHandler;
import com.eureka.challenge.model.Client;
import com.eureka.challenge.model.StockMarketResponse;
import com.eureka.challenge.repository.ClientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.regex.Pattern;

@Service
@PropertySource("classpath:challenge.properties")
public class ClientService {

    ClientRepository clientRepository;
    private Logger logger = LoggerFactory.getLogger((ClientService.class));

    private static final DecimalFormat df = new DecimalFormat("0.00");


    @Autowired
    public ClientService(ClientRepository cr) {
        this.clientRepository = cr;
    }

    @Value("${URL_API}")
    private String URL_API;

    @Value("${API_KEY_AUTH}")
    private String API_KEY_AUTH;

    @Value("${REGEX_EMAIL_VALIDATION}")
    private String REGEX_EMAIL_VALIDATION;


    public ResponseEntity<Object> signUp(Client client, RateLimiter limiter) {
        limiter.acquire();
        try{
            if(isRegistered(client)){
                logger.error("The client is already registered: {}", client.getEmail());
                return ResponseHandler.generateResponse("Client already registered", HttpStatus.CONFLICT, client);
            }
            if(client.getEmail().isEmpty() || client.getName().isEmpty() || validateEmail(client.getEmail())){
                logger.error("Bad request: {}", client.getEmail());
                return ResponseHandler.generateResponse("Invalid request data", HttpStatus.BAD_REQUEST, client);
            }
            generateAndSetApiKey(client);
            return ResponseHandler.generateResponse("New client created", HttpStatus.CREATED, clientRepository.save(client));
        } catch (Exception e) {
            logger.error("An error has occurred while creating and saving client: {}", client.getName());
            throw new RuntimeException("An error has occurred while creating and saving client", e);
        }
    }
    public ResponseEntity<Object> getStockMarketInfo(String apiKey, String stockSymbol, RateLimiter limiter) {
        limiter.acquire();
        // Check if apiKey received belongs to a registered user
        if(clientRepository.findByApiKey(apiKey).isEmpty()) {
            logger.error("Unauthorized access: {}", apiKey);
            return ResponseHandler.generateResponse("Unauthorized access", HttpStatus.BAD_REQUEST, apiKey);
        } else {
            try{

                return stockMarketCall(stockSymbol);

            } catch (Exception e) {
                logger.error("An error has occurred while retrieving stock market info: {}", stockSymbol);
                throw new RuntimeException("An error has occurred while retrieving stock market info", e);
            }
        }
    }

    private  ResponseEntity<Object> stockMarketCall(String stockSymbol) throws JsonProcessingException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(URL_API)
                // Add query parameter
                .queryParam("symbol", stockSymbol)
                .queryParam("apikey", API_KEY_AUTH);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, null,  String.class);
        String jsonResult = response.getBody();

        StockMarketResponse smr = populateStockMarketResponse(jsonResult,stockSymbol);

        return ResponseHandler.generateResponse("Stock market results",response.getStatusCode(),smr);
    }

    private StockMarketResponse populateStockMarketResponse(String jsonResult, String stockSymbol) throws JsonProcessingException {

        Double variation;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResult);

        String lastRefreshedDate =  jsonNode.get("Meta Data").get("3. Last Refreshed").asText();
        LocalDate dateTime = LocalDate.parse(lastRefreshedDate);
        String dayBeforeLastRefreshedDate = dateTime.minusDays(1).toString();
        double openPrice = jsonNode.get("Time Series (Daily)").get(lastRefreshedDate).get("1. open").asDouble();
        double higherPrice = jsonNode.get("Time Series (Daily)").get(lastRefreshedDate).get("2. high").asDouble();
        double lowerPrice = jsonNode.get("Time Series (Daily)").get(lastRefreshedDate).get("3. low").asDouble();
        double closePriceLastDay = jsonNode.get("Time Series (Daily)").get(lastRefreshedDate).get("4. close").asDouble();
        double closePricePreviousLastDay = jsonNode.get("Time Series (Daily)").get(dayBeforeLastRefreshedDate).get("4. close").asDouble();

        variation = Math.abs((((closePriceLastDay-closePricePreviousLastDay)/closePricePreviousLastDay)*100));

        return new StockMarketResponse(stockSymbol, openPrice, higherPrice, lowerPrice, Double.parseDouble(df.format(variation)));
    }

    private boolean isRegistered(Client client) {
        return !clientRepository.findByEmail(client.getEmail()).isEmpty();
    }

    private void generateAndSetApiKey(Client client) {
        long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        client.setApiKey("APIKEY" + number);
    }

    private boolean validateEmail(String email) {

        Pattern pattern = Pattern.compile(REGEX_EMAIL_VALIDATION);
        return !(pattern.matcher(email)).matches();

    }
}
