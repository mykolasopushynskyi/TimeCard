package controllers;

import com.google.gson.JsonObject;
import models.User;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.results.Redirect;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: mykola.sopushynskyi Date: 11/1/13 Time:
 * 2:17 PM To change this template use File | Settings | File Templates.
 */
public class Security extends Controller/* extends Secure.Security */{

	public static final String authURL = "https://accounts.google.com/o/oauth2/auth";
	public static final String tokenURL = "https://accounts.google.com/o/oauth2/token";
	public static final String clientId = "752848984220.apps.googleusercontent.com";
	public static final String clientSecret = "xrbD2KH0MWHKxgj2mn2ktlUJ";

	public static void logout() {
		WS.url("https://accounts.google.com/o/oauth2/revoke?token=%s",
				WS.encode(connectedUser().access_token)).post();
		session.clear();
		Application.index();
	}

	
	public static void authGoogle() {
		if (Scope.Params.current().get("code") != null) {
			User u = connectedUser();

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
				u.access_token = accessToken;
			}

			u.save();
			Application.index();
		}
	}
	

	
	public static void tryAuthGoogle() {
		throw new Redirect(authURL + "?client_id=" + clientId
				+ "&redirect_uri=" + redirectURL() + "&response_type=code"
				+ "&scope=https://www.googleapis.com/auth/userinfo.email");
	}

	@Before
	static void setuser() {
		User user = null;
		if (session.contains("uid")) {
			user = User.get(Long.parseLong(session.get("uid")));
		}
		if (user == null) {
			user = User.createNew();
			session.put("uid", user.uid);
		}
		renderArgs.put("user", user);
	}

	static String redirectURL() {
		return play.mvc.Router.getFullUrl("Security.authGoogle");
	}
	
	static User connectedUser() {
		return (User) renderArgs.get("user");
	}
}
