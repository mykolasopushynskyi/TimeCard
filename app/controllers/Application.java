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

		User u = Security.connectedUser();
		JsonObject me = null;
		String email = null;
		boolean isLogged = false;
	
		try {
			if (u != null && u.access_token != null) {
				me = WS.url(
						"https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s",
						WS.encode(u.access_token)).get().getJson()
						.getAsJsonObject();
				//System.out.println(me);
				email = me.get("email").getAsString();
				isLogged = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		render(email, isLogged);
	}
}