package model;

import com.microsoft.azure.cosmosdb.*;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.typesafe.config.Config;
import rx.Observable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CosmosDBDAO {
  private final Config config;
  private final String databaseName;
  private final String collectionName;
  private final String endPoint;
  private final String key;

  @Inject
  public CosmosDBDAO(Config config) {
    this.config=config;
    this.endPoint=config.getString("cosmosConfig.endPoint");
    this.key=config.getString("cosmosConfig.key");
    this.databaseName=config.getString("cosmosConfig.dataBase");
    this.collectionName=config.getString("cosmosConfig.collection");

  }

  private AsyncDocumentClient getClient() {
    ConnectionPolicy policy = new ConnectionPolicy();
    policy.setConnectionMode(ConnectionMode.Direct);

    return new AsyncDocumentClient.Builder()
        .withServiceEndpoint(endPoint)
        .withMasterKeyOrResourceToken(
            key)
        .withConnectionPolicy(policy)
        .withConsistencyLevel(ConsistencyLevel.Eventual)
        .build();
  }

  public Observable<Document> getReadersReadingListObservable(String reader) {
    FeedOptions queryOptions = new FeedOptions();
    queryOptions.setMaxItemCount(10);
    queryOptions.setEnableCrossPartitionQuery(true);
    String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);
    String query = "SELECT * FROM ReadingList r WHERE r.reader = " + "'" + reader + "'";
    Observable<FeedResponse<Document>> queryObservable =
        getClient().queryDocuments(collectionLink, query, queryOptions);
    return queryObservable.flatMap(page -> Observable.from(page.getResults()));
  }

  private Observable<Document> deleteBookObservable(String id) {
    String documentLink =
        String.format("/dbs/%s/colls/%s/docs/%s", databaseName, collectionName, id);
    RequestOptions reqOpts = new RequestOptions();
    reqOpts.setPartitionKey(new PartitionKey("Tella"));
    Observable<ResourceResponse<Document>> deleteObservable =
        getClient().deleteDocument(documentLink, reqOpts);
    return deleteObservable
        .single()
        .flatMap(r -> Observable.just(r.getResource()));
  }

  private Observable<Document> updateBookObservable(Book book) {
    RequestOptions reqOpts = new RequestOptions();
    String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);
    Observable<ResourceResponse<Document>> deleteObservable =
        getClient().upsertDocument(collectionLink, book, reqOpts, false);
    return deleteObservable.single().flatMap(page -> Observable.just(page.getResource()));
  }

  private Observable<Document> createBookObservable(Book book) {
    String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);
    Observable<ResourceResponse<Document>> createObservable =
        getClient().createDocument(collectionLink, book, null, false);
    return createObservable.single().flatMap(page -> Observable.just(page.getResource()));
  }

  private Observable<Document> getReadingListBookByIdObservable(String id) {
    FeedOptions queryOptions = new FeedOptions();
    queryOptions.setMaxItemCount(10);
    queryOptions.setEnableCrossPartitionQuery(true);
    String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);
    String query =
        "SELECT r.id,r.reader,r.isbn,r.title,r.author,r.description FROM ReadingList r WHERE r.id = "
            + "'"
            + id
            + "'";
    Observable<FeedResponse<Document>> queryObservable =
        getClient().queryDocuments(collectionLink, query, queryOptions);
    return queryObservable.flatMap(page -> Observable.from(page.getResults()));
  }

  public <T> CompletableFuture<List<Document>> createBookCompletableFuture(Book book) {
    return toCompletableFuture(createBookObservable(book).toList());
  }

  public <T> CompletableFuture<List<Document>> deleteBookCompletableFuture(String id) {
    return toCompletableFuture(deleteBookObservable(id).toList());
  }

  public <T> CompletableFuture<List<Document>> getReadersReadingListCompletableFuture(
      String reader) {
    return toCompletableFuture(getReadersReadingListObservable(reader).toList());
  }

  public <T> CompletableFuture<List<Document>> updateBookCompletableFuture(Book book) {
    return toCompletableFuture(updateBookObservable(book).toList());
  }

  public <T> CompletableFuture<List<Document>> getReadingListBookByIdCompletableFuture(String id) {
    return toCompletableFuture(getReadingListBookByIdObservable(id).toList());
  }

  //Converts Observable into CompletetableFuture
  private <T> CompletableFuture<T> toCompletableFuture(Observable<T> observable) {
    CompletableFuture<T> cf = new CompletableFuture();
    observable.single().subscribe(l -> cf.complete(l), l -> cf.completeExceptionally(l));
    return cf;
  }
}
