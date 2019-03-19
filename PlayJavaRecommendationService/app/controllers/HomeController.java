package controllers;

import play.libs.Json;
import play.mvc.*;


public class HomeController extends Controller {


    public Result getAll() {
        return ok(Json.toJson(new RecommendationRepository().getRecommendations()));
    }

}
