package services;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.api.client.http.HttpResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.Play;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.results.Redirect;
import play.utils.Properties;
import controllers.Application;

public class GoogleSocial /* implements Social */{

	private String redirect;

	public GoogleSocial() {
		this.redirect = play.mvc.Router.getFullUrl("Security.authGoogle");
	}

	public static final String AUTH_URL = (String) Play.configuration
			.getProperty("auth.url", "");
	public static final String TOKEN_URL = (String) Play.configuration
			.getProperty("token.url", "");
	public static final String CLIENT_ID = (String) Play.configuration
			.getProperty("client.id", "");
	public static final String CLIENT_SECRET = (String) Play.configuration
			.getProperty("client.secret", "");

	public String getSocialRedirectUrl() {
		return AUTH_URL
				+ "?client_id="
				+ CLIENT_ID
				+ "&redirect_uri="
				+ redirect
				+ "&response_type=code"
				+ "&scope=https://www.googleapis.com/auth/userinfo.email https://spreadsheets.google.com/feeds/";
	}

	public JsonObject loginToSocial(String accessCode) {
		JsonObject respObj = null;

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("client_id", CLIENT_ID);
		params.put("client_secret", CLIENT_SECRET);
		params.put("redirect_uri", redirect);
		params.put("code", accessCode);
		params.put("grant_type", "authorization_code");
		WS.HttpResponse response = WS.url(TOKEN_URL).params(params).post();

		respObj = (JsonObject) response.getJson();

		return respObj;
	}

	public boolean isLogged(String token) {
		boolean isLogged = false;

		try {
			if (token != null) {
				JsonElement mail = 
						WS.url("https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s",
						token).get().getJson().getAsJsonObject().get("email");
				if (mail != null) {
					isLogged = true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isLogged;
	}

	public void logout(String token) {
		if (token != null) {
			WS.url("https://accounts.google.com/o/oauth2/revoke?token=%s",
					token).post();
		}
	}

	public String getUserInfo(String token) {
		JsonObject userInfo = null;
		try {
			if (token != null) {
				userInfo = WS
						.url("https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s",
								token).get().getJson().getAsJsonObject();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return userInfo.get("email").getAsString();
	}
}
