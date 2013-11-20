package controllers;

import play.mvc.Controller;
import services.RallyService;
import services.UserStory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RallyManager extends Controller {

	private final static RallyService rally = new RallyService();

	public static void example(String id) {
		UserStory us = rally.getUserStoryInfo(id);
		
		JsonObject responce = new JsonObject();
		
		if (us != null) {
			responce.addProperty("rallyId", us.getRallyId());
			responce.addProperty("name", us.getName());
		} else {
			responce.addProperty("error", "User story not found!");
		}
		
		renderJSON(new Gson().toJson(responce));
	}

}