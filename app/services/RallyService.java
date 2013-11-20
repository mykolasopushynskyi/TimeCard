package services;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.libs.WS;
import play.libs.WS.WSRequest;
import play.utils.Properties;

import com.google.gson.JsonObject;
import com.ning.http.util.Base64;

public class RallyService {
	private final static String RALLY_URL = "https://rally1.rallydev.com/slm/webservice/v2.0/";

	public static UserStory getUserStoryInfo(String rallyId) {

		UserStory result = new UserStory();
		String query = buildUrl(rallyId);

		if (!StringUtils.isBlank(query)) {
			WS.HttpResponse defect = sendRequest(RALLY_URL + query);

			try {
				JsonObject fullUserStoryInfo = defect.getJson()
						.getAsJsonObject().get("QueryResult").getAsJsonObject()
						.get("Results").getAsJsonArray().get(0)
						.getAsJsonObject();

				result.setRallyId(fullUserStoryInfo.get("FormattedID")
						.getAsString());
				result.setName(fullUserStoryInfo.get("Name").getAsString());

			} catch (Exception ex) {
				result = null;
			}
		} else {
			result = null;
		}

		return result;
	}

	private static String buildUrl(String rallyId) {
		String query = null;
		if (isUserSoryId(rallyId)) {
			query = "hierarchicalrequirement?fetch=true&query=(FormattedId = "
					+ rallyId + ")";
		}
		if (isDefectId(rallyId)) {
			query = "defect?fetch=true&query=(FormattedId = " + rallyId + ")";
		}
		return query;
	}

	private static boolean isUserSoryId(String rallyId) {
		return rallyId.matches("^((us)|(US))[0-9]+$");
	}

	private static boolean isDefectId(String rallyId) {
		return rallyId.matches("^((de)|(DE))[0-9]+$");
	}

	private static WS.HttpResponse sendRequest(String url) {
		Properties prop = new Properties();

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
