package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import groovy.ui.SystemOutputInterceptor;
import models.Team;
import models.User;
import models.UserStory;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.With;

import java.util.LinkedList;
import java.util.List;

@With(Security.class)
public class Application extends Controller {

	public static void getTeamList() {
		Gson gson = new Gson();

        List<Team> teams = Team.find("order by name asc").fetch();
        List<String> data = new LinkedList<String>();

		for(Team t: teams) {
              data.add(t.name);
        }

		renderJSON(gson.toJson(data));
	}

    public static void getUserStories() {
        Gson gson = new Gson();

        List<UserStory> teams = UserStory.find("order by userStoryID asc").fetch();

        JsonArray data = new JsonArray();

        for(UserStory t: teams) {
            JsonObject team = new JsonObject();

            team.addProperty("id", t.userStoryID);
            team.addProperty("desc", t.userStoryDesc);

            data.add(team);
        }

        renderJSON(gson.toJson(data));
    }

	public static void index() {

		User u = Security.connectedUser();
		JsonObject me = null;
		String email = null;
		boolean isLogged = false;

		if (u != null && u.access_token != null) {
			me = WS.url(
					"https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s",
					WS.encode(u.access_token)).get().getJson()
					.getAsJsonObject();
			System.out.println(me);
			email = me.get("email").getAsString();
			isLogged = true;
		}

		render(email, isLogged);
	}
}