package controllers;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import models.Project;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.callback;
import views.html.gnagconfig;
import views.html.index;
import views.html.projects;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final WSClient wsClient;

    @Inject
    public HomeController(WSClient wsClient) {
        this.wsClient = wsClient;
    }

    public Result index() {

        //Option<String> clientId = Play.current().configuration().getString("play.gh.id", null);

        return ok(index.render("Your new application is ready.", "8b6feba195daa45b3f6c"));
    }

    public CompletionStage<Result> callback(String code) {

        final Http.Context context = Http.Context.current();

        return wsClient.url("https://github.com/login/oauth/access_token")
                .setQueryParameter("client_id", "8b6feba195daa45b3f6c")
                .setQueryParameter("client_secret", "2856c15506bfae0592e7cc88761af653746196da")
                .setQueryParameter("code", code)
                .setHeader("accept", "application/json")
                .setRequestTimeout(10 * 1000)
                .post("")
                .thenApply(response -> {

                    String accessToken = response.asJson().get("access_token").asText();

                    context.session().put("token", accessToken);

                    return ok(callback.render(code, accessToken));
                });
    }


    public CompletionStage<Result> loadProjects() {

        return wsClient.url("https://api.github.com/user/repos")
                .setQueryParameter("access_token", session("token"))
                .setHeader("accept", "application/json")
                .setRequestTimeout(10 * 1000)
                .get()
                .thenApply(response -> {
                    Type listType = new TypeToken<ArrayList<Project>>() {}.getType();

                    Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

                    List<Project> projectList = gson.fromJson(response.getBody(), listType);

                    return ok(projects.render(projectList));
                });
    }

    public Result configForSlug(String slug) {
        return ok(gnagconfig.render(slug, session("token")));
    }

}