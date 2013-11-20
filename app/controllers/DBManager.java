package controllers;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import models.Task;
import models.Team;
import models.TeamTask;
import play.data.validation.Required;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DBManager extends Controller {

	public static void getTeamList() {
		Gson gson = new Gson();

		List<Team> teams = Team.find("order by name asc").fetch();
		List<String> data = new LinkedList<String>();

		for (Team t : teams) {
			data.add(t.name);
		}

		renderJSON(gson.toJson(data));
	}

	public static void getUserStories(String team) {
		Gson gson = new Gson();
		Team currentTeam = Team.find("name", team).first();

		List<TeamTask> tasks = TeamTask.find("id.team.id", currentTeam.id).fetch();

		long DAY_IN_MS = 1000 * 60 * 60 * 24;
		Date twoWeeksAgo = new Date(System.currentTimeMillis()
				- (14 * DAY_IN_MS));

		JsonArray data = new JsonArray();
		JsonObject task = new JsonObject();

		for (TeamTask t : tasks) {

			if (t.updatedDate.after(twoWeeksAgo)) {
				task = new JsonObject();
				task.addProperty("id", t.id.task.id);
				task.addProperty("desc", t.id.task.description);
				data.add(task);
			}
		}

		task = new JsonObject();
		task.addProperty("id", "-");
		task.addProperty("desc", "No userstories.");
		data.add(task);

		renderJSON(gson.toJson(data));
	}

	public static void updateUserStory(@Required String taskId, @Required String description, @Required String teamName) {
		Team team = Team.find("name", teamName).first();
		if (team == null) {
			error(404, "Team not found");
		}
		
		Task task = getOrCreateTask(taskId);
		task.description = description;

		TeamTask teamTask = getOrCreateTeamTask(team, task);
		teamTask.updatedDate = new Date();
		
		task.save();
		teamTask.save();
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
