package controllers;

import java.util.Date;

import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import services.GoogleDocService;

@With(Security.class)
public class Application extends Controller {

	public static void index() {

		if (Security.isLogged()) {
			String email = Security.getUserInfo();
			boolean isLogged = true;
			
			String support = (String) Play.configuration.getProperty("multimedia.support","false");
			boolean multimediaSupport = Boolean.parseBoolean(support);
			String date = GoogleDocService.getDate();
			render( email, isLogged, multimediaSupport, date);
		}
		render();
	}
}