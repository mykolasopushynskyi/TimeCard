package controllers;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;

import jj.play.ns.com.jhlabs.image.ErodeFilter;
import models.Task;
import models.Team;
import models.TeamTask;
import play.data.validation.Required;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DBManager extends Controller {

	private static final String TEAM_NOT_FOUD_MESSAGE = "Team not found";
	
	public static void getTeamList() {
		List<Team> teams = Team.find("order by name asc").fetch();
		List<String> data = new LinkedList<String>();

		for (Team t : teams) {
			data.add(t.name);
		}
		
		renderJSON(new Gson().toJson(data));
	}

	public static void getUserStories(String teamName) {
		Team team = Team.find("name", teamName).first();
		throw404IfNull(team, TEAM_NOT_FOUD_MESSAGE);
		
		Date twoWeeksAgo = new Date(new Date().getTime() - TimeUnit.MILLISECONDS.convert(14, TimeUnit.DAYS));
		List<TeamTask> tasks = TeamTask.find("id.team.id = ? and updatedDate > ? order by updatedDate desc", team.id, twoWeeksAgo).fetch();

		JsonArray data = toJsonArray(tasks);
		addTaskToJsonArray("-", "No userstories.", data);

		renderJSON(new Gson().toJson(data));
	}

	private static JsonArray toJsonArray(List<TeamTask> tasks) {
		JsonArray array = new JsonArray();
		if (tasks != null) {
			for (TeamTask teamTask : tasks) {
				addTaskToJsonArray(teamTask.id.task.id, teamTask.id.task.description, array);
			}
		}
		
		return array;
	}
	
	private static void addTaskToJsonArray(String id, String description, JsonArray array) {
		JsonObject task = new JsonObject();
		task.addProperty("id", id);
		task.addProperty("desc", description);
		array.add(task);
	}

	public static void updateUserStory(@Required String taskId, @Required String description, @Required String teamName) {
		Team team = Team.find("name", teamName).first();
		throw404IfNull(team, TEAM_NOT_FOUD_MESSAGE);
		
		Task task = getOrCreateTask(taskId);
		task.description = description;

		TeamTask teamTask = getOrCreateTeamTask(team, task);
		teamTask.updatedDate = new Date();
		
		task.save();
		teamTask.save();
	}
	
	private static <T> void throw404IfNull(T entity, String errorMessage) {
		if (entity == null) {
			error(404, errorMessage);
		}
	}
	
	private static Task getOrCreateTask(String id) {
		Task task = Task.findById(id);
		return task == null ? new Task(id, null) : task;
	}
	
	private static TeamTask getOrCreateTeamTask(Team team, Task task) {
		TeamTask teamTask = TeamTask.find("id.task.id = ? and id.team.id = ?", task.id, team.id).first();
		return teamTask == null ? new TeamTask(team, task) : teamTask;
	}
}
