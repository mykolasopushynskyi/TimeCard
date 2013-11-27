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
import play.mvc.Scope;
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

	private static final String SPREADSHEET_KEY = (String) Play.configuration
			.getProperty("spreadsheet.key", "");
	private static final String WORKSHEET = (String) Play.configuration
			.getProperty("worksheet.name", "");

	// names of httpParametrs (duplicate ones in js of index page)
	private final static String[] REPORT_PARAMS = { "rework", "maintenance",
			"unplannedActivity", "sickLeave", "vacation", "standUp",
			"retrospective", "projectPlanning", "demo", "estimates",
			"projectMeetings", "trainingAndDevelopment", "management" };

	private final static String[] REPORT_NAMES = { "Rework", "Maintenance",
			"Unplanned Activity", "Sick Leave", "Vacation", "Stand Up",
			"Retrospective", "Project Planning", "Demo", "Estimates",
			"Project Meetings", "Training And Development", "Management" };

	// names of headers in spreadsheet
	// don't use white spaces in this names and headers names in spreadsheet
	// Also you need to create google spreadsheet with HEADER ROW labeled by the
	// following captions
	// { "Iteration", "Team", "TimeStamp", "Username", "RallyId", "StoryName",
	// "Hours", "Type" };

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private static FeedURLFactory urlFactory = FeedURLFactory.getDefault();
	private static ReportDataValidation validator = new ReportDataValidation();

	public static String saveReport(Params HTTPparams) {

		JsonObject response = new JsonObject();
		SpreadsheetService service = getSpreadsheetService();

		if (validator.isValidCredentials(HTTPparams.get("mail"))) {
			if (validator.validateFields(HTTPparams, REPORT_PARAMS)) {
				if (validator.validateReportHours(getValues(HTTPparams))) {
					try {
						response = save(HTTPparams, service);
					} catch (Exception ex) {
						ex.printStackTrace();
						response.addProperty("isError", true);
						response.addProperty("msg",
								"Error during writing to google doc.");
					}
				} else {
					response.addProperty("isError", true);
					response.addProperty("msg", "Hours sum vslidation failed.");
				}
			} else {
				response.addProperty("isError", true);
				response.addProperty("msg", "Not all fiels is valid.");
			}
		} else {
			response.addProperty("isError", true);
			response.addProperty("msg", "Session expired or email is ivalid");
		}
		return response.toString();
	}

	private static LinkedList<String> getValues(Params HTTPparams) {
		LinkedList<String> values = new LinkedList<String>();
		String val;

		for (int i = 0; i < REPORT_PARAMS.length; i++) {
			val = HTTPparams.get(REPORT_PARAMS[i]);
			if (validator.isNullNumber(val)) {
				values.add("");
			} else {
				values.add(val);
			}
		}
		return values;
	}

	private static JsonObject save(Params HTTPparams, SpreadsheetService service)
			throws IOException, ServiceException, NullPointerException {

		ListEntry row;
		String paramValue;
		String paramType;

		String storyTime = HTTPparams.get("storyTime");
		String storyId = HTTPparams.get("usId");
		String storyName = HTTPparams.get("usName");

		JsonObject response = new JsonObject();
		response.addProperty("isError", false);
		response.addProperty("msg", "Saving is successfull!");

		URL workSheetUrl = findWorksheet(service).getListFeedUrl();

		// add all parameters
		for (int i = 0; i < REPORT_PARAMS.length; i++) {
			paramValue = HTTPparams.get(REPORT_PARAMS[i]);
			paramType = REPORT_NAMES[i];
			if (!validator.isNullNumber(paramValue)) {
				row = createRowEntry(HTTPparams, paramValue, paramType);
				row = service.insert(workSheetUrl, row);
			}
		}

		// add story time parameter
		if (!StringUtils.isBlank(HTTPparams.get("storyTime"))) {
			if (validator.validateStoryTime(storyTime, storyId, storyName)) {
				row = createStoryTimeEntry(HTTPparams);
				row = service.insert(workSheetUrl, row);
			} else {
				response.addProperty("isError", true);
				response.addProperty("msg", "Can't save story time.");
			}
		}

		return response;
	}

	private static String getDate() {
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
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
		row.getCustomElements()
				.setValueLocal("StoryName", params.get("usName"));
		row.getCustomElements().setValueLocal("Type", "Story Time");

		return row;
	}
	
	private static SpreadsheetService getSpreadsheetService() {
		SpreadsheetService service = new SpreadsheetService("TimeCard");
		service.setHeader("Authorization", "Bearer " + Scope.Session.current().get("token"));
		service.setProtocolVersion(SpreadsheetService.Versions.V3);
		return service;
	}

	private static WorksheetEntry findWorksheet(SpreadsheetService service)
			throws IOException, ServiceException {

		SpreadsheetFeed feed = service.getFeed(
				urlFactory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
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

		return null;
	}
}
