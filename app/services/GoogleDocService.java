package services;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import play.Play;
import play.mvc.Scope.Params;
import play.utils.Properties;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.common.base.StringUtil;
import com.google.gson.JsonObject;

import controllers.Security;

public class GoogleDocService {

	private static final String SPREADSHEET_KEY = "0AsiyXCOmDCswdHNSUHlLNmtoMkZjdjVZWUJ3bUhMekE";
	private static final String WORKSHEET = "Polaris";

	private static final FeedURLFactory URL_FACTORY = FeedURLFactory
			.getDefault();

	// names of httpParametrs (duplicate ones from js of index page)
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
	// Also you need to create google spreadsheet with HEADER ROW labeled by
	// following captions
	public static final String[] SPREADSHEET_HEADERS = { "Iteration", "Team",
			"TimeStamp", "Username", "RallyId", "StoryName", "Hours", "Type" };
	
	public static final ReportDataValidation validator = new ReportDataValidation();

	public static String saveReport(Params HTTPparams) {

		JsonObject response = new JsonObject();

		boolean isValidHours;
		boolean isValidStoryTime;

		SpreadsheetService service = getSpreadsheetService();

		LinkedList<String> values = new LinkedList<String>();

		ListEntry row;
		String reportParamValue;

		for (int i = 0; i < REPORT_PARAMS.length; i++) {
			values.add(HTTPparams.get(REPORT_PARAMS[i]));
		}

		if (validator.isValidCredentials(HTTPparams.get("mail"))) {

			isValidHours = validator.validateReportHours(values);
			isValidStoryTime = validator.validateStoryTime(HTTPparams.get("storyTime"),
					HTTPparams.get("usId"), HTTPparams.get("usName"));
			
			if (isValidHours) {
				try {
					URL listRowsUrl = findWorksheet(service).getListFeedUrl();

					// add all parameters
					for (int i = 0; i < REPORT_PARAMS.length; i++) {
						reportParamValue = HTTPparams.get(REPORT_PARAMS[i]);
						if (!StringUtils.isBlank(reportParamValue)) {
							row = createRowEntry(HTTPparams, reportParamValue,
									REPORT_NAMES[i]);
							row = service.insert(listRowsUrl, row);
						}
					}

					// add story time parameter
					if (!StringUtils.isBlank(HTTPparams.get("storyTime"))) {
						if (isValidStoryTime) {
							row = createStoryTimeEntry(HTTPparams);
							row = service.insert(listRowsUrl, row);
						} else {
							response.addProperty("isError", true);
							response.addProperty("msg",
									"Can't save story time.");
							return response.toString();
						}
					}

				} catch (IOException | ServiceException e1) {
					//e1.printStackTrace();
					response.addProperty("isError", true);
					response.addProperty("msg",
							"Error during writing to google doc.");
					return response.toString();
				}
			} else {
				response.addProperty("isError", true);
				response.addProperty("msg", "Hours sum vslidation failed.");
				return response.toString();
			}
		} else {
			response.addProperty("isError", true);
			response.addProperty("msg", "Session expired or email is ivalid");
			return response.toString();
		}
		
		response.addProperty("isError", false);
		response.addProperty("msg", "Saving is successfull!");
		return response.toString();
	}
	
	
	

	private static String getDate() {
		Calendar calendar = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		return dateFormat.format((calendar.getTime()));
	}

	private static ListEntry createRowEntry(Params params, String hoursValue,
			String activityType) {
		ListEntry row = new ListEntry();

		row.getCustomElements().setValueLocal("Username", params.get("mail"));
		row.getCustomElements().setValueLocal("Team", params.get("team"));
		row.getCustomElements().setValueLocal("TimeStamp", getDate());
		row.getCustomElements().setValueLocal("Hours", hoursValue);
		row.getCustomElements().setValueLocal("Type", activityType);
		
		return row;
	}

	private static ListEntry createStoryTimeEntry(Params params) {
		ListEntry row = new ListEntry();

		row.getCustomElements().setValueLocal("Username", params.get("mail"));
		row.getCustomElements().setValueLocal("Team", params.get("team"));
		row.getCustomElements().setValueLocal("TimeStamp", getDate());
		row.getCustomElements().setValueLocal("Hours", params.get("storyTime"));
		row.getCustomElements().setValueLocal("RallyId", params.get("usId"));
		row.getCustomElements().setValueLocal("StoryName", params.get("usName"));
		row.getCustomElements().setValueLocal("Type", "Story Time");

		return row;
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

		SpreadsheetFeed feed = service.getFeed(
				URL_FACTORY.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
		for (SpreadsheetEntry se : feed.getEntries()) {
			if (se.getSpreadsheetLink().getHref().endsWith(SPREADSHEET_KEY)) {
				for (WorksheetEntry we : se.getWorksheets()) {
					if (we.getTitle().getPlainText()
							.equalsIgnoreCase(WORKSHEET)) {
						return we;
					}
				}
			}
		}

		throw new RuntimeException("Can't find worksheet= " + WORKSHEET);
	}
}
