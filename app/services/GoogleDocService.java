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

	public static Responce saveReport(Params HTTPparams) throws IOException,
			ServiceException {

		SpreadsheetService service = getSpreadsheetService();
		Responce saveResponse = new Responce();
		
		String responseText;
		int responseCode;
		boolean saveStoryTine;

		if (validator.isValidMail(HTTPparams.get("mail"))) {
			if (validator.validateFields(HTTPparams, REPORT_PARAMS)) {
				if (validator.validateReportHours(getValues(HTTPparams))) {
					
					if (!StringUtils.isBlank(HTTPparams.get("storyTime"))) {
						if (validator.validateStoryTime(
								HTTPparams.get("storyTime"),
								HTTPparams.get("usId"),
								HTTPparams.get("usName"))) {
							saveStoryTine = true;
							responseText = save(HTTPparams, service, saveStoryTine);
							responseCode = 200;
						} else {
							responseText = "Server: Can't save story time";
							responseCode = 400;
						}
					} else {
						saveStoryTine = false;
						responseText = save(HTTPparams, service, saveStoryTine);
						responseCode = 200;
					}
					
				} else {
					responseText = "Server: Hours sum validation failed.";
					responseCode = 400;
				}
			} else {
				responseText = "Server: Not all fiels is valid.";
				responseCode = 400;
			}
		} else {
			responseText = "Server: Email is invalid";
			responseCode = 400;
		}
		saveResponse.setStatusCode(responseCode);
		saveResponse.setMessage(responseText);
		return saveResponse;
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

	private static String save(Params HTTPparams, SpreadsheetService service,
			boolean saveStoryTime) throws IOException, ServiceException {

		ListEntry row;
		String paramValue;
		String paramType;

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
		if (saveStoryTime) {
			row = createStoryTimeEntry(HTTPparams);
			row = service.insert(workSheetUrl, row);
		}

		return "Saving is successful!";
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
		service.setHeader("Authorization", "Bearer "
				+ Scope.Session.current().get("token"));
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
