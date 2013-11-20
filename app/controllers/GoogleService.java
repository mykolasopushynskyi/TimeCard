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
	private static final FeedURLFactory URL_FACTORY = FeedURLFactory
			.getDefault();

	// names of httpParametrs
	public final static String[] REPORT_PARAMS = { "rework", "maintenance",
			"unplannedActivity", "sickLeave", "vacation", "standUp",
			"retrospective", "projectPlanning", "demo", "estimates",
			"projectMeetings", "trainingAndDevelopment", "management" };

	public final static String[] REPORT_NAMES = { "Rework", "Maintenance",
			"Unplanned Activity", "Sick Leave", "Vacation", "Stand Up",
			"Retrospective", "Project Planning", "Demo", "Estimates",
			"Project Meetings", "Training And Development", "Management" };

	// names of headers in spreadsheet
	// don't use white spaces in this names and headers in spreadsheet
	public static final String[] SPREADSHEET_HEADERS = { "Iteration", "Team",
			"TimeStamp", "Username", "RallyId", "StoryName", "Hours", "Type" };

	private static boolean validateReportHours(LinkedList<String> values) {
		double sum = 0;
		boolean isValid = true;
		
		for (int i = 0; i < values.size(); i++) {
			if (!StringUtils.isBlank(values.get(i))) {
				if (NumberUtils.isNumber(values.get(i))) {
					sum +=  Double.parseDouble(values.get(i));
				} else {
					isValid = false;
					break;
				}
			}
		}

		if (sum <= 0 || sum > 24) {
			isValid = false;
		}
		return isValid;
	}

	private static boolean validateStoryTime(String storyTime, String usId,
			String usName) {

		boolean isValid = true;

		if (!StringUtils.isBlank(storyTime)) {
			if (usId.matches("(([uU][sS])|([dD][eE]))[0-9]+")) {
				try {
					if (Double.parseDouble(storyTime) <= 0
							|| Double.parseDouble(storyTime) > 24) {
						isValid = false;
					}
				} catch (NumberFormatException e) {
					isValid = false;
				}
			} else {
				isValid = false;
			}
		} else {
			isValid = false;
		}

		return isValid;
	}

	private static boolean validateMail(String mail) {
		boolean isValid = false;

		isValid = Security.getUserInfo().equals(mail);
		isValid = isValid
				&& mail.matches("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@globallogic.com");

		return isValid;
	}

	public static void report() {
		JsonObject responce = new JsonObject();

		if (Security.isLogged() && validateMail(params.get("mail"))) {

			boolean isValidHours = true;
			boolean isValidStoryTime = true;

			LinkedList<String> values = new LinkedList<String>();

			for (int i = 0; i < REPORT_PARAMS.length; i++) {
				values.add(params.get(REPORT_PARAMS[i]));
			}

			isValidHours = validateReportHours(values);
			isValidStoryTime = validateStoryTime(params.get("storyTime"),
					params.get("usId"), params.get("usName"));

			SpreadsheetService service = getSpreadsheetService();

			try {
				URL listFeedUrl;
				listFeedUrl = findWorksheet(service).getListFeedUrl();
				ListEntry row;
				String val;
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
				Calendar calendar = Calendar.getInstance();

				for (int i = 0; i < REPORT_PARAMS.length; i++) {
					val = params.get(REPORT_PARAMS[i]);
					if (!StringUtils.isBlank(val)) {

						row = new ListEntry();

						row.getCustomElements().setValueLocal("Username",
								params.get("mail"));

						row.getCustomElements().setValueLocal("TimeStamp",
								dateFormat.format(calendar.getTime()));
						row.getCustomElements().setValueLocal("Team",
								params.get("team"));

						row.getCustomElements().setValueLocal("Hours", val);
						row.getCustomElements().setValueLocal("Type",
								REPORT_NAMES[i]);

						row = service.insert(listFeedUrl, row);
					}
				}

				// add story time
				if (isValidStoryTime) {
					row = new ListEntry();

					row.getCustomElements().setValueLocal("Username",
							params.get("mail"));
					row.getCustomElements().setValueLocal("TimeStamp",
							dateFormat.format(calendar.getTime()));
					row.getCustomElements().setValueLocal("Team",
							params.get("team"));

					row.getCustomElements().setValueLocal("Hours",
							params.get("storyTime"));
					row.getCustomElements().setValueLocal("RallyId",
							params.get("usId"));
					row.getCustomElements().setValueLocal("StoryName",
							params.get("usName"));
					row.getCustomElements().setValueLocal("Type", "Story Time");
					row = service.insert(listFeedUrl, row);
				} else {
					responce.addProperty("msg", "Can't save story time.");
					renderJSON(responce.toString());
				}

			} catch (IOException | ServiceException e1) {
				responce.addProperty("msg",
						"Error during writing to google doc.");
				renderJSON(responce.toString());
				e1.printStackTrace();
			}

		} else {
			responce.addProperty("msg", "Session expired or email is ivalid");
			renderJSON(responce.toString());
		}

		responce.addProperty("msg", "Saving is successfull!");
		renderJSON(responce.toString());
	}

	private static SpreadsheetService getSpreadsheetService() {
		SpreadsheetService service = new SpreadsheetService("TimeCard");
		Properties prop = new Properties();

		try {
			prop.load(Play.classloader.getResourceAsStream("pass.conf"));
			String user = prop.get("googleacco");
			String pass = prop.get("googlepass");

			try {
				service.setUserCredentials(user, pass);
				service.setProtocolVersion(SpreadsheetService.Versions.V3);
			} catch (AuthenticationException e) {
				e.printStackTrace();
				service = null;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			service = null;
		}
		return service;
	}

	private static WorksheetEntry findWorksheet(SpreadsheetService service)
			throws IOException, ServiceException {

		String spreadsheetKey = "0AsiyXCOmDCswdHNSUHlLNmtoMkZjdjVZWUJ3bUhMekE";
		String worksheetName = "Polaris";

		SpreadsheetFeed feed = service.getFeed(
				URL_FACTORY.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
		for (SpreadsheetEntry se : feed.getEntries()) {
			if (se.getSpreadsheetLink().getHref().endsWith(spreadsheetKey)) {
				for (WorksheetEntry we : se.getWorksheets()) {
					if (we.getTitle().getPlainText()
							.equalsIgnoreCase(worksheetName)) {
						return we;
					}
				}
			}
		}
		throw new RuntimeException("Cannot find worksheet=" + worksheetName);
	}
}