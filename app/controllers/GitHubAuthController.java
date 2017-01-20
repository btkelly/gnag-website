package controllers;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import models.Project;
import play.Configuration;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.gnagconfig;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by bobbake4 on 8/17/16.
 */
public class GitHubAuthController extends Controller {

    public static final String TOKEN_KEY = "token";
    public static final String VERSION_KEY = "latestVersion";

    private final WSClient wsClient;
    private final Configuration configuration;

    @Inject
    public GitHubAuthController(WSClient wsClient, Configuration configuration) {
        this.wsClient = wsClient;
        this.configuration = configuration;
    }

    /**
     * This call will redirect to the GitHub Oauth flow with the Gnag Plugin scopes and ID specified.
     * @return
     */
    public Result startAuth() {
        return redirect("https://github.com/login/oauth/authorize?scope=repo&client_id=" + configuration.getString("gh.id"));
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

        return wsClient.url("https://github.com/login/oauth/access_token")
                .setQueryParameter("client_id", configuration.getString("gh.id"))
                .setQueryParameter("client_secret", configuration.getString("gh.secret"))
                .setQueryParameter("code", code)
                .setHeader("accept", "application/json")
                .setRequestTimeout(10 * 1000)
                .post("")
                .thenApply(response -> {

                    String accessToken = response.asJson().get("access_token").asText();

                    context.session().put(TOKEN_KEY, accessToken);

                    return redirect("/configHelper");
                });
    }

    /**
     * Attempts to load the currently authenticated users project list using the current sessions access_token
     * which is set in the GitHubAuthController.callback function.
     * @return
     */
    public CompletionStage<Result> loadProjects() {

        return wsClient.url("https://api.github.com/user/repos")
                .setQueryParameter("access_token", session(TOKEN_KEY))
                .setHeader("accept", "application/json")
                .setRequestTimeout(10 * 1000)
                .get()
                .thenApply(response -> {
                    Gson gson = new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create();

                    Type listType = new TypeToken<ArrayList<Project>>() { }.getType();

                    List<Project> projectList = gson.fromJson(response.getBody(), listType);

                    return ok(Json.toJson(projectList));
                });
    }

    /**
     * Used to render the Gradle configuration for a specific repository slug, access_token and version combination.
     * Will fetch the latest plugin version if it has not already been cached.
     * @param slug
     * @return
     */
    public CompletionStage<Result> configForSlug(String slug) {

        if (session(VERSION_KEY) == null) {

            final Http.Context context = Http.Context.current();

            return wsClient.url("https://api.bintray.com/packages/btkelly/maven/gnag-gradle-plugin/versions/_latest")
                    .setHeader("accept", "application/json")
                    .setRequestTimeout(10 * 1000)
                    .get()
                    .thenApply(response -> {
                        String latestVersion = response.asJson().get("name").asText();
                        context.session().put(VERSION_KEY, latestVersion);
                        return ok(gnagconfig.render(slug, context.session().get(TOKEN_KEY), latestVersion));
                    });
        } else {
            return CompletableFuture.completedFuture(ok(gnagconfig.render(slug, session(TOKEN_KEY), session(VERSION_KEY))));
        }
    }
}
