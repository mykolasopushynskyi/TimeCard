package controllers;

import javax.inject.Inject;

import play.mvc.Controller;
import services.RallyService;
import validation.UserStoryBean;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RallyManager extends Controller {

	@Inject
	private static RallyService rally;

	public static void example(String id, String team) {
		UserStoryBean us = rally.getUserStoryInfo(id, team);
		
		JsonObject responce = new JsonObject();
		
		if (us != null) {
			response.status = 200;
			responce.addProperty("rallyId", us.getRallyId());
			responce.addProperty("name", us.getName());
		} else {
			response.status = 404;
			responce.addProperty("error", "User story not found!");
		}
		
		renderJSON(new Gson().toJson(responce));
	}
}