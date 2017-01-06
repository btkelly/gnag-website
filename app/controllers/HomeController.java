package controllers;

import play.mvc.Controller;
import play.mvc.Result;
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

        //TODO move client ID and client secrect to configuration / environment variables
        //Option<String> clientId = Play.current().configuration().getString("play.gh.id", null);

        return ok(index.render("Your new application is ready.", "8b6feba195daa45b3f6c"));
    }

}