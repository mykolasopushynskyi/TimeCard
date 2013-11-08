package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Controller;

import com.ning.http.util.Base64;

public class RallyService extends Controller {

	private final static String RALLY_URL = "https://rally1.rallydev.com/slm/webservice/v2.0/";
	private final static String USER = "ololo";
	private final static String PASSWORD = "ololo";

	private String password = PASSWORD;
	private String login = USER;
	private String url = RALLY_URL;

	// TODO dont delete
	public static void example() {
		WS.HttpResponse user = sendRequest(RALLY_URL + "user.js?fetch=true");

		String userId = user.getJson().getAsJsonObject().get("User")
				.getAsJsonObject().get("ObjectID").toString();

		String userLastName = user.getJson().getAsJsonObject().get("User")
				.getAsJsonObject().get("LastName").toString();

		WS.HttpResponse project = getProjectId(userId);

		String projectId = project.getJson().getAsJsonObject()
				.get("QueryResult").getAsJsonObject().get("Results")
				.getAsJsonArray().get(0).getAsJsonObject().get("ObjectID")
				.toString();

		WS.HttpResponse currentIteration = getCurrentIteration(projectId);

		String iterationId = currentIteration.getJson().getAsJsonObject()
				.get("QueryResult").getAsJsonObject().get("Results")
				.getAsJsonArray().get(0).getAsJsonObject().get("ObjectID")
				.toString();

		String iterationName = currentIteration.getJson().getAsJsonObject()
				.get("QueryResult").getAsJsonObject().get("Results")
				.getAsJsonArray().get(0).getAsJsonObject().get("Name")
				.toString();

		String iterationNumber;

		System.out.println("u" + userId);
		System.out.println("u" + userLastName);
		System.out.println("p" + projectId);
		System.out.println("i" + iterationId);
		System.out.println("i" + iterationName);

		for (String s : iterationName.split(" ")) {
			try {
				Integer.parseInt(s);
				iterationNumber = s;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		WS.HttpResponse userStories = getUserStrories(projectId, iterationId);

		renderJSON(userStories.getJson());

		// https://rally1.rallydev.com/slm/webservice/v2.0/artifact?key=61741e3c-df9b-4e9b-9b0d-7bd3abc065aa&query=(FormattedID%20=%20US6149)

		/*
		 * String auth = "Basic " + Base64.encode((USER + ":" +
		 * PASSWORD).getBytes());
		 * 
		 * WSRequest req = WS.url(RALLY_URL+
		 * "artifact?&query=(FormattedID%20=%20US6149)");
		 * req.setHeader("Authorization", auth);
		 * 
		 * renderJSON(req.get().getJson());
		 */
	}

	private static WS.HttpResponse getUserStoryList(String currentProjectId) {

		// WS.HttpResponse userStories = getUserStrories(currentProjectId);

		/*
		 * var list = new Array(); list.push(NO_STORY_STR); for (var i=0;
		 * i<userStories.length; i++) { list.push("" +
		 * userStories[i].FormattedID + " " + userStories[i].Name); }
		 * 
		 * return list;
		 */
		return null;
	};

	private static WS.HttpResponse getUserStrories(String currentProjectId,
			String currentIterationtId) {
		String query = "artifact?fetch=true&query=(Project.Objectid = "
				+ currentProjectId + ")";
		System.out.println(RALLY_URL + query);
		WS.HttpResponse userstories = sendRequest(RALLY_URL + query);
		return userstories;
	}

	private static WS.HttpResponse sendRequest(String url) {
		String auth = "Basic "
				+ Base64.encode((USER + ":" + PASSWORD).getBytes());

		WSRequest req = WS.url(url);
		req.setHeader("Authorization", auth);

		return req.get();
	}

	private static WS.HttpResponse getProjectId(String userId) {

		String query = "project?fetch=true&query=(TeamMembers.ObjectID = "
				+ userId + ")";
		System.out.println(RALLY_URL + query);
		WS.HttpResponse project = sendRequest(RALLY_URL + query);
		return project;
	};

	private static String getCurrentDate() {
		Date date = new Date();
		// date.
		return (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-"
				+ date.getDate() + "T00:00:00.000Z";
	};

	private static WS.HttpResponse getCurrentIteration(String projectId) {
		String query = "iteration?fetch=true&query=(((endDate >= "
				+ getCurrentDate() + ") and (startDate <= " + getCurrentDate()
				+ ")) and (project.objectid = " + projectId + "))";
		System.out.println(RALLY_URL + query);
		WS.HttpResponse iteration = sendRequest(RALLY_URL + query);
		return iteration;
	};

}