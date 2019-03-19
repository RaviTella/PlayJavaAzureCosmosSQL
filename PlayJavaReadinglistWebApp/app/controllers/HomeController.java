package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cosmosdb.Document;
import model.Book;
import model.CosmosDBDAO;
import model.Recommendation;
import play.data.Form;
import play.data.FormFactory;

import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.libs.Scala.asScala;
import com.typesafe.config.Config;

@Singleton
public class HomeController extends Controller {
  private HttpExecutionContext httpExecutionContext;
  private final WSClient ws;
  private final CosmosDBDAO dao;
  private final Form<RecommendationFormData> recommendationForm;
  private final Form<BookFormData> bookForm;
  private final Config config;

  @Inject
  public HomeController(Config config,HttpExecutionContext ec, WSClient ws, CosmosDBDAO dao, FormFactory formFactory) {
    this.config=config;
    this.httpExecutionContext = ec;
    this.ws = ws;
    this.dao = dao;
    this.recommendationForm = formFactory.form(RecommendationFormData.class);
    this.bookForm = formFactory.form(BookFormData.class);
  }

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

  public CompletableFuture<Result> delete(String id) {
    return dao.deleteBookCompletableFuture(id)
        .thenApplyAsync(
            doc -> {
              return redirect(routes.HomeController.index());
            },
            httpExecutionContext.current());
  }

  public CompletableFuture<Result> get(String id) {
    return dao.getReadingListBookByIdCompletableFuture(id)
        .thenApplyAsync(
            doc -> {
              return ok(
                  views.html.editReadingList.render(
                          Json.fromJson(Json.parse(doc.get(0).toString()),Book.class)));

            } ,
            httpExecutionContext.current());
  }

  public CompletableFuture<Result> add() {
    final RecommendationFormData recommendation = recommendationForm.bindFromRequest().get();
    Book book = new Book();
    book.reader = "Tella";
    book.isbn = recommendation.getIsbn();
    book.title = recommendation.getTitle();
    book.author = recommendation.getAuthor();
    book.description = recommendation.getDescription();
    return dao.createBookCompletableFuture(book)
        .thenApplyAsync(
            doc -> {
              return redirect(routes.HomeController.index());
            },
            httpExecutionContext.current());
  }

  public CompletableFuture<Result> update() {
    final BookFormData bookFromForm = bookForm.bindFromRequest().get();
    Book book  = new Book(bookFromForm.getId(),bookFromForm.getReader(),bookFromForm.getIsbn(),bookFromForm.getTitle(),bookFromForm.getAuthor(),bookFromForm.getDescription());
    return dao.updateBookCompletableFuture(book).thenApplyAsync(
            doc -> {
              return redirect(routes.HomeController.index());
            },
            httpExecutionContext.current());
  }

  public Result cancel(){
    return redirect(routes.HomeController.index());
  }

  public List<Recommendation> convertToRecommendations(String jsonString) {
    ObjectMapper objectMapper = new ObjectMapper();
    List<Recommendation> recommendations = null;
    try {
      recommendations =
          objectMapper.readValue(
              jsonString,
              objectMapper
                  .getTypeFactory()
                  .constructCollectionType(List.class, Recommendation.class));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return recommendations;
  }

  public List<Book> convertToBooks(String jsonString) {
    ObjectMapper objectMapper = new ObjectMapper();
    List<Book> books = null;
    try {
      books =
          objectMapper.readValue(
              jsonString,
              objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));

    } catch (IOException e) {
      e.printStackTrace();
    }
    return books;
  }
}
