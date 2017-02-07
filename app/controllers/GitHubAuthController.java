package controllers;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import models.PageLinks;
import models.Project;
import play.Configuration;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.gnagconfig;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

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

    /**
     * Attempts to load the currently authenticated users project list using the current sessions access_token
     * which is set in the GitHubAuthController.callback function.
     * @return
     */
    public CompletionStage<Result> loadProjects() {

        final Http.Context context = Http.Context.current();

        Type listType = new TypeToken<ArrayList<Project>>() {}.getType();

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        ArrayList<Project> projectList = new ArrayList<>();

        return getProjectPageRequest(1, context)
                .thenCompose(new Function<WSResponse, CompletionStage<Void>>() {
                    @Override
                    public CompletionStage<Void> apply(WSResponse wsResponse) {

                        addToProjectList(wsResponse);

                        PageLinks pageLinks = new PageLinks(wsResponse);

                        if (pageLinks.getLast() != null && pageLinks.getNext() != null) {

                            int nextPage = pageLinks.getNext().getPageNum();
                            int lastPage = pageLinks.getLast().getPageNum();

                            CompletableFuture[] pageRequests = new CompletableFuture[(lastPage - nextPage) + 1];

                            for (int index = 0; index <= lastPage - nextPage; index++) {
                                pageRequests[index] = getProjectPageRequest(index + nextPage, context)
                                        .thenAccept(this::addToProjectList)
                                        .toCompletableFuture();
                            }

                            return CompletableFuture.allOf(pageRequests);

                        } else {
                            return CompletableFuture.completedFuture(null);
                        }
                    }

                    private void addToProjectList(WSResponse wsResponse) {
                        projectList.addAll(gson.fromJson(wsResponse.getBody(), listType));
                    }
                })
                .thenApply(aVoid -> ok(Json.toJson(projectList)));
    }

    private CompletionStage<WSResponse> getProjectPageRequest(int page, Http.Context context) {

        return wsClient.url("https://api.github.com/user/repos")
                .setQueryParameter("access_token", context.session().get(TOKEN_KEY))
                .setQueryParameter("per_page", "100")
                .setQueryParameter("page", String.valueOf(page))
                .setHeader("accept", "application/json")
                .setRequestTimeout(10 * 1000)
                .get();
    }
}
