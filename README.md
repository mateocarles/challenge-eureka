# challenge-eureka
Eureka Stock Market Challenge

SpringBoot Aplication

API endpoints:

POST /signUp

Expected body request 
{
	"name": "Nombre",
	"surname": "Apellido",
	"email": "mail@gmail.com"
}

GET /getStockMarketInfo
required params: stockSymbol
required header: Authorization -> apiKey

