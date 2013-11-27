package controllers;

import play.Play;
import play.mvc.Controller;
import play.mvc.With;

@With(Security.class)
public class Application extends Controller {

	public static void index() {

		if (Security.isLogged()) {
			String email = Security.getUserInfo();
			boolean isLogged = true;
			
			String support = (String) Play.configuration.getProperty("multimedia.support","false");
			boolean multimediaSupport = Boolean.parseBoolean(support);
			render( email, isLogged, multimediaSupport);
		}
		render();
		
	}
}