package controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import play.Play;
import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Controller;
import play.utils.Properties;

import com.google.gson.JsonObject;
import com.ning.http.util.Base64;

public class RallyService extends Controller {

	private final static String RALLY_URL = "https://rally1.rallydev.com/slm/webservice/v2.0/";

	public static void example(String id) {
		renderJSON(getInfo(id));
	}

	private static String getInfo(String formattedId) {
		String query = "null";
		JsonObject result = new JsonObject();
		
		if (formattedId.matches("^((us)|(US))[0-9]+$")) {
			query = "hierarchicalrequirement?fetch=true&query=(FormattedId = "
					+ formattedId + ")";
		}
		if (formattedId.matches("^((de)|(DE))[0-9]+$")) {
			query = "defect?fetch=true&query=(FormattedId = " + formattedId
					+ ")";
		}

		if (!"null".equals(query)) {
			WS.HttpResponse defect = sendRequest(RALLY_URL + query);

			try {
				JsonObject fullUS = defect.getJson().getAsJsonObject()
						.get("QueryResult").getAsJsonObject().get("Results")
						.getAsJsonArray().get(0).getAsJsonObject();

				result.addProperty("FormattedID", fullUS.get("FormattedID")
						.getAsString());
				result.addProperty("Name", fullUS.get("Name").getAsString());

			} catch (Exception ex) {
				result.addProperty("FormattedID", "Error");
				result.addProperty("Name", "Can't find user story or defect!!!");
			}
			
		} else {
			result.addProperty("FormattedID", "Error");
		}

		return result.toString();
	}

	private static WS.HttpResponse sendRequest(String url) {
		Properties prop = new Properties();

		// get username and password
		try {
			prop.load(Play.classloader.getResourceAsStream("pass.conf"));
			String user = prop.get("user");
			String pass = prop.get("password");

			String auth = "Basic "
					+ Base64.encode((user + ":" + pass).getBytes());

			WSRequest req = WS.url(url);
			req.setHeader("Authorization", auth);

			return req.get();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}