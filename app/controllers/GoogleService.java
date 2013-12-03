package controllers;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.Controller;
import play.mvc.Http.StatusCode;
import play.utils.Properties;
import services.GoogleDocService;
import services.Responce;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GoogleService extends Controller {

	public static final GoogleDocService service = new GoogleDocService();

	public static void report() {
		Responce saveResponse = new Responce() ;

		if (Security.isLogged()) {
			try {
				saveResponse = service.saveReport(params);
				response.status = saveResponse.getStatusCode();
			} catch (IOException e) {
				response.status = 503;
				saveResponse.setMessage("Server: Error during writing to google doc.");
			} catch (ServiceException e) {
				response.status = 503;
				saveResponse.setMessage("Server: Error during writing to google doc.");
				e.printStackTrace();
			}
		} else {
			saveResponse.setMessage("Server: User is not logged.");
			response.status = 403;
		}
		
		renderText(saveResponse.getMessage());
	}
}