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
import play.utils.Properties;
import services.GoogleDocService;

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
		
		String responce = service.saveReport(params);
		
		renderJSON(responce);
		
	}
}