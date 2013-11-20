package controllers;

import play.mvc.Controller;
import play.mvc.With;

@With(Security.class)
public class Application extends Controller {

	public static void index() {

		if (Security.isLogged()) {
			String email = Security.getUserInfo();
			boolean isLogged = true;		
			render( email, isLogged);
		}
		render();
		
	}
}