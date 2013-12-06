package services;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import validation.ReportFormBean;
import validation.ReportFormValidator;
import validation.ValidationResult;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

// names of headers in spreadsheet
// don't use white spaces in this names and headers names in spreadsheet
// Also you need to create google spreadsheet with HEADER ROW labeled by the
// following captions
// { "Iteration", "Team", "TimeStamp", "Username", "RallyId", "StoryName",
// "Hours", "Type" };

public class GoogleDocService {

	private static final String SPREADSHEET_KEY = (String) Play.configuration
			.getProperty("spreadsheet.key", "");
	private static final String WORKSHEET = (String) Play.configuration
			.getProperty("worksheet.name", "");

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private static FeedURLFactory urlFactory = FeedURLFactory.getDefault();
	private static SpreadsheetService service;

	public static ValidationResult saveReport(ReportFormBean report, String token)
			throws IOException, ServiceException {

		boolean saveStoryTime;
		ValidationResult saveResponse;
		ReportFormValidator validator;

		validator = new ReportFormValidator();

		saveStoryTime = validator.saveStoryTime(report);
		saveResponse = validator.validateFormBean(report);

		if (!saveResponse.hasErrors()) {
			service = getSpreadsheetService(token);
			save(report, saveStoryTime);
		}

		return saveResponse;
	}

	private static void save(ReportFormBean report, boolean saveStoryTime)
			throws IOException, ServiceException {

		ListEntry row;
		URL workSheetUrl = findWorksheet(service).getListFeedUrl();

		// save all parameters
		saveRowEntry(workSheetUrl, report, report.rework, "Rework");
		saveRowEntry(workSheetUrl, report, report.maintenance, "Maintenance");
		saveRowEntry(workSheetUrl, report, report.unplannedActivity,
				"Unplanned Activity");
		saveRowEntry(workSheetUrl, report, report.sickLeave, "Sick Leave");
		saveRowEntry(workSheetUrl, report, report.vacation, "Vacation");
		saveRowEntry(workSheetUrl, report, report.standUp, "Stand Up");
		saveRowEntry(workSheetUrl, report, report.retrospective,
				"Retrospective");
		saveRowEntry(workSheetUrl, report, report.projectPlanning,
				"Project Planning");
		saveRowEntry(workSheetUrl, report, report.demo, "Demo");
		saveRowEntry(workSheetUrl, report, report.estimates, "Estimates");
		saveRowEntry(workSheetUrl, report, report.projectMeetings,
				"Project Meetings");
		saveRowEntry(workSheetUrl, report, report.trainingAndDevelopment,
				"Training and development");
		saveRowEntry(workSheetUrl, report, report.management, "Management");

		// save story time parameter
		if (saveStoryTime) {
			row = createStoryTimeEntry(report);
			row = service.insert(workSheetUrl, row);
		}
	}

	private static String getDate() {
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}

	private static ListEntry createRowEntry(ReportFormBean report,
			String hoursValue, String activityType) {
		ListEntry row = new ListEntry();

		row.getCustomElements().setValueLocal("Username", report.mail);
		row.getCustomElements().setValueLocal("Team", report.team);
		row.getCustomElements().setValueLocal("TimeStamp", getDate());
		row.getCustomElements().setValueLocal("Hours", hoursValue);
		row.getCustomElements().setValueLocal("Type", activityType);

		return row;
	}

	private static ListEntry createStoryTimeEntry(ReportFormBean report) {
		ListEntry row = new ListEntry();

		row.getCustomElements().setValueLocal("Username", report.mail);
		row.getCustomElements().setValueLocal("Team", report.team);
		row.getCustomElements().setValueLocal("TimeStamp", getDate());
		row.getCustomElements().setValueLocal("Hours", report.storyTime);
		row.getCustomElements().setValueLocal("RallyId", report.usId);
		row.getCustomElements().setValueLocal("StoryName", report.usName);
		row.getCustomElements().setValueLocal("Type", "Story Time");

		return row;
	}

	private static SpreadsheetService getSpreadsheetService(String token) {
		SpreadsheetService service = new SpreadsheetService("TimeCard");
		service.setHeader("Authorization", "Bearer " + token);
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

	private static boolean saveRowEntry(URL workSheetUrl,
			ReportFormBean report, String hoursValue, String activityType)
			throws IOException, ServiceException {

		ListEntry row;
		if (!StringUtils.isBlank(hoursValue)) {
			row = createRowEntry(report, hoursValue, activityType);
			service.insert(workSheetUrl, row);
			return true;
		} else {
			return false;
		}
	}

}
