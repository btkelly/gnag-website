package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    public Result index() {

        //Option<String> clientId = Play.current().configuration().getString("play.gh.id", null);

        return ok(index.render("Your new application is ready.", "8b6feba195daa45b3f6c"));
    }

}