package social;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonObject;

import play.Play;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.results.Redirect;
import play.utils.Properties;
import controllers.Application;

public class GoogleSocial implements Social {

	private String redirect;
	
	public GoogleSocial(String redirect) {
		this.redirect = redirect;
	}

	public static final String AUTH_URL = (String) Play.configuration.getProperty("auth.url", "");
	public static final String TOKEN_URL = (String) Play.configuration.getProperty("token.url", "");
	public static final String CLIENT_ID = (String) Play.configuration.getProperty("client.id", "");
	public static final String CLIENT_SECRET = (String) Play.configuration.getProperty("client.secret", "");

	@Override
	public void redirectToSocial() {
		throw new Redirect(AUTH_URL + "?client_id=" + CLIENT_ID
				+ "&redirect_uri=" + redirect + "&response_type=code"
				+ "&scope=https://www.googleapis.com/auth/userinfo.email");
	}

	@Override
	public void loginToSocial() {
		if (Scope.Params.current().get("code") != null) {
			String accessCode = Scope.Params.current().get("code");
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("client_id", CLIENT_ID);
			params.put("client_secret", CLIENT_SECRET);
			params.put("redirect_uri", redirect);
			params.put("code", accessCode);
			params.put("grant_type", "authorization_code");
			WS.HttpResponse response = WS.url(TOKEN_URL).params(params).post();

			String accessToken = null;

			JsonObject respObj = (JsonObject) response.getJson();

			if (respObj.has("access_token")) {
				accessToken = respObj.get("access_token").getAsString();
				Scope.Session.current().put("token", accessToken);
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
		Scope.Session.current().clear();
	}

	private String getToken() {
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
	
	public String getUserInfo() {
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

		return userInfo.get("email").getAsString();
	}
}
