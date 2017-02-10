package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.confighelper;
import views.html.index;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * Renders the main home page and provides the Gnag client ID used to create the GitHub authentication
     * link.
     * @return
     */
    public Result index() {
        session().remove(GitHubAuthController.TOKEN_KEY);
        return ok(index.render());
    }

    /**
     * Will show the page for generating a Gradle config for a specific project. If the user has not authorized GitHub
     * this will redirect to start the authentication flow.
     * @return
     */
    public Result configHelper() {
        if (session(GitHubAuthController.TOKEN_KEY) == null) {
            return redirect("/startAuth");
        } else {
            return ok(confighelper.render(session(GitHubAuthController.TOKEN_KEY)));
        }
    }

}