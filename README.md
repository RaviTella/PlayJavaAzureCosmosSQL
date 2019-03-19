![](logo.png)

# Overview
A Sample application built with Play framework, integrated with CosmosDB SQL API and a Rest service to demonstrate an end to end non-blocking application. The code base consists of two  projects a REST API called PlayJavaRecommendationService and a Web App which depends on the REST API called PlayJavaReadingListWebApp.

Specifically, the following capabilities are demonstrated:
* Play 2.6 - Routing, Twirl templates, forms, Dependency Injection etc
* Akka HTTP server backend
* Calling REST APIs with Play WS
* Azure CosmosDB SQL API
* CosmosDB Async Java SDK
* RXJava Observable to CompletableFuture

# Getting started

## First:
 * Java 8
 * sbt
 * Create a Cosmos DB collection for SQL API

## Then:
* Update the following properties in application.cong for PlayJavaReadingListWebApp with your Cosmos DB connection information
  - endPoint
  - key
  - dataBase
  - collection
* sbt "run 9001" - from PlayJavaRecommendationService the to start the REST API 
* sbt run - from PlayJavaReadingListWebApp to start the Web Application
* Access the WebApp at http://localhost:9000/

# Highlights
CosmosDB Async Java SDK natively supports RxJava Observable but Java Play framework controller actions need CompletableFuture.The following function will convert a Observable to CompletableFuture
```
  private <T> CompletableFuture<T> toCompletableFuture(Observable<T> observable) {
    CompletableFuture<T> cf = new CompletableFuture();
    observable.single().subscribe(l -> cf.complete(l), l -> cf.completeExceptionally(l));
    return cf;
  }
```

In Play frameworks, action code must be non-blocking. So, the action must return a promise of the result immediately upon invocation. In java that would be an object of type CompletionStage.  The following action function demonstrates combining two independent futures using thenCombine() and returning a CompletionStage<Result> which will eventually be redeemed by the view.
```
  public CompletionStage<Result> index() {
    CompletionStage<WSResponse> request = ws.url(config.getString("externalRestServices.recommendationService")).get();
    CompletableFuture<List<Document>> booksCF = dao.getReadersReadingListCompletableFuture("Tella");
    return request.thenCombineAsync(
        booksCF,
        (recommendations, books) -> {
          return ok(
              views.html.readingList.render(
                  convertToRecommendations(recommendations.asJson().toString()),
                  convertToBooks(books.toString())));
        },
        httpExecutionContext.current());
  }
```





