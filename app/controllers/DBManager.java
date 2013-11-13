package controllers;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;

import models.Team;
import models.Task;
import models.TeamTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import play.mvc.Controller;

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
		
		List<TeamTask> tasks = TeamTask.find("tmId="+ currentTeam.id +" order by updatedDate desc")
				.fetch();

		long DAY_IN_MS = 1000 * 60 * 60 * 24;
		Date twoWeeksAgo = new Date(System.currentTimeMillis()
				- (14 * DAY_IN_MS));

		JsonArray data = new JsonArray();
		JsonObject task = new JsonObject();

		if (tasks.size() > 0) {
			for (TeamTask t : tasks) {

				if (t.updatedDate.after(twoWeeksAgo)) {
					task.addProperty("id", t.id.task.taskId);
					task.addProperty("desc", t.id.task.description);
					data.add(task);
					task = new JsonObject();
				}
			}
		} else {
			task.addProperty("id", "-");
			task.addProperty("desc", "No userstories.");
			data.add(task);
		}

		renderJSON(gson.toJson(data));
	}

	public static void updateUserStory(String id, String desc, String team) {

		Task task = Task.findById(id);
		if (task == null) {
			task = new Task(id, desc);
		} else {
			task.description = desc;
		}
		task.save();

		Team currentTeam = Team.find("name", team).first();

		List<TeamTask> result = TeamTask.find(
				"taskId=\'" + id + "\' and tmId=" + currentTeam.id).fetch();

		TeamTask teamTask;
		if (result.size() == 0) {
			teamTask = new TeamTask();
			teamTask.id.team = Team.findById(currentTeam.id);
			teamTask.id.task = Task.findById(id);
		} else {
			teamTask = result.get(0);
			teamTask.updatedDate = new Date();
		}
		teamTask.save();
	}
}
