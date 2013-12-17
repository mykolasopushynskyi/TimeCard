package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import services.GoogleDocService;

@With(Security.class)
public class Application extends Controller {

	public static void index() {
		
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Calendar calendar = Calendar.getInstance();
		
		if (Security.isLogged()) {
			String email = Security.getUserInfo();
			boolean isLogged = true;
			
			String support = (String) Play.configuration.getProperty("multimedia.support","false");
			boolean multimediaSupport = Boolean.parseBoolean(support);
			String date =  dateFormat.format(calendar.getTime());
			render( email, isLogged, multimediaSupport, date);
		}
		render();
	}
}