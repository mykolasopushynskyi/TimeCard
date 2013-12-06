package controllers;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.results.Redirect;
import services.GoogleSocial;

import com.google.gson.JsonObject;


public class Security extends Controller {
	
	@Inject
	private static GoogleSocial socialGoogle;

	public static void logout() {
		socialGoogle.logout(getToken());
		Scope.Session.current().clear();
		Application.index();
	}

	public static void authGoogle() {
		String accessToken;
		String accessCode;
		JsonObject respObj;

		if (Scope.Params.current().get("code") != null) {
			accessCode = Scope.Params.current().get("code");
			respObj = socialGoogle.loginToSocial(accessCode);

			if (respObj.has("access_token")) {
				accessToken = respObj.get("access_token").getAsString();
				Scope.Session.current().put("token", accessToken);
			}

			Application.index();
		}
	}

	public static void tryAuthGoogle() {
		String redirectUrl = socialGoogle.getSocialRedirectUrl();
		
		throw new Redirect(redirectUrl);
	}

	public static boolean isLogged() {
		return socialGoogle.isLogged(getToken());
	}

	public static String getUserInfo() {
		return socialGoogle.getUserInfo(getToken());
	}
	
	public static String getToken() {
		String result = null;

		if (Scope.Session.current() != null) {
			result = Scope.Session.current().get("token");
		}

		if (StringUtils.isBlank(result)) {
			return null;
		} else {
			return result;
		}
	}
}
