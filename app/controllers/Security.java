package controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import models.User;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.results.Redirect;

import com.google.gdata.util.common.base.StringUtil;
import com.google.gson.JsonObject;

public class Security extends Controller/* extends Secure.Security */{

	public static final String authURL = "https://accounts.google.com/o/oauth2/auth";
	public static final String tokenURL = "https://accounts.google.com/o/oauth2/token";
	public static final String clientId = "752848984220.apps.googleusercontent.com";
	public static final String clientSecret = "xrbD2KH0MWHKxgj2mn2ktlUJ";

	public static void logout() {
		WS.url("https://accounts.google.com/o/oauth2/revoke?token=%s",
				getToken()).post();
		session.clear();
		Application.index();
	}

	public static void authGoogle() {
		if (Scope.Params.current().get("code") != null) {
			String accessCode = Scope.Params.current().get("code");
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("client_id", clientId);
			params.put("client_secret", clientSecret);
			params.put("redirect_uri", redirectURL());
			params.put("code", accessCode);
			params.put("grant_type", "authorization_code");
			WS.HttpResponse response = WS.url(tokenURL).params(params).post();

			String accessToken = null;

			JsonObject respObj = (JsonObject) response.getJson();

			if (respObj.has("access_token")) {
				accessToken = respObj.get("access_token").getAsString();
				session.put("token", accessToken);
			}

			Application.index();
		}
	}

	public static void tryAuthGoogle() {
		throw new Redirect(authURL + "?client_id=" + clientId
				+ "&redirect_uri=" + redirectURL() + "&response_type=code"
				+ "&scope=https://www.googleapis.com/auth/userinfo.email");
	}

	static String redirectURL() {
		return play.mvc.Router.getFullUrl("Security.authGoogle");
	}

	static String getToken() {
		String result = session.get("token");

		if (StringUtils.isBlank(result)) {
			return null;
		} else {
			return result;
		}
	}

	static boolean isLogged() {
		String token = getToken();
		JsonObject info = null;
		boolean isLogged = false;

		try {
			if (token != null) {
				if(getUserInfo() != null){
					isLogged = true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isLogged;
	}

	static JsonObject getUserInfo() {
		String token = getToken();
		JsonObject userInfo = null;

		try {
			if (token != null) {
				userInfo = WS.url(
						"https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s",
						getToken()).get().getJson().getAsJsonObject();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return userInfo;
	}

}
