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
import views.html.projects;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Created by bobbake4 on 8/17/16.
 */
public class GitHubAuthController extends Controller {

    private final WSClient wsClient;

    @Inject
    public GitHubAuthController(WSClient wsClient) {
        this.wsClient = wsClient;
    }

    /**
     * Function called by the GitHub authentication flow which takes the resulting code as a parameter.
     * This function will take the code passed and exchange it for a client access token used to make
     * GitHub api requests.
     * @param code
     * @return
     */
    public CompletionStage<Result> callback(String code) {

        final Http.Context context = Http.Context.current();

        //TODO move client ID and client secrect to configuration / environment variables
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

    /**
     * Attempts to load the currently authenticated users project list using the current sessions access_token
     * which is set in the GitHubAuthController.callback function.
     * @return
     */
    public CompletionStage<Result> loadProjects() {

        return wsClient.url("https://api.github.com/user/repos")
                .setQueryParameter("access_token", session("token"))
                .setHeader("accept", "application/json")
                .setRequestTimeout(10 * 1000)
                .get()
                .thenApply(response -> {
                    Type listType = new TypeToken<ArrayList<Project>>() {
                    }.getType();

                    Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

                    List<Project> projectList = gson.fromJson(response.getBody(), listType);

                    return ok(projects.render(projectList));
                });
    }

    /**
     * Used to render the Gradle configuration for a specific repository slug and access_token combination.
     * @param slug
     * @return
     */
    public Result configForSlug(String slug) {
        return ok(gnagconfig.render(slug, session("token")));
    }
}
