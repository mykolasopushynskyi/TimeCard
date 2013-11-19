package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import groovy.ui.SystemOutputInterceptor;
import models.Team;
import models.User;
import models.Task;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.With;

import java.util.LinkedList;
import java.util.List;

@With(Security.class)
public class Application extends Controller {

	public static void index() {

		if (Security.isLogged()) {
			String email = Security.getUserInfo().get("email").getAsString();
			boolean isLogged = true;		
			render( email, isLogged);
		}
		render();
		
	}
}