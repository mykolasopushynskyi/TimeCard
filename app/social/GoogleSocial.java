package social;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonObject;

import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.results.Redirect;
import controllers.Application;

public class GoogleSocial implements Social {

	private String redirect;
	private Scope.Session session;
	
	public GoogleSocial(Scope.Session session, String redirect) {
		this.redirect = redirect;
		this.session = session;
	}

	public static final String authURL = "https://accounts.google.com/o/oauth2/auth";
	public static final String tokenURL = "https://accounts.google.com/o/oauth2/token";
	public static final String clientId = "752848984220.apps.googleusercontent.com";
	public static final String clientSecret = "xrbD2KH0MWHKxgj2mn2ktlUJ";

	@Override
	public void redirectToSocial() {
		throw new Redirect(authURL + "?client_id=" + clientId
				+ "&redirect_uri=" + redirect + "&response_type=code"
				+ "&scope=https://www.googleapis.com/auth/userinfo.email");
	}

	@Override
	public void loginToSocial() {
		if (Scope.Params.current().get("code") != null) {
			String accessCode = Scope.Params.current().get("code");
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("client_id", clientId);
			params.put("client_secret", clientSecret);
			params.put("redirect_uri", redirect);
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

	@Override
	public boolean isLogged() {
		String token = getToken();
		boolean isLogged = false;

		try {
			if (token != null) {
				if (WS.url(
						"https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s",
						getToken()).get().getJson().getAsJsonObject()
						.get("email").getAsString() != null) {
					isLogged = true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isLogged;
	}

	@Override
	public void logout() {
		if (getToken() != null) {
			WS.url("https://accounts.google.com/o/oauth2/revoke?token=%s",
					getToken()).post();
		}
		session.clear();
	}

	private String getToken() {
		String result = session.get("token");

		if (StringUtils.isBlank(result)) {
			return null;
		} else {
			return result;
		}
	}
}
