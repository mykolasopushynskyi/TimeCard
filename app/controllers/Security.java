package controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.results.Redirect;
import social.GoogleSocial;

import com.google.gson.JsonObject;

public class Security extends Controller{

	
	private static GoogleSocial socialGoogle = new GoogleSocial(session, redirectURL());
	
	public static void logout() {
		socialGoogle.logout();
		Application.index();
	}

	public static void authGoogle() {
		socialGoogle.loginToSocial();
	}

	public static void tryAuthGoogle() {
		socialGoogle.redirectToSocial();
	}

	static String redirectURL() {
		return play.mvc.Router.getFullUrl("Security.authGoogle");
	}


	static boolean isLogged() {
		return socialGoogle.isLogged();
	}

	static String getToken() {
		String result = session.get("token");
		
		if (StringUtils.isBlank(result)) {
			return null;
		} else {
			return result;
		}
	}
	
	static JsonObject getUserInfo() {
		String token = getToken();
		JsonObject userInfo = null;

		try {
			if (token != null) {
				userInfo = WS
						.url("https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s",
								getToken()).get().getJson().getAsJsonObject();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return userInfo;
	}
}
